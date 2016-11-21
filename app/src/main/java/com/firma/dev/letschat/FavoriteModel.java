package com.firma.dev.letschat;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Eskoria on 31/07/2015.
 */
public class FavoriteModel {

    private static final int SECTION_HEADER = 0;
    private static final int AVAILABLE_CONTACT = 1;
    private static final int UNAVAILABLE_CONTACT = 2;

    private static final String DATEFORMAT = "0000-00-00 00:00:00";

    String name;
    int available_time;
    boolean isFavorite;
    boolean isSectionHeader;

    public FavoriteModel() {
        name = "No Name";
        available_time = 0;
        isFavorite = false;
    }

    public FavoriteModel(String name) {
        this.name = name;
        available_time = 0;
        isFavorite = false;
        isSectionHeader = true;
    }

    public FavoriteModel(String name, int available_time, boolean isFavorite) {

        this.name = name;
        this.available_time = available_time;
        this.isFavorite = isFavorite;
    }

    public boolean isSectionHeader() {
        return isSectionHeader;
    }

    public void setIsSectionHeader(boolean isSectionHeader) {
        this.isSectionHeader = isSectionHeader;
    }


    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAvailable_time() {
        return available_time;
    }

    public void setAvailable_time(int available_time) {
        this.available_time = available_time;
    }

    public boolean isFavorite() {
        return isFavorite;
    }

    public void setIsFavorite(boolean isFavorite) {
        this.isFavorite = isFavorite;
    }

    public int getType(){

        if(isSectionHeader)
            return SECTION_HEADER;
        else if(available_time > 0)
            return AVAILABLE_CONTACT;
        else
            return UNAVAILABLE_CONTACT;

    }
}
