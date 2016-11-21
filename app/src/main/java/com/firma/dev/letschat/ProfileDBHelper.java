package com.firma.dev.letschat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Eskoria on 06/08/2015.
 */
public class ProfileDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_PROFILE = "profile";
    public static final String PROFILE_ID = "id";
    public static final String PROFILE_DISPLAY_NAME = "display_name";
    public static final String PROFILE_PHONE_NUMBER = "phone_number";
    public static final String PROFILE_TIMEZONE = "timezone";
    public static final String PROFILE_COUNTRY = "country";
    public static final String PROFILE_USER_TOKEN = "user_token";
    public static final String PROFILE_AVAILABLE_UNTIL = "available_until";

    public static final String TABLE_DROP = "delete from " + TABLE_PROFILE;

    private static final String DATABASE_NAME = "profile.db";
    private static final int DATABASE_VERSION = 1;

    private static  final String DATABASE_CREATE = "create table "
            + TABLE_PROFILE + "(" + PROFILE_ID +" integer primary key autoincrement, " + PROFILE_DISPLAY_NAME + " text not null, " + PROFILE_PHONE_NUMBER + " text not null, " + PROFILE_COUNTRY +" text not null, " + PROFILE_TIMEZONE + " text not null, " + PROFILE_USER_TOKEN + " text not null, " + PROFILE_AVAILABLE_UNTIL + " date DEFAULT '0000-00-00 00:00:00');";

    public ProfileDBHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(DATABASE_CREATE);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
