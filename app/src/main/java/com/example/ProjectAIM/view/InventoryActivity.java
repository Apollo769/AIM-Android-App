package com.example.ProjectAIM.view;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectAIM.R;
import com.example.ProjectAIM.viewmodel.InventoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * View for the inventory screen.
 * Handles UI actions while routing inventory logic through the ViewModel
 * so database and validation logic stay outside the Activity.
 */
public class InventoryActivity extends AppCompatActivity {

    private static final String OPTION_SHOW_ALL = "Show All Items";
    private static final String OPTION_SORT_NAME = "Sort by Name";
    private static final String OPTION_QUANTITY_LOW_HIGH = "Quantity Low to High";
    private static final String OPTION_QUANTITY_HIGH_LOW = "Quantity High to Low";
    private static final String OPTION_LOW_STOCK = "Low Stock Only";

    private RecyclerView recyclerView;
    private FloatingActionButton fabAddItem;
    private ImageButton fabNotifications;
    private ImageButton buttonInventoryOptions;
    private EditText searchInventory;

    private InventoryAdapter adapter;
    private InventoryViewModel inventoryViewModel;
    private String currentInventoryOption = OPTION_SHOW_ALL;

    // Initializes screen components and loads inventory data
    // before user interaction begins
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        connectViews();
        setupRecyclerView();
        setupSearch();
        setupInventoryOptions();
        setupButtonListeners();
        loadInventoryItems();
    }

    // Connects layout components so UI events can be routed
    // through the Activity
    private void connectViews() {
        recyclerView = findViewById(R.id.recyclerInventory);
        fabAddItem = findViewById(R.id.fabAddItem);
        fabNotifications = findViewById(R.id.fabNotifications);
        buttonInventoryOptions = findViewById(R.id.buttonInventoryOptions);
        searchInventory = findViewById(R.id.searchInventory);
    }

    // Configures RecyclerView and adapter so inventory updates
    // can be displayed through the MVVM workflow
    private void setupRecyclerView() {
        inventoryViewModel = new InventoryViewModel();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        adapter = new InventoryAdapter(new ArrayList<>(), inventoryViewModel, this::refreshCurrentInventoryView);
        recyclerView.setAdapter(adapter);
    }

    // Retrieves inventory data through the ViewModel so UI updates
    // remain independent of Firestore operations
    private void loadInventoryItems() {
        inventoryViewModel.loadItems(this::refreshCurrentInventoryView);
    }

    private void setupSearch() {
        searchInventory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence text, int start, int count, int after) {
            }

            @Override
            public void onTextChanged(CharSequence text, int start, int before, int count) {
                currentInventoryOption = OPTION_SHOW_ALL;
                refreshCurrentInventoryView();
            }

            @Override
            public void afterTextChanged(Editable editable) {
            }
        });
    }

    private void setupInventoryOptions() {
        buttonInventoryOptions.setOnClickListener(this::showInventoryOptionsMenu);
    }

    private void setupButtonListeners() {
        fabAddItem.setOnClickListener(view -> showAddItemDialog());

        fabNotifications.setOnClickListener(view ->
                startActivity(new Intent(this, NotificationActivity.class)));
    }

    /**
     * Shows inventory options that demonstrate search, sorting, filtering, and low-stock prioritization.
     */
    private void showInventoryOptionsMenu(View anchorView) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);

        popupMenu.getMenu().add(OPTION_SHOW_ALL);
        popupMenu.getMenu().add(OPTION_SORT_NAME);
        popupMenu.getMenu().add(OPTION_QUANTITY_LOW_HIGH);
        popupMenu.getMenu().add(OPTION_QUANTITY_HIGH_LOW);
        popupMenu.getMenu().add(OPTION_LOW_STOCK);

        popupMenu.setOnMenuItemClickListener(menuItem -> {
            currentInventoryOption = String.valueOf(menuItem.getTitle());

            if (currentInventoryOption.equals(OPTION_SHOW_ALL)) {
                searchInventory.setText("");
            }

            refreshCurrentInventoryView();
            return true;
        });

        popupMenu.show();
    }

    /**
     * Reapplies the active search, sort, or filter after item data changes.
     */
    private void refreshCurrentInventoryView() {
        String searchText = searchInventory.getText().toString().trim();

        if (!searchText.isEmpty()) {
            adapter.updateItems(inventoryViewModel.searchItems(searchText));
            return;
        }

        switch (currentInventoryOption) {
            case OPTION_SORT_NAME:
                adapter.updateItems(inventoryViewModel.sortItemsByName());
                break;

            case OPTION_QUANTITY_LOW_HIGH:
                adapter.updateItems(inventoryViewModel.sortItemsByQuantityLowToHigh());
                break;

            case OPTION_QUANTITY_HIGH_LOW:
                adapter.updateItems(inventoryViewModel.sortItemsByQuantityHighToLow());
                break;

            case OPTION_LOW_STOCK:
                adapter.updateItems(inventoryViewModel.filterLowStockItems());
                break;

            case OPTION_SHOW_ALL:
            default:
                adapter.updateItems(inventoryViewModel.getItemList());
                break;
        }
    }

    /**
     * Builds the add-item dialog while leaving validation and item creation to the ViewModel
     * so the Activity stays focused on UI behavior.
     */
    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_item, null, false);

        EditText inputName = dialogView.findViewById(R.id.inputName);
        EditText inputQty  = dialogView.findViewById(R.id.inputQty);
        EditText inputDesc = dialogView.findViewById(R.id.inputDesc);
        Button buttonSave  = dialogView.findViewById(R.id.buttonSaveItem);
        Button buttonCancel= dialogView.findViewById(R.id.buttonCancel);

        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(dialogView)
                        .create();

        buttonCancel.setOnClickListener(view -> dialog.dismiss());

        buttonSave.setOnClickListener(view -> {
            String itemName = inputName.getText().toString().trim();
            String quantityInput = inputQty.getText().toString().trim();
            String itemDescription = inputDesc.getText().toString().trim();

            boolean itemAdded = inventoryViewModel.addItemIfValid(
                    itemName,
                    quantityInput,
                    itemDescription,
                    this::refreshCurrentInventoryView
            );

            if (!itemAdded) {
                Toast.makeText(this, "Please enter a valid name and quantity.", Toast.LENGTH_SHORT).show();
                return;
            }

            dialog.dismiss();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
            );
        }
    }
}