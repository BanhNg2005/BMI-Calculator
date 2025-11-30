package com.example.bmifrontend;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Patterns;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.facebook.AccessToken;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.android.material.button.MaterialButton;
import com.google.android.material.card.MaterialCardView;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {

    private static final int RC_GOOGLE_SIGN_IN = 9001;

    // UI Components
    private TextInputLayout tilUsername, tilEmail, tilPassword, tilConfirmPassword;
    private TextInputEditText etUsername, etEmail, etPassword, etConfirmPassword;
    private MaterialButton btnRegister;
    private ProgressBar progressBar;
    private MaterialCardView btnGoogleLogin, btnFacebookLogin;
    private TextView tvLoginNow;
    private ImageButton btnBack;

    // Firebase
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;
    private GoogleSignInClient mGoogleSignInClient;
    private CallbackManager mCallbackManager;

    // Password validation patterns
    private static final Pattern UPPERCASE_PATTERN = Pattern.compile("[A-Z]");
    private static final Pattern DIGIT_PATTERN = Pattern.compile("[0-9]");
    private static final Pattern SPECIAL_CHAR_PATTERN = Pattern.compile("[!@#$%^&*(),.?\":{}|<>]");

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        initializeFirebase();
        initializeViews();
        setupListeners();
        setupGoogleSignIn();
        setupFacebookSignIn();
    }

    private void initializeFirebase() {
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();
    }

    private void initializeViews() {
        tilUsername = findViewById(R.id.tilUsername);
        tilEmail = findViewById(R.id.tilEmail);
        tilPassword = findViewById(R.id.tilPassword);
        tilConfirmPassword = findViewById(R.id.tilConfirmPassword);
        etUsername = findViewById(R.id.etUsername);
        etEmail = findViewById(R.id.etEmail);
        etPassword = findViewById(R.id.etPassword);
        etConfirmPassword = findViewById(R.id.etConfirmPassword);
        btnRegister = findViewById(R.id.btnRegister);
        progressBar = findViewById(R.id.progressBar);
        btnGoogleLogin = findViewById(R.id.btnGoogleLogin);
        btnFacebookLogin = findViewById(R.id.btnFacebookLogin);
        tvLoginNow = findViewById(R.id.tvLoginNow);
        btnBack = findViewById(R.id.btnBack);
    }

    private void setupListeners() {
        btnRegister.setOnClickListener(v -> registerWithEmail());

        tvLoginNow.setOnClickListener(v -> {
            startActivity(new Intent(SignupActivity.this, SigninActivity.class));
            finish();
        });

        btnBack.setOnClickListener(v -> finish());

        btnGoogleLogin.setOnClickListener(v -> signInWithGoogle());

        btnFacebookLogin.setOnClickListener(v -> signInWithFacebook());

        // Clear errors on text change
        etUsername.addTextChangedListener(createTextWatcher(tilUsername));
        etEmail.addTextChangedListener(createTextWatcher(tilEmail));
        etPassword.addTextChangedListener(createTextWatcher(tilPassword));
        etConfirmPassword.addTextChangedListener(createTextWatcher(tilConfirmPassword));
    }

    private TextWatcher createTextWatcher(TextInputLayout textInputLayout) {
        return new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                textInputLayout.setError(null);
            }

            @Override
            public void afterTextChanged(Editable s) {}
        };
    }

    private void registerWithEmail() {
        if (etUsername == null || etEmail == null || etPassword == null || etConfirmPassword == null) {
            Toast.makeText(this, "Form initialization error", Toast.LENGTH_SHORT).show();
            return;
        }

        String username = etUsername.getText() != null ? etUsername.getText().toString().trim() : "";
        String email = etEmail.getText() != null ? etEmail.getText().toString().trim() : "";
        String password = etPassword.getText() != null ? etPassword.getText().toString().trim() : "";
        String confirmPassword = etConfirmPassword.getText() != null ? etConfirmPassword.getText().toString().trim() : "";

        // Validate all fields
        if (!validateUsername(username)) return;
        if (!validateEmail(email)) return;
        if (!validatePassword(password)) return;
        if (!validateConfirmPassword(password, confirmPassword)) return;

        // Show progress
        showLoading(true);

        // Create user account
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this, task -> {
                    if (task.isSuccessful()) {
                        // Registration success
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            updateUserProfile(user, username);
                        } else {
                            showLoading(false);
                            Toast.makeText(SignupActivity.this, "Registration failed: User is null", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showLoading(false);
                        handleRegistrationError(task.getException());
                    }
                });
    }

    private boolean validateUsername(String username) {
        if (username.isEmpty()) {
            tilUsername.setError("Username is required");
            etUsername.requestFocus();
            return false;
        }

        if (username.length() < 3) {
            tilUsername.setError("Username must be at least 3 characters");
            etUsername.requestFocus();
            return false;
        }

        tilUsername.setError(null);
        return true;
    }

    private boolean validateEmail(String email) {
        if (email.isEmpty()) {
            tilEmail.setError("Email is required");
            etEmail.requestFocus();
            return false;
        }

        if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            tilEmail.setError("Please enter a valid email address");
            etEmail.requestFocus();
            return false;
        }

        tilEmail.setError(null);
        return true;
    }

    private boolean validatePassword(String password) {
        if (password.isEmpty()) {
            tilPassword.setError("Password is required");
            etPassword.requestFocus();
            return false;
        }

        if (password.length() < 8) {
            tilPassword.setError("Password must be at least 8 characters");
            etPassword.requestFocus();
            return false;
        }

        if (!UPPERCASE_PATTERN.matcher(password).find()) {
            tilPassword.setError("Password must contain at least 1 uppercase letter");
            etPassword.requestFocus();
            return false;
        }

        if (!DIGIT_PATTERN.matcher(password).find()) {
            tilPassword.setError("Password must contain at least 1 number");
            etPassword.requestFocus();
            return false;
        }

        if (!SPECIAL_CHAR_PATTERN.matcher(password).find()) {
            tilPassword.setError("Password must contain at least 1 special character");
            etPassword.requestFocus();
            return false;
        }

        tilPassword.setError(null);
        return true;
    }

    private boolean validateConfirmPassword(String password, String confirmPassword) {
        if (confirmPassword.isEmpty()) {
            tilConfirmPassword.setError("Please confirm your password");
            etConfirmPassword.requestFocus();
            return false;
        }

        if (!password.equals(confirmPassword)) {
            tilConfirmPassword.setError("Passwords do not match");
            etConfirmPassword.requestFocus();
            return false;
        }

        tilConfirmPassword.setError(null);
        return true;
    }

    private void handleRegistrationError(Exception exception) {
        if (exception instanceof FirebaseAuthUserCollisionException) {
            tilEmail.setError("This email is already registered");
            etEmail.requestFocus();
        } else {
            Toast.makeText(this, "Registration failed: " + exception.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void updateUserProfile(FirebaseUser user, String username) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        user.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        saveUserToFirestore(user, username, "email");
                    } else {
                        showLoading(false);
                        String errorMsg = "Failed to update profile";
                        if (task.getException() != null) {
                            errorMsg += ": " + task.getException().getMessage();
                        }
                        Toast.makeText(SignupActivity.this, errorMsg, Toast.LENGTH_LONG).show();
                        // Still save to Firestore even if profile update fails
                        saveUserToFirestore(user, username, "email");
                    }
                });
    }

    private void saveUserToFirestore(FirebaseUser user, String username, String authProvider) {
        Map<String, Object> userData = new HashMap<>();
        userData.put("username", username);
        userData.put("email", user.getEmail());
        userData.put("authProvider", authProvider);
        userData.put("isGuest", false);
        userData.put("createdAt", System.currentTimeMillis());

        db.collection("users").document(user.getUid())
                .set(userData)
                .addOnSuccessListener(aVoid -> {
                    showLoading(false);
                    Toast.makeText(SignupActivity.this, "Registration successful!", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                })
                .addOnFailureListener(e -> {
                    showLoading(false);
                    Toast.makeText(SignupActivity.this, "Failed to save user data", Toast.LENGTH_SHORT).show();
                    navigateToMainActivity();
                });
    }

    private void setupGoogleSignIn() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken(getString(R.string.default_web_client_id))
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signInWithGoogle() {
        if (mGoogleSignInClient == null) {
            Toast.makeText(this, "Google Sign-In not configured properly", Toast.LENGTH_SHORT).show();
            return;
        }
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_GOOGLE_SIGN_IN);
    }

    private void setupFacebookSignIn() {
        try {
            mCallbackManager = CallbackManager.Factory.create();

            LoginManager.getInstance().registerCallback(mCallbackManager,
                    new FacebookCallback<LoginResult>() {
                        @Override
                        public void onSuccess(LoginResult loginResult) {
                            handleFacebookAccessToken(loginResult.getAccessToken());
                        }

                        @Override
                        public void onCancel() {
                            Toast.makeText(SignupActivity.this, "Facebook registration cancelled", Toast.LENGTH_SHORT).show();
                        }

                        @Override
                        public void onError(FacebookException error) {
                            Toast.makeText(SignupActivity.this, "Facebook registration failed: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        } catch (Exception e) {
            Toast.makeText(this, "Facebook Sign-In setup failed: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }
    }

    private void signInWithFacebook() {
        LoginManager.getInstance().logInWithReadPermissions(this, Arrays.asList("email", "public_profile"));
    }

    private void handleFacebookAccessToken(AccessToken token) {
        showLoading(true);
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String username = user.getDisplayName() != null ? user.getDisplayName() : "";
                            saveUserToFirestore(user, username, "facebook");
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Facebook authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        // Google Sign-In result
        if (requestCode == RC_GOOGLE_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            try {
                GoogleSignInAccount account = task.getResult(ApiException.class);
                firebaseAuthWithGoogle(account.getIdToken());
            } catch (ApiException e) {
                Toast.makeText(this, "Google sign in failed: " + e.getMessage(), Toast.LENGTH_SHORT).show();
            }
        }

        // Facebook Sign-In result
        mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void firebaseAuthWithGoogle(String idToken) {
        showLoading(true);
        AuthCredential credential = GoogleAuthProvider.getCredential(idToken, null);

        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, task -> {
                    showLoading(false);
                    if (task.isSuccessful()) {
                        FirebaseUser user = mAuth.getCurrentUser();
                        if (user != null) {
                            String username = user.getDisplayName() != null ? user.getDisplayName() : "";
                            saveUserToFirestore(user, username, "google");
                        }
                    } else {
                        Toast.makeText(SignupActivity.this, "Google authentication failed", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    private void showLoading(boolean show) {
        progressBar.setVisibility(show ? View.VISIBLE : View.GONE);
        btnRegister.setEnabled(!show);
        btnGoogleLogin.setEnabled(!show);
        btnFacebookLogin.setEnabled(!show);
    }

    private void navigateToMainActivity() {
        Intent intent = new Intent(SignupActivity.this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}