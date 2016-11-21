package com.firma.dev.letschat;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.telephony.PhoneNumberUtils;
import android.telephony.TelephonyManager;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firma.dev.apimanager.*;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.i18n.phonenumbers.Phonenumber;
import com.loopj.android.http.*;

import org.apache.http.Header;

import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


public class MainActivity extends FragmentActivity {

    FrameLayout countryContainer;
    RelativeLayout dummyTitle;
    EditText phoneEdit;

    ProfileDataSource profileDAO;


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


    //Spinner Animation

    RelativeLayout spinnerLayout;
    Animation spinnerRotation;
    ImageView spinner;



    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        hideKeyboard();
        countryContainer.setVisibility(View.VISIBLE);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        spinnerRotation = AnimationUtils.loadAnimation(this,R.anim.spinner_rotation);
        spinner = (ImageView) findViewById(R.id.spinner);

        dummyTitle = (RelativeLayout) findViewById(R.id.dummyTitle);

        chatLogo = (ImageView) findViewById(R.id.chatLogo);
        letsLogo = (ImageView) findViewById(R.id.letsLogo);

        logoLayout = (RelativeLayout) findViewById(R.id.dummyLayout);

        phoneEdit  = (EditText) findViewById(R.id.phoneText);
        final EditText nameEdit = (EditText) findViewById(R.id.nameEdit);
        phoneEdit.addTextChangedListener(new PhoneNumberFormattingTextWatcher());

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            if(extras.getBoolean("intro_animation")){
                setLogoAnimations(false);

                dummyTitle.setVisibility(View.INVISIBLE);
                letsLogo.startAnimation(letsScaleAnim);

                Profile currentProfile = (Profile) extras.get("current_profile");

                if(currentProfile != null){
                    nameEdit.setText(currentProfile.getDisplay_name());
                }


            }else{
                logoLayout.setVisibility(View.GONE);
                chatLogo.setVisibility(View.GONE);
                letsLogo.setVisibility(View.GONE);
                phoneEdit.requestFocus();
                phoneEdit.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Show KEYBOARD 3");
                        InputMethodManager keyboard = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(phoneEdit, 0);
                    }
                }, 400);
                //setLogoAnimations(true);
            }
        }else{
            logoLayout.setVisibility(View.GONE);
            chatLogo.setVisibility(View.GONE);
            letsLogo.setVisibility(View.GONE);
            phoneEdit.requestFocus();
            phoneEdit.postDelayed(new Runnable() {
                @Override
                public void run() {
                    System.out.println("Show KEYBOARD 2");
                    InputMethodManager keyboard = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);
                    keyboard.showSoftInput(phoneEdit, 0);
                }
            }, 400);
            //setLogoAnimations(true);
        }



        SharedPreferences.Editor editor = getSharedPreferences("last",MODE_PRIVATE).edit();
        editor.putInt("last", 1);
        editor.apply();

        profileDAO = new ProfileDataSource(this);

        final TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);



        final TextView countryText = (TextView) findViewById(R.id.countryText);
        final TextView countryCodeView = (TextView) findViewById(R.id.countryCodeView);
        final String[] rl=this.getResources().getStringArray(R.array.CountryCodes);

        countryContainer = (FrameLayout) findViewById(R.id.country_container);

        phoneEdit.requestFocus();

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        CountryPicker picker = new CountryPicker();
        transaction.replace(R.id.country_container, picker);
        transaction.commit();

        countryText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countryContainer.setVisibility(View.VISIBLE);
                hideKeyboard();
            }
        });

        picker.setListener(new CountryPickerListener() {
            @Override
            public void onSelectCountry(String name, String code) {
                countryText.setText(name);
                String countryZipCode = "";


                for (String aRl : rl) {
                    String[] g = aRl.split(",");
                    if (g[0].trim().equals(code.trim())) {
                        countryZipCode = g[1];
                        break;
                    }
                }
                countryCodeView.setText("+" + countryZipCode);
            }
        });

        final LayoutInflater layoutInflater = LayoutInflater.from(this);

        final View tosView = layoutInflater.inflate(R.layout.tos_dialog, null);

        final AlertDialog tosDialog = new AlertDialog.Builder(this).create();

        Button privacyBtn = (Button) tosView.findViewById(R.id.privacyBtn);
        Button tosBtn = (Button) tosView.findViewById(R.id.tosBtn);
        Button agreeBtn = (Button) tosView.findViewById(R.id.agreeBtn);

        privacyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.letschatapp.com/privacy")));
            }
        });

        tosBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("http://www.letschatapp.com/terms")));
            }
        });


        final Intent validationIntent = new Intent(this,ValidationActivity.class);

        phoneEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                System.out.println(actionId);
                return false;
            }
        });

        nameEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {


                if (actionId == 6 || actionId == 5) {

                    countryContainer.setVisibility(View.INVISIBLE);
                    tosDialog.setView(tosView);
                    tosDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                    tosDialog.show();
                    hideKeyboard();

                }

                return false;
            }
        });


        agreeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                tosDialog.hide();

                final View errorView = layoutInflater.inflate(R.layout.ok_dialog, null);
                final AlertDialog errorDialog = new AlertDialog.Builder(MainActivity.this).create();


                final TextView errorTitle = (TextView) errorView.findViewById(R.id.title);
                final TextView errorDesc = (TextView) errorView.findViewById(R.id.desc);

                Button okErrorBtn = (Button) errorView.findViewById(R.id.okBtn);

                okErrorBtn.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        errorDialog.hide();
                    }
                });

                errorTitle.setText("Error");



                startSpinnerAnimation();

                final ApiManager apiManager = new ApiManager();

                final AsyncHttpClient post = new AsyncHttpClient();


                final String url = "http://52.24.89.20/index_v2.php/"+ApiManager.getApiKey()+"/request_code";


                Map<String,String> params = new TreeMap<>();



                final String display_name = nameEdit.getText().toString();
                final String phone_number = PhoneNumberUtils.stripSeparators(countryCodeView.getText().toString().substring(1) + phoneEdit.getText().toString());

                String auxCountry = manager.getSimCountryIso().toUpperCase();
                if(auxCountry.isEmpty()) auxCountry = "US";

                final String country = auxCountry;
                final String timezone = TimeZone.getDefault().getID();

                params.put("display_name",display_name);
                params.put("phone_number", phone_number);
                params.put("country", country);
                params.put("timezone", timezone);

                System.out.println("Request Code Params: " + params);

                post.post(url, apiManager.preparePostParams(params), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        System.out.println("Request Code Result: " + new String(responseBody));

                        JsonParser parser = new JsonParser();
                        JsonObject reply = (JsonObject) parser.parse(new String(responseBody));

                        if (!reply.get("error").getAsBoolean()) {
                            final JsonObject response = (JsonObject) parser.parse(reply.get("response").toString());


                            try {
                                profileDAO.open();

                                profileDAO.clearAll();

                                profileDAO.createProfile(display_name, phone_number, country, timezone, response.get("user_token").toString().replace("\"", ""), "0000-00-00 00:00:00");

                                profileDAO.close();

                                stopSpinnerAnimation();
                                startActivity(validationIntent);
                                overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                            } catch (SQLException e) {
                                stopSpinnerAnimation();
                                e.printStackTrace();
                            }

                        } else {
                            errorDesc.setText(reply.get("description").getAsString());
                            errorDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                            errorDialog.setView(errorView);

                            stopSpinnerAnimation();
                            errorDialog.show();

                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });


            }
        });

        TextView whyText = (TextView)findViewById(R.id.whyView);

        final View whyView = layoutInflater.inflate(R.layout.why_dialog, null);

        final AlertDialog whyDialog = new AlertDialog.Builder(this).create();
        Button okBtn = (Button) whyView.findViewById(R.id.okBtn);

        whyText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                whyDialog.setView(whyView);
                whyDialog.getWindow().getAttributes().windowAnimations = R.style.DialogAnimation;
                whyDialog.show();

            }
        });

        okBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                whyDialog.hide();
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
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


    public String GetCountryZipCode(String countryID){
//        String CountryID="";
        String CountryZipCode="";

//        TelephonyManager manager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
//        //getNetworkCountryIso
//        CountryID= manager.getSimCountryIso().toUpperCase();
        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for (String aRl : rl) {
            String[] g = aRl.split(",");
            if (g[1].trim().equals(countryID.trim())) {
                CountryZipCode = g[0];
                break;
            }
        }
        return CountryZipCode;
    }

    public String GetCountryIDCode(String countryZip){
//        String CountryID="";
        String CountryZipCode="";

        String[] rl=this.getResources().getStringArray(R.array.CountryCodes);
        for (String aRl : rl) {
            String[] g = aRl.split(",");
            if (g[0].trim().equals(countryZip.trim())) {
                CountryZipCode = g[1];
                break;
            }
        }
        return CountryZipCode;
    }

    private void hideKeyboard() {

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    private void setLogoAnimations(final boolean fastMode){

        letsScaleAnim = AnimationUtils.loadAnimation(this, fastMode ? R.anim.instant_scale_in_logo : R.anim.scale_in_logo);
        chatScaleAnim = AnimationUtils.loadAnimation(this, fastMode ? R.anim.instant_scale_in_logo : R.anim.scale_in_logo);

        letsToTitleAnim = AnimationUtils.loadAnimation(this, fastMode ? R.anim.instant_from_center_to_title : R.anim.from_center_to_title);
        chatToTitleAnim = AnimationUtils.loadAnimation(this, fastMode ? R.anim.instant_from_center_to_title : R.anim.from_center_to_title);

        logoResize = AnimationUtils.loadAnimation(this, fastMode ? R.anim.instant_title_scale : R.anim.title_scale);

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
                }, fastMode ? 1 : 200);
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
                },fastMode ? 1 : 50);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        logoLayout.startAnimation(logoResize);
                    }
                },fastMode ? 1 : 150);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        logoResize.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                phoneEdit.requestFocus();
                phoneEdit.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        System.out.println("Show KEYBOARD 2");
                        InputMethodManager keyboard = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(phoneEdit, 0);
                    }
                }, 400);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    void startSpinnerAnimation(){
        spinnerLayout.setVisibility(View.VISIBLE);
        spinnerRotation.setRepeatCount(Animation.INFINITE);
        spinnerRotation.setDuration(1000);
        spinner.startAnimation(spinnerRotation);

    }

    void stopSpinnerAnimation(){

        spinner.clearAnimation();
        spinnerLayout.setVisibility(View.GONE);


    }


}
