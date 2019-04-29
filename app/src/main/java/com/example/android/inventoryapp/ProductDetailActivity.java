package com.example.android.inventoryapp;

import android.Manifest;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.inventoryapp.data.InventoryContract.InventoryEntry;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;

public class ProductDetailActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor>{

    private EditText mProductName;

    private EditText mProductDescription;

    private TextView mProductQuantity;

    private EditText mProductPrice;

    private EditText mProductQuantityUpdate;

    private ImageView mProductImage;

    private Uri mCurrentUri;

    private String mProductNameString;

    private final static String LOG_TAG = ProductDetailActivity.class.getSimpleName();

    private Bitmap mBitmap;

    @Override
    protected void onResume() {
        super.onResume();

        mProductImage.setImageBitmap(mBitmap);
    }

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

        mProductImage = findViewById(R.id.imageProduct);

        // Get current URI
        Intent intent = getIntent();
        mCurrentUri = intent.getData();

        // Initialize activity title
        if (mCurrentUri == null) {
            setTitle(R.string.activity_title_add_new_product);
            mProductQuantity.setText(R.string.input_value_zero);
            mProductQuantityUpdate.setText(R.string.input_value_zero);
        }
        else {
            setTitle(R.string.activity_title_update_product);

            LoaderManager.getInstance(this).initLoader(Constants.INVENTORY_LOADER, null, this);
        }

        initializeButtonsClickListeners();

        setDeleteButtonVisibility();

        setOrderButtonVisibility();
    }

    private void logMessage(String message) {
        Log.v(LOG_TAG ,message);
    }

    private void setImage(Uri uri) {
        try {
            Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);

            mBitmap = bitmap;

            mProductImage.setImageBitmap(bitmap);

        } catch (IOException e) {
            logMessage(e.getMessage());

            showMessage(e.getMessage());
        }
    }

    private void removeImage() {
        mProductImage.setImageResource(0);
    }

    private void startImageChooserActivity() {
        // If user doesn't select any image from gallery original image on the ImageView remains via onResume
        BitmapDrawable currentDrawable = (BitmapDrawable)mProductImage.getDrawable();
        mBitmap = (currentDrawable != null) ? currentDrawable.getBitmap() : null;

        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

        startActivityForResult(intent, Constants.REQUESTCODE_CHOOSE_IMAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == Constants.REQUESTCODE_CHOOSE_IMAGE) {
            if(grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                startImageChooserActivity();
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if(requestCode == Constants.REQUESTCODE_CHOOSE_IMAGE  && intent != null) {
            setImage(intent.getData());
        }
    }

    private boolean isAllowedReadExternalStorage() {

        boolean isAllowed = false;

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) !=
                PackageManager.PERMISSION_GRANTED) {

            if(ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                showMessage(getString(R.string.message_gallery_access_not_granted));
            }
            else {
                ActivityCompat.requestPermissions(
                        this,
                        new String[] {Manifest.permission.READ_EXTERNAL_STORAGE},
                        Constants.REQUESTCODE_CHOOSE_IMAGE);
            }
        }
        else {
            isAllowed = true;
        }

        return isAllowed;
    }

    private void launchImageChooser() {

        if(!isAllowedReadExternalStorage()) {
            return;
        }

        startImageChooserActivity();
    }

    private void initializeButtonsClickListeners() {
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

        Button buttonChooseImage = findViewById(R.id.buttonChooseImage);
        buttonChooseImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                launchImageChooser();
            }
        });

        Button buttonRemoveImage = findViewById(R.id.buttonRemoveImage);
        buttonRemoveImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                removeImage();
            }
        });

        // Delete button
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

                String confirmationMessage = String.format(getString(R.string.confirmation_delete_product), mProductNameString);
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
        removeImage();
    }

    /**
     * Update input quantity by a click of increase (+) or decrease (-) button
     * @param isIncrease true = increase input quantity; false = decrease
     */
    private void setProductQuantity(boolean isIncrease){
        String quantityUpdateString = mProductQuantityUpdate.getText().toString().trim();
        String currentQuantityString = mProductQuantity.getText().toString().trim();

        int currentQuantityInt = TextUtils.isEmpty(currentQuantityString) ? 0 : Integer.parseInt(currentQuantityString);
        int quantityUpdateInt = TextUtils.isEmpty(quantityUpdateString) ? 0 : Integer.parseInt(quantityUpdateString);

        int newQuantity = isIncrease ? (currentQuantityInt + quantityUpdateInt) : (currentQuantityInt - quantityUpdateInt);

        if(!isIncrease && newQuantity < 1) { newQuantity = 0; }

        mProductQuantity.setText(String.format(Locale.getDefault(), "%1$d", newQuantity));
    }

    private void showMessage(String message) {
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
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

    private ContentValues getContentValues()
    {
        String name = mProductName.getText().toString().trim();
        String description = mProductDescription.getText().toString().trim();

        String quantityString = mProductQuantity.getText().toString();
        int quantity = TextUtils.isEmpty(quantityString) ?  0 : Integer.parseInt(quantityString);

        String priceString = mProductPrice.getText().toString().trim();
        Double price = TextUtils.isEmpty(priceString) ?  0 : Double.parseDouble(priceString);

        BitmapDrawable imageDrawable = (BitmapDrawable)mProductImage.getDrawable();
        Bitmap bitmap = (imageDrawable != null) ? imageDrawable.getBitmap() : null;

        byte[] imageByteArray = null;

        if(bitmap != null) {
            ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 0, outputStream);
            imageByteArray = outputStream.toByteArray();
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_PRODUCT_NAME, name);
        values.put(InventoryEntry.COLUMN_PRODUCT_DESCRIPTION, description);
        values.put(InventoryEntry.COLUMN_PRODUCT_QUANTITY, quantity);
        values.put(InventoryEntry.COLUMN_PRODUCT_PRICE, price);
        values.put(InventoryEntry.COLUMN_PRODUCT_IMAGE, imageByteArray);

        return values;
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

        String message;

        // Save new product
        if(uri == null) {
            uri = InventoryEntry.CONTENT_URI;

            mProductNameString = values.getAsString(InventoryEntry.COLUMN_PRODUCT_NAME);

            getContentResolver().insert(uri, values);

            message = String.format(getString(R.string.message_product_saved), mProductNameString);
        }
        else {
            // Update existing product
            getContentResolver().update(uri,
                    values,
                    null,
                    null);

            message = String.format(getString(R.string.message_product_updated), mProductNameString);
        }

        showMessage(message);

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
        String confirmationMessage = String.format(Locale.getDefault(), getString(R.string.confirmation_order_product), productName);

        showConfirmationDialog(confirmationMessage, clickListener);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_PRODUCT_NAME,
                InventoryEntry.COLUMN_PRODUCT_DESCRIPTION,
                InventoryEntry.COLUMN_PRODUCT_QUANTITY,
                InventoryEntry.COLUMN_PRODUCT_PRICE,
                InventoryEntry.COLUMN_PRODUCT_IMAGE};

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
            int productImageColumnIndex = cursor.getColumnIndex(InventoryEntry.COLUMN_PRODUCT_IMAGE);

            String name = cursor.getString(nameColumnIndex);
            String description = cursor.getString(descriptionColumnIndex);
            int quantity = cursor.getInt(quantityColumnIndex);
            Double price = cursor.getDouble(priceColumnIndex);

            byte[] imageByte = cursor.getBlob(productImageColumnIndex);

            String quantityString = String.valueOf(quantity);
            quantityString = TextUtils.isEmpty(quantityString) ? "0" : quantityString;

            String priceString = String.valueOf(price);

            mProductName.setText(name);
            mProductDescription.setText(description);
            mProductQuantity.setText(quantityString);
            mProductPrice.setText(priceString);

            if(imageByte != null) {
                Bitmap image = BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length);
                mProductImage.setImageBitmap(image);
            }

            mProductNameString = name;
        }
    }

    @Override
    public void onLoaderReset( Loader<Cursor> loader) {
        clearFields();
    }
}
