package com.example.dsd20252.parser;

import android.util.Log;

import com.example.dsd20252.model.Product;
import com.example.dsd20252.model.Store;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StoreParser {
    private static final String TAG = "StoreParser";

    /**
     * Pure JSON-to-Store conversion:
     * call this with the full JSON payload.
     */
    public static Store parseStoreFromJsonString(String json) {
        try {
            JSONObject obj = new JSONObject(json);
            Store store = new Store();

            // Basic store properties
            store.setStoreName(obj.optString("StoreName", "Unknown Store"));
            store.setLatitude(obj.optDouble("Latitude", 0));
            store.setLongitude(obj.optDouble("Longitude", 0));

            // Handle food category with normalization
            String rawCategory = obj.optString("FoodCategory", "");
            String normalizedCategory = normalizeCategory(rawCategory);
            store.setFoodCategory(normalizedCategory);

            store.setStars(obj.optInt("Stars", 0));
            store.setNoOfVotes(obj.optInt("NoOfVotes", 0));
            store.setStoreLogo(obj.optString("StoreLogo", ""));

            // Parse products if available
            List<Product> products = new ArrayList<>();
            double sum = 0;

            if (obj.has("Products") && !obj.isNull("Products")) {
                JSONArray productsJson = obj.getJSONArray("Products");

                for (int i = 0; i < productsJson.length(); i++) {
                    try {
                        JSONObject p = productsJson.getJSONObject(i);
                        Product product = new Product();

                        product.setProductName(p.optString("ProductName", "Unknown Product"));
                        product.setProductType(p.optString("ProductType", ""));

                        // Handle field name variations for Available Amount
                        if (p.has("Available Amount")) {
                            product.setAvailableAmount(p.optInt("Available Amount", 0));
                        } else if (p.has("AvailableAmount")) {
                            product.setAvailableAmount(p.optInt("AvailableAmount", 0));
                        } else {
                            product.setAvailableAmount(0);
                        }

                        product.setPrice(p.optDouble("Price", 0));
                        sum += product.getPrice();

                        // Set optional fields with defaults
                        product.setSoldAmount(p.optInt("SoldAmount", 0));
                        product.setInitialAmount(p.optInt("InitialAmount", product.getAvailableAmount()));

                        products.add(product);
                    } catch (Exception e) {
                        Log.e(TAG, "Error parsing product: " + e.getMessage());
                    }
                }
            }

            store.setProducts(products);

            // Compute price category based on average product price
            if (!products.isEmpty()) {
                double avg = sum / products.size();
                if (avg <= 5) store.setPriceCategory("$");
                else if (avg <= 15) store.setPriceCategory("$$");
                else store.setPriceCategory("$$$");
            } else {
                // Default price category if no products
                store.setPriceCategory("$");
            }

            return store;
        } catch (JSONException e) {
            Log.e(TAG, "Error parsing store JSON: " + e.getMessage());
            return null;
        } catch (Exception e) {
            Log.e(TAG, "Unexpected error parsing store: " + e.getMessage());
            return null;
        }
    }

    /**
     * Normalizes category names to match filter categories
     */
    private static String normalizeCategory(String rawCategory) {
        if (rawCategory == null || rawCategory.isEmpty()) {
            return "";
        }

        String lowerCategory = rawCategory.toLowerCase();

        if (lowerCategory.equals("pizzeria") || lowerCategory.contains("pizza")) {
            return "pizzeria";
        } else if (lowerCategory.equals("souvlaki_house") || lowerCategory.contains("souvlaki")) {
            return "souvlaki_house";
        } else if (lowerCategory.equals("vegancorner") || lowerCategory.contains("vegan")) {
            return "VeganCorner";
        }

        // Return the original if no match
        return rawCategory;
    }
}