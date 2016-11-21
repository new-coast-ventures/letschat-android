package com.firma.dev.letschat;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.firma.dev.AlertDFragment;
import com.firma.dev.apimanager.ApiManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonPrimitive;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.AsyncHttpResponseHandler;
import com.loopj.android.http.RequestParams;

import org.apache.http.Header;

public class AlphabetListAdapter extends BaseAdapter {

    ListView parentList;

    public static abstract class Row {}
    
    public static final class Section extends Row {
        public final String text;

        public Section(String text) {
            this.text = text;
        }
    }
    
    public static final class Item extends Row {
        public final String text;
        public final String number;
        public final Integer hasLetsChat;
        public final Integer isFavorite;

        public Item(String text, String number,Integer hasLetsChat, Integer isFavorite) {
            this.text = text;
            this.number = number;
            this.hasLetsChat = hasLetsChat;
            this.isFavorite = isFavorite;
        }
    }
    
    private List<Row> rows;
    
    public void setRows(List<Row> rows) {
        this.rows = rows;
    }

    @Override
    public int getCount() {
        return rows.size();
    }

    @Override
    public Row getItem(int position) {
        return rows.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }
    
    @Override
    public int getViewTypeCount() {
        return 2;
    }
    
    @Override
    public int getItemViewType(int position) {
        if (getItem(position) instanceof Section) {
            return 1;
        } else {
            return 0;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = convertView;

        if(parentList == null)
            parentList = (ListView) parent;


        if (getItemViewType(position) == 0) { // Item
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = (RelativeLayout) inflater.inflate(R.layout.row_item, parent, false);
            }

            Item currentItem = (Item) getItem(position);

            RelativeLayout inviteLayout = (RelativeLayout) view.findViewById(R.id.inviteLayout);
            RelativeLayout favLayout = (RelativeLayout) view.findViewById(R.id.favLayout);
            
            Item item = (Item) getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.textView1);
            TextView numberView = (TextView) view.findViewById(R.id.numberText);
            ImageButton inviteBtn = (ImageButton) view.findViewById(R.id.inviteBtn);
            ToggleButton favBtn = (ToggleButton) view.findViewById(R.id.favBtn);

            System.out.println("Current Item : " + item.text + " Has lets chat " + item.hasLetsChat);

            if(currentItem.hasLetsChat == 1){
                inviteLayout.setVisibility(View.GONE);
                favLayout.setVisibility(View.VISIBLE);
                favLayout.setOnClickListener(mOnFavClickListener);
                favBtn.setChecked(currentItem.isFavorite == 1 ? true : false);

            }else {
                favLayout.setVisibility(View.GONE);
                inviteLayout.setVisibility(View.VISIBLE);
                inviteBtn.setOnClickListener(mOnInviteClickListener);
            }

            textView.setText(item.text);
            numberView.setText(item.number);


        } else { // Section
            if (view == null) {
                LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = (LinearLayout) inflater.inflate(R.layout.row_section, parent, false);  
            }
            
            Section section = (Section) getItem(position);
            TextView textView = (TextView) view.findViewById(R.id.textView1);
            textView.setText(section.text);
        }
        
        return view;
    }

    private View.OnClickListener mOnInviteClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            final String pressed = ((Item)getItem(parentList.getPositionForView(v))).number;
            ApiManager apiManager = new ApiManager();
            final AsyncHttpClient post = new AsyncHttpClient();

             ProfileDataSource pDao = new ProfileDataSource(parentList.getContext());

            try {
                pDao.open();

                Profile currentProfile = pDao.getAllProfiles().get(0);

                Map<String,String> params = new TreeMap<>();

                params.put("from_name",currentProfile.getDisplay_name());

                String url = "http://altatrabajo.com.ar/index_v2.php/" + ApiManager.getApiKey() + "/invice_contacts";


                post.post(url, apiManager.preparePostParams(params), new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        //System.out.println(new String(responseBody)); //"Invite Contacts Result: " +

                        //JsonParser parser = new JsonParser();
                        //JsonObject response = (JsonObject) parser.parse(new String(responseBody));

//                        if(!response.get("error").getAsBoolean()){
                            Uri uri = Uri.parse("smsto:"+pressed);
                            Intent it = new Intent(Intent.ACTION_SENDTO, uri);
                            it.putExtra("sms_body", "Check out the Let's Chat app: http://www.letschatapp.com");
                            parentList.getContext().startActivity(it);
///                        }


                        //System.out.println(response);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });


                pDao.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }


        }
    };

    private View.OnClickListener mOnFavClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            ContactsDataSource cDao = new ContactsDataSource(parentList.getContext());
            ProfileDataSource pDao = new ProfileDataSource(parentList.getContext());

            try {
                cDao.open();
                pDao.open();
            FragmentActivity activity = (FragmentActivity)parentList.getContext();
            FragmentManager fm = activity.getFragmentManager();
            AlertDFragment favView = new AlertDFragment();


            Contact favClicked = cDao.getContactFromNumber(((Item)rows.get(parentList.getPositionForView(v))).number);
            final AsyncHttpClient post = new AsyncHttpClient();

            ToggleButton favTBtn = (ToggleButton) v.findViewById(R.id.favBtn);

            boolean newFavState = !(favClicked.getFavorite() != 0);
            favTBtn.setChecked(newFavState);
            favClicked.setFavorite(newFavState ? 1 : 0);

            favView.setTitleTxt("Let's Chat");

            if(newFavState)
                favView.setDescTxt("Great! We'll notify you when " + favClicked.getDisplay_name() + " is available to chat!");
            else
                favView.setDescTxt("You'll no longer receive notifications when " + favClicked.getDisplay_name() + " is available!");

            favView.show(fm,"fragment_alert");



                cDao.setFavorite(favClicked);

                Profile currentProfile = pDao.getAllProfiles().get(0);

                List<Contact> allFavorites = cDao.getAllFavorites();
                File file = new File(parentList.getContext().getFilesDir(), currentProfile.getUser_token() + ".json");

                FileOutputStream fos = new FileOutputStream(file);
                JsonArray numbersArray = new JsonArray();
                Gson gsonParser = new GsonBuilder()
                        .setPrettyPrinting()
                        .disableHtmlEscaping()
                        .create();

                for(Contact favContact : allFavorites)
                    numbersArray.add(new JsonPrimitive(favContact.getFormatted_phone_number()));

                fos.write(gsonParser.toJson(numbersArray).getBytes());
                fos.close();

                String apiScret = "083041de5bd5f17bcedc04fee69b8af5";

                String token = "";

                token += apiScret;


                String urlContacts = "http://altatrabajo.com.ar/index_v2.php/" + ApiManager.getApiKey() + "/sync_fav_contacts";

                RequestParams tmpParams = new RequestParams();
                try {
                    tmpParams.put("user_contacts", file);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }

                token += currentProfile.getUser_token();
                tmpParams.put("user_token", currentProfile.getUser_token());
                tmpParams.put("token", ApiManager.sha256(token));

                post.post(urlContacts, tmpParams, new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                        System.out.println("Sync Fav Contacts Result: " + new String(responseBody));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                    }
                });


                cDao.close();
            } catch (SQLException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

    };

}
