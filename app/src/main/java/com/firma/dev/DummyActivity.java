package com.firma.dev;

import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.firma.dev.letschat.ContactsDataSource;
import com.firma.dev.letschat.LetsActivity;
import com.firma.dev.letschat.MainActivity;
import com.firma.dev.letschat.R;
import com.firma.dev.letschat.ValidationActivity;
import com.firma.dev.letschat.WelcomeActivity;
import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class DummyActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dummy);
        SharedPreferences prefs = getSharedPreferences("last", MODE_PRIVATE);

        int last = prefs.getInt("last", 0);

        Intent welcomeIntent = new Intent(this, WelcomeActivity.class);
        Intent mainIntent = new Intent(this, MainActivity.class);
        Intent validationIntent = new Intent(this, ValidationActivity.class);
        Intent letsIntent = new Intent(this, LetsActivity.class);



        switch(last){
            case 0:
                welcomeIntent.putExtra("intro_animation",true);
                startActivityForResult(welcomeIntent, 0);
                break;

            case 1:
                mainIntent.putExtra("intro_animation",true);
                startActivityForResult(welcomeIntent,1);
                break;

            case 2:
                validationIntent.putExtra("intro_animation",true);
                startActivityForResult(validationIntent,2);
                break;

            case 3:
                letsIntent.putExtra("intro_animation",true);
                startActivityForResult(letsIntent,3);
                break;
        }


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_dummy, menu);
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
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        finish();
    }

}
