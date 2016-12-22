package com.example.android.barinventory;

import android.content.Context;
import android.database.Cursor;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;

import com.example.android.barinventory.data.InventoryContract.InventoryEntry;

import org.w3c.dom.Text;

import static android.R.attr.name;

/**
 * Created by toddskinner on 12/21/16.
 */

public class InventoryCursorAdapter extends CursorAdapter {

    public InventoryCursorAdapter(Context context, Cursor c){
        super(context, c, 0);
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        return LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        // Find fields to populate in inflated template
        TextView itemName = (TextView) view.findViewById(R.id.name);
        TextView itemCategory = (TextView) view.findViewById(R.id.category);
        TextView itemQuantity = (TextView) view.findViewById(R.id.quantity);
        // Extract properties from cursor
        String name = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_NAME));
        String category = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_CATEGORY));
        String quantity = cursor.getString(cursor.getColumnIndexOrThrow(InventoryEntry.COLUMN_ITEM_QUANTITY));
        // Populate fields with extracted properties
        itemName.setText(name);
        itemCategory.setText(category);
        itemQuantity.setText(quantity);
    }
}
