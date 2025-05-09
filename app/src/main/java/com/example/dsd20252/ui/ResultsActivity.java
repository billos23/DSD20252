package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsd20252.R;
import com.example.dsd20252.model.SearchRequest;
import com.example.dsd20252.model.Store;
import com.example.dsd20252.network.NetworkClient;

import java.util.List;

public class ResultsActivity extends AppCompatActivity {
    private ProgressBar pbLoading;
    private TextView tvEmpty;
    private RecyclerView rvStores;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_results);

        pbLoading = findViewById(R.id.pbLoading);
        tvEmpty   = findViewById(R.id.tvEmpty);
        rvStores  = findViewById(R.id.rvStores);

        pbLoading.setVisibility(View.VISIBLE);
        tvEmpty.setVisibility(View.GONE);
        rvStores.setVisibility(View.GONE);

        SearchRequest req = (SearchRequest) getIntent()
                .getSerializableExtra("searchReq");

        new Thread(() -> {
            try {
                List<Store> stores = new NetworkClient()
                        .searchStores(req);
                runOnUiThread(() -> onSearchSuccess(stores));
            } catch (Exception e) {
                runOnUiThread(() -> onSearchError(e));
            }
        }).start();
    }

    private void onSearchSuccess(List<Store> stores) {
        pbLoading.setVisibility(View.GONE);
        if (stores.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
        } else {
            rvStores.setVisibility(View.VISIBLE);
            StoreAdapter adapter = new StoreAdapter(
                    stores,
                    store -> {
                        Intent i = new Intent(this, StoreDetailActivity.class);
                        i.putExtra("store", store);
                        startActivity(i);
                    }
            );
            rvStores.setAdapter(adapter);
        }
    }

    private void onSearchError(Exception e) {
        pbLoading.setVisibility(View.GONE);
        Toast.makeText(
                this,
                getString(R.string.toast_search_error, e.getMessage()),
                Toast.LENGTH_LONG
        ).show();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }
}
