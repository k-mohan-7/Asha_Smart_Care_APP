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
import com.simats.ashasmartcare.adapters.VisitAdapter;
import com.simats.ashasmartcare.models.Visit;

import java.util.ArrayList;
import java.util.List;

public class AddChildVisitActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    VisitAdapter visitAdapter;
    List<Visit> visitList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_child_visit);

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
        // Fixed: Adapter requires Context, List, and Listener
        visitAdapter = new VisitAdapter(this, visitList, null);
        recyclerView.setAdapter(visitAdapter);
    }

    private void loadVisitData() {
        // Using Visit class instead of VisitModel
        // Visit constructor: Visit(long patientId, String visitDate, String visitType,
        // String purpose)
        // Or using empty constructor and setters for flexibility since the dummy data
        // doesn't match the constructor exactly

        Visit v1 = new Visit();
        v1.setVisitType("Vaccination");
        v1.setVisitDate("2023-11-15");
        v1.setPurpose("DUE"); // Using purpose field for status/description for now
        visitList.add(v1);

        Visit v2 = new Visit();
        v2.setVisitType("Growth Monitoring");
        v2.setVisitDate("2023-10-20");
        v2.setPurpose("COMPLETED");
        visitList.add(v2);

        Visit v3 = new Visit();
        v3.setVisitType("Vaccination");
        v3.setVisitDate("2023-09-05");
        v3.setPurpose("COMPLETED");
        visitList.add(v3);

        Visit v4 = new Visit();
        v4.setVisitType("HBNC Visit");
        v4.setVisitDate("2023-07-10");
        v4.setPurpose("MISSED");
        visitList.add(v4);

        Visit v5 = new Visit();
        v5.setVisitType("Registration");
        v5.setVisitDate("2023-06-15");
        v5.setPurpose("COMPLETED");
        visitList.add(v5);

        visitAdapter.notifyDataSetChanged();
    }
}
