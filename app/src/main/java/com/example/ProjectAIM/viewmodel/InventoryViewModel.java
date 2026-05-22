package com.example.ProjectAIM.viewmodel;

import android.content.Context;

import com.example.ProjectAIM.model.Item;
import com.example.ProjectAIM.repository.InventoryRepository;

import java.util.ArrayList;

public class InventoryViewModel {
    private final InventoryRepository repository;
    private ArrayList<Item> itemList;

    // Creates the ViewModel and connects it to the repository
    public InventoryViewModel(Context context) {
        repository = new InventoryRepository(context);
        itemList = repository.getAllItems();
    }

    // Gets the current inventory list
    public ArrayList<Item> getItemList() {
        return itemList;
    }

    // Reloads inventory items from the repository
    public void loadItems() {
        itemList = repository.getAllItems();
    }

    // Checks if the item name and quantity fields are valid
    public boolean isValidItemInput(String name, String quantity) {
        return !name.isEmpty() && !quantity.isEmpty();
    }

    // Converts quantity text into an integer
    public int parseQuantity(String quantity) {
        return Integer.parseInt(quantity);
    }

    // Adds a new item and refreshes the inventory list
    public void addItem(String name, int quantity, String description) {
        repository.addItem(name, quantity, description);
        loadItems();
    }

    // Updates an existing item quantity
    public void updateQuantity(int id, int newQuantity) {
        repository.updateQuantity(id, newQuantity);
        loadItems();
    }

    // Deletes an item and refreshes the inventory list
    public void deleteItem(int id) {
        repository.deleteItem(id);
        loadItems();
    }
}