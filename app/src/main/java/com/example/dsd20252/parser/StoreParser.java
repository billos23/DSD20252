package com.example.dsd20252.parser;

import com.example.dsd20252.model.Product;
import com.example.dsd20252.model.Store;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class StoreParser {
    /**
     * Pure JSON-to-Store conversion:
     * call this with the full JSON payload.
     */
    public static Store parseStoreFromJsonString(String json) throws Exception {
        JSONObject obj = new JSONObject(json);
        Store store = new Store();

        store.setStoreName(obj.getString("StoreName"));
        store.setLatitude(obj.getDouble("Latitude"));
        store.setLongitude(obj.getDouble("Longitude"));
        store.setFoodCategory(obj.getString("FoodCategory"));
        store.setStars(obj.getInt("Stars"));
        store.setNoOfVotes(obj.getInt("NoOfVotes"));
        store.setStoreLogo(obj.getString("StoreLogo"));

        JSONArray productsJson = obj.getJSONArray("Products");
        List<Product> products = new ArrayList<>();
        double sum = 0;
        for (int i = 0; i < productsJson.length(); i++) {
            JSONObject p = productsJson.getJSONObject(i);
            Product product = new Product();
            product.setProductName(p.getString("ProductName"));
            product.setProductType(p.getString("ProductType"));
            product.setAvailableAmount(p.getInt("Available Amount"));
            product.setPrice(p.getDouble("Price"));
            sum += product.getPrice();
            products.add(product);
        }
        store.setProducts(products);

        // compute price category
        double avg = sum / products.size();
        if      (avg <=  5) store.setPriceCategory("$");
        else if (avg <= 15) store.setPriceCategory("$$");
        else                 store.setPriceCategory("$$$");

        return store;
    }
}
