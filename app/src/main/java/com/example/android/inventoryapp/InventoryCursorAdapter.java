package com.example.android.inventoryapp;

import android.content.Context;
import android.database.Cursor;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;

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
        // TODO:  Fill views in list_item.xml with data from curosr
    }
}
