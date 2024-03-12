package com.example.finalproject.Image;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class Image implements Serializable {
    public String uri;
    private String id;
    private String path;
    private Date dateTaken;
    private Date AddAt;
    private String albumName;
    private boolean isChecked;

    private List<String> hashtag = new ArrayList<>();

    public Image() {
        this.dateTaken = new Date();
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    public Date getAddAt() {
        return AddAt;
    }

    public void setAddAt(Date addAt) {
        this.AddAt = addAt;
    }

    public List<String> getHashtag() {
        return hashtag;
    }

    public void setHashtag(List<String> hashtag) {
        this.hashtag = hashtag;
    }

    public void addHashtag(String hashtag) {
        this.hashtag.add(hashtag);
    }

    public void removeHashtag(String hashtag) {
        this.hashtag.remove(hashtag);
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(String albumName) {
        this.albumName = albumName;
    }

    public String getUri() {
        return uri;
    }

    public void setUri(String uri) {
        this.uri = uri;
    }

    public boolean isChecked() {
        return this.isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    public void setCheck(boolean check) {
        this.isChecked = check;
    }
}
