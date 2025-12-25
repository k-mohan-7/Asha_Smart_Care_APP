package com.simats.ashasmartcare;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;

import java.util.ArrayList;
import java.util.List;

public class AIInsightsActivity extends AppCompatActivity {

    private ImageView ivBack;
    private RecyclerView recyclerView;
    private LinearLayout layoutEmpty;
    private ProgressBar progressBar;
    private TextView tvHighRiskCount, tvDueVaccinations, tvUpcomingVisits;

    private DatabaseHelper dbHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ai_insights);

        initViews();
        loadData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivBack);
        recyclerView = findViewById(R.id.recyclerView);
        layoutEmpty = findViewById(R.id.layoutEmpty);
        progressBar = findViewById(R.id.progressBar);
        tvHighRiskCount = findViewById(R.id.tvHighRiskCount);
        tvDueVaccinations = findViewById(R.id.tvDueVaccinations);
        tvUpcomingVisits = findViewById(R.id.tvUpcomingVisits);

        dbHelper = DatabaseHelper.getInstance(this);

        ivBack.setOnClickListener(v -> finish());

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
    }

    private void loadData() {
        progressBar.setVisibility(View.VISIBLE);

        // Calculate statistics
        List<Patient> patients = dbHelper.getAllPatients();
        int highRisk = 0;
        int dueVaccinations = 0;
        int upcomingVisits = 0;

        for (Patient patient : patients) {
            if (patient.isHighRisk()) {
                highRisk++;
            }
        }

        // In a real implementation, you would query vaccinations and visits
        // that are due within the next 7 days
        dueVaccinations = 5; // Placeholder
        upcomingVisits = 12; // Placeholder

        tvHighRiskCount.setText(String.valueOf(highRisk));
        tvDueVaccinations.setText(String.valueOf(dueVaccinations));
        tvUpcomingVisits.setText(String.valueOf(upcomingVisits));

        // Generate AI insights based on data
        List<InsightItem> insights = generateInsights(patients, highRisk, dueVaccinations, upcomingVisits);
        
        InsightAdapter adapter = new InsightAdapter(insights);
        recyclerView.setAdapter(adapter);

        progressBar.setVisibility(View.GONE);

        if (insights.isEmpty()) {
            recyclerView.setVisibility(View.GONE);
            layoutEmpty.setVisibility(View.VISIBLE);
        } else {
            recyclerView.setVisibility(View.VISIBLE);
            layoutEmpty.setVisibility(View.GONE);
        }
    }

    private List<InsightItem> generateInsights(List<Patient> patients, int highRisk, int dueVaccinations, int upcomingVisits) {
        List<InsightItem> insights = new ArrayList<>();

        if (highRisk > 0) {
            insights.add(new InsightItem(
                    "High Risk Alert",
                    highRisk + " patients require immediate attention due to high-risk conditions. Prioritize home visits.",
                    R.drawable.ic_warning,
                    R.color.error
            ));
        }

        if (dueVaccinations > 0) {
            insights.add(new InsightItem(
                    "Vaccination Reminder",
                    dueVaccinations + " vaccinations are due this week. Ensure timely immunization.",
                    R.drawable.ic_vaccine,
                    R.color.success
            ));
        }

        if (upcomingVisits > 0) {
            insights.add(new InsightItem(
                    "Scheduled Visits",
                    upcomingVisits + " follow-up visits scheduled. Plan your route efficiently.",
                    R.drawable.ic_history,
                    R.color.info
            ));
        }

        // Add general health tips
        insights.add(new InsightItem(
                "Maternal Health Tip",
                "Encourage pregnant women to attend all ANC visits. Early detection of complications can save lives.",
                R.drawable.ic_pregnant,
                R.color.accent_pink
        ));

        insights.add(new InsightItem(
                "Child Nutrition",
                "Promote exclusive breastfeeding for first 6 months. Monitor child growth regularly.",
                R.drawable.ic_child,
                R.color.info
        ));

        insights.add(new InsightItem(
                "Community Awareness",
                "Conduct health education sessions on hygiene, nutrition, and family planning.",
                R.drawable.ic_people,
                R.color.primary
        ));

        return insights;
    }

    // Inner class for insight items
    static class InsightItem {
        String title;
        String description;
        int iconRes;
        int colorRes;

        InsightItem(String title, String description, int iconRes, int colorRes) {
            this.title = title;
            this.description = description;
            this.iconRes = iconRes;
            this.colorRes = colorRes;
        }
    }

    // Inner adapter class
    class InsightAdapter extends RecyclerView.Adapter<InsightAdapter.ViewHolder> {
        private List<InsightItem> items;

        InsightAdapter(List<InsightItem> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(android.view.ViewGroup parent, int viewType) {
            View view = getLayoutInflater().inflate(R.layout.item_insight, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ViewHolder holder, int position) {
            InsightItem item = items.get(position);
            holder.tvTitle.setText(item.title);
            holder.tvDescription.setText(item.description);
            holder.ivIcon.setImageResource(item.iconRes);
            holder.ivIcon.setColorFilter(getResources().getColor(item.colorRes));
        }

        @Override
        public int getItemCount() {
            return items.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            ImageView ivIcon;
            TextView tvTitle, tvDescription;

            ViewHolder(View itemView) {
                super(itemView);
                ivIcon = itemView.findViewById(R.id.ivIcon);
                tvTitle = itemView.findViewById(R.id.tvTitle);
                tvDescription = itemView.findViewById(R.id.tvDescription);
            }
        }
    }
}
