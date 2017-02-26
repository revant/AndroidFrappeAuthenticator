package io.frappe.frappeauthenticator.sync;

import android.accounts.Account;
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

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by revant on 26/2/17.
 */

public class ContactsSyncAdapterService extends Service {
    private static SyncAdapterImpl sSyncAdapter = null;

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
                Log.e("tmessages", e.toString());
            }
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return getSyncAdapter().getSyncAdapterBinder();
    }

    private SyncAdapterImpl getSyncAdapter() {
        if (sSyncAdapter == null) {
            sSyncAdapter = new SyncAdapterImpl(this);
        }
        return sSyncAdapter;
    }

    private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
            throws OperationCanceledException {
        Log.d("FrappeSync", "performSync: " + account.toString());
    }
}

//public class ContactsSyncAdapterService extends Service {
//    private static final String TAG = "ContactsSyncAdapterService";
//    private static SyncAdapterImpl sSyncAdapter = null;
//    private static ContentResolver mContentResolver = null;
//
//    public ContactsSyncAdapterService() {
//        super();
//    }
//
//    private static class SyncAdapterImpl extends AbstractThreadedSyncAdapter {
//        private Context mContext;
//
//        public SyncAdapterImpl(Context context) {
//            super(context, true);
//            mContext = context;
//        }
//
//        @Override
//        public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
//            try {
//                ContactsSyncAdapterService.performSync(mContext, account, extras, authority, provider, syncResult);
//            } catch (OperationCanceledException e) {
//            }
//        }
//    }
//
//    @Override
//    public IBinder onBind(Intent intent) {
//        IBinder ret = null;
//        ret = getSyncAdapter().getSyncAdapterBinder();
//        return ret;
//    }
//
//    private SyncAdapterImpl getSyncAdapter() {
//        if (sSyncAdapter == null)
//            sSyncAdapter = new SyncAdapterImpl(this);
//        return sSyncAdapter;
//    }
//
//    private static void performSync(Context context, Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult)
//            throws OperationCanceledException {
//        mContentResolver = context.getContentResolver();
//        HashMap<String, Long> localContacts = new HashMap<String, Long>();
//
//        Log.i("Frappe Sync: ", account.toString());
//
//        // Load the local Last.fm contacts
//        Uri rawContactUri = ContactsContract.RawContacts.CONTENT_URI.buildUpon().appendQueryParameter(ContactsContract.RawContacts.ACCOUNT_NAME, account.name).appendQueryParameter(
//                ContactsContract.RawContacts.ACCOUNT_TYPE, account.type).build();
//        Cursor c1 = mContentResolver.query(rawContactUri, new String[] { BaseColumns._ID, ContactsContract.RawContacts.SYNC1 }, null, null, null);
//        while (c1.moveToNext()) {
//            localContacts.put(c1.getString(1), c1.getLong(0));
//        }
//
//        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
//        LastFmServer server = AndroidLastFmServerFactory.getServer();
//        try {
//            Friends friends = server.getFriends(account.name, "", "50");
//            for (User user : friends.getFriends()) {
//                if (!localContacts.containsKey(user.getName())) {
//                    if (user.getRealName().length() > 0)
//                        addContact(account, user.getRealName(), user.getName());
//                    else
//                        addContact(account, user.getName(), user.getName());
//                } else {
//                    Track[] tracks = server.getUserRecentTracks(user.getName(), "true", 1);
//                    if (tracks.length > 0) {
//                        updateContactStatus(operationList, localContacts.get(user.getName()), tracks[0]);
//                    }
//                }
//            }
//            if(operationList.size() > 0)
//                mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
//        } catch (Exception e1) {
//            // TODO Auto-generated catch block
//            e1.printStackTrace();
//        }
//
//    }
//
//    private static void addContact(Account account, String name, String username) {
//        Log.i("Frappe Adding contact: ", name);
//        ArrayList<ContentProviderOperation> operationList = new ArrayList<ContentProviderOperation>();
//
//        //Create our RawContact
//        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.RawContacts.CONTENT_URI);
//        builder.withValue(ContactsContract.RawContacts.ACCOUNT_NAME, account.name);
//        builder.withValue(ContactsContract.RawContacts.ACCOUNT_TYPE, account.type);
//        builder.withValue(ContactsContract.RawContacts.SYNC1, username);
//        operationList.add(builder.build());
//
//        //Create a Data record of common type 'StructuredName' for our RawContact
//        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
//        builder.withValueBackReference(ContactsContract.CommonDataKinds.StructuredName.RAW_CONTACT_ID, 0);
//        builder.withValue(ContactsContract.Data.MIMETYPE, ContactsContract.CommonDataKinds.StructuredName.CONTENT_ITEM_TYPE);
//        builder.withValue(ContactsContract.CommonDataKinds.StructuredName.DISPLAY_NAME, name);
//        operationList.add(builder.build());
//
//        //Create a Data record of custom type "vnd.android.cursor.item/vnd.fm.last.android.profile" to display a link to the Last.fm profile
//        builder = ContentProviderOperation.newInsert(ContactsContract.Data.CONTENT_URI);
//        builder.withValueBackReference(ContactsContract.Data.RAW_CONTACT_ID, 0);
//        builder.withValue(ContactsContract.Data.MIMETYPE, "vnd.android.cursor.item/vnd.fm.last.android.profile");
//        builder.withValue(ContactsContract.Data.DATA1, username);
//        builder.withValue(ContactsContract.Data.DATA2, "Last.fm Profile");
//        builder.withValue(ContactsContract.Data.DATA3, "View profile");
//        operationList.add(builder.build());
//
//        try {
//            mContentResolver.applyBatch(ContactsContract.AUTHORITY, operationList);
//        } catch (Exception e) {
//            Log.e("Frappe Sync Error", e.toString());
//            e.printStackTrace();
//        }
//    }
//
//    private static void updateContact(ArrayList<ContentProviderOperation> operationList, long rawContactId, JSONObject contactInfo) {
//        Uri rawContactUri = ContentUris.withAppendedId(ContactsContract.RawContacts.CONTENT_URI, rawContactId);
//        Uri entityUri = Uri.withAppendedPath(rawContactUri, ContactsContract.RawContacts.Entity.CONTENT_DIRECTORY);
//        Cursor c = mContentResolver.query(entityUri, new String[] { ContactsContract.RawContacts.SOURCE_ID, ContactsContract.RawContacts.Entity.DATA_ID, ContactsContract.RawContacts.Entity.MIMETYPE, ContactsContract.RawContacts.Entity.DATA1 }, null, null, null);
////        try {
////            while (c.moveToNext()) {
////                if (!c.isNull(1)) {
////                    String mimeType = c.getString(2);
////                    String status = "";
////                    if (track.getNowPlaying() != null && track.getNowPlaying().equals("true"))
////                        status = "Listening to " + track.getName() + " by " + track.getArtist();
////                    else
////                        status = "Listened to " + track.getName() + " by " + track.getArtist();
////
////                    if (mimeType.equals("vnd.android.cursor.item/vnd.fm.last.android.profile")) {
////                        ContentProviderOperation.Builder builder = ContentProviderOperation.newInsert(ContactsContract.StatusUpdates.CONTENT_URI);
////                        builder.withValue(ContactsContract.StatusUpdates.DATA_ID, c.getLong(1));
////                        builder.withValue(ContactsContract.StatusUpdates.STATUS, status);
////                        builder.withValue(ContactsContract.StatusUpdates.STATUS_RES_PACKAGE, "fm.last.android");
////                        builder.withValue(ContactsContract.StatusUpdates.STATUS_LABEL, R.string.app_name);
////                        builder.withValue(ContactsContract.StatusUpdates.STATUS_ICON, R.drawable.icon);
////                        if (track.getDate() != null) {
////                            long date = Long.parseLong(track.getDate()) * 1000;
////                            builder.withValue(ContactsContract.StatusUpdates.STATUS_TIMESTAMP, date);
////                        }
////                        operationList.add(builder.build());
////
////                        builder = ContentProviderOperation.newUpdate(ContactsContract.Data.CONTENT_URI);
////                        builder.withSelection(BaseColumns._ID + " = '" + c.getLong(1) + "'", null);
////                        builder.withValue(ContactsContract.Data.DATA3, status);
////                        operationList.add(builder.build());
////                    }
////                }
////            }
////        } finally {
////            c.close();
////        }
//    }
//}