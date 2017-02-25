package io.frappe.frappeauthenticator.authenticator;

import org.json.JSONObject;

/**
 * User: Frappe
 * Date: 3/27/13
 * Time: 2:35 AM
 */
public interface ServerAuthenticate {
    //public String userSignUp(final String name, final String email, final String pass, String authType) throws Exception;
    public String userSignIn(String TOKEN_URL, JSONObject authMethod, String CLIENT_ID, String REDIRECT_URI) throws Exception;
    public JSONObject getOpenIDProfile(String accessToken, String OPENID_PROFILE_URL);
}
