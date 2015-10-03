package com.hhi.training.spotifystreamer;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.RemoteViews;

import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;



/**
 * Created by Josiah Hadley on 9/25/2015.
 */
public class MediaNotification {

    private Context mContext;
    private NotificationManager mNoteManager;
    private NotificationCompat.Builder mNoteBuilder;
    private Target mTarget;

    private RemoteViews mRemoteViews;
    private final int NOTIFY_ID = 2;
    private SongData mTrack;
    private String mTrackName;
    private IntentFilter mIntentFilter;
    private boolean mPlaybackPaused;

    public final String LOG_TAG = MediaNotification.class.getSimpleName();


    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BroadcastContract.MSG_ASYNC_READY)){
                mPlaybackPaused = false;
                mRemoteViews.setImageViewResource(R.id.note_play_pause, android.R.drawable.ic_media_pause);
                mTrack = intent.getParcelableExtra(IntentContract.TRACK);
                setPlayPauseListener();
                buildNotification();
                updateNotification();
            }  else if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PLAYING)){
                mRemoteViews.setImageViewResource(R.id.note_play_pause, android.R.drawable.ic_media_pause);
                mPlaybackPaused = false;
                setPlayPauseListener();
                updateNotification();
            } else if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PAUSED)){
                mRemoteViews.setImageViewResource(R.id.note_play_pause, android.R.drawable.ic_media_play);
                mPlaybackPaused = true;
                setPlayPauseListener();
                updateNotification();
            }
        }
    };

    public MediaNotification(Context context, SongData track){
        this.mContext = context;
        this.mTrack = track;

        mTarget = new Target(){
            @Override
            public void onBitmapLoaded(Bitmap bitmap, Picasso.LoadedFrom from){
                mNoteBuilder.setLargeIcon(bitmap);
            }

            @Override
            public void onBitmapFailed(Drawable errorDrawable){}

            @Override
            public void onPrepareLoad(Drawable placeHolderDrawable){}
        };

        mRemoteViews = new RemoteViews(mContext.getPackageName(), R.layout.notification_player);
        mNoteManager = (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        buildNotification();
        setListeners();
        updateNotification();

        mIntentFilter = BroadcastContract.getNotificationFilter();
        mContext.registerReceiver(mReceiver, mIntentFilter);
    }


    private void setListeners() {
        Intent nextTrack = new Intent();
        nextTrack.setAction(BroadcastContract.MSG_NEXT_TRACK);
        PendingIntent nextTrackIntent = PendingIntent.getBroadcast(mContext
                , IntentContract.NOTE_NEXTTRACK
                , nextTrack, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.note_next_track, nextTrackIntent);

        Intent prevTrack = new Intent();
        prevTrack.setAction(BroadcastContract.MSG_PREV_TRACK);
        PendingIntent prevTrackIntent = PendingIntent.getBroadcast(mContext
                , IntentContract.NOTE_PREVTRACK
                , prevTrack, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.note_prev_track, prevTrackIntent);
        setPlayPauseListener();

    }

    private void setPlayPauseListener() {
        Intent playPause = new Intent();
        if(mPlaybackPaused){
            playPause.setAction(BroadcastContract.MSG_PLAY_TRACK);
        }else{
            playPause.setAction(BroadcastContract.MSG_PAUSE_TRACK);
        }
        PendingIntent playPauseIntent = PendingIntent.getBroadcast(mContext
                , IntentContract.NOTE_PLAYPAUSE
                , playPause, PendingIntent.FLAG_UPDATE_CURRENT);
        mRemoteViews.setOnClickPendingIntent(R.id.note_play_pause, playPauseIntent);
    }

    private void buildNotification(){
        mNoteBuilder = new NotificationCompat.Builder(mContext)
                .setSmallIcon(android.R.drawable.ic_media_play)
                .setContent(mRemoteViews)
                .setContentTitle("SpotifyStreamer")
                .setContentText("Playing music now")
                .setOngoing(true);

        Picasso.with(mContext).load(mTrack.getAlbumImage()).resize(250, 250).into(mRemoteViews, R.id.note_album_image, NOTIFY_ID, mNoteBuilder.build());
        adjustTrackName();
        mRemoteViews.setTextViewText(R.id.note_track_name, mTrackName);
        mRemoteViews.setTextColor(R.id.note_track_name, Color.BLACK);
        mRemoteViews.setImageViewResource(R.id.note_play_pause, android.R.drawable.ic_media_pause);
    }

    private void updateNotification() {
        mNoteBuilder.setContent(mRemoteViews);
        mNoteManager.notify(NOTIFY_ID, mNoteBuilder.build());
    }

    private void adjustTrackName(){
        if(mTrack.getSongName().length() > 13){
            mTrackName = mTrack.getSongName().substring(0, 13) + "\r\n" + mTrack.getSongName().substring(13, mTrack.getSongName().length());
        } else{
            mTrackName = mTrack.getSongName();
        }
    }

    public void notificationCancel(){
        Log.i(LOG_TAG, "canceling notification");
        mNoteManager.cancel(NOTIFY_ID);
        mContext.unregisterReceiver(mReceiver);
    }
}
