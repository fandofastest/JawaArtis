package com.satux.duax.tigax.models;

public class SongModel {

    private String Title, Album, Artist, Duration, Path, Name, AlbumID, lyric;

    /**
     * ******** Set Methods *****************
     */

    @Override
    public boolean equals(Object other) {
        if (!(other instanceof SongModel)) {
            return false;
        }

        SongModel that = (SongModel) other;

        // Custom equality check here.
        return this.getAlbum().equals(that.getAlbum())
                && this.getAlbumID().equals(that.getAlbumID())
                && this.getArtist().equals(that.getArtist())
                && this.getDuration().equals(that.getDuration())
                && this.getFileName().equals(that.getFileName())
                && this.getPath().equals(that.getPath())
                && this.getTitle().equals(that.getTitle());
    }
    /**
     * ******** Get Methods ***************
     */

    public String getTitle() {
        return this.Title;
    }
    public void setTitle(String a) {
        this.Title = a;
    }
    public String getDuration() {
        return this.Duration;
    }
    public void setDuration(String Duration) {
        this.Duration = Duration;
    }
    public String getArtist() {
        return this.Artist;
    }
    public void setArtist(String a) {
        this.Artist = a;
    }
    public String getPath() {
        return this.Path;
    }
    public void setPath(String Url) {
        this.Path = Url;
    }
    public String getFileName() {
        return this.Name;
    }
    public void setFileName(String a) {
        this.Name = a;
    }
    public String getAlbum() {
        return this.Album;
    }
    public void setAlbum(String album) {
        this.Album = album;
    }
    public String getAlbumID() {
        return this.AlbumID;
    }
    public void setAlbumID(String albumid) {
        this.AlbumID = albumid;
    }
    public String getLyric() {
        return this.lyric;
    }
    public void setLyric(String lyric) {
        this.lyric = lyric;
    }
}
