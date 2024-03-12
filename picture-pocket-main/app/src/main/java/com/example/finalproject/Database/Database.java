package com.example.finalproject.Database;

import android.content.Context;

import androidx.room.Room;
import androidx.room.RoomDatabase;

import com.example.finalproject.Database.Album.AlbumData;
import com.example.finalproject.Database.Album.AlbumDataDAO;
import com.example.finalproject.Database.Hashtag.HashtagData;
import com.example.finalproject.Database.Hashtag.HashtagDataDAO;
import com.example.finalproject.Database.Image.ImageAlbum;
import com.example.finalproject.Database.Image.ImageAlbumDAO;

@androidx.room.Database(entities = {AlbumData.class, ImageAlbum.class, HashtagData.class}, version = 3)
public abstract class Database extends RoomDatabase {
    private static final String DbName = "18Album.db";
    private static Database instance = null;

    public static synchronized Database getInstance(Context context) {
        if (instance == null) {
            instance = Room.databaseBuilder(context.getApplicationContext(), Database.class, DbName)
                    .allowMainThreadQueries().build();
        }
        return instance;
    }

    public abstract AlbumDataDAO AlbumDataDAO();

    public abstract ImageAlbumDAO ImageAlbumDAO();

    public abstract HashtagDataDAO hashtagDataDAO();
}
