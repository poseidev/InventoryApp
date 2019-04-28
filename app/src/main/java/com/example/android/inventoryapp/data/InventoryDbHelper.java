package com.example.android.inventoryapp.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 1;
    public static final String DATABASE_NAME = "inventory.db";

    public InventoryDbHelper( Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String SQL_CREATE_PETS_TABLE = "CREATE TABLE " +
            InventoryEntry.TABLE_NAME +" ( " +
            InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
            InventoryEntry.COLUMN_PRODUCT_NAME + " TEXT NOT NULL, " +
            InventoryEntry.COLUMN_PRODUCT_DESCRIPTION + " TEXT, " +
            InventoryEntry.COLUMN_PRODUCT_PRICE + " REAL NOT NULL DEFAULT 0, " +
            InventoryEntry.COLUMN_PRODUCT_QUANTITY + " TEXT NOT NULL DEFAULT 0,"+
            InventoryEntry.COLUMN_PRODUCT_IMAGE + " BLOB)";

        db.execSQL(SQL_CREATE_PETS_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int i, int i1) {
        db.execSQL("DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME);
        onCreate(db);
    }
}
