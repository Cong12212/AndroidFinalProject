package com.example.finalproject.Database.Hashtag;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

@Dao
public interface HashtagDataDAO {
    @Query("Select * From HashtagDataTable")
    List<HashtagData> getListHashtag();

    @Query("Select a.Hashtag From HashtagDataTable a Where imagePath=:imagePath")
    List<String> geHashtagByImagePath(String imagePath);

    @Query("Select imagePath From HashtagDataTable Where Hashtag=:hashtag")
    List<String> geListImageByHashtag(String hashtag);

    @Insert
    void insertHashtag(HashtagData hashtag);

    @Update
    void updateHashtag(HashtagData hashtag);

    @Delete
    void deleteHashtag(HashtagData hashtag);
}
