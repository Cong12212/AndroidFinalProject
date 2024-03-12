package com.example.finalproject.Database.Hashtag;

import androidx.annotation.NonNull;
import androidx.room.Entity;

@Entity(tableName = "HashtagDataTable", primaryKeys = {"imagePath", "Hashtag"})
public class HashtagData {

    @NonNull
    private final String Hashtag;
    @NonNull
    private String imagePath;

    public HashtagData(@NonNull String Hashtag, @NonNull String imagePath) {
        this.Hashtag = Hashtag;
        this.imagePath = imagePath;
    }

    @NonNull
    public String getHashtag() {
        return Hashtag;
    }

    public void setHashtag(@NonNull String Hashtag) {
        Hashtag = Hashtag;
    }

    @NonNull
    public String getImagePath() {
        return imagePath;
    }

    public void setImagePath(@NonNull String ImagePath) {
        imagePath = ImagePath;
    }
}
