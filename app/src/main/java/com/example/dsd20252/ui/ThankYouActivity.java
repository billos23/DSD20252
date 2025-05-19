package com.example.dsd20252.ui;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dsd20252.R;

public class ThankYouActivity extends AppCompatActivity {
    private int selectedRating = 0;
    private ImageView[] stars;
    private String storeName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_thank_you);

        // Remove action bar for a cleaner look
        if (getSupportActionBar() != null) {
            getSupportActionBar().hide();
        }

        // Get data from intent
        storeName = getIntent().getStringExtra("storeName");
        String orderDetails = getIntent().getStringExtra("orderDetails");

        // Initialize views
        TextView tvStoreName = findViewById(R.id.tvStoreName);
        TextView tvOrderDetails = findViewById(R.id.tvOrderDetails);
        TextView tvRatingPrompt = findViewById(R.id.tvRatingPrompt);
        Button btnReturnHome = findViewById(R.id.btnReturnHome);
        Button btnSubmitRating = findViewById(R.id.btnSubmitRating);

        // Initialize star rating views
        stars = new ImageView[5];
        stars[0] = findViewById(R.id.star1);
        stars[1] = findViewById(R.id.star2);
        stars[2] = findViewById(R.id.star3);
        stars[3] = findViewById(R.id.star4);
        stars[4] = findViewById(R.id.star5);

        // Set store name if provided
        if (storeName != null && !storeName.isEmpty()) {
            tvStoreName.setText(storeName);
            tvRatingPrompt.setText("How was your experience with " + storeName + "?");
        } else {
            tvRatingPrompt.setText("How was your experience?");
        }

        // Set order details if provided
        if (orderDetails != null && !orderDetails.isEmpty()) {
            tvOrderDetails.setText(orderDetails);
        } else {
            tvOrderDetails.setText("Your order has been processed");
        }

        // Set up star click listeners
        setupStarRating();

        // Submit rating button
        btnSubmitRating.setOnClickListener(v -> {
            if (selectedRating > 0) {
                submitRating();
            } else {
                Toast.makeText(this, "Please select a rating before submitting", Toast.LENGTH_SHORT).show();
            }
        });

        // Return to Home button
        btnReturnHome.setOnClickListener(v -> {
            Intent homeIntent = new Intent(ThankYouActivity.this, HomeActivity.class);
            // Clear all activities and start fresh
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        });
    }

    private void setupStarRating() {
        for (int i = 0; i < stars.length; i++) {
            final int rating = i + 1;
            stars[i].setOnClickListener(v -> {
                selectedRating = rating;
                updateStarDisplay();
            });
        }

        // Initialize with empty stars
        updateStarDisplay();
    }

    private void updateStarDisplay() {
        for (int i = 0; i < stars.length; i++) {
            if (i < selectedRating) {
                // Filled star
                stars[i].setImageResource(android.R.drawable.btn_star_big_on);
            } else {
                // Empty star
                stars[i].setImageResource(android.R.drawable.btn_star_big_off);
            }
        }
    }

    private void submitRating() {
        // Here you could send the rating to your server
        // For now, we'll just show a toast and then go home

        String ratingText = selectedRating == 1 ? "star" : "stars";
        String message = "Thank you for rating " +
                (storeName != null ? storeName : "us") +
                " " + selectedRating + " " + ratingText + "!";

        Toast.makeText(this, message, Toast.LENGTH_LONG).show();

        // Optional: Save rating to SharedPreferences or send to server
        // saveRatingLocally(storeName, selectedRating);

        // Return to home after a short delay
        new android.os.Handler().postDelayed(() -> {
            Intent homeIntent = new Intent(ThankYouActivity.this, HomeActivity.class);
            homeIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(homeIntent);
            finish();
        }, 1500); // 1.5 second delay to show the thank you message
    }

    // Optional method to save rating locally
    private void saveRatingLocally(String storeName, int rating) {
        getSharedPreferences("store_ratings", MODE_PRIVATE)
                .edit()
                .putInt(storeName != null ? storeName : "unknown_store", rating)
                .apply();
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