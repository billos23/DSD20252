package com.example.dsd20252.ui;

import android.os.Bundle;
import android.view.MenuItem;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dsd20252.R;
import com.example.dsd20252.model.BuyRequest;
import com.example.dsd20252.model.BuyResponse;
import com.example.dsd20252.model.Product;
import com.example.dsd20252.model.Store;
import com.example.dsd20252.network.NetworkClient;
import com.example.dsd20252.parser.StoreParser;

import org.json.JSONException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class StoreDetailActivity extends AppCompatActivity {
    private Store store;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_store_detail);

        // ─────────────────────────────────────────────────────────────
        // Load the sample JSON from assets/store.json and parse it
        try {
            InputStream is = getAssets().open("store.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            String json = new String(buffer, StandardCharsets.UTF_8);

            store = StoreParser.parseStoreFromJsonString(json);
        } catch (Exception e) {   // catch all exceptions including those from parser
            Toast.makeText(
                    this,
                    "Failed to load store data: " + e.getMessage(),
                    Toast.LENGTH_LONG
            ).show();
            finish();
            return;
        }
        // ─────────────────────────────────────────────────────────────

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.title_store_details);

        TextView tvName   = findViewById(R.id.tvDetailName);
        TextView tvCoords = findViewById(R.id.tvDetailCoords);
        Spinner spinner   = findViewById(R.id.spinnerProducts);
        EditText etQty    = findViewById(R.id.etQuantity);
        Button btnBuy     = findViewById(R.id.btnBuy);

        // Populate UI from parsed Store object
        tvName.setText(store.getStoreName());
        tvCoords.setText(
                String.format(
                        "%.6f, %.6f",
                        store.getLatitude(),
                        store.getLongitude()
                )
        );

        List<Product> products = store.getProducts();
        List<String> names = new ArrayList<>();
        for (Product p : products) {
            names.add(p.getProductName());
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this,
                android.R.layout.simple_spinner_item,
                names
        );
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        btnBuy.setOnClickListener(v -> {
            String selected = (String) spinner.getSelectedItem();
            int qty;
            try {
                qty = Integer.parseInt(etQty.getText().toString());
            } catch (NumberFormatException ex) {
                Toast.makeText(
                        this,
                        getString(R.string.err_invalid_qty),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            BuyRequest req = new BuyRequest(
                    store.getStoreName(),
                    selected,
                    qty
            );

            new Thread(() -> {
                try {
                    BuyResponse resp = new NetworkClient().buyProducts(req);
                    runOnUiThread(() -> {
                        String msg = resp.isSuccess()
                                ? getString(R.string.toast_buy_success)
                                : getString(R.string.toast_buy_failure);
                        Toast.makeText(
                                StoreDetailActivity.this,
                                msg,
                                Toast.LENGTH_LONG
                        ).show();
                    });
                } catch (Exception e) {
                    runOnUiThread(() -> Toast.makeText(
                            StoreDetailActivity.this,
                            getString(R.string.toast_buy_error, e.getMessage()),
                            Toast.LENGTH_LONG
                    ).show());
                }
            }).start();
        });
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
