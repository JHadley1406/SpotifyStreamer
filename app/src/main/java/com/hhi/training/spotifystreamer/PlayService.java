package com.hhi.training.spotifystreamer;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.IBinder;
import android.os.PowerManager;
import android.util.Log;
import android.widget.Toast;

import java.io.IOException;
import java.util.ArrayList;

public class PlayService extends Service implements MediaPlayer.OnPreparedListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnCompletionListener{

    public final String LOG_TAG = PlayService.class.getSimpleName();

    private ArrayList<SongData> mTracks = null;

    private int mTrackNumber;
    private int mTrackPosition;

    private MediaPlayer mPlayer = null;

    private boolean mIsPaused;

    private Intent mOutgoingIntent;
    private IntentFilter mIntentFilter;

    private Handler seekBarHandler = new Handler();

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BroadcastContract.MSG_GET_MUSIC_SERVICE)){
                mTracks = intent.getParcelableArrayListExtra(IntentContract.TRACKS);
                mTrackNumber = intent.getIntExtra(IntentContract.TRACK_NUMBER, 0);
                assignSong();
                playSong();
            } else if(intent.getAction().equals(BroadcastContract.MSG_PLAY_TRACK)){
                playSong();
            } else if(intent.getAction().equals(BroadcastContract.MSG_PAUSE_TRACK)){
                Log.i(LOG_TAG, "Received Track paused call");
                pauseSong();
            } else if(intent.getAction().equals(BroadcastContract.MSG_NEXT_TRACK)) {
                incSong();
                assignSong();
                playSong();
            } else if(intent.getAction().equals(BroadcastContract.MSG_PREV_TRACK)){
                decSong();
                assignSong();
                playSong();
            } else if(intent.getAction().equals(BroadcastContract.MSG_SEEK_TO)){
                setSeek(intent.getIntExtra(IntentContract.TRACK_POSITION, 0));
                mOutgoingIntent.setAction(BroadcastContract.MSG_SEND_POS);
                mOutgoingIntent.putExtra(IntentContract.TRACK_POSITION
                        , intent.getIntExtra(IntentContract.TRACK_POSITION, 0));
                sendBroadcast(mOutgoingIntent);
            } else if(intent.getAction().equals(BroadcastContract.MSG_SEND_TRACK)){
                Log.i(LOG_TAG, "In broadcast receiver, mIsPaused is: " + mIsPaused);
                mOutgoingIntent.setAction(BroadcastContract.MSG_GET_MUSIC_CONTROL);
                mOutgoingIntent.putExtra(IntentContract.TRACK, mTracks.get(mTrackNumber));
                mOutgoingIntent.putExtra(IntentContract.TRACK_POSITION, mTrackPosition);
                mOutgoingIntent.putExtra(IntentContract.TRACK_DURATION, getDuration());
                mOutgoingIntent.putExtra(IntentContract.IS_PAUSED, mIsPaused);
                sendBroadcast(mOutgoingIntent);
            }
        }
    };


    public PlayService() {
    }

    public void onCreate(){
        super.onCreate();
        mTrackPosition = 0;
        initMusicPlayer();
        mOutgoingIntent = new Intent();
        mIntentFilter = BroadcastContract.getServiceFilter();
        registerReceiver(mReceiver, mIntentFilter);

        mOutgoingIntent.setAction(BroadcastContract.MSG_NEED_MUSIC);
        sendBroadcast(mOutgoingIntent);

    }

    @Override
    public  int onStartCommand(Intent intent, int flags, int startId){

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        unregisterReceiver(mReceiver);
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    @Override
    public void onCompletion(MediaPlayer mp) {                  //need to send Track Finished message
        if(mTrackNumber < mTracks.size()-1) {
            incSong();
            assignSong();
            playSong();
        } else {
            mOutgoingIntent.setAction(BroadcastContract.MSG_PAUSE_TRACK);
            sendBroadcast(mOutgoingIntent);
        }
    }

    @Override
    public boolean onError(MediaPlayer mp, int what, int extra) {
        mp.reset();
        Log.e(LOG_TAG, "An error occured, Error number " + what);
        Toast.makeText(getApplicationContext(), "Error " + what + " Occured", Toast.LENGTH_LONG).show();
        return false;
    }

    @Override
    public void onPrepared(MediaPlayer mp) {
        mp.start();
        mIsPaused = false;
        mOutgoingIntent.setAction(BroadcastContract.MSG_ASYNC_READY);
        mOutgoingIntent.putExtra(IntentContract.TRACK_DURATION, getDuration());
        mOutgoingIntent.putExtra(IntentContract.TRACK, mTracks.get(mTrackNumber));
        mOutgoingIntent.putExtra(IntentContract.IS_PAUSED, mIsPaused);
        sendBroadcast(mOutgoingIntent);
        startPlayProgressUpdate();

    }

    public void initMusicPlayer(){
        mPlayer = new MediaPlayer();
        mPlayer.setWakeMode(getApplicationContext(),
                PowerManager.PARTIAL_WAKE_LOCK);
        mPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        mPlayer.setOnPreparedListener(this);
        mPlayer.setOnCompletionListener(this);
        mPlayer.setOnErrorListener(this);
    }


    public void playSong(){
        if(mIsPaused){
            mPlayer.start();
        }
        else if (!isPlaying()){
            mPlayer.prepareAsync();
        }
        startPlayProgressUpdate();
        mIsPaused = false;
        mOutgoingIntent.putExtra(IntentContract.IS_PAUSED, mIsPaused);
        mOutgoingIntent.setAction(BroadcastContract.MSG_TRACK_PLAYING);
        sendBroadcast(mOutgoingIntent);
    }

    private void startPlayProgressUpdate(){
        if(isPlaying()){
            Runnable notification = new Runnable(){
                public void run(){
                    startPlayProgressUpdate();
                    mTrackPosition = mPlayer.getCurrentPosition();
                    mOutgoingIntent.setAction(BroadcastContract.MSG_SEND_POS);
                    mOutgoingIntent.putExtra(IntentContract.TRACK_POSITION, mTrackPosition);
                    sendBroadcast(mOutgoingIntent);
                }
            };

            seekBarHandler.postDelayed(notification, 500);
        }

    }

    private void assignSong(){
        if(mPlayer.isPlaying()){
            mPlayer.stop();
        }
        mPlayer.reset();
        mIsPaused = false;
        try {
            mPlayer.setDataSource(mTracks.get(mTrackNumber).getSongPreview());
        } catch (IOException e) {
            Log.e(LOG_TAG, "Error Fetching Song: ", e);
            Toast.makeText(getApplicationContext(), "Couldn't play song", Toast.LENGTH_SHORT).show();
        }
    }


    public void pauseSong(){
        if(mPlayer.isPlaying()) {
            mPlayer.pause();
            mIsPaused = true;
            mOutgoingIntent.setAction(BroadcastContract.MSG_TRACK_PAUSED);
            mOutgoingIntent.putExtra(IntentContract.IS_PAUSED, mIsPaused);
            sendBroadcast(mOutgoingIntent);
        }
    }

    public int getDuration(){
        return mPlayer.getDuration();
    }

    public boolean isPlaying(){
        return mPlayer.isPlaying();
    }

    public void setSeek(int position){
        mPlayer.seekTo(position);
    }

    private void incSong(){
        if(mTrackNumber < mTracks.size()-1) {
            mTrackNumber++;
        }
    }

    private void decSong(){
        if(mTrackNumber > 0) {
            mTrackNumber--;
        }
    }




}
