package com.example.ProjectAIM.view;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectAIM.R;
import com.example.ProjectAIM.model.Item;
import com.example.ProjectAIM.viewmodel.InventoryViewModel;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;

/**
 * View for the inventory screen.
 * Handles UI actions while routing inventory logic through the ViewModel
 * so database and validation logic stay outside the Activity.
 */
public class InventoryActivity extends AppCompatActivity {

    private ArrayList<Item> itemList;
    private InventoryAdapter adapter;
    private InventoryViewModel inventoryViewModel;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // Connect layout controls used by this screen
        RecyclerView recyclerView = findViewById(R.id.recyclerInventory);
        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        ImageButton fabNotifications = findViewById(R.id.fabNotifications);

        // Set up inventory data through the ViewModel instead of direct database access
        inventoryViewModel = new InventoryViewModel(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = inventoryViewModel.getItemList();

        // Connect the RecyclerView to the adapter so inventory items display on screen
        adapter = new InventoryAdapter(itemList, inventoryViewModel);
        recyclerView.setAdapter(adapter);

        fabAddItem.setOnClickListener(view -> showAddItemDialog());

        fabNotifications.setOnClickListener(view ->
                startActivity(new Intent(this, NotificationActivity.class)));
    }

    /**
     * Builds the add-item dialog while leaving validation and item creation to the ViewModel
     * so the Activity stays focused on UI behavior.
     */
    private void showAddItemDialog() {
        View dialogView = LayoutInflater.from(this).inflate(R.layout.add_item, null, false);

        // Connect dialog inputs used to collect new item details
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

            // Use the ViewModel to validate and save item data before updating the UI
            if (!inventoryViewModel.addItemIfValid(itemName, quantityInput, itemDescription)) {
                Toast.makeText(this, "Please enter a valid name and quantity.", Toast.LENGTH_SHORT).show();
                return;
            }

            // Refresh the local list because RecyclerView does not automatically see ViewModel changes
            itemList.clear();
            itemList.addAll(inventoryViewModel.getItemList());
            adapter.notifyItemInserted(itemList.size() - 1);

            dialog.dismiss();
        });

        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}