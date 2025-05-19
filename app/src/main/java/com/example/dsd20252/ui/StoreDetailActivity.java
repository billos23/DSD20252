package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dsd20252.R;
import com.example.dsd20252.model.BuyRequest;
import com.example.dsd20252.model.BuyResponse;
import com.example.dsd20252.model.CartItem;
import com.example.dsd20252.model.Product;
import com.example.dsd20252.model.Store;
import com.example.dsd20252.network.NetworkClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StoreDetailActivity extends AppCompatActivity {
    private static final String TAG = "StoreDetailActivity";
    private Store store;

    // Cart management
    private Map<String, CartItem> cartItems = new HashMap<>();
    private LinearLayout cartTotalSection;
    private TextView tvCartItems;
    private TextView tvCartTotal;
    private Button btnCompletePurchase;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {
            setContentView(R.layout.activity_store_detail);
            Log.d(TAG, "Layout set successfully");

            // Get the store object from the intent
            Intent intent = getIntent();
            if (intent != null && intent.hasExtra("store")) {
                store = (Store) intent.getSerializableExtra("store");
                Log.d(TAG, "Store object received: " + (store != null ? store.getStoreName() : "null"));
            }

            if (store == null) {
                Log.e(TAG, "Store object is null");
                Toast.makeText(this, "Store data not found", Toast.LENGTH_LONG).show();
                finish();
                return;
            }

            // Set up action bar safely
            try {
                if (getSupportActionBar() != null) {
                    getSupportActionBar().setDisplayHomeAsUpEnabled(true);
                    setTitle(store.getStoreName());
                }
            } catch (Exception e) {
                Log.e(TAG, "Error setting up action bar", e);
            }

            // Initialize UI components safely
            initializeViews();

        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
            Toast.makeText(this, "Error loading store details", Toast.LENGTH_LONG).show();
            finish();
        }
    }

    private void initializeViews() {
        try {
            // Find all views
            TextView tvStoreName = findViewById(R.id.tvDetailName);
            TextView tvCoords = findViewById(R.id.tvDetailCoords);
            TextView tvStars = findViewById(R.id.tvDetailStars);
            TextView tvCategory = findViewById(R.id.tvDetailCategory);
            TextView tvPriceRange = findViewById(R.id.tvDetailPriceRange);
            TextView tvVotes = findViewById(R.id.tvDetailVotes);
            ImageView ivStoreLogo = findViewById(R.id.ivStoreLogo);
            Spinner spinnerProducts = findViewById(R.id.spinnerProducts);
            EditText etQuantity = findViewById(R.id.etQuantity);
            Button btnBuy = findViewById(R.id.btnBuy);

            // Cart related views
            cartTotalSection = findViewById(R.id.cartTotalSection);
            tvCartItems = findViewById(R.id.tvCartItems);
            tvCartTotal = findViewById(R.id.tvCartTotal);
            btnCompletePurchase = findViewById(R.id.btnCompletePurchase);

            // Check if all views were found
            if (tvStoreName == null || tvCoords == null || tvStars == null ||
                    tvCategory == null || tvPriceRange == null || tvVotes == null ||
                    ivStoreLogo == null || spinnerProducts == null ||
                    etQuantity == null || btnBuy == null) {
                Log.e(TAG, "Some views are null - layout file might be incorrect");
                Toast.makeText(this, "Layout error - some views not found", Toast.LENGTH_LONG).show();
                return;
            }

            // Populate store information
            tvStoreName.setText(store.getStoreName());
            tvCoords.setText(String.format("%.6f, %.6f", store.getLatitude(), store.getLongitude()));
            tvStars.setText(String.format("★ %d", store.getStars()));
            tvCategory.setText(store.getFoodCategory());
            tvPriceRange.setText(store.getPriceCategory() != null ? store.getPriceCategory() : "N/A");
            tvVotes.setText(String.format("%d votes", store.getNoOfVotes()));

            // Load store logo if available
            loadStoreLogo(ivStoreLogo);

            // Set up products spinner
            setupProductsSpinner(spinnerProducts);

            // Set up add to cart button
            btnBuy.setOnClickListener(v -> {
                String selectedProduct = (String) spinnerProducts.getSelectedItem();
                if (selectedProduct == null || selectedProduct.equals("No products available")) {
                    Toast.makeText(this, "Please select a valid product", Toast.LENGTH_SHORT).show();
                    return;
                }

                int quantity;
                try {
                    String qtyText = etQuantity.getText().toString().trim();
                    if (qtyText.isEmpty()) {
                        Toast.makeText(this, "Please enter a quantity", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    quantity = Integer.parseInt(qtyText);
                    if (quantity <= 0) {
                        throw new NumberFormatException("Quantity must be positive");
                    }
                } catch (NumberFormatException ex) {
                    Toast.makeText(this, "Please enter a valid quantity", Toast.LENGTH_SHORT).show();
                    return;
                }

                // Add to cart
                addToCart(selectedProduct, quantity);

                // Clear quantity field
                etQuantity.setText("");
            });

            // Set up complete purchase button
            btnCompletePurchase.setOnClickListener(v -> completePurchase());

            // Initialize Bottom Navigation
            initializeBottomNavigation();

        } catch (Exception e) {
            Log.e(TAG, "Error initializing views", e);
            Toast.makeText(this, "Error setting up views", Toast.LENGTH_LONG).show();
        }
    }

    private void addToCart(String selectedProduct, int quantity) {
        try {
            // Extract product name and find the product details
            String productName = extractProductName(selectedProduct);
            Product product = findProductByName(productName);

            if (product == null) {
                Toast.makeText(this, "Product not found", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if item already exists in cart
            if (cartItems.containsKey(productName)) {
                CartItem existingItem = cartItems.get(productName);
                existingItem.setQuantity(existingItem.getQuantity() + quantity);
                Toast.makeText(this, String.format("Updated %s quantity to %d",
                        productName, existingItem.getQuantity()), Toast.LENGTH_SHORT).show();
            } else {
                CartItem newItem = new CartItem(productName, product.getPrice(), quantity);
                cartItems.put(productName, newItem);
                Toast.makeText(this, String.format("Added %d x %s to cart",
                        quantity, productName), Toast.LENGTH_SHORT).show();
            }

            // Update cart display
            updateCartDisplay();

        } catch (Exception e) {
            Log.e(TAG, "Error adding to cart", e);
            Toast.makeText(this, "Error adding item to cart", Toast.LENGTH_SHORT).show();
        }
    }

    private Product findProductByName(String productName) {
        List<Product> products = store.getProducts();
        if (products != null) {
            for (Product product : products) {
                if (product.getProductName().equals(productName)) {
                    return product;
                }
            }
        }
        return null;
    }

    private void updateCartDisplay() {
        if (cartItems.isEmpty()) {
            cartTotalSection.setVisibility(View.GONE);
            btnCompletePurchase.setVisibility(View.GONE);
        } else {
            cartTotalSection.setVisibility(View.VISIBLE);
            btnCompletePurchase.setVisibility(View.VISIBLE);

            // Calculate totals
            int totalItems = 0;
            double totalPrice = 0.0;

            for (CartItem item : cartItems.values()) {
                totalItems += item.getQuantity();
                totalPrice += item.getTotalPrice();
            }

            // Update display
            tvCartItems.setText(String.format("%d item%s in cart",
                    totalItems, totalItems == 1 ? "" : "s"));
            tvCartTotal.setText(String.format("Total: €%.2f", totalPrice));
        }
    }

    private void completePurchase() {
        if (cartItems.isEmpty()) {
            Toast.makeText(this, "Cart is empty", Toast.LENGTH_SHORT).show();
            return;
        }

        // Show loading state
        Toast.makeText(this, "Processing purchase...", Toast.LENGTH_SHORT).show();

        // Disable button to prevent multiple clicks
        btnCompletePurchase.setEnabled(false);

        // Calculate order total for thank you page
        double orderTotal = 0.0;
        int totalItems = 0;
        for (CartItem item : cartItems.values()) {
            orderTotal += item.getTotalPrice();
            totalItems += item.getQuantity();
        }
        final double finalOrderTotal = orderTotal;
        final int finalTotalItems = totalItems;

        Log.d(TAG, "Starting purchase process for " + cartItems.size() + " different items");

        // For testing purposes, let's skip the network call and directly go to thank you
        // Comment/uncomment the sections below to test

        // OPTION 1: Skip network call for testing (uncomment this section)
        runOnUiThread(() -> {
            btnCompletePurchase.setEnabled(true);
            Log.d(TAG, "Navigating to ThankYouActivity (test mode)");

            try {
                Intent thankYouIntent = new Intent(StoreDetailActivity.this, ThankYouActivity.class);
                thankYouIntent.putExtra("storeName", store.getStoreName());
                thankYouIntent.putExtra("orderDetails",
                        String.format("Order total: €%.2f (%d items)", finalOrderTotal, finalTotalItems));

                Log.d(TAG, "Intent created successfully");
                startActivity(thankYouIntent);
                Log.d(TAG, "startActivity called successfully");

                // Clear cart and finish this activity
                clearCart();
                finish();
                Log.d(TAG, "Activity finished");
            } catch (Exception e) {
                Log.e(TAG, "Error navigating to ThankYouActivity", e);
                Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
            }
        });

        /* OPTION 2: Use network call (comment out OPTION 1 and uncomment this section)
        // Process purchase in background thread
        new Thread(() -> {
            try {
                NetworkClient networkClient = new NetworkClient(this);
                boolean allSuccessful = true;
                StringBuilder results = new StringBuilder();

                // Process each item in cart
                for (CartItem item : cartItems.values()) {
                    BuyRequest buyRequest = new BuyRequest(store.getStoreName(),
                            item.getProductName(), item.getQuantity());
                    BuyResponse response = networkClient.buyProducts(buyRequest);

                    if (response.isSuccess()) {
                        results.append(String.format("✓ %d x %s\n",
                                item.getQuantity(), item.getProductName()));
                    } else {
                        allSuccessful = false;
                        results.append(String.format("✗ %d x %s: %s\n",
                                item.getQuantity(), item.getProductName(), response.getMessage()));
                    }
                }

                final boolean finalAllSuccessful = allSuccessful;
                final String finalResults = results.toString();

                runOnUiThread(() -> {
                    btnCompletePurchase.setEnabled(true);

                    if (finalAllSuccessful) {
                        Log.d(TAG, "All purchases successful, navigating to ThankYouActivity");

                        try {
                            // Navigate to Thank You screen
                            Intent thankYouIntent = new Intent(StoreDetailActivity.this, ThankYouActivity.class);
                            thankYouIntent.putExtra("storeName", store.getStoreName());
                            thankYouIntent.putExtra("orderDetails",
                                    String.format("Order total: €%.2f (%d items)", finalOrderTotal, finalTotalItems));
                            startActivity(thankYouIntent);

                            // Clear cart and finish this activity
                            clearCart();
                            finish();
                        } catch (Exception e) {
                            Log.e(TAG, "Error navigating to ThankYouActivity", e);
                            Toast.makeText(this, "Purchase successful but error navigating: " + e.getMessage(),
                                    Toast.LENGTH_LONG).show();
                        }
                    } else {
                        Log.d(TAG, "Some purchases failed: " + finalResults);
                        Toast.makeText(this, "Some items failed to purchase:\n" + finalResults,
                                Toast.LENGTH_LONG).show();
                    }
                });

            } catch (Exception e) {
                Log.e(TAG, "Error processing complete purchase", e);
                runOnUiThread(() -> {
                    btnCompletePurchase.setEnabled(true);
                    Toast.makeText(this, "Error processing purchase: " + e.getMessage(),
                            Toast.LENGTH_LONG).show();
                });
            }
        }).start();
        */
    }

    private void clearCart() {
        cartItems.clear();
        updateCartDisplay();
        Toast.makeText(this, "Cart cleared", Toast.LENGTH_SHORT).show();
    }

    private void initializeBottomNavigation() {
        try {
            BottomNavigationView bottomNavigationView = findViewById(R.id.bottomNav);
            if (bottomNavigationView != null) {
                bottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
                    @Override
                    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                        int itemId = item.getItemId();

                        if (itemId == R.id.homeBtn) {
                            Intent homeIntent = new Intent(StoreDetailActivity.this, HomeActivity.class);
                            homeIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(homeIntent);
                            return true;
                        } else if (itemId == R.id.searchbtn) {
                            Intent searchIntent = new Intent(StoreDetailActivity.this, FilterActivity.class);
                            searchIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(searchIntent);
                            return true;
                        } else if (itemId == R.id.accountbtn) {
                            Intent accountIntent = new Intent(StoreDetailActivity.this, SettingsActivity.class);
                            accountIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(accountIntent);
                            return true;
                        }
                        return false;
                    }
                });
            } else {
                Log.e(TAG, "Bottom navigation view not found");
            }
        } catch (Exception e) {
            Log.e(TAG, "Error initializing bottom navigation", e);
        }
    }

    private void loadStoreLogo(ImageView imageView) {
        try {
            String logoPath = store.getStoreLogo();
            if (logoPath != null && !logoPath.isEmpty()) {
                // Try to extract drawable name from path
                String drawableName = extractDrawableName(logoPath);
                if (drawableName != null) {
                    int resourceId = getResources().getIdentifier(
                            drawableName, "drawable", getPackageName());

                    if (resourceId != 0) {
                        imageView.setImageResource(resourceId);
                        return;
                    }
                }
            }
            // Set default image if no custom logo
            setDefaultLogo(imageView);
        } catch (Exception e) {
            Log.e(TAG, "Error loading store logo", e);
            setDefaultLogo(imageView);
        }
    }

    private void setDefaultLogo(ImageView imageView) {
        // Set default image based on category
        try {
            String category = store.getFoodCategory();
            if (category != null) {
                switch (category.toLowerCase()) {
                    case "pizzeria":
                    case "souvlaki_house":
                    case "vegancorner":
                    default:
                        imageView.setImageResource(android.R.drawable.ic_menu_gallery);
                        break;
                }
            } else {
                imageView.setImageResource(android.R.drawable.ic_menu_gallery);
            }
        } catch (Exception e) {
            Log.e(TAG, "Error setting default logo", e);
            imageView.setImageResource(android.R.drawable.ic_menu_gallery);
        }
    }

    private String extractDrawableName(String path) {
        if (path == null || path.isEmpty()) {
            return null;
        }

        try {
            String[] parts = path.split("[/\\\\]");
            if (parts.length > 0) {
                String fileName = parts[parts.length - 1];
                int dotIndex = fileName.lastIndexOf('.');
                if (dotIndex > 0) {
                    return fileName.substring(0, dotIndex);
                }
                return fileName;
            }
        } catch (Exception e) {
            Log.e(TAG, "Error extracting drawable name", e);
        }
        return null;
    }

    private void setupProductsSpinner(Spinner spinner) {
        try {
            List<Product> products = store.getProducts();
            if (products == null || products.isEmpty()) {
                // No products available
                List<String> emptyList = new ArrayList<>();
                emptyList.add("No products available");
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        this, android.R.layout.simple_spinner_item, emptyList);
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
                spinner.setAdapter(adapter);
                spinner.setEnabled(false);
                return;
            }

            // Create list of product names with prices
            List<String> productNames = new ArrayList<>();
            for (Product product : products) {
                String productInfo = String.format("%s - €%.2f (Available: %d)",
                        product.getProductName(),
                        product.getPrice(),
                        product.getAvailableAmount());
                productNames.add(productInfo);
            }

            ArrayAdapter<String> adapter = new ArrayAdapter<>(
                    this, android.R.layout.simple_spinner_item, productNames);
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            spinner.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up products spinner", e);
            Toast.makeText(this, "Error loading products", Toast.LENGTH_SHORT).show();
        }
    }

    private String extractProductName(String productInfo) {
        // Extract product name from "ProductName - €Price (Available: X)" format
        if (productInfo == null || productInfo.isEmpty()) {
            return "";
        }

        int dashIndex = productInfo.indexOf(" - €");
        if (dashIndex > 0) {
            return productInfo.substring(0, dashIndex);
        }
        return productInfo;
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