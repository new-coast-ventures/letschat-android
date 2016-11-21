package com.firma.dev.letschat;

import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Handler;
import android.provider.ContactsContract;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.telephony.TelephonyManager;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firma.dev.CustomPhoneUtils;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.google.i18n.phonenumbers.NumberParseException;
import com.google.i18n.phonenumbers.PhoneNumberUtil;
import com.google.i18n.phonenumbers.Phonenumber;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class WelcomeActivity extends FragmentActivity {

    WelcomeCollectionPagerAdapter dAdapter;
    ViewPager wPager;

    RadioButton rb1;
    RadioButton rb2;
    RadioButton rb3;

    ImageView chatLogo;
    ImageView letsLogo;

    RelativeLayout logoLayout;

    //Animations

    //Start Scale
    Animation letsScaleAnim;
    Animation chatScaleAnim;


    Animation letsToTitleAnim;
    Animation chatToTitleAnim;

    Animation logoResize;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_welcome);

        SharedPreferences.Editor editor = getSharedPreferences("last",MODE_PRIVATE).edit();
        editor.putInt("last", 0);
        editor.commit();

        dAdapter = new WelcomeCollectionPagerAdapter(getSupportFragmentManager());
        wPager = (ViewPager) findViewById(R.id.pager);
        wPager.setAdapter(dAdapter);

        chatLogo = (ImageView) findViewById(R.id.chatLogo);
        letsLogo = (ImageView) findViewById(R.id.letsLogo);

        logoLayout = (RelativeLayout) findViewById(R.id.dummyLayout);

        setLogoAnimation();

        rb1 = (RadioButton)findViewById(R.id.rb1);
        rb2 = (RadioButton)findViewById(R.id.rb2);
        rb3 = (RadioButton)findViewById(R.id.rb3);
        rb1.setClickable(false);
        rb2.setClickable(false);
        rb3.setClickable(false);

        wPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int i, float v, int i1) {

            }

            @Override
            public void onPageSelected(int i) {
                switch (i) {
                    case 0:
                        rb1.setChecked(true);
                        rb2.setChecked(false);
                        rb3.setChecked(false);
                        break;
                    case 1:
                        rb2.setChecked(true);
                        rb1.setChecked(false);
                        rb3.setChecked(false);
                        break;
                    case 2:
                        rb3.setChecked(true);
                        rb2.setChecked(false);
                        rb1.setChecked(false);
                        break;
                    default:
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int i) {

            }
        });

        final Intent mainIntent = new Intent(this,MainActivity.class);

        Button startBtn = (Button) findViewById(R.id.startBtn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(mainIntent);
                overridePendingTransition(R.anim.fadein, R.anim.fadeout);
                finish();
            }
        });

        letsLogo.startAnimation(letsScaleAnim);

        new Thread(new Runnable() {
            @Override
            public void run() {
                System.out.println("Starting Reading Contacts");
                loadAllContacts();
                System.out.println("All contacts");

                try {
                    this.finalize();
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                }
            }
        }).start();


        //showAllContacts();

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_welcome, menu);
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

    public class WelcomeCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public WelcomeCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment = new WelcomeObjectFragment();
            Bundle args = new Bundle();
            // Our object is just an integer :-P
            args.putInt(WelcomeObjectFragment.ARG_OBJECT, i + 1);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public int getCount() {
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    public static class WelcomeObjectFragment extends Fragment {
        public static final String ARG_OBJECT = "object";

        @Override
        public View onCreateView(LayoutInflater inflater,
                                 ViewGroup container, Bundle savedInstanceState) {

            View rootView = inflater.inflate(
                    R.layout.fragment_welcome, container, false);
            Bundle args = getArguments();

            TextView titleView = (TextView) rootView.findViewById(R.id.welcomeTitle);
            TextView textView = (TextView) rootView.findViewById(R.id.welcomeText);

            ImageView imageView = (ImageView) rootView.findViewById(R.id.welcomeImage);
            imageView.setMaxHeight(250);
            imageView.setMaxWidth(250);


            switch (args.getInt(ARG_OBJECT)){
                case 1:
                    titleView.setText(getResources().getString(R.string.welcome1_title));
                    textView.setText(getResources().getString(R.string.welcome1_text));

                    imageView.setBackgroundResource(R.drawable.welcome1);
                    break;
                case 2:
                    titleView.setText(getResources().getString(R.string.welcome2_title));
                    textView.setText(getResources().getString(R.string.welcome2_text));

                    imageView.setBackgroundResource(R.drawable.welcome2);
                    break;
                case 3:
                    titleView.setText(getResources().getString(R.string.welcome3_title));
                    textView.setText(getResources().getString(R.string.welcome3_text));

                    imageView.setBackgroundResource(R.drawable.welcome3);
                    break;
                default:
                    break;
            }

            return rootView;
        }
    }

    void setLogoAnimation(){
        letsScaleAnim = AnimationUtils.loadAnimation(this,R.anim.scale_in_logo);
        chatScaleAnim = AnimationUtils.loadAnimation(this,R.anim.scale_in_logo);

        letsToTitleAnim = AnimationUtils.loadAnimation(this,R.anim.from_center_to_title);
        chatToTitleAnim = AnimationUtils.loadAnimation(this,R.anim.from_center_to_title);

        logoResize = AnimationUtils.loadAnimation(this,R.anim.title_scale);

        letsToTitleAnim.setFillAfter(true);
        chatToTitleAnim.setFillAfter(true);

        logoResize.setFillAfter(true);

        letsScaleAnim.setInterpolator(new BounceInterpolator());
        chatScaleAnim.setInterpolator(new BounceInterpolator());

        letsScaleAnim.setFillAfter(true);
        chatScaleAnim.setFillAfter(true);

        letsScaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                letsLogo.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chatLogo.startAnimation(chatScaleAnim);
                    }
                },200);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        chatScaleAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                chatLogo.setVisibility(View.VISIBLE);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

                letsLogo.startAnimation(letsToTitleAnim);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        chatLogo.startAnimation(chatToTitleAnim);
                    }
                }, 50);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        logoLayout.startAnimation(logoResize);
                    }
                }, 150);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    void loadAllContacts(){

        final TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);

        File file = new File(this.getFilesDir(),"contacts.json");
        final FileOutputStream fos;
        JsonArray numbersArray = new JsonArray();
        Gson gsonParser = new GsonBuilder()
                .setPrettyPrinting()
                .disableHtmlEscaping()
                .create();

        ContactsDataSource c = new ContactsDataSource(this);

        try {
            c.open();
            c.clearAll();

        } catch (SQLException e) {
            e.printStackTrace();
        }
        try{
            fos = new FileOutputStream(file);

            ContentResolver cr = getContentResolver();
            Cursor cur = cr.query(ContactsContract.Contacts.CONTENT_URI,
                    null, null, null, null);
            if (cur.getCount() > 0) {
                while (cur.moveToNext()) {
                    String id = cur.getString(cur.getColumnIndex(ContactsContract.Contacts._ID));
                    String name = cur.getString(cur.getColumnIndex(ContactsContract.Contacts.DISPLAY_NAME));

                    String[] spName = name.split(" ");

                    name = "";
                    for(String aux : spName){
                        name += aux.substring(0,1).toUpperCase() + aux.substring(1).toLowerCase() +" ";
                    }

                    if (Integer.parseInt(cur.getString(
                            cur.getColumnIndex(ContactsContract.Contacts.HAS_PHONE_NUMBER))) > 0) {
                        Cursor pCur = cr.query(
                                ContactsContract.CommonDataKinds.Phone.CONTENT_URI,
                                null,
                                ContactsContract.CommonDataKinds.Phone.CONTACT_ID +" = ?",
                                new String[]{id}, null);
                        while (pCur.moveToNext()) {
                            String phoneNo = pCur.getString(pCur.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));
                            String formatted = phoneNo;

                            if(formatted.startsWith("+")){
                                formatted = formatted.substring(1).replaceAll("\\W","");
                                numbersArray.add(new JsonPrimitive(formatted));
                            }else {

                                PhoneNumberUtil phoneUtil = PhoneNumberUtil.getInstance();
                                Phonenumber.PhoneNumber arNumber = null;


                                try {

                                    String auxCountryCode = manager.getSimCountryIso().toUpperCase();
                                    if(auxCountryCode.isEmpty()) auxCountryCode = "US";

                                    arNumber = phoneUtil.parse(formatted.replaceAll("\\W",""),auxCountryCode);

                                    System.out.println(arNumber);
                                    formatted = String.valueOf(arNumber.getCountryCode()) + arNumber.getNationalNumber();

                                    if(formatted.length() >= 8)
                                        numbersArray.add(new JsonPrimitive(formatted));

                                } catch (NumberParseException e) {
                                    e.printStackTrace();
                                }

                            }

                            c.createContact(name,phoneNo,formatted,false,"0000-00-00 00:00:00");

                        }
                        pCur.close();
                    }
                }

            }
            fos.write(gsonParser.toJson(numbersArray).getBytes());
            fos.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }


        c.close();

    }

}
