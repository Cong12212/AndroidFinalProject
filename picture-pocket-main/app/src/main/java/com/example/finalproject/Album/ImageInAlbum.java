package com.example.finalproject.Album;

import static com.example.finalproject.Image.ImageFragment.imageAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Database.Database;
import com.example.finalproject.Database.Image.ImageAlbum;
import com.example.finalproject.FullScreenImage.FullScreenImage;
import com.example.finalproject.Image.Image;
import com.example.finalproject.Image.ImageCRUD;
import com.example.finalproject.Image.ImageFragmentAdapter;
import com.example.finalproject.Image.RecyclerData;
import com.example.finalproject.Image.helpers;
import com.example.finalproject.R;
import com.example.finalproject.Type.Folder;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.gson.Gson;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class ImageInAlbum extends Fragment {
    Database db;
    List<Image> images = null;
    List<String> dateTakeImage;
    MenuItem add_new_image;
    MenuItem choose_all_in_album;
    MenuItem clear_choose_in_album;
    MenuItem delete_in_album;
    BiConsumer<Integer, View> onItemClick;
    BiConsumer<Integer, View> onItemLongClick;
    private RecyclerView rcvView = null;
    private AlbumCRUD albumCRUD = null;
    private ProgressDialog progressDialog;
    private final AlbumFragment albumFragment = new AlbumFragment();
    private ArrayList<RecyclerData> viewListFragment = null;
    private ArrayList<RecyclerData> viewListDialog = null;
    private String albumName;

    public ImageInAlbum() {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        View view = inflater.inflate(R.layout.image_in_album_fragment, container, false);
        rcvView = view.findViewById(R.id.rcvImageInAlbum);

        Bundle args = getArguments();
        if (args != null) {
            albumName = args.getString("nameAlbum");
        }

        MaterialToolbar topAppBar1 = ((Activity) getContext()).findViewById(R.id.topAppBar1);
        topAppBar1.setVisibility(View.VISIBLE);

        topAppBar1.setNavigationOnClickListener(v -> requireActivity().getSupportFragmentManager().beginTransaction().remove(ImageInAlbum.this).commit());

        Toolbar topAppBar = ((Activity) getContext()).findViewById(R.id.topAppBar);
        topAppBar.setTitle("Album:" + albumName);


        rcvView.setHasFixedSize(true);
        rcvView.setLayoutManager(new GridLayoutManager(requireContext(), 4));

        albumCRUD = new AlbumCRUD(getActivity());
        Album album = albumCRUD.getAlbum(albumName);
        if (albumName.equals(Folder.BinAlbumName)) {
            images = new ArrayList<>();
            File folder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                folder = new File(this.getActivity().getExternalFilesDir(null) + Folder.binPath);
            } else {
                folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Folder.root + Folder.binPath);
            }
            if (folder.exists()) {
                File[] allFiles = folder.listFiles((dir, name) -> (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")));
                Log.d("image in album", "onCreateView: private foldersize: " + allFiles);

                for (File file : allFiles) {
                    if (file != null && file.exists()) {
                        Image image = new Image();
                        image.setPath(file.getPath());
                        image.setAlbumName(albumName);
                        image.setAddAt(new Date(file.lastModified()));
                        image.setUri(ImageCRUD.getFilePathToMediaUri(file.getPath(), getActivity()).toString());
                        try {
                            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                image.setDateTaken(new Date(exifInterface.getDateTime()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        images.add(image);
                    }
                }
            } else {
                Log.e("image in album", "onCreateView: private folder");
                folder.mkdirs();
            }
        } else if (albumName.equals(Folder.PrivateAlbumName)) {
            images = new ArrayList<>();
            File folder;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                folder = new File(this.getActivity().getExternalFilesDir(null) + Folder.privateAlbum);
            } else {
                folder = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + Folder.root + Folder.privateAlbum);
            }
            if (folder.exists()) {
                File[] allFiles = folder.listFiles((dir, name) -> (name.endsWith(".jpg") || name.endsWith(".jpeg") || name.endsWith(".png")));
                for (File file : allFiles) {
                    if (file != null && file.exists()) {
                        Image image = new Image();
                        image.setPath(file.getPath());
                        image.setAlbumName(albumName);
                        image.setAddAt(new Date(file.lastModified()));
                        try {
                            ExifInterface exifInterface = new ExifInterface(file.getAbsolutePath());
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                                image.setDateTaken(new Date(exifInterface.getDateTime()));
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        images.add(image);
                    }
                }
            }
        } else {
            images = albumCRUD.getImagesOfAlbum(album.getId());
        }

        TextView tvEmptyAlbum = view.findViewById(R.id.tvEmptyAlbum);

        dateTakeImage = generateDateString();
        displayToView(tvEmptyAlbum);

        onItemClick = (Integer integer, View view1) -> {
            Log.d("POSITION_Click", integer.toString());
            if (imageAdapter.getState() == ImageFragmentAdapter.State.MultipleSelect) {
                if (!viewListFragment.get(integer).imageData.isChecked()) {
                    viewListFragment.get(integer).imageData.setChecked(true);
                    images.get(viewListFragment.get(integer).index).setChecked(true);
                } else {
                    viewListFragment.get(integer).imageData.setChecked(false);
                    images.get(viewListFragment.get(integer).index).setChecked(false);
                }
                helpers.moveCheck(images, imageAdapter, integer);
            } else {
                Intent intent = new Intent(getContext(), FullScreenImage.class);
                Bundle bundle = new Bundle();
                Gson gson = new Gson();
                String jsonImagesList = gson.toJson(images);
                bundle.putString("images", jsonImagesList);
                bundle.putString("albumName", albumName);

                bundle.putString("path", viewListFragment.get(integer).imageData.getPath());
                bundle.putInt("position", images.indexOf(viewListFragment.get(integer).imageData));
                intent.putExtras(bundle);
                startActivity(intent);
            }
        };

        onItemLongClick = (Integer integer, View view1) -> {
            Log.d("POSITION_longClick", integer.toString() + "|" + viewListFragment.get(integer).index);
            imageAdapter.setState(ImageFragmentAdapter.State.MultipleSelect);
            viewListFragment.get(integer).imageData.setChecked(true);
            images.get(viewListFragment.get(integer).index).setChecked(true);
            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount());
            choose_all_in_album.setVisible(false);
            delete_in_album.setVisible(true);
            add_new_image.setVisible(false);
        };

        imageAdapter = new ImageFragmentAdapter(viewListFragment, onItemClick, onItemLongClick);
        rcvView.setAdapter(imageAdapter);
        return view;
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.add_new_image) {
            Bundle args = getArguments();
            if (args != null) {
                albumName = args.getString("nameAlbum");
            }
            openImageSelectionDialog(albumName);
            return true;
        }
        if (item.getItemId() == R.id.choose_all_in_album) {
            if (this.images == null || this.images.size() == 0) {
                Toast.makeText(getContext(), "Album is empty", Toast.LENGTH_SHORT).show();
                return true;
            }
            imageAdapter.setCheckAllImage(true);
            choose_all_in_album.setVisible(false);
            delete_in_album.setVisible(true);
            add_new_image.setVisible(false);
            return true;
        }
        if (item.getItemId() == R.id.clear_choose_in_album) {
//            imageAdapter.setState(ImageFragmentAdapter.State.Normal);
//            imageAdapter.notifyItemRangeChanged(0, imageAdapter.getItemCount());
            if (imageAdapter.getState() != ImageFragmentAdapter.State.MultipleSelect) {
                imageAdapter.setState(ImageFragmentAdapter.State.MultipleSelect);
            }
            delete_in_album.setVisible(false);
            choose_all_in_album.setVisible(true);
            add_new_image.setVisible(true);
            return true;
        }
        if (item.getItemId() == R.id.delete_in_album) {
            if (imageAdapter.getState() != ImageFragmentAdapter.State.MultipleSelect) {
                imageAdapter.setState(ImageFragmentAdapter.State.MultipleSelect);
            }
            delete_in_album.setVisible(false);
            choose_all_in_album.setVisible(true);
            add_new_image.setVisible(true);
            deleteInAlbum();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public List<String> generateDateString() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        for (Image img : images) {
            String formattedDate = format.format(img.getDateTaken());
            if (!dates.contains(formattedDate))
                dates.add(formattedDate);
        }
        return dates;
    }

    private void displayToViewDialog(TextView myTextView) {
        if (dateTakeImage.size() == 0) {
            myTextView.setVisibility(View.VISIBLE);
            return;
        }
        viewListDialog = new ArrayList<>();
        for (int j = 0; j < images.size(); j++) {
            viewListDialog.add(new RecyclerData(RecyclerData.Type.Image, images.get(j).getPath(), images.get(j), j));
        }
    }

    private void displayToView(TextView myTextView) {
        if (dateTakeImage.size() == 0) {
            myTextView.setVisibility(View.VISIBLE);
            return;
        }
        viewListFragment = new ArrayList<>();
        for (int i = 0; i < dateTakeImage.size(); i++) {
            for (int j = 0; j < images.size(); j++) {
                viewListFragment.add(new RecyclerData(RecyclerData.Type.Image, images.get(j).getPath(), images.get(j), j));
            }
        }
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.top_menu_image_library, menu);
        add_new_image = menu.findItem(R.id.add_new_image);
        clear_choose_in_album = menu.findItem(R.id.clear_choose_in_album);
        choose_all_in_album = menu.findItem(R.id.choose_all_in_album);
        delete_in_album = menu.findItem(R.id.delete_in_album);
        delete_in_album.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public void onStop() {
        super.onStop();
        Toolbar topAppBar = ((Activity) getContext()).findViewById(R.id.topAppBar);
        topAppBar.setTitle("18 Album");
        MaterialToolbar topAppBar1 = ((Activity) getContext()).findViewById(R.id.topAppBar1);
        topAppBar1.setVisibility(View.GONE);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Toolbar topAppBar = ((Activity) getContext()).findViewById(R.id.topAppBar);
        topAppBar.setTitle("18 Album");
        MaterialToolbar topAppBar1 = ((Activity) getContext()).findViewById(R.id.topAppBar1);
        topAppBar1.setVisibility(View.GONE);
    }

    private void openImageSelectionDialog(String albumName) {
        Activity activity = requireActivity();

        final Dialog dialog1 = new Dialog(requireContext());
        dialog1.setContentView(R.layout.dialog_new_album);

        Button btnCancelAddImageToAlbum = dialog1.findViewById(R.id.btnCancelAddImageToAlbum);
        Button btnAddImageToAlbum = dialog1.findViewById(R.id.btnAddImageToAlbum);

        rcvView = dialog1.findViewById(R.id.rcvAddImageToAlbum);
        rcvView.setLayoutManager(new GridLayoutManager(requireContext(), 4));
        rcvView.setHasFixedSize(true);
        rcvView.setNestedScrollingEnabled(true);

        ImageCRUD imageCRUD = ImageCRUD.get_instance();
        images = imageCRUD.getImageList(getContext());

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setTitle("Please wait");
        progressDialog.setCanceledOnTouchOutside(false);

        dateTakeImage = generateDateString();
        displayToViewDialog(null);
        onItemClick = (Integer integer, View view) -> {
            Log.d("POSITION_Click", integer.toString());
            imageAdapter.setState(ImageFragmentAdapter.State.MultipleSelect);
            RecyclerData selectedData = viewListDialog.get(integer);
            if (selectedData.imageData.isChecked()) {
                selectedData.imageData.setChecked(false);
                images.get(selectedData.index).setChecked(false);
            } else {
                selectedData.imageData.setChecked(true);
                images.get(selectedData.index).setChecked(true);
            }
            imageAdapter.notifyItemChanged(integer);
        };
        onItemLongClick = (Integer integer, View view) -> {
        };

        imageAdapter = new ImageFragmentAdapter(viewListDialog, onItemClick, onItemLongClick);
        rcvView.setAdapter(imageAdapter);
        dialog1.show();
        btnAddImageToAlbum.setOnClickListener(v -> {
            ArrayList<Image> selectedImages = images.stream().filter(Image::isChecked).collect(Collectors.toCollection(ArrayList::new));
            db = Database.getInstance(requireContext().getApplicationContext());
            albumCRUD = new AlbumCRUD(getActivity());
            Album album = albumCRUD.getAlbum(albumName);
            List<Image> temp = albumCRUD.getImagesOfAlbum(album.getId());
            int position = albumCRUD.getImagesOfAlbum(album.getId()).size();
            for (Image img : selectedImages) {
                boolean isAlreadyInAlbum = false;
                for (Image imageAlbum : temp) {
                    if (imageAlbum.getPath().equals(img.getPath())) {
                        isAlreadyInAlbum = true;
                        break;
                    }
                }
                if (!isAlreadyInAlbum) {
                    db.ImageAlbumDAO().insertImageAlbum(new ImageAlbum(
                            img.getPath(), db.AlbumDataDAO().getAlbumByName(albumName).getId()));
                    if (viewListFragment == null)
                        viewListFragment = new ArrayList<>();
                    viewListFragment.add(position, new RecyclerData(RecyclerData.Type.Image, img.getPath(), img, position));
                    imageAdapter.setData(viewListFragment);
                    imageAdapter.notifyItemInserted(position);
                    position = position + 1;
                    Toast.makeText(activity.getApplicationContext(), "Thêm ảnh thành công", Toast.LENGTH_SHORT).show();
                    onResume();
                }
            }
            dialog1.dismiss();
            imageAdapter.notifyDataSetChanged();
            ImageInAlbum.this.updateData();
        });
        btnCancelAddImageToAlbum.setOnClickListener(v -> dialog1.dismiss());
    }

    private void deleteInAlbum() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        ArrayList<Image> selectedImages = images.stream().filter(Image::isChecked).collect(Collectors.toCollection(ArrayList::new));

        builder.setTitle("Xóa " + selectedImages.size() + " ảnh đã chọn?");
        builder.setPositiveButton("Xóa khỏi album", (dialog, id) -> {
            db = Database.getInstance(requireContext().getApplicationContext());
            for (Image img : selectedImages) {
                db.ImageAlbumDAO().deleteImageAlbum(new ImageAlbum(
                        img.getPath(), db.AlbumDataDAO().getAlbumByName(albumName).getId())
                );
            }
            imageAdapter.notifyDataSetChanged();
        });

        builder.setNegativeButton("Xóa hẳn", (dialog, id) -> {
            db = Database.getInstance(requireContext().getApplicationContext());
            for (Image img : selectedImages) {
                db.ImageAlbumDAO().deleteImageAlbum(new ImageAlbum(
                        img.getPath(), db.AlbumDataDAO().getAlbumByName(albumName).getId())
                );
                String bin = Environment.getExternalStorageDirectory().getAbsolutePath() + Folder.root + "/RecycleBin";
                File directory = new File(bin);
                if (!directory.exists())
                    directory.mkdirs();
                for (Image image : selectedImages) {
                    helpers.moveImage(image.getPath(), bin);
                }
            }
            imageAdapter.notifyDataSetChanged();
        });

        builder.setNeutralButton("Hủy", (dialog, id) -> dialog.dismiss());

        onResume();
        AlertDialog dialog = builder.create();
        dialog.show();
    }

    @Override
    public void onResume() {
        super.onResume();
        updateData();
//        imageAdapter = new ImageFragmentAdapter(viewListFragment, onItemClick, onItemLongClick);
//        rcvView.setAdapter(imageAdapter);
    }

    public void sortImageByDateTaken(boolean isASC) {
        Comparator<Image> comparator = Comparator.comparing(Image::getDateTaken);
        if (!isASC)
            comparator = comparator.reversed();
        Collections.sort(images, comparator);
    }

    private void updateData() {
        Album album = albumCRUD.getAlbum(albumName);
        images = albumCRUD.getImagesOfAlbum(album.getId());
        dateTakeImage = generateDateString();
        imageAdapter.notifyDataSetChanged();
    }
}