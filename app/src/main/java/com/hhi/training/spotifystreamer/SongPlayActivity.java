package com.hhi.training.spotifystreamer;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;

public class SongPlayActivity extends ActionBarActivity {

    public final String LOG_TAG = SongPlayActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_play_track);

        Bundle songData = new Bundle();
        songData.putParcelable(IntentContract.TRACK, getIntent().getParcelableExtra(IntentContract.TRACK));

        SongPlayFragment existingPlayFragment = (SongPlayFragment) getSupportFragmentManager().findFragmentById(R.id.linear_layout_play_track);

        if(existingPlayFragment == null) {
            DialogFragment songPlayFragment = SongPlayFragment.newInstance(songData);
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.linear_layout_play_track, songPlayFragment);
            fragmentTransaction.commit();
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


}
