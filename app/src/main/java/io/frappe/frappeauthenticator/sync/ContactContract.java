package io.frappe.frappeauthenticator.sync;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by revant on 2/3/17.
 */

public class ContactContract {
    private ContactContract() {
    }

    /**
     * Content provider authority.
     */
    public static final String CONTENT_AUTHORITY = "io.frappe.frappeauthenticator";

    /**
     * Base URI. (content://com.example.android.basicsyncadapter)
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     * Path component for "entry"-type resources..
     */
    private static final String PATH_ENTRIES = "contacts";

    /**
     * Columns supported by "entries" records.
     */
    public static class Entry implements BaseColumns {
        /**
         * MIME type for lists of entries.
         */
        public static final String CONTENT_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.frappeauthenticator.contacts";
        /**
         * MIME type for individual entries.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.frappeauthenticator.contact";

        /**
         * Fully qualified URI for "entry" resources.
         */
        public static final Uri CONTENT_URI =
                BASE_CONTENT_URI.buildUpon().appendPath(PATH_ENTRIES).build();

        /**
         * Table name where records are stored for "entry" resources.
         */

        public static final String TABLE_NAME = "contact";

        /* The name of the account instance to which this row belongs, which when paired with
         * {@link #ACCOUNT_TYPE} identifies a specific account.
         * <P>Type: TEXT</P>
         */
        public static final String ACCOUNT_NAME = "account_name";

        /**
         * The type of account to which this row belongs, which when paired with
         * {@link #ACCOUNT_NAME} identifies a specific account.
         * <P>Type: TEXT</P>
         */
        public static final String ACCOUNT_TYPE = "account_type";

        /**
         * Atom ID. (Note: Not to be confused with the database primary key, which is _ID.
         */

        public static final String MIMETYPE = "mimetype";
        public static final String COLUMN_NAME_ENTRY_ID = "contact_id";
        public static final String COLUMN_NAME_NAME = "name";
        public static final String COLUMN_NAME_CUSTOMER_NAME = "customer_name";
        public static final String COLUMN_NAME_SUPPLIER_NAME = "supplier_name";
        public static final String COLUMN_NAME_SALES_PARTNER_NAME = "sales_partner";
        public static final String COLUMN_NAME_LAST_NAME = "last_name";
        public static final String COLUMN_NAME_FIRST_NAME = "first_name";
        public static final String COLUMN_NAME_DEPARTMENT = "department";
        public static final String COLUMN_NAME_DESIGNATION = "designation";
        public static final String COLUMN_NAME_PHONE = "phone";
        public static final String COLUMN_NAME_MOBILE = "mobile_no";
        public static final String COLUMN_NAME_EMAIL = "email_id";
    }
}
