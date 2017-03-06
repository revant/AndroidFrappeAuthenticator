package io.frappe.frappeauthenticator.sync;

import android.app.Activity;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

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
            List<String> contactInfo = new ArrayList<String>();
            ArrayAdapter adapter = new ArrayAdapter<String>(ProfileActivity.this, android.R.layout.simple_list_item_1, android.R.id.text1, contactInfo);
            Cursor cursor = managedQuery(intentData, null, null, null, null);
            System.out.println(DatabaseUtils.dumpCursorToString(cursor));
            if (cursor.moveToNext())
            {
                String displayName = cursor.getString(cursor.getColumnIndex("DATA2"));
//                if (displayName!=null && !displayName.isEmpty()){
//                    contactInfo.add("Name");
//                    contactInfo.add(displayName);
//                }

                String mobileNo = cursor.getString(cursor.getColumnIndex("DATA1"));
                if (!mobileNo.equals("null")){
                    contactInfo.add("Mobile No");
                    contactInfo.add(mobileNo);
                }

                String phone = cursor.getString(cursor.getColumnIndex("DATA4"));
                if (!phone.equals("null")){
                    contactInfo.add("Phone");
                    contactInfo.add(phone);
                }

                String emailID = cursor.getString(cursor.getColumnIndex("DATA5"));
                if (!emailID.equals("null")){
                    contactInfo.add("EMail");
                    contactInfo.add(emailID);
                }

                String customerName = cursor.getString(cursor.getColumnIndex("DATA10"));
                if (!customerName.equals("null")){
                    contactInfo.add("Customer Name");
                    contactInfo.add(customerName);
                }

                String supplierName = cursor.getString(cursor.getColumnIndex("DATA11"));
                if (!supplierName.equals("null")){
                    contactInfo.add("Supplier Name");
                    contactInfo.add(supplierName);
                }

                String salesPartnerName = cursor.getString(cursor.getColumnIndex("DATA12"));
                if (!salesPartnerName.equals("null")){
                    contactInfo.add("Sales Partner Name");
                    contactInfo.add(salesPartnerName);
                }

                String designation = cursor.getString(cursor.getColumnIndex("DATA8"));
                if (!designation.equals("null")){
                    contactInfo.add("Designation");
                    contactInfo.add(designation);
                }

                String department = cursor.getString(cursor.getColumnIndex("DATA9"));
                if (!department.equals("null")){
                    contactInfo.add("Department");
                    contactInfo.add(department);
                }

                String firstName = cursor.getString(cursor.getColumnIndex("DATA6"));
                String lastName = cursor.getString(cursor.getColumnIndex("DATA7"));
                String contactName = cursor.getString(cursor.getColumnIndex("DATA3"));

                adapter=new ArrayAdapter<String>(ProfileActivity.this,
                        android.R.layout.simple_list_item_1, android.R.id.text1,
                        contactInfo);

                listView.setAdapter(adapter);
                listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                    @Override
                    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                        if (parent.getItemAtPosition(position-1) == "Phone"){
                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", parent.getItemAtPosition(position).toString(), null));
                            startActivity(phoneIntent);
                        } else if (parent.getItemAtPosition(position-1) == "Mobile No"){
                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", parent.getItemAtPosition(position).toString(), null));
                            startActivity(phoneIntent);
                        } else if (parent.getItemAtPosition(position-1) == "EMail"){
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto",parent.getItemAtPosition(position).toString(), null));
                            startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        }
                    }
                });

            }
        }
    }
}
