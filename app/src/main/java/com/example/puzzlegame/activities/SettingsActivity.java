package com.example.puzzlegame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.puzzlegame.R;
import com.example.puzzlegame.utils.SessionManager;
import com.google.android.material.button.MaterialButton;

public class SettingsActivity extends AppCompatActivity {

    private SwitchCompat switchMusic;
    private SwitchCompat switchSfx;
    private RadioGroup rgDifficulty;
    private RadioButton rbEasy;
    private RadioButton rbMedium;
    private RadioButton rbHard;
    private MaterialButton btnLogout;
    private TextView tvCurrentUser;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        sessionManager = new SessionManager(this);

        // Initialize views - Now using SwitchCompat
        switchMusic = findViewById(R.id.switchMusic);
        switchSfx = findViewById(R.id.switchSfx);
        rgDifficulty = findViewById(R.id.rgDifficulty);
        rbEasy = findViewById(R.id.rbEasy);
        rbMedium = findViewById(R.id.rbMedium);
        rbHard = findViewById(R.id.rbHard);
        btnLogout = findViewById(R.id.btnLogout);
        tvCurrentUser = findViewById(R.id.tvCurrentUser);

        // Load current settings
        loadSettings();

        // ============ SET LISTENERS ============

        // Music toggle
        switchMusic.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setMusicEnabled(isChecked);
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(this, "Music " + status, Toast.LENGTH_SHORT).show();
        });

        // Sound effects toggle
        switchSfx.setOnCheckedChangeListener((buttonView, isChecked) -> {
            sessionManager.setSfxEnabled(isChecked);
            String status = isChecked ? "enabled" : "disabled";
            Toast.makeText(this, "Sound effects " + status, Toast.LENGTH_SHORT).show();
        });

        // Difficulty selection
        rgDifficulty.setOnCheckedChangeListener((group, checkedId) -> {
            int size = 4;
            String difficulty = "Medium (4x4)";

            if (checkedId == R.id.rbEasy) {
                size = 3;
                difficulty = "Easy (3x3)";
            } else if (checkedId == R.id.rbMedium) {
                size = 4;
                difficulty = "Medium (4x4)";
            } else if (checkedId == R.id.rbHard) {
                size = 5;
                difficulty = "Hard (5x5)";
            }

            sessionManager.setGridSize(size);
            Toast.makeText(this, "Difficulty changed to " + difficulty, Toast.LENGTH_SHORT).show();
        });

        // Logout button
        btnLogout.setOnClickListener(v -> {
            sessionManager.logout();
            Toast.makeText(this, "Logged out", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(SettingsActivity.this, LoginActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        // Display current user
        tvCurrentUser.setText("Logged in as: " + sessionManager.getUsername());
    }

    private void loadSettings() {
        switchMusic.setChecked(sessionManager.isMusicEnabled());
        switchSfx.setChecked(sessionManager.isSfxEnabled());

        int gridSize = sessionManager.getGridSize();
        if (gridSize == 3) {
            rbEasy.setChecked(true);
        } else if (gridSize == 4) {
            rbMedium.setChecked(true);
        } else if (gridSize == 5) {
            rbHard.setChecked(true);
        }
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        Intent intent = new Intent(SettingsActivity.this, GameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        startActivity(intent);
        finish();
    }
}