package com.hhi.training.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Artist;


/**
 * Created by jhadley on 6/12/2015.
 */
public class FindArtistListAdapter extends ArrayAdapter<ArtistData> {

    public final String LOG_TAG = FindArtistListAdapter.class.getSimpleName();

    private Context context;
    private int layoutResourceId;
    private List<ArtistData> artists;

    public FindArtistListAdapter(Context context, int resourceId, ArrayList<ArtistData> artists){
        super(context, resourceId, artists);
        this.context = context;
        this.layoutResourceId = resourceId;
        this.artists = artists;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent){
        View row = view;
        ArtistHolder artistHolder;

        if(row == null){
            artistHolder = new ArtistHolder();
            LayoutInflater inflater =(LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            row = inflater.inflate(layoutResourceId, parent, false);
            artistHolder.artistImage = (ImageView) row.findViewById(R.id.layout_artists_imageView);
            artistHolder.artistName = (TextView) row.findViewById(R.id.layout_artists_textview);
            row.setTag(artistHolder);
        }
        else{
            artistHolder = (ArtistHolder)row.getTag();
        }

        ArtistData artist = artists.get(position);
        artistHolder.artistName.setText(artist.getArtistName());
        Picasso.with(context).load(artist.getArtistImage()).resize(200, 200).centerCrop().into(artistHolder.artistImage);

        return row;
    }

    static class ArtistHolder{
        ImageView artistImage;
        TextView artistName;
    }

}
