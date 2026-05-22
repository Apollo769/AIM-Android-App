package com.example.ProjectAIM.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.ProjectAIM.R;
import com.example.ProjectAIM.model.Item;
import com.example.ProjectAIM.viewmodel.InventoryViewModel;

import java.util.ArrayList;

// Adapter that connects the item list to the RecyclerView
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final ArrayList<Item> items;                       // all items shown in list
    private final InventoryViewModel inventoryViewModel;       // manages item updates and deletes

    public InventoryAdapter(ArrayList<Item> items, InventoryViewModel inventoryViewModel) {
        this.items = items;
        this.inventoryViewModel = inventoryViewModel;
    }

    // creates a single row for the list
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(v);
    }

    // fills the row with item info and sets up button actions
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.textItemName.setText(item.getName());
        holder.editItemQty.setText(String.valueOf(item.getQuantity()));

        // updates item quantity when quantity field loses focus
        holder.editItemQty.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) {
                String s = holder.editItemQty.getText().toString().trim();
                if (!s.isEmpty()) {
                    int newQty = Integer.parseInt(s);
                    item.setQuantity(newQty);
                    inventoryViewModel.updateQuantity(item.getId(), newQty);
                    Toast.makeText(v.getContext(), "Quantity updated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // deletes the item when delete button is clicked
        holder.buttonDelete.setOnClickListener(v -> {
            int pos = holder.getBindingAdapterPosition();
            if (pos == RecyclerView.NO_POSITION) return;
            Item toRemove = items.get(pos);
            inventoryViewModel.deleteItem(toRemove.getId());
            items.remove(pos);
            notifyItemRemoved(pos);
            Toast.makeText(v.getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
        });
    }

    // returns total item count for recycler view
    @Override
    public int getItemCount() {
        return items.size();
    }

    // holds each row view in memory for smoother scrolling
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textItemName;   // shows item name
        EditText editItemQty;    // lets user change quantity
        ImageButton buttonDelete;// removes item from list

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            editItemQty = itemView.findViewById(R.id.editItemQty);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}