package com.hhi.training.spotifystreamer;

import android.support.v4.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
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
                mPlaybackPaused = intent.getBooleanExtra(IntentContract.IS_PAUSED, false);
                setPlayPauseButton();
                setSongDetails();
            }  else if(intent.getAction().equals(BroadcastContract.MSG_GET_MUSIC_CONTROL)){
                mTrack = intent.getParcelableExtra(IntentContract.TRACK);
                mPlaybackPaused = intent.getBooleanExtra(IntentContract.IS_PAUSED, false);
                mTrackDuration = intent.getIntExtra(IntentContract.TRACK_DURATION, mTrackDuration);
                mTrackPosition = intent.getIntExtra(IntentContract.TRACK_POSITION, mTrackPosition);
                setSongDetails();
                mSeekbar.setProgress(mTrackPosition);
                setPlayPauseButton();
            } else if(intent.getAction().equals(BroadcastContract.MSG_SEND_POS)){
                mTrackPosition = intent.getIntExtra(IntentContract.TRACK_POSITION, mTrackPosition);
                mSeekbar.setProgress(mTrackPosition);
            } else if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PAUSED)){
                mPlaybackPaused = intent.getBooleanExtra(IntentContract.IS_PAUSED, true);
                setPlayPauseButton();
            } else if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PLAYING)){
                mPlaybackPaused = intent.getBooleanExtra(IntentContract.IS_PAUSED, false);
                setPlayPauseButton();
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        if(getArguments() != null) {
            mTrack = getArguments().getParcelable(IntentContract.TRACK);
        } else {
            mTrack = getActivity().getIntent().getParcelableExtra(IntentContract.TRACK);
        }

        setStyle(DialogFragment.STYLE_NORMAL, 0);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState){


        View rootView = inflater.inflate(R.layout.fragment_play_track, container, false);

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

        setPlayPauseButton();
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
        Log.i(LOG_TAG, "in onSaveInstanceState, mPlaybackPaused is: " + mPlaybackPaused);
        savedInstanceState.putParcelable(IntentContract.TRACK, mTrack);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            getActivity().sendBroadcast(new Intent().setAction(BroadcastContract.MSG_SEND_TRACK));
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
        setPlayPauseButton();
    }

    public void pause() {
        Log.i(LOG_TAG, "Sending pause broadcast from Song Controls");
        mOutgoingIntent.setAction(BroadcastContract.MSG_PAUSE_TRACK);
        getActivity().sendBroadcast(mOutgoingIntent);

        setPlayPauseButton();
    }

    private String buildTime(){
        long seconds = mTrackDuration / 1000;
        long minutes = seconds / 60;
        return (minutes % 60) + ":" + (seconds % 60);
    }

    private void setPlayPauseButton(){
        if(mPlaybackPaused){
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_play);
        } else {
            mPlayPauseButton.setImageResource(android.R.drawable.ic_media_pause);
        }
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
