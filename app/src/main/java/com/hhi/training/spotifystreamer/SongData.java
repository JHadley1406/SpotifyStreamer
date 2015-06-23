package com.hhi.training.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jhadley on 6/22/2015.
 */
public class SongData implements Parcelable {

    private String songId;
    private String songName;
    private String albumName;
    private String albumImage;

    public SongData(String songId, String songName, String albumName, String albumImage) {
        this.songId = songId;
        this.songName = songName;
        this.albumName = albumName;
        this.albumImage = albumImage;
    }

    public SongData(Parcel in){
        this.songId = in.readString();
        this.songName = in.readString();
        this.albumName = in.readString();
        this.albumImage = in.readString();
    }

    public String getSongId() {
        return songId;
    }

    public void setSongId(String songId) {
        this.songId = songId;
    }

    public String getSongName() {
        return songName;
    }

    public void setSongName(String songName) {
        this.songName = songName;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getAlbumImage() {
        return albumImage;
    }

    public void setAlbumImage(String albumImage) {
        this.albumImage = albumImage;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(songId);
        dest.writeString(songName);
        dest.writeString(albumName);
        dest.writeString(albumImage);
    }

    public static final Parcelable.Creator<SongData> CREATOR = new Parcelable.Creator<SongData>(){
        public SongData createFromParcel(Parcel in){
            return new SongData(in);
        }

        public SongData[] newArray(int size){
            return new SongData[size];
        }
    };
}
