package com.hhi.training.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by jhadley on 6/22/2015.
 */
public class SongData implements Parcelable {

    private String mSongId;
    private String mSongName;
    private String mSongPreview;
    private String mAlbumName;
    private String mAlbumImage;
    private String mArtistName;

    public SongData(String songId, String songName, String songPreview, String albumName, String albumImage, String artistName) {
        this.setSongId(songId);
        this.setSongName(songName);
        this.setSongPreview(songPreview);
        this.setAlbumName(albumName);
        this.setAlbumImage(albumImage);
        this.setArtistName(artistName);
    }

    public SongData(Parcel in){
        this.setSongId(in.readString());
        this.setSongName(in.readString());
        this.setSongPreview(in.readString());
        this.setAlbumName(in.readString());
        this.setAlbumImage(in.readString());
        this.setArtistName(in.readString());

    }

    public String getSongId() {
        return mSongId;
    }

    public void setSongId(String songId) {
        this.mSongId = songId;
    }

    public String getSongName() {
        return mSongName;
    }

    public void setSongName(String songName) {
        this.mSongName = songName;
    }

    public String getSongPreview() { return mSongPreview; }

    public void setSongPreview(String songPreview) { this.mSongPreview = songPreview; }

    public String getAlbumName() {
        return mAlbumName;
    }

    public void setAlbumName(String albumName) {
        this.mAlbumName = albumName;
    }

    public String getAlbumImage() {
        return mAlbumImage;
    }

    public void setAlbumImage(String albumImage) {
        this.mAlbumImage = albumImage;
    }

    public String getArtistName() { return mArtistName; }

    public void setArtistName(String artistName) { this.mArtistName = artistName; }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getSongId());
        dest.writeString(this.getSongName());
        dest.writeString(this.getSongPreview());
        dest.writeString(this.getAlbumName());
        dest.writeString(this.getAlbumImage());
        dest.writeString(this.getArtistName());
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
