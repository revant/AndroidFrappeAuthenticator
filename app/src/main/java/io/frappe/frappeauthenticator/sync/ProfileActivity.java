package io.frappe.frappeauthenticator.sync;

import android.app.Activity;
import android.app.ListActivity;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import io.frappe.frappeauthenticator.R;

public class ProfileActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        ListView listView = (ListView) findViewById(R.id.profiletext);
        Uri intentData = getIntent().getData();
        if ((intentData)!=null)
        {
            Cursor cursor = managedQuery(intentData, null, null, null, null);
            System.out.println(DatabaseUtils.dumpCursorToString(cursor));
            if (cursor.moveToNext())
            {
                ArrayList<String> listItems=new ArrayList<String>();
                ArrayAdapter<String> adapter;

                String mobileNo = cursor.getString(cursor.getColumnIndex("DATA1"));
                listItems.add(mobileNo);
                String displayName = cursor.getString(cursor.getColumnIndex("DATA2")); listItems.add(displayName);
                String contactName = cursor.getString(cursor.getColumnIndex("DATA3")); listItems.add(contactName);
                String phone = cursor.getString(cursor.getColumnIndex("DATA4")); listItems.add(phone);
                String emailID = cursor.getString(cursor.getColumnIndex("DATA5")); listItems.add(emailID);
                String firstName = cursor.getString(cursor.getColumnIndex("DATA6")); listItems.add(firstName);
                String lastName = cursor.getString(cursor.getColumnIndex("DATA7")); listItems.add(lastName);
                String designation = cursor.getString(cursor.getColumnIndex("DATA8")); listItems.add(designation);
                String department = cursor.getString(cursor.getColumnIndex("DATA9")); listItems.add(department);
                String customerName = cursor.getString(cursor.getColumnIndex("DATA10")); listItems.add(customerName);
                String supplierName = cursor.getString(cursor.getColumnIndex("DATA11")); listItems.add(supplierName);
                String salesPartnerName = cursor.getString(cursor.getColumnIndex("DATA12")); listItems.add(salesPartnerName);
                adapter=new ArrayAdapter<String>(ProfileActivity.this,
                        android.R.layout.simple_list_item_1,
                        listItems);
                listView.setAdapter(adapter);
            }
        }
    }
}
