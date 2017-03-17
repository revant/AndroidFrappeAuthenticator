# Develop Android Apps using Frappe Authenticator

### Prerequisites:

 - Add OAuth Client on frappe server
 - Android - using Volley for jsonrequest

#### Sample App: https://github.com/revant/FrappeAuthExample

### Step 1 : How to check whether Frappe Authenticator is installed. Call this wherever the check is required.

```
if (!appInstalledOrNot("io.frappe.frappeauthenticator")){
    new AlertDialog.Builder(MainActivity.this)
        .setTitle("Install App")
        .setMessage("Please Install Frappe Authenticator")
        .show();
}
// method to check if app is installed.
private boolean appInstalledOrNot(String uri) {
    PackageManager pm = getPackageManager();
    try {
        pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
        return true;
    } catch (PackageManager.NameNotFoundException e) {
        // TODO
    }
    return false;
}
```
#### Option 1 : If one account found, select it, get token and use it

#### Option 2 : If multiple accounts found, ask to select one.

```
private void getAuthToken(String accountType, String authTokenType) {
    Account[] accounts = mAccountManager.getAccountsByType(accountType);
    rememberIdpSettings(accountType, authTokenType);
    if (!appInstalledOrNot("io.frappe.frappeauthenticator")){
        new AlertDialog.Builder(MainActivity.this)
            .setTitle("Install App")
            .setMessage("Please Install Frappe Authenticator")
            .show();
    }
    else if (accounts.length == 1) {
        Log.d("account", accounts[0].name);
        mAccount = accounts[0];
        getAuthToken(accounts[0], authTokenType);
    }
    else {
        Intent intent = AccountManager.newChooseAccountIntent(null, null, new String[]{accountType}, null, null, null, null);
        startActivityForResult(intent, 1);
    }
}
```
### Step 2 : Use token as header in API to interact with frappe REST endpoints.

Class to connect to Frappe Server.

```
public class ERPNextContactProvider {
    JSONArray out;
    public JSONArray getContacts(String frappeServerURL, final String access_token, final FrappeServerCallback callback) {

        // Post params to be sent to the server
        // HashMap<String, String> params = new HashMap<String, String>();
        // params.put("limit_page_length","None");

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

        // add the request object to the queue to be executed
        ApplicationController.getInstance().addToRequestQueue(req);
        return out;
    }
}
```
Other things required,

ApplicationController class for volley
```
public class ApplicationController extends Application {

    /**
     * Log or request TAG
     */
    public static final String TAG = "VolleyPatterns";

    /**
     * Global request queue for Volley
     */
    private RequestQueue mRequestQueue;

    /**
     * A singleton instance of the application class for easy access in other places
     */
    private static ApplicationController sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        // initialize the singleton
        sInstance = this;
    }

    /**
     * @return ApplicationController singleton instance
     */
    public static synchronized ApplicationController getInstance() {
        return sInstance;
    }

    /**
     * @return The Volley Request queue, the queue will be created if it is null
     */
    public RequestQueue getRequestQueue() {
        // lazy initialize the request queue, the queue instance will be
        // created when it is accessed for the first time
        if (mRequestQueue == null) {
            mRequestQueue = Volley.newRequestQueue(getApplicationContext());
        }

        return mRequestQueue;
    }

    /**
     * Adds the specified request to the global queue, if tag is specified
     * then it is used else Default TAG is used.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req, String tag) {
        // set the default tag if tag is empty
        req.setTag(TextUtils.isEmpty(tag) ? TAG : tag);

        VolleyLog.d("Adding request to queue: %s", req.getUrl());

        getRequestQueue().add(req);
    }

    /**
     * Adds the specified request to the global queue using the Default TAG.
     *
     * @param req
     * @param tag
     */
    public <T> void addToRequestQueue(Request<T> req) {
        // set the default tag if tag is empty
        req.setTag(TAG);

        getRequestQueue().add(req);
    }

    /**
     * Cancels all pending requests by the specified TAG, it is important
     * to specify a TAG so that the pending/ongoing requests can be cancelled.
     *
     * @param tag
     */
    public void cancelPendingRequests(Object tag) {
        if (mRequestQueue != null) {
            mRequestQueue.cancelAll(tag);
        }
    }
}
```
Changes in AndroidManifest.xml
```
<?xml version="1.0" encoding="utf-8"?>
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.examplefrappe">
    <uses-permission android:name="android.permission.USE_CREDENTIALS" />
    <uses-permission android:name="android.permission.AUTHENTICATE_ACCOUNTS" />
    <uses-permission android:name="android.permission.INTERNET" />
    <application
        android:name=".ApplicationController"
        android:allowBackup="true"
        android:icon="@mipmap/ic_launcher"
        android:label="@string/app_name"
        android:supportsRtl="true"
        android:theme="@style/AppTheme">
        <activity
            android:name=".MainActivity"
            android:exported="true">
            <intent-filter>
                <action android:name="android.intent.action.MAIN" />

                <category android:name="android.intent.category.LAUNCHER" />
            </intent-filter>
        </activity>
    </application>

</manifest>
```
Frappe Authenticator does the following - 

 - Stores OAuth 2 bearer token json (access_token/refresh_token)
 - When getAuthToken is called it checks if access_token is valid
 - If access_token has expired it renews bearer_token, stores it and returns new access_token
 - If refresh_token paired with access_token is revoked or deleted, asks user to login again or shows notification depending on the getAuthToken called.

