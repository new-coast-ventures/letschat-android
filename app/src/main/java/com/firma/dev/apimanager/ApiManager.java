package com.firma.dev.apimanager;

import android.util.Base64;

import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.loopj.android.http.RequestParams;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Created by Eskoria on 06/08/2015.
 */
public class ApiManager {

    final static String apiSecret = "083041de5bd5f17bcedc04fee69b8af5";

    public static String getApiKey() {
        return apiKey;
    }

    final static String apiKey = "3ffac77fb238612caf9624d7c8f64223";


    public ApiManager() {

    }


    public RequestParams preparePostParams(Map<String, String> params){

        RequestParams body = new RequestParams();

        String token = "";

        token += apiSecret;

        SortedSet<String> keys = new TreeSet<String>(params.keySet());
        for (String key : keys) {
            String value = params.get(key);

            body.put(key, value);
            token += value;
        }

        body.put("token", sha256(token));

        return  body;
    }

    public RequestParams preparePostFileParams(Map<String,String> params, String file){
        RequestParams body = new RequestParams();

        String token = "";

        token += apiSecret;

            File auxFile = new File(file);

            try {
                body.put("user_contacts",auxFile);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

        SortedSet<String> keys = new TreeSet<String>(params.keySet());
        for (String key : keys) {
            String value = params.get(key);

            body.put(key, value);
            token += value;
        }

        body.put("token", sha256(token));

        return  body;
    }

    public String prepareGetParams(Map<String,String> params,boolean base64encode){
        String token = "";

        token += apiSecret;

        JsonObject getObj = new JsonObject();


        SortedSet<String> keys = new TreeSet<String>(params.keySet());
        for (String key : keys) {
            String value = params.get(key);

            getObj.add(key, new JsonPrimitive(value));

            token += value;
        }

        getObj.add("token", new JsonPrimitive(sha256(token)));


        if(base64encode){
            try {
                return Base64.encodeToString(getObj.toString().getBytes("UTF-8"),Base64.DEFAULT);
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }else{
            return getObj.toString();
        }

        return "";

    }

    public static String sha256(String base) {
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(base.getBytes("UTF-8"));
            StringBuffer hexString = new StringBuffer();

            for (int i = 0; i < hash.length; i++) {
                String hex = Integer.toHexString(0xff & hash[i]);
                if(hex.length() == 1) hexString.append('0');
                hexString.append(hex);
            }

            return hexString.toString();
        } catch(Exception ex){
            throw new RuntimeException(ex);
        }
    }

}
