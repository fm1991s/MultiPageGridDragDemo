package com.huangjie.demo.data;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;

import java.io.BufferedInputStream;
import java.io.Serializable;

import okhttp3.Request;

/**
 * Created by huangjie on 2017/6/4.
 */

public class GridItem implements Comparable, Serializable {


    private static final long serialVersionUID = 1L;

    @Override
    public int compareTo(@NonNull Object o) {
        GridItem item = (GridItem) o;
        return this.mPosition - item.getPosition();
    }

    public GridItem() {

    }

    public GridItem(int id, String iconPath, String title, String url, int position, GRID_TYPE type, Bitmap bitmap) {
        this.mID = id;
        this.mIconPath = iconPath;
        this.mTitle = title;
        this.mUrl = url;
        this.mPosition = position;
        this.mType = type;
        this.mBitmap = bitmap;
    }

    private int mID;
    private String mIconPath;
    private String mTitle;
    private String mUrl;
    private int mPosition;
    private Bitmap mBitmap;
    private GRID_TYPE mType;

    public GRID_TYPE getType() {
        return mType;
    }

    public void setType(GRID_TYPE type) {
        this.mType = type;
    }

    public Bitmap getBitmap() {
        return mBitmap;
    }

    public void setBitmap(Bitmap bitmap) {
        this.mBitmap = bitmap;
    }

    public int getPosition() {
        return mPosition;
    }

    public void setPosition(int position) {
        this.mPosition = position;
    }

    public int getID() {
        return mID;
    }

    public void setID(int id) {
        this.mID = id;
    }

    public String getIconPath() {
        return mIconPath;
    }

    public void setIconPath(String iconPath) {
        this.mIconPath = iconPath;
    }

    public String getTitle() {
        return mTitle;
    }

    public void setTitle(String title) {
        this.mTitle = title;
    }

    public String getUrl() {
        return mUrl;
    }

    public void setUrl(String url) {
        this.mUrl = url;
    }

    @Override
    public String toString() {
        return "GridItem [id:" + this.mID
                + ", title:" + this.mTitle
                + ", iconPath:" + this.mIconPath
                + ", position:" + this.mPosition
                + ", url:" + this.mUrl
                + ", type:" + this.mType
                + ", bitmap:" + this.mBitmap + "]";
    }

    public enum GRID_TYPE {
        TYPE_EMPTY,
        TYPE_NORMAL
    }

}
