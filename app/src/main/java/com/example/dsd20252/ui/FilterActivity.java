package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.example.dsd20252.R;
import com.example.dsd20252.model.SearchRequest;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {
    private BottomNavigationView bottomNav;
    private MenuItem homeBtn;
    private MenuItem searchbtn;
    private MenuItem accountbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        // Filter components
        EditText etLat          = findViewById(R.id.etLatitude);
        EditText etLon          = findViewById(R.id.etLongitude);
        SeekBar seekStars       = findViewById(R.id.seekStars);
        CheckBox cbDollar       = findViewById(R.id.cbDollar);
        CheckBox cbDoubleDollar = findViewById(R.id.cbDoubleDollar);
        CheckBox cbTripleDollar = findViewById(R.id.cbTripleDollar);
        CheckBox cbPizzeria     = findViewById(R.id.cbPizzeria);
        CheckBox cbsouvlaki_house = findViewById(R.id.cbsouvlaki_house);
        CheckBox cbVeganCorner = findViewById(R.id.cbVeganCorner);
        Button btnSearch        = findViewById(R.id.btnSearch);
        Button btnSettings      = findViewById(R.id.btnSettings);

        // Initialize navigation
        bottomNav = findViewById(R.id.bottomNav);

        // Access menu items
        Menu menu = bottomNav.getMenu();
        homeBtn = menu.findItem(R.id.homeBtn);
        searchbtn = menu.findItem(R.id.searchbtn);
        accountbtn = menu.findItem(R.id.accountbtn);

        // Set the search tab as selected (since this is a filter/search activity)
        bottomNav.setSelectedItemId(R.id.searchbtn);

        // Set up navigation listener
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.homeBtn) {
                    // Navigate to home screen
                    Intent intent = new Intent(FilterActivity.this, HomeActivity.class);
                    // Clear back stack when going to home
                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.searchbtn) {
                    // Already on search/filter screen
                    return true;
                } else if (itemId == R.id.accountbtn) {
                    // Navigate to account screen
                    Intent intent = new Intent(FilterActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });

        btnSearch.setOnClickListener(v -> {
            double lat, lon;
            try {
                String latStr = etLat.getText().toString().trim();
                String lonStr = etLon.getText().toString().trim();
                if (latStr.isEmpty() || lonStr.isEmpty()) {
                    throw new NumberFormatException();
                }
                lat = Double.parseDouble(latStr);
                lon = Double.parseDouble(lonStr);

                // Validate coordinates
                if (lat < -90 || lat > 90 || lon < -180 || lon > 180) {
                    Toast.makeText(this, "Invalid coordinates. Latitude: -90 to 90, Longitude: -180 to 180",
                            Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException ex) {
                Toast.makeText(
                        this,
                        getString(R.string.err_invalid_latlon),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            int minStars = seekStars.getProgress() + 1;

            List<String> priceCategories = new ArrayList<>();
            if (cbDollar.isChecked())       priceCategories.add("$");
            if (cbDoubleDollar.isChecked()) priceCategories.add("$$");
            if (cbTripleDollar.isChecked()) priceCategories.add("$$$");
            if (priceCategories.isEmpty()) {
                Toast.makeText(
                        this,
                        getString(R.string.err_select_price),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Using normalized category names that match StoreParser
            List<String> foodCats = new ArrayList<>();
            if (cbPizzeria.isChecked())     foodCats.add("pizzeria");
            if (cbsouvlaki_house.isChecked()) foodCats.add("souvlaki_house");
            if (cbVeganCorner.isChecked())    foodCats.add("VeganCorner");
            if (foodCats.isEmpty()) {
                Toast.makeText(
                        this,
                        getString(R.string.err_select_category),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            // Create search request according to assignment requirements
            SearchRequest req = new SearchRequest(
                    lat, lon, foodCats, minStars, priceCategories
            );

            Toast.makeText(this, "Searching stores within 5km radius...", Toast.LENGTH_SHORT).show();

            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("searchReq", req);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v -> {
            Intent intent = new Intent(FilterActivity.this, SettingsActivity.class);
            startActivity(intent);
        });
    }
}