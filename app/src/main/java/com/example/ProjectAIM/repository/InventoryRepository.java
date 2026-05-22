package com.example.ProjectAIM.repository;

import android.content.Context;

import com.example.ProjectAIM.data.DatabaseHelper;
import com.example.ProjectAIM.model.Item;

import java.util.ArrayList;

public class InventoryRepository {
    private final DatabaseHelper dbHelper;

    // Creates the repository and connects it to the existing database helper
    public InventoryRepository(Context context) {
        dbHelper = new DatabaseHelper(context);
    }

    // Gets all inventory items from the database
    public ArrayList<Item> getAllItems() {
        return dbHelper.getAllItems();
    }

    // Adds a new inventory item to the database
    public void addItem(String name, int quantity, String description) {
        dbHelper.addItem(name, quantity, description);
    }

    // Updates the quantity for an existing inventory item
    public void updateQuantity(int id, int newQuantity) {
        dbHelper.updateQuantity(id, newQuantity);
    }

    // Deletes an inventory item from the database
    public void deleteItem(int id) {
        dbHelper.deleteItem(id);
    }
}