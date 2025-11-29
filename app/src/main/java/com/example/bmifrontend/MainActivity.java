package com.example.bmifrontend;

import android.animation.ObjectAnimator;
import android.app.Dialog;
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

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.android.material.textfield.TextInputEditText;

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
    private float currentBmi = 0f;
    private float currentWeight = 0f;
    private float currentHeight = 0f;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        initialize();
        updateGenderSelection();
    }

    private void updateGenderSelection() {
        int selectedColor = Color.parseColor("#FFF1C4");
        int defaultColor = Color.parseColor("#fbf8f3");

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
            // Handle settings button click
        }
        if (id == R.id.btnProfile) {
            // Handle profile button click
        }
        if (id == R.id.imgMan) {
            selectedGender = "male";
            updateGenderSelection();
        }
        if (id == R.id.imgWoman) {
            selectedGender = "female";
            updateGenderSelection();
        }
    }

    private void calculateBMI() {
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

        if (age >= 20) {
            if (bmi < 18.5f) {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Underweight");
            } else if (bmi < 24.9f) {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Normal");
            } else if (bmi < 29.9f) {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Overweight");
            } else {
                ObjectAnimator.ofInt(pbHealth, "bmiValue", (int) bmi).setDuration(1000).start();
                tvBmiCategory.setText("Obese");
            }
        } else {
            tvBmiCategory.setText("");
            ObjectAnimator.ofInt(pbHealth, "bmiValue", 0).setDuration(1000).start();
            tvResultVal.setText("--");
            Snackbar.make(findViewById(R.id.main), "BMI categories are for ages 20 and above only! Use BMI-for-age percentiles (under 20)", Snackbar.LENGTH_LONG).show();
        }
    }

    private void showBmiGoalDialog() {
        // Check if current BMI is calculated
        if (currentBmi == 0f || currentHeight == 0f) {
            Snackbar.make(findViewById(R.id.main), "Please calculate your BMI first!", Snackbar.LENGTH_SHORT).show();
            return;
        }

        final Dialog dialog = new Dialog(this);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.activity_weight_goal);
        dialog.setCancelable(true);

        // Initialize dialog views
        TextView tvCurrentBmi = dialog.findViewById(R.id.tvCurrentBmi);
        TextInputEditText etGoalBmi = dialog.findViewById(R.id.etGoalBmi);
        LinearLayout progressSection = dialog.findViewById(R.id.progressSection);
        ProgressBar progressBar = dialog.findViewById(R.id.progressBar);
        TextView tvRemainingValue = dialog.findViewById(R.id.tvRemainingValue);
        MaterialButton btnCancel = dialog.findViewById(R.id.btnCancel);
        MaterialButton btnSetGoal = dialog.findViewById(R.id.btnSetGoal);

        // Set current BMI
        tvCurrentBmi.setText(String.format("%.2f", currentBmi));

        // If goal already exists, show progress
        if (goalBmi > 0) {
            etGoalBmi.setText(String.valueOf(goalBmi));
            progressSection.setVisibility(View.VISIBLE);
            updateProgress(progressBar, tvRemainingValue);
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
                progressSection.setVisibility(View.VISIBLE);
                updateProgress(progressBar, tvRemainingValue);

                // Calculate target weight based on goal BMI
                float targetWeight = goalBmi * currentHeight * currentHeight;

                Snackbar.make(findViewById(R.id.main),
                        String.format("BMI goal set! Target weight: %.1f kg", targetWeight),
                        Snackbar.LENGTH_LONG).show();

                dialog.dismiss();

            } catch (NumberFormatException e) {
                etGoalBmi.setError("Please enter a valid number");
            }
        });

        dialog.show();

        // Set dialog width
        if (dialog.getWindow() != null) {
            dialog.getWindow().setLayout(
                    (int) (getResources().getDisplayMetrics().widthPixels * 0.9),
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
        }
    }

    private void updateProgress(ProgressBar progressBar, TextView tvRemainingValue) {
        if (goalBmi <= 0 || currentBmi <= 0) return;

        float difference = Math.abs(goalBmi - currentBmi);
        float totalDistance = Math.abs(goalBmi - currentBmi);

        // Calculate how far we've come from start
        float progress;
        if (goalBmi < currentBmi) {
            // Losing BMI (going down)
            progress = 0; // Just started, no progress yet
        } else {
            // Gaining BMI (going up)
            progress = 0; // Just started, no progress yet
        }

        // When user updates their weight, recalculate actual progress
        // For now, showing the remaining BMI points

        progressBar.setProgress((int) progress);

        String remaining = String.format("%.2f BMI", difference);
        tvRemainingValue.setText(remaining);
    }
}