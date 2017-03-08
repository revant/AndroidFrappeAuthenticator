package io.frappe.frappeauthenticator.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
import android.provider.CalendarContract;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.frappe.frappeauthenticator.authenticator.AccountGeneral;

/**
 * Created by revant on 26/2/17.
 */

public class EventsSyncAdapterService extends Service {
    private static final String TAG = "FrappeSync";
    private static SyncAdapterImpl sSyncAdapter = null;
    private static ContentResolver mContentResolver = null;

    public EventsSyncAdapterService() {
        super();
    }

    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
        private Context mContext;

        public SyncAdapterImpl(Context context) {
            super(context, true);
            mContext = context;
        }

        @Override
        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
            try {
                EventsSyncAdapterService.performSync(mContext, account, extras, authority, provider, syncResult);
            } catch (OperationCanceledException e) {
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        IBinder ret = null;
        ret = getSyncAdapter().getSyncAdapterBinder();
        return ret;
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (sSyncAdapter == null)
            sSyncAdapter = new SyncAdapterImpl(this);
        return sSyncAdapter;
    }

    private static void performSync(Context context, final Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
            throws OperationCanceledException {
        mContentResolver = context.getContentResolver();

        AccountManager am = AccountManager.get(context);
        String frappeServer = am.getUserData(account, "frappeServer");
        String authToken = null;
        try {
            authToken = am.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
            try {
                JSONObject bearerToken = null;
                FrappeEventProvider server = new FrappeEventProvider();
                try {
                    bearerToken = new JSONObject(authToken);
                    server.getEvents(frappeServer,bearerToken.getString("access_token"),new FrappeServerCallback() {
                        @Override
                        public void onSuccessJSONObject(JSONObject response) {
                            // Load the local app calendar events
                            EventsHelper eHelper = new EventsHelper();
                            long calId = 0;
                            calId = eHelper.queryCalender(account,mContentResolver);
                            if (calId!=0) {
                                //eHelper.updateCalendar(account,mContentResolver,calId);
                                // Add events
                                JSONArray eventList = new JSONArray();
                                try {
                                    eventList = response.getJSONArray("data");
                                    for(int i=0;i<eventList.length();i++){
                                        JSONObject eventInfo = null;
                                        try {
                                            eventInfo = eventList.getJSONObject(i);
                                            HashMap<String, Long> localEvents = new HashMap<String, Long>();
                                            try{
                                                Uri eventsUri = CalendarContract.Events.CONTENT_URI.buildUpon().appendQueryParameter(CalendarContract.Events.ACCOUNT_NAME, account.name).appendQueryParameter(
                                                        CalendarContract.Events.ACCOUNT_TYPE, account.type).build();
                                                Cursor c1 = mContentResolver.query(eventsUri, new String[] { BaseColumns._ID, CalendarContract.Events.SYNC_DATA1 }, null, null, null);
                                                while (c1.moveToNext()) {
                                                    localEvents.put(c1.getString(1), c1.getLong(0));
                                                }
                                            }
                                            catch (Exception e){
                                                e.printStackTrace();
                                            }
                                            if (!localEvents.containsKey(eventInfo.getString("name"))) {
                                                eHelper.addEvent(account, mContentResolver, eventInfo, calId);
                                            }
                                        } catch (JSONException e) {
                                            e.printStackTrace();
                                        }
                                    }
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                                ;
                            } else {
                                // add Calender
                                eHelper.insertCalendar(account,mContentResolver);
                            }
                            System.out.println(response.toString());
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                }

            } catch (Exception e1) {
                // TODO Auto-generated catch block
                e1.printStackTrace();
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (AuthenticatorException e) {
            e.printStackTrace();
        }
    }
}