package com.hhi.training.spotifystreamer;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

/**
 * Created by jhadley on 6/12/2015.
 */
public class TopSongsActivity extends ActionBarActivity{

    private final String LOG_TAG = TopSongsActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_top_tracks);

        if(savedInstanceState == null){
            Log.i(LOG_TAG, "savedInstanceState is apparently NULL");
            Bundle arguments = new Bundle();
            arguments.putString(IntentContract.ARTISTID
                    , getIntent().getStringExtra(IntentContract.ARTISTID));
            arguments.putBoolean(IntentContract.IS_SINGLE_PANE, getIntent().getBooleanExtra(IntentContract.IS_SINGLE_PANE, false));
            TopSongsFragment fragment = new TopSongsFragment();
            fragment.setArguments(arguments);

            getSupportFragmentManager().beginTransaction()
                    .add(R.id.top_track_container, fragment)
                    .commit();
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu){
        getMenuInflater().inflate(R.menu.menu_top_tracks, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        switch (item.getItemId())
        {
            default:
                return super.onOptionsItemSelected(item);
        }
    }
}
