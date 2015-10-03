package com.hhi.training.spotifystreamer;


import android.support.v4.app.DialogFragment;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

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

    private int mListPosition;

    private boolean mServiceBound;
    private boolean mSinglePane;

    private final String SONG_TAG = "songs";
    private final String COUNTRY_OPTION = "country";
    private final String COUNTRY_TAG = "US";
    private final String DEFAULT_IMAGE = "android.resource://com.hhi.training.spotifystreamer/" + R.mipmap.ic_launcher;

    private ArrayList<SongData> mReturnedSongs;

    private TopSongsListAdapter mSongAdapter;

    private ListView mSongListView;

    private String mArtistId;

    private Toast mSongToast;

    private MediaNotification mNotificationPanel = null;

    private IntentFilter mIntentFilter;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().equals(BroadcastContract.MSG_ASYNC_READY)) {
                if (mNotificationPanel == null) {
                    mNotificationPanel = new MediaNotification(
                            getActivity().getApplicationContext()
                            ,(SongData) intent.getParcelableExtra(IntentContract.TRACK)
                    );
                }
            }
        }
    };


    private ServiceConnection mPlayServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {

        }

        @Override
        public void onServiceDisconnected(ComponentName name) {

        }
    };

    public TopSongsFragment(){
        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState){

        Bundle arguments = getArguments();
        if(arguments != null){

            mArtistId = arguments.getString(IntentContract.ARTISTID);
            mSinglePane = arguments.getBoolean(IntentContract.IS_SINGLE_PANE, false);
        }

        mReturnedSongs = new ArrayList<>();

        View rootView = inflater.inflate(R.layout.fragment_top_tracks, container, false);

        mSongAdapter = new TopSongsListAdapter(getActivity(), R.layout.list_layout_songs, mReturnedSongs);

        mSongListView = (ListView) rootView.findViewById(R.id.activity_songs_listview);

        mSongListView.setAdapter(mSongAdapter);

        mIntentFilter = BroadcastContract.getTopSongsFilter();
        getActivity().registerReceiver(mReceiver, mIntentFilter);
        doBindService();

        if(savedInstanceState != null && savedInstanceState.containsKey(IntentContract.SELECTED_KEY)){
            mListPosition = savedInstanceState.getInt(IntentContract.SELECTED_KEY);
        }


        return rootView;
    }

    @Override
    public void onStart(){
        super.onStart();
        if(mReturnedSongs.size() == 0)
            getTracks(mArtistId);

        mSongListView.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(LOG_TAG, "Album Image: " + mReturnedSongs.get(position).getAlbumImage());
                Intent playService = new Intent();
                playService.setAction(BroadcastContract.MSG_GET_MUSIC_SERVICE);
                playService.putParcelableArrayListExtra(IntentContract.TRACKS, mReturnedSongs);
                playService.putExtra(IntentContract.TRACK_NUMBER, position);
                getActivity().sendBroadcast(playService);
                showSongControls(mReturnedSongs.get(position));
                mListPosition = position;

            }
        });
    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelableArrayList(SONG_TAG, mReturnedSongs);
        if(mListPosition != ListView.INVALID_POSITION){
            savedInstanceState.putInt(IntentContract.SELECTED_KEY, mListPosition);
        }
        super.onSaveInstanceState(savedInstanceState);

    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            mReturnedSongs = savedInstanceState.getParcelableArrayList(SONG_TAG);

            refreshAdapter();
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        if(mNotificationPanel != null)
            mNotificationPanel.notificationCancel();
        doUnbindService();
        getActivity().unregisterReceiver(mReceiver);
    }

    private void showSongControls(SongData track){
        if(!mSinglePane) {
            Bundle songData = new Bundle();
            songData.putParcelable(IntentContract.TRACK, track);
            DialogFragment songPlayFragment = SongPlayFragment.newInstance(songData);
            songPlayFragment.show(getFragmentManager(), "dialog");
        } else {
            Intent singlePaneIntent = new Intent(getActivity().getApplicationContext(), SongPlayActivity.class);
            singlePaneIntent.putExtra(IntentContract.TRACK, track);
            startActivity(singlePaneIntent);

        }

    }

    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()){
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void doBindService(){
        getActivity().getApplicationContext().bindService(new Intent(getActivity().getApplicationContext(),
                PlayService.class), mPlayServiceConnection, Context.BIND_AUTO_CREATE);

        mServiceBound = true;
    }

    private void doUnbindService(){
        if(mServiceBound){
            getActivity().getApplicationContext().unbindService(mPlayServiceConnection);
            mServiceBound = false;
        }
    }

    private void makeToast(String message, int length)
    {
        if(mSongToast != null)
        {
            mSongToast = null;
        }

        mSongToast = Toast.makeText(getActivity(), message, length);
        mSongToast.show();
    }

    private void getTracks(String artistId){
        if(isOnline()) {
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
                            if (mReturnedSongs.size() == 0)
                                makeToast(getResources().getString(R.string.no_songs), Toast.LENGTH_LONG);
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
        else
            makeToast(getResources().getString(R.string.not_online), Toast.LENGTH_LONG);
    }

    private void assignSongs(ArrayList<Track> songs){
        mReturnedSongs = new ArrayList<>();
        String imageUrl;
        String previewUrl;
        String artist;

        for(Track song: songs){

            if(song.album.images.size() > 0 && validateUrl(song.album.images.get(0).url))
                imageUrl = song.album.images.get(0).url;
            else
                imageUrl = DEFAULT_IMAGE;

            if(validateUrl(song.preview_url))
                previewUrl = song.preview_url;
            else
                previewUrl = "";

            if(song.artists.size() > 0)
                artist = song.artists.get(0).name;
            else
                artist = "No Artist Listed";

            mReturnedSongs.add(new SongData(song.id, song.name, previewUrl, song.album.name, imageUrl, artist));
        }
    }

    // Code suggested by Udacity Reviewer
    private boolean isOnline()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
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
        mSongAdapter.clear();
        mSongAdapter.addAll(mReturnedSongs);
        mSongAdapter.notifyDataSetChanged();
    }
}
