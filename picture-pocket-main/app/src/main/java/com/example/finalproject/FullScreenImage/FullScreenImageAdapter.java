package com.example.finalproject.FullScreenImage;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.finalproject.Image.Image;
import com.example.finalproject.R;

import java.util.List;

public class FullScreenImageAdapter extends RecyclerView.Adapter<FullScreenImageAdapter.FullScreenSingleImageHolder> {

    private List<Image> imageList;

    public FullScreenImageAdapter(List<Image> imageList) {
        this.imageList = imageList;
    }

    public List<Image> getImageList() {
        return imageList;
    }

    public void setImageList(List<Image> imageList) {
        this.imageList = imageList;
    }

    @NonNull
    @Override
    public FullScreenSingleImageHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        View view = layoutInflater.inflate(R.layout.full_screen_single_image, parent, false);
        return new FullScreenSingleImageHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull FullScreenSingleImageHolder holder, int position) {
        Log.d("position_binding", Integer.toString(position));
        ImageView imageView = holder.imageView;
        Glide.with(imageView.getContext())
                //.asDrawable()
                .load(imageList.get(position).getPath())
                .diskCacheStrategy(DiskCacheStrategy.NONE)
                .fitCenter()
                .skipMemoryCache(true)
                .placeholder(R.drawable.ic_launcher_foreground)
                .error(R.mipmap.ic_launcher_round)
                .into(imageView);
    }

    @Override
    public int getItemCount() {
        if (imageList == null) return 0;
        if (imageList.size() != 0)
            return imageList.size();
        return 0;
    }

    public static class FullScreenSingleImageHolder extends RecyclerView.ViewHolder {
        ImageView imageView;

        public FullScreenSingleImageHolder(@NonNull View itemView) {
            super(itemView);
            imageView = itemView.findViewById(R.id.singleImageViewHolder);
        }
    }
}
