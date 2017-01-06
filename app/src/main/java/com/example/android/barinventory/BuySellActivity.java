package com.example.android.barinventory;

import android.Manifest;
import android.app.Activity;
import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.ParcelFileDescriptor;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.barinventory.data.InventoryContract;
import com.example.android.barinventory.data.InventoryContract.InventoryEntry;
import com.example.android.barinventory.data.InventoryDbHelper;

import java.io.FileDescriptor;
import java.io.IOException;

import static com.example.android.barinventory.data.InventoryProvider.LOG_TAG;
import static java.lang.Integer.parseInt;

/**
 * Created by toddskinner on 12/31/16.
 */

public class BuySellActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {
    private TextView mBuySellItemName;
    private TextView mBuySellItemQuantity;
    private TextView mBuySellItemCategory;
    private ImageView mBuySellItemPhoto;
    private int mBuySellItemPrice;
    private String mBuySellItemPhone;
    private String mBuySellItemPhotoString;
    private Drawable mDrawable;
    private Uri mCurrentBuySellInventoryItemUri;
    private Uri mBuySellItemPhotoUri;
    private static final int ITEM_URL_LOADER = 0;
    private boolean mInventoryHasChanged = false;
    private InventoryDbHelper mDbHelper;
    private EditText mBuyQuantityEditText;
    private EditText mSellQuantityEditText;
    private int mCategory = 0;

    private static final int REQUEST_EXTERNAL_STORAGE = 1;
    private static String[] PERMISSIONS_STORAGE = {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };
    private Bitmap mBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buy_sell);


        Intent intent = getIntent();
        mCurrentBuySellInventoryItemUri = intent.getData();
        setTitle(R.string.buy_sell_activity_title);
        getLoaderManager().initLoader(ITEM_URL_LOADER, null, this);

        // Find all relevant views that we will need to display data
        mBuySellItemName = (TextView) findViewById(R.id.buy_sell_item_name);
        mBuySellItemCategory = (TextView) findViewById(R.id.buy_sell_item_category);
        mBuySellItemQuantity = (TextView) findViewById(R.id.buy_sell_item_quantity);
        mBuySellItemPhoto = (ImageView) findViewById(R.id.buy_item_photo);

        // Find all relevant views that we will need to read user input from
        mBuyQuantityEditText = (EditText) findViewById(R.id.buy_item_quantity);
        mSellQuantityEditText = (EditText) findViewById(R.id.sell_item_quantity);

        mBuyQuantityEditText.setOnTouchListener(mTouchListener);
        mSellQuantityEditText.setOnTouchListener(mTouchListener);

        mDbHelper = new InventoryDbHelper(this);

        Button phoneOrder = (Button) findViewById(R.id.phonecall_button);
        phoneOrder.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_DIAL);
                intent.setData(Uri.parse("tel:" + mBuySellItemPhone));
                if (intent.resolveActivity(getPackageManager()) != null) {
                    startActivity(intent);
                }
            }
        });

        Button orderReceived = (Button) findViewById(R.id.received_confirmation_button);
        orderReceived.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                saveOrderReceivedItem();
            }
        });

        Button itemSold = (Button) findViewById(R.id.sold_confirmation_button);
        itemSold.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View view) {
                saveSoldItem();
            }
        });

        requestPermissions();
    };

    //Referenced the following discussion and gist to figure out how to request permissions and get a bitmap from the URI in order to
    //show the image:
    // https://discussions.udacity.com/t/im-having-some-problems-with-the-final-project-image-and-sell-button/200641/10
    // https://gist.github.com/crlsndrsjmnz/1652e9cfea1061aa67bdc2cf0d622ef9

    //Code below from the above referenced gist and discussion

    public void requestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED ||
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {

            verifyStoragePermissions(this);


        } else {
            Log.i(LOG_TAG, "Permissions granted");
        }
    }

    //Code below from the above referenced gist and discussion

    public static void verifyStoragePermissions(Activity activity) {
        int permission = ActivityCompat.checkSelfPermission(
                activity, Manifest.permission.WRITE_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    activity,
                    PERMISSIONS_STORAGE,
                    REQUEST_EXTERNAL_STORAGE
            );
        }
    }

    //Code below from the above referenced gist and discussion

    private Bitmap getBitmapFromUri(Uri uri) {
        ParcelFileDescriptor parcelFileDescriptor = null;
        try {
            parcelFileDescriptor =
                    getContentResolver().openFileDescriptor(uri, "r");
            FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
            Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
            parcelFileDescriptor.close();
            return image;
        } catch (Exception e) {
            Log.e(LOG_TAG, "Failed to load image.", e);
            return null;
        } finally {
            try {
                if (parcelFileDescriptor != null) {
                    parcelFileDescriptor.close();
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(LOG_TAG, "Error closing ParcelFile Descriptor");
            }
        }
    }

    private void saveOrderReceivedItem() {
        String nameString = mBuySellItemName.getText().toString().trim();
        int buyQuantityInteger;
        int totalQuantityInteger = 0;
        int priceInteger = mBuySellItemPrice;
        String phoneString = mBuySellItemPhone;
        String photoString = mBuySellItemPhotoString;

        if (mCurrentBuySellInventoryItemUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(mBuySellItemQuantity.getText()) && mCategory == InventoryEntry.CATEGORY_MISC) {
            return;
        }

        if (!TextUtils.isEmpty(mBuyQuantityEditText.getText())) {
            totalQuantityInteger = parseInt(mBuySellItemQuantity.getText().toString().trim());
            buyQuantityInteger = parseInt(mBuyQuantityEditText.getText().toString().trim());
            totalQuantityInteger = totalQuantityInteger + buyQuantityInteger;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_CATEGORY, mCategory);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, totalQuantityInteger);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceInteger);
        values.put(InventoryEntry.COLUMN_ITEM_PHONE, phoneString);
        values.put(InventoryEntry.COLUMN_ITEM_PHOTO, photoString);


        if (mCurrentBuySellInventoryItemUri != null) {
            int editedUri = getContentResolver().update(mCurrentBuySellInventoryItemUri, values, null, null);
            mBuyQuantityEditText.setText("");

            if (editedUri != 0) {
                Toast.makeText(this, R.string.toast_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_fail, Toast.LENGTH_SHORT).show();
            }
        } else {
            Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

            if (newUri != null) {
                Toast.makeText(this, R.string.toast_success, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(this, R.string.toast_fail, Toast.LENGTH_SHORT).show();
            }
        }
    }

    private void saveSoldItem(){
        String nameString = mBuySellItemName.getText().toString().trim();
        int sellQuantityInteger;
        int totalQuantityInteger = 0;
        int priceInteger = mBuySellItemPrice;
        String phoneString = mBuySellItemPhone;
        String photoString = mBuySellItemPhotoString;

        if(mCurrentBuySellInventoryItemUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(mBuySellItemQuantity.getText()) && mCategory == InventoryEntry.CATEGORY_MISC){
            return;
        }

        if (!TextUtils.isEmpty(mSellQuantityEditText.getText())) {
            totalQuantityInteger = parseInt(mBuySellItemQuantity.getText().toString().trim());
            sellQuantityInteger = parseInt(mSellQuantityEditText.getText().toString().trim());
            totalQuantityInteger = totalQuantityInteger - sellQuantityInteger;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_CATEGORY, mCategory);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, totalQuantityInteger);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceInteger);
        values.put(InventoryEntry.COLUMN_ITEM_PHONE, phoneString);
        values.put(InventoryEntry.COLUMN_ITEM_PHOTO, photoString);

        if(totalQuantityInteger >= 0) {

            if (mCurrentBuySellInventoryItemUri != null) {
                int editedUri = getContentResolver().update(mCurrentBuySellInventoryItemUri, values, null, null);
                mSellQuantityEditText.setText("");

                if (editedUri != 0) {
                    Toast.makeText(this, R.string.toast_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.toast_fail, Toast.LENGTH_SHORT).show();
                }
            } else {
                Uri newUri = getContentResolver().insert(InventoryEntry.CONTENT_URI, values);

                if (newUri != null) {
                    Toast.makeText(this, R.string.toast_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this, R.string.toast_fail, Toast.LENGTH_SHORT).show();
                }
            }
        } else {
            Toast.makeText(this, R.string.toast_fail_no_negative, Toast.LENGTH_SHORT).show();
            mSellQuantityEditText.setText("");
        }
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle bundle) {
        //Define a projection that specifies the columns from the table we care about
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_CATEGORY,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_PHONE,
                InventoryEntry.COLUMN_ITEM_PHOTO};

        //this loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,   // Parent activity context
                mCurrentBuySellInventoryItemUri,        // Table to query
                projection,     // Projection to return
                null,            // No selection clause
                null,            // No selection arguments
                null             // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mBuySellItemName.setText(data.getString(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_NAME)));
            mBuySellItemQuantity.setText(Integer.toString(data.getInt(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY))));
            mBuySellItemPrice = data.getInt(data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_QUANTITY));
            mBuySellItemPhone = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_PHONE));

            mBuySellItemPhotoString = data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_PHOTO));


            //Code below from the above referenced gist and discussion

            Uri uri = Uri.parse(mBuySellItemPhotoString);

            Log.d(LOG_TAG, ">>>>>>>>>>>>>>>>>> uri: " + uri);

            mBitmap = getBitmapFromUri(uri);
            mBuySellItemPhoto.setImageBitmap(mBitmap);
            Log.e("Image Uri ", mBuySellItemPhotoString);


            int categoryColumnIndex = data.getColumnIndex(InventoryContract.InventoryEntry.COLUMN_ITEM_CATEGORY);
            int category = data.getInt(categoryColumnIndex);

            switch (category) {
                case InventoryContract.InventoryEntry.CATEGORY_MISC:
                    mCategory = 0;
                    mBuySellItemCategory.setText("Misc");
                    break;
                case InventoryContract.InventoryEntry.CATEGORY_BEER:
                    mCategory = 1;
                    mBuySellItemCategory.setText("Beer");
                    break;
                case InventoryContract.InventoryEntry.CATEGORY_WINE:
                    mCategory = 2;
                    mBuySellItemCategory.setText("Wine");
                    break;
                case InventoryContract.InventoryEntry.CATEGORY_LIQUOR:
                    mCategory = 3;
                    mBuySellItemCategory.setText("Liquor");
                    break;
                case InventoryContract.InventoryEntry.CATEGORY_SODA:
                    mCategory = 4;
                    mBuySellItemCategory.setText("Soda");
                    break;
                case InventoryContract.InventoryEntry.CATEGORY_JUICE:
                    mCategory = 5;
                    mBuySellItemCategory.setText("Juice");
                    break;
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_buy_sell, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save_buy_sell:
                //exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete_buy_sell:
                showDeleteConfirmationDialog();
                return true;
            // Respond to a click on the "Up" arrow button in the app bar
            case android.R.id.home:
                // If the item hasn't changed, continue with navigating up to parent activity
                // which is the {@link InventoryActivity}.
                if(!mInventoryHasChanged){
                    NavUtils.navigateUpFromSameTask(this);
                    return true;
                }
                // Otherwise if there are unsaved changes, setup a dialog to warn the user.
                // Create a click listener to handle the user confirming that
                // changes should be discarded.
                DialogInterface.OnClickListener discardButtonClickListener =
                        new DialogInterface.OnClickListener(){
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                // User clicked "Discard" button, navigate to parent activity.
                                NavUtils.navigateUpFromSameTask(BuySellActivity.this);
                            }
                        };
                //show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mBuyQuantityEditText.setText("");
        mSellQuantityEditText.setText("");
    }

    private View.OnTouchListener mTouchListener = new View.OnTouchListener() {
        @Override
        public boolean onTouch(View view, MotionEvent motionEvent) {
            mInventoryHasChanged = true;
            return false;
        }
    };

    private void showUnsavedChangesDialog(DialogInterface.OnClickListener discardButtonClickListener) {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the positive and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.unsaved_changes_dialog_msg);
        builder.setPositiveButton(R.string.discard, discardButtonClickListener);
        builder.setNegativeButton(R.string.keep_editing, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Keep editing" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {
        // If the item hasn't changed, continue with handling back button press
        if (!mInventoryHasChanged) {
            super.onBackPressed();
            return;
        }
        // Otherwise if there are unsaved changes, setup a dialog to warn the user.
        // Create a click listener to handle the user confirming that changes should be discarded.
        DialogInterface.OnClickListener discardButtonClickListener =
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        // User clicked "Discard" button, close the current activity.
                        finish();
                    }
                };
        // Show dialog that there are unsaved changes
        showUnsavedChangesDialog(discardButtonClickListener);
    }

    private void showDeleteConfirmationDialog() {
        // Create an AlertDialog.Builder and set the message, and click listeners
        // for the postivie and negative buttons on the dialog.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage(R.string.delete_dialog_msg);
        builder.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Delete" button, so delete the item.
                deleteBuySellItem();
            }
        });
        builder.setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {
                // User clicked the "Cancel" button, so dismiss the dialog
                // and continue editing the item.
                if (dialog != null) {
                    dialog.dismiss();
                }
            }
        });

        // Create and show the AlertDialog
        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    /**
     * Perform the deletion of the item in the database.
     */
    private void deleteBuySellItem() {
        if(mCurrentBuySellInventoryItemUri != null){
            int rowsDeleted = getContentResolver().delete(mCurrentBuySellInventoryItemUri, null, null);
            if(rowsDeleted == 0){
                // If no rows were deleted, then there was an error with the delete.
                Toast.makeText(this, getString(R.string.editor_delete_item_failed),
                        Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, the delete was successful and we can display a toast.
                Toast.makeText(this, getString(R.string.editor_delete_item_successful),
                        Toast.LENGTH_SHORT).show();
            }
            finish();
        }
    }
}
