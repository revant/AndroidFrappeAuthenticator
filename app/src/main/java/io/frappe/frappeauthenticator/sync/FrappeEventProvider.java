package io.frappe.frappeauthenticator.sync;

import com.android.volley.AuthFailureError;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.JsonObjectRequest;

import org.json.JSONArray;
import org.json.JSONObject;

import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by revant on 26/2/17.
 */

public class FrappeEventProvider {
    JSONObject data;
    JSONArray out;
    //private String out = new String();
    public void getEvents(String frappeServerURL, final String access_token, final FrappeServerCallback callback) {
        Calendar cal = Calendar.getInstance();
        cal.add(Calendar.DATE,-3);
        Date t_minus_3 = cal.getTime();
        SimpleDateFormat dt = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        // POST params to be sent to the server
        // HashMap<String, String> params = new HashMap<String, String>();
        // params.put("limit_page_length","None");

        // GET params
        //name, last_name, email_id, mobile_no, supplier_name, customer_name, first_name, department, designation, phone, sales_partner
        JsonObjectRequest req = new JsonObjectRequest(
                frappeServerURL + "/api/resource/Event?limit_page_length=None" +
                        "&fields=[\"event_type\",\"all_day\",\"subject\",\"description\",\"name\",\"starts_on\",\"ends_on\"]" /*+
                        "&filters=[[\"starts_on\",\">\",\"" + URLEncoder.encode(dt.format(t_minus_3)) + "\"]]"*/,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        callback.onSuccessJSONObject(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        System.out.println(error.getClass().getSimpleName());
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

        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(req);
    }
}
