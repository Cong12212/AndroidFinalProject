package com.example.finalproject.Database.Album;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface AlbumDataDAO {
    @Query("Select * From albumdatatable ")
    List<AlbumData> getListAlbum();

    @Query("Select * From albumdatatable Where AlbumName=:albumName")
    AlbumData getAlbumByName(String albumName);

    @Query("Select AlbumName From albumdatatable ")
    List<String> getListNameAlbum();

    @Query("Select * From albumdatatable Where id=:id")
    AlbumData getAlbumById(int id);

    @Insert
    void insertAlbum(AlbumData album);

    @Update
    void updateAlbum(AlbumData album);

    @Delete
    void deleteAlbum(AlbumData album);
}
