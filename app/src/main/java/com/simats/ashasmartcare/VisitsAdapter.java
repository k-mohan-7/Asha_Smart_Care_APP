package com.simats.ashasmartcare;

import android.graphics.Color;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class VisitsAdapter extends RecyclerView.Adapter<VisitsAdapter.ViewHolder> {

    private List<String> visits;

    public VisitsAdapter(List<String> visits) {
        this.visits = visits;
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
        
        // Visit title
        TextView titleView = new TextView(parent.getContext());
        titleView.setTextColor(0xFF1E293B);
        titleView.setTextSize(16);
        titleView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        
        // Visit date/details
        TextView detailsView = new TextView(parent.getContext());
        detailsView.setTextColor(0xFF64748B);
        detailsView.setTextSize(14);
        detailsView.setTextAlignment(View.TEXT_ALIGNMENT_VIEW_START);
        LinearLayout.LayoutParams detailsParams = new LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        );
        detailsParams.topMargin = 8;
        detailsView.setLayoutParams(detailsParams);
        
        textContainer.addView(titleView);
        textContainer.addView(detailsView);
        
        // Status badge
        TextView statusView = new TextView(parent.getContext());
        statusView.setTextSize(12);
        statusView.setPadding(24, 12, 24, 12);
        statusView.setGravity(Gravity.CENTER);
        
        mainLayout.addView(iconView);
        mainLayout.addView(textContainer);
        mainLayout.addView(statusView);
        
        cardView.addView(mainLayout);
        
        return new ViewHolder(cardView, titleView, detailsView, statusView);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        String visitInfo = visits.get(position);
        String[] parts = visitInfo.split("\\n");
        
        if (parts.length >= 2) {
            holder.titleView.setText(parts[0]); // Visit type
            holder.detailsView.setText(parts[1]); // Visit date
            
            // Determine status and set badge
            if (parts.length >= 3 && parts[2].contains("Purpose:")) {
                // This is a completed visit
                holder.statusView.setText("Completed");
                holder.statusView.setBackgroundColor(0xFFD1FAE5); // Light green
                holder.statusView.setTextColor(0xFF059669); // Dark green
            } else {
                // Check for status keywords in the text
                String lowerText = visitInfo.toLowerCase();
                if (lowerText.contains("completed")) {
                    holder.statusView.setText("Completed");
                    holder.statusView.setBackgroundColor(0xFFD1FAE5);
                    holder.statusView.setTextColor(0xFF059669);
                } else if (lowerText.contains("due soon")) {
                    holder.statusView.setText("Due Soon");
                    holder.statusView.setBackgroundColor(0xFFDBEAFE);
                    holder.statusView.setTextColor(0xFF2563EB);
                } else if (lowerText.contains("missed")) {
                    holder.statusView.setText("Missed");
                    holder.statusView.setBackgroundColor(0xFFFEE2E2);
                    holder.statusView.setTextColor(0xFFDC2626);
                } else {
                    holder.statusView.setText("Upcoming");
                    holder.statusView.setBackgroundColor(0xFFF1F5F9);
                    holder.statusView.setTextColor(0xFF475569);
                }
            }
        } else {
            holder.titleView.setText(visitInfo);
            holder.detailsView.setText("");
            holder.statusView.setVisibility(View.GONE);
        }
    }

    @Override
    public int getItemCount() {
        return visits.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView titleView;
        TextView detailsView;
        TextView statusView;

        ViewHolder(View itemView, TextView titleView, TextView detailsView, TextView statusView) {
            super(itemView);
            this.titleView = titleView;
            this.detailsView = detailsView;
            this.statusView = statusView;
        }
    }
}
