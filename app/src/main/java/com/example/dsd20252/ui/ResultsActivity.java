package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsd20252.R;
import com.example.dsd20252.model.Store;
import com.example.dsd20252.parser.StoreParser;

import org.json.JSONArray;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
// RecyclerView
public class ResultsActivity extends AppCompatActivity {
    private static final String TAG = "ResultsActivity";

    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    // filter criteria from intent
    private int minStars = 1;
    private List<String> categoryFilters = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        // DEBUG: show asset list
        try {
            String[] rootItems = getAssets().list("");
            Toast.makeText(
                    this,
                    "Assets: " + Arrays.toString(rootItems),
                    Toast.LENGTH_LONG
            ).show();
        } catch (IOException e) {
            Log.e(TAG, "Failed to list assets", e);
        }

        // read filter parameters
        Intent intent = getIntent();
        minStars = intent.getIntExtra("minStars", 1);
        List<String> categories = intent.getStringArrayListExtra("categoryFilters");
        if (categories != null) categoryFilters = categories;

        setTitle("Search Results");
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        progressBar  = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty      = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        loadStoresFromAssets();
    }

    private void loadStoresFromAssets() {
        new Thread(() -> {
            List<Store> allStores = new ArrayList<>();
            try {
                String[] rootItems = getAssets().list("C:\\Users\\billo\\AndroidStudioProjects\\DSD202522\\app\\src\\main\\assets\\pizza-fun");
                if (rootItems != null) {
                    for (String item : rootItems) {
                        if (item.toLowerCase().endsWith(".json")) {
                            Store s = parseFromAssetPath(item);
                            if (s != null) allStores.add(s);
                        } else {
                            String[] sub = getAssets().list(item);
                            if (sub != null) {
                                for (String file : sub) {
                                    if (file.toLowerCase().endsWith(".json")) {
                                        Store s = parseFromAssetPath(item + "/" + file);
                                        if (s != null) allStores.add(s);
                                    }
                                }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                Log.e(TAG, "Error reading assets", e);
            }
            Log.d(TAG, "Total stores loaded before filtering: " + allStores.size());
            // Apply filters and show
            List<Store> filtered = applyFilters(allStores);
            runOnUiThread(() -> showStores(filtered));
        }).start();
    }

    private Store parseFromAssetPath(String assetPath) {
        try (InputStream is = getAssets().open(assetPath)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String json = new String(buffer, StandardCharsets.UTF_8);
            Log.d(TAG, "Raw JSON from " + assetPath + ": " + json);
            // parse using JSON-to-Store string parser
            return StoreParser.parseStoreFromJsonString(json);
        } catch (Exception e) {
            Log.e(TAG, "Failed to parse asset JSON: " + assetPath, e);
            return null;
        }
    }

    /**
     * Filters stores by minimum stars and selected categories.
     */
    private List<Store> applyFilters(List<Store> stores) {
        List<Store> result = new ArrayList<>();
        for (Store s : stores) {
            if (s == null) continue;
            if (s.getStars() < minStars) continue;
            if (!categoryFilters.isEmpty() && !categoryFilters.contains(s.getFoodCategory())) continue;
            result.add(s);
        }
        Log.d(TAG, "Total stores after filtering: " + result.size());
        return result;
    }

    private void showStores(List<Store> stores) {
        progressBar.setVisibility(View.GONE);
        if (stores.isEmpty()) {
            tvEmpty.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        } else {
            tvEmpty.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            StoreAdapter adapter = new StoreAdapter(stores, store -> {
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