package com.example.ProjectAIM.ui;

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

// Main screen for viewing, adding, and editing inventory items
public class InventoryActivity extends AppCompatActivity {

    private ArrayList<Item> itemList;                  // holds all items to show in list
    private InventoryAdapter adapter;                  // connects data to recycler view
    private InventoryViewModel inventoryViewModel;     // manages inventory logic and data updates

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_inventory);

        // link the layout views
        RecyclerView recyclerView = findViewById(R.id.recyclerInventory);
        FloatingActionButton fabAddItem = findViewById(R.id.fabAddItem);
        ImageButton fabNotifications = findViewById(R.id.fabNotifications);

        // create ViewModel and load items
        inventoryViewModel = new InventoryViewModel(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        itemList = inventoryViewModel.getItemList();

        // set adapter to display items in the recycler
        adapter = new InventoryAdapter(itemList, inventoryViewModel);
        recyclerView.setAdapter(adapter);

        // add new items using a pop-up dialog
        fabAddItem.setOnClickListener(v -> showAddItemDialog());

        // open notification settings page
        fabNotifications.setOnClickListener(v ->
                startActivity(new Intent(this, NotificationActivity.class)));
    }

    // shows the pop-up form to add a new item to the list
    private void showAddItemDialog() {
        View view = LayoutInflater.from(this).inflate(R.layout.add_item, null, false);

        // connect input fields
        EditText inputName = view.findViewById(R.id.inputName);
        EditText inputQty  = view.findViewById(R.id.inputQty);
        EditText inputDesc = view.findViewById(R.id.inputDesc);
        Button buttonSave  = view.findViewById(R.id.buttonSaveItem);
        Button buttonCancel= view.findViewById(R.id.buttonCancel);

        // create and show dialog
        androidx.appcompat.app.AlertDialog dialog =
                new androidx.appcompat.app.AlertDialog.Builder(this)
                        .setView(view)
                        .create();

        // close without saving
        buttonCancel.setOnClickListener(v -> dialog.dismiss());

        // save new item when user confirms
        buttonSave.setOnClickListener(v -> {
            String name = inputName.getText().toString().trim();
            String qtyStr = inputQty.getText().toString().trim();
            String desc = inputDesc.getText().toString().trim();

            // check for empty fields using ViewModel
            if (!inventoryViewModel.isValidItemInput(name, qtyStr)) {
                Toast.makeText(this, "Please enter a name and quantity.", Toast.LENGTH_SHORT).show();
                return;
            }

            try {
                // convert quantity and add item through ViewModel
                int qty = inventoryViewModel.parseQuantity(qtyStr);
                inventoryViewModel.addItem(name, qty, desc);

                // refresh recycler view
                itemList.clear();
                itemList.addAll(inventoryViewModel.getItemList());
                adapter.notifyItemInserted(itemList.size() - 1);

                dialog.dismiss();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "Please enter a valid quantity.", Toast.LENGTH_SHORT).show();
            }
        });

        // show the dialog and make it use the full screen width for better layout
        dialog.show();
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        }
    }
}