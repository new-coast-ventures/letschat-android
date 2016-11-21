package com.firma.dev.letschat;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.support.v4.app.FragmentActivity;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
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
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.zip.Inflater;

/**
 * Created by Eskoria on 29/07/2015.
 */
public class FavoriteAdapter extends BaseAdapter {

    private static final int SECTION_HEADER = 0;
    private static final int AVAILABLE_CONTACT = 1;
    private static final int UNAVAILABLE_CONTACT = 2;

    private ArrayList<Contact> mData = new ArrayList<Contact>();

    ListView parentList;

    public Context context;
    public Context actContext;
    private LayoutInflater mInflater;

    public FavoriteAdapter(Context context,Context actContext) {
        this.context = context;
        this.actContext = actContext;
        mInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    public void addItem(final Contact item){
        mData.add(item);
        notifyDataSetChanged();
    }

    public void clearAll(){
        mData.clear();
        notifyDataSetChanged();
    }

    @Override
    public int getViewTypeCount() {
        return 3;
    }

    @Override
    public int getItemViewType(int position) {
        return mData.get(position).getType();
    }

    @Override
    public int getCount() {
        return mData.size();
    }

    @Override
    public Object getItem(int position) {
        return mData.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(parentList == null)
            parentList = (ListView) parent;

        View v = convertView;
        int type = getItemViewType(position);

        if(v == null){
            LayoutInflater inflater = (LayoutInflater) parent.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

            if(type == SECTION_HEADER){
                v = inflater.inflate(R.layout.row_section,parent,false);

            }else{
                v = inflater.inflate(R.layout.favorite_item,parent,false);
            }
        }

        Contact fav = mData.get(position);

        if(type == SECTION_HEADER){
            TextView sectionTitle = (TextView)v.findViewById(R.id.textView1);
            sectionTitle.setText(fav.getDisplay_name());
        }else{
            TextView contactName = (TextView) v.findViewById(R.id.contactName);
            TextView contactTimer = (TextView) v.findViewById(R.id.contactTimer);
            ToggleButton favBtn = (ToggleButton) v.findViewById(R.id.favBtn);

            RelativeLayout backLayout = (RelativeLayout) v.findViewById(R.id.backLayout);
            RelativeLayout favLayout = (RelativeLayout) v.findViewById(R.id.favLayout);

            backLayout.setOnClickListener(mOnBackClickListener);
            favLayout.setOnClickListener(mOnFavClickListener);

            favBtn.setChecked(fav.getFavorite() == 0 ? false : true);

            String fullName = fav.getDisplay_name();

            String[] spName = fullName.split(" ");

            fullName = "";

            for(int i = 0; i < spName.length; i++){

                if (i == 0)
                    fullName += "<b>" + spName[0] + "</b> ";
                else
                    fullName += spName[i] + " ";

            }


            if(type == AVAILABLE_CONTACT){
                contactName.setTextColor(Color.parseColor("#000000"));
                contactName.setText(Html.fromHtml(fullName));

                long timeAvailable = fav.getAvailableTime();
                String timeText = "";

                if(timeAvailable <= 0) {
                    contactName.setTextColor(Color.parseColor("#cccccc"));
                }else if(timeAvailable > 0 && timeAvailable < 15){
                    contactTimer.setTextColor(Color.parseColor("#b52f0e"));
                    timeText = timeAvailable + " min";
                }else{
                    contactTimer.setTextColor(Color.parseColor("#41e00f"));
                    timeText = timeAvailable + " min";
                }

                contactTimer.setText(timeText);

            }else if(type == UNAVAILABLE_CONTACT){
                contactName.setTextColor(Color.parseColor("#cccccc"));
                contactName.setText(fav.getDisplay_name());
                contactTimer.setText("");
            }
        }


        return v;
    }

    private View.OnClickListener mOnBackClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {


            ProfileDataSource pDao = new ProfileDataSource(mInflater.getContext());

            Contact favClicked = mData.get(parentList.getPositionForView(v));
            final AsyncHttpClient post = new AsyncHttpClient();

            ApiManager apiManager = new ApiManager();

            if(favClicked.isCurrentlyAvailable()){

                String uri = "tel:" + favClicked.getPhone_number();
                Intent intent = new Intent(Intent.ACTION_CALL);
                intent.setData(Uri.parse(uri));
                parentList.getContext().startActivity(intent);


                try {
                    pDao.open();
                    Profile currentProfile = pDao.getAllProfiles().get(0);
                    pDao.close();

                    Map<String,String> params = new TreeMap<>();
                    params.put("user_phone_number",currentProfile.getPhone_number());
                    params.put("contact_phone_number",favClicked.getFormatted_phone_number());
                    params.put("available_time",String.valueOf(currentProfile.getAvailableTime()));

                    String url = "http://altatrabajo.com.ar/index_v2.php/" + ApiManager.getApiKey() + "/set_unavailable";

                    post.post(url, apiManager.preparePostParams(params), new AsyncHttpResponseHandler() {
                        @Override
                        public void onSuccess(int statusCode, Header[] headers, byte[] responseBody) {
                            System.out.println("Set Unavailable Result: " + new String(responseBody));
                        }

                        @Override
                        public void onFailure(int statusCode, Header[] headers, byte[] responseBody, Throwable error) {

                        }
                    });

                } catch (SQLException e) {
                    e.printStackTrace();
                }



            }else{
                FragmentActivity activity = (FragmentActivity)actContext;
                FragmentManager fm = activity.getFragmentManager();
                AlertDFragment favView = new AlertDFragment();

                favView.setTitleTxt("Let's Chat");
                favView.setDescTxt(favClicked.getDisplay_name() + " is not available to chat right now!");
                favView.show(fm,"fragment_alert");

            }

        }
    };

    private View.OnClickListener mOnFavClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {

            FragmentActivity activity = (FragmentActivity)actContext;
            FragmentManager fm = activity.getFragmentManager();
            AlertDFragment favView = new AlertDFragment();


            Contact favClicked = mData.get(parentList.getPositionForView(v));
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

            ContactsDataSource cDao = new ContactsDataSource(mInflater.getContext());
            ProfileDataSource pDao = new ProfileDataSource(mInflater.getContext());

            try {
                cDao.open();
                pDao.open();
                cDao.setFavorite(favClicked);

                Profile currentProfile = pDao.getAllProfiles().get(0);

                List<Contact> allFavorites = cDao.getAllFavorites();
                File file = new File(mInflater.getContext().getFilesDir(), currentProfile.getUser_token() + ".json");

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
