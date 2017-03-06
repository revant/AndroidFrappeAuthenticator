package io.frappe.frappeauthenticator.sync;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import io.frappe.frappeauthenticator.R;

public class ProfileActivity extends Activity {

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        TextView contactNameLabel = (TextView) findViewById(R.id.contactNameLabel);
        TextView contactName = (TextView) findViewById(R.id.contactName);

        TextView contactPhoneLabel = (TextView) findViewById(R.id.contactPhoneLabel);
        TextView contactPhone = (TextView) findViewById(R.id.contactPhone);

        TextView contactMobileLabel = (TextView) findViewById(R.id.contactMobileLabel);
        TextView contactMobile = (TextView) findViewById(R.id.contactMobile);

        TextView contactEmailLabel = (TextView) findViewById(R.id.contactEmailLabel);
        TextView contactEmail= (TextView) findViewById(R.id.contactEmail);

        TextView contactOrganizationLabel = (TextView) findViewById(R.id.contactOrganizationLabel);
        TextView contactOrganization = (TextView) findViewById(R.id.contactOrganization);

        Uri intentData = getIntent().getData();
        if ((intentData)!=null)
        {
            Cursor cursor = managedQuery(intentData, null, null, null, null);

            if (cursor.moveToNext())
            {
                final String displayName = cursor.getString(cursor.getColumnIndex("DATA2"));
                if (!displayName.equals("null")){
                    contactNameLabel.setVisibility(View.VISIBLE);
                    contactName.setVisibility(View.VISIBLE);
                    contactName.setText(displayName);
                }

                final String mobileNo = cursor.getString(cursor.getColumnIndex("DATA1"));
                if (!mobileNo.equals("null")){
                    contactMobileLabel.setVisibility(View.VISIBLE);
                    contactMobile.setVisibility(View.VISIBLE);
                    contactMobile.setText(mobileNo);
                    contactMobile.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", mobileNo, null));
                            startActivity(phoneIntent);
                        }
                    });
                }

                final String phone = cursor.getString(cursor.getColumnIndex("DATA4"));
                if (!phone.equals("null")){
                    contactPhoneLabel.setVisibility(View.VISIBLE);
                    contactPhone.setVisibility(View.VISIBLE);
                    contactPhone.setText(phone);
                    contactPhone.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent phoneIntent = new Intent(Intent.ACTION_DIAL, Uri.fromParts("tel", phone, null));
                            startActivity(phoneIntent);
                        }
                    });
                }

                final String emailID = cursor.getString(cursor.getColumnIndex("DATA5"));
                if (!emailID.equals("null")){
                    contactEmailLabel.setVisibility(View.VISIBLE);
                    contactEmail.setVisibility(View.VISIBLE);
                    contactEmail.setText(emailID);
                    contactEmail.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            Intent emailIntent = new Intent(Intent.ACTION_SENDTO, Uri.fromParts(
                                    "mailto", emailID , null));
                            startActivity(Intent.createChooser(emailIntent, "Send email..."));
                        }
                    });
                }

                final String customerName = cursor.getString(cursor.getColumnIndex("DATA10"));
                if (!customerName.equals("null")){
                    contactOrganizationLabel.setVisibility(View.VISIBLE);
                    contactOrganization.setVisibility(View.VISIBLE);
                    contactOrganization.setText(customerName);
                }

                final String supplierName = cursor.getString(cursor.getColumnIndex("DATA11"));
                if (!supplierName.equals("null")){
                    contactOrganizationLabel.setVisibility(View.VISIBLE);
                    contactOrganization.setVisibility(View.VISIBLE);
                    contactOrganization.setText(supplierName);
                }

                final String salesPartnerName = cursor.getString(cursor.getColumnIndex("DATA12"));
                if (!salesPartnerName.equals("null")){
                    contactOrganizationLabel.setVisibility(View.VISIBLE);
                    contactOrganization.setVisibility(View.VISIBLE);
                    contactOrganization.setText(salesPartnerName);
                }

//                String designation = cursor.getString(cursor.getColumnIndex("DATA8"));
//                if (!designation.equals("null")){
//                    contactInfo.add("Designation");
//                    contactInfo.add(designation);
//                }
//
//                String department = cursor.getString(cursor.getColumnIndex("DATA9"));
//                if (!department.equals("null")){
//                    contactInfo.add("Department");
//                    contactInfo.add(department);
//                }

//                String firstName = cursor.getString(cursor.getColumnIndex("DATA6"));
//                String lastName = cursor.getString(cursor.getColumnIndex("DATA7"));
//                String sContactName = cursor.getString(cursor.getColumnIndex("DATA3"));

            }
        }
    }
}
