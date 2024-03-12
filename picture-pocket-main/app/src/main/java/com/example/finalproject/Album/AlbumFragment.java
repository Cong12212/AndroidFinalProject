package com.example.finalproject.Album;

import static com.example.finalproject.Image.ImageFragment.imageAdapter;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.finalproject.Database.Database;
import com.example.finalproject.Database.Image.ImageAlbum;
import com.example.finalproject.Image.Image;
import com.example.finalproject.Image.ImageCRUD;
import com.example.finalproject.Image.ImageFragmentAdapter;
import com.example.finalproject.Image.RecyclerData;
import com.example.finalproject.R;
import com.example.finalproject.databinding.FragmentAlbumBinding;
import com.google.android.material.appbar.MaterialToolbar;
import com.google.android.material.snackbar.Snackbar;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

public class AlbumFragment extends Fragment implements AlbumAdapter.OnAlbumClickListener, AlbumAdapter.OnAlbumDeleteListener {

    Database db;
    List<Image> images;
    List<String> dateTakeImage;
    boolean isDeleteMode = false;
    BiConsumer<Integer, View> onItemClick;
    BiConsumer<Integer, View> onItemLongClick;
    private FragmentAlbumBinding binding;
    private AlbumAdapter albumAdapter;
    private AlbumAdapter albumDeFautAdapter;
    private List<Album> albumsDeFaut;
    private List<Album> albums;
    private RecyclerView recyclerView1;
    private RecyclerView recyclerView2;
    private MenuItem clearChooseItem;
    private MenuItem actionEdit;
    private MenuItem actionAdd;
    private RecyclerView rcvView = null;
    private AlbumCRUD albumCRUD = null;
    private ProgressDialog progressDialog;
    private ArrayList<RecyclerData> viewList = null;


    public AlbumFragment() {
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentAlbumBinding.inflate(inflater, container, false);
        setHasOptionsMenu(true);

        Toolbar topAppBar = ((Activity) getContext()).findViewById(R.id.topAppBar);
        topAppBar.setTitle("18 Album");
        MaterialToolbar topAppBar1 = ((Activity) getContext()).findViewById(R.id.topAppBar1);
        topAppBar1.setVisibility(View.GONE);

        albumCRUD = new AlbumCRUD(getActivity());
        ///////////////////
        recyclerView1 = binding.folderDeFautPicturesRecView;
        recyclerView1.setHasFixedSize(true);
        recyclerView1.setLayoutManager(new GridLayoutManager(requireContext(), 3));
        loadDeFaultAlbums();

        albumDeFautAdapter = new AlbumAdapter(this, this, albumsDeFaut);
        recyclerView1.setAdapter(albumDeFautAdapter);
        //////////////////
        recyclerView2 = binding.folderPicturesRecView;
        recyclerView2.setHasFixedSize(true);
        recyclerView2.setLayoutManager(new GridLayoutManager(requireContext(), 4));

        loadAlbums();
        albumAdapter = new AlbumAdapter(this, this, albums);
        albumAdapter.notifyDataSetChanged();
        recyclerView2.setAdapter(albumAdapter);
        ///////////////////////
        return binding.getRoot();
    }

    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.top_menu_album, menu);
        clearChooseItem = menu.findItem(R.id.clear_choose);
        actionAdd = menu.findItem(R.id.action_add);
        actionEdit = menu.findItem(R.id.action_edit);
        clearChooseItem.setVisible(false);
        super.onCreateOptionsMenu(menu, inflater);
    }

    public void onAlbumClick(String albumName) {
        ImageInAlbum imageInAlbum = new ImageInAlbum();
        Bundle bundle = new Bundle();
        bundle.putString("nameAlbum", albumName);
        imageInAlbum.setArguments(bundle);
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.setReorderingAllowed(true);
        fragmentTransaction.replace(R.id.frame_layout_album, imageInAlbum);
        fragmentTransaction.addToBackStack("album");
        fragmentTransaction.commit();
    }

    public void onAlbumDelete(String albumName) {
        Activity activity = requireActivity();
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(R.string.confirm);
        builder.setMessage("Xóa album: " + albumName);
        builder.setPositiveButton(R.string.OK, (DialogInterface dialog, int which) -> {
            albumCRUD.getINSTANCE(getContext()).deleteAlbumByName(albumName);
            Snackbar.make(binding.getRoot(), "Xóa album thành công", Snackbar.LENGTH_SHORT).show();
            albumAdapter.deleteAlbum(albumName);
        });
        builder.setNegativeButton(R.string.CANCLE, (DialogInterface dialog, int which) -> {
            dialog.dismiss();
        });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void loadDeFaultAlbums() {
        albumsDeFaut = albumCRUD.getINSTANCE(getContext()).getAlbumDefault();
    }

    private void loadAlbums() {
        albums = albumCRUD.getINSTANCE(getContext()).getAlbumList();
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        Activity activity = requireActivity();
        if (item.getItemId() == R.id.action_add) {
            showDialogToAddAlbum();
            return true;
        }
        if (item.getItemId() == R.id.action_edit) {
            isDeleteMode = true;
            setDeleteMode(isDeleteMode);
            clearChooseItem.setVisible(true);
            actionAdd.setVisible(false);
            actionEdit.setVisible(false);
            return true;
        }
        if (item.getItemId() == R.id.clear_choose) {
            isDeleteMode = false;
            setDeleteMode(isDeleteMode);
            clearChooseItem.setVisible(false);
            actionAdd.setVisible(true);
            actionEdit.setVisible(true);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showDialogToAddAlbum() {
        Activity activity = requireActivity();
        final Dialog dialog = new Dialog(requireContext());
        dialog.setContentView(R.layout.add_new_album);
        Button confirmButton = dialog.findViewById(R.id.buttonConfirm);
        Button cancelButton = dialog.findViewById(R.id.buttonCancel);
        final EditText editTextAlbumName = dialog.findViewById(R.id.editTextAlbumName);
        confirmButton.setOnClickListener(v -> {
            String albumName = editTextAlbumName.getText().toString();
            albums = albumCRUD.getINSTANCE(getContext()).getAlbumList();
            if (!albumName.isEmpty() && !isAlbumExisting(albumName)) {
                addNewAlbum(albumName);
                openImageSelectionDialog(albumName);
                albumAdapter.notifyDataSetChanged();
            } else {
                Toast.makeText(activity.getApplicationContext(), "Album đã tồn tại", Toast.LENGTH_SHORT).show();
            }
            dialog.dismiss();
        });
        cancelButton.setOnClickListener(v -> dialog.dismiss());
        dialog.show();
    }

    public void addNewAlbum(String albumName) {
        albumCRUD.getINSTANCE(getContext()).insertAlbum(albumName);
        albums = albumCRUD.getINSTANCE(getContext()).getAlbumList();
        albumAdapter.setAlbums(albums);
        albumAdapter.notifyDataSetChanged();
        Log.d("InsertAlbum", "success");
    }

    public void openImageSelectionDialog(String albumName) {
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
        displayToView();
        onItemClick = (Integer integer, View view) -> {
            Log.d("POSITION_Click", integer.toString());
            imageAdapter.setState(ImageFragmentAdapter.State.MultipleSelect);
            RecyclerData selectedData = viewList.get(integer);
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

        imageAdapter = new ImageFragmentAdapter(viewList, onItemClick, onItemLongClick);
        rcvView.setAdapter(imageAdapter);
        dialog1.show();
        btnAddImageToAlbum.setOnClickListener(v -> {
            ArrayList<Image> selectedImages = images.stream().filter(Image::isChecked).collect(Collectors.toCollection(ArrayList::new));
            db = Database.getInstance(requireContext().getApplicationContext());
            for (Image img : selectedImages) {
                db.ImageAlbumDAO().insertImageAlbum(new ImageAlbum(
                        img.getPath(), db.AlbumDataDAO().getAlbumByName(albumName).getId())
                );
            }
            dialog1.dismiss();

        });
        btnCancelAddImageToAlbum.setOnClickListener(v -> dialog1.dismiss());
    }

    public List<String> generateDateString() {
        List<String> dates = new ArrayList<>();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        Log.d("imagesSize", Integer.toString(images.size()));
        for (Image img : images) {
            String formattedDate = format.format(img.getDateTaken());
            if (!dates.contains(formattedDate))
                dates.add(formattedDate);
        }
        return dates;
    }

    private void displayToView() {
        Activity activity = requireActivity();
        SimpleDateFormat format = new SimpleDateFormat("dd/MM/yyyy");
        if (dateTakeImage == null || dateTakeImage.size() == 0) {
            return;
        }
        viewList = new ArrayList<>();
        for (String date : dateTakeImage) {
            List<Image> imagesForDate = images.stream()
                    .filter(img -> date.equals(format.format(img.getDateTaken())))
                    .collect(Collectors.toList());
            for (Image img : imagesForDate) {
                viewList.add(new RecyclerData(RecyclerData.Type.Image, img.getPath(), img, images.indexOf(img)));
            }
        }
    }

    private boolean isAlbumExisting(String albumName) {
        List<Album> albums = albumCRUD.getINSTANCE(getContext()).getAlbumList();
        List<Album> albumsDefaut = albumCRUD.getINSTANCE(getContext()).getAlbumDefault();
        for (Album album : albums) {
            if (album.getName().equals(albumName)) {
                return true;
            }
        }
        for (Album album : albumsDefaut) {
            if (album.getName().equals(albumName)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onPause() {
        super.onPause();
        setHasOptionsMenu(false);
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshDataAndView();
    }

    public void refreshDataAndView() {
        loadDeFaultAlbums();
        albumDeFautAdapter.notifyDataSetChanged();

        loadAlbums();
        albumAdapter.notifyDataSetChanged();
    }

    private void setDeleteMode(boolean isDeleteMode) {
        albumAdapter.setDeleteMode(isDeleteMode);
        albumAdapter.notifyDataSetChanged();
    }
}