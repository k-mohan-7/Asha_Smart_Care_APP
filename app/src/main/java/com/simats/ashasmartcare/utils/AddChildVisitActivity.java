package com.simats.ashasmartcare.utils;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.simats.ashasmartcare.R;

import java.util.ArrayList;
import java.util.List;

public class AddChildVisitActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    VisitAdapter visitAdapter;
    List<VisitModel> visitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        setupToolbar();
        setupRecyclerView();
        loadVisitData();
    }

    private void setupToolbar() {
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        ImageView backBtn = findViewById(R.id.btnBack);
        backBtn.setOnClickListener(v -> finish());

        getSupportActionBar().setTitle("Patient Profile");
    }

    private void setupRecyclerView() {
        recyclerView = findViewById(R.id.rvVisits);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        visitList = new ArrayList<>();
        visitAdapter = new VisitAdapter(visitList);
        recyclerView.setAdapter(visitAdapter);
    }

    private void loadVisitData() {

        visitList.add(new VisitModel(
                "Vaccination",
                "Due: Nov 15, 2023",
                "DUE"
        ));

        visitList.add(new VisitModel(
                "Growth Monitoring",
                "Oct 20, 2023",
                "COMPLETED"
        ));

        visitList.add(new VisitModel(
                "Vaccination",
                "Sep 05, 2023",
                "COMPLETED"
        ));

        visitList.add(new VisitModel(
                "HBNC Visit",
                "Jul 10, 2023",
                "MISSED"
        ));

        visitList.add(new VisitModel(
                "Registration",
                "Jun 15, 2023",
                "COMPLETED"
        ));

        visitAdapter.notifyDataSetChanged();
    }
}
