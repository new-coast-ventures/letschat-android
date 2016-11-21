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
public class ContactsDataSource {

    private SQLiteDatabase database;
    private ContactsDBHelper contactsDbHelper;
    private String[] allColumns = {ContactsDBHelper.CONTACTS_ID , ContactsDBHelper.CONTACTS_DISPLAY_NAME, ContactsDBHelper.CONTACTS_PHONE_NUMBER, ContactsDBHelper.CONTACTS_FORMATTED_PHONE_NUMBER, ContactsDBHelper.CONTACTS_FAVORITE , ContactsDBHelper.CONTACTS_AVAILABLE_UNTIL, ContactsDBHelper.CONTACTS_HAS_LETS_CHAT};

    public ContactsDataSource(Context context){
        contactsDbHelper = new ContactsDBHelper(context);
    }

    public  void open() throws SQLException {
        database = contactsDbHelper.getWritableDatabase();
    }

    public void close(){
        contactsDbHelper.close();
    }

    public Contact createContact(String display_name, String phone_number, String formatted_phone_number, Boolean favorite, String available_until){

        ContentValues values = new ContentValues();
        values.put(ContactsDBHelper.CONTACTS_DISPLAY_NAME,display_name);
        values.put(ContactsDBHelper.CONTACTS_PHONE_NUMBER,phone_number);
        values.put(ContactsDBHelper.CONTACTS_FORMATTED_PHONE_NUMBER,formatted_phone_number);
        values.put(ContactsDBHelper.CONTACTS_FAVORITE,favorite ? 1 : 0);
        values.put(ContactsDBHelper.CONTACTS_AVAILABLE_UNTIL,available_until);


        long insertId = database.insert(ContactsDBHelper.TABLE_CONTACTS,null,values);

        Cursor cursor = database.query(ContactsDBHelper.TABLE_CONTACTS,allColumns, ContactsDBHelper.CONTACTS_ID + " = " + insertId,null,null,null,null,"1");
        cursor.moveToFirst();
        Contact newProfile = cursorToContact(cursor);
        cursor.close();
        return  newProfile;

    }

    public void deleteContact(Contact contact){
        long id = contact.getId();
        database.delete(ContactsDBHelper.TABLE_CONTACTS, ContactsDBHelper.CONTACTS_ID + " = " + id, null);
    }

    public void setFavorite(Contact contact){
        ContentValues args = new ContentValues();

        args.put(ContactsDBHelper.CONTACTS_FAVORITE,contact.getFavorite());

        database.update(ContactsDBHelper.TABLE_CONTACTS, args, ContactsDBHelper.CONTACTS_ID + "=" + contact.getId(), null);
    }

    public void setHasLetsChat(Contact contact){
        ContentValues args = new ContentValues();

        args.put(ContactsDBHelper.CONTACTS_HAS_LETS_CHAT, contact.getHasLetsChat());

        database.update(ContactsDBHelper.TABLE_CONTACTS, args, ContactsDBHelper.CONTACTS_ID + "=" + contact.getId(), null);
    }

    public List<Contact> getAllFavorites(){
        List<Contact> contacts = new ArrayList<Contact>();

        Cursor cursor = database.query(ContactsDBHelper.TABLE_CONTACTS,
                allColumns, ContactsDBHelper.CONTACTS_FAVORITE + "=1", null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Contact contact = cursorToContact(cursor);
            contacts.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return contacts;

    }

    public List<Contact> getAllContacts(){

        List<Contact> contacts = new ArrayList<Contact>();

        Cursor cursor = database.query(ContactsDBHelper.TABLE_CONTACTS,
                allColumns, null, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            Contact contact = cursorToContact(cursor);
            contacts.add(contact);
            cursor.moveToNext();
        }
        // make sure to close the cursor
        cursor.close();
        return contacts;

    }

    public List<String> getAllNumbers(){
        List<String> contactsNumbers = new ArrayList<String>();

        Cursor cursor = database.query(ContactsDBHelper.TABLE_CONTACTS,new String[] { ContactsDBHelper.CONTACTS_PHONE_NUMBER},null,null,null,null,null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            contactsNumbers.add(cursor.getString(cursor.getColumnIndex("phone_number")));
            cursor.moveToNext();
        }

        cursor.close();
        return  contactsNumbers;
    }

    public List<String> getAllFormattedNumbers(){
        List<String> contactsNumbers = new ArrayList<String>();

        Cursor cursor = database.query(ContactsDBHelper.TABLE_CONTACTS,new String[] { ContactsDBHelper.CONTACTS_FORMATTED_PHONE_NUMBER},null,null,null,null,null);

        cursor.moveToFirst();

        while (!cursor.isAfterLast()) {
            contactsNumbers.add(cursor.getString(cursor.getColumnIndex("formatted_phone_number")));
            cursor.moveToNext();
        }

        cursor.close();
        return  contactsNumbers;
    }

    public Contact getContactFromFormattedNumber(String formattedNumber){
        Contact contact = new Contact();

        Cursor cursor = database.query(ContactsDBHelper.TABLE_CONTACTS,
                allColumns, ContactsDBHelper.CONTACTS_FORMATTED_PHONE_NUMBER + "=" + formattedNumber, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            contact = cursorToContact(cursor);
            cursor.moveToNext();
        }
        cursor.close();


        return contact;
    }

    public Contact getContactFromNumber(String number){
        Contact contact = new Contact();

        Cursor cursor = database.query(ContactsDBHelper.TABLE_CONTACTS,
                allColumns, ContactsDBHelper.CONTACTS_PHONE_NUMBER + "=" + number, null, null, null, null);

        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            contact = cursorToContact(cursor);
            cursor.moveToNext();
        }
        cursor.close();


        return contact;
    }

    public void clearAll(){
        database.execSQL(ContactsDBHelper.TABLE_DROP);

    }


    private Contact cursorToContact(Cursor cursor){
        Contact contact = new Contact();

        contact.setId(cursor.getLong(0));
        contact.setDisplay_name(cursor.getString(cursor.getColumnIndex(ContactsDBHelper.CONTACTS_DISPLAY_NAME)));
        contact.setPhone_number(cursor.getString(cursor.getColumnIndex(ContactsDBHelper.CONTACTS_PHONE_NUMBER)));
        contact.setFormatted_phone_number(cursor.getString(cursor.getColumnIndex(ContactsDBHelper.CONTACTS_FORMATTED_PHONE_NUMBER)));
        contact.setFavorite(cursor.getInt(cursor.getColumnIndex(ContactsDBHelper.CONTACTS_FAVORITE)));
        contact.setAvailable_until(cursor.getString(cursor.getColumnIndex(ContactsDBHelper.CONTACTS_AVAILABLE_UNTIL)));
        contact.setHasLetsChat(cursor.getInt(cursor.getColumnIndex(ContactsDBHelper.CONTACTS_HAS_LETS_CHAT)));

        return contact;
    }


}
