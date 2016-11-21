package com.firma.dev;

import android.util.Log;

import com.parse.Parse;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParsePush;
import com.parse.PushService;
import com.parse.SaveCallback;

public class Application extends android.app.Application {

    public Application() {
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Parse.initialize(this, "LMYycFYINhJ7UnjHhk7N3UYEJQivJqdHozBG4T11", "AzkMZcJRu5hLjA2zvFvlalIh9re7gH0JQZCuvWDq");
        ParseInstallation.getCurrentInstallation().saveInBackground();

        //PushService.setDefaultPushCallback(this, DummyActivity.class);
    }
}