package com.example.finalproject.Image;

import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;
import android.util.Log;

import com.example.finalproject.Type.Folder;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class ImageCRUD {

    private static ImageCRUD _instance;
    private List<Image> imageList;

    private ImageCRUD() {
    }

    public static ImageCRUD getInstance() {
        if (_instance == null) {
            _instance = new ImageCRUD();
        }
        return _instance;
    }

    public static Uri getFilePathToMediaUri(String songPath, Context context) {
        long id = 0;
        ContentResolver cr = context.getContentResolver();

        Uri uri = MediaStore.Files.getContentUri("external");
        String selection = MediaStore.Audio.Media.DATA;
        String[] selectionArgs = {songPath};
        String[] projection = {MediaStore.Audio.Media._ID};
        String sortOrder = MediaStore.Audio.Media.TITLE + " ASC";

        Cursor cursor = cr.query(uri, projection, selection + "=?", selectionArgs, null);

        if (cursor != null) {
            while (cursor.moveToNext()) {
                int idIndex = cursor.getColumnIndex(MediaStore.Audio.Media._ID);
                id = Long.parseLong(cursor.getString(idIndex));
            }
        }
        Uri Uri_one = ContentUris.withAppendedId(MediaStore.Images.Media.getContentUri("external"), id);

        return Uri_one;
    }

    public static ImageCRUD get_instance() {
        if (_instance == null) {
            _instance = new ImageCRUD();
        }
        return _instance;
    }

    public static void set_instance(ImageCRUD _instance) {
        ImageCRUD._instance = _instance;
    }

    public List<Image> getImageList(Context context) {
        setImageList(LoadListImage(context));
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    private List<Image> LoadListImage(Context context) {
        Uri uri;
        Cursor cursor;
        int index_data;
        ArrayList<Image> listOfAllImages = new ArrayList<>();
        String absolutePathOfImage;
        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA,
                MediaStore.Images.Media.BUCKET_DISPLAY_NAME,
                MediaStore.Images.Media.DATE_TAKEN,
                MediaStore.Images.Media.DATE_ADDED,
                MediaStore.Images.Media._ID
        };

        String orderBy = MediaStore.Video.Media.DATE_MODIFIED;
        cursor = context.getContentResolver().query(uri, projection,
                null, null, orderBy + " DESC");
        index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);

        int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID);
        while (cursor.moveToNext()) {
            String absolutePath = cursor.getString(index_data);
            if (absolutePath.contains("/" + Folder.binPath + "/")) {
                continue;
            }
            int dateTakenColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_TAKEN);
            int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATE_ADDED);
            absolutePathOfImage = cursor.getString(index_data);

            File file = new File(absolutePathOfImage);
            if (!file.exists() || file.isDirectory()) {
                continue;
            }

            Date dateTaken = new Date(cursor.getLong(dateTakenColumn));
            Date addAt = new Date(cursor.getLong(dateAddedColumn) * 1000L);

            Image image = new Image();
            image.setPath(absolutePathOfImage);
            image.setDateTaken(dateTaken);
            image.setAddAt(addAt);

            long id = cursor.getLong(idColumn);
            Uri contentUri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id);
            image.setUri(contentUri.toString());

            Log.d("Path file: ", image.getPath());
            Log.d("PHOTO DATE", "Date Taken: " + dateTaken + " Created At: " + addAt);

            listOfAllImages.add(image);
        }
        cursor.close();
        Log.d("LOAD_DATA", Integer.toString(listOfAllImages.size()));
        return listOfAllImages;
    }

}
