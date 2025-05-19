package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ResultsActivity extends AppCompatActivity {
    private ProgressBar progressBar;
    private RecyclerView recyclerView;
    private TextView tvEmpty;
    private Handler mainHandler;
    private ExecutorService executor;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        progressBar = findViewById(R.id.progressBar);
        recyclerView = findViewById(R.id.recyclerView);
        tvEmpty = findViewById(R.id.tvEmpty);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mainHandler = new Handler(Looper.getMainLooper());
        executor = Executors.newSingleThreadExecutor();

        setTitle("Search Results");
        ActionBar ab = getSupportActionBar();
        if (ab != null) ab.setDisplayHomeAsUpEnabled(true);

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

        loadStores();
    }

    private void loadStores() {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.GONE);

        // Get search request from intent
        Intent intent = getIntent();
        SearchRequest searchRequest = null;
        if (intent.hasExtra("searchReq")) {
            searchRequest = (SearchRequest) intent.getSerializableExtra("searchReq");
        }

        if (searchRequest == null) {
            showError("No search criteria provided");
            return;
        }

        final SearchRequest finalSearchRequest = searchRequest;

        // Set a timeout for the entire operation
        mainHandler.postDelayed(() -> {
            if (progressBar.getVisibility() == View.VISIBLE) {
                showError("Search timeout - please try again");
            }
        }, 10000); // 10 second timeout

        executor.execute(() -> {
            try {
                // For now, just load from assets to avoid network issues
                // Comment: Server connection would go here when backend is ready

                List<Store> allStores = loadStoresFromAssets();

                if (allStores == null) {
                    allStores = new ArrayList<>();
                }

                // Apply filters
                List<Store> filteredStores = applyFilters(allStores, finalSearchRequest);

                // Apply distance filter (5km radius)
                List<Store> finalStores = filterByDistance(filteredStores,
                        finalSearchRequest.getLatitude(), finalSearchRequest.getLongitude());

                // Make allStores final for lambda
                final List<Store> finalAllStores = allStores;

                // Update UI on main thread
                mainHandler.post(() -> {
                    showStores(finalStores);

                    if (finalStores.isEmpty()) {
                        if (finalAllStores.isEmpty()) {
                            Toast.makeText(ResultsActivity.this, "No stores found in local data", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(ResultsActivity.this, "No stores within 5km match your criteria", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Toast.makeText(ResultsActivity.this, "Found " + finalStores.size() + " stores", Toast.LENGTH_SHORT).show();
                    }
                });

            } catch (Exception e) {
                mainHandler.post(() -> showError("Error loading stores: " + e.getMessage()));
            }
        });
    }

    private List<Store> loadStoresFromAssets() {
        List<Store> allStores = new ArrayList<>();

        try {
            String[] targetDirs = {"pizza-fun", "souvlaki-house", "vegan-corner"};

            for (String dir : targetDirs) {
                try {
                    String[] files = getAssets().list(dir);
                    if (files != null && files.length > 0) {
                        for (String file : files) {
                            if (file.toLowerCase().endsWith(".json")) {
                                String path = dir + "/" + file;
                                Store store = parseFromAssetPath(path);
                                if (store != null) {
                                    // Set category based on directory
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
                    // Continue with next directory
                }
            }

            // If no stores found in target directories, search recursively
            if (allStores.isEmpty()) {
                searchDirectoryRecursively("", allStores);
            }

        } catch (Exception e) {
            // Return what we have so far
        }

        return allStores;
    }

    private void searchDirectoryRecursively(String dirPath, List<Store> stores) {
        try {
            String[] list = getAssets().list(dirPath);
            if (list == null || list.length == 0) return;

            for (String file : list) {
                String fullPath = dirPath.isEmpty() ? file : dirPath + "/" + file;

                try {
                    String[] subFiles = getAssets().list(fullPath);
                    if (subFiles != null && subFiles.length > 0) {
                        // It's a directory, search recursively
                        searchDirectoryRecursively(fullPath, stores);
                    } else if (file.toLowerCase().endsWith(".json")) {
                        // It's a JSON file
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
                } catch (Exception e) {
                    // Continue with next file
                }
            }
        } catch (Exception e) {
            // Failed to list assets
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

    private List<Store> applyFilters(List<Store> stores, SearchRequest request) {
        List<Store> result = new ArrayList<>();

        for (Store store : stores) {
            if (store == null) continue;

            // Check star rating
            if (store.getStars() < request.getMinStars()) {
                continue;
            }

            // Check category
            boolean categoryMatch = false;
            if (request.getCategories().isEmpty()) {
                categoryMatch = true; // No category filter
            } else {
                for (String category : request.getCategories()) {
                    if (store.getFoodCategory() != null &&
                            store.getFoodCategory().equalsIgnoreCase(category)) {
                        categoryMatch = true;
                        break;
                    }
                }
            }

            if (!categoryMatch) {
                continue;
            }

            // Check price category
            boolean priceMatch = false;
            if (request.getPriceCategories().isEmpty()) {
                priceMatch = true; // No price filter
            } else {
                for (String priceCategory : request.getPriceCategories()) {
                    if (store.getPriceCategory() != null &&
                            store.getPriceCategory().equals(priceCategory)) {
                        priceMatch = true;
                        break;
                    }
                }
            }

            if (!priceMatch) {
                continue;
            }

            result.add(store);
        }

        return result;
    }

    private List<Store> filterByDistance(List<Store> stores, double userLat, double userLon) {
        List<Store> filteredStores = new ArrayList<>();

        for (Store store : stores) {
            double distance = calculateDistance(userLat, userLon,
                    store.getLatitude(), store.getLongitude());

            // Assignment requires 5km radius
            if (distance <= 5.0) {
                filteredStores.add(store);
            }
        }

        return filteredStores;
    }

    private double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        final int R = 6371; // Radius of the earth in km

        double latDistance = Math.toRadians(lat2 - lat1);
        double lonDistance = Math.toRadians(lon2 - lon1);
        double a = Math.sin(latDistance / 2) * Math.sin(latDistance / 2)
                + Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2))
                * Math.sin(lonDistance / 2) * Math.sin(lonDistance / 2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        double distance = R * c; // Distance in km

        return distance;
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

    private void showError(String message) {
        progressBar.setVisibility(View.GONE);
        tvEmpty.setVisibility(View.VISIBLE);
        Toast.makeText(this, message, Toast.LENGTH_LONG).show();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (executor != null) {
            executor.shutdown();
            try {
                if (!executor.awaitTermination(1, TimeUnit.SECONDS)) {
                    executor.shutdownNow();
                }
            } catch (InterruptedException e) {
                executor.shutdownNow();
            }
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