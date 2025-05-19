package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsd20252.R;
import com.example.dsd20252.model.SearchRequest;
import com.example.dsd20252.model.Store;
import com.example.dsd20252.parser.StoreParser;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class ResultsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;

    private int minStars = 1;
    private List<String> categoryFilters = new ArrayList<>();
    private List<String> priceCategories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        setTitle("Search Results");
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

        Intent intent = getIntent();
        if (intent.hasExtra("searchReq")) {
            SearchRequest req = (SearchRequest) intent.getSerializableExtra("searchReq");
            if (req != null) {
                minStars = req.getMinStars();
                categoryFilters = req.getCategories();
                priceCategories = req.getPriceCategories();
            }
        } else {
            minStars = intent.getIntExtra("minStars", 1);
            List<String> categories = intent.getStringArrayListExtra("categoryFilters");
            if (categories != null) categoryFilters = categories;
        }

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
        bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.homeBtn) {
                    Intent homeIntent = new Intent(ResultsActivity.this, HomeActivity.class);
                    startActivity(homeIntent);
                    return true;
                } else if (itemId == R.id.searchbtn) {
                    Intent searchIntent = new Intent(ResultsActivity.this, FilterActivity.class);
                    startActivity(searchIntent);
                    return true;
                } else if (itemId == R.id.accountbtn) {
                    Intent accountIntent = new Intent(ResultsActivity.this, SettingsActivity.class);
                    startActivity(accountIntent);
                    return true;
                }
                return false;
            }
        });

        loadStoresFromAssets();
    }

    private void loadStoresFromAssets() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        new Thread(() -> {
            List<Store> allStores = new ArrayList<>();

            try {
                String[] targetDirs = {"pizza-fun", "souvlaki-house", "vegan-corner"};
                boolean foundAnyTargetDirs = false;

                for (String dir : targetDirs) {
                    try {
                        String[] files = getAssets().list(dir);
                        if (files != null && files.length > 0) {
                            foundAnyTargetDirs = true;
                            for (String file : files) {
                                if (file.toLowerCase().endsWith(".json")) {
                                    String path = dir + "/" + file;
                                    Store store = parseFromAssetPath(path);
                                    if (store != null) {
                                        if (dir.equals("pizza-fun")) {
                                            store.setFoodCategory("pizzeria");
                                        } else if (dir.equals("souvlaki-house")) {
                                            store.setFoodCategory("souvlaki_house");
                                        } else if (dir.equals("vegan-corner")) {
                                            store.setFoodCategory("VeganCorner");
                                        }
                                        allStores.add(store);
                                    }
                                }
                            }
                        }
                    } catch (IOException e) {
                        // Error reading directory, continue with next
                    }
                }

                if (!foundAnyTargetDirs) {
                    searchDirectoryRecursively("", allStores);
                }

            } catch (Exception e) {
                runOnUiThread(() -> Toast.makeText(
                        ResultsActivity.this,
                        "Error loading stores: " + e.getMessage(),
                        Toast.LENGTH_SHORT).show());
            }

            final List<Store> filtered = applyFilters(allStores);
            runOnUiThread(() -> {
                showStores(filtered);
                if (filtered.isEmpty() && !allStores.isEmpty()) {
                    Toast.makeText(ResultsActivity.this,
                            "No stores match your filters. Try different criteria.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }

    private void searchDirectoryRecursively(String dirPath, List<Store> stores) throws IOException {
        String[] list = getAssets().list(dirPath);
        if (list == null || list.length == 0) return;

        for (String file : list) {
            String fullPath = dirPath.isEmpty() ? file : dirPath + "/" + file;

            String[] subFiles = getAssets().list(fullPath);
            if (subFiles != null && subFiles.length > 0) {
                searchDirectoryRecursively(fullPath, stores);
            } else if (file.toLowerCase().endsWith(".json")) {
                Store store = parseFromAssetPath(fullPath);
                if (store != null) {
                    String lowerPath = fullPath.toLowerCase();
                    if (lowerPath.contains("pizza")) {
                        store.setFoodCategory("pizzeria");
                    } else if (lowerPath.contains("souvlaki")) {
                        store.setFoodCategory("souvlaki_house");
                    } else if (lowerPath.contains("vegan")) {
                        store.setFoodCategory("VeganCorner");
                    }
                    stores.add(store);
                }
            }
        }
    }

    private Store parseFromAssetPath(String assetPath) {
        try (InputStream is = getAssets().open(assetPath)) {
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            String json = new String(buffer, StandardCharsets.UTF_8);

            return StoreParser.parseStoreFromJsonString(json);
        } catch (Exception e) {
            return null;
        }
    }

    private List<Store> applyFilters(List<Store> stores) {
        List<Store> result = new ArrayList<>();

        for (Store s : stores) {
            if (s == null) continue;

            if (s.getStars() < minStars) {
                continue;
            }

            if (!categoryFilters.isEmpty()) {
                boolean categoryMatch = false;
                for (String category : categoryFilters) {
                    if (s.getFoodCategory().equalsIgnoreCase(category)) {
                        categoryMatch = true;
                        break;
                    }
                }

                if (!categoryMatch) {
                    continue;
                }
            }

            if (!priceCategories.isEmpty()) {
                if (s.getPriceCategory() == null || !priceCategories.contains(s.getPriceCategory())) {
                    continue;
                }
            }

            result.add(s);
        }

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