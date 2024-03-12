package com.example.finalproject.FullScreenImage;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.print.PrintHelper;
import androidx.viewpager2.widget.ViewPager2;

import com.example.finalproject.Album.AlbumFragment;
import com.example.finalproject.Database.Album.AlbumData;
import com.example.finalproject.Database.Database;
import com.example.finalproject.Database.Hashtag.HashtagData;
import com.example.finalproject.Database.Image.ImageAlbum;
import com.example.finalproject.Image.Image;
import com.example.finalproject.Image.helpers;
import com.example.finalproject.R;
import com.example.finalproject.Type.Folder;
import com.example.finalproject.Type.UriTypeAdapter;
import com.example.finalproject.databinding.ActivityFullScreenImageBinding;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import com.greenfrvr.hashtagview.HashtagView;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Type;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;


public class FullScreenImage extends AppCompatActivity implements View.OnClickListener {
    public Image imageCur;
    ActivityFullScreenImageBinding binding;
    ViewPager2 viewPager2;
    Database db;
    boolean btnClick = false;
    GestureDetector gestureDetector;
    Activity activity = this;
    List<Image> receivedImageList;
    String albumName = "";
    Integer position = 0;
    int STATE = 0;
    ActivityResultLauncher<Intent> mStartForResult;
    private FullScreenImageAdapter fullScreenImageAdapter;
    private ProgressDialog progressDialog;
    private Status status = Status.VisibleButton;

    protected void upDateState(String album_name) {
        this.albumName = album_name;
        if (albumName.isEmpty() || albumName.equals(Folder.FavoriteAlbumName)) {
            this.STATE = 0;
            return;
        }
        if (albumName.equals(Folder.BinAlbumName)) {
            this.STATE = 1;
            return;
        }
        if (albumName.equals(Folder.PrivateAlbumName)) {
            this.STATE = 2;
            return;
        }
        this.STATE = 3;
    }

    public void onAlbumClick() {
        Intent intent = new Intent(getApplicationContext(), AlbumFragment.class);
        Bundle bundle = new Bundle();
        Gson gson = new Gson();
        bundle.putString("albumName", albumName);
        intent.putExtras(bundle);
        activity.startActivity(intent);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityFullScreenImageBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());
        viewPager2 = binding.imageViewPager;
        binding.topAppBar.setNavigationOnClickListener((View v) -> {
            finish();
        });
        registerButton();
        db = Database.getInstance(this.getApplicationContext());

        gestureDetector = new GestureDetector(this, new GestureDetector.SimpleOnGestureListener() {
            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if (btnClick) {
                    btnClick = false;
                    return false;
                }
                setButtonVisible(status);
                return true;
            }
        });

        Bundle bundle = getIntent().getExtras();
        if (bundle != null) {
            upDateState(bundle.getString("albumName"));

            position = bundle.getInt("position");
            String jsonImagesList = bundle.getString("images");
            Log.d("position", position.toString());
            setButtonEnable();

            if (jsonImagesList != null) {
                Gson gson = new GsonBuilder()
                        .registerTypeAdapter(Uri.class, new UriTypeAdapter())
                        .create();
                Type listType = new TypeToken<List<Image>>() {
                }.getType();
                receivedImageList = gson.fromJson(jsonImagesList, listType);
                fullScreenImageAdapter = new FullScreenImageAdapter(receivedImageList);
                viewPager2.setAdapter(fullScreenImageAdapter);
                viewPager2.setCurrentItem(position, false);

                imageCur = receivedImageList.get(viewPager2.getCurrentItem());

                viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
                    @Override
                    public void onPageSelected(int position) {
                        //imageCur = receivedImageList.get(position);
                        if (checkImageAlbum(imageCur, Folder.FavoriteAlbumName)) {
                            binding.btnFav.setCompoundDrawableTintList(ColorStateList.valueOf(Color.RED));
                            binding.btnFav.setTooltipText("Bỏ thích");
                        } else {
                            binding.btnFav.setTooltipText("Thích ảnh");
                            binding.btnFav.setCompoundDrawableTintList(ColorStateList.valueOf(Color.BLACK));
                        }
                    }

                    @Override
                    public void onPageScrolled(
                            int position,
                            float positionOffset,
                            int positionOffsetPixels) {
                        imageCur = receivedImageList.get(position);
                        if (checkImageAlbum(imageCur, Folder.FavoriteAlbumName)) {
                            binding.btnFav.setCompoundDrawableTintList(ColorStateList.valueOf(Color.RED));
                            binding.btnFav.setTooltipText("Bỏ thích");
                        } else {
                            binding.btnFav.setTooltipText("Thích ảnh");
                            binding.btnFav.setCompoundDrawableTintList(ColorStateList.valueOf(Color.BLACK));
                        }
                    }

                });

                setButtonVisible(status);

                mStartForResult = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                        new ActivityResultCallback<ActivityResult>() {
                            @Override
                            public void onActivityResult(ActivityResult result) {
                                Intent intent = result.getData();
                                if (result.getResultCode() == Activity.RESULT_OK) {
                                    FullScreenImageAdapter adapter = new FullScreenImageAdapter(receivedImageList);
                                    viewPager2.setAdapter(adapter);
                                    viewPager2.setCurrentItem(receivedImageList.indexOf(imageCur), false);
                                }
                            }
                        });
            } else {
                Log.d("FullScreenImage", "jsonImagesList is null");
            }
        } else Log.d("FullScreenImage", "null");

    }

    void setButtonEnable() {
        if (STATE == 1) {
            binding.btnMore.setVisibility(View.INVISIBLE);
            binding.btnEdit.setVisibility(View.INVISIBLE);
            binding.btnFav.setVisibility(View.INVISIBLE);
            binding.btnHide.setVisibility(View.INVISIBLE);
            binding.btnHashtag.setVisibility(View.INVISIBLE);
            binding.btnShare.setVisibility(View.INVISIBLE);

            binding.btnRestor.setVisibility(View.VISIBLE);
        }
        if (STATE == 2) {
            binding.btnShare.setVisibility(View.INVISIBLE);
            binding.btnEdit.setVisibility(View.INVISIBLE);
            binding.btnFav.setVisibility(View.INVISIBLE);
            binding.btnHide.setVisibility(View.INVISIBLE);
            binding.btnHashtag.setVisibility(View.INVISIBLE);
            binding.btnRestor.setVisibility(View.VISIBLE);
        }

    }


    private Boolean checkImageAlbum(Image img, String albumName) {
        db = Database.getInstance(this.getApplicationContext());
        if (db.AlbumDataDAO().getAlbumByName(albumName) == null && !albumName.isEmpty()) {
            db.AlbumDataDAO().insertAlbum(new AlbumData(albumName));
        }
        return db.ImageAlbumDAO().checkImageInAlbumByName(albumName, img.getPath());
    }

    private void addImageAlbum(Image img, String albumName) {
        db = Database.getInstance(this.getApplicationContext());

        if (!checkImageAlbum(img, albumName)) {
            db.ImageAlbumDAO().insertImageAlbum(new ImageAlbum(
                    img.getPath(), db.AlbumDataDAO().getAlbumByName(albumName).getId())
            );
        }
    }

    private void deleteImageAlbum(Image img, String albumName) {
        db = Database.getInstance(this.getApplicationContext());
        if (checkImageAlbum(img, albumName)) {
            db.ImageAlbumDAO().deleteImageAlbum(new ImageAlbum(
                    img.getPath(), db.AlbumDataDAO().getAlbumByName(albumName).getId())
            );
        }
    }

    private void setButtonVisible(Status status) {
        int isVisibleButton = (status == Status.VisibleButton ? View.VISIBLE : View.INVISIBLE);
        this.status = (status == Status.VisibleButton ? Status.InVisibleButton : Status.VisibleButton);
        binding.bottomAppBar.setVisibility(isVisibleButton);
        binding.topAppBar.setVisibility(isVisibleButton);
    }

    @Override
    public boolean dispatchTouchEvent(MotionEvent event) {
        gestureDetector.onTouchEvent(event);
        return super.dispatchTouchEvent(event);
    }

    private void registerButton() {
        binding.btnDelete.setOnClickListener(this::onClick);
        binding.btnEdit.setOnClickListener(this::onClick);
        binding.btnFav.setOnClickListener(this::onClick);
        binding.btnMore.setOnClickListener(this::onClick);
        binding.btnHide.setOnClickListener(this::onClick);
        binding.btnShare.setOnClickListener(this::onClick);
        binding.btnHashtag.setOnClickListener(this::onClick);
        binding.btnRestor.setOnClickListener(this::onClick);
    }

    private void deleteHashtag(HashtagView hashtagView, Object item) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.confirm);
        builder.setMessage("Xoá hashtag đã chọn?");
        builder.setPositiveButton(R.string.OK, (DialogInterface dialog, int which) -> {
            hashtagView.removeItem(item);
            imageCur.removeHashtag((String) item);
        });
        builder.setNegativeButton(R.string.CANCLE, (DialogInterface dialog, int which) -> {
            dialog.dismiss();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    public void onAddHashtagView() {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.add_new_hashtag);
        Button confirmButton = dialog.findViewById(R.id.buttonConfirm);
        Button cancelButton = dialog.findViewById(R.id.buttonCancel);
        HashtagView hashtagView = dialog.findViewById(R.id.Hashtag);
        List<String> hashtags = db.hashtagDataDAO().geHashtagByImagePath(imageCur.getPath());
        imageCur.setHashtag(hashtags);
        hashtagView.setData(imageCur.getHashtag());
        hashtagView.addOnTagClickListener(new HashtagView.TagsClickListener() {
            @Override
            public void onItemClicked(Object item) {
                //String tag =  (String) item;
                deleteHashtag(hashtagView, item);
            }
        });
        EditText editTextHashtagName = dialog.findViewById(R.id.editTextHashtag);
        confirmButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String hashtag = editTextHashtagName.getText().toString();
                if (!hashtag.isEmpty() && !imageCur.getHashtag().contains(hashtag)) {
                    imageCur.addHashtag(hashtag);
                    hashtagView.addItem(hashtag);
                    db.hashtagDataDAO().insertHashtag(new HashtagData(hashtag, imageCur.getPath()));
                    //dialog.dismiss();
                } else {
                    Snackbar.make(dialog.getCurrentFocus().getRootView(), "Thêm Hashtag" + hashtag + " không thành công!", Snackbar.LENGTH_SHORT).show();
                }
            }
        });
        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    private String getExif(ExifInterface exif) {
        String atrribute = null;
        atrribute += "Length: " + exif.getAttribute(ExifInterface.TAG_IMAGE_LENGTH) + "\n";
        atrribute += "Width: " + exif.getAttribute(ExifInterface.TAG_IMAGE_WIDTH) + "\n";
        atrribute += "Location: ";
        if (exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) == "null") {
            atrribute += exif.getAttribute(ExifInterface.TAG_GPS_AREA_INFORMATION) + " ";
            atrribute += exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + "'";

            atrribute += ": " + exif.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF) + ", ";
            atrribute += ": " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE) + "'";
            atrribute += ": " + exif.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);
        }
        atrribute += "\nMaker: " + exif.getAttribute(ExifInterface.TAG_MAKE) + "\n";
        atrribute += "Model: " + exif.getAttribute(ExifInterface.TAG_MODEL) + "\n";
        atrribute += "Flash: " + exif.getAttribute(ExifInterface.TAG_FLASH) + "\n";
        atrribute += "Exposure time: " + exif.getAttribute(ExifInterface.TAG_EXPOSURE_TIME) + "\n";
        atrribute += "Orientation: " + exif.getAttribute(ExifInterface.TAG_ORIENTATION) + "\n";
        atrribute += "White balance: " + exif.getAttribute(ExifInterface.TAG_WHITE_BALANCE) + "\n";
        atrribute += "ISO: " + exif.getAttribute(ExifInterface.TAG_ISO) + "\n";
        return atrribute;
    }

    private void ImageInfo(Image img) {
        File file = new File(img.getPath());
        Dialog dialog = new Dialog(activity);
        String exifAttribute = null;
        String filename = file.getName();
        String ext = filename.substring(filename.lastIndexOf('.') + 1);
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        LinearLayout layout = new LinearLayout(activity);
        layout.setOrientation(LinearLayout.VERTICAL);
        TextView infor = new TextView(activity);
        TextView time = new TextView(activity);
        Button btnEdit = new Button(activity);
        LinearLayout layout1 = new LinearLayout(activity);
        ExifInterface exif = null;
        try {
            exif = new ExifInterface(file.toString());
            exifAttribute = getExif(exif);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy:MM:dd hh:mm:ss");
            String atrrTime = exif.getAttribute(ExifInterface.TAG_DATETIME);

            time.setText("Time: " + atrrTime);
            time.setTextSize(20);
            btnEdit.setCompoundDrawablesWithIntrinsicBounds(getResources().getDrawable(R.drawable.outline_edit_24), null, null, null);
            //ExifInterface finalExif1 = exif;
            btnEdit.setBackgroundTintList(ColorStateList.valueOf(Color.WHITE));
            ExifInterface finalExif = exif;
            btnEdit.setOnClickListener(view -> {
                Calendar calendar = Calendar.getInstance();
                try {
                    calendar.setTime(simpleDateFormat.parse(atrrTime));
                } catch (ParseException e) {
                    // TODO Auto-generated catch block
                    e.printStackTrace();
                }//
                final String[] timeAt = {null};

                int mYear = calendar.get(Calendar.YEAR);
                int mMonth = calendar.get(Calendar.MONTH);
                int mDay = calendar.get(Calendar.DAY_OF_MONTH);
                DatePickerDialog datePickerDialog = new DatePickerDialog(this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year,
                                                  int monthOfYear, int dayOfMonth) {
                                timeAt[0] = dayOfMonth + ":" + monthOfYear + ":" + year;
//                            Toast.makeText(activity,dayOfMonth + ":" + monthOfYear + ":" + year,Toast.LENGTH_LONG).show();
                            }
                        }, mYear, mMonth, mDay);
                datePickerDialog.show();

                int hour = calendar.get(Calendar.HOUR);
                int minute = calendar.get(Calendar.MINUTE);
                int second = calendar.get(Calendar.SECOND);
                TimePickerDialog timePickerDialog = new TimePickerDialog(this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay,
                                          int minute) {
                        timeAt[0] += " " + hourOfDay + ":" + minute + ":" + second;
//                        Toast.makeText(activity, hourOfDay + ":" + minute, Toast.LENGTH_LONG).show();
                    }
                }, hour, minute, DateFormat.is24HourFormat(this));
                timePickerDialog.show();
                finalExif.setAttribute(ExifInterface.TAG_DATETIME, timeAt[0]);
                try {
                    finalExif.saveAttributes();
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                time.setText("Time: " + timeAt[0]);
            });
            layout1.addView(time);
            layout1.addView(btnEdit);
            layout.addView(layout1);
        } catch (IOException e) {
            exifAttribute = "null";
        }
        exifAttribute += "Path: " + file.getPath();
        infor.setTextSize(20);
        infor.setText(exifAttribute);
        layout.addView(infor);
        builder.setView(layout);
        builder.setNegativeButton("CLOSE", null);
        AlertDialog alertDialog = builder.create();
        alertDialog.setTitle("[" + filename + "]");

        alertDialog.show();
    }

    @Override
    public void onClick(View v) {
        btnClick = true;
        if (v.getId() == R.id.btnFav) {
            if (albumName.equals(Folder.FavoriteAlbumName)) {
//                Log.d("FullImageRemoveItem", "Before: " + Integer.toString(receivedImageList.size()));
                deleteImageAlbum(imageCur, Folder.FavoriteAlbumName);
                receivedImageList.remove(imageCur);
                if (receivedImageList.isEmpty()) {
                    finish();
                    return;
                }
                FullScreenImageAdapter adapter = new FullScreenImageAdapter(receivedImageList);
                viewPager2.setAdapter(adapter);
                imageCur = receivedImageList.get(viewPager2.getCurrentItem());
//                Log.d("FullImageRemoveItem",Integer.toString(receivedImageList.size()));
            } else {
                if (checkImageAlbum(imageCur, Folder.FavoriteAlbumName)) {
                    binding.btnFav.setCompoundDrawableTintList(ColorStateList.valueOf(Color.BLACK));
                    binding.btnFav.setTooltipText("Thích ảnh");
                    deleteImageAlbum(imageCur, Folder.FavoriteAlbumName);
                    return;
                }
                addImageAlbum(imageCur, Folder.FavoriteAlbumName);
                binding.btnFav.setCompoundDrawableTintList(ColorStateList.valueOf(Color.RED));
                binding.btnFav.setTooltipText("Bỏ thích");
            }
            return;
        }
        if (v.getId() == R.id.btnDelete) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setMessage("Xóa ảnh?");
            builder.setPositiveButton(R.string.OK, (DialogInterface dialog, int which) -> {
                if (STATE == 1) {
                    File file = new File(imageCur.getPath());
                    List<ImageAlbum> imageAlbums = db.ImageAlbumDAO().getImageAlbumByAlbumId(db.AlbumDataDAO().getAlbumByName(albumName).getId());
                    String path = "";
                    for (ImageAlbum img : imageAlbums)
                        if (file.getName().equals(new File(img.getImagePath()).getName()))
                            path = img.getImagePath();
                    List<ImageAlbum> imgs = db.ImageAlbumDAO().getImageAlbumByImagePath(path);
                    for (int i = 0; i < imgs.size(); i++) {
                        db.ImageAlbumDAO().deleteImageAlbum(imgs.get(i));
                    }
                    file.delete();
                } else {
                    File directory;
                    String bin = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                        bin = this.getExternalFilesDir(null) + Folder.binPath;
                        directory = new File(bin);
                    } else {
                        bin = Environment.getExternalStorageDirectory().getAbsolutePath() + Folder.root + Folder.binPath;
                        directory = new File(bin);
                    }
                    if (!directory.exists())
                        directory.mkdirs();
                    helpers.moveImage(imageCur.getPath(), bin);
                    receivedImageList.remove(imageCur);
                }
                addImageAlbum(imageCur, Folder.BinAlbumName);
                FullScreenImageAdapter adapter = new FullScreenImageAdapter(receivedImageList);
                viewPager2.setAdapter(adapter);
                imageCur = receivedImageList.get(viewPager2.getCurrentItem());
                Toast.makeText(this, R.string.removeSuccess, Toast.LENGTH_SHORT).show();
            });

            builder.setNegativeButton(R.string.CANCLE, (DialogInterface dialog, int which) -> {
                dialog.dismiss();
            });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        if (v.getId() == R.id.btnEdit) {
            Intent editItent = new Intent(Intent.ACTION_EDIT);
            editItent.setType("image/*");
            editItent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            editItent.setData(Uri.parse(imageCur.getUri()));

            mStartForResult.launch(editItent);
            return;
        }
        if (v.getId() == R.id.btnHide) {
            Log.d("BinDir", Environment.getExternalStorageDirectory().getAbsolutePath());
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle(R.string.confirm);
            builder.setMessage("Ẩn ảnh đã chọn?");
            builder.setPositiveButton(R.string.OK, (DialogInterface dialog, int which) -> {
                File directory;
                String hint = "";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    hint = this.getExternalFilesDir(null) + Folder.privateAlbum;
                    directory = new File(hint);
                } else {
                    hint = Environment.getExternalStorageDirectory().getAbsolutePath() + Folder.root + Folder.privateAlbum;
                    directory = new File(hint);
                }

                if (!directory.exists())
                    directory.mkdirs();
                else Log.d("HintDir", hint);

                helpers.moveImage(imageCur.getPath(), hint);
                addImageAlbum(imageCur, Folder.PrivateAlbumName);

                activity.invalidateOptionsMenu();
                receivedImageList.remove(imageCur);
                FullScreenImageAdapter adapter = new FullScreenImageAdapter(receivedImageList);
                viewPager2.setAdapter(adapter);
                imageCur = receivedImageList.get(viewPager2.getCurrentItem());
                Snackbar.make(binding.getRoot(), R.string.hintSuccess, Snackbar.LENGTH_SHORT).show();
            });
            builder.setNegativeButton(R.string.CANCLE, (DialogInterface dialog, int which) -> {
                dialog.dismiss();
            });
            AlertDialog alert = builder.create();
            alert.show();
            return;
        }
        if (v.getId() == R.id.btnHashtag) {
            onAddHashtagView();
            return;
        }
        if (v.getId() == R.id.btnMore) {
            String date = imageCur.getDateTaken().toString();
            PopupMenu pm = new PopupMenu(activity, v);
            pm.getMenuInflater().inflate(R.menu.bottom_fullscreen_image, pm.getMenu());
            if (this.STATE == 3) {
                pm.getMenu().getItem(3).setVisible(false);
                pm.getMenu().getItem(4).setVisible(true);
            }
            if (this.STATE == 2) {
                pm.getMenu().getItem(0).setVisible(false);
                pm.getMenu().getItem(3).setVisible(false);
            }
            pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    int id = item.getItemId();
                    if (id == R.id.add_to_album) {
                        actionAddToAlbum();
                        return true;
                    }
                    if (id == R.id.action_use_as) {
                        Intent intent = new Intent(Intent.ACTION_ATTACH_DATA);
                        intent.setDataAndType(Uri.parse(imageCur.getUri()), "image/jpeg");
                        intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                        startActivity(Intent.createChooser(intent, "Use as"));
                        return true;
                    }
                    if (id == R.id.see_infor) {
                        ImageInfo(imageCur);
                        return true;
                    }
                    if (id == R.id.action_print) {
                        PrintHelper photoPrinter = new PrintHelper(activity);
                        photoPrinter.setScaleMode(PrintHelper.SCALE_MODE_FIT);
                        try (InputStream in = getContentResolver().openInputStream(Uri.parse(imageCur.getUri()))) {
                            Bitmap bitmap = BitmapFactory.decodeStream(in);
                            photoPrinter.printBitmap(String.format("print_%s", imageCur.getPath()), bitmap);
                        } catch (Exception e) {
                            Log.e("print", String.format("unable to print %s", Uri.parse(imageCur.getUri())), e);
                            Toast.makeText(getApplicationContext(), R.string.print_error, Toast.LENGTH_SHORT).show();
                        }
                        return true;
                    }
                    if (id == R.id.remove_from_album) {
                        deleteImageAlbum(imageCur, albumName);
                        receivedImageList.remove(imageCur);
                        FullScreenImageAdapter adapter = new FullScreenImageAdapter(receivedImageList);
                        viewPager2.setAdapter(adapter);
                        imageCur = receivedImageList.get(viewPager2.getCurrentItem());
                        return true;
                    }
                    return false;
                }
            });
            pm.show();

            return;
        }
        if (v.getId() == R.id.btnShare) {
            Intent shareIntent = new Intent();
            shareIntent.setAction(Intent.ACTION_SEND);
            shareIntent.putExtra(Intent.EXTRA_STREAM, imageCur.getUri());
            shareIntent.setType("image/jpeg");
            startActivity(Intent.createChooser(shareIntent, "Share"));
        }
        if (v.getId() == R.id.btnRestor) {
            File file = new File(imageCur.getPath());
            List<ImageAlbum> imageAlbums = db.ImageAlbumDAO().getImageAlbumByAlbumId(db.AlbumDataDAO().getAlbumByName(albumName).getId());
            File path = this.getExternalFilesDir(null);
            for (ImageAlbum img : imageAlbums) {
                path = new File(img.getImagePath());
                if (file.getName().equals(path.getName())) {
                    break;
                }
            }
            helpers.moveImage(imageCur.getPath(), path.getParent());
            imageCur.setPath(path.getPath());
            deleteImageAlbum(imageCur, albumName);
            receivedImageList.remove(imageCur);
            FullScreenImageAdapter adapter = new FullScreenImageAdapter(receivedImageList);
            viewPager2.setAdapter(adapter);
            imageCur = receivedImageList.get(viewPager2.getCurrentItem());
        }
    }

    private void actionAddToAlbum() {
        Dialog dialog = new Dialog(activity);
        dialog.setContentView(R.layout.add_to_album);
        Button addNewAlbumButton = dialog.findViewById(R.id.add_new_album);
        ListView albumsView = dialog.findViewById(R.id.list_view);
        final List<String> albums = db.AlbumDataDAO().getListNameAlbum();
        albums.remove(Folder.FavoriteAlbumName);
        albums.remove(Folder.PrivateAlbumName);
        albums.remove(Folder.BinAlbumName);
        albumsView.setAdapter(new ArrayAdapter<String>(activity, android.R.layout.simple_list_item_1, albums));
        albumsView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String alb = (String) parent.getItemAtPosition(position);
                if (!checkImageAlbum(imageCur, alb)) {
                    addImageAlbum(imageCur, alb);
                }
                dialog.dismiss();
            }
        });
        addNewAlbumButton.setOnClickListener(v -> {
            final Dialog newDialog = new Dialog(activity);
            newDialog.setContentView(R.layout.add_new_album);
            Button confirmButton = newDialog.findViewById(R.id.buttonConfirm);
            Button cancelButton = newDialog.findViewById(R.id.buttonCancel);
            final EditText editTextAlbumName = newDialog.findViewById(R.id.editTextAlbumName);
            confirmButton.setOnClickListener(v3 -> {
                String albumName = editTextAlbumName.getText().toString();
                if (!albumName.isEmpty()) {
                    if (!albums.contains(albumName)) {
                        addImageAlbum(imageCur, albumName);
                        albums.add(albumName);

                        albumsView.setAdapter(new ArrayAdapter<String>(activity,
                                android.R.layout.simple_list_item_1, albums));

                        addImageAlbum(imageCur, albumName);
                    }
                }
                newDialog.dismiss();
            });
            cancelButton.setOnClickListener(v1 -> {
                newDialog.dismiss();
            });
            newDialog.show();
        });
        dialog.show();
    }
}
