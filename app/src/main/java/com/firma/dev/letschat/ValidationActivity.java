package com.firma.dev.letschat;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.Bundle;
import android.os.Looper;
import android.telephony.PhoneNumberUtils;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.firma.dev.apimanager.ApiManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import java.io.File;
import java.io.FileNotFoundException;
import java.sql.SQLException;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;


public class ValidationActivity extends Activity {

    EditText codeEdit;
    RelativeLayout dummyTitle;

    Profile currentProfile;

    ImageView chatLogo;
    ImageView letsLogo;

    RelativeLayout logoLayout;

    Button resendBtn;
    Button modifyBtn;

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
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_validation);

        ProfileDataSource pDao = new ProfileDataSource(this);

        try {
            pDao.open();
            currentProfile = pDao.getAllProfiles().get(0);
            pDao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        spinnerLayout = (RelativeLayout) findViewById(R.id.spinnerLayout);
        spinnerRotation = AnimationUtils.loadAnimation(this,R.anim.spinner_rotation);
        spinner = (ImageView) findViewById(R.id.spinner);

        dummyTitle = (RelativeLayout) findViewById(R.id.dummyTitle);

        chatLogo = (ImageView) findViewById(R.id.chatLogo);
        letsLogo = (ImageView) findViewById(R.id.letsLogo);

        logoLayout = (RelativeLayout) findViewById(R.id.dummyLayout);

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            if(extras.getBoolean("intro_animation")){
                setLogoAnimations(false);

                dummyTitle.setVisibility(View.INVISIBLE);
                letsLogo.startAnimation(letsScaleAnim);
            }else{
                logoLayout.setVisibility(View.GONE);
                chatLogo.setVisibility(View.GONE);
                letsLogo.setVisibility(View.GONE);
                setLogoAnimations(true);
            }
        }else {
            logoLayout.setVisibility(View.GONE);
            chatLogo.setVisibility(View.GONE);
            letsLogo.setVisibility(View.GONE);
            setLogoAnimations(true);
        }


        resendBtn = (Button) findViewById(R.id.resendBtn);
        modifyBtn = (Button) findViewById(R.id.modifyBtn);


        SharedPreferences.Editor editor = getSharedPreferences("last",MODE_PRIVATE).edit();
        editor.putInt("last", 2);
        editor.apply();

        final Intent resendIntent = new Intent(this,MainActivity.class);

        modifyBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                resendIntent.putExtra("current_profile",currentProfile);
                startActivityForResult(resendIntent,101);
                finish();


            }
        });

        resendBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpinnerAnimation();
                hideKeyboard();

                final ApiManager apiManager = new ApiManager();

                final AsyncHttpClient post = new AsyncHttpClient();


                final String url = "http://52.24.89.20/index_v2.php/"+ApiManager.getApiKey()+"/request_code";

                Map<String,String> params = new TreeMap<>();


                params.put("display_name",currentProfile.getDisplay_name());
                params.put("phone_number", currentProfile.getPhone_number());
                params.put("country", currentProfile.getCountry());
                params.put("timezone", currentProfile.getTimezone());

                post.post(url, apiManager.preparePostParams(params), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                        System.out.println("Request Code Result: " + new String(responseBody));

                        JsonParser parser = new JsonParser();
                        JsonObject reply = (JsonObject) parser.parse(new String(responseBody));

                        if (!reply.get("error").getAsBoolean()) {
                            final JsonObject response = (JsonObject) parser.parse(reply.get("response").toString());
                                stopSpinnerAnimation();


                        } else {
                            stopSpinnerAnimation();
                        }

                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                        stopSpinnerAnimation();
                    }
                });

            }
        });

        final Button doneBtn = (Button) findViewById(R.id.doneBtn);
        codeEdit = (EditText) findViewById(R.id.codeEdit);

        codeEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {

                if (v.getText().length() == 4)
                    doneBtn.setVisibility(View.VISIBLE);
                else
                    doneBtn.setVisibility(View.GONE);

                return false;
            }
        });

        final Intent inviteIntent = new Intent(this,InviteActivity.class);

        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startSpinnerAnimation();
                final ApiManager apiManager = new ApiManager();

                Map<String,String> params = new TreeMap<>();

                final AsyncHttpClient post = new AsyncHttpClient();

                ProfileDataSource profileDao = new ProfileDataSource(getBaseContext());
                final Profile currentProfile;

                System.out.println("Done Pressed");

                try {
                    profileDao.open();

                } catch (SQLException e) {
                    e.printStackTrace();
                }

                currentProfile = profileDao.getAllProfiles().get(0);

                String url = "http://52.24.89.20/index_v2.php/"+ApiManager.getApiKey()+"/validate_code";

                String user_token = currentProfile.getUser_token();

                if(user_token == null) {
                    System.out.println("Corte 1");
                    stopSpinnerAnimation();
                    return;
                }else{

                    params.put("validation_code", codeEdit.getText().toString());
                    params.put("user_token", user_token);

                    post.post(url, apiManager.preparePostParams(params), new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                            JsonParser parser = new JsonParser();
                            JsonObject reply = (JsonObject) parser.parse(new String(responseBody));

                            if (!reply.get("error").getAsBoolean()) {

                                String urlNotify = "http://52.24.89.20/index_v2.php/" + ApiManager.getApiKey() + "/notify_contacts_that_im_here";
                                Map<String, String> notifyParams = new TreeMap<>();

                                notifyParams.put("user_token", currentProfile.getUser_token());
                                notifyParams.put("name", currentProfile.getDisplay_name());

                                post.post(urlNotify, apiManager.preparePostParams(notifyParams), new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

                                        System.out.println("Notify Contacts Result: " + new String(responseBody));


                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }
                                });

                                String apiScret = "083041de5bd5f17bcedc04fee69b8af5";

                                String token = "";

                                token += apiScret;

                                String fileLocation = getFilesDir().getAbsolutePath() + "/contacts.json";

                                String urlContacts = "http://52.24.89.20/index_v2.php/" + ApiManager.getApiKey() + "/sync_contacts";

                                File tempFile = new File(fileLocation);

                                RequestParams tmpParams = new RequestParams();
                                try {
                                    tmpParams.put("user_contacts", tempFile);
                                } catch (FileNotFoundException e) {
                                    e.printStackTrace();
                                }

                                token += currentProfile.getUser_token();
                                tmpParams.put("user_token", currentProfile.getUser_token());
                                tmpParams.put("token", ApiManager.sha256(token));

                                post.post(urlContacts, tmpParams, new AsyncHttpResponseHandler() {
                                    @Override
                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                        System.out.println("Sync Contacts Result: " + new String(responseBody));

                                        new Thread(new Runnable() {
                                            @Override
                                            public void run() {

                                                final ContactsDataSource contactsDao = new ContactsDataSource(ValidationActivity.this);

                                                try {
                                                    contactsDao.open();
                                                } catch (SQLException e) {
                                                    e.printStackTrace();
                                                }

                                                ApiManager apiManager = new ApiManager();
                                                SyncHttpClient get = new SyncHttpClient();

                                                Map<String,String> params = new TreeMap<>();
                                                params.put("user_token", currentProfile.getUser_token());

                                                String urlGet = "http://52.24.89.20/index_v2.php/"+apiManager.getApiKey()+"/user_contacts/" + apiManager.prepareGetParams(params, true);

                                                get.get(urlGet, new AsyncHttpResponseHandler() {
                                                    @Override
                                                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                                                        System.out.println(new String(responseBody));

                                                        JsonParser parser = new JsonParser();
                                                        JsonObject responseObj = (JsonObject) parser.parse(new String(responseBody));

                                                        if (!responseObj.get("error").getAsBoolean()) {

                                                            JsonArray user_contacts = responseObj.get("response").getAsJsonObject().get("user_contacts").getAsJsonArray();

                                                            for (JsonElement aux : user_contacts) {
                                                                JsonObject auxObj = aux.getAsJsonObject();

                                                                Contact auxContact = contactsDao.getContactFromFormattedNumber(auxObj.get("phone_number").getAsString());
                                                                auxContact.setHasLetsChat(1);

                                                                contactsDao.setHasLetsChat(auxContact);

                                                            }

                                                            contactsDao.close();
                                                        }

                                                        runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                stopSpinnerAnimation();
                                                            }
                                                        });

                                                        startActivityForResult(inviteIntent, 1);
                                                        overridePendingTransition(R.anim.fadein, R.anim.fadeout);

                                                        try {
                                                            this.finalize();
                                                        } catch (Throwable throwable) {
                                                            throwable.printStackTrace();
                                                        }
                                                    }

                                                    @Override
                                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                                                        try {
                                                            this.finalize();
                                                        } catch (Throwable throwable) {
                                                            throwable.printStackTrace();
                                                        }
                                                    }
                                                });
                                            }
                                        }).start();


                                    }

                                    @Override
                                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                                    }
                                });

                            } else {
                                System.out.println("Code Error");
                                stopSpinnerAnimation();

                            }

                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {
                            stopSpinnerAnimation();
                        }
                    });

                }

            }
        });

        codeEdit.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == 6 || actionId == 5) {
                    doneBtn.callOnClick();
                }
                return false;
            }
        });

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_validation, menu);
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
                codeEdit.requestFocus();
                codeEdit.postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        InputMethodManager keyboard = (InputMethodManager)
                                getSystemService(Context.INPUT_METHOD_SERVICE);
                        keyboard.showSoftInput(codeEdit, 0);
                    }
                }, 100);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


    }

    void startSpinnerAnimation(){
        System.out.println("Starting anim");
        spinnerLayout.setVisibility(View.VISIBLE);
        spinnerRotation.setRepeatCount(Animation.INFINITE);
        spinnerRotation.setDuration(1000);
        spinner.startAnimation(spinnerRotation);

    }

    void stopSpinnerAnimation(){

        System.out.println("Stoping anim");

        spinner.clearAnimation();
        spinnerLayout.setVisibility(View.GONE);
    }

    private void hideKeyboard() {

        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputManager = (InputMethodManager) this.getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }
}
