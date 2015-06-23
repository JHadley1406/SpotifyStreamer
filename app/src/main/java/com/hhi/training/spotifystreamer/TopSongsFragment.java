package com.hhi.training.spotifystreamer;


import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.Tracks;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

/**
 * Created by jhadley on 6/12/2015.
 */
public class TopSongsFragment extends Fragment {

    public final String LOG_TAG = TopSongsFragment.class.getSimpleName();

    private final String SONG_TAG = "songs";
    private final String ARTISTID = "artistid";
    private final String COUNTRY_OPTION = "country";
    private final String COUNTRY_TAG = "US";
    private final String DEFAULT_IMAGE = "android.resource://com.hhi.training.spotifystreamer/" + R.mipmap.ic_launcher;

    private ArrayList<SongData> returnedSongs;

    private TopSongsListAdapter songAdapter;

    private ListView songList;

    private String artistId;

    private Toast songToast;

    public TopSongsFragment(){
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState){

        Intent artistIntent = getActivity().getIntent();
        artistId = artistIntent.getStringExtra(ARTISTID);

        returnedSongs = new ArrayList<>();

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        songAdapter = new TopSongsListAdapter(getActivity(), R.layout.list_layout_songs, returnedSongs);

        songList = (ListView) rootView.findViewById(R.id.activity_songs_listview);

        songList.setAdapter(songAdapter);

        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        if(returnedSongs.size() == 0)
            getTracks(artistId);
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelableArrayList(SONG_TAG, returnedSongs);
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            returnedSongs = savedInstanceState.getParcelableArrayList(SONG_TAG);
            refreshAdapter();
        }
    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void makeToast(String message, int length)
    {
        if(songToast != null)
        {
            songToast = null;
        }

        songToast = Toast.makeText(getActivity(), message, length);
        songToast.show();
    }

    private void getTracks(String artistId){
        SpotifyApi spotifyApi = new SpotifyApi();
        SpotifyService spotifyService = spotifyApi.getService();
        Map<String, Object> spotifyOptions = new HashMap<>();
        spotifyOptions.put(COUNTRY_OPTION, COUNTRY_TAG);
        spotifyService.getArtistTopTrack(artistId, spotifyOptions, new Callback<Tracks>() {

            @Override
            public void success(Tracks tracks, Response response) {
                if (tracks != null)
                    assignSongs((ArrayList) tracks.tracks);

                getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        if (returnedSongs.size() == 0)
                            makeToast("No Tracks Found For This Artist", Toast.LENGTH_LONG);
                        else
                            refreshAdapter();
                    }
                });
            }

            @Override
            public void failure(RetrofitError error) {
                Log.e(LOG_TAG, error.toString());
            }
        });
    }

    private void assignSongs(ArrayList<Track> songs){
        returnedSongs = new ArrayList<>();
        for(Track song: songs){

            if(song.album.images.size() > 0 && validateUrl(song.album.images.get(0).url))
                returnedSongs.add(new SongData(song.id, song.name, song.album.name, song.album.images.get(0).url));
            else
                returnedSongs.add(new SongData(song.id, song.name, song.album.name, DEFAULT_IMAGE));
        }
    }

    private boolean validateUrl(String image){
        URL url;

        try{
            url = new URL(image);
        } catch(MalformedURLException e){
            return false;
        }

        try{
            url.toURI();
        } catch (URISyntaxException e){
            return false;
        }

        return true;
    }

    private void refreshAdapter(){
        songAdapter.clear();
        songAdapter.addAll(returnedSongs);
        songAdapter.notifyDataSetChanged();
    }
}
