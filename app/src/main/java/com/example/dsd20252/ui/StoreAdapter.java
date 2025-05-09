package com.example.dsd20252.ui;

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

    public interface OnStoreClickListener {
        void onClick(Store store);
    }

    private final List<Store> stores;
    private final OnStoreClickListener listener;

    public StoreAdapter(List<Store> stores, OnStoreClickListener listener) {
        this.stores   = stores;
        this.listener = listener;
    }

    @NonNull @Override
    public StoreViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View row = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_store, parent, false);
        return new StoreViewHolder(row);
    }

    @Override
    public void onBindViewHolder(@NonNull StoreViewHolder holder, int position) {
        Store s = stores.get(position);
        holder.tvName.setText(s.getStoreName());
        holder.tvDetails.setText(
                String.format("%.6f, %.6f",
                        s.getLatitude(), s.getLongitude()
                )
        );
        holder.itemView.setOnClickListener(v -> listener.onClick(s));
    }

    @Override public int getItemCount() {
        return stores.size();
    }

    static class StoreViewHolder extends RecyclerView.ViewHolder {
        final TextView tvName, tvDetails;
        StoreViewHolder(@NonNull View itemView) {
            super(itemView);
            tvName    = itemView.findViewById(R.id.tvStoreName);
            tvDetails = itemView.findViewById(R.id.tvStoreDetails);
        }
    }
}
