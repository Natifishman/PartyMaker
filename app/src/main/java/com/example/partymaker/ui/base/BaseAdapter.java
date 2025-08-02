package com.example.partymaker.ui.base;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Base RecyclerView Adapter class that provides common functionality for all adapters in the PartyMaker app.
 * This class implements common adapter patterns and provides type safety.
 * 
 * Features:
 * - Type-safe item handling
 * - Common list operations (add, remove, clear, update)
 * - Click listener management
 * - Performance optimizations
 * - Null safety
 * 
 * @param <T> The data model type
 * @param <VH> The ViewHolder type that extends BaseViewHolder
 */
public abstract class BaseAdapter<T, VH extends BaseAdapter.BaseViewHolder> extends RecyclerView.Adapter<VH> {

    /**
     * Tag for logging, automatically set to the class name
     */
    protected final String TAG = getClass().getSimpleName();
    
    /**
     * The list of items displayed by this adapter
     */
    protected final List<T> items;
    
    /**
     * Click listener for item clicks
     */
    protected OnItemClickListener<T> onItemClickListener;
    
    /**
     * Long click listener for item long clicks
     */
    protected OnItemLongClickListener<T> onItemLongClickListener;

    /**
     * Constructor
     */
    public BaseAdapter() {
        this.items = new ArrayList<>();
        Log.d(TAG, "BaseAdapter created");
    }

    /**
     * Constructor with initial data
     * @param items Initial list of items
     */
    public BaseAdapter(@NonNull List<T> items) {
        this.items = new ArrayList<>(items);
        Log.d(TAG, "BaseAdapter created with " + items.size() + " items");
    }

    @NonNull
    @Override
    public VH onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        View view = inflater.inflate(getItemLayoutId(), parent, false);
        
        VH holder = createViewHolder(view);
        
        // Setup click listeners
        setupClickListeners(holder);
        
        Log.d(TAG, "ViewHolder created for position");
        return holder;
    }

    @Override
    public void onBindViewHolder(@NonNull VH holder, int position) {
        T item = getItem(position);
        if (item != null) {
            holder.bind(item, position);
            onBindViewHolder(holder, item, position);
        }
    }

    @Override
    public int getItemCount() {
        return items.size();
    }

    /**
     * Setup click listeners for the ViewHolder
     * @param holder The ViewHolder to setup listeners for
     */
    private void setupClickListeners(@NonNull VH holder) {
        // Item click listener
        holder.itemView.setOnClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onItemClickListener != null) {
                T item = getItem(position);
                if (item != null) {
                    onItemClickListener.onItemClick(item, position);
                }
            }
        });
        
        // Item long click listener
        holder.itemView.setOnLongClickListener(v -> {
            int position = holder.getAdapterPosition();
            if (position != RecyclerView.NO_POSITION && onItemLongClickListener != null) {
                T item = getItem(position);
                if (item != null) {
                    return onItemLongClickListener.onItemLongClick(item, position);
                }
            }
            return false;
        });
    }

    // Abstract methods that subclasses must implement

    /**
     * @return The layout resource ID for individual items
     */
    @LayoutRes
    protected abstract int getItemLayoutId();

    /**
     * Create a ViewHolder for the given view
     * @param view The inflated item view
     * @return A new ViewHolder instance
     */
    @NonNull
    protected abstract VH createViewHolder(@NonNull View view);

    /**
     * Bind data to the ViewHolder
     * Called after the base binding is complete
     * @param holder The ViewHolder to bind to
     * @param item The item to bind
     * @param position The position in the adapter
     */
    protected void onBindViewHolder(@NonNull VH holder, @NonNull T item, int position) {
        // Default implementation does nothing - subclasses can override
    }

    // Public API methods

    /**
     * Get item at the specified position
     * @param position The position
     * @return The item at the position, or null if position is invalid
     */
    public T getItem(int position) {
        if (position >= 0 && position < items.size()) {
            return items.get(position);
        }
        return null;
    }

    /**
     * Add an item to the end of the list
     * @param item The item to add
     */
    public void addItem(@NonNull T item) {
        items.add(item);
        notifyItemInserted(items.size() - 1);
        Log.d(TAG, "Item added at position " + (items.size() - 1));
    }

    /**
     * Add an item at the specified position
     * @param item The item to add
     * @param position The position to add at
     */
    public void addItem(@NonNull T item, int position) {
        if (position >= 0 && position <= items.size()) {
            items.add(position, item);
            notifyItemInserted(position);
            Log.d(TAG, "Item added at position " + position);
        }
    }

    /**
     * Add multiple items to the end of the list
     * @param newItems The items to add
     */
    public void addItems(@NonNull List<T> newItems) {
        if (!newItems.isEmpty()) {
            int startPosition = items.size();
            items.addAll(newItems);
            notifyItemRangeInserted(startPosition, newItems.size());
            Log.d(TAG, newItems.size() + " items added starting at position " + startPosition);
        }
    }

    /**
     * Remove an item at the specified position
     * @param position The position to remove
     */
    public void removeItem(int position) {
        if (position >= 0 && position < items.size()) {
            items.remove(position);
            notifyItemRemoved(position);
            Log.d(TAG, "Item removed at position " + position);
        }
    }

    /**
     * Remove a specific item
     * @param item The item to remove
     * @return true if the item was removed, false otherwise
     */
    public boolean removeItem(@NonNull T item) {
        int position = items.indexOf(item);
        if (position != -1) {
            removeItem(position);
            return true;
        }
        return false;
    }

    /**
     * Update an item at the specified position
     * @param item The new item data
     * @param position The position to update
     */
    public void updateItem(@NonNull T item, int position) {
        if (position >= 0 && position < items.size()) {
            items.set(position, item);
            notifyItemChanged(position);
            Log.d(TAG, "Item updated at position " + position);
        }
    }

    /**
     * Clear all items
     */
    public void clear() {
        int itemCount = items.size();
        items.clear();
        notifyItemRangeRemoved(0, itemCount);
        Log.d(TAG, "All items cleared (" + itemCount + " items)");
    }

    /**
     * Replace all items with new data
     * @param newItems The new items
     */
    public void setItems(@NonNull List<T> newItems) {
        items.clear();
        items.addAll(newItems);
        notifyDataSetChanged();
        Log.d(TAG, "Items replaced with " + newItems.size() + " new items");
    }

    /**
     * Get a copy of all items
     * @return A new list containing all items
     */
    @NonNull
    public List<T> getItems() {
        return new ArrayList<>(items);
    }

    /**
     * Check if the adapter is empty
     * @return true if no items, false otherwise
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    // Click listener interfaces and setters

    /**
     * Interface for item click events
     * @param <T> The item type
     */
    public interface OnItemClickListener<T> {
        void onItemClick(@NonNull T item, int position);
    }

    /**
     * Interface for item long click events
     * @param <T> The item type
     */
    public interface OnItemLongClickListener<T> {
        boolean onItemLongClick(@NonNull T item, int position);
    }

    /**
     * Set the item click listener
     * @param listener The click listener
     */
    public void setOnItemClickListener(OnItemClickListener<T> listener) {
        this.onItemClickListener = listener;
    }

    /**
     * Set the item long click listener
     * @param listener The long click listener
     */
    public void setOnItemLongClickListener(OnItemLongClickListener<T> listener) {
        this.onItemLongClickListener = listener;
    }

    /**
     * Base ViewHolder class that provides common functionality
     */
    public static abstract class BaseViewHolder extends RecyclerView.ViewHolder {
        
        /**
         * Constructor
         * @param itemView The item view
         */
        public BaseViewHolder(@NonNull View itemView) {
            super(itemView);
        }

        /**
         * Bind data to the views
         * @param item The item to bind
         * @param position The position in the adapter
         */
        public abstract void bind(@NonNull Object item, int position);
    }
}