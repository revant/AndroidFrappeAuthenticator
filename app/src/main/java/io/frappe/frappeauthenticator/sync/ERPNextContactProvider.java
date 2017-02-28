package io.frappe.frappeauthenticator.sync;

import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import io.frappe.frappeauthenticator.ApplicationController;
import io.frappe.frappeauthenticator.FrappeServerCallback;
import io.frappe.frappeauthenticator.authenticator.FrappeUtils;

/**
 * Created by revant on 26/2/17.
 */

public class ERPNextContactProvider {
    JSONObject data;
    JSONArray out;
    //private String out = new String();
    public JSONArray getContacts(String frappeServerURL, final String access_token, final FrappeServerCallback callback) {

        // Post params to be sent to the server
        HashMap<String, String> params = new HashMap<String, String>();
        params.put("limit_page_length","None");

        JsonObjectRequest req = new JsonObjectRequest(frappeServerURL+"/api/resource/Contact?limit_page_length=None", null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccessJSONObject(response);
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                VolleyLog.e("Error: ", error.getMessage());
            }
        }){
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> params = new HashMap<>();
                //..add other headers
                params.put("Authorization", "Bearer " + access_token);
                return params;
            }
        };
//        StringRequest stringRequest = new StringRequest(Request.Method.GET, frappeServerURL+"/api/resource/Contact",
//                new Response.Listener<String>() {
//                    @Override
//                    public void onResponse(String response) {
//                        // Display the first 500 characters of the response string.
//                        JSONArray list = new JSONArray();
//                        try {
//                            JSONObject data = new JSONObject(response);
//                            list = data.getJSONArray("data");
//                        } catch (JSONException e) {
//                            e.printStackTrace();
//                        }
//                        System.out.println(list.toString());
//                    }
//                }, new Response.ErrorListener() {
//            @Override
//            public void onErrorResponse(VolleyError error) {
//                System.out.println(error.networkResponse.toString());
//            }
//        }){
//            @Override
//            public Map<String, String> getHeaders() throws AuthFailureError {
//                Map<String,String> params = new HashMap<>();
//                //..add other headers
//                params.put("Authorization", "Bearer " + access_token);
//                return params;
//            }
//        };


        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(req);
        return out;
    }
}
