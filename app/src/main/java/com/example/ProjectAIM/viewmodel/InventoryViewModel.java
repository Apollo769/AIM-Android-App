package com.example.ProjectAIM.viewmodel;

import com.example.ProjectAIM.model.Item;
import com.example.ProjectAIM.repository.InventoryRepository;

import java.util.ArrayList;
import java.util.Comparator;

/**
 * ViewModel for the inventory screen.
 * Keeps validation and inventory update logic separate from the View
 * and routes data changes through the repository instead of direct database access.
 */
public class InventoryViewModel {
    private final InventoryRepository repository;
    private ArrayList<Item> itemList;

    private static final int LOW_STOCK_THRESHOLD = 5;
    private static final int MAX_NAME_LENGTH = 100;
    private static final int MAX_DESCRIPTION_LENGTH = 500;

    public interface InventoryUpdateCallback {
        void onInventoryUpdated();
    }

    // Connects inventory screen logic to the Firestore-backed repository layer
    public InventoryViewModel() {
        repository = new InventoryRepository();
        itemList = new ArrayList<>();
    }

    public ArrayList<Item> getItemList() {
        return itemList;
    }

    // Loads inventory items from Firestore and updates the local working list
    public void loadItems(InventoryUpdateCallback callback) {
        repository.getAllItems(items -> updateLocalItems(items, callback));
    }

    // Validates and parses input before allowing new item data to reach Firestore
    public boolean addItemIfValid(
            String itemName,
            String quantityInput,
            String itemDescription,
            InventoryUpdateCallback callback
    ) {
        if (hasInvalidItemInput(itemName, quantityInput, itemDescription)) {
            return false;
        }

        try {
            int itemQuantity = Integer.parseInt(quantityInput);

            if (itemQuantity < 0) {
                return false;
            }

            repository.addItem(itemName, itemQuantity, itemDescription,
                    items -> updateLocalItems(items, callback));

            return true;
        } catch (NumberFormatException exception) {
            return false;
        }
    }

    private boolean hasInvalidItemInput(
            String itemName,
            String quantityInput,
            String itemDescription
    ) {
        return itemName.isEmpty()
                || quantityInput.isEmpty()
                || itemName.length() > MAX_NAME_LENGTH
                || itemDescription.length() > MAX_DESCRIPTION_LENGTH;
    }

    // Searches inventory by item name so users can quickly find matching items
    public ArrayList<Item> searchItems(String searchText) {
        ArrayList<Item> searchResults = new ArrayList<>();

        if (searchText == null || searchText.isEmpty()) {
            searchResults.addAll(itemList);
            return searchResults;
        }

        String lowerSearchText = searchText.toLowerCase().trim();

        for (Item item : itemList) {
            if (item.getName().toLowerCase().contains(lowerSearchText)) {
                searchResults.add(item);
            }
        }

        return searchResults;
    }

    // Sorts a copy of the list alphabetically without changing the stored database order
    public ArrayList<Item> sortItemsByName() {
        ArrayList<Item> sortedItems = new ArrayList<>(itemList);

        sortedItems.sort(Comparator.comparing(Item::getName, String.CASE_INSENSITIVE_ORDER));

        return sortedItems;
    }

    // Sorts a copy of the list so low quantities appear first
    public ArrayList<Item> sortItemsByQuantityLowToHigh() {
        ArrayList<Item> sortedItems = new ArrayList<>(itemList);

        sortedItems.sort(Comparator.comparingInt(Item::getQuantity));

        return sortedItems;
    }

    // Sorts a copy of the list so high quantities appear first
    public ArrayList<Item> sortItemsByQuantityHighToLow() {
        ArrayList<Item> sortedItems = new ArrayList<>(itemList);

        sortedItems.sort((firstItem, secondItem) ->
                Integer.compare(secondItem.getQuantity(), firstItem.getQuantity()));

        return sortedItems;
    }

    // Filters the list to show only items at or below the low-stock threshold
    public ArrayList<Item> filterLowStockItems() {
        ArrayList<Item> lowStockItems = new ArrayList<>();

        for (Item item : itemList) {
            if (item.getQuantity() <= LOW_STOCK_THRESHOLD) {
                lowStockItems.add(item);
            }
        }

        return lowStockItems;
    }

    // Routes quantity changes through the repository to keep Firestore access out of the View
    public void updateQuantity(String id, int newQuantity, InventoryUpdateCallback callback) {
        repository.updateQuantity(id, newQuantity, items -> updateLocalItems(items, callback));
    }

    // Routes delete requests through the repository to keep Firestore access out of the View
    public void deleteItem(String id, InventoryUpdateCallback callback) {
        repository.deleteItem(id, items -> updateLocalItems(items, callback));
    }

    private void updateLocalItems(ArrayList<Item> items, InventoryUpdateCallback callback) {
        itemList = items;
        callback.onInventoryUpdated();
    }
}