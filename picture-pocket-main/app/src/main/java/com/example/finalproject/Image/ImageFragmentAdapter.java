package com.example.finalproject.Image;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.example.finalproject.R;

import java.util.List;
import java.util.function.BiConsumer;

public class ImageFragmentAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public static int ITEM_TYPE_TIME = 0;
    public static int ITEM_TYPE_IMAGE = 1;
    @NonNull
    private final BiConsumer<Integer, View> onItemClick;
    @NonNull
    private final BiConsumer<Integer, View> onItemLongClick;
    @NonNull
    private List<RecyclerData> DataList;
    private State state = State.Normal;
    public ImageFragmentAdapter(@NonNull List<RecyclerData> imageDataList, @NonNull BiConsumer<Integer, View> onItemClick, @NonNull BiConsumer<Integer, View> onItemLongClick) {
        this.DataList = imageDataList;
        this.onItemClick = onItemClick;
        this.onItemLongClick = onItemLongClick;

    }

    public void setData(List<RecyclerData> imageDataList) {
        this.DataList = imageDataList;
        notifyDataSetChanged();
    }

    @Override
    public int getItemViewType(int position) {
        if (DataList.get(position).type == RecyclerData.Type.Label) {
            return ITEM_TYPE_TIME;
        } else {
            return ITEM_TYPE_IMAGE;
        }
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        if (viewType == ITEM_TYPE_IMAGE) {
            View view = inflater.inflate(R.layout.single_view_image, parent, false);
            return new ImageViewHolder(view, onItemClick, onItemLongClick);
        } else {
            View view = inflater.inflate(R.layout.timeline_item, parent, false);
            return new TimelineViewHolder(view);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        if (DataList.get(position).type == RecyclerData.Type.Label) {
            final TimelineViewHolder timelineHolder = (TimelineViewHolder) holder;
            timelineHolder.textView.setText(DataList.get(position).labelData);
        } else {
            final ImageViewHolder imageHolder = (ImageViewHolder) holder;
            ImageView imageView = imageHolder.imageView;
            Image imageData = DataList.get(position).imageData;
            Glide.with(imageView.getContext())
                    .load(imageData.getPath())
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .skipMemoryCache(true)
                    .fitCenter()
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .error(R.mipmap.ic_launcher_round)
                    .into(imageView);

            if (state == State.Normal) {
                imageHolder.scrim.setVisibility(View.GONE);
                imageHolder.check.setVisibility(View.GONE);
                imageData.setChecked(false);
            } else {
                imageHolder.check.setVisibility(View.VISIBLE);
                imageHolder.scrim.setVisibility(View.VISIBLE);
                imageHolder.check.setChecked(imageData.isChecked());
            }
        }
    }

    @Override
    public int getItemCount() {
        if (DataList == null) return 0;
        else if (DataList.size() > 0) {
            return DataList.size();
        }
        return 0;
    }

    public State getState() {
        return state;
    }

    public void setState(State state) {
        this.state = state;
    }

    public void setCheckAllImage(boolean isCheck) {
        if (this.DataList == null || this.DataList.size() == 0)
            return;
        setState(State.MultipleSelect);
        for (RecyclerData data : DataList) {
            if (data.type == RecyclerData.Type.Image)
                data.imageData.setChecked(isCheck);
        }
        notifyDataSetChanged();
    }

    public enum State {
        Normal,
        MultipleSelect
    }

    public static class ImageViewHolder extends RecyclerView.ViewHolder {

        public ImageView imageView;
        public View scrim;
        public CheckBox check;

        ImageViewHolder(@NonNull View view, BiConsumer<Integer, View> onItemClick,
                        BiConsumer<Integer, View> onItemLongClick) {
            super(view);
            imageView = view.findViewById(R.id.image);
            scrim = itemView.findViewById(R.id.pictureItemScrim);
            check = itemView.findViewById(R.id.checkImage);
            itemView.setOnLongClickListener((View v) -> {
                onItemLongClick.accept(getAdapterPosition(), v);
                return true;
            });

            itemView.setOnClickListener((View v) -> {
                onItemClick.accept(getAdapterPosition(), v);
            });
        }

    }

    public static class TimelineViewHolder extends RecyclerView.ViewHolder {
        public TextView textView;

        TimelineViewHolder(@NonNull View view) {
            super(view);
            textView = view.findViewById(R.id.timeLineText);
        }
    }
}
