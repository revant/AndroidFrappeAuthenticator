package io.frappe.frappeauthenticator;

/**
 * Created by revant on 28/2/17.
 */

import org.json.JSONArray;
import org.json.JSONObject;

public interface FrappeServerCallback{
    public void onSuccessJSONObject(JSONObject result);
    //public void onSuccessJSONArray(JSONArray result);
    //public void onSuccessString(String result);
}
