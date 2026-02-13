package com.simats.ashasmartcare;

import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VisitsWithStatusAdapter extends RecyclerView.Adapter<VisitsWithStatusAdapter.ViewHolder> {

    private List<String> visits;
    private List<String> statuses;

    public VisitsWithStatusAdapter(List<String> visits, List<String> statuses) {
        this.visits = visits;
        this.statuses = statuses;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Create card view for each visit
        CardView cardView = new CardView(parent.getContext());
        RecyclerView.LayoutParams cardParams = new RecyclerView.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 0, 0, 24);
        cardView.setLayoutParams(cardParams);
        cardView.setCardElevation(2);
        cardView.setRadius(12);
        cardView.setCardBackgroundColor(Color.WHITE);
        
        // Main horizontal layout
        LinearLayout mainLayout = new LinearLayout(parent.getContext());
        mainLayout.setOrientation(LinearLayout.HORIZONTAL);
        mainLayout.setPadding(32, 32, 32, 32);
        mainLayout.setGravity(Gravity.CENTER_VERTICAL);
        
        // Icon (using text as placeholder)
        TextView iconView = new TextView(parent.getContext());
        iconView.setText("\uD83D\uDCC5"); // Calendar emoji
        iconView.setTextSize(24);
        LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        iconParams.setMarginEnd(32);
        iconView.setLayoutParams(iconParams);
        
        // Text container (vertical layout)
        LinearLayout textContainer = new LinearLayout(parent.getContext());
        textContainer.setOrientation(LinearLayout.VERTICAL);
        LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
            0,
            LinearLayout.LayoutParams.WRAP_CONTENT,
            1.0f
        );
        textContainer.setLayoutParams(textParams);
        
        // Visit info text
        TextView infoView = new TextView(parent.getContext());
        infoView.setTextColor(0xFF1E293B);
        infoView.setTextSize(15);
        infoView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        
        textContainer.addView(infoView);
        
        // Status badge
        TextView statusView = new TextView(parent.getContext());
        statusView.setTextSize(12);
        statusView.setTextColor(Color.WHITE);
        statusView.setPadding(24, 16, 24, 16);
        statusView.setGravity(Gravity.CENTER);
        
        // Create rounded background for badge
        GradientDrawable badgeBackground = new GradientDrawable();
        badgeBackground.setCornerRadius(20);
        statusView.setBackground(badgeBackground);
        
        mainLayout.addView(iconView);
        mainLayout.addView(textContainer);
        mainLayout.addView(statusView);
        
        cardView.addView(mainLayout);
        
        return new ViewHolder(cardView, infoView, statusView, badgeBackground);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String visitInfo = visits.get(position);
        String status = statuses.get(position);
        
        holder.infoView.setText(visitInfo);
        
        // Set status badge color and text based on status
        if (status.equalsIgnoreCase("completed")) {
            holder.statusView.setText("Completed");
            holder.badgeBackground.setColor(0xFF10B981); // Green
        } else if (status.equalsIgnoreCase("missed")) {
            holder.statusView.setText("Missed");
            holder.badgeBackground.setColor(0xFFEF4444); // Red
        } else if (status.equalsIgnoreCase("upcoming")) {
            holder.statusView.setText("Upcoming");
            holder.badgeBackground.setColor(0xFF3B82F6); // Blue
        } else if (status.equalsIgnoreCase("cancelled")) {
            holder.statusView.setText("Cancelled");
            holder.badgeBackground.setColor(0xFF6B7280); // Gray
        } else {
            holder.statusView.setText(status);
            holder.badgeBackground.setColor(0xFF9CA3AF); // Light gray for unknown
        }
    }

    @Override
    public int getItemCount() {
        return visits.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView infoView;
        TextView statusView;
        GradientDrawable badgeBackground;

        ViewHolder(View itemView, TextView infoView, TextView statusView, GradientDrawable badgeBackground) {
            super(itemView);
            this.infoView = infoView;
            this.statusView = statusView;
            this.badgeBackground = badgeBackground;
        }
    }
}
