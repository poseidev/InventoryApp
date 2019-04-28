package com.example.android.inventoryapp.data;

import android.content.ContentResolver;
import android.content.res.Resources;
import android.net.Uri;
import android.provider.BaseColumns;

import com.example.android.inventoryapp.R;

public final class InventoryContract {

    /**
     * Empty class prevents accidental instantiating.
     */
    private InventoryContract() { }

    /**
     * Name for the entire provider - like a domain name to its website
     * */
    public static final String CONTENT_AUTHORITY = "com.example.android.inventoryapp";

    /**
     * Base of all URIs which apps will use to contact the contact provider
     */
    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    /**
     *  Appended to the base content URI
     */
    public static final String PATH_INVENTORY = "inventory";

    public static final class InventoryEntry implements BaseColumns {

        public static Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" +
                        PATH_INVENTORY;

        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" +
                        CONTENT_AUTHORITY + "/" +
                        PATH_INVENTORY;

        public static final String TABLE_NAME = "inventory";

        public static final String _ID = BaseColumns._ID;

        public static final String COLUMN_PRODUCT_NAME ="name";

        public static final String COLUMN_PRODUCT_DESCRIPTION ="description";

        public static final String COLUMN_PRODUCT_QUANTITY ="quantity";

        public static final String COLUMN_PRODUCT_PRICE ="price";

        public static final String COLUMN_PRODUCT_IMAGE ="image";
    }
}
