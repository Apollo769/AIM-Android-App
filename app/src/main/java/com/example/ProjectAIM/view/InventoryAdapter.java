package com.example.ProjectAIM.view;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DiffUtil;
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

    public interface OnInventoryChangedListener {
        void onInventoryChanged();
    }

    private final ArrayList<Item> items;
    private final InventoryViewModel inventoryViewModel;
    private final OnInventoryChangedListener inventoryChangedListener;

    public InventoryAdapter(
            ArrayList<Item> items,
            InventoryViewModel inventoryViewModel,
            OnInventoryChangedListener inventoryChangedListener
    ) {
        this.items = new ArrayList<>(items);
        this.inventoryViewModel = inventoryViewModel;
        this.inventoryChangedListener = inventoryChangedListener;
    }

    // Replaces the displayed list using DiffUtil so RecyclerView only updates changed rows
    public void updateItems(ArrayList<Item> updatedItems) {
        DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(
                new ItemDiffCallback(items, updatedItems)
        );

        items.clear();
        items.addAll(updatedItems);

        diffResult.dispatchUpdatesTo(this);
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

        configureDescriptionButton(holder, item);
        configureQuantityUpdate(holder, item);
        configureDeleteButton(holder);
    }

    // Shows the description icon only when the item has saved details to display
    private void configureDescriptionButton(ViewHolder holder, Item item) {
        String itemDescription = item.getDescription();

        if (itemDescription == null || itemDescription.trim().isEmpty()) {
            holder.buttonDescription.setVisibility(View.GONE);
            holder.buttonDescription.setOnClickListener(null);
            return;
        }

        holder.buttonDescription.setVisibility(View.VISIBLE);
        holder.buttonDescription.setOnClickListener(view ->
                showDescriptionDialog(view, item.getName(), itemDescription));
    }

    // Validates row edits here because quantity changes happen directly inside each RecyclerView row
    private void configureQuantityUpdate(ViewHolder holder, Item item) {
        holder.editItemQty.setOnFocusChangeListener((view, hasFocus) -> {
            if (hasFocus) {
                return;
            }

            String quantityInput = holder.editItemQty.getText().toString().trim();

            if (quantityInput.isEmpty()) {
                showToast(view, "Quantity cannot be empty.");
                return;
            }

            try {
                int newQuantity = Integer.parseInt(quantityInput);

                if (newQuantity < 0) {
                    showToast(view, "Quantity cannot be negative.");
                    return;
                }

                item.setQuantity(newQuantity);

                inventoryViewModel.updateQuantity(
                        item.getId(),
                        newQuantity,
                        () -> handleQuantityUpdated(view)
                );
            } catch (NumberFormatException exception) {
                showToast(view, "Please enter a valid quantity.");
            }
        });
    }

    // Gets the current adapter position so the correct item is deleted even if the list changes
    private void configureDeleteButton(ViewHolder holder) {
        holder.buttonDelete.setOnClickListener(view -> {
            int positionToRemove = holder.getBindingAdapterPosition();

            if (positionToRemove == RecyclerView.NO_POSITION) {
                return;
            }

            Item itemToRemove = items.get(positionToRemove);

            inventoryViewModel.deleteItem(
                    itemToRemove.getId(),
                    () -> handleItemDeleted(view)
            );
        });
    }

    // Refreshes the active inventory view after a successful
    // quantity change so sorting and filtering remain accurate
    private void handleQuantityUpdated(View view) {
        inventoryChangedListener.onInventoryChanged();
        showToast(view, "Quantity updated");
    }

    // Reapplies the current inventory view after deletion so
    // displayed results remain synchronized with Firestore data
    private void handleItemDeleted(View view) {
        inventoryChangedListener.onInventoryChanged();
        showToast(view, "Item deleted");
    }

    // Displays additional item details without expanding the
    // inventory row layout.
    private void showDescriptionDialog(View view, String itemName, String itemDescription) {
        new AlertDialog.Builder(view.getContext())
                .setTitle(itemName)
                .setMessage(itemDescription)
                .setPositiveButton("Back", null)
                .show();
    }

    private void showToast(View view, String message) {
        Toast.makeText(view.getContext(), message, Toast.LENGTH_SHORT).show();
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Compares old and new inventory lists so RecyclerView can update only the rows that changed.
     */
    private static class ItemDiffCallback extends DiffUtil.Callback {
        private final ArrayList<Item> oldItems;
        private final ArrayList<Item> newItems;

        ItemDiffCallback(ArrayList<Item> oldItems, ArrayList<Item> newItems) {
            this.oldItems = oldItems;
            this.newItems = newItems;
        }

        @Override
        public int getOldListSize() {
            return oldItems.size();
        }

        @Override
        public int getNewListSize() {
            return newItems.size();
        }

        @Override
        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
            return oldItems.get(oldItemPosition)
                    .getId()
                    .equals(newItems.get(newItemPosition).getId());
        }

        @Override
        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
            Item oldItem = oldItems.get(oldItemPosition);
            Item newItem = newItems.get(newItemPosition);

            return hasSameContent(oldItem, newItem);
        }

        private boolean hasSameContent(Item oldItem, Item newItem) {
            return oldItem.getQuantity() == newItem.getQuantity()
                    && textMatches(oldItem.getName(), newItem.getName())
                    && textMatches(oldItem.getDescription(), newItem.getDescription());
        }

        // Normalizes null values before comparison so DiffUtil can
        // safely detect content changes
        private boolean textMatches(String oldText, String newText) {
            return String.valueOf(oldText).equals(String.valueOf(newText));
        }
    }

    /**
     * Stores row view references so RecyclerView can reuse item layouts without repeatedly finding views.
     */
    public static class ViewHolder extends RecyclerView.ViewHolder {
        final TextView textItemName;
        final EditText editItemQty;
        final ImageButton buttonDescription;
        final ImageButton buttonDelete;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            textItemName = itemView.findViewById(R.id.textItemName);
            editItemQty = itemView.findViewById(R.id.editItemQty);
            buttonDescription = itemView.findViewById(R.id.buttonDescription);
            buttonDelete = itemView.findViewById(R.id.buttonDelete);
        }
    }
}