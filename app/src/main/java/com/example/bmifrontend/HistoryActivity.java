package com.example.bmifrontend;

import android.content.Intent;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.User;

public class HistoryActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private HistoryAdapter historyAdapter;
    private LinearLayout emptyState;
    private MaterialButton btnBack;
    private TextView tvTitle;

    private DatabaseReference databaseReference;
    private FirebaseFirestore db;
    private List<User.Measurement> measurements = new ArrayList<>();

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
        historyAdapter = new HistoryAdapter(measurements);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(historyAdapter);
    }

    private void loadHistoryData() {
        FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();

        if (currentUser != null) {
            checkRealtimeDatabase(currentUser.getUid());
        } else {
            showEmptyState();
            Toast.makeText(this, "Please sign in to view history", Toast.LENGTH_SHORT).show();
        }
    }

    private void checkRealtimeDatabase(String userId) {
        DatabaseReference realtimeDb = FirebaseDatabase.getInstance().getReference();
        realtimeDb.child("users").child(userId).child("measurements")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(DataSnapshot dataSnapshot) {
                        measurements.clear();

                        if (dataSnapshot.exists()) {
                            for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                                User.Measurement measurement = snapshot.getValue(User.Measurement.class);
                                if (measurement != null) {
                                    measurements.add(measurement);
                                }
                            }

                            // Sort by timestamp (newest first)
                            Collections.sort(measurements, (m1, m2) ->
                                    Long.compare(m2.getTimestamp(), m1.getTimestamp()));

                            updateUI();
                        } else {
                            checkFirestore(userId);
                        }
                    }

                    @Override
                    public void onCancelled(DatabaseError databaseError) {
                        checkFirestore(userId);
                    }
                });
    }

    private void checkFirestore(String userId) {
        db.collection("users").document(userId)
                .collection("measurements")
                .orderBy("timestamp", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && !task.getResult().isEmpty()) {
                        measurements.clear();
                        for (QueryDocumentSnapshot document : task.getResult()) {
                            User.Measurement measurement = document.toObject(User.Measurement.class);
                            measurements.add(measurement);
                        }
                        updateUI();
                    } else {
                        checkMainFirestoreDocument(userId);
                    }
                });
    }

    private void checkMainFirestoreDocument(String userId) {
        db.collection("users").document(userId).get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult().exists()) {
                        User user = task.getResult().toObject(User.class);
                        if (user != null && user.getMeasurements() != null && !user.getMeasurements().isEmpty()) {
                            measurements.clear();
                            measurements.addAll(user.getMeasurements().values());
                            Collections.sort(measurements, (m1, m2) ->
                                    Long.compare(m2.getTimestamp(), m1.getTimestamp()));
                            updateUI();
                        } else {
                            showEmptyState();
                            Toast.makeText(HistoryActivity.this, "No BMI records found. Calculate your BMI first!", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        showEmptyState();
                    }
                });
    }

    private void updateUI() {
        if (measurements.isEmpty()) {
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
        if (!measurements.isEmpty() && tvTitle != null) {
            String summary = String.format(Locale.getDefault(),
                    "Total records: %d", measurements.size());
            tvTitle.setText(summary);
        }
    }
}