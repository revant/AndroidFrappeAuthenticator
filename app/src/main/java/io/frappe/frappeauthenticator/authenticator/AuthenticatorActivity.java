package io.frappe.frappeauthenticator.authenticator;

import android.accounts.Account;
import android.accounts.AccountAuthenticatorActivity;
import android.accounts.AccountManager;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import io.frappe.frappeauthenticator.R;
import static io.frappe.frappeauthenticator.authenticator.AccountGeneral.sServerAuthenticate;
import static io.frappe.frappeauthenticator.authenticator.AccountGeneral.AUTH_ENDPOINT;
import static io.frappe.frappeauthenticator.authenticator.AccountGeneral.TOKEN_ENDPOINT;
import static io.frappe.frappeauthenticator.authenticator.AccountGeneral.OPENID_PROFILE_ENDPOINT;


/**
 * The Authenticator activity.
 *
 * Called by the Authenticator and in charge of identifing the user.
 *
 * It sends back to the Authenticator the result.
 */
public class AuthenticatorActivity extends AccountAuthenticatorActivity {

    public final static String ARG_ACCOUNT_TYPE = "ACCOUNT_TYPE";
    public final static String ARG_AUTH_TYPE = "AUTH_TYPE";
    public final static String ARG_ACCOUNT_NAME = "ACCOUNT_NAME";
    public final static String ARG_IS_ADDING_NEW_ACCOUNT = "IS_ADDING_ACCOUNT";

    public static final String KEY_ERROR_MESSAGE = "ERR_MSG";

    public final static String PARAM_USER_PASS = "USER_PASS";

    public String authCode;

    private final int REQ_SIGNUP = 1;

    private final String TAG = this.getClass().getSimpleName();

    private AccountManager mAccountManager;
    private String mAuthTokenType, REDIRECT_URI, userFrappeServer, CLIENT_ID, authToken, OPENID_PROFILE_URL;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.act_login);
        mAccountManager = AccountManager.get(getBaseContext());

        String accountName = getIntent().getStringExtra(ARG_ACCOUNT_NAME);
        mAuthTokenType = getIntent().getStringExtra(ARG_AUTH_TYPE);
        if (mAuthTokenType == null)
            mAuthTokenType = AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS;

//        if (accountName != null) {
//            ((TextView)findViewById(R.id.accountName)).setText(accountName);
//        }

        findViewById(R.id.submit).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submit();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        // The sign up activity returned that the user has successfully created an account
        if (requestCode == REQ_SIGNUP && resultCode == RESULT_OK) {
            finishLogin(data);
        } else
            super.onActivityResult(requestCode, resultCode, data);
    }

    public void submit() {

        final String clientSecret = ((TextView) findViewById(R.id.accountClientSecret)).getText().toString();
        userFrappeServer = ((TextView) findViewById(R.id.accountFrappeServer)).getText().toString();
        final String AUTH_URL = userFrappeServer + AUTH_ENDPOINT;
        final String TOKEN_URL = userFrappeServer + TOKEN_ENDPOINT;
        CLIENT_ID = ((TextView) findViewById(R.id.accountClientId)).getText().toString();
        REDIRECT_URI = ((TextView) findViewById(R.id.accountRedirectUri)).getText().toString();
        OPENID_PROFILE_URL = userFrappeServer + OPENID_PROFILE_ENDPOINT;
        final String OAUTH_SCOPE = ((TextView) findViewById(R.id.accountScope)).getText().toString();

        final String accountType = getIntent().getStringExtra(ARG_ACCOUNT_TYPE);

        final WebView webView = (WebView) findViewById(R.id.webv);
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setVisibility(View.VISIBLE);
        webView.clearCache(true);
        webView.loadUrl(AUTH_URL+"?redirect_uri="+REDIRECT_URI+"&response_type=code&client_id="+CLIENT_ID+"&scope="+OAUTH_SCOPE);
        webView.setWebViewClient(new WebViewClient() {

            boolean authComplete = false;
            Intent resultIntent = new Intent();

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon){
                super.onPageStarted(view, url, favicon);

            }
            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                if (url.contains("?code=") && authComplete != true) {
                    Uri uri = Uri.parse(url);
                    authCode = uri.getQueryParameter("code");
                    Log.i("", "CODE : " + authCode);
                    new AsyncTask<String, Void, Intent>() {

                        @Override
                        protected Intent doInBackground(String... params) {
                            Log.d("frappe", TAG + "> Started authenticating");

                            String authtoken = null;
                            Bundle data = new Bundle();
                            try {
                                JSONObject authMethod = new JSONObject();
                                authMethod.put("type", "code");
                                authMethod.put("code", authCode);
                                authtoken = sServerAuthenticate.userSignIn(TOKEN_URL, authMethod, CLIENT_ID, REDIRECT_URI);
                                authToken = authtoken;
                                JSONObject bearerToken = new JSONObject(authtoken);
                                JSONObject openIDProfile = sServerAuthenticate.getOpenIDProfile(bearerToken.get("access_token").toString(),OPENID_PROFILE_URL);
                                //JSONObject id_token = JWTUtils.decoded(bearerToken.get("id_token").toString());
                                data.putString(AccountManager.KEY_ACCOUNT_NAME, openIDProfile.get("email").toString());
                                data.putString(AccountManager.KEY_ACCOUNT_TYPE, accountType);
                                data.putString(AccountManager.KEY_AUTHTOKEN, authtoken);
                                data.putString(PARAM_USER_PASS, clientSecret);

                            } catch (Exception e) {
                                data.putString(KEY_ERROR_MESSAGE, e.getMessage());
                            }

                            final Intent res = new Intent();
                            res.putExtras(data);
                            return res;
                        }

                        @Override
                        protected void onPostExecute(Intent intent) {
                            if (intent.hasExtra(KEY_ERROR_MESSAGE)) {
                                Toast.makeText(getBaseContext(), intent.getStringExtra(KEY_ERROR_MESSAGE), Toast.LENGTH_SHORT).show();
                            } else {
                                finishLogin(intent);
                            }
                        }
                    }.execute();
                    webView.setVisibility(View.GONE);
                }else if(url.contains("redirect_uri=http%3A%2F%2Flocalhost") && authComplete != true) {
                    Toast.makeText(getBaseContext(), "Allow or Deny Access to Resources", Toast.LENGTH_LONG).show();
                }else if(url.contains("error=access_denied")){
                    Log.i("", "ACCESS_DENIED_HERE");
                    resultIntent.putExtra("code", authCode);
                    authComplete = true;
                }
            }
        });
    }

    private void finishLogin(Intent intent) {
        Log.d("frappe", TAG + "> finishLogin");

        String accountName = intent.getStringExtra(AccountManager.KEY_ACCOUNT_NAME);
        String accountPassword = intent.getStringExtra(PARAM_USER_PASS);
        final Account account = new Account(accountName, intent.getStringExtra(AccountManager.KEY_ACCOUNT_TYPE));

        if (getIntent().getBooleanExtra(ARG_IS_ADDING_NEW_ACCOUNT, false)) {
            Log.d("frappe", TAG + "> finishLogin > addAccountExplicitly");
            String authtoken = intent.getStringExtra(AccountManager.KEY_AUTHTOKEN);
            String authtokenType = mAuthTokenType;

            // Creating the account on the device and setting the auth token we got
            // (Not setting the auth token will cause another call to the server to authenticate the user)
            mAccountManager.addAccountExplicitly(account, accountPassword, null);
            mAccountManager.setAuthToken(account, authtokenType, authtoken);
            JSONObject bearerToken;
            try {
                bearerToken = new JSONObject(authtoken);
                mAccountManager.setUserData(account, "refreshToken", bearerToken.get("refresh_token").toString());
                mAccountManager.setUserData(account, "redirectURI", REDIRECT_URI);
                mAccountManager.setUserData(account, "frappeServer", userFrappeServer);
                mAccountManager.setUserData(account, "clientId", CLIENT_ID);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Log.d("frappe", TAG + "> finishLogin > setPassword");
        } else {
            Log.d("frappe", TAG + "> finishLogin > setPassword");
            mAccountManager.setPassword(account, accountPassword);
        }

        setAccountAuthenticatorResult(intent.getExtras());
        setResult(RESULT_OK, intent);
        finish();
    }

}
