package io.frappe.frappeauthenticator.sync;

import android.app.Activity;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.widget.TextView;

import io.frappe.frappeauthenticator.R;

public class ProfileActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
//        Uri intentData = getIntent().getData();
//        Cursor cursor = managedQuery(intentData, null, null, null, null);
//        if (cursor.moveToNext())
//        {
//            String username = cursor.getString(cursor.getColumnIndex("DATA1"));
//            String number = cursor.getString(cursor.getColumnIndex("DATA2"));
//            TextView view = (TextView) findViewById(R.id.profiletext);
//            view.setText("Name : "+username+"\n"+"Phone : "+number);
//        }
    }
}
