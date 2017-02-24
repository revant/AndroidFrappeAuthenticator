package io.frappe.frappeauthenticator.authenticator;


import android.util.Base64;

import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

/**
 * Created by revant on 22/2/17.
 */

public class JWTUtils {
    public static JSONObject decoded(String JWTEncoded) throws Exception {
        JSONObject out = new JSONObject();
        try {
            String[] split = JWTEncoded.split("\\.");
            out.put("header", getJson(split[0]));
            out.put("body", getJson(split[1]));
        } catch (UnsupportedEncodingException e) {
            //Error
        }
        return out;
    }

    private static String getJson(String strEncoded) throws UnsupportedEncodingException{
        byte[] decodedBytes = Base64.decode(strEncoded, Base64.URL_SAFE);
        return new String(decodedBytes, "UTF-8");
    }
}
