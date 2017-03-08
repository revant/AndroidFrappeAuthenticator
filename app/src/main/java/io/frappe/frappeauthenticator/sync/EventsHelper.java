package io.frappe.frappeauthenticator.sync;

/**
 * Created by revant on 1/3/17.
 */

import android.accounts.Account;
import android.content.ContentProviderOperation;
import android.content.ContentProviderResult;
import android.content.ContentResolver;
import android.content.OperationApplicationException;
import android.net.Uri;
import android.os.RemoteException;
import android.provider.CalendarContract;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class EventsHelper {

    public static void addEvent(Account account, JSONObject contactInfo, ContentResolver mContentResolver) {

        // "event_type", "all_day", "subject", "description", "name", "starts_on", "ends_on"
        String eventType = null;
        String allDay = null;
        String subject = null;
        String description = null;
        String eventName = null;
        String startsOn = null;
        String endsOn = null;

        try {
            eventType = contactInfo.getString("event_type");
            allDay = contactInfo.getString("all_day");
            subject = contactInfo.getString("subject");
            description = contactInfo.getString("description");
            eventName = contactInfo.getString("name");
            startsOn = contactInfo.getString("starts_on");
            endsOn = contactInfo.getString("ends_on");
        } catch (JSONException e) {
            e.printStackTrace();
        }

        //Create our Event
        ArrayList<ContentProviderOperation> op_list = new ArrayList<ContentProviderOperation>();
        op_list.add(ContentProviderOperation.newInsert(addCallerIsSyncAdapterParameter(
                CalendarContract.Calendars.CONTENT_URI, true))
                .withValue(CalendarContract.Calendars.ACCOUNT_TYPE, account.type)
                .withValue(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .withValue(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL,CalendarContract.Calendars.CAL_ACCESS_READ)
                .build());

        // Event Data

           op_list.add(ContentProviderOperation.newInsert(CalendarContract.Events.CONTENT_URI)
                    .withValueBackReference(CalendarContract.Events.CALENDAR_ID, 0)
                    .withValue(CalendarContract.Events.SYNC_DATA1,eventName)
                    .withValue(CalendarContract.EXTRA_EVENT_BEGIN_TIME, startsOn)
                    .withValue(CalendarContract.EXTRA_EVENT_END_TIME, endsOn)
                    .withValue(CalendarContract.EXTRA_EVENT_ALL_DAY, allDay)
                    .withValue(CalendarContract.Events.TITLE, subject)
                    .withValue(CalendarContract.Events.DESCRIPTION, description)
                    .build());

        try{
            ContentProviderResult[] results = mContentResolver.applyBatch(CalendarContract.AUTHORITY, op_list);
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public static void updateEvent(Account account, JSONObject contactInfo, ContentResolver mContentResolver) {

    }

    public void deleteEvent(Account account, String name, ContentResolver mContentResolver) {

        String where = CalendarContract.Events.SYNC_DATA1 + " = ? AND " + CalendarContract.Events.ACCOUNT_TYPE + " = ? AND " + CalendarContract.Events.ACCOUNT_NAME + " = ? ";
        String[] params = new String[] {name, account.type, account.name};

        ArrayList<ContentProviderOperation> ops = new ArrayList<ContentProviderOperation>();
        ops.add(ContentProviderOperation.newDelete(addCallerIsSyncAdapterParameter(
                CalendarContract.Events.CONTENT_URI, true))
                .withSelection(where, params)
                .build());
        try {
            mContentResolver.applyBatch(CalendarContract.AUTHORITY, ops);
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
                    .appendQueryParameter(CalendarContract.CALLER_IS_SYNCADAPTER,
                            "true").build();
        }
        return uri;
    }

}
