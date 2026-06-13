package com.example.ProjectAIM.repository;

import android.util.Log;

import com.example.ProjectAIM.model.Item;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Repository for inventory data.
 * Keeps Firestore operations in one layer so the ViewModel does not directly depend on cloud database details.
 */
public class InventoryRepository {

    public interface InventoryLoadCallback {
        void onItemsLoaded(ArrayList<Item> items);
    }

    private static final String TAG = "InventoryRepository";

    private static final String COLLECTION_USERS = "users";
    private static final String COLLECTION_ITEMS = "items";
    private static final String FIELD_NAME = "name";
    private static final String FIELD_QUANTITY = "quantity";
    private static final String FIELD_DESCRIPTION = "description";

    private final FirebaseFirestore db;
    private final FirebaseAuth auth;

    public InventoryRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    // Builds a user-specific Firestore path so inventory records
    // remain isolated between authenticated accounts
    private CollectionReference getItemsCollection() {
        String userId = getCurrentUserId();

        return db.collection(COLLECTION_USERS)
                .document(userId)
                .collection(COLLECTION_ITEMS);
    }

    // Requires authentication before inventory access to prevent
    // operations against shared or anonymous data
    private String getCurrentUserId() {
        FirebaseUser currentUser = auth.getCurrentUser();

        if (currentUser == null) {
            throw new IllegalStateException(
                    "Inventory data cannot be accessed without a signed-in user."
            );
        }

        return currentUser.getUid();
    }

    public void getAllItems(InventoryLoadCallback callback) {
        getItemsCollection()
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    ArrayList<Item> items = new ArrayList<>();

                    for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                        items.add(createItemFromDocument(document));
                    }

                    callback.onItemsLoaded(items);
                })
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Failed to load inventory items.", exception));
    }

    public void addItem(
            String name,
            int quantity,
            String description,
            InventoryLoadCallback callback
    ) {
        getItemsCollection()
                .add(createItemData(name, quantity, description))
                .addOnSuccessListener(documentReference ->
                        getAllItems(callback))
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Failed to add inventory item.", exception));
    }

    public void updateQuantity(
            String id,
            int newQuantity,
            InventoryLoadCallback callback
    ) {
        getItemsCollection()
                .document(id)
                .update(FIELD_QUANTITY, newQuantity)
                .addOnSuccessListener(unused ->
                        getAllItems(callback))
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Failed to update inventory quantity.", exception));
    }

    public void deleteItem(
            String id,
            InventoryLoadCallback callback
    ) {
        getItemsCollection()
                .document(id)
                .delete()
                .addOnSuccessListener(unused ->
                        getAllItems(callback))
                .addOnFailureListener(exception ->
                        Log.e(TAG, "Failed to delete inventory item.", exception));
    }

    // Converts Firestore documents into Item objects so cloud data
    // can be used throughout the MVVM layers
    private Item createItemFromDocument(QueryDocumentSnapshot document) {
        String id = document.getId();
        String name = document.getString(FIELD_NAME);
        String description = document.getString(FIELD_DESCRIPTION);
        Long quantityValue = document.getLong(FIELD_QUANTITY);

        int quantity = quantityValue == null
                ? 0
                : quantityValue.intValue();

        return new Item(id, name, quantity, description);
    }

    // Maps item fields into Firestore-compatible key/value pairs
    // before inventory records are stored in the cloud database
    private Map<String, Object> createItemData(
            String name,
            int quantity,
            String description
    ) {
        Map<String, Object> itemData = new HashMap<>();

        itemData.put(FIELD_NAME, name);
        itemData.put(FIELD_QUANTITY, quantity);
        itemData.put(FIELD_DESCRIPTION, description);

        return itemData;
    }
}