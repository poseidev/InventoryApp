package com.example.android.inventoryapp;

import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

// TODO: implement Loader
public class ProductDetailActivity extends AppCompatActivity {
    // TODO: global variables for input fields

    private static final int EXISTING_PET_LOADER = 0;

    private Uri mCurrentUri;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_detail);

        // TODO: Get Uri from intent

        // TODO: Using retrieved Uri Set activity title
            // Initialize Loader when in Edit mode
    }


    // TODO: Increment item count method

    // TODO: Decrement item count method

    // TODO: Method to display AlertDialog for delete confirmation

    // TODO: Save item method

    // TODO: Delete item method

    // TODO: Order item method (e-mail, call, etc.)

    // TODO: Override Loader methods

}
