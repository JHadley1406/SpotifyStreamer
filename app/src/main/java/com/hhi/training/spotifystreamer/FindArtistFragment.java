package com.hhi.training.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.v7.widget.LinearLayoutCompat;
import android.text.Editable;
import android.text.InputType;
import android.text.TextWatcher;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.Artists;
import kaaes.spotify.webapi.android.models.ArtistsPager;
import retrofit.Callback;
import retrofit.RetrofitError;
import retrofit.client.Response;

public class FindArtistFragment extends Fragment {

    public final String LOG_TAG = FindArtistFragment.class.getSimpleName();

    private final String ARTIST_TAG = "artists";
    private final String ARTISTID = "artistid";

    private final String DEFAULT_IMAGE = "android.resource://com.hhi.training.spotifystreamer/" + R.mipmap.ic_launcher;
    private ArrayList<ArtistData> returnedArtists;

    private EditText artistName;
    private ListView artistList;

    private FindArtistListAdapter artistAdapter;

    private Toast artistToast;

    public FindArtistFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        returnedArtists = new ArrayList<ArtistData>();


        View rootView = inflater.inflate(R.layout.fragment_find_artist, container, false);

        artistName = (EditText) rootView.findViewById(R.id.activity_artists_search_text);

        artistAdapter = new FindArtistListAdapter(getActivity(), R.layout.list_layout_artists, returnedArtists);

        artistList = (ListView) rootView.findViewById(R.id.activity_artists_listview);
        artistList.setAdapter(artistAdapter);

        return rootView;
    }

    @Override
    public void onStart()
    {
        super.onStart();
        artistName.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE ||
                        actionId == EditorInfo.IME_ACTION_SEARCH ||
                        actionId == KeyEvent.ACTION_DOWN) {
                    getArtists(v.getText().toString());
                    ((InputMethodManager) getActivity()
                            .getSystemService(Activity.INPUT_METHOD_SERVICE))
                            .toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
                    return true;
                }
                return false;
            }
        });

        artistList.setOnItemClickListener(new ListView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent artistIntent = new Intent(artistAdapter.getContext(), TopSongsActivity.class);
                artistIntent.putExtra(ARTISTID, returnedArtists.get(position).getArtistId());
                startActivity(artistIntent);
            }
        });

    }

    @Override
    public void onSaveInstanceState(Bundle savedInstanceState){
        savedInstanceState.putParcelableArrayList(ARTIST_TAG, returnedArtists);
        super.onSaveInstanceState(savedInstanceState);
    }

    @Override
    public void onViewStateRestored(Bundle savedInstanceState){
        super.onViewStateRestored(savedInstanceState);
        if(savedInstanceState != null) {
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
                            makeToast("No Artists Found, Try Refining Your Search\n"
                                    + "Or Check Your Internet Connection", Toast.LENGTH_LONG);
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
