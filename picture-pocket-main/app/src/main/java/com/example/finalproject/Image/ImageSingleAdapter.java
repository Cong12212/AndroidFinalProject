package com.example.finalproject.Image;

import java.util.List;

public class ImageSingleAdapter {

    private final List<Image> _listImages;
    private final OnItemClickListener _listener;
    private final OnItemLongClickListener _listeners;

    public ImageSingleAdapter(List<Image> _listImages, OnItemClickListener _listener, OnItemLongClickListener _listeners) {
        this._listImages = _listImages;
        this._listener = _listener;
        this._listeners = _listeners;
    }
    public interface OnItemClickListener {
        void onItemClick(Image image);
    }
    public interface OnItemLongClickListener {
        boolean onItemLongClick(Image image);
    }

}
