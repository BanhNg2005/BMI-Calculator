package com.example.bmifrontend;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

import Model.User;

public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private static final String USERS_PATH = "users";

    private final DatabaseReference databaseReference;
    private final FirebaseAuth firebaseAuth;

    public FirebaseHelper() {
        this.databaseReference = FirebaseDatabase.getInstance().getReference();
        this.firebaseAuth = FirebaseAuth.getInstance();
    }

    // Get current user ID
    private String getCurrentUserId() {
        FirebaseUser currentUser = firebaseAuth.getCurrentUser();
        return currentUser != null ? currentUser.getUid() : null;
    }

    // Create new user in database
    public void createUser(String email, String username, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        User user = new User(email, username);

        databaseReference.child(USERS_PATH).child(userId).setValue(user)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "User created successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error creating user", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Update personal info
    public void updatePersonalInfo(int age, String gender, float height,
                                   float currentWeight, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("personalInfo/age", age);
        updates.put("personalInfo/gender", gender);
        updates.put("personalInfo/height", height);
        updates.put("personalInfo/currentWeight", currentWeight);

        databaseReference.child(USERS_PATH).child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Personal info updated");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error updating personal info", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Add new measurement
    public void addMeasurement(float weight, float height, float bmi,
                               String category, String note, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        Map<String, Object> measurement = new HashMap<>();
        measurement.put("timestamp", System.currentTimeMillis());
        measurement.put("weight", weight);
        measurement.put("height", height);
        measurement.put("bmi", bmi);
        measurement.put("category", category);
        measurement.put("note", note != null ? note : "");

        String measurementKey = databaseReference.child(USERS_PATH)
                .child(userId)
                .child("measurements")
                .push()
                .getKey();

        if (measurementKey != null) {
            databaseReference.child(USERS_PATH)
                    .child(userId)
                    .child("measurements")
                    .child(measurementKey)
                    .setValue(measurement)
                    .addOnSuccessListener(aVoid -> {
                        Log.d(TAG, "Measurement added");
                        listener.onSuccess();
                    })
                    .addOnFailureListener(e -> {
                        Log.e(TAG, "Error adding measurement", e);
                        listener.onFailure(e.getMessage());
                    });
        }
    }

    // Set weight goal
    public void setWeightGoal(float targetWeight, float targetBmi, float startWeight,
                              long targetDate, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        Map<String, Object> goal = new HashMap<>();
        goal.put("targetWeight", targetWeight);
        goal.put("targetBmi", targetBmi);
        goal.put("startWeight", startWeight);
        goal.put("startDate", System.currentTimeMillis());
        goal.put("targetDate", targetDate);
        goal.put("status", "active");
        goal.put("progress", 0f);

        Map<String, Object> updates = new HashMap<>();
        updates.put("goals/currentGoal", goal);
        updates.put("personalInfo/goalWeight", targetWeight);
        updates.put("personalInfo/goalBmi", targetBmi);

        databaseReference.child(USERS_PATH).child(userId).updateChildren(updates)
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "Goal set successfully");
                    listener.onSuccess();
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error setting goal", e);
                    listener.onFailure(e.getMessage());
                });
    }

    // Update goal progress
    public void updateGoalProgress(float currentWeight, OnCompleteListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        // First, get the current goal to calculate progress
        databaseReference.child(USERS_PATH).child(userId).child("goals").child("currentGoal")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            Float targetWeight = snapshot.child("targetWeight").getValue(Float.class);
                            Float startWeight = snapshot.child("startWeight").getValue(Float.class);

                            if (targetWeight != null && startWeight != null) {
                                float totalDistance = Math.abs(targetWeight - startWeight);
                                float currentDistance = Math.abs(currentWeight - startWeight);
                                float progress = (currentDistance / totalDistance) * 100;
                                progress = Math.min(100, Math.max(0, progress));

                                Map<String, Object> updates = new HashMap<>();
                                updates.put("goals/currentGoal/progress", progress);
                                updates.put("personalInfo/currentWeight", currentWeight);

                                databaseReference.child(USERS_PATH).child(userId).updateChildren(updates)
                                        .addOnSuccessListener(aVoid -> listener.onSuccess())
                                        .addOnFailureListener(e -> listener.onFailure(e.getMessage()));
                            }
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    // Get user data
    public void getUserData(OnUserDataListener listener) {
        String userId = getCurrentUserId();
        if (userId == null) {
            listener.onFailure("User not authenticated");
            return;
        }

        databaseReference.child(USERS_PATH).child(userId)
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            User user = snapshot.getValue(User.class);
                            if (user != null) {
                                listener.onSuccess(user);
                            } else {
                                listener.onFailure("Failed to parse user data");
                            }
                        } else {
                            listener.onFailure("User data not found");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        listener.onFailure(error.getMessage());
                    }
                });
    }

    // Update last login
    public void updateLastLogin() {
        String userId = getCurrentUserId();
        if (userId == null) return;

        databaseReference.child(USERS_PATH)
                .child(userId)
                .child("profile")
                .child("lastLogin")
                .setValue(System.currentTimeMillis());
    }

    // Interfaces for callbacks
    public interface OnCompleteListener {
        void onSuccess();
        void onFailure(String error);
    }

    public interface OnUserDataListener {
        void onSuccess(User user);
        void onFailure(String error);
    }
}