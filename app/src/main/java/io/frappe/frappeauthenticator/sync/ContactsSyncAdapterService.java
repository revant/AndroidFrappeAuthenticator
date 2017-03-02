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
                                Uri rawContactUri = ContactContract.Entry.CONTENT_URI.buildUpon().appendQueryParameter(ContactContract.Entry.ACCOUNT_NAME, account.name).appendQueryParameter(
                                        ContactContract.Entry.ACCOUNT_TYPE, account.type).build();
                                Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, ContactContract.Entry.COLUMN_NAME_NAME }, null, null, null);
                                while (c1.moveToNext()) {
                                    localContacts.put(c1.getString(1), c1.getLong(0));
                                }
                            }
                            catch (Exception e){
                                e.printStackTrace();
                            }

                            JSONArray contactsList = new JSONArray();
                            System.out.println(localContacts);
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
                                    mContentResolver.applyBatch(ContactContract.CONTENT_AUTHORITY, operationList);
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
        System.out.println("Trying TOr Create item");
        //Create our RawContact
        ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>();
        int rawContactInsertIndex = op_list.size();
        op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                .withValue(ContactContract.Entry.ACCOUNT_NAME, account.name)
                .withValue(ContactContract.Entry.ACCOUNT_TYPE, account.type)
                //.withValue(ContactsContract.RawContacts.Data.IS_READ_ONLY,"1")
                .withValue(ContactContract.Entry.COLUMN_NAME_NAME,contactName)
                //.withValue(ContactsContract.RawContacts.AGGREGATION_MODE, ContactsContract.RawContacts.AGGREGATION_MODE_DEFAULT)
                .build());

        // first and last names
        if (firstName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_FIRST_NAME, firstName)
                    .build());
        }
        if (lastName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_LAST_NAME, lastName)
                    .build());
        }

        // add phone number
        if (phone!=null) {
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_PHONE, phone)
                    .build());
        }

        //add mobile number
        if (mobileNo!=null) {
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_MOBILE, mobileNo)
                    .build());
        }

        //add email
        if (emailID!=null) {
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Email.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_EMAIL, emailID)
                    .build());
        }

        //add Customer
        if(customerName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_CUSTOMER_NAME, customerName)
                    .build());
        }

        //add Supplier
        if(supplierName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_SUPPLIER_NAME, supplierName)
                    .build());
        }

        //add Sales Partner
        if(salePartnerName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_SALES_PARTNER_NAME, salePartnerName)
                    .build());
        }

        //add Department
        if(department!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_DEPARTMENT, department)
                    .build());
        }

        //add Designation
        if(designation!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactContract.Entry.CONTENT_URI)
                    .withValueBackReference(ContactContract.Entry._ID, rawContactInsertIndex )
                    .withValue(ContactContract.Entry.MIMETYPE,ContactsContract.CommonDataKinds.Organization.CONTENT_ITEM_TYPE)
                    .withValue(ContactContract.Entry.COLUMN_NAME_DESIGNATION, designation)
                    .build());
        }

        try{
            ContentProviderResult[] results = mContentResolver.applyBatch(ContactContract.CONTENT_AUTHORITY, op_list);
        }catch(Exception e){
            e.printStackTrace();
        }
    }
}
