package com.firma.dev.letschat;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Eskoria on 07/08/2015.
 */
public class ProfileDataSource {

    private SQLiteDatabase database;
    private ProfileDBHelper profileDbHelper;
    private String[] allColumns = {ProfileDBHelper.PROFILE_ID , ProfileDBHelper.PROFILE_DISPLAY_NAME, ProfileDBHelper.PROFILE_PHONE_NUMBER, ProfileDBHelper.PROFILE_COUNTRY, ProfileDBHelper.PROFILE_TIMEZONE, ProfileDBHelper.PROFILE_USER_TOKEN, ProfileDBHelper.PROFILE_AVAILABLE_UNTIL};

    public ProfileDataSource(Context context){
        profileDbHelper = new ProfileDBHelper(context);
    }

    public  void open() throws SQLException{
        database = profileDbHelper.getWritableDatabase();
    }

    public void close(){
        profileDbHelper.close();
    }

    public Profile createProfile(String display_name, String phone_number, String country, String timezone, String user_token, String available_until){

        ContentValues values = new ContentValues();
        values.put(ProfileDBHelper.PROFILE_DISPLAY_NAME,display_name);
        values.put(ProfileDBHelper.PROFILE_PHONE_NUMBER,phone_number);
        values.put(ProfileDBHelper.PROFILE_COUNTRY,country);
        values.put(ProfileDBHelper.PROFILE_TIMEZONE,timezone);
        values.put(ProfileDBHelper.PROFILE_USER_TOKEN,user_token);
        values.put(ProfileDBHelper.PROFILE_AVAILABLE_UNTIL,available_until);

        long insertId = database.insert(ProfileDBHelper.TABLE_PROFILE,null,values);

        Cursor cursor = database.query(ProfileDBHelper.TABLE_PROFILE,allColumns, ProfileDBHelper.PROFILE_ID + " = " + insertId,null,null,null,null,"1");
        cursor.moveToFirst();
        Profile newProfile = cursorToProfile(cursor);
        cursor.close();
        return  newProfile;

    }

    public void setAvailable(long id, String available_until){

        ContentValues args = new ContentValues();

        args.put(ProfileDBHelper.PROFILE_AVAILABLE_UNTIL,available_until);

        database.update(ProfileDBHelper.TABLE_PROFILE,args,ProfileDBHelper.PROFILE_ID + "=" + id, null);

    }

    public void deleteProfile(Profile profile){
        long id = profile.getId();
        database.delete(ProfileDBHelper.TABLE_PROFILE, ProfileDBHelper.PROFILE_ID + " = " + id, null);
    }

    public List<Profile> getAllProfiles(){

        List<Profile> comments = new ArrayList<Profile>();

        Cursor cursor = database.query(ProfileDBHelper.TABLE_PROFILE,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Profile comment = cursorToProfile(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return comments;

    }
    public void clearAll(){
        database.execSQL(ProfileDBHelper.TABLE_DROP);
    }


    private Profile cursorToProfile(Cursor cursor){
        Profile profile = new Profile();

        profile.setId(cursor.getLong(0));
        profile.setDisplay_name(cursor.getString(cursor.getColumnIndex("display_name")));
        profile.setPhone_number(cursor.getString(cursor.getColumnIndex("phone_number")));
        profile.setCountry(cursor.getString(cursor.getColumnIndex("country")));
        profile.setTimezone(cursor.getString(cursor.getColumnIndex("timezone")));
        profile.setUser_token(cursor.getString(cursor.getColumnIndex("user_token")));
        profile.setAvailable_until(cursor.getString(cursor.getColumnIndex("available_until")));

        return profile;

    }

}
