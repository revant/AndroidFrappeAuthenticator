package io.frappe.frappeauthenticator.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.AuthenticatorException;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.OperationApplicationException;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.BaseColumns;
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

public class ContactsSyncAdapterService extends Service {
    private static final String TAG = "FrappeSync";
    private static SyncAdapterImpl sSyncAdapter = null;
    private static ContentResolver mContentResolver = null;

    public ContactsSyncAdapterService() {
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
                ContactsSyncAdapterService.performSync(mContext, account, extras, authority, provider, syncResult);
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

        Log.i("Frappe Sync: ", account.toString());
        AccountManager am = AccountManager.get(context);
        String frappeServer = am.getUserData(account, "frappeServer");
        String authToken = null;
        try {
            authToken = am.blockingGetAuthToken(account, AccountGeneral.AUTHTOKEN_TYPE_FULL_ACCESS, true);
            ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
            try {
                JSONObject bearerToken = null;
                ERPNextContactProvider server = new ERPNextContactProvider();
                try {
                    bearerToken = new JSONObject(authToken);
                    server.getContacts(frappeServer,bearerToken.getString("access_token"),new FrappeServerCallback() {
                        @Override
                        public void onSuccessJSONObject(JSONObject response) {
                            // Load the local app contacts
                            ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
                            HashMap<String, Long> localContacts = new HashMap<String, Long>();
                            try{
                                Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name).appendQueryParameter(
                                        ContactsContract.RawContacts.ACCOUNT_TYPE, account.type).build();
                                Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, ContactsContract.RawContacts.SYNC1 }, null, null, null);
                                while (c1.moveToNext()) {
                                    localContacts.put(c1.getString(1), c1.getLong(0));
                                }
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }

                            JSONArray contactsList = new JSONArray();
                            try {
                                contactsList = response.getJSONArray("data");
                                for(int i=0;i<contactsList.length();i++){
                                    JSONObject object = null;
                                    try {
                                        object = contactsList.getJSONObject(i);
                                        if (!localContacts.containsKey(object.getString("name"))) {
                                            addContact(account, object, operationList);
                                        }
                                    } catch (JSONException e) {
                                        e.printStackTrace();
                                    }
                                }
                                if(operationList.size() > 0)
                                    mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
                                System.out.println(contactsList.toString());
                            } catch (JSONException e) {
                                e.printStackTrace();
                            } catch (RemoteException e) {
                                e.printStackTrace();
                            } catch (OperationApplicationException e) {
                                e.printStackTrace();
                            }
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

    private static void addContact(Account account, JSONObject contactInfo, ArrayList<ContentProviderOperation> operationList) {
        String customerName = null;
        String supplierName = null;
        String salePartnerName = null;
        try {
            customerName = contactInfo.get("customer_name").toString();
            supplierName = contactInfo.get("supplier_name").toString();
            salePartnerName = contactInfo.get("sales_partner").toString();
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Create our RawContact
        ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>();
        op_list.add(ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                //.withValue(RawContacts.AGGREGATION_MODE, RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());

        // first and last names
        try {
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contactInfo.getString("first_name"))
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contactInfo.getString("last_name"))
                    .build());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try {
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactInfo.getString("phone"))
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.RawContacts.Data.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactInfo.getString("mobile_no"))
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)

                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, contactInfo.getString("email_id"))
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        } catch (JSONException e) {
            e.printStackTrace();
        }

        try{
            ContentProviderResult[] results = mContentResolver.applyBatch(ContactsContract.AUTHORITY, op_list);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}