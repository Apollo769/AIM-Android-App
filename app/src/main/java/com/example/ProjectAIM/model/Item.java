package com.example.ProjectAIM.model;

/**
 * Model for a single inventory item.
 * Stores item data separately from UI and database logic so the app can pass inventory records between layers.
 */
public class Item {
    private final String id;
    private final String name;
    private int quantity;
    private final String description;

    public Item(String id, String name, int quantity, String description) {
        this.id = id;
        this.name = name;
        this.quantity = quantity;
        this.description = description;
    }

    // Getters and Setters
    public String getId() { return id; }

    public String getName() { return name; }

    public int getQuantity() { return quantity; }

    public String getDescription() { return description; }

    public void setQuantity(int quantity) { this.quantity = quantity; }
}