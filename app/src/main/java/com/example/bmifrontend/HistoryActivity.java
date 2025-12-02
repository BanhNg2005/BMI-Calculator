package com.example.bmifrontend;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;
import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import Model.User;

public class HistoryActivity extends AppCompatActivity implements HistoryAdapter.OnItemActionListener {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private LinearLayout emptyState;
    private MaterialButton btnBack;
    private TextView tvTitle;

    private DatabaseReference databaseReference;
    private List<User.Measurement> filteredMeasurements = new ArrayList<>();
    private Map<String, User.Measurement> allMeasurementsMap = new HashMap<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_history);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        databaseReference = FirebaseDatabase.getInstance().getReference();
        initializeViews();
        setupRecyclerView();
        loadHistoryData();
    }

    private void initializeViews() {
        recyclerView = findViewById(R.id.recyclerViewHistory);
        emptyState = findViewById(R.id.emptyState);
        btnBack = findViewById(R.id.btnBack);
        tvTitle = findViewById(R.id.tvTitle);

        if (btnBack != null) {
            btnBack.setOnClickListener(v -> finish());
        }
    }

    private void setupRecyclerView() {
        historyAdapter = new HistoryAdapter(filteredMeasurements);
        historyAdapter.setOnItemActionListener(this);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(historyAdapter);
    }

    private void loadHistoryData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            loadAllMeasurements(currentUser.getUid());
        } else {
            showEmptyState();
            Toast.makeText(this, "Please sign in to view history", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAllMeasurements(String userId) {
        DatabaseReference realtimeDb = FirebaseDatabase.getInstance().getReference();
        realtimeDb.child("users").child(userId).child("measurements")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        filteredMeasurements.clear();
                        allMeasurementsMap.clear();

                        if (dataSnapshot.exists()) {
                            List<User.Measurement> allMeasurements = new ArrayList<>();

                            // load EVERY measurements
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User.Measurement measurement = snapshot.getValue(User.Measurement.class);
                                if (measurement != null) {
                                    allMeasurements.add(measurement);
                                    allMeasurementsMap.put(snapshot.getKey(), measurement);
                                }
                            }

                            // sort them (newest always first)
                            Collections.sort(allMeasurements, (m1, m2) ->
                                    Long.compare(m2.getTimestamp(), m1.getTimestamp()));

                            filteredMeasurements.addAll(filterMeasurementsByDay(allMeasurements));

                            updateUI();
                        } else {
                            showEmptyState();
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        showEmptyState();
                        Toast.makeText(HistoryActivity.this, "Error loading data", Toast.LENGTH_SHORT).show();
                    }
                });
    }

    // keep only the most recent measurement one PER day
    private List<User.Measurement> filterMeasurementsByDay(List<User.Measurement> allMeasurements) {
        if (allMeasurements.isEmpty()) {
            return allMeasurements;
        }

        // map to store the most recent measurement for EACH day only
        Map<String, User.Measurement> measurementsByDay = new HashMap<>();
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());

        for (User.Measurement measurement : allMeasurements) {
            String dateKey = dateFormat.format(new Date(measurement.getTimestamp()));

            if (!measurementsByDay.containsKey(dateKey) ||
                    measurement.getTimestamp() > measurementsByDay.get(dateKey).getTimestamp()) {
                measurementsByDay.put(dateKey, measurement);
            }
        }

        List<User.Measurement> filteredList = new ArrayList<>(measurementsByDay.values());
        Collections.sort(filteredList, (m1, m2) ->
                Long.compare(m2.getTimestamp(), m1.getTimestamp()));

        return filteredList;
    }

    @Override
    public void onEditMeasurement(int position, User.Measurement measurement) {
        showEditMeasurementDialog(position, measurement);
    }

    private void showEditMeasurementDialog(int position, User.Measurement measurement) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Edit Measurement");

        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(50, 20, 50, 20);

        TextInputEditText etWeight = new TextInputEditText(this);
        etWeight.setHint("Weight (kg)");
        etWeight.setText(String.valueOf(measurement.getWeight()));
        layout.addView(etWeight);

        TextInputEditText etHeight = new TextInputEditText(this);
        etHeight.setHint("Height (cm)");
        etHeight.setText(String.valueOf(measurement.getHeight()));
        layout.addView(etHeight);

        builder.setView(layout);

        builder.setPositiveButton("Save", (dialog, which) -> {
            String weightStr = etWeight.getText().toString();
            String heightStr = etHeight.getText().toString();

            if (!weightStr.isEmpty() && !heightStr.isEmpty()) {
                try {
                    float weight = Float.parseFloat(weightStr);
                    float height = Float.parseFloat(heightStr);

                    if (weight <= 0 || height <= 0) {
                        Toast.makeText(this, "Please enter positive values", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // recalculate BMI
                    float heightInMeters = height / 100;
                    float bmi = weight / (heightInMeters * heightInMeters);

                    // set category
                    String category = getBmiCategory(bmi);

                    measurement.setWeight(weight);
                    measurement.setHeight(height);
                    measurement.setBmi(bmi);
                    measurement.setCategory(category);

                    updateMeasurementInFirebase(measurement);

                    filteredMeasurements.set(position, measurement);
                    historyAdapter.notifyItemChanged(position);

                    Toast.makeText(HistoryActivity.this,
                            "Measurement updated", Toast.LENGTH_SHORT).show();

                } catch (NumberFormatException e) {
                    Toast.makeText(this, "Please enter valid numbers", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show();
            }
        });

        builder.setNegativeButton("Cancel", null);
        builder.show();
    }

    private String getBmiCategory(float bmi) {
        if (bmi < 18.5f) return "Underweight";
        else if (bmi < 24.9f) return "Normal";
        else if (bmi < 29.9f) return "Overweight";
        else return "Obese";
    }

    private void updateMeasurementInFirebase(User.Measurement measurement) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentUser.getUid()).child("measurements");

        // find in Firebase
        for (Map.Entry<String, User.Measurement> entry : allMeasurementsMap.entrySet()) {
            if (entry.getValue().getTimestamp() == measurement.getTimestamp()) {
                userRef.child(entry.getKey()).setValue(measurement);
                break;
            }
        }
    }

    @Override
    public void onDeleteMeasurement(int position, User.Measurement measurement) {
        showDeleteConfirmationDialog(position, measurement);
    }

    private void showDeleteConfirmationDialog(int position, User.Measurement measurement) {
        new AlertDialog.Builder(this)
                .setTitle("Delete Measurement")
                .setMessage("Delete this measurement from " +
                        new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                                .format(new Date(measurement.getTimestamp())) + "?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    deleteMeasurementsFromDay(position, measurement);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteMeasurementsFromDay(int position, User.Measurement measurement) {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
        if (currentUser == null) return;

        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference()
                .child("users").child(currentUser.getUid()).child("measurements");

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        String targetDate = dateFormat.format(new Date(measurement.getTimestamp()));

        // find in Firebase
        for (Map.Entry<String, User.Measurement> entry : allMeasurementsMap.entrySet()) {
            String existingDate = dateFormat.format(new Date(entry.getValue().getTimestamp()));
            if (existingDate.equals(targetDate)) {
                userRef.child(entry.getKey()).removeValue();
            }
        }

        // remove from filtered list
        filteredMeasurements.remove(position);
        historyAdapter.notifyItemRemoved(position);

        // remove from map
        allMeasurementsMap.entrySet().removeIf(entry -> {
            String existingDate = dateFormat.format(new Date(entry.getValue().getTimestamp()));
            return existingDate.equals(targetDate);
        });

        Toast.makeText(this, "Measurement deleted", Toast.LENGTH_SHORT).show();

        if (historyAdapter.getItemCount() == 0) {
            showEmptyState();
        }
    }

    private void updateUI() {
        if (filteredMeasurements.isEmpty()) {
            showEmptyState();
        } else {
            hideEmptyState();
            if (historyAdapter != null) {
                historyAdapter.notifyDataSetChanged();
            }
            updateSummary();
        }
    }

    private void showEmptyState() {
        if (emptyState != null) emptyState.setVisibility(View.VISIBLE);
        if (recyclerView != null) recyclerView.setVisibility(View.GONE);
        if (tvTitle != null) tvTitle.setText("BMI History");
    }

    private void hideEmptyState() {
        if (emptyState != null) emptyState.setVisibility(View.GONE);
        if (recyclerView != null) recyclerView.setVisibility(View.VISIBLE);
    }

    private void updateSummary() {
        if (!filteredMeasurements.isEmpty() && tvTitle != null) {
            String summary = "BMI History - " + filteredMeasurements.size() + " days";
            tvTitle.setText(summary);
        }
    }
}