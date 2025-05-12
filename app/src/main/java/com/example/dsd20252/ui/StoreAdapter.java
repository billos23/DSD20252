package com.example.dsd20252.ui;

import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsd20252.R;
import com.example.dsd20252.model.Store;

import java.util.List;

public class StoreAdapter extends RecyclerView.Adapter<StoreAdapter.StoreViewHolder> {
    /**
     * Callback for when a store is clicked.
     */
    public interface OnStoreClickListener {
        void onStoreClick(Store store);
    }

    private final List<Store> stores;
    private final OnStoreClickListener listener;

    public StoreAdapter(List<Store> stores, OnStoreClickListener listener) {
        this.stores = stores;
        this.listener = listener;
    }

    @NonNull
    @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store store = stores.get(position);
        holder.tvName.setText(store.getStoreName());
        holder.tvCoords.setText(
                String.format("%.6f, %.6f",
                        store.getLatitude(), store.getLongitude()));
        holder.tvStars.setText(String.valueOf(store.getStars()));
        holder.tvCategory.setText(store.getFoodCategory());

        holder.itemView.setOnClickListener(v -> listener.onStoreClick(store));
    }

    @Override
    public int getItemCount() {
        return stores.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName;
        final TextView tvCoords;
        final TextView tvStars;
        final TextView tvCategory;

        StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName     = itemView.findViewById(R.id.tvStoreName);
            tvCoords   = itemView.findViewById(R.id.tvCoords);
            tvStars    = itemView.findViewById(R.id.tvStars);
            tvCategory = itemView.findViewById(R.id.tvCategory);
        }
    }
}