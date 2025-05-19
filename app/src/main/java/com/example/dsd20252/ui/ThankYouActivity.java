package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dsd20252.R;

public class ThankYouActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        // Remove action bar for a cleaner look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Get data from intent
        String storeName = getIntent().getStringExtra("storeName");
        String orderDetails = getIntent().getStringExtra("orderDetails");

        // Initialize views
        TextView tvStoreName = findViewById(R.id.tvStoreName);
        TextView tvOrderDetails = findViewById(R.id.tvOrderDetails);
        Button btnReturnHome = findViewById(R.id.btnReturnHome);

        // Set store name if provided
        if (storeName != null && !storeName.isEmpty()) {
            tvStoreName.setText(storeName);
        }

        // Set order details if provided
        if (orderDetails != null && !orderDetails.isEmpty()) {
            tvOrderDetails.setText(orderDetails);
        } else {
            tvOrderDetails.setText("Your order has been processed");
        }

        // Return to Home button
        btnReturnHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(ThankYouActivity.this, HomeActivity.class);
            // Clear all activities and start fresh
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    // Override back button to prevent going back to the previous activity
    @Override
    public void onBackPressed() {
        // Redirect to home instead of going back
        Intent homeIntent = new Intent(ThankYouActivity.this, HomeActivity.class);
        homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(homeIntent);
        finish();
    }
}