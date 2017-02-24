package io.frappe.frappeauthenticator.authenticator;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import io.frappe.frappeauthenticator.R;

/**
 * Handles the comminication with Parse.com
 *
 * User: Frappe
 * Date: 3/27/13
 * Time: 3:30 AM
 */
public class FrappeServerAuthenticate implements ServerAuthenticate{
    String authtoken;
//    private static FrappeServerAuthenticate instance = null;
//    //for Volley API
//    public RequestQueue requestQueue;
//    public FrappeServerAuthenticate()
//    {
//        //other stuf if you need
//    }
//    private FrappeServerAuthenticate(Context context)
//    {
//        requestQueue = Volley.newRequestQueue(context.getApplicationContext());
//        //other stuf if you need
//    }
//    public static synchronized FrappeServerAuthenticate getInstance(Context context)
//    {
//        if (null == instance)
//            instance = new FrappeServerAuthenticate(context);
//        return instance;
//    }
//
//    //this is so you don't need to pass context each time
//    public static synchronized FrappeServerAuthenticate getInstance()
//    {
//        if (null == instance)
//        {
//            throw new IllegalStateException(FrappeServerAuthenticate.class.getSimpleName() +
//                    " is not initialized, callt getInstance(...) first");
//        }
//        return instance;
//    }
    @Override
    public String userSignIn(String TOKEN_URL, final JSONObject authCode, final String CLIENT_ID, final String REDIRECT_URI) throws Exception {
        Log.i("SigningIn Params", TOKEN_URL+authCode.toString()+CLIENT_ID+REDIRECT_URI);
        HashMap<String,String> params=new HashMap<>();
        params.put("client_id", CLIENT_ID);
        params.put("redirect_uri", REDIRECT_URI);
        try {
            if (authCode.get("type")=="refresh"){
                String refresh_token = (String) authCode.get("refresh_token");
                params.put("grant_type", "refresh_token");
                params.put("refresh_token", refresh_token);
            }
            else if(authCode.get("type")=="code"){
                String code = (String) authCode.get("code");
                params.put("grant_type", "authorization_code");
                params.put("code", code);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try{
            URL url=new URL(TOKEN_URL);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(postDataStr(params));

            writer.flush();
            writer.close();
            os.close();
            InputStream inputStream = conn.getInputStream();
            StringWriter stringWriter = new StringWriter();
            IOUtils.copy(inputStream, stringWriter, "UTF-8");
            String is = stringWriter.toString();
            int responseCode=conn.getResponseCode();
            conn.disconnect();
            if (responseCode == HttpURLConnection.HTTP_OK)
                return is;
            else
                return "";
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return authtoken;
    }
    private class ParseComError implements Serializable {
        int code;
        String error;
    }
    private String postDataStr(HashMap<String, String> params) throws UnsupportedEncodingException {
        StringBuilder result = new StringBuilder();
        boolean first = true;
        for(Map.Entry<String, String> entry : params.entrySet()){
            if (first)
                first = false;
            else
                result.append("&");

            result.append(URLEncoder.encode(entry.getKey(), "UTF-8"));
            result.append("=");
            result.append(URLEncoder.encode(entry.getValue(), "UTF-8"));
        }

        return result.toString();
    }
    private class User implements Serializable {
        private String firstName;
        private String lastName;
        private String username;
        private String phone;
        private String objectId;
        public String sessionToken;
        private String gravatarId;


        private String avatarUrl;

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getPhone() {
            return phone;
        }

        public void setPhone(String phone) {
            this.phone = phone;
        }

        public String getObjectId() {
            return objectId;
        }

        public void setObjectId(String objectId) {
            this.objectId = objectId;
        }

        public String getSessionToken() {
            return sessionToken;
        }

        public void setSessionToken(String sessionToken) {
            this.sessionToken = sessionToken;
        }

        public String getGravatarId() {
            return gravatarId;
        }

        public void setGravatarId(String gravatarId) {
            this.gravatarId = gravatarId;
        }

        public String getAvatarUrl() {
            return avatarUrl;
        }
        public void setAvatarUrl(String avatarUrl) {
            this.avatarUrl = avatarUrl;
        }
    }
}
