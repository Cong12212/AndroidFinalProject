package com.example.finalproject.Database.Image;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "Image_Album", primaryKeys = {"imagePath", "albumId"})
public class ImageAlbum {
    @NonNull
    private String imagePath;

    @NonNull
    private int albumId;

    public ImageAlbum(@NonNull String imagePath, @NonNull int albumId) {
        this.imagePath = imagePath;
        this.albumId = albumId;
    }

    @NonNull
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(@NonNull String imagePath) {
        this.imagePath = imagePath;
    }

    @NonNull
    public int getAlbumId() {
        return albumId;
    }

    public void setAlbumId(@NonNull int albumId) {
        this.albumId = albumId;
    }
}
