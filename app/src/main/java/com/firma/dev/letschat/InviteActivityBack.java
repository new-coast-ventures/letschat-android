package com.firma.dev.letschat;

import android.app.AlertDialog;
import android.app.ListActivity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;


public class InviteActivityBack extends ListActivity {


    private AlphabetListAdapter adapter = new AlphabetListAdapter();
    private GestureDetector mGestureDetector;
    private List<Object[]> alphabet = new ArrayList<Object[]>();
    private HashMap<String, Integer> sections = new HashMap<String, Integer>();
    private int sideIndexHeight;
    private static float sideIndexX;
    private static float sideIndexY;
    private int indexListSize;

    class SideIndexGestureListener extends GestureDetector.SimpleOnGestureListener {
        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            sideIndexX = sideIndexX - distanceX;
            sideIndexY = sideIndexY - distanceY;

            if (sideIndexX >= 0 && sideIndexY >= 0) {
                displayListItem();
            }

            return super.onScroll(e1, e2, distanceX, distanceY);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        SharedPreferences.Editor editor = getSharedPreferences("last",MODE_PRIVATE).edit();
        editor.putInt("last", 3);
        editor.commit();

        LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View inviteView = layoutInflater.inflate(R.layout.ok_dialog, null);
        final AlertDialog inviteDialog = new AlertDialog.Builder(this).create();

        final View errorView = layoutInflater.inflate(R.layout.ok_dialog, null);
        final AlertDialog errorDialog = new AlertDialog.Builder(this).create();


        final TextView title = (TextView) inviteView.findViewById(R.id.title);
        final TextView desc = (TextView) inviteView.findViewById(R.id.desc);

        final TextView errorTitle = (TextView) errorView.findViewById(R.id.title);
        final TextView errorDesc = (TextView) errorView.findViewById(R.id.desc);

        Button okBtn = (Button) inviteView.findViewById(R.id.okBtn);

        Button okErrorBtn = (Button) errorView.findViewById(R.id.okBtn);

        title.setText("Let's Chat");
        desc.setText("Let's chat is way better with friends. Invite a few!");

        errorTitle.setText("Error");
        errorDesc.setText("Your device doesn't support SMS!");

        inviteDialog.setView(inviteView);

        inviteDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
        errorDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;

        inviteDialog.show();

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                inviteDialog.hide();

                if (!getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY)) {
                    errorDialog.show();
                }
            }
        });

        okErrorBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                errorDialog.hide();
            }
        });

        mGestureDetector = new GestureDetector(this, new SideIndexGestureListener());

        List<Contact> contacts = populateContacts();
        Collections.sort(contacts);

        List<AlphabetListAdapter.Row> rows = new ArrayList<AlphabetListAdapter.Row>();
        int start = 0;
        int end = 0;
        String previousLetter = null;
        Object[] tmpIndexItem = null;
        Pattern numberPattern = Pattern.compile("[0-9]");

        for (Contact contact : contacts) {
            String firstLetter = contact.getDisplay_name().substring(0, 1);

            // Group numbers together in the scroller
            if (numberPattern.matcher(firstLetter).matches()) {
                firstLetter = "#";
            }

            // If we've changed to a new letter, add the previous letter to the alphabet scroller
            if (previousLetter != null && !firstLetter.equals(previousLetter)) {
                end = rows.size() - 1;
                tmpIndexItem = new Object[3];
                tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
                tmpIndexItem[1] = start;
                tmpIndexItem[2] = end;
                alphabet.add(tmpIndexItem);

                start = end + 1;
            }

            // Check if we need to add a header row
            if (!firstLetter.equals(previousLetter)) {
                rows.add(new AlphabetListAdapter.Section(firstLetter));
                sections.put(firstLetter, start);
            }

            // Add the country to the list

            if(contact.getHasLetsChat() == 1)
                System.out.println(contact.getDisplay_name() + " Has Les;t Chat");
            rows.add(new AlphabetListAdapter.Item(contact.getDisplay_name(),contact.getPhone_number(),contact.getHasLetsChat(),contact.getFavorite()));
            previousLetter = firstLetter;
        }

        if (previousLetter != null) {
            // Save the last letter
            tmpIndexItem = new Object[3];
            tmpIndexItem[0] = previousLetter.toUpperCase(Locale.UK);
            tmpIndexItem[1] = start;
            tmpIndexItem[2] = rows.size() - 1;
            alphabet.add(tmpIndexItem);
        }

        adapter.setRows(rows);
        setListAdapter(adapter);

        updateList();

        final Intent letsIntent = new Intent(this,LetsActivity.class);

        Button doneInviteBtn = (Button) findViewById(R.id.doneInviteBtn);

        doneInviteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(letsIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
            }
        });

    }

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_invite, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (mGestureDetector.onTouchEvent(event)) {
            return true;
        } else {
            return false;
        }
    }

    public void updateList() {
        LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
        sideIndex.removeAllViews();
        indexListSize = alphabet.size();
        if (indexListSize < 1) {
            return;
        }

        int indexMaxSize = (int) Math.floor(sideIndex.getHeight() / 20);
        int tmpIndexListSize = indexListSize;
        while (tmpIndexListSize > indexMaxSize) {
            tmpIndexListSize = tmpIndexListSize / 2;
        }
        double delta;
        if (tmpIndexListSize > 0) {
            delta = indexListSize / tmpIndexListSize;
        } else {
            delta = 1;
        }

        TextView tmpTV;
        for (double i = 1; i <= indexListSize; i = i + delta) {
            Object[] tmpIndexItem = alphabet.get((int) i - 1);
            String tmpLetter = tmpIndexItem[0].toString();

            tmpTV = new TextView(this);
            tmpTV.setText(tmpLetter);
            tmpTV.setGravity(Gravity.CENTER);
            tmpTV.setTextSize(15);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, 1);
            tmpTV.setLayoutParams(params);
            sideIndex.addView(tmpTV);
        }

        sideIndexHeight = sideIndex.getHeight();

        sideIndex.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                // now you know coordinates of touch
                sideIndexX = event.getX();
                sideIndexY = event.getY();

                // and can display a proper item it country list
                displayListItem();

                return false;
            }
        });
    }

    public void displayListItem() {
        LinearLayout sideIndex = (LinearLayout) findViewById(R.id.sideIndex);
        sideIndexHeight = sideIndex.getHeight();
        // compute number of pixels for every side index item
        double pixelPerIndexItem = (double) sideIndexHeight / indexListSize;

        // compute the item index for given event position belongs to
        int itemPosition = (int) (sideIndexY / pixelPerIndexItem);

        // get the item (we can do it since we know item index)
        if (itemPosition < alphabet.size()) {
            Object[] indexItem = alphabet.get(itemPosition);
            int subitemPosition = sections.get(indexItem[0]);

            //ListView listView = (ListView) findViewById(android.R.id.list);
            getListView().setSelection(subitemPosition);
        }
    }

    private List<Contact> populateContacts() {

        System.out.println("Population Contacts");

        ContactsDataSource cDao = new ContactsDataSource(this);

        try {
            cDao.open();

            List<Contact> allContacts = cDao.getAllContacts();

            cDao.close();

            return allContacts;

        } catch (SQLException e) {
            e.printStackTrace();
        }


        return null;
    }
}
