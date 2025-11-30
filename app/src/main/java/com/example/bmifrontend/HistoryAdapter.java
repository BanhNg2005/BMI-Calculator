package com.example.bmifrontend;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.User;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<User.Measurement> measurements;

    public HistoryAdapter(List<User.Measurement> measurements) {
        this.measurements = measurements;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_measurement, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        User.Measurement measurement = measurements.get(position);
        holder.bind(measurement);
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvWeight;
        private TextView tvHeight;
        private TextView tvBmi;
        private TextView tvCategory;
        private View categoryIndicator;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvHeight = itemView.findViewById(R.id.tvHeight);
            tvBmi = itemView.findViewById(R.id.tvBmi);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            categoryIndicator = itemView.findViewById(R.id.categoryIndicator);
        }

        public void bind(User.Measurement measurement) {
            if (tvDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy HH:mm", Locale.getDefault());
                String date = sdf.format(new Date(measurement.getTimestamp()));
                tvDate.setText(date);
            }

            if (tvWeight != null) {
                tvWeight.setText(String.format(Locale.getDefault(), "%.1f kg", measurement.getWeight()));
            }

            if (tvHeight != null) {
                tvHeight.setText(String.format(Locale.getDefault(), "%.0f cm", measurement.getHeight()));
            }

            if (tvBmi != null) {
                tvBmi.setText(String.format(Locale.getDefault(), "%.2f", measurement.getBmi()));
            }

            if(tvCategory != null && measurement.getCategory() != null){
                tvCategory.setText(measurement.getCategory());
                setCategoryColor(measurement.getCategory());

                setCategoryChipColor(measurement.getCategory());
            }
        }

        private void setCategoryChipColor(String category) {
            if (tvCategory == null) return;

            int colorRes;
            switch (category.toLowerCase()) {
                case "underweight":
                    colorRes = R.color.lightUnderweight;
                    break;
                case "normal":
                    colorRes = R.color.lightNormal;
                    break;
                case "overweight":
                    colorRes = R.color.lightOverweight;
                    break;
                case "obese":
                    colorRes = R.color.lightObese;
                    break;
                default:
                    colorRes = R.color.lightNormal;
            }

            GradientDrawable shape = new GradientDrawable();
            shape.setColor(ContextCompat.getColor(itemView.getContext(), colorRes));
            shape.setCornerRadius(32);
            shape.setShape(GradientDrawable.RECTANGLE);
            tvCategory.setBackground(shape);
        }

        private void setCategoryColor(String category) {
            if (categoryIndicator == null) return;

            int colorRes;
            switch (category.toLowerCase()) {
                case "underweight":
                    colorRes = R.color.lightUnderweight;
                    break;
                case "normal":
                    colorRes = R.color.lightNormal;
                    break;
                case "overweight":
                    colorRes = R.color.lightOverweight;
                    break;
                case "obese":
                    colorRes = R.color.lightObese;
                    break;
                default:
                    colorRes = R.color.lightNormal;
            }
            categoryIndicator.setBackgroundColor(
                    ContextCompat.getColor(itemView.getContext(), colorRes)
            );
        }
    }
}