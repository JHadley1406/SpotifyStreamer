package com.hhi.training.spotifystreamer;

import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;


public class FindArtistActivity extends ActionBarActivity implements FindArtistFragment.FragmentCallback {

    private final String LOG_TAG = FindArtistActivity.class.getSimpleName();
    private static final String ARTISTFRAGMENT_TAG = "FRAGMENTTAG";

    private boolean mTwoPane;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_find_artist);

        if(findViewById(R.id.top_track_container) != null){
            mTwoPane = true;

            if(savedInstanceState == null){
                getSupportFragmentManager().beginTransaction()
                        .replace(R.id.top_track_container, new TopSongsFragment(), ARTISTFRAGMENT_TAG)
                        .commit();
            }
        } else{
            mTwoPane = false;
            getSupportActionBar().setElevation(0f);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId()){
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume(){
        super.onResume();
        //probably don't have to do anything here, but leaving this stub just in case
    }

    @Override
    public void onItemSelected(String artistId){
        if(mTwoPane) {
            Bundle args = new Bundle();
            args.putString(IntentContract.ARTISTID, artistId);

            TopSongsFragment fragment = new TopSongsFragment();
            fragment.setArguments(args);

            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.top_track_container, fragment, ARTISTFRAGMENT_TAG)
                    .commit();
        }else{
            Intent intent = new Intent(getApplicationContext(), TopSongsActivity.class);
            intent.putExtra(IntentContract.ARTISTID, artistId);
            intent.putExtra(IntentContract.IS_SINGLE_PANE, true);
            startActivity(intent);
        }
    }
}
