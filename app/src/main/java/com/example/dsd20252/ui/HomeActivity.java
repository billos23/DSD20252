package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.dsd20252.R;
import com.example.dsd20252.model.Product;
import com.example.dsd20252.model.Store;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class HomeActivity extends AppCompatActivity {
    private TextView titleTextView;
    private RecyclerView recyclerView;
    private BottomNavigationView bottomNav;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate the main home screen layout
        setContentView(R.layout.activity_homescreen);

        // Bind views
        titleTextView = findViewById(R.id.title);
        recyclerView  = findViewById(R.id.recyclerView);
        bottomNav     = findViewById(R.id.bottomNav);

        // Set the title text
        titleTextView.setText("Foodie, the food that comes to you.");

        // RecyclerView setup
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        List<String> items = new ArrayList<>();
        items.add("Home Item 1");
        items.add("Home Item 2");
        items.add("Home Item 3");





        // BottomNavigationView item selection handling

    }

    // TODO: Define YourAdapter as a nested class or in its own file
    // public class YourAdapter extends RecyclerView.Adapter<YourAdapter.ViewHolder> { ... }
}
