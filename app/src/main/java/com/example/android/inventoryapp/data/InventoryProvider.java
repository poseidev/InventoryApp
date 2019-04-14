package com.example.android.inventoryapp.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.content.res.Resources;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.inventoryapp.R;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryProvider extends ContentProvider {

    private InventoryDbHelper mDbHelper;

    private final static int ITEMS = 100;

    private final static int ITEM_ID = 101;

    private final static String LOG_TAG = InventoryProvider.class.getSimpleName();

    private final static UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // URI patterns needed to be recognized by this provider are saved
    static {
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryEntry.TABLE_NAME, ITEMS);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, InventoryEntry.TABLE_NAME + "/#", ITEM_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());

        return true;
    }

    /**
     * @param resourceId
     * @return The string mapped to the given resource Id
     */
    private String getResourceString(Integer resourceId) {
        return Resources.getSystem().getString(resourceId);
    }

    private void throwIllegalArgumentException(String message) {
        throw new IllegalArgumentException(message);
    }

    private void logMessage(String message) {
        Log.e(LOG_TAG, message);
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        SQLiteDatabase db = mDbHelper.getReadableDatabase();
        
        Cursor cursor = null;

        int uriPatternId = sUriMatcher.match(uri);

        switch(uriPatternId) {

            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // no break here
                // proceeds to execute the query command in the case below

            case ITEMS:
                cursor = db.query(
                        InventoryEntry.TABLE_NAME,
                        projection,
                        selection,
                        selectionArgs,
                        null,
                        null,
                        sortOrder
                );

                break;
                
            default:
                throwIllegalArgumentException(getResourceString(R.string.exception_query_not_supported) + uri + ".");
        }

        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        String type = null;

        int uriPatternId = sUriMatcher.match(uri);

        switch (uriPatternId) {
            case ITEM_ID:
                type = InventoryEntry.CONTENT_ITEM_TYPE;
                break;

            case ITEMS:
                type = InventoryEntry.CONTENT_LIST_TYPE;
                break;

            default:
                throwIllegalArgumentException(
                        getResourceString(R.string.exception_unknown_uri_type) +
                        uri +
                        getResourceString(R.string.exception_with_match) +
                        uriPatternId + ".");
        }

        return type;
    }

    private Uri insertItem(Uri uri, ContentValues values) {
        // Item name cannot be null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if(name == null) {
            throwIllegalArgumentException(getResourceString(R.string.error_item_name_required));
        }

        // Price cannot be null
        Double price = values.getAsDouble(InventoryEntry.COLUMN_ITEM_PRICE);
        if(price == null) {
            throwIllegalArgumentException(getResourceString(R.string.error_item_price_required));
        }

        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        if(quantity == null) {
            throwIllegalArgumentException(getResourceString(R.string.error_item_quantity_required));
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        long id = db.insert(
                InventoryEntry.TABLE_NAME,
                null,
                values);

        if(id < 1) {
            logMessage(getResourceString(R.string.error_insert_failed) + uri);

            return null;
        }

        return ContentUris.withAppendedId(uri, id);
    }

    private int deleteItems(String selection, String[] selectionArgs) {
        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        int deletedRowsCount = db.delete(InventoryEntry.TABLE_NAME,
                            selection,
                            selectionArgs);

        return deletedRowsCount;
    }

    private int updateItems(ContentValues values, String selection, String[] selectionArgs){
        int updatedRowsCount;

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        updatedRowsCount = db.update(InventoryEntry.TABLE_NAME,
                values,
                selection,
                selectionArgs);

        return updatedRowsCount;
    }

    @Override
    public Uri insert(Uri uri, ContentValues values) {
        int uriPatternUd = sUriMatcher.match(uri);

        Uri insertUri= null;

        switch (uriPatternUd) {
            case ITEMS:
                insertUri = insertItem(uri, values);

            default:
                throwIllegalArgumentException(getResourceString(R.string.exception_insert_not_supported) + uri);
        }

        return insertUri;
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {

        int deletedRowCount = 0;

        int uriPatternId = sUriMatcher.match(uri);

        switch (uriPatternId) {
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                // no break, proceed executing delete in the case below

            case ITEMS:
                deletedRowCount = deleteItems(selection, selectionArgs);
                break;

            default:
                throwIllegalArgumentException("Delete item not supported for " + uri);
        }

        return deletedRowCount;
    }

    @Override
    public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
        int updatedRowsCount = 0;

        int uriPatternId = sUriMatcher.match(uri);

        switch(uriPatternId) {
            case ITEM_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                //no break here, execute update command in the case below

            case ITEMS:
                updatedRowsCount = updateItems(values, selection, selectionArgs);
                break;

            default:
                throwIllegalArgumentException(getResourceString(R.string.exception_update_not_supported) + uri);
        }

        return updatedRowsCount;
    }
}
