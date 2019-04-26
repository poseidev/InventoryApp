package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.util.Locale;

public class InventoryCursorAdapter extends CursorAdapter {

    /**
     * @param context   The context.
     * @param cursor    Object containing inventory data.
     */
    public InventoryCursorAdapter(Context context, Cursor cursor) {
        super(context, cursor, 0);
    }

    /**
     * Creates a new blank list item view.
     * @param context   The context
     * @param cursor    Object containing inventory data
     * @param viewGroup Container of views that the new view will be a part of.
     * @return A new list item view.
     */
    @Override
    public View newView(Context context, Cursor cursor, ViewGroup viewGroup) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, viewGroup, false);
    }

    /**
     * Binds current data pointed to by the cursor, to the list item layout.
     * @param view      The view created in newView method
     * @param context   The app context
     * @param cursor    Object containing the data
     */
    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        int productIdColumnIndex = cursor.getColumnIndex(InventoryEntry._ID);
        int productNameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
        int productQuantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
        int productPriceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);

        final int id = cursor.getInt(productIdColumnIndex);
        String name = cursor.getString(productNameColumnIndex);
        final int quantity = cursor.getInt(productQuantityColumnIndex);
        double price = cursor.getDouble(productPriceColumnIndex);

        TextView productNameTextView = view.findViewById(R.id.textProductName);
        TextView productQuantityTextView = view.findViewById(R.id.textProductQuantity);
        TextView productPriceTextView = view.findViewById(R.id.textProductPrice);

        productNameTextView.setText(name);
        productQuantityTextView.setText(String.format(Locale.getDefault(), "%1$d", quantity));
        productPriceTextView.setText(String.format(Locale.getDefault(), "%1$.2f", price));

        Button saleButton = view.findViewById(R.id.buttonSale);

        final Context contextTemp = context;

        // Sale button that decreases the quantity by 1
        saleButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int newQuantity =(quantity > 0) ? (quantity - 1) : quantity;

                ContentValues values = new ContentValues();
                values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, newQuantity);

                Uri uri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);
                contextTemp.getContentResolver().update(uri, values, null, null);

            }
        });
    }
}
