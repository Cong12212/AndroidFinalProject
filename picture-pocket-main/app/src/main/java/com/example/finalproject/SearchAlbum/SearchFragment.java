package com.example.finalproject.SearchAlbum;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.SearchView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

import com.example.finalproject.R;
import com.example.finalproject.databinding.FragmentSearchBinding;
import com.google.android.material.appbar.MaterialToolbar;

public class SearchFragment extends Fragment implements MenuProvider {
    FragmentSearchBinding binding;
    SearchView searchView;
    ArrayAdapter<String> adapterImageName;

    public SearchFragment() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

    }


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.top_menu_image_multiple, menu);

    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.img_camera) {
            Toast.makeText(requireContext(), "teoa", Toast.LENGTH_SHORT).show();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final FragmentActivity activity = requireActivity();
        binding = FragmentSearchBinding.inflate(inflater, container, false);

        Toolbar topAppBar = ((Activity) getContext()).findViewById(R.id.topAppBar);
        topAppBar.setTitle("18 Album");
        MaterialToolbar topAppBar1 = ((Activity) getContext()).findViewById(R.id.topAppBar1);
        topAppBar1.setVisibility(View.GONE);

        //searchView = getView().findViewById(R.id.search_view);
        //setupSearchView();
        return binding.getRoot();
    }

    @Override
    public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
        menu.clear();
        menuInflater.inflate(R.menu.top_menu_image_multiple, menu);

    }

    @Override
    public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
        return false;
    }
}
