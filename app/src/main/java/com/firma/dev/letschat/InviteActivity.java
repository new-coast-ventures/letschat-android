package com.firma.dev.letschat;

import android.app.AlertDialog;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
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


public class InviteActivity extends FragmentActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_invite);

        MyContactsFragment listFragment = new MyContactsFragment();
        android.support.v4.app.FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.fragmentFrame,listFragment);
        ft.setTransition(android.support.v4.app.FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        ft.addToBackStack(null);
        ft.commit();

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



}
