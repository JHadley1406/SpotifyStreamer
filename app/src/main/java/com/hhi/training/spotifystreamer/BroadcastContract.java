package com.hhi.training.spotifystreamer;

import android.content.Intent;
import android.content.IntentFilter;

/**
 * Created by Josiah Hadley on 9/28/2015.
 */
public class BroadcastContract {
    public final static String MSG_GET_MUSIC_SERVICE = "SERVICEGETMUSIC";
    public final static String MSG_GET_MUSIC_CONTROL = "CONTROLGETMUSIC";
    public final static String MSG_PLAY_TRACK = "PLAY_TRACK";
    public final static String MSG_TRACK_PLAYING = "TRACK_PLAYING";
    public final static String MSG_PAUSE_TRACK = "PAUSE_TRACK";
    public final static String MSG_TRACK_PAUSED = "TRACK_PAUSED";
    public final static String MSG_NEXT_TRACK = "NEXTTRACK";
    public final static String MSG_PREV_TRACK = "PREVTRACK";
    public final static String MSG_ASYNC_READY = "ASYNCREADY";
    public final static String MSG_SEND_TRACK = "SENDTRACK";
    public final static String MSG_SEEK_TO = "SEEKTO";
    public final static String MSG_SEND_POS = "SENDPOS";
    public final static String MSG_NEED_MUSIC = "NEEDMUSIC";

    public static IntentFilter getNotificationFilter(){
        IntentFilter noteFilter = new IntentFilter();
        noteFilter.addAction(MSG_ASYNC_READY);
        noteFilter.addAction(MSG_NEED_MUSIC);
        noteFilter.addAction(MSG_TRACK_PLAYING);
        noteFilter.addAction(MSG_TRACK_PAUSED);
        return noteFilter;
    }

    public static IntentFilter getServiceFilter(){
        IntentFilter serviceFilter = new IntentFilter();
        serviceFilter.addAction(MSG_GET_MUSIC_SERVICE);
        serviceFilter.addAction(MSG_PLAY_TRACK);
        serviceFilter.addAction(MSG_PAUSE_TRACK);
        serviceFilter.addAction(MSG_NEXT_TRACK);
        serviceFilter.addAction(MSG_PREV_TRACK);
        serviceFilter.addAction(MSG_SEEK_TO);
        serviceFilter.addAction(MSG_SEND_TRACK);
        return serviceFilter;
    }

    public static IntentFilter getPlayFilter(){
        IntentFilter playFilter = new IntentFilter();
        playFilter.addAction(MSG_ASYNC_READY);
        playFilter.addAction(MSG_NEED_MUSIC);
        playFilter.addAction(MSG_GET_MUSIC_CONTROL);
        playFilter.addAction(MSG_SEND_POS);
        playFilter.addAction(MSG_TRACK_PLAYING);
        playFilter.addAction(MSG_TRACK_PAUSED);
        return playFilter;
    }

    public static IntentFilter getTopSongsFilter(){
        IntentFilter topSongsFilter = new IntentFilter();
        topSongsFilter.addAction(MSG_ASYNC_READY);
        topSongsFilter.addAction(MSG_TRACK_PAUSED);
        topSongsFilter.addAction(MSG_TRACK_PLAYING);
        topSongsFilter.addAction(MSG_GET_MUSIC_CONTROL);
        return topSongsFilter;
    }

    public static IntentFilter getArtistsFilter(){
        IntentFilter artistsFilter = new IntentFilter();
        artistsFilter.addAction(MSG_TRACK_PAUSED);
        artistsFilter.addAction(MSG_TRACK_PLAYING);
        return artistsFilter;
    }

}
