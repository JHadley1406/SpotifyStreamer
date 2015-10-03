package com.hhi.training.spotifystreamer;

import android.os.Parcel;
import android.os.Parcelable;

import kaaes.spotify.webapi.android.models.Image;

/**
 * Created by jhadley on 6/22/2015.
 */
public class ArtistData implements Parcelable {

    private String artistId;
    private String artistName;
    private String artistImage;


    public ArtistData(String artistId, String artistName, String artistImage){
        this.setArtistId(artistId);
        this.setArtistName(artistName);
        this.setArtistImage(artistImage);
    }

    public ArtistData(Parcel in){
        this.setArtistId(in.readString());
        this.setArtistName(in.readString());
        this.setArtistImage(in.readString());
    }

    public String getArtistName() {
        return artistName;
    }

    public void setArtistName(String artistName) {
        this.artistName = artistName;
    }

    public String getArtistImage() {
        return artistImage;
    }

    public void setArtistImage(String artistImage) {
        this.artistImage = artistImage;
    }

    public String getArtistId() {

        return artistId;
    }

    public void setArtistId(String artistId) {
        this.artistId = artistId;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.getArtistId());
        dest.writeString(this.getArtistName());
        dest.writeString(this.getArtistImage());
    }

    public static final Parcelable.Creator<ArtistData> CREATOR = new Parcelable.Creator<ArtistData>(){
        public ArtistData createFromParcel(Parcel in){
            return new ArtistData(in);
        }

        public ArtistData[] newArray(int size){
            return new ArtistData[size];
        }
    };
}
