package com.firma.dev.letschat;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Created by Eskoria on 07/08/2015.
 */
public class ContactsDBHelper extends SQLiteOpenHelper {

    public static final String TABLE_CONTACTS = "contacts";
    public static final String CONTACTS_ID = "id";
    public static final String CONTACTS_PHONE_NUMBER = "phone_number";
    public static final String CONTACTS_FORMATTED_PHONE_NUMBER = "formated_phone_number";
    public static final String CONTACTS_DISPLAY_NAME = "display_name";
    public static final String CONTACTS_FAVORITE = "favorite";
    public static final String CONTACTS_AVAILABLE_UNTIL = "available_until";
    public static final String CONTACTS_HAS_LETS_CHAT = "has_lets_chat";

    public static final String TABLE_DROP = "delete from " + TABLE_CONTACTS;

    private static final String DATABASE_NAME = "contacts.db";
    private static final int DATABASE_VERSION = 1;

    private static  final String DATABASE_CREATE = "create table "  + TABLE_CONTACTS + "(" + CONTACTS_ID + " integer primary key autoincrement, " + CONTACTS_DISPLAY_NAME + " text not null, " + CONTACTS_PHONE_NUMBER + " text not null, " + CONTACTS_FORMATTED_PHONE_NUMBER + " text not null, "+ CONTACTS_FAVORITE + " integer not null, " + CONTACTS_AVAILABLE_UNTIL + " date DEFAULT '0000-00-00 00:00:00',"+ CONTACTS_HAS_LETS_CHAT + " integer DEFAULT 0);";



    public ContactsDBHelper(Context context){
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
