package com.example.bmifrontend;

import android.graphics.drawable.GradientDrawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.button.MaterialButton;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import Model.User;

public class HistoryAdapter extends RecyclerView.Adapter<HistoryAdapter.ViewHolder> {

    private List<User.Measurement> measurements;
    private OnItemActionListener itemActionListener;
    private int expandedPosition = -1;

    public interface OnItemActionListener {
        void onEditMeasurement(int position, User.Measurement measurement);
        void onDeleteMeasurement(int position, User.Measurement measurement);
    }

    public HistoryAdapter(List<User.Measurement> measurements) {
        this.measurements = measurements;
    }

    public void setOnItemActionListener(OnItemActionListener listener) {
        this.itemActionListener = listener;
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

        boolean isExpanded = position == expandedPosition;
        holder.actionButtons.setVisibility(isExpanded ? View.VISIBLE : View.GONE);

        holder.btnMenu.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (expandedPosition == currentPosition) {
                expandedPosition = -1;
                notifyItemChanged(currentPosition);
            } else {
                int previousExpanded = expandedPosition;
                expandedPosition = currentPosition;
                if (previousExpanded != -1) {
                    notifyItemChanged(previousExpanded);
                }
                notifyItemChanged(currentPosition);
            }
        });

        holder.btnEdit.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (itemActionListener != null) {
                itemActionListener.onEditMeasurement(currentPosition, measurement);
                expandedPosition = -1;
                notifyItemChanged(currentPosition);
            }
        });

        holder.btnDelete.setOnClickListener(v -> {
            int currentPosition = holder.getAdapterPosition();
            if (currentPosition == RecyclerView.NO_POSITION) return;

            if (itemActionListener != null) {
                itemActionListener.onDeleteMeasurement(currentPosition, measurement);
                expandedPosition = -1;
                notifyItemChanged(currentPosition);
            }
        });
    }

    @Override
    public int getItemCount() {
        return measurements.size();
    }

    public void removeItem(int position) {
        if (position >= 0 && position < measurements.size()) {
            measurements.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void updateItem(int position, User.Measurement measurement) {
        if (position >= 0 && position < measurements.size()) {
            measurements.set(position, measurement);
            notifyItemChanged(position);
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView tvDate;
        private TextView tvWeight;
        private TextView tvHeight;
        private TextView tvBmi;
        private TextView tvCategory;
        private View categoryIndicator;
        private LinearLayout actionButtons;
        private MaterialButton btnEdit;
        private MaterialButton btnDelete;
        private ImageView btnMenu;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            tvDate = itemView.findViewById(R.id.tvDate);
            tvWeight = itemView.findViewById(R.id.tvWeight);
            tvHeight = itemView.findViewById(R.id.tvHeight);
            tvBmi = itemView.findViewById(R.id.tvBmi);
            tvCategory = itemView.findViewById(R.id.tvCategory);
            categoryIndicator = itemView.findViewById(R.id.categoryIndicator);
            actionButtons = itemView.findViewById(R.id.actionButtons);
            btnEdit = itemView.findViewById(R.id.btnEdit);
            btnDelete = itemView.findViewById(R.id.btnDelete);
            btnMenu = itemView.findViewById(R.id.btnMenu);
        }

        public void bind(User.Measurement measurement) {
            if (tvDate != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("MMM dd, yyyy", Locale.getDefault());
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