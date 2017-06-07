package com.example.android.booklisting;

/**
 * Created by hp on 05-06-2017.
 */

public class Library {

    private String mTitle;
    private String mAuthor;
    private String mDescription;
    private String mUrl;

    public Library(String title, String author, String description, String url){


        mTitle = title;
        mAuthor = author;
        mDescription = description;
        mUrl = url;
    }


    public String getTitle() {return mTitle;}

    public String getAuthor(){
        return mAuthor;
    }

    public String getDescription(){
        return mDescription;
    }

    public String getUrl(){
        return  mUrl;
    }

}



