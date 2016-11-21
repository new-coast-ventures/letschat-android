package com.firma.dev.letschat;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.app.NotificationCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.SwipeRefreshLayout;
import android.telephony.PhoneNumberFormattingTextWatcher;
import android.text.style.TtsSpan;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.BounceInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.TranslateAnimation;
import android.view.inputmethod.InputMethodManager;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.firma.dev.MyAccountActivity;
import com.firma.dev.apimanager.ApiManager;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonSyntaxException;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.ResponseHandlerInterface;
import com.loopj.android.http.SyncHttpClient;
import com.parse.ParsePush;
import com.parse.SaveCallback;

import org.apache.http.Header;
import org.w3c.dom.Text;

import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;
import java.util.TimeZone;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;


public class LetsActivity extends FragmentActivity implements MyFavoriteFragment.onUserTimerForcedToExpire {

    static final String DATEFORMAT = "yyyy-MM-dd HH:mm:ss";
    static final long ONE_MINUTE_IN_MILLIS=60000;

    RelativeLayout timerLayout;
    TextView skipText;
    TextView timerDesc;
    boolean visibleTimerButtons = false;
    boolean imAvailable = false;
    boolean newlyStarted = true;
    boolean jumpToMyAccount = false;
    ToggleButton tggBtn;

    LetsCollectionPagerAdapter dAdapter;
    ViewPager wPager;

    AlphaAnimation fadeInAnim;
    AlphaAnimation fadeOutAnim;

    Timer updateTimer;
    UpdateTimerTask updateTimerTask;


    Boolean animationIsRunning = false;

    SwipeRefreshLayout mSwipeRefreshLayout;

    Profile currentProfile;

    //Buttons

    ImageButton min10Btn;
    ImageButton min20Btn;
    ImageButton min30Btn;
    ImageButton min45Btn;
    ImageButton min60Btn;
    ImageButton timerBtn;
    ImageButton preTimerBtn;
    ImageButton endBtn;
    Button availableBtn;

    ImageButton frontGearBtn;
    ImageButton backGearBtn;

    ////General Animations
    int animDuration = 150;

    RotateAnimation min10Rot;
    RotateAnimation min20Rot;
    RotateAnimation min30Rot;
    RotateAnimation min45Rot;
    RotateAnimation min60Rot;

    ////Show Buttons Animations
    AnimationSet min10AnimSetOut;
    AnimationSet min20AnimSetOut;
    AnimationSet min30AnimSetOut;
    AnimationSet min45AnimSetOut;
    AnimationSet min60AnimSetOut;


    ////Hide Buttons Animations
    AnimationSet min10AnimSetIn;
    AnimationSet min20AnimSetIn;
    AnimationSet min30AnimSetIn;
    AnimationSet min45AnimSetIn;
    AnimationSet min60AnimSetIn;

    ////Timer Set Animations

    //Scale IN
    Animation min10ScaleIn;
    Animation min20ScaleIn;
    Animation min30ScaleIn;
    Animation min45ScaleIn;
    Animation min60ScaleIn;

    //Scale OUT
    Animation min10ScaleOut;
    Animation min20ScaleOut;
    Animation min30ScaleOut;
    Animation min45ScaleOut;
    Animation min60ScaleOut;

    //Intro Animations
    RelativeLayout backLogoLayout;

    ImageView chatLogo;
    ImageView letsLogo;

    RelativeLayout logoLayout;


    //Start Scale
    Animation letsScaleAnim;
    Animation chatScaleAnim;


    Animation letsToTitleAnim;
    Animation chatToTitleAnim;

    Animation logoResize;

    @Override
    public void onBackPressed() {
//        super.onBackPressed();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        System.out.println("on Create");

        setContentView(R.layout.activity_lets);

        backLogoLayout = (RelativeLayout) findViewById(R.id.backLogoLayout);

        chatLogo = (ImageView) findViewById(R.id.chatLogo);
        letsLogo = (ImageView) findViewById(R.id.letsLogo);

        logoLayout = (RelativeLayout) findViewById(R.id.dummyLayout);

        Bundle extras = getIntent().getExtras();

        if(extras != null){
            if(extras.getBoolean("intro_animation")){
                setLogoAnimations(false);
                backLogoLayout.setVisibility(View.INVISIBLE);
                letsLogo.startAnimation(letsScaleAnim);
            }else{
                logoLayout.setVisibility(View.GONE);
                chatLogo.setVisibility(View.GONE);
                letsLogo.setVisibility(View.GONE);
             }
        }else{
            logoLayout.setVisibility(View.GONE);
            chatLogo.setVisibility(View.GONE);
            letsLogo.setVisibility(View.GONE);
        }


        final ApiManager apiManager = new ApiManager();
        ProfileDataSource profileDao = new ProfileDataSource(this);

        try {
            profileDao.open();

            currentProfile = profileDao.getAllProfiles().get(0);
            profileDao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        ParsePush.subscribeInBackground("letsc_"+currentProfile.getUser_token(), new SaveCallback() {
            @Override
            public void done(com.parse.ParseException e) {
                Log.e("A", "Successfully subscribed to Parse!");
            }
        });


        updateTimer = new Timer();
        updateTimerTask = new UpdateTimerTask();

        updateTimer.schedule(updateTimerTask,0,60000);


        frontGearBtn = (ImageButton) findViewById(R.id.frontGearBtn);
        backGearBtn = (ImageButton) findViewById(R.id.backGearBtn);

        min10Btn = (ImageButton) findViewById(R.id.min10Btn);
        min20Btn = (ImageButton) findViewById(R.id.min20Btn);
        min30Btn = (ImageButton) findViewById(R.id.min30Btn);
        min45Btn = (ImageButton) findViewById(R.id.min45Btn);
        min60Btn = (ImageButton) findViewById(R.id.min60Btn);

        endBtn = (ImageButton) findViewById(R.id.endBtn);
        availableBtn = (Button) findViewById(R.id.availableBtn);

        setRotAnimations();
        setOutButtonsAnimations();
        setInButtonsAnimations();
        setButtonsScaleOutAnim();
        setButtonsScaleInAnim();
        setCirclesOnClick();

        SharedPreferences.Editor editor = getSharedPreferences("last",MODE_PRIVATE).edit();
        editor.putInt("last", 3);
        editor.commit();

        dAdapter = new LetsCollectionPagerAdapter(getSupportFragmentManager());
        wPager = (ViewPager) findViewById(R.id.letsPager);
        wPager.setAdapter(dAdapter);

        tggBtn = (ToggleButton) findViewById(R.id.toggleButton);

        preTimerBtn = (ImageButton) findViewById(R.id.preTimerBtn);
        timerBtn = (ImageButton) findViewById(R.id.timerBtn);
        timerLayout = (RelativeLayout) findViewById(R.id.TimerLayout);
        skipText = (TextView) findViewById(R.id.skipText);
        timerDesc = (TextView) findViewById(R.id.timerDesc);

        fadeInAnim = new AlphaAnimation(0.0f,0.9f);
        fadeInAnim.setDuration(200);
        fadeInAnim.setFillAfter(true);

        fadeInAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                timerLayout.setVisibility(View.VISIBLE);
                //timerLayout.setClickable(true);
            }

            @Override
            public void onAnimationEnd(Animation animation) {

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        fadeOutAnim = new AlphaAnimation(0.9f,0.0f);
        fadeOutAnim.setDuration(200);
        fadeOutAnim.setFillAfter(true);

        fadeOutAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                timerLayout.setVisibility(View.GONE);
                timerLayout.clearAnimation();
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });


        preTimerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timerDesc.setText("Set the amount of time you have to chat");
                skipText.setVisibility(View.INVISIBLE);

                timerLayout.startAnimation(fadeInAnim);
                timerLayout.setVisibility(View.VISIBLE);

                endBtn.setVisibility(View.GONE);
                timerBtn.setVisibility(View.VISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startOutButtonsAnimations();
                    }
                }, 500);


            }
        });

        timerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!visibleTimerButtons){
                    startOutButtonsAnimations();
                }else{
                    startInButtonsAnimations();
                }
            }
        });

        timerLayout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleTimerButtons = false;
                startInButtonsAnimations();
            }
        });

        skipText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                visibleTimerButtons = false;
                startInButtonsAnimations();
            }
        });

        wPager.setOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case 0:
                        tggBtn.setChecked(false);
                        break;
                    case 1:
                        tggBtn.setChecked(true);
                        break;
                }
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tggBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (tggBtn.isChecked())
                    wPager.setCurrentItem(1, true);
                else
                    wPager.setCurrentItem(0, true);
            }
        });

        endBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(animationIsRunning)
                    return;

                startInButtonsAnimations();

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        onTimeFinished();
                    }
                },300);

            }
        });

        availableBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                timerDesc.setText("Change your time or end chat");
                skipText.setVisibility(View.INVISIBLE);

                timerLayout.startAnimation(fadeInAnim);
                timerLayout.setVisibility(View.VISIBLE);

                endBtn.setVisibility(View.VISIBLE);
                timerBtn.setVisibility(View.INVISIBLE);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        startOutButtonsAnimations();
                    }
                }, 500);

            }
        });


        if(checkIfImAvailable()){
            preTimerBtn.setVisibility(View.INVISIBLE);
            availableBtn.setVisibility(View.VISIBLE);
        }

        final Intent myAccountIntent = new Intent(this, MyAccountActivity.class);

        frontGearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newlyStarted = true;
                jumpToMyAccount = true;
                startActivityForResult(myAccountIntent, 102);

            }
        });

        backGearBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                newlyStarted = true;
                jumpToMyAccount = true;
                startActivityForResult(myAccountIntent, 102);

            }
        });


    }

    @Override
    protected void onPause() {
        super.onPause();

        if(!jumpToMyAccount)
            newlyStarted = false;
    }

    @Override
    protected void onResume() {
        super.onResume();

        jumpToMyAccount = false;

        if(newlyStarted)
            return;

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if(!visibleTimerButtons)
                    if (imAvailable)
                        availableBtn.callOnClick();
                    else
                        preTimerBtn.callOnClick();

            }
        },1000);


    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_lets, menu);
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


    private void setRotAnimations(){

        min10Rot = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        min10Rot.setDuration(animDuration);
        min10Rot.setFillAfter(true);

        min20Rot = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        min20Rot.setDuration(animDuration);
        min20Rot.setFillAfter(true);

        min30Rot = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        min30Rot.setDuration(animDuration);
        min30Rot.setFillAfter(true);

        min45Rot = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        min45Rot.setDuration(animDuration);
        min45Rot.setFillAfter(true);

        min60Rot = new RotateAnimation(0, 720, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF,0.5f);
        min60Rot.setDuration(animDuration);
        min60Rot.setFillAfter(true);


    }


    private void setOutButtonsAnimations(){

        TranslateAnimation min10Trans = new TranslateAnimation(170,0,0,0);
        TranslateAnimation min20Trans = new TranslateAnimation(120,0,120,0);
        TranslateAnimation min30Trans = new TranslateAnimation(0,0,170,0);
        TranslateAnimation min45Trans = new TranslateAnimation(-120,0,120,0);
        TranslateAnimation min60Trans = new TranslateAnimation(-170,0,0,0);

        min10Trans.setDuration(animDuration);
        min20Trans.setDuration(animDuration);
        min30Trans.setDuration(animDuration);
        min45Trans.setDuration(animDuration);
        min60Trans.setDuration(animDuration);

        min10Trans.setFillAfter(true);
        min20Trans.setFillAfter(true);
        min30Trans.setFillAfter(true);
        min45Trans.setFillAfter(true);
        min60Trans.setFillAfter(true);

        min10AnimSetOut = new AnimationSet(true);
        min20AnimSetOut = new AnimationSet(true);
        min30AnimSetOut = new AnimationSet(true);
        min45AnimSetOut = new AnimationSet(true);
        min60AnimSetOut = new AnimationSet(true);
        min10AnimSetOut.setInterpolator(new AccelerateInterpolator(1.0f));
        min20AnimSetOut.setInterpolator(new AccelerateInterpolator(1.0f));
        min30AnimSetOut.setInterpolator(new AccelerateInterpolator(1.0f));
        min45AnimSetOut.setInterpolator(new AccelerateInterpolator(1.0f));
        min60AnimSetOut.setInterpolator(new AccelerateInterpolator(1.0f));

        min10AnimSetOut.setFillAfter(true);
        min20AnimSetOut.setFillAfter(true);
        min30AnimSetOut.setFillAfter(true);
        min45AnimSetOut.setFillAfter(true);
        min60AnimSetOut.setFillAfter(true);

        min10AnimSetOut.addAnimation(min10Rot);
        min10AnimSetOut.addAnimation(min10Trans);
        min20AnimSetOut.addAnimation(min20Rot);
        min20AnimSetOut.addAnimation(min20Trans);
        min30AnimSetOut.addAnimation(min30Rot);
        min30AnimSetOut.addAnimation(min30Trans);
        min45AnimSetOut.addAnimation(min45Rot);
        min45AnimSetOut.addAnimation(min45Trans);
        min60AnimSetOut.addAnimation(min60Rot);
        min60AnimSetOut.addAnimation(min60Trans);


        min10AnimSetOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bounceAnimation(min10Btn, 0, -20, 0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        min20AnimSetOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bounceAnimation(min20Btn, 0, -20, 0, -20);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        min30AnimSetOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bounceAnimation(min30Btn, 0, 0, 0, -20);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        min45AnimSetOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bounceAnimation(min45Btn, 0, 20, 0, -20);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        min60AnimSetOut.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                bounceAnimation(min60Btn, 0, 20, 0, 0);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });



    }

    private void startOutButtonsAnimations(){

        if(animationIsRunning)
            return;
        else
            animationIsRunning = true;

        min10Btn.startAnimation(min10AnimSetOut);
        min10Btn.setVisibility(View.VISIBLE);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                min20Btn.startAnimation(min20AnimSetOut);
                min20Btn.setVisibility(View.VISIBLE);
            }
        }, 50);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                min30Btn.startAnimation(min30AnimSetOut);
                min30Btn.setVisibility(View.VISIBLE);
            }
        }, 100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                min45Btn.startAnimation(min45AnimSetOut);
                min45Btn.setVisibility(View.VISIBLE);
            }
        }, 150);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                min60Btn.startAnimation(min60AnimSetOut);
                min60Btn.setVisibility(View.VISIBLE);

            }
        }, 200);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animationIsRunning = false;
            }
        },400);

        visibleTimerButtons = true;

    }

    private void setInButtonsAnimations(){

        TranslateAnimation min10Trans = new TranslateAnimation(0,170,0,0);
        TranslateAnimation min20Trans = new TranslateAnimation(0,120,0,120);
        TranslateAnimation min30Trans = new TranslateAnimation(0,0,0,170);
        TranslateAnimation min45Trans = new TranslateAnimation(0,-120,0,120);
        TranslateAnimation min60Trans = new TranslateAnimation(0,-170,0,0);

        min10Trans.setDuration(animDuration);
        min20Trans.setDuration(animDuration);
        min30Trans.setDuration(animDuration);
        min45Trans.setDuration(animDuration);
        min60Trans.setDuration(animDuration);

        min10Trans.setFillAfter(true);
        min20Trans.setFillAfter(true);
        min30Trans.setFillAfter(true);
        min45Trans.setFillAfter(true);
        min60Trans.setFillAfter(true);

        min10AnimSetIn = new AnimationSet(true);
        min20AnimSetIn = new AnimationSet(true);
        min30AnimSetIn = new AnimationSet(true);
        min45AnimSetIn = new AnimationSet(true);
        min60AnimSetIn = new AnimationSet(true);

        min10AnimSetIn.setInterpolator(new AccelerateInterpolator(0.4f));
        min20AnimSetIn.setInterpolator(new AccelerateInterpolator(0.4f));
        min30AnimSetIn.setInterpolator(new AccelerateInterpolator(0.4f));
        min45AnimSetIn.setInterpolator(new AccelerateInterpolator(0.4f));
        min60AnimSetIn.setInterpolator(new AccelerateInterpolator(0.4f));

        min10AnimSetIn.setFillAfter(true);
        min20AnimSetIn.setFillAfter(true);
        min30AnimSetIn.setFillAfter(true);
        min45AnimSetIn.setFillAfter(true);
        min60AnimSetIn.setFillAfter(true);

        min10AnimSetIn.addAnimation(min10Rot);
        min10AnimSetIn.addAnimation(min10Trans);
        min20AnimSetIn.addAnimation(min20Rot);
        min20AnimSetIn.addAnimation(min20Trans);
        min30AnimSetIn.addAnimation(min30Rot);
        min30AnimSetIn.addAnimation(min30Trans);
        min45AnimSetIn.addAnimation(min45Rot);
        min45AnimSetIn.addAnimation(min45Trans);
        min60AnimSetIn.addAnimation(min60Rot);
        min60AnimSetIn.addAnimation(min60Trans);


    }

    private void startInButtonsAnimations(){
        if(animationIsRunning)
            return;
        else
            animationIsRunning = true;


        min60Btn.startAnimation(min60AnimSetIn);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                min45Btn.startAnimation(min45AnimSetIn);
            }
        }, 50);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                min30Btn.startAnimation(min30AnimSetIn);
            }
        }, 100);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                min20Btn.startAnimation(min20AnimSetIn);
            }
        }, 150);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                min10Btn.startAnimation(min10AnimSetIn);
            }
        }, 200);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                min10Btn.setVisibility(View.INVISIBLE);
                min20Btn.setVisibility(View.INVISIBLE);
                min30Btn.setVisibility(View.INVISIBLE);
                min45Btn.setVisibility(View.INVISIBLE);
                min60Btn.setVisibility(View.INVISIBLE);

                timerLayout.startAnimation(fadeOutAnim);
            }
        }, 500);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                animationIsRunning = false;
            }
        }, 700);

        visibleTimerButtons = false;


    }

    public void bounceAnimation(final ImageButton btn,float fromXDelta, float toXDelta, float fromYDelta, float toYDelta){

        TranslateAnimation startBounceAnim = new TranslateAnimation(fromXDelta,toXDelta,fromYDelta,toYDelta);
        startBounceAnim.setDuration(25);
        startBounceAnim.setFillAfter(true);

        final TranslateAnimation finishBounceAnim = new TranslateAnimation(toXDelta,fromXDelta,toYDelta,fromYDelta);
        finishBounceAnim.setDuration(25);
        finishBounceAnim.setFillAfter(true);

        startBounceAnim.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {

            }

            @Override
            public void onAnimationEnd(Animation animation) {
                btn.startAnimation(finishBounceAnim);
            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

        btn.startAnimation(startBounceAnim);

    }

    @Override
    public void finishUserTimer() {
        onTimeFinished();
    }

    public class LetsCollectionPagerAdapter extends FragmentStatePagerAdapter {
        public LetsCollectionPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            Fragment fragment;

            if(i == 0) fragment = new MyFavoriteFragment();
            else fragment = new MyContactsFragment();

            return fragment;
        }

        @Override
        public int getCount() {
            return 2;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return "OBJECT " + (position + 1);
        }
    }

    private void setButtonsScaleOutAnim(){

        min10ScaleOut = AnimationUtils.loadAnimation(this,R.anim.timer_scale_out);
        min20ScaleOut = AnimationUtils.loadAnimation(this,R.anim.timer_scale_out);
        min30ScaleOut = AnimationUtils.loadAnimation(this,R.anim.timer_scale_out);
        min45ScaleOut = AnimationUtils.loadAnimation(this,R.anim.timer_scale_out);
        min60ScaleOut = AnimationUtils.loadAnimation(this,R.anim.timer_scale_out);

        min10ScaleOut.setFillAfter(true);
        min20ScaleOut.setFillAfter(true);
        min30ScaleOut.setFillAfter(true);
        min45ScaleOut.setFillAfter(true);
        min60ScaleOut.setFillAfter(true);

    }

    private void setButtonsScaleInAnim(){

        min10ScaleIn = AnimationUtils.loadAnimation(this,R.anim.timer_scale_in);
        min20ScaleIn = AnimationUtils.loadAnimation(this,R.anim.timer_scale_in);
        min30ScaleIn = AnimationUtils.loadAnimation(this,R.anim.timer_scale_in);
        min45ScaleIn = AnimationUtils.loadAnimation(this,R.anim.timer_scale_in);
        min60ScaleIn = AnimationUtils.loadAnimation(this,R.anim.timer_scale_in);

        min10ScaleIn.setFillAfter(true);
        min20ScaleIn.setFillAfter(true);
        min30ScaleIn.setFillAfter(true);
        min45ScaleIn.setFillAfter(true);
        min60ScaleIn.setFillAfter(true);
    }

    public void setCirclesOnClick(){

        min10Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAvailableTime(10);
            }
        });

        min20Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAvailableTime(20);
            }
        });

        min30Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAvailableTime(30);
            }
        });

        min45Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAvailableTime(45);
            }
        });

        min60Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                setAvailableTime(60);
            }
        });

    }

    public void setAvailableTime(int time){

        final ProfileDataSource profileDao = new ProfileDataSource(this);


        if(currentProfile == null)
            return;

        try {
            profileDao.open();
            String available_until = GetUTCdatetimeAsString(time);

            availableBtn.setVisibility(View.VISIBLE);
            availableBtn.setText( time + " min");

            switch (time){
                case 10:
                    min10Btn.startAnimation(min10ScaleIn);
                    min20Btn.startAnimation(min20ScaleOut);
                    min30Btn.startAnimation(min30ScaleOut);
                    min45Btn.startAnimation(min45ScaleOut);
                    min60Btn.startAnimation(min60ScaleOut);

                    setAvailableTime(available_until,time);
                    currentProfile.setAvailable_until(available_until);
                    profileDao.setAvailable(currentProfile.getId(),available_until);

                    break;

                case 20:

                    min10Btn.startAnimation(min10ScaleOut);
                    min20Btn.startAnimation(min20ScaleIn);
                    min30Btn.startAnimation(min30ScaleOut);
                    min45Btn.startAnimation(min45ScaleOut);
                    min60Btn.startAnimation(min60ScaleOut);

                    setAvailableTime(available_until,time);
                    currentProfile.setAvailable_until(available_until);
                    profileDao.setAvailable(currentProfile.getId(), available_until);

                    break;

                case 30:

                    min10Btn.startAnimation(min10ScaleOut);
                    min20Btn.startAnimation(min20ScaleOut);
                    min30Btn.startAnimation(min30ScaleIn);
                    min45Btn.startAnimation(min45ScaleOut);
                    min60Btn.startAnimation(min60ScaleOut);

                    setAvailableTime(available_until,time);
                    currentProfile.setAvailable_until(available_until);
                    profileDao.setAvailable(currentProfile.getId(), available_until);

                    break;

                case 45:

                    min10Btn.startAnimation(min10ScaleOut);
                    min20Btn.startAnimation(min20ScaleOut);
                    min30Btn.startAnimation(min30ScaleOut);
                    min45Btn.startAnimation(min45ScaleIn);
                    min60Btn.startAnimation(min60ScaleOut);

                    setAvailableTime(available_until,time);
                    currentProfile.setAvailable_until(available_until);
                    profileDao.setAvailable(currentProfile.getId(), available_until);

                    break;

                case 60:

                    min10Btn.startAnimation(min10ScaleOut);
                    min20Btn.startAnimation(min20ScaleOut);
                    min30Btn.startAnimation(min30ScaleOut);
                    min45Btn.startAnimation(min45ScaleOut);
                    min60Btn.startAnimation(min60ScaleIn);

                    setAvailableTime(available_until,time);
                    currentProfile.setAvailable_until(available_until);
                    profileDao.setAvailable(currentProfile.getId(), available_until);

                    break;
            }

            imAvailable = true;
            profileDao.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {

                min10Btn.setVisibility(View.INVISIBLE);
                min20Btn.setVisibility(View.INVISIBLE);
                min30Btn.setVisibility(View.INVISIBLE);
                min45Btn.setVisibility(View.INVISIBLE);
                min60Btn.setVisibility(View.INVISIBLE);

                timerLayout.startAnimation(fadeOutAnim);

            }
        }, 500);

        visibleTimerButtons = false;

    }

    public static String GetUTCdatetimeAsString(int extraMins)
    {
        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        Date date = new Date();

        long t = date.getTime();

        final String utcTime = sdf.format(new Date(t + (extraMins * ONE_MINUTE_IN_MILLIS)));

        System.out.println(utcTime);

        return utcTime;
    }



    private void setAvailableTime(String availableUntil, int mins){

        final ApiManager apiManager = new ApiManager();
        final AsyncHttpClient post = new AsyncHttpClient();
        boolean moreThan60 = (mins > 60);

        System.out.println("Available: " + imAvailable);

        final String url = "http://52.24.89.20/index_v2.php/"+ApiManager.getApiKey()+"/set_available_time";

        if(currentProfile != null) {


            Map<String,String> params = new TreeMap<>();

            params.put("user_token",currentProfile.getUser_token());
            params.put("available_time",availableUntil);
            params.put("name",currentProfile.getDisplay_name());
            params.put("ended","0");
            params.put("available_seconds", "0");
            if(moreThan60){
                params.put("available_hours","1");
                params.put("available_minutes","0");
            }else{
                params.put("available_hours","0");
                params.put("available_minutes",String.valueOf(mins));
            }


            params.put("push",(imAvailable ? "0" : "1"));

            post.post(url, apiManager.preparePostParams(params), new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                    System.out.println(new String(responseBody));
                    JsonParser parser = new JsonParser();

                    try {
                        JsonObject reply = (JsonObject) parser.parse(new String(responseBody));

                        if (!reply.get("error").getAsBoolean()) {

                            preTimerBtn.setVisibility(View.INVISIBLE);
                            timerBtn.setVisibility(View.INVISIBLE);

                        }
                    }catch (JsonSyntaxException e){
                        e.printStackTrace();
                    }

                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });

        }
    }

    public void onTimeFinished(){

        final ApiManager apiManager = new ApiManager();

        final AsyncHttpClient post = new AsyncHttpClient();
        final String url = "http://52.24.89.20/index_v2.php/"+apiManager.getApiKey()+"/set_available_time";

        Map<String,String> params = new TreeMap<String, String>();
        params.put("user_token",currentProfile.getUser_token());
        params.put("available_time",GetUTCdatetimeAsString(0));
        params.put("name", currentProfile.getDisplay_name());
        params.put("ended", "1");
        params.put("push", "0");

        post.post(url, apiManager.preparePostParams(params), new AsyncHttpResponseHandler() {
            @Override
            public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

            }

            @Override
            public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

            }
        });

        final ProfileDataSource profileDao = new ProfileDataSource(this);


        try {
            profileDao.open();
            currentProfile.setAvailable_until(GetUTCdatetimeAsString(0));
            profileDao.setAvailable(currentProfile.getId(), GetUTCdatetimeAsString(0));

            profileDao.close();

        } catch (SQLException e) {
            e.printStackTrace();
        }


        endBtn.setVisibility(View.GONE);
        availableBtn.setVisibility(View.GONE);
        preTimerBtn.setVisibility(View.VISIBLE);
        timerBtn.setVisibility(View.VISIBLE);

        imAvailable = false;

    }

    public boolean checkIfImAvailable(){

        final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
        sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

        try {
            Date available_until = sdf.parse(currentProfile.getAvailable_until());
            Date current_time = new Date();

            imAvailable = !current_time.after(available_until);
            return imAvailable;
        } catch (ParseException e) {
            e.printStackTrace();
        }

        return imAvailable;

    }

    public void setTimerButtonText(long time){
        System.out.println("TIME: " + time);
        if(time >= 60) {
            availableBtn.setText("1 hr 0 min");
            availableBtn.setTextSize(11);
        }else if (time >= 1){
            availableBtn.setText(time + " min");
            availableBtn.setTextSize(12);
        }else{
            availableBtn.setText("< 1 min");
            availableBtn.setTextSize(12);

            Intent intent = new Intent(this, LetsActivity.class);
            PendingIntent contentIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT);

            NotificationCompat.Builder b = new NotificationCompat.Builder(this);

            b.setAutoCancel(true)
                    .setDefaults(Notification.DEFAULT_ALL)
                    .setWhen(System.currentTimeMillis())
                    .setSmallIcon(R.mipmap.ic_launcher)
                    .setTicker("Your time is about to expire!")
                    .setContentTitle("Let's Chat")
                    .setContentText("Your time is about to expire!")
                    .setDefaults(Notification.DEFAULT_LIGHTS| Notification.DEFAULT_SOUND)
                    .setContentIntent(contentIntent)
                    .setContentInfo("Info");


            NotificationManager notificationManager = (NotificationManager) this.getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(1, b.build());
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
                }, fastMode ? 1 : 50);

                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {
                        logoLayout.startAnimation(logoResize);
                        frontGearBtn.setVisibility(View.VISIBLE);
                    }
                }, fastMode ? 1 : 150);

            }

            @Override
            public void onAnimationRepeat(Animation animation) {

            }
        });

    }

    class UpdateTimerTask extends TimerTask{


        @Override
        public void run() {
            final SimpleDateFormat sdf = new SimpleDateFormat(DATEFORMAT);
            sdf.setTimeZone(TimeZone.getTimeZone("UTC"));

            try {
                Date available_until = sdf.parse(currentProfile.getAvailable_until());
                Date current_time = new Date();

                if(current_time.after(available_until)){

                    if(imAvailable) {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                onTimeFinished();
                            }
                        });

                        imAvailable = false;
                    }

                }else{

                    final long mins = (available_until.getTime()-current_time.getTime())/ONE_MINUTE_IN_MILLIS;

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            setTimerButtonText(mins);
                        }
                    });

                }

                //System.out.println("Current Time" + current_time + " Available Time" + available_until);

            } catch (ParseException e) {
                e.printStackTrace();
            }

            //long t = date.getTime();

            //final String utcTime = sdf.format(new Date(t + (extraMins * ONE_MINUTE_IN_MILLIS)));

            //System.out.println(utcTime);

        }
    }


}
