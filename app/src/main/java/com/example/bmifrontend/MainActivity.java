package com.example.bmifrontend;

import android.animation.ObjectAnimator;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    TextView tvBmiCategory, tvResultVal;
    TextInputEditText etAge, etWeight, etHeight, etGoal;
    Button btnCalc, btnSetGoal;
    ImageButton imgMan, imgWoman;
    MaterialButton btnSettings, btnProfile;
    BMICustomProgressBar pbHealth;


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
    }

    private void initialize() {
        tvBmiCategory = findViewById(R.id.tvBmiCategory);
        tvResultVal = findViewById(R.id.tvResultVal);
        etAge = findViewById(R.id.etAge);
        etWeight = findViewById(R.id.etWeight);
        etHeight = findViewById(R.id.etHeight);
        etGoal = findViewById(R.id.etGoal);
        btnCalc = findViewById(R.id.btnCalc);
        btnSetGoal = findViewById(R.id.btnSetGoal);
        imgMan = findViewById(R.id.imgMan);
        imgWoman = findViewById(R.id.imgWoman);
        btnSettings = findViewById(R.id.btnSettings);
        btnProfile = findViewById(R.id.btnProfile);
        pbHealth = findViewById(R.id.pbHealth);

        btnCalc.setOnClickListener(this);
        btnSetGoal.setOnClickListener(this);
        imgMan.setOnClickListener(this);
        imgWoman.setOnClickListener(this);
        btnSettings.setOnClickListener(this);
        btnProfile.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();

        if (id == R.id.btnCalc) {
            calculateBMI();
        }
        if (id == R.id.btnSetGoal) {
            // Handle setting goal
        }
        if (id == R.id.btnSettings) {
            // Handle settings button click
        }
        if (id == R.id.btnProfile) {
            // Handle profile button click
        }

    }

    private void calculateBMI() {
        if (etWeight.getText().toString().isEmpty() || etHeight.getText().toString().isEmpty()) {
            tvResultVal.setText("Please enter weight and height");
            return;
        }

        float weight = Float.parseFloat(etWeight.getText().toString());
        float height = Float.parseFloat(etHeight.getText().toString()) / 100;

        int age = -1;
        String ageStr = etAge.getText().toString();
        if (!ageStr.isEmpty()) {
            try {
                age = Integer.parseInt(ageStr);
            } catch (NumberFormatException e) {
                tvResultVal.setText("Invalid age");
                return;
            }
        }
        float bmi = weight / (height * height);
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
            tvBmiCategory.setText("Use BMI-for-age percentiles (under 20)");
        }

    }



}