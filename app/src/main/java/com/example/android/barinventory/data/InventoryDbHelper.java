package com.example.android.barinventory.data;

/**
 * Created by toddskinner on 12/17/16.
 */

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import com.example.android.barinventory.data.InventoryContract.InventoryEntry;

public class InventoryDbHelper extends SQLiteOpenHelper {
    public static final int DATABASE_VERSION = 4;
    public static final String DATABASE_NAME = "barinventory.db";
    private static final String SQL_CREATE_ENTRIES =
            "CREATE TABLE " + InventoryEntry.TABLE_NAME + " (" +
                    InventoryEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                    InventoryEntry.COLUMN_ITEM_NAME + " TEXT," +
                    InventoryEntry.COLUMN_ITEM_CATEGORY + " INTEGER," +
                    InventoryEntry.COLUMN_ITEM_QUANTITY + " INTEGER," +
                    InventoryEntry.COLUMN_ITEM_PRICE + " INTEGER," +
                    InventoryEntry.COLUMN_ITEM_PHONE + " TEXT," +
                    InventoryEntry.COLUMN_ITEM_PHOTO + " TEXT" + ")";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + InventoryEntry.TABLE_NAME;

    public InventoryDbHelper(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    public void onCreate(SQLiteDatabase db){
        db.execSQL(SQL_CREATE_ENTRIES);
    }

    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion){
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion){
        onUpgrade(db, oldVersion, newVersion);
    }

}
