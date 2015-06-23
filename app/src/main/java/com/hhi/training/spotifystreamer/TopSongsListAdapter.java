package com.hhi.training.spotifystreamer;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

import kaaes.spotify.webapi.android.models.Track;

/**
 * Created by jhadley on 6/12/2015.
 */
public class TopSongsListAdapter extends ArrayAdapter<SongData> {

    public final String LOG_TAG = TopSongsListAdapter.class.getSimpleName();

    private Context context;
    private int layoutResourceId;
    private List<SongData> songs;

    public TopSongsListAdapter(Context context, int resourceId, ArrayList<SongData> songs)
    {
        super(context, resourceId, songs);
        this.context = context;
        this.layoutResourceId = resourceId;
        this.songs = songs;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent)
    {
        View row = view;
        SongHolder songHolder = null;

        if(row == null) {
            songHolder = new SongHolder();
            LayoutInflater inflater = ((Activity) context).getLayoutInflater();
            row = inflater.inflate(layoutResourceId, parent, false);
            songHolder.songImage = (ImageView) row.findViewById(R.id.layout_songs_image);
            songHolder.songAlbum = (TextView) row.findViewById(R.id.layout_songs_album);
            songHolder.songName = (TextView) row.findViewById(R.id.layout_songs_song);
            row.setTag(songHolder);
        }
        else
        {
            songHolder = (SongHolder) row.getTag();
        }

        SongData song = songs.get(position);
        Picasso.with(context).load(song.getAlbumImage()).resize(200,200).centerCrop().into(songHolder.songImage);
        songHolder.songAlbum.setText(song.getAlbumName());
        songHolder.songName.setText(song.getSongName());

        return row;
    }

    static class SongHolder
    {
        ImageView songImage;
        TextView songAlbum;
        TextView songName;
    }
}
