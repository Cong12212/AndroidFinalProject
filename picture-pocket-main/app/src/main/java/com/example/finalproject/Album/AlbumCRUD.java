package com.example.finalproject.Album;

import android.content.Context;
import android.media.ExifInterface;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

import com.example.finalproject.Database.Album.AlbumData;
import com.example.finalproject.Database.Album.AlbumDataDAO;
import com.example.finalproject.Database.Database;
import com.example.finalproject.Database.Image.ImageAlbum;
import com.example.finalproject.Image.Image;
import com.example.finalproject.Image.ImageCRUD;
import com.example.finalproject.R;
import com.example.finalproject.Type.Folder;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class AlbumCRUD {
    private static Context context = null;
    private List<Album> albumList = new ArrayList<>();
    private AlbumCRUD INSTANCE = null;

    public AlbumCRUD(Context context) {
        AlbumCRUD.context = context;
    }

    public List<Album> getAlbumList() {
        LoadAlbum();
        Log.d("AlbumCRUD", "Album List Size (getAlbumList): " + albumList.size());
        return albumList;
    }

    public void setAlbumList(List<Album> albumList) {
        this.albumList = albumList;
    }

    public AlbumCRUD getINSTANCE(Context context) {
        if (this.INSTANCE == null) {
            synchronized (AlbumCRUD.class) {
                if (this.INSTANCE == null) {
                    this.INSTANCE = new AlbumCRUD(context.getApplicationContext());
                }
            }
        }
        return INSTANCE;
    }

    public List<Image> getImagesAlbum(String albumName) {
        return null;
    }

    private void LoadAlbum() {
        this.albumList = new ArrayList<>();
        List<AlbumData> temp = Database.getInstance(context).AlbumDataDAO().getListAlbum();
        List<String> listAlbum = new ArrayList<>() {{
            add(Folder.BinAlbumName);
            add(Folder.FavoriteAlbumName);
            add(Folder.PrivateAlbumName);
        }};

        for (AlbumData albD : temp) {
            if (!listAlbum.contains(albD.getAlbumName())) {
                Album album = getAlbum(albD.getAlbumName());
                this.albumList.add(album);
            }
        }
    }

    private void createAlbumDefault() {
        String[] listAlbum = {Folder.FavoriteAlbumName, Folder.PrivateAlbumName, Folder.BinAlbumName};
        for (String name : listAlbum) {
            AlbumData albumData = Database.getInstance(context).AlbumDataDAO().getAlbumByName(name);
            if (albumData == null) {
                AlbumDataDAO albumDataDAO = Database.getInstance(context).AlbumDataDAO();
                if (albumDataDAO != null) {
                    Log.d("AlbumCRUD", "Inserting new AlbumData: " + name);
                    albumDataDAO.insertAlbum(new AlbumData(name));
                } else {
                    Log.e("AlbumCRUD", "AlbumDataDAO is null");
                }
            } else {
                Log.d("AlbumCRUD", "AlbumData already exists for name: " + name);
            }
        }
    }

    protected List<Album> getAlbumDefault() {
        createAlbumDefault();
        List<Album> albums = new ArrayList<>();
        String[] listAlbumName = {Folder.FavoriteAlbumName, Folder.PrivateAlbumName, Folder.BinAlbumName};
        Integer[] imageFolder = {R.drawable.favorite_album, R.drawable.private_album, R.drawable.bin};

        for (int i = 0; i < listAlbumName.length; i++) {
            String albumName = listAlbumName[i];
            Album album = getAlbum(albumName);
            album.setImageOfAlbum(imageFolder[i]);
            albums.add(album);
        }
        return albums;
    }

    public Album getAlbum(String name) {
        AlbumData albumData = Database.getInstance(context).AlbumDataDAO().getAlbumByName(name);
        if (albumData != null) {
//            Album album = new Album(albumData.getId(),null,albumData.getAlbumName(),getImagesOfAlbum(albumData.getId()));
//            return album;
//            List<Image> images = getImagesOfAlbum(albumData.getId()); // Lấy danh sách hình ảnh của album
//            Album album = new Album(albumData.getId(), null, albumData.getAlbumName(), images);
            List<Image> images = getImagesOfAlbumByName(albumData.getAlbumName()); // Lấy danh sách hình ảnh của album
            Album album = new Album(albumData.getId(), null, albumData.getAlbumName(), images);
            return album;
        }
        return null;
    }

    public List<Image> getImagesOfAlbum(int albumId) {
        List<Image> imageList = new ArrayList<>();
        AlbumData albumData = Database.getInstance(context).AlbumDataDAO().getAlbumById(albumId);
        if (albumData == null) return null;
        List<ImageAlbum> imageAlbumList = Database.getInstance(context).ImageAlbumDAO().getImageAlbumByAlbumId(albumId);
        for (int i = 0; i < imageAlbumList.size(); i++) {
            ImageAlbum imageAlbum = imageAlbumList.get(i);

            Image image = new Image();
            image.setId(Integer.toString(i));
            image.setPath(imageAlbum.getImagePath());
            image.setAlbumName(albumData.getAlbumName());
            File file = new File(image.getPath());
            image.setUri(ImageCRUD.getFilePathToMediaUri(file.getPath(), context.getApplicationContext()).toString());

            if (file != null && file.exists()) {
                image.setAddAt(new Date(file.lastModified()));
                try {
                    ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        image.setDateTaken(new Date(exifInterface.getDateTime()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
                imageList.add(image);
            }
        }
        return imageList;
    }

    protected void insertAlbum(String albumName) {

        Album album = getAlbum(albumName);
        if (album != null) {
            Toast.makeText(context.getApplicationContext(), "Exists", Toast.LENGTH_LONG).show();
        }
        if (albumName != "" && albumName != null) {
            Database.getInstance(context).AlbumDataDAO().insertAlbum(new AlbumData(albumName));
        }
    }

    public void deleteAlbumByName(String albumName) {
        AlbumData albumToDelete = Database.getInstance(context).AlbumDataDAO().getAlbumByName(albumName);
        if (albumToDelete != null) {
            int albumId = albumToDelete.getId();
            List<ImageAlbum> imagesToDelete = Database.getInstance(context).ImageAlbumDAO().getImageAlbumByAlbumId(albumId);
            for (ImageAlbum image : imagesToDelete) {
                Database.getInstance(context).ImageAlbumDAO().deleteImageAlbum(image);
            }
            Database.getInstance(context).AlbumDataDAO().deleteAlbum(albumToDelete);
        }
    }

    public List<Image> getImagesOfAlbumByName(String albumName) {
        List<Image> imageList = new ArrayList<>();
        AlbumData albumData = Database.getInstance(context).AlbumDataDAO().getAlbumByName(albumName);
        if (albumData == null) return null;
        int albumId = albumData.getId();
        List<ImageAlbum> imageAlbumList = Database.getInstance(context).ImageAlbumDAO().getImageAlbumByAlbumId(albumId);

        for (int i = 0; i < imageAlbumList.size(); i++) {
            ImageAlbum imageAlbum = imageAlbumList.get(i);

            Image image = new Image();
            image.setId(Integer.toString(i));
            image.setPath(imageAlbum.getImagePath());
            image.setAlbumName(albumData.getAlbumName());
            File file = new File(image.getPath());
            if (file != null && file.exists()) {
                image.setAddAt(new Date(file.lastModified()));
                try {
                    ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                        image.setDateTaken(new Date(exifInterface.getDateTime()));
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            imageList.add(image);
        }
        return imageList;
    }
}
