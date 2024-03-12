package com.example.finalproject.Database.Image;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface ImageAlbumDAO {
    @Query("Select * From Image_Album ")
    List<ImageAlbum> getListImageAlbum();

    @Query("Select * From Image_Album Where albumId=:album_id")
    List<ImageAlbum> getImageAlbumByAlbumId(int album_id);

    @Query("Select * From Image_Album Where imagePath=:img_path")
    List<ImageAlbum> getImageAlbumByImagePath(String img_path);

    @Query("Select count(*) From Image_Album i,  AlbumDataTable a Where a.AlbumName=:album_name and a.Id = i.albumId and i.imagePath=:path")
    boolean checkImageInAlbumByName(String album_name, String path);

    @Insert
    void insertImageAlbum(ImageAlbum imageAlbum);

    @Update
    void updateImageAlbum(ImageAlbum imageAlbum);

    @Delete
    void deleteImageAlbum(ImageAlbum imageAlbum);
}
