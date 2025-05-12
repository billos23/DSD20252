package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dsd20252.R;
import com.example.dsd20252.model.SearchRequest;

import java.util.ArrayList;
import java.util.List;

public class FilterActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_filter);

        EditText etLat          = findViewById(R.id.etLatitude);
        EditText etLon          = findViewById(R.id.etLongitude);
        SeekBar seekStars       = findViewById(R.id.seekStars);
        CheckBox cbDollar       = findViewById(R.id.cbDollar);
        CheckBox cbDoubleDollar = findViewById(R.id.cbDoubleDollar);
        CheckBox cbTripleDollar = findViewById(R.id.cbTripleDollar);
        CheckBox cbPizzeria     = findViewById(R.id.cbPizzeria);
        CheckBox cbsouvlaki_house     = findViewById(R.id.cbsouvlaki_house);
        CheckBox cbVeganCorner = findViewById(R.id.cbVeganCorner);
        Button btnSearch        = findViewById(R.id.btnSearch);
        Button btnSettings      = findViewById(R.id.btnSettings);

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
            } catch (NumberFormatException ex) {
                Toast.makeText(
                        this,
                        getString(R.string.err_invalid_latlon),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            int minStars = seekStars.getProgress() + 1;

            List<String> priceCats = new ArrayList<>();
            if (cbDollar.isChecked())       priceCats.add("$");
            if (cbDoubleDollar.isChecked()) priceCats.add("$$");
            if (cbTripleDollar.isChecked()) priceCats.add("$$$");
            if (priceCats.isEmpty()) {
                Toast.makeText(
                        this,
                        getString(R.string.err_select_price),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            List<String> foodCats = new ArrayList<>();
            if (cbPizzeria.isChecked())  foodCats.add("pizzeria");
            if (cbsouvlaki_house.isChecked())  foodCats.add("souvlaki_house");
            if (cbVeganCorner.isChecked())    foodCats.add("VeganCorner");
            if (foodCats.isEmpty()) {
                Toast.makeText(
                        this,
                        getString(R.string.err_select_category),
                        Toast.LENGTH_SHORT
                ).show();
                return;
            }

            SearchRequest req = new SearchRequest(
                    lat, lon, foodCats, minStars, priceCats
            );
            Intent intent = new Intent(this, ResultsActivity.class);
            intent.putExtra("searchReq", req);
            startActivity(intent);
        });

        btnSettings.setOnClickListener(v ->
                startActivity(new Intent(this, SettingsActivity.class))
        );
    }
}
