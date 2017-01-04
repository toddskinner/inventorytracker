package com.example.android.barinventory;

import android.app.LoaderManager;
import android.content.ContentValues;
import android.content.CursorLoader;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.android.barinventory.data.InventoryContract.InventoryEntry;
import com.example.android.barinventory.data.InventoryDbHelper;

import static java.lang.Integer.parseInt;

/**
 * Created by toddskinner on 12/21/16.
 */

public class EditorActivity extends AppCompatActivity implements LoaderManager.LoaderCallbacks<Cursor> {

    private EditText mNameEditText;
    private EditText mQuantityEditText;
    private EditText mPriceEditText;
    private EditText mPhoneEditText;
    private Spinner mCategorySpinner;
    private int mCategory = 0;
    private static final int INVENTORY_URL_LOADER = 0;
    private Uri mCurrentInventoryItemUri;
    private boolean mInventoryHasChanged = false;
    private InventoryDbHelper mDbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        //examine the intent that was used to launch this activity
        //in order to figure if we are creating a new inventory item or editing an existing one.
        Intent intent = getIntent();
        mCurrentInventoryItemUri = intent.getData();

        //if the intent DOES NOT contain a inventory item content URI, then we know that we are creating a new inventory item
        if (mCurrentInventoryItemUri == null) {
            //this is a new inventory item, so change the app bar to say "Add an Item"
            setTitle(getString(R.string.editor_activity_title_new_item));

            //invalidate the options menu, so the "Delete" menu option can be hidden
            //it doesn't make sense to delete an item that hasn't been created yet
            invalidateOptionsMenu();
        } else {
            //Otherwise this is an existing pet, so change app bar to say "Edit Pet"
            setTitle(R.string.editor_activity_title_edit_item);
            getLoaderManager().initLoader(INVENTORY_URL_LOADER, null, this);
        }

        // Find all relevant views that we will need to read user input from
        mNameEditText = (EditText) findViewById(R.id.edit_pet_name);
        mQuantityEditText = (EditText) findViewById(R.id.edit_item_quantity);
        mPriceEditText = (EditText) findViewById(R.id.edit_item_price);
        mPhoneEditText = (EditText) findViewById(R.id.edit_item_phone);
        //mPhoneEditText.addTextChangedListener(new PhoneNumberFormattingTextWatcher());
        mCategorySpinner = (Spinner) findViewById(R.id.spinner_category);

        mNameEditText.setOnTouchListener(mTouchListener);
        mQuantityEditText.setOnTouchListener(mTouchListener);
        mPriceEditText.setOnTouchListener(mTouchListener);
        mPhoneEditText.setOnTouchListener(mTouchListener);
        mCategorySpinner.setOnTouchListener(mTouchListener);

        mDbHelper = new InventoryDbHelper(this);
        setupSpinner();
    }

    /**
     * Setup the dropdown spinner that allows the user to select the category of the inventory item.
     */
    private void setupSpinner() {
        // Create adapter for spinner. The list options are from the String array it will use
        // the spinner will use the default layout
        ArrayAdapter categorySpinnerAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_category_options, android.R.layout.simple_spinner_item);

        // Specify dropdown layout style - simple list view with 1 item per line
        categorySpinnerAdapter.setDropDownViewResource(android.R.layout.simple_dropdown_item_1line);

        // Apply the adapter to the spinner
        mCategorySpinner.setAdapter(categorySpinnerAdapter);

        // Set the integer mSelected to the constant values
        mCategorySpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selection = (String) parent.getItemAtPosition(position);
                if (!TextUtils.isEmpty(selection)) {
                    if (selection.equals(getString(R.string.category_beer))) {
                        mCategory = InventoryEntry.CATEGORY_BEER;
                    } else if (selection.equals(getString(R.string.category_wine))) {
                        mCategory = InventoryEntry.CATEGORY_WINE;
                    } else if (selection.equals(getString(R.string.category_liquor))) {
                        mCategory = InventoryEntry.CATEGORY_LIQUOR;
                    } else if (selection.equals(getString(R.string.category_soda))) {
                        mCategory = InventoryEntry.CATEGORY_SODA;
                    } else if (selection.equals(getString(R.string.category_juice))) {
                        mCategory = InventoryEntry.CATEGORY_JUICE;
                    } else {
                        mCategory = InventoryEntry.CATEGORY_MISC;
                    }
                }
            }
            // Because AdapterView is an abstract class, onNothingSelected must be defined
            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                mCategory = 0; // Unknown
            }
        });
    }

    private void saveItem(){
        String nameString = mNameEditText.getText().toString().trim();
        int quantityInteger = 0;
        int priceInteger = 0;
        String phoneString = mPhoneEditText.getText().toString().trim();

        if(mCurrentInventoryItemUri == null && TextUtils.isEmpty(nameString) && TextUtils.isEmpty(mQuantityEditText.getText()) && mCategory == InventoryEntry.CATEGORY_MISC && TextUtils.isEmpty(mPhoneEditText.getText())){
            return;
        }

        if (!TextUtils.isEmpty(mQuantityEditText.getText())) {
            quantityInteger = parseInt(mQuantityEditText.getText().toString().trim());;
        }

        if (!TextUtils.isEmpty(mPriceEditText.getText())) {
            priceInteger = parseInt(mPriceEditText.getText().toString().trim());;
        }

        ContentValues values = new ContentValues();
        values.put(InventoryEntry.COLUMN_ITEM_NAME, nameString);
        values.put(InventoryEntry.COLUMN_ITEM_CATEGORY, mCategory);
        values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, quantityInteger);
        values.put(InventoryEntry.COLUMN_ITEM_PRICE, priceInteger);
        values.put(InventoryEntry.COLUMN_ITEM_PHONE, phoneString);

        if(mCurrentInventoryItemUri != null){
            int editedUri = getContentResolver().update(mCurrentInventoryItemUri, values, null, null);

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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu options from the res/menu/menu_editor.xml file.
        // This adds menu items to the app bar.
        getMenuInflater().inflate(R.menu.menu_editor, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        // If this is a new item, hide the "Delete" menu item.
        if (mCurrentInventoryItemUri == null) {
            MenuItem menuItem = menu.findItem(R.id.action_delete);
            menuItem.setVisible(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // User clicked on a menu option in the app bar overflow menu
        switch (item.getItemId()) {
            // Respond to a click on the "Save" menu option
            case R.id.action_save:
                //save item to database
                saveItem();
                //exit activity
                finish();
                return true;
            // Respond to a click on the "Delete" menu option
            case R.id.action_delete:
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
                                NavUtils.navigateUpFromSameTask(EditorActivity.this);
                            }
                        };
                //show a dialog that notifies the user they have unsaved changes
                showUnsavedChangesDialog(discardButtonClickListener);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }


    @Override
    public Loader<Cursor> onCreateLoader(int loaderID, Bundle args) {
        //Define a projection that specifies the columns from the table we care about
        String[] projection = {
                InventoryEntry._ID,
                InventoryEntry.COLUMN_ITEM_NAME,
                InventoryEntry.COLUMN_ITEM_CATEGORY,
                InventoryEntry.COLUMN_ITEM_QUANTITY,
                InventoryEntry.COLUMN_ITEM_PRICE,
                InventoryEntry.COLUMN_ITEM_PHONE};

        //this loader will execute the ContentProvider's query method on a background thread
        return new CursorLoader(
                this,   // Parent activity context
                mCurrentInventoryItemUri,        // Table to query
                projection,     // Projection to return
                null,            // No selection clause
                null,            // No selection arguments
                null             // Default sort order
        );
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        if (data.moveToFirst()) {
            mNameEditText.setText(data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_NAME)));
            mQuantityEditText.setText(Integer.toString(data.getInt(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_QUANTITY))));
            mPriceEditText.setText(Integer.toString(data.getInt(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_PRICE))));
            mPhoneEditText.setText(data.getString(data.getColumnIndex(InventoryEntry.COLUMN_ITEM_PHONE)));

            int categoryColumnIndex = data.getColumnIndex(InventoryEntry.COLUMN_ITEM_CATEGORY);
            int category = data.getInt(categoryColumnIndex);

            switch (category) {
                case InventoryEntry.CATEGORY_MISC:
                    mCategorySpinner.setSelection(0);
                    break;
                case InventoryEntry.CATEGORY_BEER:
                    mCategorySpinner.setSelection(1);
                    break;
                case InventoryEntry.CATEGORY_WINE:
                    mCategorySpinner.setSelection(2);
                    break;
                case InventoryEntry.CATEGORY_LIQUOR:
                    mCategorySpinner.setSelection(3);
                    break;
                case InventoryEntry.CATEGORY_SODA:
                    mCategorySpinner.setSelection(4);
                    break;
                case InventoryEntry.CATEGORY_JUICE:
                    mCategorySpinner.setSelection(5);
                    break;
            }
        }
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        //callback called when the data needs to be deleted
        mNameEditText.setText("");
        mQuantityEditText.setText("");
        mPriceEditText.setText("");
        mPhoneEditText.setText("");
        mCategorySpinner.setSelection(0);
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
                deleteItem();
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
    private void deleteItem() {
        if(mCurrentInventoryItemUri != null){
            int rowsDeleted = getContentResolver().delete(mCurrentInventoryItemUri, null, null);
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
