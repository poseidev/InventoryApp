package com.example.android.inventoryapp;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mProductName;

    private EditText mProductDescription;

    private TextView mProductQuantity;

    private EditText mProductPrice;

    private EditText mProductQuantityUpdate;

    private static final int INVENTORY_LOADER = 0;

    private Uri mCurrentUri;

    private String mProductNameString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // Initialize input fields
        mProductName = findViewById(R.id.editProductName);
        mProductQuantity = findViewById(R.id.editProductQuantityDisplay);
        mProductDescription = findViewById(R.id.editProductDescription);
        mProductPrice = findViewById(R.id.editProductPrice);

        mProductQuantityUpdate = findViewById(R.id.editProductQuantity);

        // Get current URI
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        // Initialize activity title
        if (mCurrentUri == null) {
            setTitle("Add New Product");
            mProductQuantity.setText("0");
            mProductQuantityUpdate.setText("0");
        }
        else {
            setTitle("Update Product");

            LoaderManager.getInstance(this).initLoader(INVENTORY_LOADER, null, this);
        }

        initializeButtonsClickListeners();

        setDeleteButtonVisibility();

        setOrderButtonVisibility();
    }

    private void initializeButtonsClickListeners() {
        // Implement delete button click listener
        FloatingActionButton fabDeleteProduct = findViewById(R.id.fabDeleteProduct);
        fabDeleteProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                DialogInterface.OnClickListener deleteListener = new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        if(dialog != null) {
                            deleteProduct(mCurrentUri);

                            String message = String.format(getString(R.string.message_product_deleted), mProductNameString);
                            showMessage(message);

                            finish();
                        }
                    }
                };

                String confirmationMessage = getString(R.string.confirmation_delete_product) + " " + mProductNameString + "?";
                showConfirmationDialog(confirmationMessage, deleteListener);
            }
        });

        // Order button
        FloatingActionButton fabOrderButton = findViewById(R.id.fabOrderProduct);
        fabOrderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendEmailForProductOrder();
            }
        });

        // Save button
        FloatingActionButton fabSaveProduct = findViewById(R.id.fabSaveProduct);
        fabSaveProduct.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                saveProduct(mCurrentUri);
            }
        });

        // Decrease quantity button
        Button increaseQuantityButton = findViewById(R.id.buttonIncreaseQuantity);
        increaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProductQuantity(true);
            }
        });

        // Increase quantity button
        Button decreaseQuantityButton = findViewById(R.id.buttonDecreaseQuantity);
        decreaseQuantityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setProductQuantity(false);
            }
        });
    }

    private void setDeleteButtonVisibility() {

        View button = findViewById(R.id.fabDeleteProduct);

        if(mCurrentUri == null) {
            button.setVisibility(View.INVISIBLE);
        }
        else {
            button.setVisibility(View.VISIBLE);
        }
    }

    private void setOrderButtonVisibility() {

        View button = findViewById(R.id.fabOrderProduct);

        if(mCurrentUri == null) {
            button.setVisibility(View.INVISIBLE);
        }
        else {
            button.setVisibility(View.VISIBLE);
        }
    }

    private void clearFields() {
        mProductName.getText().clear();
        mProductDescription.getText().clear();
        mProductQuantity.setText("");
        mProductPrice.getText().clear();
    }

    private void setProductQuantity(boolean isIncrease){
        String quantityUpdateString = mProductQuantityUpdate.getText().toString().trim();
        String currentQuantityString = mProductQuantity.getText().toString().trim();

        int currentQuantityInt = TextUtils.isEmpty(currentQuantityString) ? 0 : Integer.parseInt(currentQuantityString);
        int quantityUpdateInt = TextUtils.isEmpty(quantityUpdateString) ? 0 : Integer.parseInt(quantityUpdateString);

        int newQuantity = isIncrease ? (currentQuantityInt + quantityUpdateInt) : (currentQuantityInt - quantityUpdateInt);

        if(!isIncrease && newQuantity < 1) { newQuantity = 0; }

        mProductQuantity.setText(Integer.toString(newQuantity));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show();
    }

    /**
     * Display dialog box that performs custom action upon click of positive button.
     *
     * @param confirmationMessage Message to display on the dialog box
     * @param clickListener Listener to attach to positive button that will perform custom action.
     */
    private void showConfirmationDialog(String confirmationMessage, DialogInterface.OnClickListener clickListener) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(confirmationMessage);
        builder.setPositiveButton(R.string.dialog_text_yes, clickListener);
        builder.setNegativeButton(R.string.dialog_text_no, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }


    // TODO: Method to display AlertDialog for order production confirmation

    private ContentValues getContentValues()
    {
        String name = mProductName.getText().toString().trim();
        String description = mProductDescription.getText().toString().trim();

        String quantityString = mProductQuantity.getText().toString();
        int quantity = TextUtils.isEmpty(quantityString) ?  0 : Integer.parseInt(quantityString);

        String priceString = mProductPrice.getText().toString().trim();
        Double price = TextUtils.isEmpty(priceString) ?  0 : Double.parseDouble(priceString);

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);

        return values;
    }

    private void throwIllegalArgumentException(String message) {
        throw new IllegalArgumentException(message);
    }

    private boolean isValidProduct(ContentValues values) {
        String productName = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);

        if(TextUtils.isEmpty(productName)) {
            return false;
        }

        return true;
    }

    /** Save new product or update existing product
     *
     * @param uri Determines the data to perform action on
     */
    private void saveProduct(Uri uri) {
        ContentValues values = getContentValues();

        boolean isValidProduct = isValidProduct(values);
        if(!isValidProduct) {
            showMessage(getString(R.string.validation_product_name_required));
            return;
        }

        // Save new product
        if(uri == null) {
            uri = InventoryEntry.CONTENT_URI;

            getContentResolver().insert(uri, values);
        }
        else {
            // Update existing product
            getContentResolver().update(uri,
                    values,
                    null,
                    null);
        }

        finish();
    }


    /** Save new product or update existing product
     *
     * @param uri Determines the data to perform delete on
     */
    private void deleteProduct(Uri uri) {
        getContentResolver().delete(uri, null, null);
    }

    /**
     * Display dialog box to confirm product order
     *
     */
    private void sendEmailForProductOrder() {
        DialogInterface.OnClickListener clickListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int i) {
                if(dialog != null) {
                    Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:"));

                    startActivity(intent);
                }
            }
        };

        String productName = mProductNameString;
        String confirmationMessage = getString(R.string.confirmation_order_product) + " " + productName + "?";

        showConfirmationDialog(confirmationMessage, clickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE};

        return new CursorLoader(this,
                mCurrentUri,
                projection,
                null,
                null,
                null);
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor cursor) {
        if (cursor == null) {

            return;
        }

        if (cursor.moveToFirst()) {
            int nameColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_NAME);
            int descriptionColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION);
            int quantityColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_QUANTITY);
            int priceColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_PRICE);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);

            String quantityString = String.valueOf(quantity);
            quantityString = TextUtils.isEmpty(quantityString) ? "0" : quantityString;

            String priceString = String.valueOf(price);

            mProductName.setText(name);
            mProductDescription.setText(description);
            mProductQuantity.setText(quantityString);
            mProductPrice.setText(priceString);

            mProductNameString = name;
        }
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader) {
        clearFields();
    }
}
