package com.example.ProjectAIM.viewmodel;

import android.content.Context;

import com.example.ProjectAIM.model.Item;
import com.example.ProjectAIM.repository.InventoryRepository;

import java.util.ArrayList;

/**
 * ViewModel for the inventory screen.
 * Keeps validation and inventory update logic separate from the View
 * and routes data changes through the repository instead of direct database access.
 */
public class InventoryViewModel {
    private final InventoryRepository repository;
    private ArrayList<Item> itemList;

    // Connects inventory screen logic to the repository layer instead of the View
    public InventoryViewModel(Context context) {
        repository = new InventoryRepository(context);
        itemList = repository.getAllItems();
    }

    public ArrayList<Item> getItemList() {
        return itemList;
    }

    // Refreshes the local list because repository changes do not update it automatically
    public void loadItems() {
        itemList = repository.getAllItems();
    }

    // Validates and parses input before allowing new item data to reach the database
    public boolean addItemIfValid(String itemName, String quantityInput, String itemDescription) {
        if (itemName.isEmpty() || quantityInput.isEmpty()) {
            return false;
        }

        try {
            int itemQuantity = Integer.parseInt(quantityInput);

            if (itemQuantity < 0) {
                return false;
            }

            repository.addItem(itemName, itemQuantity, itemDescription);
            loadItems();
            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    // Routes quantity changes through the repository to keep database access out of the View
    public void updateQuantity(int id, int newQuantity) {
        repository.updateQuantity(id, newQuantity);
        loadItems();
    }

    // Routes delete requests through the repository to keep database access out of the View
    public void deleteItem(int id) {
        repository.deleteItem(id);
        loadItems();
    }
}