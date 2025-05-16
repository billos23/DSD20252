package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsd20252.R;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarView;

import java.util.ArrayList;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private TextView titleTextView;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;
    private Button btnContinue;
    private MenuItem homeBtn;
    private MenuItem searchbtn;
    private MenuItem accountbtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the main home screen layout
        setContentView(R.layout.activity_homescreen);

        // Bind views
        titleTextView = findViewById(R.id.title);
        recyclerView = findViewById(R.id.recyclerView);
        bottomNav = findViewById(R.id.bottomNav);
        btnContinue = findViewById(R.id.btnContinue);

        // Access menu items - the proper way
        Menu menu = bottomNav.getMenu();
        homeBtn = menu.findItem(R.id.homeBtn);
        searchbtn = menu.findItem(R.id.searchbtn);
        accountbtn = menu.findItem(R.id.accountbtn);

        // Set the title text
        titleTextView.setText("Foodie, the food that comes to you.");

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> items = new ArrayList<>();
        items.add("Home Item 1");
        items.add("Home Item 2");
        items.add("Home Item 3");

        // Create and set the adapter for RecyclerView
        // SimpleAdapter adapter = new SimpleAdapter(items);
        // recyclerView.setAdapter(adapter);

        // Set click listener for the Continue button to navigate to FilterActivity
        btnContinue.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Create an intent to navigate to the activity that uses activity_filter.xml
                Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
                startActivity(intent);
            }
        });

        // Set up navigation listener
        bottomNav.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int itemId = item.getItemId();

                if (itemId == R.id.homeBtn) {
                    // Already on home screen, do nothing or refresh
                    return true;
                } else if (itemId == R.id.searchbtn) {
                    // Navigate to search screen
                    Intent intent = new Intent(HomeActivity.this, FilterActivity.class);
                    startActivity(intent);
                    return true;
                } else if (itemId == R.id.accountbtn) {
                    // Navigate to account screen
                    Intent intent = new Intent(HomeActivity.this, SettingsActivity.class);
                    startActivity(intent);
                    return true;
                }

                return false;
            }
        });
    }
}