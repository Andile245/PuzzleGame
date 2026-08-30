package com.example.puzzlegame.activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.puzzlegame.R;
import com.example.puzzlegame.database.DatabaseHelper;
import com.example.puzzlegame.models.User;
import com.example.puzzlegame.utils.SessionManager;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class LoginActivity extends AppCompatActivity {

    private TextInputEditText etUsername;
    private TextInputEditText etPassword;
    private MaterialButton btnLogin;
    private TextView tvRegisterLink;
    private TextView tvErrorMessage;
    private DatabaseHelper dbHelper;
    private SessionManager sessionManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        dbHelper = new DatabaseHelper(this);
        sessionManager = new SessionManager(this);

        if (sessionManager.isLoggedIn()) {
            goToGame();
            return;
        }

        etUsername = findViewById(R.id.etUsername);
        etPassword = findViewById(R.id.etPassword);
        btnLogin = findViewById(R.id.btnLogin);
        tvRegisterLink = findViewById(R.id.tvRegisterLink);
        tvErrorMessage = findViewById(R.id.tvErrorMessage);

        btnLogin.setOnClickListener(v -> loginUser());

        tvRegisterLink.setOnClickListener(v -> {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        });
    }

    private void loginUser() {
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (username.isEmpty() || password.isEmpty()) {
            showError("Please enter username and password");
            return;
        }

        User user = dbHelper.loginUser(username, password);

        if (user != null) {
            sessionManager.createLoginSession(user.getId(), user.getUsername());
            Toast.makeText(this, "Welcome " + username + "!", Toast.LENGTH_SHORT).show();
            goToGame();
        } else {
            showError("Invalid username or password");
        }
    }

    private void showError(String message) {
        tvErrorMessage.setText(message);
        tvErrorMessage.setVisibility(View.VISIBLE);
    }

    private void goToGame() {
        Intent intent = new Intent(LoginActivity.this, GameActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}