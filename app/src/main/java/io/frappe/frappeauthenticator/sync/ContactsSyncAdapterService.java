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
import android.content.ContentUris;
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
import android.provider.ContactsContract;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import io.frappe.frappeauthenticator.R;
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
                            String selection = "raw_contact_id IN (SELECT data._id from data)";
                            String[] selectionArgs = new String[]{"value1", "value2"};
                            Cursor phones = mContentResolver.query(
                                    ContactsContract.Data.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name).appendQueryParameter(
                                            ContactsContract.RawContacts.ACCOUNT_TYPE, account.type).build(),
                                    null, //columns
                                    null, //selection
                                    null, //selectionArgs
                                    null); //Order by
                            phones.moveToFirst();
                            String[] tempFields = new String[] {
                                    ContactsContract.CommonDataKinds.GroupMembership.GROUP_ROW_ID, ContactsContract.CommonDataKinds.GroupMembership.GROUP_SOURCE_ID};
                            Cursor tempCur = mContentResolver.query(ContactsContract.Data.CONTENT_URI, tempFields,
                                    ContactsContract.Contacts.Data.MIMETYPE + "='" + ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE + "'",
                                    null, null);
                            System.out.println(DatabaseUtils.dumpCursorToString(phones));
                            phones.close();
                            JSONArray contactsList = new JSONArray();
                            try {
                                contactsList = response.getJSONArray("data");
                                for(int i=0;i<contactsList.length();i++){
                                    JSONObject object = null;
                                    try {
                                        object = contactsList.getJSONObject(i);
                                        if (!localContacts.containsKey(object.getString("name"))) {
                                            addContact(account, object);
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

    private static void addContact(Account account, JSONObject contactInfo) {

        String contactName = null;
        String supplierName = null;
        String customerName = null;
        String salePartnerName = null;
        String lastName = null;
        String emailID = null;
        String mobileNo = null;
        String firstName = null;
        String department = null;
        String designation = null;
        String phone = null;

        try {
            contactName = contactInfo.getString("name");
            customerName = contactInfo.getString("customer_name");
            supplierName = contactInfo.getString("supplier_name");
            salePartnerName = contactInfo.getString("sales_partner");
            lastName = contactInfo.getString("last_name");
            emailID = contactInfo.getString("email_id");
            mobileNo = contactInfo.getString("mobile_no");
            firstName = contactInfo.getString("first_name");
            department = contactInfo.getString("department");
            designation = contactInfo.getString("designation");
            phone = contactInfo.getString("phone");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Create our RawContact
        ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>();
        op_list.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(
                ContactsContract.RawContacts.CONTENT_URI, true))
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type)
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name)
                .withValue(ContactsContract.RawContacts.RAW_CONTACT_IS_READ_ONLY,"1")
                .withValue(ContactsContract.RawContacts.SYNC1,contactName)
                .withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());

        // this is for display name
        op_list.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Settings.CONTENT_URI, true))
                .withValue(ContactsContract.RawContacts.ACCOUNT_NAME, AccountGeneral.ACCOUNT_NAME)
                .withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, AccountGeneral.ACCOUNT_TYPE)
                .withValue(ContactsContract.Settings.UNGROUPED_VISIBLE, 1)
                .build());

        // first and last names
        if (firstName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.GIVEN_NAME, firstName)
                    .build());
        }
        if (lastName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.FAMILY_NAME, lastName)
                    .build());
        }

        // add phone number
        if (phone!=null) {
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_WORK)
                    .build());
        }

        //add mobile number
        if (mobileNo!=null) {
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Phone.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Phone.NUMBER, mobileNo)
                    .withValue(ContactsContract.CommonDataKinds.Phone.TYPE, ContactsContract.CommonDataKinds.Phone.TYPE_MOBILE)
                    .build());
        }

        //add email
        if (emailID!=null) {
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Email.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Email.DATA, emailID)
                    .withValue(ContactsContract.CommonDataKinds.Email.TYPE, ContactsContract.CommonDataKinds.Email.TYPE_WORK)
                    .build());
        }

        //add Customer
        if(customerName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, customerName)
                    .build());
        }

        //add Supplier
        if(supplierName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, supplierName)
                    .build());
        }

        //add Sales Partner
        if(salePartnerName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.COMPANY, salePartnerName)
                    .build());
        }

        //add Department
        if(department!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.DEPARTMENT, department)
                    .build());
        }

        //add Designation
        if(designation!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.Data.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.Organization.TITLE, designation)
                    .build());
        }

        op_list.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(ContactsContract.Data.CONTENT_URI, true))
                .withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0)
                .withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.io.frappe.frappeauthenticator.contact")
                .withValue(ContactsContract.Data.DATA1, firstName)
                .withValue(ContactsContract.Data.DATA2, lastName)
                .withValue(ContactsContract.Data.DATA3, phone)
                .withValue(ContactsContract.Data.DATA4, mobileNo)
                .withValue(ContactsContract.Data.DATA5, emailID)
                .withValue(ContactsContract.Data.DATA6, contactName)
                .build());

//        op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
//                .withValueBackReference(ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID, 0)
//                .withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.GroupMembership.CONTENT_ITEM_TYPE)
//                .withValue(ContactsContract.CommonDataKinds.GroupMembership.GROUP_SOURCE_ID, 6)
//        .build());

//        op_list.add(ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI)
//                .withValueBackReference(ContactsContract.CommonDataKinds.Organization.RAW_CONTACT_ID, 0)
//                .withValue(ContactsContract.StatusUpdates.PROTOCOL, "io.frappe.frappeauthenticator")
//                .withValue(ContactsContract.StatusUpdates.IM_HANDLE, contactName)
//                .withValue(ContactsContract.StatusUpdates.STATUS_RES_PACKAGE, "io.frappe.frappeauthenticator")
//                .withValue(ContactsContract.StatusUpdates.STATUS_LABEL, R.string.app_name)
//                .withValue(ContactsContract.StatusUpdates.STATUS_ICON, R.drawable.frappe)
//        .build());

        try{
            ContentProviderResult[] results = mContentResolver.applyBatch(ContactsContract.AUTHORITY, op_list);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    private static Uri addCallerIsSyncAdapterParameter(Uri uri, boolean isSyncOperation) {
        if (isSyncOperation) {
            return uri.buildUpon()
                    .appendQueryParameter(ContactsContract.CALLER_IS_SYNCADAPTER,
                            "true").build();
        }
        return uri;
    }
}