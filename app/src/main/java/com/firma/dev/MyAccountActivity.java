package com.firma.dev;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firma.dev.letschat.Profile;
import com.firma.dev.letschat.ProfileDataSource;
import com.firma.dev.letschat.R;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.sql.SQLException;

public class MyAccountActivity extends Activity {

    RelativeLayout logoOverlay;
    RelativeLayout ppLayout;
    RelativeLayout tosLayout;

    TextView lblName;
    TextView lblPhone;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_account);

        logoOverlay = (RelativeLayout) findViewById(R.id.logoOverlay);
        ppLayout = (RelativeLayout) findViewById(R.id.ppLayout);
        tosLayout = (RelativeLayout) findViewById(R.id.tosLayout);

        ppLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.letschatapp.com/privacy")));
            }
        });

        tosLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.letschatapp.com/terms")));
            }
        });

        lblName = (TextView) findViewById(R.id.lblName);
        lblPhone = (TextView) findViewById(R.id.lblPhone);

        logoOverlay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        ProfileDataSource profileDao = new ProfileDataSource(this);
        Profile currentProfile;

        PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();

        try {
            profileDao.open();
            currentProfile = profileDao.getAllProfiles().get(0);
            profileDao.close();

            Phonenumber.PhoneNumber phone = phoneUtil.parse(currentProfile.getPhone_number(),currentProfile.getCountry());

            lblPhone.setText(phoneUtil.format(phone, PhoneNumberUtil.PhoneNumberFormat.NATIONAL));
            lblName.setText(currentProfile.getDisplay_name());

        } catch (SQLException e) {
            e.printStackTrace();
        } catch (NumberParseException e) {
            e.printStackTrace();
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_account, menu);
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
}
