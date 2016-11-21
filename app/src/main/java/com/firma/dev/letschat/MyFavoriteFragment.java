/*
 * Copyright 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.firma.dev.letschat;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.ListFragment;
import android.support.v4.view.ViewCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.ToggleButton;

import com.firma.dev.apimanager.ApiManager;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.SyncHttpClient;

import org.apache.http.Header;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeMap;

public class MyFavoriteFragment extends ListFragment {

    onUserTimerForcedToExpire signalCallback;

    public interface onUserTimerForcedToExpire {
        public void finishUserTimer();
    }

    private SwipeRefreshLayout mSwipeRefreshLayout;
    private FavoriteAdapter fAdapter;


    Timer userContactsTimer;
    GetUserContactsTask getUserContactsTask;

    ApiManager apiManager;
    Profile currentProfile;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        final View listFragmentView = super.onCreateView(inflater, container, savedInstanceState);

        apiManager = new ApiManager();
        ProfileDataSource profileDao = new ProfileDataSource(inflater.getContext());

        try {
            profileDao.open();

            currentProfile = profileDao.getAllProfiles().get(0);
            profileDao.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        userContactsTimer = new Timer();
        getUserContactsTask = new GetUserContactsTask();

        userContactsTimer.schedule(getUserContactsTask, 1000, 60000);

        fAdapter = new FavoriteAdapter(inflater.getContext(),getActivity());
        setListAdapter(fAdapter);

        mSwipeRefreshLayout = new ListFragmentSwipeRefreshLayout(container.getContext());

        mSwipeRefreshLayout.addView(listFragmentView,
                ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        mSwipeRefreshLayout.setLayoutParams(
                new ViewGroup.LayoutParams(
                        ViewGroup.LayoutParams.MATCH_PARENT,
                        ViewGroup.LayoutParams.MATCH_PARENT));

        this.setRefreshing(false);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
            forceUpdate();
            }
        });
        return mSwipeRefreshLayout;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        try {
            signalCallback = (onUserTimerForcedToExpire) activity;
        } catch (ClassCastException e){
            throw  new ClassCastException(activity.toString() + " must implement onUserTimerForcedToExpire");
        }
    }

    private void forceUpdate(){
        getUserContactsTask.cancel();
        userContactsTimer.cancel();
        userContactsTimer.purge();

        userContactsTimer = new Timer();
        getUserContactsTask = new GetUserContactsTask();

        userContactsTimer.schedule(getUserContactsTask,0,60000);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        this.getListView().setDivider(null);
        this.getListView().setDividerHeight(0);
    }

    @Override
    public void onResume() {
        super.onResume();

        forceUpdate();
        this.setSelection(0);
    }

    public void setOnRefreshListener(SwipeRefreshLayout.OnRefreshListener listener) {
        mSwipeRefreshLayout.setOnRefreshListener(listener);
    }

    public boolean isRefreshing() {
        return mSwipeRefreshLayout.isRefreshing();
    }

    public void setRefreshing(boolean refreshing) {
        mSwipeRefreshLayout.setRefreshing(refreshing);
    }


    public SwipeRefreshLayout getSwipeRefreshLayout() {
        return mSwipeRefreshLayout;
    }

    private class ListFragmentSwipeRefreshLayout extends SwipeRefreshLayout {

        public ListFragmentSwipeRefreshLayout(Context context) {
            super(context);
        }

        @Override
        public boolean canChildScrollUp() {
            final ListView listView = getListView();
            if (listView.getVisibility() == View.VISIBLE) {
                return canListViewScrollUp(listView);
            } else {
                return false;
            }
        }

    }

    private static boolean canListViewScrollUp(ListView listView) {
        if (android.os.Build.VERSION.SDK_INT >= 14) {
            return ViewCompat.canScrollVertically(listView, -1);
        } else {
           return listView.getChildCount() > 0 &&
                    (listView.getFirstVisiblePosition() > 0
                            || listView.getChildAt(0).getTop() < listView.getPaddingTop());
        }
    }

    class GetUserContactsTask extends TimerTask {

        ApiManager apiManager = new ApiManager();
        SyncHttpClient get = new SyncHttpClient();

        List<Contact> favoriteContacts = new ArrayList<>();
        List<Contact> availableContacts = new ArrayList<>();
        List<Contact> unavailableContacts = new ArrayList<>();

        ContactsDataSource contactsDao = new ContactsDataSource(getActivity().getBaseContext());
        ProfileDataSource profileDao = new ProfileDataSource(getActivity().getBaseContext());

        @Override
        public void run() {

            try {
                contactsDao.open();
                profileDao.open();

            } catch (SQLException e) {
                e.printStackTrace();
            }


            Map<String,String> params = new TreeMap<>();
            params.put("user_token", currentProfile.getUser_token());

            String url = "http://52.24.89.20/index_v2.php/"+apiManager.getApiKey()+"/user_contacts/" + apiManager.prepareGetParams(params, true);

            new Handler(Looper.getMainLooper()).post(new Runnable() {
                @Override
                public void run() {
                    mSwipeRefreshLayout.setRefreshing(false);
                }
            });

            get.get(url, new AsyncHttpResponseHandler() {
                @Override
                public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {

//                    System.out.println(new String(responseBody));

                    JsonParser parser = new JsonParser();
                    JsonObject responseObj = (JsonObject) parser.parse(new String(responseBody));

                    if (!responseObj.get("error").getAsBoolean()) {

                        favoriteContacts.clear();
                        availableContacts.clear();
                        unavailableContacts.clear();

                        JsonArray user_contacts = responseObj.get("response").getAsJsonObject().get("user_contacts").getAsJsonArray();

                        for (JsonElement aux : user_contacts) {
                            JsonObject auxObj = aux.getAsJsonObject();

                            Contact auxContact = contactsDao.getContactFromFormattedNumber(auxObj.get("phone_number").getAsString());

                            if (auxContact != null && (!auxContact.getFormatted_phone_number().equals(currentProfile.getPhone_number()) )) {
                                auxContact.setAvailable_until(auxObj.get("available_until").getAsString());
                                if (auxContact.getAvailableTime() > 0) {
                                    if(auxContact.getFavorite() == 1)
                                        favoriteContacts.add(auxContact);
                                    else
                                        availableContacts.add(auxContact);
                                } else {
                                    unavailableContacts.add(auxContact);
                                }
                            } else {
                                if (auxObj.get("available_until").getAsString().equals("0000-00-00 00:00:00") && currentProfile.getPhone_number().equals(auxObj.get("phone_number").getAsString())) {
                                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            signalCallback.finishUserTimer();
                                        }
                                    });
                                }
                            }
                        }

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                fAdapter.clearAll();

                                if(favoriteContacts.size() > 0)
                                    fAdapter.addItem(new Contact("Favorites"));

                                for (Contact aux : favoriteContacts)
                                    fAdapter.addItem(aux);

                                if (availableContacts.size() > 0)
                                    fAdapter.addItem(new Contact("Available Contacts"));

                                for (Contact aux : availableContacts)
                                    fAdapter.addItem(aux);

                                if (unavailableContacts.size() > 0)
                                    fAdapter.addItem(new Contact("Unavailable Contacts"));

                                for (Contact aux : unavailableContacts)
                                    fAdapter.addItem(aux);

                            }
                        });
                    }
                }

                @Override
                public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                }
            });

            contactsDao.close();
        }

    }

}
