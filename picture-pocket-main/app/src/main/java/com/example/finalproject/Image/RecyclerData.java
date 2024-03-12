package com.example.finalproject.Image;

public class RecyclerData {
    public final Type type;
    public final Image imageData;
    public final int index;
    public String labelData;
    public RecyclerData(Type type, String labelData, Image imageData, int index) {
        this.type = type;
        this.labelData = labelData;
        this.imageData = imageData;
        this.index = index;
    }

    public enum Type {
        Label,
        Image
    }
}
