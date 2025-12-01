package com.example.bmifrontend;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

public class WelcomeActivity extends AppCompatActivity {

    private MaterialButton btnLogin, btnRegister;
    private TextView tvContinueAsGuest;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Check if user is already logged in
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();

        if (currentUser != null && !currentUser.isAnonymous()) {
            // User is already logged in, go to main activity
            navigateToMainActivity();
            return;
        }

        setContentView(R.layout.activity_welcome);

        initializeFirebase();
        initializeViews();
        setupListeners();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        btnLogin = findViewById(R.id.btnLogin);
        btnRegister = findViewById(R.id.btnRegister);
        tvContinueAsGuest = findViewById(R.id.tvContinueAsGuest);
    }

    private void setupListeners() {
        btnLogin.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, SigninActivity.class));
            finish(); // Close WelcomeActivity when going to login
        });

        btnRegister.setOnClickListener(v -> {
            startActivity(new Intent(WelcomeActivity.this, SignupActivity.class));
            finish(); // Close WelcomeActivity when going to register
        });

        tvContinueAsGuest.setOnClickListener(v -> showGuestLoginDialog());
    }

    private void showGuestLoginDialog() {
        new AlertDialog.Builder(this)
                .setTitle("Continue as Guest")
                .setMessage("As a guest, your data will not be saved. You can create an account later to save your progress.\n\nDo you want to continue?")
                .setPositiveButton("Continue", (dialog, which) -> loginAsGuest())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void loginAsGuest() {
        btnLogin.setEnabled(false);
        btnRegister.setEnabled(false);
        tvContinueAsGuest.setEnabled(false);

        mAuth.signInAnonymously()
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            saveGuestUserToFirestore(user);
                        }
                    } else {
                        btnLogin.setEnabled(true);
                        btnRegister.setEnabled(true);
                        tvContinueAsGuest.setEnabled(true);
                        Toast.makeText(WelcomeActivity.this,
                                "Failed to continue as guest: " + task.getException().getMessage(),
                                Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void saveGuestUserToFirestore(FirebaseUser user) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", "Guest");
        userData.put("email", "");
        userData.put("authProvider", "anonymous");
        userData.put("isGuest", true);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    Toast.makeText(WelcomeActivity.this, "Welcome, Guest!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    // Still navigate even if Firestore save fails
                    navigateToMainActivity();
                });
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(WelcomeActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onStart() {
        super.onStart();
        // Check if user logged in while app was in background
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser != null && !currentUser.isAnonymous()) {
            navigateToMainActivity();
        }
    }
}