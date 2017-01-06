package com.example.android.barinventory.data;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Created by toddskinner on 12/16/16.
 */

public class InventoryContract {

    public static final String CONTENT_AUTHORITY = "com.example.android.barinventory";

    public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);

    public static final String PATH_INVENTORY = "barinventory";

    public static abstract class InventoryEntry implements BaseColumns {

        /**
         * The MIME type of the {@link #CONTENT_URI} for a list of inventory items.
         * You’ll notice that we’re making use of the constants defined in the ContentResolver class:
         * CURSOR_DIR_BASE_TYPE (which maps to the constant "vnd.android.cursor.dir") and CURSOR_ITEM_BASE_TYPE (which
         * maps to the constant “vnd.android.cursor.item”).
         */

        public static final String CONTENT_LIST_TYPE =
                ContentResolver.CURSOR_DIR_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        /**
         * The MIME type of the {@link #CONTENT_URI} for a single inventory item.
         */
        public static final String CONTENT_ITEM_TYPE =
                ContentResolver.CURSOR_ITEM_BASE_TYPE + "/" + CONTENT_AUTHORITY + "/" + PATH_INVENTORY;

        //create a full URI for the class as a constant called CONTENT_URI
        public static final Uri CONTENT_URI = Uri.withAppendedPath(BASE_CONTENT_URI, PATH_INVENTORY);

        public static final String TABLE_NAME = "inventory";
        public static final String _ID = BaseColumns._ID;
        public static final String COLUMN_ITEM_NAME = "name";
        public static final String COLUMN_ITEM_CATEGORY = "category";
        public static final String COLUMN_ITEM_QUANTITY = "quantity";
        public static final String COLUMN_ITEM_PRICE = "price";
        public static final String COLUMN_ITEM_PHONE = "phone";
        public static final String COLUMN_ITEM_PHOTO = "photo";

        /**
         * Possible values for the category of the item.
         */
        public static final int CATEGORY_MISC = 0;
        public static final int CATEGORY_BEER = 1;
        public static final int CATEGORY_WINE = 2;
        public static final int CATEGORY_LIQUOR = 3;
        public static final int CATEGORY_SODA = 4;
        public static final int CATEGORY_JUICE = 5;

        /**
         * Returns whether or not the given category is {@link #CATEGORY_MISC}, {@link #CATEGORY_BEER},
         * or {@link #CATEGORY_WINE}, {@link #CATEGORY_LIQUOR}, {@link #CATEGORY_SODA}, {@link #CATEGORY_JUICE}.
         */
        public static boolean isValidCategory(int category) {
            if (category == CATEGORY_MISC || category == CATEGORY_BEER || category == CATEGORY_WINE || category == CATEGORY_LIQUOR ||
                category == CATEGORY_SODA || category == CATEGORY_JUICE) {
                return true;
            }
            return false;
        }
    }
}
