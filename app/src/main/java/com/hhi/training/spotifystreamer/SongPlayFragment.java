package com.hhi.training.spotifystreamer;

import android.support.v4.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;


import com.squareup.picasso.Picasso;


/**
 * Created by Josiah Hadley on 9/14/2015.
 */
public class SongPlayFragment extends DialogFragment {

    public final String LOG_TAG = SongPlayFragment.class.getSimpleName();

    private boolean mPlaybackPaused = false;

    private SongData mTrack;
    private int mTrackPosition;
    private int mTrackDuration;

    private IntentFilter mIntentFilter;
    private Intent mOutgoingIntent;

    private ImageButton mPlayPauseButton;
    private ImageButton mPreviousButton;
    private ImageButton mNextButton;

    private ImageView mTrackImageView;

    private SeekBar mSeekbar;

    private TextView mArtistNameView;
    private TextView mAlbumNameView;
    private TextView mTrackNameView;
    private TextView mTrackDurationView;





    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BroadcastContract.MSG_ASYNC_READY)){
                mTrack = intent.getParcelableExtra(IntentContract.TRACK);
                mTrackDuration = intent.getIntExtra(IntentContract.TRACK_DURATION, mTrackDuration);
                mPlaybackPaused = false;
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
                setSongDetails();
            }  else if(intent.getAction().equals(BroadcastContract.MSG_GET_MUSIC_CONTROL)){
                mTrackPosition = intent.getIntExtra(IntentContract.TRACK_POSITION, mTrackPosition)/1000;
                mSeekbar.setProgress(mTrackPosition);
            } else if(intent.getAction().equals(BroadcastContract.MSG_SEND_POS)){
                mTrackPosition = intent.getIntExtra(IntentContract.TRACK_POSITION, mTrackPosition);
                mSeekbar.setProgress(mTrackPosition);
            } else if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PAUSED)){
                mPlaybackPaused = true;
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
            } else if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PLAYING)){
                mPlaybackPaused = false;
                mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
            }
        }
    };





    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null)
            mTrack = getArguments().getParcelable(IntentContract.TRACK);
        else {
            mTrack = getActivity().getIntent().getParcelableExtra(IntentContract.TRACK);
        }

        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState){


        View rootView = inflater.inflate(R.layout.fragment_play_track, container, false);
        //View rootView = inflater.inflate(R.layout.fragment_play_track, null);

        mTrackDuration = 0;
        mPlayPauseButton = (ImageButton) rootView.findViewById(R.id.layout_play_play_pause);
        mPreviousButton = (ImageButton) rootView.findViewById(R.id.layout_play_previous);
        mNextButton = (ImageButton) rootView.findViewById(R.id.layout_play_next);
        mSeekbar = (SeekBar) rootView.findViewById(R.id.layout_play_seek_bar);
        mTrackImageView = (ImageView) rootView.findViewById(R.id.layout_play_album_art);
        mArtistNameView = (TextView) rootView.findViewById(R.id.layout_play_artist_name);
        mAlbumNameView = (TextView) rootView.findViewById(R.id.layout_play_album_name);
        mTrackNameView = (TextView) rootView.findViewById(R.id.layout_play_song_name);
        mTrackDurationView = (TextView) rootView.findViewById(R.id.layout_play_song_dur_end);


        getActivity().getApplicationContext().startService(new Intent(getActivity().getApplicationContext(),
                PlayService.class));

        mIntentFilter = BroadcastContract.getPlayFilter();
        mOutgoingIntent = new Intent();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
        return rootView;
    }

    @Override
    public void onStart() {
        super.onStart();
        setSongDetails();

        if(mPlaybackPaused)
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        else
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);

        mPlayPauseButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mPlaybackPaused) {
                    play();
                } else {
                    pause();
                }
            }
        });

        mNextButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playNext();
            }
        });

        mPreviousButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playPrev();
            }
        });

        mSeekbar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {

            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                mOutgoingIntent.setAction(BroadcastContract.MSG_SEEK_TO);
                mOutgoingIntent.putExtra(IntentContract.TRACK_POSITION, seekBar.getProgress());
                getActivity().sendBroadcast(mOutgoingIntent);
            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelable(IntentContract.TRACK, mTrack);
        savedInstanceState.putInt(IntentContract.TRACK_DURATION, mTrackDuration);
        savedInstanceState.putBoolean(IntentContract.IS_PAUSED, mPlaybackPaused);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            mTrack = savedInstanceState.getParcelable(IntentContract.TRACK);
            mTrackDuration = savedInstanceState.getInt(IntentContract.TRACK_DURATION);
            mPlaybackPaused = savedInstanceState.getBoolean(IntentContract.IS_PAUSED);
        }
    }

    public static SongPlayFragment newInstance(Bundle bundle){
        SongPlayFragment fragment = new SongPlayFragment();

        fragment.setArguments(bundle);

        return fragment;
    }

    private void setSongDetails(){
        Picasso.with(getActivity().getApplicationContext()).load(mTrack.getAlbumImage()).resize(600, 600).centerCrop().into(mTrackImageView);
        mAlbumNameView.setText(mTrack.getAlbumName());
        mTrackNameView.setText(mTrack.getSongName());
        mArtistNameView.setText(mTrack.getArtistName());
        mTrackDurationView.setText(buildTime());
        mSeekbar.setMax(mTrackDuration);
    }



    private void playNext(){
        mOutgoingIntent.setAction(BroadcastContract.MSG_NEXT_TRACK);
        getActivity().sendBroadcast(mOutgoingIntent);
    }

    private void playPrev(){
        mOutgoingIntent.setAction(BroadcastContract.MSG_PREV_TRACK);
        getActivity().sendBroadcast(mOutgoingIntent);
    }

    public void play() {
        mOutgoingIntent.setAction(BroadcastContract.MSG_PLAY_TRACK);
        getActivity().sendBroadcast(mOutgoingIntent);
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        mPlaybackPaused = false;
    }

    public void pause() {
        mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        mOutgoingIntent.setAction(BroadcastContract.MSG_PAUSE_TRACK);
        getActivity().sendBroadcast(mOutgoingIntent);
        mPlaybackPaused = true;
    }

    private String buildTime(){
        long seconds = mTrackDuration / 1000;
        long minutes = seconds / 60;
        return (minutes % 60) + ":" + (seconds % 60);
    }


    @Override
    public void onPause(){
        super.onPause();
    }

    @Override
    public void onResume(){
        super.onResume();
    }


    @Override
    public void onDestroy(){
        getActivity().unregisterReceiver(mReceiver);
        super.onDestroy();
    }

}
