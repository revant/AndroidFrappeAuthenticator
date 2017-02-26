package io.frappe.frappeauthenticator.sync;

import android.util.Log;

import org.apache.commons.io.IOUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import io.frappe.frappeauthenticator.authenticator.FrappeUtils;

/**
 * Created by revant on 26/2/17.
 */

public class ERPNextContactProvider {
    public JSONArray getContacts(String frappeServerURL, String access_token) {
        Log.i("GETCont", frappeServerURL+" "+access_token);
        JSONArray out = new JSONArray();
        JSONObject data = new JSONObject();
        HashMap<String,String> params=new HashMap<>();
        params.put("fields", "[\"name\",\"last_name\", \"email_id\", \"mobile_no\",\"supplier_name\",\"customer_name\",\"first_name\",\"department\",\"designation\",\"phone\",\"sales_partner\"]");
        params.put("limit_page_length", "None");
        try{
            URL url=new URL(frappeServerURL+"/api/resource/Contact");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");
            conn.setRequestProperty("Authorization", "Bearer " + access_token);
            conn.setDoInput(true);
            conn.setDoOutput(true);

            OutputStream os = conn.getOutputStream();
            BufferedWriter writer = new BufferedWriter(
                    new OutputStreamWriter(os, "UTF-8"));
            writer.write(FrappeUtils.postDataStr(params));

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
                data = new JSONObject(is);
                out = data.getJSONArray("data");
        }catch (Exception e)
        {
            e.printStackTrace();
        }
        return out;
    }
}
