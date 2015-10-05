package com.hhi.training.spotifystreamer;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.SearchView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
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

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FindArtistFragment extends Fragment {

    public final String LOG_TAG = FindArtistFragment.class.getSimpleName();

    private final String ARTIST_TAG = "artists";

    private final String DEFAULT_IMAGE = "android.resource://com.hhi.training.spotifystreamer/" + R.mipmap.ic_launcher;
    private ArrayList<ArtistData> returnedArtists;

    private SearchView artistName;
    private ListView artistList;

    private int mListPosition;

    private FindArtistListAdapter artistAdapter;

    private Toast artistToast;
    private boolean mTrackPaused;

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PAUSED)){
                mTrackPaused= intent.getBooleanExtra(IntentContract.IS_PAUSED, true);
            } else if(intent.getAction().equals(BroadcastContract.MSG_TRACK_PLAYING)){
                mTrackPaused = intent.getBooleanExtra(IntentContract.IS_PAUSED, false);
            }
        }
    };

    public interface FragmentCallback{
        public void onItemSelected(String artistId);
    }

    public FindArtistFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        returnedArtists = new ArrayList<ArtistData>();


        View rootView = inflater.inflate(R.layout.fragment_find_artist, container, false);

        artistName = (SearchView) rootView.findViewById(R.id.activity_artists_search_text);

        artistAdapter = new FindArtistListAdapter(getActivity(), R.layout.list_layout_artists, returnedArtists);

        artistList = (ListView) rootView.findViewById(R.id.activity_artists_listview);
        artistList.setAdapter(artistAdapter);


        if(savedInstanceState != null && savedInstanceState.containsKey(IntentContract.SELECTED_KEY)){
            mListPosition = savedInstanceState.getInt(IntentContract.SELECTED_KEY);
        }

        getActivity().registerReceiver(mReceiver, BroadcastContract.getArtistsFilter());
        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        // SearchView Suggested by Udacity Reviewer
        artistName.setIconifiedByDefault(false);
        artistName.setQueryHint(getResources().getString(R.string.artist_search_hint));
        artistName.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String s) {
                getArtists(s);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String s) {
                return false;
            }
        });

        artistList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //Intent artistIntent = new Intent(artistAdapter.getContext(), TopSongsActivity.class);
                //artistIntent.putExtra(IntentContract.ARTISTID, returnedArtists.get(position).getArtistId());
                //startActivity(artistIntent);
                ((FragmentCallback) getActivity())
                        .onItemSelected(returnedArtists.get(position).getArtistId());
                mListPosition = position;
            }
        });


    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelableArrayList(ARTIST_TAG, returnedArtists);
        savedInstanceState.putBoolean(IntentContract.IS_PAUSED, mTrackPaused);
        if(mListPosition != ListView.INVALID_POSITION){
            savedInstanceState.putInt(IntentContract.SELECTED_KEY, mListPosition);
        }
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
            mTrackPaused = savedInstanceState.getBoolean(IntentContract.IS_PAUSED);
            if(!mTrackPaused){
                getActivity().sendBroadcast(new Intent().setAction(BroadcastContract.MSG_PLAY_TRACK));
            }
            returnedArtists = savedInstanceState.getParcelableArrayList(ARTIST_TAG);
            refreshAdapter();
        }
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater){
        inflater.inflate(R.menu.menu_find_artist, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId())
        {
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onDestroy(){
        super.onDestroy();
        getActivity().unregisterReceiver(mReceiver);
        getActivity().sendBroadcast(new Intent().setAction(BroadcastContract.MSG_PAUSE_TRACK));
    }

    private void makeToast(String message, int length)
    {
        if(artistToast != null)
        {
            artistToast = null;
        }

        artistToast = Toast.makeText(getActivity(), message, length);
        artistToast.show();
    }


    private void getArtists(String artistName)
    {
        if(isOnline()) {
            SpotifyApi spotifyApi = new SpotifyApi();
            SpotifyService spotifyService = spotifyApi.getService();
            spotifyService.searchArtists(artistName, new Callback<ArtistsPager>() {
                @Override
                public void success(ArtistsPager artistsPager, Response response) {
                    if (artistsPager != null)
                        assignArtists((ArrayList) artistsPager.artists.items);

                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (returnedArtists.size() == 0)
                                makeToast(getResources().getString(R.string.no_artist), Toast.LENGTH_LONG);
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

    // Code suggested by Udacity Reviewer
    private boolean isOnline()
    {
        ConnectivityManager connectivityManager
                = (ConnectivityManager) getActivity().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        return activeNetworkInfo != null && activeNetworkInfo.isConnected();
    }

    private void assignArtists(ArrayList<Artist> artists){
        returnedArtists = new ArrayList<>();
        for(Artist artist: artists){
            if(artist.images.size() > 0 && validateUrl(artist.images.get(0).url))
                returnedArtists.add(new ArtistData(artist.id, artist.name, artist.images.get(0).url));
            else
                returnedArtists.add(new ArtistData(artist.id, artist.name, DEFAULT_IMAGE));
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
        artistAdapter.clear();
        artistAdapter.addAll(returnedArtists);
        artistAdapter.notifyDataSetChanged();
    }
}
