package com.example.finalproject;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.finalproject.Album.AlbumFragment;
import com.example.finalproject.Image.ImageFragment;
import com.example.finalproject.SearchAlbum.SearchFragment;
import com.example.finalproject.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    public static String[] storage_permissions = {
            Manifest.permission.WRITE_EXTERNAL_STORAGE,
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String[] storage_permissions_31 = {
            Manifest.permission.READ_MEDIA_IMAGES,
            Manifest.permission.READ_MEDIA_AUDIO,
            Manifest.permission.READ_MEDIA_VIDEO,
            Manifest.permission.MANAGE_EXTERNAL_STORAGE,
            Manifest.permission.CAMERA
    };
    Toolbar toolbar;
    ActivityMainBinding binding;
    int fragmentCurrent = 1;

    public static String[] permissions() {
        String[] p;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            p = storage_permissions_31;
        else p = storage_permissions;

        return p;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        ActivityCompat.requestPermissions(MainActivity.this,
                permissions(),
                1);
        setContentView(binding.getRoot());
        toolbar = binding.topAppBar;
        setSupportActionBar(toolbar);
        replaceFragment(new ImageFragment());
        binding.bottomView.setOnItemSelectedListener(item -> {
            if (item.getItemId() == R.id.image_option && fragmentCurrent != 1) {
                fragmentCurrent = 1;
                replaceFragment(new ImageFragment());
            } else if (item.getItemId() == R.id.album_option && fragmentCurrent != 2) {
                fragmentCurrent = 2;
                replaceFragment(new AlbumFragment());
            } else if (item.getItemId() == R.id.search_option && fragmentCurrent != 3) {
                fragmentCurrent = 3;
                replaceFragment(new SearchFragment());
            }
            return true;
        });

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                replace();
            } else {
                Toast.makeText(MainActivity.this, "Permission denied!", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void replace() {
        if (fragmentCurrent == 1) replaceFragment(new ImageFragment());
        else if (fragmentCurrent == 2) replaceFragment(new AlbumFragment());
        else if (fragmentCurrent == 3) replaceFragment(new SearchFragment());
    }

    @Override
    protected void onResume() {
        super.onResume();
        replace();
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }

}