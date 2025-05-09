package com.example.dsd20252.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsd20252.R;
import com.example.dsd20252.model.Store;
import com.example.dsd20252.parser.StoreParser;

import org.json.JSONArray;
import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import android.content.Intent;
import com.example.dsd20252.ui.StoreDetailActivity;

public class ResultsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        setTitle("Search Results");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        progressBar  = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty      = findViewById(R.id.tvEmpty);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        loadStoresFromAssets();
    }

    private void loadStoresFromAssets() {
        new Thread(() -> {
            List<Store> stores = new ArrayList<>();
            try {
                String[] files = getAssets().list("");
                if (files != null) {
                    for (String filename : files) {
                        if (filename.endsWith(".json")) {
                            InputStream is = getAssets().open(filename);
                            int size = is.available();
                            byte[] buffer = new byte[size];
                            is.read(buffer);
                            is.close();
                            String json = new String(buffer, StandardCharsets.UTF_8);

                            if (json.trim().startsWith("[")) {
                                JSONArray arr = new JSONArray(json);
                                for (int i = 0; i < arr.length(); i++) {
                                    stores.add(
                                            StoreParser.parseStoreFromJson(
                                                    arr.getJSONObject(i).toString()
                                            )
                                    );
                                }
                            } else {
                                stores.add(StoreParser.parseStoreFromJson(json));
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            runOnUiThread(() -> showStores(stores));
        }).start();
    }

    private void showStores(List<Store> stores) {
        progressBar.setVisibility(View.GONE);
        if (stores.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            // Use the adapter constructor that takes only a List<Store>
            StoreAdapter adapter = new StoreAdapter(stores, store -> {
                // Handle item click: open StoreDetailActivity
                Intent intent = new Intent(ResultsActivity.this, StoreDetailActivity.class);
                intent.putExtra("store", store);
                startActivity(intent);
            });
            recyclerView.setAdapter(adapter);
        }
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
