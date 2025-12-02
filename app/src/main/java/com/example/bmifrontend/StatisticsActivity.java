package com.example.bmifrontend;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.User;

public class StatisticsActivity extends AppCompatActivity {

    private LineChart weightChart, bmiChart;
    private TextView tvCurrentWeight, tvGoalWeight, tvNoData;
    private Spinner spinnerTimeRange;

    private List<User.Measurement> allMeasurements = new ArrayList<>();
    private User userData;
    private FirebaseHelper firebaseHelper;
    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_statistics);

        firebaseHelper = new FirebaseHelper();
        firebaseAuth = FirebaseAuth.getInstance();

        initializeViews();
        setupToolbar();
        setupCharts();
        setupTimeRangeSpinner();

        loadUserData();
    }

    private void initializeViews() {
        weightChart = findViewById(R.id.weightChart);
        bmiChart = findViewById(R.id.bmiChart);
        tvCurrentWeight = findViewById(R.id.tvCurrentWeight);
        tvGoalWeight = findViewById(R.id.tvGoalWeight);
        tvNoData = findViewById(R.id.tvNoData);
        spinnerTimeRange = findViewById(R.id.spinnerTimeRange);
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle("Statistics");
        }
    }

    private void loadUserData() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();

        if (currentUser == null) {
            showNoDataMessage("Sign in to view your statistics");
            return;
        }

        showLoadingState(true);

        firebaseHelper.getUserData(new FirebaseHelper.OnUserDataListener() {
            @Override
            public void onSuccess(User user) {
                showLoadingState(false);
                userData = user;
                processUserData();
            }

            @Override
            public void onFailure(String error) {
                showLoadingState(false);
                showNoDataMessage("No data available yet. Start tracking your BMI!");
            }
        });
    }

    private void processUserData() {
        if (userData == null) return;

        // Extract measurements from map
        if (userData.getMeasurements() != null && !userData.getMeasurements().isEmpty()) {
            allMeasurements = new ArrayList<>(userData.getMeasurements().values());

            // Sort by timestamp
            Collections.sort(allMeasurements, new Comparator<User.Measurement>() {
                @Override
                public int compare(User.Measurement m1, User.Measurement m2) {
                    return Long.compare(m1.getTimestamp(), m2.getTimestamp());
                }
            });

            tvNoData.setVisibility(View.GONE);

            // Apply default filter (1 Month - position 1)
            filterMeasurementsByTimeRange(spinnerTimeRange.getSelectedItemPosition());
        } else {
            showNoDataMessage("No measurements yet. Calculate your BMI to start tracking!");
        }

        updateStatistics();
    }

    private void setupCharts() {
        setupWeightChart();
        setupBmiChart();
    }

    private void setupWeightChart() {
        weightChart.getDescription().setEnabled(false);
        weightChart.setTouchEnabled(true);
        weightChart.setDragEnabled(true);
        weightChart.setScaleEnabled(true);
        weightChart.setPinchZoom(true);
        weightChart.setDrawGridBackground(false);

        // X-axis
        XAxis xAxis = weightChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new DateAxisValueFormatter());

        // Y-axis
        YAxis leftAxis = weightChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);

        weightChart.getAxisRight().setEnabled(false);
        weightChart.getLegend().setEnabled(false);
    }

    private void setupBmiChart() {
        bmiChart.getDescription().setEnabled(false);
        bmiChart.setTouchEnabled(true);
        bmiChart.setDragEnabled(true);
        bmiChart.setScaleEnabled(true);
        bmiChart.setPinchZoom(true);
        bmiChart.setDrawGridBackground(false);

        // X-axis
        XAxis xAxis = bmiChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setValueFormatter(new DateAxisValueFormatter());

        // Y-axis
        YAxis leftAxis = bmiChart.getAxisLeft();
        leftAxis.setDrawGridLines(true);
        leftAxis.setGridColor(Color.LTGRAY);

        bmiChart.getAxisRight().setEnabled(false);
        bmiChart.getLegend().setEnabled(false);
    }

    private void updateChartData(List<User.Measurement> measurements) {
        if (measurements.isEmpty()) {
            weightChart.clear();
            bmiChart.clear();
            weightChart.invalidate();
            bmiChart.invalidate();
            return;
        }

        // Prepare weight data
        List<Entry> weightEntries = new ArrayList<>();
        List<Entry> bmiEntries = new ArrayList<>();

        for (User.Measurement m : measurements) {
            weightEntries.add(new Entry(m.getTimestamp(), m.getWeight()));
            bmiEntries.add(new Entry(m.getTimestamp(), m.getBmi()));
        }

        // Weight chart
        LineDataSet weightDataSet = new LineDataSet(weightEntries, "Weight");
        weightDataSet.setColor(Color.parseColor("#2196F3"));
        weightDataSet.setCircleColor(Color.parseColor("#2196F3"));
        weightDataSet.setLineWidth(2f);
        weightDataSet.setCircleRadius(4f);
        weightDataSet.setDrawCircleHole(false);
        weightDataSet.setValueTextSize(9f);
        weightDataSet.setDrawFilled(true);
        weightDataSet.setFillColor(Color.parseColor("#2196F3"));
        weightDataSet.setFillAlpha(50);
        weightDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData weightLineData = new LineData(weightDataSet);
        weightChart.setData(weightLineData);
        weightChart.notifyDataSetChanged();
        weightChart.invalidate();
        weightChart.animateX(500);

        // BMI chart
        LineDataSet bmiDataSet = new LineDataSet(bmiEntries, "BMI");
        bmiDataSet.setColor(Color.parseColor("#FF5722"));
        bmiDataSet.setCircleColor(Color.parseColor("#FF5722"));
        bmiDataSet.setLineWidth(2f);
        bmiDataSet.setCircleRadius(4f);
        bmiDataSet.setDrawCircleHole(false);
        bmiDataSet.setValueTextSize(9f);
        bmiDataSet.setDrawFilled(true);
        bmiDataSet.setFillColor(Color.parseColor("#FF5722"));
        bmiDataSet.setFillAlpha(50);
        bmiDataSet.setMode(LineDataSet.Mode.CUBIC_BEZIER);

        LineData bmiLineData = new LineData(bmiDataSet);
        bmiChart.setData(bmiLineData);
        bmiChart.notifyDataSetChanged();
        bmiChart.invalidate();
        bmiChart.animateX(500);
    }

    private void updateStatistics() {
        if (userData == null || userData.getPersonalInfo() == null) return;

        User.PersonalInfo info = userData.getPersonalInfo();
        User.Goals goals = userData.getGoals();

        // Current and goal weight
        if (info.getCurrentWeight() > 0) {
            tvCurrentWeight.setText(String.format(Locale.getDefault(), "%.1f kg", info.getCurrentWeight()));
        } else {
            tvCurrentWeight.setText("--");
        }

        if (info.getGoalWeight() > 0) {
            tvGoalWeight.setText(String.format(Locale.getDefault(), "%.1f kg", info.getGoalWeight()));
        } else {
            tvGoalWeight.setText("--");
        }



    }

    private void setupTimeRangeSpinner() {
        spinnerTimeRange.setSelection(1);

        spinnerTimeRange.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                filterMeasurementsByTimeRange(position);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
    }

    private void filterMeasurementsByTimeRange(int position) {
        if (allMeasurements.isEmpty()) {
            return;
        }

        Calendar cal = Calendar.getInstance();
        long startTime;

        switch (position) {
            case 0: // 7 days
                cal.add(Calendar.DAY_OF_YEAR, -7);
                startTime = cal.getTimeInMillis();
                break;
            case 1: // 1 month
                cal.add(Calendar.MONTH, -1);
                startTime = cal.getTimeInMillis();
                break;
            case 2: // 3 month
                cal.add(Calendar.MONTH, -3);
                startTime = cal.getTimeInMillis();
                break;
            case 3: // 6 month
                cal.add(Calendar.MONTH, -6);
                startTime = cal.getTimeInMillis();
                break;
            case 4: // year
                cal.add(Calendar.YEAR, -1);
                startTime = cal.getTimeInMillis();
                break;
            case 5: // all time
            default:
                startTime = 0;
                break;
        }

        // filter measurements
        List<User.Measurement> filteredMeasurements = new ArrayList<>();
        for (User.Measurement m : allMeasurements) {
            if (m.getTimestamp() >= startTime) {
                filteredMeasurements.add(m);
            }
        }


        updateChartData(filteredMeasurements);
    }

    private void showLoadingState(boolean loading) {
        if (loading) {
            tvNoData.setVisibility(View.VISIBLE);
            tvNoData.setText("Loading your statistics...");
        }
    }

    private void showNoDataMessage(String message) {
        tvNoData.setVisibility(View.VISIBLE);
        tvNoData.setText(message);
        weightChart.clear();
        bmiChart.clear();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    // Custom formatter for date
    private class DateAxisValueFormatter extends ValueFormatter {
        private SimpleDateFormat dateFormat = new SimpleDateFormat("MMM dd", Locale.getDefault());

        @Override
        public String getFormattedValue(float value) {
            return dateFormat.format(new Date((long) value));
        }
    }
}