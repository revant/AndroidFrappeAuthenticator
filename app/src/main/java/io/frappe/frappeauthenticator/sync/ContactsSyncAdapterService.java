package io.frappe.frappeauthenticator.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.accounts.OperationCanceledException;
import android.app.Service;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.BaseColumns;
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

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

    private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
            throws OperationCanceledException {
        mContentResolver = context.getContentResolver();
        HashMap<String, Long> localContacts = new HashMap<String, Long>();
        Log.i("Frappe Sync: ", account.toString());

        // Load the local app contacts
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
        AccountManager am = AccountManager.get(context);
        String authToken = am.peekAuthToken(account,"Read Only");
        for(int o = 0; o < 1000; ++o){
            Log.i("authToken : ", authToken);
        }
        String frappeServer = am.getUserData(account, "frappeServer");
        JSONObject bearerToken;
        JSONArray contactsList = new JSONArray();
        ERPNextContactProvider server = new ERPNextContactProvider();
        try {
            bearerToken = new JSONObject(authToken);
            contactsList = server.getContacts(frappeServer, bearerToken.getString("access_token"));
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        for (int i=0;i < contactsList.length(); ++i){
            try {
                JSONObject rec = contactsList.getJSONObject(i);
                Log.i("CONTACT", rec.toString());
            } catch (JSONException e) {
                e.printStackTrace();
                Log.i("NOCONTACTS", "NOT FOUND");
            }
        }
        Log.i("ContactList", contactsList.toString());
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
//        try {
//            Friends friends = server.getFriends(account.name, "", "50");
//            for (User user : friends.getFriends()) {
//                if (!localContacts.containsKey(user.getName())) {
//                    if (user.getRealName().length() > 0)
//                        addContact(account, user.getRealName(), user.getName());
//                    else
//                        addContact(account, user.getName(), user.getName());
//                }
//            }
//            if(operationList.size() > 0)
//                mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
//        } catch (Exception e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }

    }

    private static void addContact(Account account, JSONObject contactInfo) {
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
        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();

        //Create our RawContact
        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name);
        builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type);
        builder.withValue(ContactsContract.RawContacts.Data.IS_READ_ONLY, "1");
        try {
            builder.withValue(ContactsContract.RawContacts.SYNC1, contactInfo.get("name").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        operationList.add(builder.build());

        //Create a Data record of common type 'StructuredName' for our RawContact
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
        try {
            builder.withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, contactInfo.get("first_name").toString());
            builder.withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, contactInfo.get("last_name").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        operationList.add(builder.build());

        //add Phone
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        try {
            builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactInfo.get("phone").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        operationList.add(builder.build());

        //add Mobile
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK);
        try {
            builder.withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, contactInfo.get("mobile_no").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        operationList.add(builder.build());

        //add Email Id
        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
        builder.withValue(ContactsContract.Data.MIMETYPE,
                ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE);
        builder.withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK);
        try {
            builder.withValue(ContactsContract.CommonDataKinds.Email.ADDRESS, contactInfo.get("email_id").toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        operationList.add(builder.build());

        //add Customer
        if(customerName!=null){
            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
            builder.withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            try {
                builder.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contactInfo.get("customer_name").toString());
                builder.withValue(ContactsContract.RawContacts.Data.DATA5, contactInfo.get("designation").toString());
                builder.withValue(ContactsContract.RawContacts.Data.DATA6, contactInfo.get("department").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            operationList.add(builder.build());
        }

        //add Supplier
        if(supplierName!=null){
            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
            builder.withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            try {
                builder.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contactInfo.get("supplier_name").toString());
                builder.withValue(ContactsContract.RawContacts.Data.DATA5, contactInfo.get("designation").toString());
                builder.withValue(ContactsContract.RawContacts.Data.DATA6, contactInfo.get("department").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            operationList.add(builder.build());
        }

        //add Sales Partner
        if(salePartnerName!=null){
            builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
            builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
            builder.withValue(ContactsContract.Data.MIMETYPE,
                    ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE);
            try {
                builder.withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, contactInfo.get("sales_partner").toString());
                builder.withValue(ContactsContract.RawContacts.Data.DATA5, contactInfo.get("designation").toString());
                builder.withValue(ContactsContract.RawContacts.Data.DATA6, contactInfo.get("department").toString());
            } catch (JSONException e) {
                e.printStackTrace();
            }
            operationList.add(builder.build());
        }

        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
        } catch (Exception e) {
            Log.e("Frappe Sync Error", e.toString());
            e.printStackTrace();
        }
    }
}