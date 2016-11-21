package com.firma.dev.letschat;

import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * Created by Eskoria on 07/08/2015.
 */

public class Profile implements Serializable {

    private static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";

    private long id;
    String display_name;
    String phone_number;
    String country;
    String timezone;
    String user_token;

    @Override
    public String toString() {
        return "Profile{" +
                "id=" + id +
                ", display_name='" + display_name + '\'' +
                ", phone_number='" + phone_number + '\'' +
                ", country='" + country + '\'' +
                ", timezone='" + timezone + '\'' +
                ", user_token='" + user_token + '\'' +
                ", available_until='" + available_until + '\'' +
                '}';
    }

    public String getAvailable_until() {
        return available_until;
    }

    public void setAvailable_until(String available_until) {
        this.available_until = available_until;
    }

    String available_until;

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getDisplay_name() {
        return display_name;
    }

    public void setDisplay_name(String display_name) {
        this.display_name = display_name;
    }

    public String getPhone_number() {
        return phone_number;
    }

    public void setPhone_number(String phone_number) {
        this.phone_number = phone_number;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public String getTimezone() {
        return timezone;
    }

    public void setTimezone(String timezone) {
        this.timezone = timezone;
    }

    public String getUser_token() {
        return user_token;
    }

    public void setUser_token(String user_token) {
        this.user_token = user_token;
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
}
