package com.example.android.barinventory.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.util.Log;

import com.example.android.barinventory.data.InventoryContract.InventoryEntry;

import static com.example.android.barinventory.data.InventoryContract.PATH_INVENTORY;

/**
 * Created by toddskinner on 12/17/16.
 */

public class InventoryProvider extends ContentProvider {

    //Database helper object
    private InventoryDbHelper mDbHelper;
    public static final String LOG_TAG = InventoryProvider.class.getSimpleName();

    // URI matcher code for the content URI for the inventory table */
    private static final int INVENTORY = 100;

    // URI matcher code for the content URI for a single inventory item in the inventory table */
    private static final int INVENTORY_ID = 101;

    // UriMatcher object to match a content URI to a corresponding code.
    private static final UriMatcher sUriMatcher = new UriMatcher(UriMatcher.NO_MATCH);

    // Static initializer. This is run the first time anything is called from this class.
    static {
        // The calls to addURI() go here, for all of the content URI patterns that the provider
        // should recognize. All paths added to the UriMatcher have a corresponding code to return
        // when a match is found.

        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, PATH_INVENTORY, INVENTORY);
        sUriMatcher.addURI(InventoryContract.CONTENT_AUTHORITY, PATH_INVENTORY + "/#", INVENTORY_ID);
    }

    @Override
    public boolean onCreate() {
        mDbHelper = new InventoryDbHelper(getContext());
        return true;
    }

    @Override
    public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs, String sortOrder) {
        //get readable database
        SQLiteDatabase database = mDbHelper.getReadableDatabase();
        //This cursor will hold the result of the query
        Cursor cursor;
        //figure out if the URI matcher can match the URI to a specific code
        int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            case INVENTORY_ID:
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[]{String.valueOf(ContentUris.parseId(uri))};
                cursor = database.query(InventoryEntry.TABLE_NAME, projection, selection, selectionArgs, null, null, sortOrder);
                break;
            default:
                throw new IllegalArgumentException("Cannot query unknown URI " + uri);
        }

        cursor.setNotificationUri(getContext().getContentResolver(), uri);
        return cursor;
    }

    @Override
    public String getType(Uri uri) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return InventoryEntry.CONTENT_LIST_TYPE;
            case INVENTORY_ID:
                return InventoryEntry.CONTENT_ITEM_TYPE;
            default:
                throw new IllegalStateException("Unknown URI " + uri + " with match " + match);
        }
    }

    @Override
    public Uri insert(Uri uri, ContentValues contentValues) {
        final int match = sUriMatcher.match(uri);
        switch (match){
            case INVENTORY:
                return insertInventoryItem(uri, contentValues);
            default:
                throw new IllegalArgumentException("Insertion not supported for " + uri);
        }
    }

    private Uri insertInventoryItem(Uri uri, ContentValues values){
        // Check that the name is not null
        String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
        if (name == null) {
            throw new IllegalArgumentException("Item requires a name");
        }

        Integer category = values.getAsInteger(InventoryEntry.COLUMN_ITEM_CATEGORY);
        if (category == null || !InventoryEntry.isValidCategory(category)) {
            throw new IllegalArgumentException("Item requires a valid category");
        }

        // If the quantity is provided, check that it's greater than or equal to 0
        Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
        if (quantity == null || quantity < 0){
            throw new IllegalArgumentException("Item quantity can not be null or less than 0");
        }

        // If the price is provided, check that it's greater than or equal to 0
        Integer price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
        if (price == null || price < 0){
            throw new IllegalArgumentException("Item price can not be null or less than 0");
        }

        // If the phone # is provided, check that it's equal to 10 digits
        String phone = values.getAsString(InventoryEntry.COLUMN_ITEM_PHONE);
        if (phone == null || phone.length() != 10){
            throw new IllegalArgumentException("Supplier contact number can not be null or less than 10 digits");
        }

        // Check that photo is provided
        String photo = values.getAsString(InventoryEntry.COLUMN_ITEM_PHOTO);
        if (photo == null){
            throw new IllegalArgumentException("Photo not provided");
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Insert the new row, returning the primary key value of the new row
        long newRowId = db.insert(InventoryEntry.TABLE_NAME, null, values);

        if (newRowId == -1) {
            Log.e(LOG_TAG, "Failed to insert row for " + uri);
            return null;
        }

        //notify all listeners that the data has changed for the inventory content URI
        getContext().getContentResolver().notifyChange(uri, null);

        return ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, newRowId);
    }

    @Override
    public int delete(Uri uri, String selection, String[] selectionArgs) {
        int rowsDeleted;
        SQLiteDatabase db = mDbHelper.getWritableDatabase();
        final int match = sUriMatcher.match(uri);
        switch (match) {
            case INVENTORY:
                // Delete all rows that match the selection and selection args
                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            case INVENTORY_ID:
                // Delete a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };

                rowsDeleted = db.delete(InventoryEntry.TABLE_NAME, selection, selectionArgs);
                if(rowsDeleted != 0){
                    getContext().getContentResolver().notifyChange(uri, null);
                }
                return rowsDeleted;
            default:
                throw new IllegalArgumentException("Deletion is not supported for " + uri);
        }
    }

    @Override
    public int update(Uri uri, ContentValues contentValues, String selection, String[] selectionArgs) {
        final int match = sUriMatcher.match(uri);
        switch(match){
            case INVENTORY:
                return updateInventory(uri, contentValues, selection, selectionArgs);
            case INVENTORY_ID:
                // Update a single row given by the ID in the URI
                selection = InventoryEntry._ID + "=?";
                selectionArgs = new String[] { String.valueOf(ContentUris.parseId(uri)) };
                return updateInventory(uri, contentValues, selection, selectionArgs);
            default:
                throw new IllegalArgumentException("Update is not supported for " + uri);
        }
    }

    private int updateInventory(Uri uri, ContentValues values, String selection, String[] selectionArgs){

        if(values.containsKey(InventoryEntry.COLUMN_ITEM_NAME)){
            String name = values.getAsString(InventoryEntry.COLUMN_ITEM_NAME);
            if(name == null){
                throw new IllegalArgumentException("Item requires a name");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_ITEM_CATEGORY)) {
            Integer category = values.getAsInteger(InventoryEntry.COLUMN_ITEM_CATEGORY);
            if(category != null || !InventoryEntry.isValidCategory(category)){
                throw new IllegalArgumentException("Item requires a valid category");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_ITEM_QUANTITY)){
            Integer quantity = values.getAsInteger(InventoryEntry.COLUMN_ITEM_QUANTITY);
            if(quantity != null || quantity < 0){
                throw new IllegalArgumentException("Item quantity can not be null or less than 0");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_ITEM_PRICE)){
            Integer price = values.getAsInteger(InventoryEntry.COLUMN_ITEM_PRICE);
            if(price != null || price < 0){
                throw new IllegalArgumentException("Item price can not be null or less than 0");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_ITEM_PHONE)){
            String phone = values.getAsString(InventoryEntry.COLUMN_ITEM_PHONE);
            if(phone != null || phone.length() != 10){
                throw new IllegalArgumentException("Supplier contact phone number can not be null or less than 10 digits");
            }
        }

        if(values.containsKey(InventoryEntry.COLUMN_ITEM_PHOTO)){
            String photo = values.getAsString(InventoryEntry.COLUMN_ITEM_PHOTO);
            if(photo == null){
                throw new IllegalArgumentException("Photo not provided");
            }
        }

        if (values.size() == 0) {
            return 0;
        }

        SQLiteDatabase db = mDbHelper.getWritableDatabase();

        // Perform the update on the database and get the number of rows affected
        int rowsUpdated = db.update(InventoryEntry.TABLE_NAME, values, selection, selectionArgs);
        if(rowsUpdated != 0){
            getContext().getContentResolver().notifyChange(uri, null);
        }
        return rowsUpdated;
    }
}
