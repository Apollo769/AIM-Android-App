package com.example.ProjectAIM.view;

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

/**
 * Adapter for displaying inventory items in the RecyclerView.
 * Routes row-level item changes through the ViewModel so the adapter does not directly manage database operations.
 */
public class InventoryAdapter extends RecyclerView.Adapter<InventoryAdapter.ViewHolder> {

    private final ArrayList<Item> items;
    private final InventoryViewModel inventoryViewModel;

    public InventoryAdapter(ArrayList<Item> items, InventoryViewModel inventoryViewModel) {
        this.items = items;
        this.inventoryViewModel = inventoryViewModel;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_inventory, parent, false);
        return new ViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Item item = items.get(position);
        holder.textItemName.setText(item.getName());
        holder.editItemQty.setText(String.valueOf(item.getQuantity()));

        // Validates row edits here because quantity changes happen directly inside each RecyclerView row
        holder.editItemQty.setOnFocusChangeListener((view, hasFocus) -> {
            if (!hasFocus) {
                String quantityInput = holder.editItemQty.getText().toString().trim();

                if (quantityInput.isEmpty()) {
                    Toast.makeText(view.getContext(), "Quantity cannot be empty.", Toast.LENGTH_SHORT).show();
                    return;
                }

                try {
                    int newQuantity = Integer.parseInt(quantityInput);

                    if (newQuantity < 0) {
                        Toast.makeText(view.getContext(), "Quantity cannot be negative.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    item.setQuantity(newQuantity);
                    inventoryViewModel.updateQuantity(item.getId(), newQuantity);
                    Toast.makeText(view.getContext(), "Quantity updated", Toast.LENGTH_SHORT).show();
                } catch (NumberFormatException exception) {
                    Toast.makeText(view.getContext(), "Please enter a valid quantity.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Gets the current adapter position so the correct item is removed even if the list changes
        holder.buttonDelete.setOnClickListener(view -> {
            int positionToRemove = holder.getBindingAdapterPosition();
            if (positionToRemove == RecyclerView.NO_POSITION) return;

            Item itemToRemove = items.get(positionToRemove);
            inventoryViewModel.deleteItem(itemToRemove.getId());
            items.remove(positionToRemove);
            notifyItemRemoved(positionToRemove);
            Toast.makeText(view.getContext(), "Item deleted", Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Stores row view references so RecyclerView can reuse item layouts without repeatedly finding views.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView textItemName;
        EditText editItemQty;
        ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            editItemQty = itemView.findViewById(R.id.editItemQty);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}