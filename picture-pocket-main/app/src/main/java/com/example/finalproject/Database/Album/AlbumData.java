package com.example.finalproject.Database.Album;

import androidx.annotation.NonNull;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters;

import com.example.finalproject.Image.Image;

import java.util.List;

@Entity(tableName = "AlbumDataTable")
@TypeConverters(ImageTypeConverTer.class)
public class AlbumData {
    @PrimaryKey(autoGenerate = true)
    private int id;

    @NonNull
    private String albumName;

    private List<Image> listImageOfAlbum;

    public AlbumData() {
    }

    public AlbumData(int id, String albumName, List<Image> listImageOfAlbum) {
        this.albumName = albumName;
        this.id = id;
        this.listImageOfAlbum = listImageOfAlbum;
    }

    public AlbumData(@NonNull String albumName) {
        this.albumName = albumName;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    @NonNull
    public String getAlbumName() {
        return albumName;
    }

    public void setAlbumName(@NonNull String albumName) {
        this.albumName = albumName;
    }

    public List<Image> getListImageOfAlbum() {
        return listImageOfAlbum;
    }

    public void setListImageOfAlbum(List<Image> listImageOfAlbum) {
        this.listImageOfAlbum = listImageOfAlbum;
    }
}

//import androidx.annotation.NonNull;
//import androidx.room.Entity;
//import androidx.room.PrimaryKey;
//
//import com.example.finalproject.Image.Image;
//
//import java.util.List;
//
//@Entity(tableName = "AlbumDataTable")
//public class AlbumData {
//    @PrimaryKey(autoGenerate = true)
//    private int id;
//    @NonNull
//    private String AlbumName;
//
//    private List<Image> ListImageOfAlbum;
//
//    public AlbumData(){}
//
//    public AlbumData(@NonNull String albumName) {
//        this.AlbumName = albumName;
//    }
//
//    public int getId() {
//        return id;
//    }
//
//    public void setId(int id) {
//        this.id = id;
//    }
//
//    @NonNull
//    public String getAlbumName() {
//        return AlbumName;
//    }
//
//    public void setAlbumName(@NonNull String albumName) {
//        AlbumName = albumName;
//    }
//    public List<Image> getListImageOfAlbum() {
//        return ListImageOfAlbum;
//    }
//    public void setListImageOfAlbum(List<Image> listImageOfAlbum) {
//        ListImageOfAlbum = listImageOfAlbum;
//    }
//}

