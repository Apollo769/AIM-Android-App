package com.example.ProjectAIM.repository;

import android.content.Context;

import com.example.ProjectAIM.data.DatabaseHelper;
import com.example.ProjectAIM.model.Item;

import java.util.ArrayList;

/**
 * Repository for inventory data.
 * Keeps database operations in one layer so the ViewModel does not directly depend on SQLite details.
 */
public class InventoryRepository {
    private final DatabaseHelper dbHelper;

    public InventoryRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Keeps repository methods as the single path between the ViewModel and database helper
    public ArrayList<Item> getAllItems() {
        return dbHelper.getAllItems();
    }

    public void addItem(String name, int quantity, String description) {
        dbHelper.addItem(name, quantity, description);
    }

    public void updateQuantity(int id, int newQuantity) {
        dbHelper.updateQuantity(id, newQuantity);
    }

    public void deleteItem(int id) {
        dbHelper.deleteItem(id);
    }
}