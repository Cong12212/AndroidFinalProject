package com.example.finalproject.Album;

import com.example.finalproject.Image.Image;

import java.util.List;

public class Album {
    private int id;
    private String name;
    private List<Image> imageList;
    private Integer imageOfAlbum;

    public Album(int id, Integer imageOfAlbum, String name, List<Image> imageList) {
        this.id = id;
        this.name = name;
        this.imageOfAlbum = imageOfAlbum;
        this.imageList = imageList;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    public Integer getImageOfAlbum() {
        return imageOfAlbum;
    }

    public void setImageOfAlbum(Integer imageOfAlbum) {
        this.imageOfAlbum = imageOfAlbum;
    }
}
