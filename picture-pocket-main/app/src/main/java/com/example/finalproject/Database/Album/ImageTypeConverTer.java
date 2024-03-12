package com.example.finalproject.Database.Album;

import androidx.room.TypeConverter;

import com.example.finalproject.Image.Image;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class ImageTypeConverTer {

    @TypeConverter
    public static List<Image> fromString(String value) {
        Type listType = new TypeToken<List<Image>>() {
        }.getType();
        return new Gson().fromJson(value, listType);
    }

    @TypeConverter
    public static String fromList(List<Image> list) {
        Gson gson = new Gson();
        return gson.toJson(list);
    }
}
