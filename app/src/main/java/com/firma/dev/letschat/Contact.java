package com.firma.dev.letschat;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Eskoria on 07/08/2015.
 */
public class Contact implements Comparable {

    private static final int SECTION_HEADER = 0;
    private static final int AVAILABLE_CONTACT = 1;
    private static final int UNAVAILABLE_CONTACT = 2;

    private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    private long id;
    String phone_number;
    String formatted_phone_number;
    String display_name;
    Integer favorite;
    Integer hasLetsChat = 0;
    boolean isSectionHeader;
    String available_until = "";


    public Integer getHasLetsChat() {
        return hasLetsChat;
    }

    public void setHasLetsChat(Integer hasLetsChat) {
        this.hasLetsChat = hasLetsChat;
    }

    public boolean isSectionHeader() {
        return isSectionHeader;
    }

    public void setIsSectionHeader(boolean isSectionHeader) {
        this.isSectionHeader = isSectionHeader;
    }

    public Contact() {
        id = -1;
    }

    public Contact(String display_name) {
        this.display_name = display_name;
        this.isSectionHeader = true;
    }


    public String getAvailable_until() {
        return available_until;
    }

    public void setAvailable_until(String available_until) {
        this.available_until = available_until;
    }


    @Override
    public String toString() {
        return "Contact{" +
                "id=" + id +
                ", phone_number='" + phone_number + '\'' +
                ", formatted_phone_number='" + formatted_phone_number + '\'' +
                ", display_name='" + display_name + '\'' +
                ", favorite=" + favorite +
                ", available_until='" + available_until + '\'' +
                '}';
    }

    public String getFormatted_phone_number() {
        return formatted_phone_number;
    }

    public void setFormatted_phone_number(String formatted_phone_number) {
        this.formatted_phone_number = formatted_phone_number;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public Integer getFavorite() {
        return favorite;
    }

    public void setFavorite(Integer favorite) {
        this.favorite = favorite;
    }

    public int getType(){

        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date available_until = sdf.parse(getAvailable_until());
            Date current_time = new Date();

            if(isSectionHeader)
                return SECTION_HEADER;
            else if((available_until.getTime() - current_time.getTime()) > 0)
                return AVAILABLE_CONTACT;
            else
                return UNAVAILABLE_CONTACT;

        } catch (ParseException e) {
            //e.printStackTrace();
        }

    return 0;
    }

    public long getAvailableTime(){

        long availableTime = -1;

        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date available_until = sdf.parse(getAvailable_until());
            Date current_time = new Date();

            availableTime = available_until.getTime() - current_time.getTime();
            return availableTime/60000;

        } catch (ParseException e) {
            //e.printStackTrace();
        }


        return availableTime;
    }

    public boolean isCurrentlyAvailable(){


        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date available_until = sdf.parse(getAvailable_until());
            Date current_time = new Date();

            return (current_time.before(available_until));


        } catch (ParseException e) {
            //e.printStackTrace();
        }

        return false;
    }

    @Override
    public int compareTo(Object another) {
        return this.getDisplay_name().compareTo(((Contact)another).getDisplay_name());
    }
}
