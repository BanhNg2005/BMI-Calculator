package com.example.bmifrontend;

import android.animation.ObjectAnimator;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.content.SharedPreferences;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import Model.User;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvBmiCategory, tvResultVal;
    TextInputEditText etAge, etWeight, etHeight;
    Button btnCalc, btnBmiGoal;
    ImageButton imgMan, imgWoman;
    MaterialButton btnSettings, btnProfile;
    BMICustomProgressBar pbHealth;
    private String selectedGender = null;

    // Goal tracking variables
    private float goalBmi = 0f;
    private float startBmi = 0f;
    private float currentBmi = 0f;
    private float currentWeight = 0f;
    private float currentHeight = 0f;

    // Firebase
    private FirebaseHelper firebaseHelper;
    private FirebaseAuth firebaseAuth;
    private boolean isLoadingData = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // Apply saved theme preference before setting content view
        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("pref_dark_mode", false);
        AppCompatDelegate.setDefaultNightMode(
                darkMode ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
        );

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Initialize Firebase
        firebaseHelper = new FirebaseHelper();
        firebaseAuth = FirebaseAuth.getInstance();

        initialize();
        updateGenderSelection();

        // Load user data if logged in
        loadUserData();
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser != null) {
            // User is logged in, load their data
            isLoadingData = true;
            showLoadingState(true);

            firebaseHelper.getUserData(new FirebaseHelper.OnUserDataListener() {
                @Override
                public void onSuccess(User user) {
                    isLoadingData = false;
                    showLoadingState(false);
                    populateUserData(user);
                }

                @Override
                public void onFailure(String error) {
                    isLoadingData = false;
                    showLoadingState(false);
                    // User might be new, that's okay
                    Snackbar.make(findViewById(R.id.main),
                            "Welcome! Enter your details to get started.",
                            Snackbar.LENGTH_SHORT).show();
                }
            });
        } else {
            // User not logged in - guest mode
            Snackbar.make(findViewById(R.id.main),
                    "You're using guest mode. Sign in to save your data.",
                    Snackbar.LENGTH_LONG).show();
        }
    }

    private void populateUserData(User user) {
        if (user == null) return;

        // Populate personal info
        if (user.getPersonalInfo() != null) {
            if (user.getPersonalInfo().getAge() > 0) {
                etAge.setText(String.valueOf(user.getPersonalInfo().getAge()));
            }

            if (user.getPersonalInfo().getCurrentWeight() > 0) {
                etWeight.setText(String.valueOf(user.getPersonalInfo().getCurrentWeight()));
            }

            if (user.getPersonalInfo().getHeight() > 0) {
                etHeight.setText(String.valueOf((int) user.getPersonalInfo().getHeight()));
            }

            if (user.getPersonalInfo().getGender() != null) {
                selectedGender = user.getPersonalInfo().getGender();
                updateGenderSelection();
            }

            // Load goal BMI if exists
            if (user.getPersonalInfo().getGoalBmi() > 0) {
                goalBmi = user.getPersonalInfo().getGoalBmi();
            }
        }

//        // Auto-calculate BMI if we have all the data
            if (etAge.getText() != null && !etAge.getText().toString().isEmpty() &&
                    etWeight.getText() != null && !etWeight.getText().toString().isEmpty() &&
                    etHeight.getText() != null && !etHeight.getText().toString().isEmpty() &&
                    selectedGender != null) {
                calculateBMI(false); // Don't save to Firebase on load
            }

        Snackbar.make(findViewById(R.id.main),
                "Welcome back! Let's continue your health journey.",
                Snackbar.LENGTH_SHORT).show();
    }

    private void showLoadingState(boolean loading) {
        btnCalc.setEnabled(!loading);
        btnBmiGoal.setEnabled(!loading);
        btnCalc.setText(loading ? "Loading..." : getString(R.string.calculate));
    }

    private void updateGenderSelection() {
        int selectedColor = getResources().getColor(R.color.genderSelected, getTheme());
        int defaultColor = getResources().getColor(R.color.genderBackground, getTheme());

        if ("male".equals(selectedGender)) {
            imgMan.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            imgWoman.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            imgMan.setContentDescription("Male selected");
            imgWoman.setContentDescription("Female not selected");
        } else if ("female".equals(selectedGender)) {
            imgWoman.setBackgroundTintList(ColorStateList.valueOf(selectedColor));
            imgMan.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            imgWoman.setContentDescription("Female selected");
            imgMan.setContentDescription("Male not selected");
        } else {
            imgMan.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            imgWoman.setBackgroundTintList(ColorStateList.valueOf(defaultColor));
            imgMan.setContentDescription("Male not selected");
            imgWoman.setContentDescription("Female not selected");
        }
    }

    private void initialize() {
        tvBmiCategory = findViewById(R.id.tvBmiCategory);
        tvResultVal = findViewById(R.id.tvResultVal);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        btnCalc = findViewById(R.id.btnCalc);
        btnBmiGoal = findViewById(R.id.btnBmiGoal);
        imgMan = findViewById(R.id.imgMan);
        imgWoman = findViewById(R.id.imgWoman);
        btnSettings = findViewById(R.id.btnSettings);
        btnProfile = findViewById(R.id.btnProfile);
        pbHealth = findViewById(R.id.pbHealth);

        btnCalc.setOnClickListener(this);
        btnBmiGoal.setOnClickListener(this);
        imgMan.setOnClickListener(this);
        imgWoman.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnProfile.setOnClickListener(this);

        imgMan.setFocusable(true);
        imgWoman.setFocusable(true);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnCalc) {
            calculateBMI();
        }
        if (id == R.id.btnBmiGoal) {
            showBmiGoalDialog();
        }
        if (id == R.id.btnSettings) {
            showSettingsDialog();
        }
        if (id == R.id.btnProfile) {
            showProfileMenu();
        }
        if (id == R.id.imgMan) {
            selectedGender = "male";
            updateGenderSelection();
            savePersonalInfoToFirebase();
        }
        if (id == R.id.imgWoman) {
            selectedGender = "female";
            updateGenderSelection();
            savePersonalInfoToFirebase();
        }
    }
    private void calculateBMI() {
        calculateBMI(true); // Save to Firebase by default
    }
    private void calculateBMI(boolean saveToFirebase) {
        if (etWeight.getText().toString().isEmpty() || etHeight.getText().toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Please enter weight and height!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (etAge.getText().toString().isEmpty()) {
            Snackbar.make(findViewById(R.id.main), "Please enter age!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (selectedGender == null) {
            Snackbar.make(findViewById(R.id.main), "Please select gender!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (etHeight.getText().toString().equals("0")) {
            Snackbar.make(findViewById(R.id.main), "Height cannot be zero!", Snackbar.LENGTH_SHORT).show();
            return;
        }
        if (etWeight.getText().toString().equals("0")) {
            Snackbar.make(findViewById(R.id.main), "Weight cannot be zero!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        float weight = Float.parseFloat(etWeight.getText().toString());
        float height = Float.parseFloat(etHeight.getText().toString()) / 100;
        currentWeight = weight;
        currentHeight = height;

        int age = -1;
        String ageStr = etAge.getText().toString();
        if (!ageStr.isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                Snackbar.make(findViewById(R.id.main), "Please enter a valid age!", Snackbar.LENGTH_SHORT).show();
                return;
            }
        }

        float bmi = weight / (height * height);
        currentBmi = bmi;
        tvResultVal.setText(String.format("BMI: %.2f", bmi));

        String category = "";

        if (age >= 20) {
            if (bmi < 18.5f) {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Underweight");
                category = "Underweight";
            } else if (bmi < 24.9f) {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Normal");
                category = "Normal";
            } else if (bmi < 29.9f) {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Overweight");
                category = "Overweight";
            } else {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Obese");
                category = "Obese";
            }

            // Only save to Firebase if explicitly requested
            if (saveToFirebase) {
                saveBmiCalculationToFirebase(weight, height * 100, bmi, category, age);
            }

        } else {
            tvBmiCategory.setText("");
            ObjectAnimator.ofInt(pbHealth, "bmiValue", 0).setDuration(1000).start();
            tvResultVal.setText("--");
            Snackbar.make(findViewById(R.id.main), "BMI categories are for ages 20 and above only! Use BMI-for-age percentiles (under 20)", Snackbar.LENGTH_LONG).show();
        }
    }

    private void saveBmiCalculationToFirebase(float weight, float height, float bmi, String category, int age) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            return;
        }

        firebaseHelper.updatePersonalInfo(age, selectedGender, height, weight,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        // Personal info updated
                    }

                    @Override
                    public void onFailure(String error) {
                        // Silent fail
                    }
                });

        firebaseHelper.addMeasurement(weight, height, bmi, category, "",
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Snackbar.make(findViewById(R.id.main),
                                "BMI saved to your history!",
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Snackbar.make(findViewById(R.id.main),
                                "Calculated, but couldn't save: " + error,
                                Snackbar.LENGTH_SHORT).show();
                    }
                });

        // Update goal progress if goal exists
        if (goalBmi > 0) {
            firebaseHelper.updateGoalProgress(currentBmi, new FirebaseHelper.OnCompleteListener() {
                @Override
                public void onSuccess() {
                    // Progress updated
                }

                @Override
                public void onFailure(String error) {
                    // Silent fail
                }
            });
        }
    }

    private void savePersonalInfoToFirebase() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null || isLoadingData) {
            return;
        }

        String ageStr = etAge.getText() != null ? etAge.getText().toString() : "";
        String weightStr = etWeight.getText() != null ? etWeight.getText().toString() : "";
        String heightStr = etHeight.getText() != null ? etHeight.getText().toString() : "";

        if (ageStr.isEmpty() || weightStr.isEmpty() || heightStr.isEmpty() || selectedGender == null) {
            return;
        }

        try {
            int age = Integer.parseInt(ageStr);
            float weight = Float.parseFloat(weightStr);
            float height = Float.parseFloat(heightStr);

            firebaseHelper.updatePersonalInfo(age, selectedGender, height, weight,
                    new FirebaseHelper.OnCompleteListener() {
                        @Override
                        public void onSuccess() {
                            // Silent success
                        }

                        @Override
                        public void onFailure(String error) {
                            // Silent fail
                        }
                    });
        } catch (NumberFormatException e) {
            // Invalid input
        }
    }

    private void showBmiGoalDialog() {
        if (currentBmi == 0f || currentHeight == 0f) {
            Snackbar.make(findViewById(R.id.main), "Please calculate your BMI first!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_weight_goal);
        dialog.setCancelable(true);

        TextView tvCurrentBmi = dialog.findViewById(R.id.tvCurrentBmi);
        TextInputEditText etGoalBmi = dialog.findViewById(R.id.etGoalBmi);
        LinearLayout progressSection = dialog.findViewById(R.id.progressSection);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        TextView tvRemainingValue = dialog.findViewById(R.id.tvRemainingValue);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnSetGoal = dialog.findViewById(R.id.btnSetGoal);

        tvCurrentBmi.setText(String.format("%.2f", currentBmi));

        // If goal already exists, show it and calculate progress
        if (goalBmi > 0) {
            etGoalBmi.setText(String.valueOf(goalBmi));
            progressSection.setVisibility(View.VISIBLE);

            // Calculate and display current progress
            int progress = calculateProgress(startBmi, currentBmi, goalBmi);
            progressBar.setProgress(progress);

            float bmiRemaining = Math.abs(goalBmi - currentBmi);
            tvRemainingValue.setText(String.format("%.2f BMI remaining", bmiRemaining));
        }

        btnCancel.setOnClickListener(v -> dialog.dismiss());

        btnSetGoal.setOnClickListener(v -> {
            String goalStr = etGoalBmi.getText().toString();

            if (goalStr.isEmpty()) {
                etGoalBmi.setError("Please enter goal BMI");
                return;
            }

            try {
                float goal = Float.parseFloat(goalStr);

                if (goal <= 0) {
                    etGoalBmi.setError("Goal BMI must be positive");
                    return;
                }

                if (goal < 10 || goal > 50) {
                    etGoalBmi.setError("Please enter a realistic BMI (10-50)");
                    return;
                }

                if (Math.abs(goal - currentBmi) < 0.1) {
                    etGoalBmi.setError("Goal BMI must be different from current BMI");
                    return;
                }

                goalBmi = goal;
                startBmi = currentBmi; // Set start BMI to current BMI when setting goal

                float targetWeight = goalBmi * currentHeight * currentHeight;

                saveGoalToFirebase(targetWeight, goal);

                progressSection.setVisibility(View.VISIBLE);

                // Initial progress is 0 when just set
                progressBar.setProgress(0);
                float bmiRemaining = Math.abs(goalBmi - currentBmi);
                tvRemainingValue.setText(String.format("%.2f BMI remaining", bmiRemaining));

                Snackbar.make(findViewById(R.id.main),
                        String.format("BMI goal set! Target weight: %.1f kg", targetWeight),
                        Snackbar.LENGTH_LONG).show();

                dialog.dismiss();

            } catch (NumberFormatException e) {
                etGoalBmi.setError("Please enter a valid number");
            }
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private int calculateProgress(float startBmi, float currentBmi, float goalBmi) {
        // Calculate the total distance from start to goal
        float totalDistance = Math.abs(goalBmi - startBmi);

        // If there's no distance, we're already at goal
        if (totalDistance == 0) {
            return 100;
        }

        // Calculate how much progress has been made
        float progressMade = Math.abs(startBmi - currentBmi);

        // Calculate percentage
        int percentage = (int) ((progressMade / totalDistance) * 100);

        // Cap at 100%
        return Math.min(100, Math.max(0, percentage));
    }

    private void saveGoalToFirebase(float targetWeight, float targetBmi) {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            Snackbar.make(findViewById(R.id.main),
                    "Sign in to save your goal",
                    Snackbar.LENGTH_SHORT).show();
            return;
        }

        long targetDate = System.currentTimeMillis() + (90L * 24 * 60 * 60 * 1000);

        firebaseHelper.setBmiGoal(targetWeight, targetBmi, currentWeight, targetDate,
                new FirebaseHelper.OnCompleteListener() {
                    @Override
                    public void onSuccess() {
                        Snackbar.make(findViewById(R.id.main),
                                "Goal saved successfully!",
                                Snackbar.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFailure(String error) {
                        Snackbar.make(findViewById(R.id.main),
                                "Couldn't save goal: " + error,
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
    }

    private void showProfileMenu() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_profile_menu);
        dialog.setCancelable(true);

        LinearLayout menuHistory = dialog.findViewById(R.id.menuHistory);
        LinearLayout menuStatistics = dialog.findViewById(R.id.menuStatistics);
        LinearLayout menuNotifications = dialog.findViewById(R.id.menuNotifications);

        menuHistory.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, HistoryActivity.class);
            startActivity(intent);
        });

        menuStatistics.setOnClickListener(v -> {
            dialog.dismiss();
            Intent intent = new Intent(MainActivity.this, StatisticsActivity.class);
            startActivity(intent);
        });

        menuNotifications.setOnClickListener(v -> {
            dialog.dismiss();
            Snackbar.make(findViewById(R.id.main),
                    "Notifications - Coming Soon!",
                    Snackbar.LENGTH_SHORT).show();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void showSettingsDialog() {
        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.dialog_settings);
        dialog.setCancelable(true);

        MaterialButton btnSignOut = dialog.findViewById(R.id.btnSignOut);
        SwitchMaterial switchTheme = dialog.findViewById(R.id.switchTheme);

        SharedPreferences prefs = getSharedPreferences("app_prefs", MODE_PRIVATE);
        boolean darkMode = prefs.getBoolean("pref_dark_mode", false);
        switchTheme.setChecked(darkMode);

        switchTheme.setOnCheckedChangeListener((buttonView, isChecked) -> {
            prefs.edit().putBoolean("pref_dark_mode", isChecked).apply();
            AppCompatDelegate.setDefaultNightMode(
                    isChecked ? AppCompatDelegate.MODE_NIGHT_YES : AppCompatDelegate.MODE_NIGHT_NO
            );
            Snackbar.make(findViewById(R.id.main),
                    isChecked ? "Dark mode enabled" : "Light mode enabled",
                    Snackbar.LENGTH_SHORT).show();
        });

        btnSignOut.setOnClickListener(v -> {
            firebaseHelper.signOut();
            dialog.dismiss();

            Intent intent = new Intent(MainActivity.this, WelcomeActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
            finish();
        });

        dialog.show();

        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.85),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
    }
}