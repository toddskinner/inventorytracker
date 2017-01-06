package com.example.android.barinventory;

import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CursorAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.example.android.barinventory.data.InventoryContract.InventoryEntry;

/**
 * Created by toddskinner on 12/21/16.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    int currentInventory;
    int rowID;

    public InventoryCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, final Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        final TextView itemName = (TextView) view.findViewById(R.id.name);
        final TextView itemCategory = (TextView) view.findViewById(R.id.category);
        final TextView itemQuantity = (TextView) view.findViewById(R.id.quantity);
        final TextView itemPrice = (TextView) view.findViewById(R.id.price);

        // Reference this Stackoverflow:
        // http://stackoverflow.com/questions/5323033/how-do-i-get-the-row-id-of-the-row-from-a-listview-which-contains-a-clickable-i
        rowID = cursor.getInt(cursor.getColumnIndex(InventoryEntry._ID));

        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_NAME));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_CATEGORY));

        if(category.equals("1")){
            category = "Beer";
        } else if(category.equals("2")){
            category = "Wine";
        } else if(category.equals("3")){
            category = "Liquor";
        } else if(category.equals("4")){
            category = "Soda";
        } else if(category.equals("5")){
            category = "Juice";
        } else {
            category = "Misc";
        }

        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_QUANTITY));
        String price = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_PRICE));

        // Populate fields with extracted properties
        itemName.setText(name);
        itemCategory.setText(category);
        itemQuantity.setText(quantity);
        itemPrice.setText(price);

        //set tag per the following http://stackoverflow.com/questions/5323033/how-do-i-get-the-row-id-of-the-row-from-a-listview-which-contains-a-clickable-i
        // http://stackoverflow.com/questions/11156078/android-get-the-listview-item-from-button-clicked-in-custom-listview
        itemName.setTag(rowID);

        Button listSellButton = (Button) view.findViewById(R.id.list_sell_button);

        listSellButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                currentInventory = Integer.valueOf(itemQuantity.getText().toString());
                currentInventory = currentInventory - 1;
                if(currentInventory < 0){
                    Toast.makeText(context, R.string.toast_not_less_zero, Toast.LENGTH_SHORT).show();
                    itemQuantity.setText("0");
                } else {
                    itemQuantity.setText(String.valueOf(currentInventory));

                    ContentValues values = new ContentValues();
                    values.put(InventoryEntry.COLUMN_ITEM_QUANTITY, currentInventory);

                    //get tag per the following http://stackoverflow.com/questions/5323033/how-do-i-get-the-row-id-of-the-row-from-a-listview-which-contains-a-clickable-i
                    // http://stackoverflow.com/questions/11156078/android-get-the-listview-item-from-button-clicked-in-custom-listview

                    int itemID = (Integer) itemName.getTag();
                    Uri mCurrentItemUri = ContentUris.withAppendedId(InventoryEntry.CONTENT_URI, itemID);
                    context.getContentResolver().update(mCurrentItemUri, values, null, null);
                }
            }
        });
    }
}
