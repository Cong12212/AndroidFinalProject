package com.example.finalproject.Image;

import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.provider.MediaStore;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.finalproject.FullScreenImage.FullScreenImage;
import com.example.finalproject.FullScreenImage.SlideShowImage;
import com.example.finalproject.R;
import com.example.finalproject.Type.Folder;
import com.example.finalproject.databinding.FragmentImageBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;
import com.google.gson.Gson;
import com.itextpdf.text.Document;
import com.itextpdf.text.pdf.PdfWriter;
import com.squareup.gifencoder.FloydSteinbergDitherer;
import com.squareup.gifencoder.GifEncoder;
import com.squareup.gifencoder.ImageOptions;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ImageFragment extends Fragment {

    public static ImageFragmentAdapter imageAdapter;
    public int gridViewNumber = 4;
    FragmentImageBinding binding;
    FragmentActivity activity;
    GridLayoutManager gridLayoutManager;
    BiConsumer<Integer, View> onItemClick;
    BiConsumer<Integer, View> onItemLongClick;
    ActivityResultLauncher<String> requestPermissionLauncher;
    ActivityResultLauncher<Intent> activityResultLauncher;
    List<Image> images;
    List<String> dateTakeImage;
    private RecyclerView rcvView = null;
    private ProgressDialog progressDialog;
    private TextView txtImageEmpty;
    private ArrayList<RecyclerData> viewList = null;
    private RecyclerView.LayoutManager layoutManager;
    private Uri imageUri;
    private int typeMode = 1;
    private boolean typeSort = false;

    public ImageFragment() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        activity = requireActivity();
        binding = FragmentImageBinding.inflate(inflater, container, false);
        rcvView = binding.fragmentImageRcv;
        txtImageEmpty = binding.textView;
        layoutManager = new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false);
        rcvView.setLayoutManager(layoutManager);
        rcvView.setHasFixedSize(true);
        rcvView.setNestedScrollingEnabled(true);
        ImageCRUD imageCRUD = ImageCRUD.get_instance();
        images = imageCRUD.getImageList(getContext());

        Toolbar topAppBar = ((Activity) getContext()).findViewById(R.id.topAppBar);
        topAppBar.setTitle("18 Album");
        MaterialToolbar topAppBar1 = ((Activity) getContext()).findViewById(R.id.topAppBar1);
        topAppBar1.setVisibility(View.GONE);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        setGridViewLayout(4);
        sortImageByDateTaken(typeSort);
        dateTakeImage = generateDateString(typeMode);
        displayToView(typeMode);


        if (!images.isEmpty()) {
            txtImageEmpty.clearComposingText();
        }//}
        onItemClick = (Integer integer, View view) -> {
            Log.d("POSITION_Click", integer.toString());
            if (imageAdapter.getState() == ImageFragmentAdapter.State.MultipleSelect) {
                if (!viewList.get(integer).imageData.isChecked()) {
                    viewList.get(integer).imageData.setChecked(true);
                    images.get(viewList.get(integer).index).setChecked(true);
                } else {
                    viewList.get(integer).imageData.setChecked(false);
                    images.get(viewList.get(integer).index).setChecked(false);
                }
                helpers.moveCheck(images, imageAdapter, integer);
                requireActivity().invalidateOptionsMenu();
            } else {

                Intent intent = new Intent(getContext(), FullScreenImage.class);
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                String jsonImagesList = gson.toJson(images);
                bundle.putString("albumName", "");

                bundle.putString("images", jsonImagesList);
                bundle.putString("path", viewList.get(integer).imageData.getPath());
                bundle.putInt("position", images.indexOf(viewList.get(integer).imageData));
                intent.putExtras(bundle);
                activity.startActivity(intent);
            }
        };

        onItemLongClick = (Integer integer, View view) -> {
            Log.d("POSITION_longClick", integer.toString() + "|" + viewList.get(integer).index);
            imageAdapter.setState(ImageFragmentAdapter.State.MultipleSelect);
            viewList.get(integer).imageData.setChecked(true);
            images.get(viewList.get(integer).index).setChecked(true);
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount());
            requireActivity().invalidateOptionsMenu();
        };

        imageAdapter = new ImageFragmentAdapter(viewList, onItemClick, onItemLongClick);
        rcvView.setAdapter(imageAdapter);
        requestPermissionLauncher = registerForActivityResult(new ActivityResultContracts.RequestPermission(),
                isGranted -> {
                    if (isGranted) {
                        ContentValues values = new ContentValues();
                        values.put(MediaStore.Images.Media.TITLE, "New image");
                        values.put(MediaStore.Images.Media.DESCRIPTION, "From your Camera");
                        imageUri = requireContext().getContentResolver().insert(
                                MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values);
                        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                        intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
                        activityResultLauncher.launch(intent);
                    } else {
                        Toast.makeText(getContext(), "There is no app that support this action", Toast.LENGTH_SHORT)
                                .show();
                    }
                });
        activityResultLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    getActivity();
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Log.e("ACTION_IMAGE_CAPTURE", "success");
                    } else {
                        Log.e("ACTION_IMAGE_CAPTURE", "fail");
                        requireContext().getContentResolver().delete(imageUri, null, null);
                    }
                });

        return binding.getRoot();
    }

    @Override
    public void onResume() {
        super.onResume();
        clearChoose();
        ImageCRUD imageCRUD = ImageCRUD.get_instance();
        images = imageCRUD.getImageList(getContext());
        setGridViewLayout(4);
        sortImageByDateTaken(typeSort);
        dateTakeImage = generateDateString(typeMode);
        displayToView(typeMode);
        imageAdapter.setData(viewList);
    }

    private boolean isCameraAvailable(Context context) {
        PackageManager packageManager = context.getPackageManager();
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        List<ResolveInfo> cameraApps = packageManager.queryIntentActivities(intent, 0);
        return cameraApps != null && !cameraApps.isEmpty();
    }

    public void sortImageByDateTaken(boolean isASC) {
        Comparator<Image> comparator = Comparator.comparing(Image::getDateTaken);
        if (!isASC)
            comparator = comparator.reversed();
        Collections.sort(images, comparator);
    }

    public List<String> generateDateString(int type) {

        List<String> dates = new ArrayList<>();
        SimpleDateFormat format;
        switch (type) {
            case 2:
                format = new SimpleDateFormat("MM/yyyy");
                break;
            case 3:
                format = new SimpleDateFormat("yyyy");
                break;
            default:
                format = new SimpleDateFormat("dd/MM/yyyy");
                break;
        }
        Log.d("imagesSize", Integer.toString(images.size()));
        for (Image img : images) {
            String formattedDate = format.format(img.getDateTaken());
            if (!dates.contains(formattedDate))
                dates.add(formattedDate);
        }
        return dates;
    }

    private void displayToView(int type) {
        if (dateTakeImage.size() == 0) {
            txtImageEmpty.setVisibility(View.VISIBLE);
            return;
        }
        txtImageEmpty.setVisibility(View.INVISIBLE);
        SimpleDateFormat format;
        switch (type) {
            case 2:
                format = new SimpleDateFormat("MM/yyyy");
                break;
            case 3:
                format = new SimpleDateFormat("yyyy");
                break;
            default:
                format = new SimpleDateFormat("dd/MM/yyyy");
                break;
        }
        viewList = new ArrayList<>();
        for (int i = 0; i < dateTakeImage.size(); i++) {
            String str = dateTakeImage.get(i);
            viewList.add(new RecyclerData(RecyclerData.Type.Label, str, null, i));
            for (int j = 0; j < images.size(); j++) {
                if (format.format(images.get(j).getDateTaken()).equals(str)) {
                    viewList.add(new RecyclerData(RecyclerData.Type.Image, images.get(j).getPath(), images.get(j), j));
                }
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.top_menu_image, menu);
        if (imageAdapter.getState() == ImageFragmentAdapter.State.Normal) {
            menu.getItem(1).setVisible(false);
            menu.getItem(3).setVisible(false);
            menu.getItem(4).setVisible(false);
            menu.getItem(5).setVisible(false);
            menu.getItem(6).setVisible(false);
            menu.getItem(7).setVisible(false);
        } else if (imageAdapter.getState() == ImageFragmentAdapter.State.MultipleSelect) {
            menu.getItem(0).setVisible(false);
        }
    }

    public void setGridViewLayout(int column) {
        gridViewNumber = column;
        gridLayoutManager = new GridLayoutManager(getContext(), gridViewNumber);
        gridLayoutManager.setSpanSizeLookup(new GridLayoutManager.SpanSizeLookup() {
            @Override
            public int getSpanSize(int position) {
                return viewList.get(position).type == RecyclerData.Type.Label ? gridViewNumber : 1;
            }
        });
        binding.fragmentImageRcv.setLayoutManager(gridLayoutManager);
    }

    private void convertImagesToPDF() {
        progressDialog.setMessage("Convert to PDF .... ");
        progressDialog.show();
        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> {
            List<Image> images2PDF = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i).isChecked()) {
                    images2PDF.add(images.get(i));
                }
            }

            // create PDF from images
            try {
                File root = new File(Environment.getExternalStorageDirectory() + Folder.root, Folder.PdfFolder);
                Log.d("FILE_DIR", root.getAbsolutePath());
                if (!root.exists())
                    root.mkdirs();
                long timestamp = System.currentTimeMillis();
                String fileName = "PDF_" + timestamp + ".pdf";
                File file = new File(root, fileName);
                if (file.exists()) {
                    file.delete();
                }
                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(file.getAbsoluteFile()));
                document.open();
                for (int i = 0; i < images2PDF.size(); i++) {
                    Bitmap bmp = BitmapFactory.decodeFile(images2PDF.get(i).getPath());
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();
                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    com.itextpdf.text.Image image = com.itextpdf.text.Image.getInstance(stream.toByteArray());
                    float scaler = ((document.getPageSize().getWidth() - document.leftMargin()
                            - document.rightMargin() - 0) / image.getWidth()) * 100;
                    image.scalePercent(scaler);
                    image.setAlignment(com.itextpdf.text.Image.ALIGN_CENTER | com.itextpdf.text.Image.ALIGN_TOP);
                    document.add(image);
                    progressDialog.setMessage("Convert to PDF .... ");

                }
                document.close();
                handler.post(() -> {
                    progressDialog.dismiss();
                    Snackbar.make(requireView(), "Convert to PDF success", Snackbar.LENGTH_SHORT).show();
                });
            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e("RunConvert", "Run", e);
            }
        });
    }

    private void CreateGIF(long time) {
        progressDialog.setMessage("Convert to GIF .... ");
        progressDialog.show();

        ExecutorService executorService = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());
        executorService.execute(() -> {
            List<Image> images2GIF = new ArrayList<>();
            for (int i = 0; i < images.size(); i++) {
                if (images.get(i).isChecked()) {
                    images2GIF.add(images.get(i));
                }
            }

            try {
                File root = new File(Environment.getExternalStorageDirectory(), Folder.root + Folder.GifFolder);
                Log.d("FILE_DIR", root.getAbsolutePath());
                if (!root.exists())
                    root.mkdirs();
                long timestamp = System.currentTimeMillis();
                String fileName = "Gif_" + timestamp + ".gif";
                File file = new File(root, "/" + fileName);

                try (FileOutputStream outputStream = new FileOutputStream(file.getPath())) {
                    ImageOptions options = new ImageOptions();
                    long timeDelay = time < 0 ? 0 : time * 1000;
                    options.setDelay(timeDelay, TimeUnit.MILLISECONDS);
                    options.setDitherer(FloydSteinbergDitherer.INSTANCE);

                    int screenWidth = getResources().getDisplayMetrics().widthPixels;
                    int screenHeight = screenWidth - 200;
                    if (images2GIF.size() > 0) {
                        File temp = new File(images2GIF.get(0).getPath());
                        screenHeight = getImageHeight(temp);
                    }

                    Log.e("SizeImage: ", "" + screenWidth + " " + screenHeight);

                    GifEncoder gifEncoder = new GifEncoder(outputStream, screenWidth, screenHeight, 0);

                    for (int i = 0; i < images2GIF.size(); i++) {
                        Image image = images2GIF.get(i);
                        File fileTemp = new File(image.getPath());
                        gifEncoder.addImage(convertAndResizeImageToArray(fileTemp, screenWidth, screenHeight), options);
                    }
                    gifEncoder.finishEncoding();
                }

                handler.post(() -> {
                    progressDialog.dismiss();
                    Snackbar.make(requireView(), "Convert to GIF success", Snackbar.LENGTH_SHORT).show();
                });

            } catch (Exception e) {
                progressDialog.dismiss();
                Log.e("RunConvert", "Run", e);
            }
        });
    }


    public int[][] convertAndResizeImageToArray(File file, int targetWidth, int targetHeight) throws ExecutionException, InterruptedException {
        try {
            // Load the original bitmap without resizing
            Bitmap originalBitmap = Glide.with(getContext())
                    .asBitmap()
                    .load(file)
                    .centerCrop()
                    .submit(targetWidth, targetHeight)
                    .get();

            // Rotate image
            Matrix matrix = new Matrix();
            matrix.postRotate(-90);
            originalBitmap = Bitmap.createBitmap(originalBitmap, 0, 0, originalBitmap.getWidth(), originalBitmap.getHeight(), matrix, true);

            int width = originalBitmap.getWidth();
            int height = originalBitmap.getHeight();

            int[][] rgbArray = new int[width][height];
            for (int i = 0; i < width; i++) {
                for (int j = 0; j < height; j++) {
                    rgbArray[i][j] = originalBitmap.getPixel(i, j);
                }
            }
            return rgbArray;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public int getImageHeight(File file) {
        try {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeFile(file.getAbsolutePath(), options);
            return options.outHeight;
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    private void clearChoose() {
        imageAdapter.setState(ImageFragmentAdapter.State.Normal);
        imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount());
        activity.invalidateOptionsMenu();
    }

    private List<Image> getCheckedImage() {
        return images.stream().filter(Image::isChecked).collect(Collectors.toCollection(ArrayList::new));
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = requireActivity();
        if (item.getItemId() == R.id.choose_all) {
            imageAdapter.setCheckAllImage(true);
            activity.invalidateOptionsMenu();
            return true;
        }
        if (item.getItemId() == R.id.clear_choose) {
            clearChoose();
            return true;
        }
        if (item.getItemId() == R.id.img_camera) {
            if (isCameraAvailable(activity)) {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            } else
                Toast.makeText(activity.getApplicationContext(), "img_camera", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (item.getItemId() == R.id.delete_images) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            ArrayList<Image> selectedImages = images.stream().filter(Image::isChecked)
                    .collect(Collectors.toCollection(ArrayList::new));
            builder.setTitle(R.string.confirm);
            builder.setMessage("Xóa " + selectedImages.size() + " ảnh đã chọn?");
            builder.setPositiveButton(R.string.OK, (DialogInterface dialog, int which) -> {
                String bin = Environment.getExternalStorageDirectory().getAbsolutePath() + Folder.root + "/RecycleBin";
                File directory = new File(bin);
                if (!directory.exists())
                    directory.mkdirs();
                for (Image image : selectedImages) {
                    helpers.moveImage(image.getPath(), bin);
                }
                onResume();
                activity.invalidateOptionsMenu();
                Snackbar.make(requireView(), R.string.removeSuccess, Snackbar.LENGTH_SHORT).show();
            });

            builder.setNegativeButton(R.string.CANCLE, (DialogInterface dialog, int which) -> {
                dialog.dismiss();
            });
            AlertDialog alert = builder.create();
            imageAdapter.setState(ImageFragmentAdapter.State.Normal);
            alert.show();
            return true;
        }
        if (item.getItemId() == R.id.move_images) {
            Toast.makeText(activity.getApplicationContext(), "move_images", Toast.LENGTH_SHORT).show();
            return true;
        }
        if (item.getItemId() == R.id.create_GIF) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle("Nhập thời gian delay");
            final EditText input = new EditText(getContext());
            input.setInputType(InputType.TYPE_CLASS_NUMBER);
            builder.setView(input);
            builder.setPositiveButton("OK", (DialogInterface dialog, int which) -> {
                String m_Text = input.getText().toString();
                int time = Integer.parseInt(m_Text);
                time = time * 1000;
                new Handler().postDelayed(() -> {
                    CreateGIF(Integer.parseInt(m_Text));
                }, time);
            });
            builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });
            builder.show();

            return true;
        }
        if (item.getItemId() == R.id.slideShow) {
            Intent intent = new Intent(getContext(), SlideShowImage.class);
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            List<Image> temp = getCheckedImage();
            String jsonImagesList = gson.toJson(temp);
            bundle.putString("images", jsonImagesList);
            intent.putExtras(bundle);
            activity.startActivity(intent);
            return true;
        }
        if (item.getItemId() == R.id.create_PDF) {
            AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
            builder.setTitle(R.string.ConverPDF).setMessage("Convert selected images to PDF")
                    .setPositiveButton("Convert Selected", (DialogInterface dialog, int which) -> {
                        convertImagesToPDF();
                    })
                    .setNegativeButton("Cancel", (DialogInterface dialog, int which) -> {
                        dialog.dismiss();
                    }).show();

            return true;
        }
//        if (item.getItemId() == R.id.img_choose) {
//            Toast.makeText(activity.getApplicationContext(), "img_choose", Toast.LENGTH_SHORT).show();
//            return true;
//        }
        if (item.getItemId() == R.id.img_grid_col_2) {
            setGridViewLayout(2);
            return true;
        }
        if (item.getItemId() == R.id.img_grid_col_3) {
            setGridViewLayout(3);
            return true;
        }
        if (item.getItemId() == R.id.img_grid_col_4) {
            setGridViewLayout(4);
            return true;
        }
        if (item.getItemId() == R.id.img_grid_col_5) {
            setGridViewLayout(5);
            return true;
        }
        if (item.getItemId() == R.id.img_view_mode_normal) {
            this.typeSort = false;
            sortImageByDateTaken(this.typeSort);
            dateTakeImage = generateDateString(typeMode);
            displayToView(typeMode);
            imageAdapter.setData(viewList);

            return true;
        }
        if (item.getItemId() == R.id.img_view_mode_convert) {
            this.typeSort = true;
            sortImageByDateTaken(this.typeSort);
            dateTakeImage = generateDateString(typeMode);
            displayToView(typeMode);
            imageAdapter.setData(viewList);
            return true;
        }
        if (item.getItemId() == R.id.img_view_mode_day) {
            sortImageByDateTaken(this.typeSort);
            this.typeMode = 1;
            dateTakeImage = generateDateString(typeMode);
            displayToView(typeMode);
            imageAdapter.setData(viewList);
            return true;
        }
        if (item.getItemId() == R.id.img_view_mode_month) {
            sortImageByDateTaken(this.typeSort);
            this.typeMode = 2;
            dateTakeImage = generateDateString(typeMode);
            displayToView(typeMode);
            imageAdapter.setData(viewList);
            return true;
        }
        if (item.getItemId() == R.id.slideShow_) {
            Intent intent = new Intent(getContext(), SlideShowImage.class);
            Bundle bundle = new Bundle();
            Gson gson = new Gson();
            String jsonImagesList = gson.toJson(images);
            bundle.putString("images", jsonImagesList);
            intent.putExtras(bundle);
            activity.startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}