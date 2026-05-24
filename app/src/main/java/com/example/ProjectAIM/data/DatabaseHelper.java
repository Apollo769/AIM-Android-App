package com.example.ProjectAIM.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ProjectAIM.model.Item;

import java.util.ArrayList;

/**
 * SQLite helper for the local inventory database.
 * Keeps table creation and inventory queries in one place so database details stay out of the View and ViewModel layers.
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db";
    private static final int DATABASE_VERSION = 1;

    // Table and column constants reduce repeated strings across database operations
    private static final String TABLE_ITEMS = "items";
    private static final String COLUMN_ID   = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QTY  = "quantity";
    private static final String COLUMN_DESC = "description";

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // Creates the inventory table when the local database is first initialized
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_QTY + " INTEGER, " +
                COLUMN_DESC + " TEXT)");
    }

    // Rebuilds the table when the schema version changes
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // Stores inventory data locally so items remain available between app sessions
    public void addItem(String name, int quantity, String description) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_NAME, name);
        values.put(COLUMN_QTY, quantity);
        values.put(COLUMN_DESC, description);

        db.insert(TABLE_ITEMS, null, values);
        db.close();
    }

    // Converts database rows into Item objects so other layers do not work directly with cursors
    public ArrayList<Item> getAllItems() {
        ArrayList<Item> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

        if (cursor.moveToFirst()) {
            do {
                list.add(new Item(
                        cursor.getInt(0),
                        cursor.getString(1),
                        cursor.getInt(2),
                        cursor.getString(3)
                ));
            } while (cursor.moveToNext());
        }

        cursor.close();
        db.close();
        return list;
    }

    // Updates only the quantity field so item identity and description are preserved
    public void updateQuantity(int id, int newQuantity) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();

        values.put(COLUMN_QTY, newQuantity);

        db.update(TABLE_ITEMS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // Deletes by ID so the correct inventory record is removed even if names are duplicated
    public void deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();

        db.delete(TABLE_ITEMS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}