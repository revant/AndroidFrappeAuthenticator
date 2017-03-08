package io.frappe.frappeauthenticator.sync;

/**
 * Created by revant on 1/3/17.
 */

import android.accounts.Account;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.provider.CalendarContract;
import android.support.v4.database.DatabaseUtilsCompat;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

public class EventsHelper {

    public static final String[] EVENT_PROJECTION = new String[] {
            CalendarContract.Calendars._ID,                           // 0
            CalendarContract.Calendars.ACCOUNT_NAME,                  // 1
            CalendarContract.Calendars.CALENDAR_DISPLAY_NAME,         // 2
            CalendarContract.Calendars.OWNER_ACCOUNT                  // 3
    };

    // The indices for the projection array above.
    private static final int PROJECTION_ID_INDEX = 0;
    private static final int PROJECTION_ACCOUNT_NAME_INDEX = 1;
    private static final int PROJECTION_DISPLAY_NAME_INDEX = 2;
    private static final int PROJECTION_OWNER_ACCOUNT_INDEX = 3;

    static public long queryCalender(Account account, ContentResolver mContentResolver){
        Uri uri = CalendarContract.Calendars.CONTENT_URI;
        String selection = "((" + CalendarContract.Calendars.ACCOUNT_NAME + " = ?) AND ("
                + CalendarContract.Calendars.ACCOUNT_TYPE + " = ?))";
        String[] selectionArgs = new String[] {account.name, account.type};
        // Submit the query and get a Cursor object back.
        Cursor cr = mContentResolver.query(uri, EVENT_PROJECTION, selection, selectionArgs, null);
        long calID = 0;
        while (cr.moveToNext()) {
            calID = cr.getLong(PROJECTION_ID_INDEX);
        }
        cr.moveToFirst();
        DatabaseUtils.dumpCursorToString(cr);
        return calID;
    }

    static public void insertCalendar(Account account, ContentResolver mContentResolver){
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Calendars.ACCOUNT_NAME, account.name);
        values.put(CalendarContract.Calendars.ACCOUNT_TYPE, account.type);
        values.put(CalendarContract.Calendars.NAME, account.name);
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, account.name);
        values.put(CalendarContract.Calendars.CALENDAR_COLOR, 0xEA8561);
        //user can only read the calendar
        values.put(CalendarContract.Calendars.CALENDAR_ACCESS_LEVEL, CalendarContract.Calendars.CAL_ACCESS_READ);
        values.put(CalendarContract.Calendars.OWNER_ACCOUNT, account.name);
        values.put(CalendarContract.Calendars.VISIBLE, 1);
        values.put(CalendarContract.Calendars.SYNC_EVENTS, 1);
        Uri updateUri = asSyncAdapter(CalendarContract.Calendars.CONTENT_URI, account);
        mContentResolver.insert(updateUri, values);
    }

    static public void updateCalendar(Account account, ContentResolver mContentResolver, long calID){
        ContentValues values = new ContentValues();
        // The new display name for the calendar
        values.put(CalendarContract.Calendars.CALENDAR_DISPLAY_NAME, account.name);
        Uri updateUri = ContentUris.withAppendedId(asSyncAdapter(CalendarContract.Calendars.CONTENT_URI, account), calID);
        int rows = mContentResolver.update(updateUri, values, null, null);
        Log.i("updateCal", "Rows updated: " + rows);

    }

    static public void addEvent(Account account, ContentResolver mContentResolver, JSONObject eventInfo, long calID){
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
        ContentValues values = new ContentValues();
        values.put(CalendarContract.Events.CALENDAR_ID, calID);
        try {
            Calendar beginTime = Calendar.getInstance();
            Calendar endTime = Calendar.getInstance();
            try {
                beginTime.setTime(sdf.parse(eventInfo.getString("starts_on")));
                endTime.setTime(sdf.parse(eventInfo.getString("ends_on")));
                long startMillis = beginTime.getTimeInMillis();
                long endMillis = endTime.getTimeInMillis();
                values.put(CalendarContract.Events.DTSTART, startMillis);
                values.put(CalendarContract.Events.DTEND, endMillis);
            } catch (ParseException e) {
                e.printStackTrace();
            }
            values.put(CalendarContract.Events.SYNC_DATA1, eventInfo.getString("name"));
            values.put(CalendarContract.Events.TITLE, eventInfo.getString("subject"));
            values.put(CalendarContract.Events.DESCRIPTION, eventInfo.getString("description"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Uri uri = mContentResolver.insert(asSyncAdapter(CalendarContract.Events.CONTENT_URI, account), values);
    }

    static public void deleteEvent(Account account, ContentResolver mContentResolver, long eventID){
        ContentValues values = new ContentValues();
        Uri deleteUri = ContentUris.withAppendedId(asSyncAdapter(CalendarContract.Events.CONTENT_URI, account), eventID);
        int rows = mContentResolver.delete(deleteUri, null, null);
        Log.i("deleteEvent", "Rows deleted: " + rows);
    }

    static public Uri asSyncAdapter(Uri uri, Account account) {
        return uri.buildUpon()
                .appendQueryParameter(android.provider.CalendarContract.CALLER_IS_SYNCADAPTER,"true")
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_NAME, account.name)
                .appendQueryParameter(CalendarContract.Calendars.ACCOUNT_TYPE, account.type).build();
    }

}
