package com.example.android.newsfeedapp;

import android.graphics.Bitmap;

public class Article {

    private String mTitle;
    private String mCategory;
    private String mPublished;
    private Bitmap mThumbnail;
    private String mUrl;
    private String mAuthor;

    public Article(String title, String category, String published, Bitmap thumbnail, String url, String author){
        mTitle = title;
        mCategory = category;
        mPublished = published;
        mThumbnail = thumbnail;
        mUrl = url;
        mAuthor = author;
    }

    public String getTitle() {
        return mTitle;
    }

    public String getCategory() {
        return mCategory;
    }

    public String getPublished() {
        return mPublished;
    }

    public Bitmap getThumbnail() {
        return mThumbnail;
    }

    public String getUrl() {
        return mUrl;
    }

    public String getAuthor() {
        return mAuthor;
    }
}
