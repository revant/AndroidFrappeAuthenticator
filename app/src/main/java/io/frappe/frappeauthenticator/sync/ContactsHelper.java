package io.frappe.frappeauthenticator.sync;

/**
 * Created by revant on 1/3/17.
 */

import java.util.ArrayList;

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.database.Cursor;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.ContactsContract;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.PhoneLookup;
import android.provider.ContactsContract.RawContacts;

import org.json.JSONException;
import org.json.JSONObject;

import io.frappe.frappeauthenticator.authenticator.AccountGeneral;

public class ContactsHelper {

    public static void addContact(Account account, JSONObject contactInfo, ContentResolver mContentResolver) {

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
        String displayName = null;

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

        if (firstName!=null && lastName!=null){
            displayName = firstName + " " + lastName;
        }
        else if (firstName!=null && lastName.isEmpty()){
            displayName = firstName;
        }
        else if (lastName!=null && firstName.isEmpty()){
            displayName = lastName;
        }

        if (displayName!=null){
            op_list.add(ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI)
                    .withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0)
                    .withValue(ContactsContract.RawContacts.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE)
                    .withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, displayName)
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
                .withValue(ContactsContract.Data.DATA1, mobileNo)
                .withValue(ContactsContract.Data.DATA2, displayName)
                .withValue(ContactsContract.Data.DATA3, contactName)
                .withValue(ContactsContract.Data.DATA4, phone)
                .withValue(ContactsContract.Data.DATA5, emailID)
                .withValue(ContactsContract.Data.DATA6, firstName)
                .withValue(ContactsContract.Data.DATA7, lastName)
                .withValue(ContactsContract.Data.DATA8, designation)
                .withValue(ContactsContract.Data.DATA9, department)
                .withValue(ContactsContract.Data.DATA10, customerName)
                .withValue(ContactsContract.Data.DATA11, supplierName)
                .withValue(ContactsContract.Data.DATA12, salePartnerName)
                .build());

        try{
            ContentProviderResult[] results = mContentResolver.applyBatch(ContactsContract.AUTHORITY, op_list);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void updateContact(Account account, JSONObject contactInfo, ContentResolver mContentResolver) {

    }

    public void deleteContact(Account account, String name, ContentResolver mContentResolver) {

        String where = ContactsContract.RawContacts.SYNC1 + " = ? AND " + ContactsContract.RawContacts.ACCOUNT_TYPE + " = ? AND " + ContactsContract.RawContacts.ACCOUNT_NAME + " = ? ";
        String[] params = new String[] {name, account.type, account.name};

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(addCallerIsSyncAdapterParameter(
                ContactsContract.RawContacts.CONTENT_URI, true))
                .withSelection(where, params)
                .build());
        try {
            mContentResolver.applyBatch(ContactsContract.AUTHORITY, ops);
        } catch (RemoteException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (OperationApplicationException e) {
            // TODO Auto-generated catch block
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
