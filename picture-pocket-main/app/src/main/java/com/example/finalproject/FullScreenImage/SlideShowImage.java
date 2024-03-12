package com.example.finalproject.FullScreenImage;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.example.finalproject.Image.Image;
import com.example.finalproject.Type.UriTypeAdapter;
import com.example.finalproject.databinding.ActivitySlideShowImageBinding;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

public class SlideShowImage extends AppCompatActivity {

    ActivitySlideShowImageBinding binding;
    ViewPager2 viewPager2;

    List<Image> imageList;

    Handler mHandler = new Handler(Looper.getMainLooper());

    Runnable mRunnable = () -> {
        int currentPosition = viewPager2.getCurrentItem();
        if (currentPosition == imageList.size() - 1)
            viewPager2.setCurrentItem(0);
        else viewPager2.setCurrentItem(currentPosition + 1);
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySlideShowImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewPager2 = binding.imageViewPager;

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            String jsonImagesList = bundle.getString("images");

            if (jsonImagesList != null) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Uri.class, new UriTypeAdapter())
                        .create();
                Type listType = new TypeToken<List<Image>>() {
                }.getType();
                imageList = gson.fromJson(jsonImagesList, listType);
//                imageList = ImageCRUD.get_instance().getImageList(getApplicationContext());
                FullScreenImageAdapter fullScreenImageAdapter = new FullScreenImageAdapter(imageList);
                binding.topAppBar.setNavigationOnClickListener((View v) -> finish());

                viewPager2.setAdapter(fullScreenImageAdapter);
                viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        super.onPageSelected(position);
                        mHandler.removeCallbacks(mRunnable);
                        mHandler.postDelayed(mRunnable, 1000);
                    }
                });
            }
        }
    }
}