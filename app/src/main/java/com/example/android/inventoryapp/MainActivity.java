package com.example.android.inventoryapp;

import android.content.ContentUris;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.example.android.inventoryapp.data.InventoryContract;
import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class MainActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    InventoryCursorAdapter mAdapter;

    private static final int INVENTORY_LOADER = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Listener for floating action button
        // Open ProductDetailActivity
        FloatingActionButton fabAddProduct = findViewById(R.id.fabAddNewProduct);
        fabAddProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);
                startActivity(intent);
            }
        });

        initializeListView();

        LoaderManager.getInstance(this).initLoader(INVENTORY_LOADER, null, this);
    }

    private void setColumnLabelsVisibility(int visibility) {
        View columnLabelsLayout = findViewById(R.id.listColumnLabels);
        TextView textProductNameLabel = findViewById(R.id.textProductNameLabel);
        TextView textProductQuantityLabel = findViewById(R.id.textProductQuantityLabel);
        TextView textProductPriceLabel = findViewById(R.id.textProductPriceLabel);


        textProductNameLabel.setVisibility(visibility);
        textProductPriceLabel.setVisibility(visibility);
        textProductQuantityLabel.setVisibility(visibility);
        columnLabelsLayout.setVisibility(visibility);
    }

    private void initializeListView() {
        ListView listView = findViewById(R.id.inventoryListContainer);

        // Set empty view
        View emptyView = findViewById(R.id.emptyView);
        listView.setEmptyView(emptyView);

        // Set listview adapter
        mAdapter = new InventoryCursorAdapter(this, null);
        listView.setAdapter(mAdapter);

        // Implement listener for item click event
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long id) {
                // Go to {@link ProductDetailActivity}
                Intent intent = new Intent(MainActivity.this, ProductDetailActivity.class);

                Uri currentUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, id);

                intent.setData(currentUri);

                startActivity(intent);
            }
        });
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(
                this,
                InventoryEntry.CONTENT_URI,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        mAdapter.swapCursor(cursor);

        // Set list column labels visibility
        if(mAdapter.getCount() > 0) {
            setColumnLabelsVisibility(View.VISIBLE);
        }
        else {
            setColumnLabelsVisibility(View.INVISIBLE);
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }
}
