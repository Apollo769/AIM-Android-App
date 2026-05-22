package com.example.ProjectAIM.data;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.example.ProjectAIM.model.Item;

import java.util.ArrayList;

// Handles creating and managing the app's local SQLite database
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "inventory.db"; // file name
    private static final int DATABASE_VERSION = 1;              // version number

    // table and column setup
    private static final String TABLE_ITEMS = "items";
    private static final String COLUMN_ID   = "id";
    private static final String COLUMN_NAME = "name";
    private static final String COLUMN_QTY  = "quantity";
    private static final String COLUMN_DESC = "description";

    // sets up database
    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    // creates the database table if it doesn’t exist yet
    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE " + TABLE_ITEMS + " (" +
                COLUMN_ID + " INTEGER PRIMARY KEY AUTOINCREMENT, " +
                COLUMN_NAME + " TEXT, " +
                COLUMN_QTY + " INTEGER, " +
                COLUMN_DESC + " TEXT)");
    }

    // if version changes, deletes old table and rebuilds
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ITEMS);
        onCreate(db);
    }

    // adds a new record to the table
    public void addItem(String name, int quantity, String description) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_NAME, name);
        values.put(COLUMN_QTY, quantity);
        values.put(COLUMN_DESC, description);
        db.insert(TABLE_ITEMS, null, values);
        db.close();
    }

    // gets every item in the database and returns them in a list
    public ArrayList<Item> getAllItems() {
        ArrayList<Item> list = new ArrayList<>();
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT * FROM " + TABLE_ITEMS, null);

        if (c.moveToFirst()) {
            do {
                list.add(new Item(
                        c.getInt(0),
                        c.getString(1),
                        c.getInt(2),
                        c.getString(3)
                ));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return list;
    }

    // updates item quantity using its ID
    public void updateQuantity(int id, int newQty) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COLUMN_QTY, newQty);
        db.update(TABLE_ITEMS, values, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    // removes an item by ID
    public void deleteItem(int id) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(TABLE_ITEMS, COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }
}
