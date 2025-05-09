package com.example.dsd20252.ui;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.dsd20252.R;

public class SettingsActivity extends AppCompatActivity {
    public static final String PREFS_NAME = "com.example.dsd20252.PREFERENCES";
    public static final String KEY_HOST   = "pref_master_host";
    public static final String KEY_PORT   = "pref_master_port";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        setTitle(R.string.btn_settings);

        EditText etHost = findViewById(R.id.etHost);
        EditText etPort = findViewById(R.id.etPort);
        Button btnSave  = findViewById(R.id.btnSaveSettings);

        SharedPreferences prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE);
        String host = prefs.getString(KEY_HOST, "10.0.2.2");
        int port    = prefs.getInt(KEY_PORT, 5555);

        etHost.setText(host);
        etPort.setText(String.valueOf(port));

        btnSave.setOnClickListener(v -> {
            String newHost = etHost.getText().toString().trim();
            String portStr = etPort.getText().toString().trim();
            int newPort;
            try {
                newPort = Integer.parseInt(portStr);
            } catch (NumberFormatException e) {
                Toast.makeText(this, getString(R.string.err_invalid_port), Toast.LENGTH_SHORT).show();
                return;
            }

            prefs.edit()
                    .putString(KEY_HOST, newHost)
                    .putInt(KEY_PORT, newPort)
                    .apply();

            Toast.makeText(this, getString(R.string.toast_settings_saved), Toast.LENGTH_SHORT).show();
            finish();
        });
    }
}
