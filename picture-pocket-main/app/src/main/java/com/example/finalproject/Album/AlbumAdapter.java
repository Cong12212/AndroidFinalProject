package com.example.finalproject.Album;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.finalproject.R;
import com.example.finalproject.Type.Folder;

import java.io.File;
import java.util.List;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {

    private final OnAlbumClickListener onAlbumClickListener;
    private final OnAlbumDeleteListener onAlbumDeleteListener;
    private List<Album> albums;
    private boolean isDeleteMode = false;

    public AlbumAdapter(OnAlbumClickListener onAlbumClickListener, OnAlbumDeleteListener onAlbumDeleteListener, List<Album> albumList) {
        this.onAlbumClickListener = onAlbumClickListener;
        this.onAlbumDeleteListener = onAlbumDeleteListener;
        albums = albumList;
    }

    public void setDeleteMode(boolean deleteMode) {
        isDeleteMode = deleteMode;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        if (isDeleteMode) {
            holder.imgDeleteAlbum.setVisibility(View.VISIBLE);
            holder.imgDeleteAlbum.setOnClickListener(v -> {
                int position1 = holder.getAdapterPosition();
                if (position1 != RecyclerView.NO_POSITION) {
                    Album album1 = albums.get(position1);
                    if (onAlbumDeleteListener != null) {
                        onAlbumDeleteListener.onAlbumDelete(album1.getName());
                    }
                }
            });
        } else {
            holder.imgDeleteAlbum.setVisibility(View.INVISIBLE);
        }
        holder.albumNameTextView.setText(album.getName());
        String path = "";
        String[] folders = {Folder.FavoriteAlbumName, Folder.PrivateAlbumName, Folder.BinAlbumName};

        if (album.getImageList() != null && !album.getImageList().isEmpty() && !checkDefaultAlbum(album.getName())) {
            int i = 0;
            File file = new File(album.getImageList().get(i).getPath());
            while (!file.exists() && i + 1 < album.getImageList().size()) {
                i++;
                file = new File(album.getImageList().get(i).getPath());
            }
            path = file.getPath();
        }
        Glide.with(holder.imgAlbumImage.getContext())
                .load(path != "" ? path : album.getImageOfAlbum())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .skipMemoryCache(true)
                .fitCenter()
                .placeholder(R.drawable.folder_icon)
                .into(holder.imgAlbumImage);

        holder.itemView.setOnClickListener(view -> {
            if (onAlbumClickListener != null) {
                onAlbumClickListener.onAlbumClick(album.getName());
            }
        });
    }

    public int getItemCount() {
        return albums.size();
    }

    public void setAlbums(List<Album> _albums) {
        this.albums = _albums;
        notifyDataSetChanged();
    }

    public void deleteAlbum(String albumName) {
        for (int i = 0; i < albums.size(); i++) {
            if (albums.get(i).getName().equals(albumName)) {
                albums.remove(i);
                notifyItemRemoved(i);
                break;
            }
        }
    }

    private boolean checkDefaultAlbum(String albumName) {
        String[] folders = {Folder.FavoriteAlbumName, Folder.PrivateAlbumName, Folder.BinAlbumName};

        for (String folder : folders) {
            if (folder.equals(albumName)) {
                return true;
            }
        }

        return false;
    }

    public interface OnAlbumClickListener {
        void onAlbumClick(String albumName);
    }

    public interface OnAlbumDeleteListener {
        void onAlbumDelete(String albumName);
    }

    public static class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final TextView albumNameTextView;
        private final ImageView imgAlbumImage;

        private final ImageView imgDeleteAlbum;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumNameTextView = itemView.findViewById(R.id.tvAlbumName);
            imgAlbumImage = itemView.findViewById(R.id.imgAlbumImage);
            imgDeleteAlbum = itemView.findViewById(R.id.imgDeleteAlbum);
        }
    }

}