package com.example.finalproject.Image;

import android.util.Log;

import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class helpers {

    public static void moveImage(String pathFile, String pathDir) {
        try {
            FileUtils.copyFileToDirectory(pathFile, pathDir);
            File file = new File(pathFile);
            if (file.exists()) {
                if (file.delete()) {
                    Log.d("delete", "file Deleted :" + pathFile);
                } else {
                    Log.d("delete", "file not Deleted :" + pathFile);
                }
            }
            //FileUtils.fileDelete(pathFile);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static void moveCheck(List<Image> images, ImageFragmentAdapter imageFragmentAdapter, Integer integer) {
        boolean check = false;
        for (int i = 0; i < images.size(); i++) {
            Image image = images.get(i);
            if (image.isChecked()) {
                check = true;
                Log.e("CHECK_HERE: ", Integer.toString(i));
            }
        }
        if (!check) {
            Log.d("CLEAR_CHECK", "SUCCESS");
            imageFragmentAdapter.setState(ImageFragmentAdapter.State.Normal);
            imageFragmentAdapter.notifyItemRangeChanged(0, imageFragmentAdapter.getItemCount());
        } else imageFragmentAdapter.notifyItemChanged(integer);
    }
}
