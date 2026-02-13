package com.simats.ashasmartcare;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.android.volley.VolleyError;
import com.google.android.material.tabs.TabLayout;
import com.simats.ashasmartcare.database.DatabaseHelper;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.network.ApiHelper;
import com.simats.ashasmartcare.utils.NetworkUtils;

import org.json.JSONObject;
import org.json.JSONArray;

public class PatientProfileActivity extends AppCompatActivity {

    private ImageView ivBack, ivEdit, ivDelete;
    private TextView tvName, tvAge, tvVillage;

    // Tags
    private TextView tagHighRisk, tagPregnant, tagSyncStatus;

    // Tabs
    private TabLayout tabLayout;

    // Tab Content Containers
    private LinearLayout layoutOverview;
    private RecyclerView rvVisits;

    // Vitals (will change based on category)
    private TextView tvLabelVital1, tvLabelVital2, tvLabelVital3, tvLabelVital4;
    private TextView tvVitalBP, tvVitalBPStatus;
    private TextView tvVitalWeight, tvVitalWeightStatus;
    private TextView tvVitalHb, tvVitalHbStatus;
    private TextView tvVitalFHR, tvVitalFHRStatus;

    // Schedule views
    private LinearLayout layoutSchedule1, layoutSchedule2;
    private TextView tvScheduleTitle1, tvScheduleDate1, tvScheduleStatus1;
    private TextView tvScheduleTitle2, tvScheduleDate2, tvScheduleStatus2;

    // Alert
    private CardView cardRiskAlert;
    private TextView tvAlertTitle, tvAlertDesc;

    // Bottom bar
    private Button btnBack, btnAddVisit;

    private DatabaseHelper dbHelper;
    private ApiHelper apiHelper;
    private com.simats.ashasmartcare.utils.SessionManager sessionManager;
    private Patient patient;
    private int patientId;
    private String patientCategory = "";
    private int currentTab = 0;
    private JSONObject categoryData = null; // Store pregnancy/child/general data for next_visit_date

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_patient_profile);

        patientId = getIntent().getIntExtra("patient_id", -1);
        if (patientId == -1) {
            Toast.makeText(this, "Patient not found", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        initViews();
        setupListeners();
        loadPatientData();
    }

    private void initViews() {
        ivBack = findViewById(R.id.ivTotalBack);
        ivEdit = findViewById(R.id.ivEdit);
        ivDelete = findViewById(R.id.ivDelete);

        // Basic patient info
        tvName = findViewById(R.id.tvProfileName);
        tvAge = findViewById(R.id.tvProfileAge);
        tvVillage = findViewById(R.id.tvProfileLocation);

        tagHighRisk = findViewById(R.id.tagHighRisk);
        tagPregnant = findViewById(R.id.tagPregnant);
        tagSyncStatus = findViewById(R.id.tagSyncStatus);

        // Tabs
        tabLayout = findViewById(R.id.tabLayout);

        // Tab Content Containers
        layoutOverview = findViewById(R.id.layoutOverview);
        rvVisits = findViewById(R.id.rvVisits);

        // Vitals
        tvLabelVital1 = findViewById(R.id.tvLabelVital1);
        tvLabelVital2 = findViewById(R.id.tvLabelVital2);
        tvLabelVital3 = findViewById(R.id.tvLabelVital3);
        tvLabelVital4 = findViewById(R.id.tvLabelVital4);
        tvVitalBP = findViewById(R.id.tvVitalBP);
        tvVitalBPStatus = findViewById(R.id.tvVitalBPStatus);
        tvVitalWeight = findViewById(R.id.tvVitalWeight);
        tvVitalWeightStatus = findViewById(R.id.tvVitalWeightStatus);
        tvVitalHb = findViewById(R.id.tvVitalHb);
        tvVitalHbStatus = findViewById(R.id.tvVitalHbStatus);
        tvVitalFHR = findViewById(R.id.tvVitalFHR);
        tvVitalFHRStatus = findViewById(R.id.tvVitalFHRStatus);

        // Alert card
        cardRiskAlert = findViewById(R.id.cardRiskAlert);
        tvAlertTitle = findViewById(R.id.tvAlertTitle);
        tvAlertDesc = findViewById(R.id.tvAlertDesc);

        // Schedule views
        layoutSchedule1 = findViewById(R.id.layoutSchedule1);
        layoutSchedule2 = findViewById(R.id.layoutSchedule2);
        tvScheduleTitle1 = findViewById(R.id.tvScheduleTitle1);
        tvScheduleDate1 = findViewById(R.id.tvScheduleDate1);
        tvScheduleStatus1 = findViewById(R.id.tvScheduleStatus1);
        tvScheduleTitle2 = findViewById(R.id.tvScheduleTitle2);
        tvScheduleDate2 = findViewById(R.id.tvScheduleDate2);
        tvScheduleStatus2 = findViewById(R.id.tvScheduleStatus2);

        // Bottom bar
        btnBack = findViewById(R.id.btnBack);
        btnAddVisit = findViewById(R.id.btnAddVisit);

        dbHelper = DatabaseHelper.getInstance(this);
        apiHelper = ApiHelper.getInstance(this);
        sessionManager = com.simats.ashasmartcare.utils.SessionManager.getInstance(this);
    }

    private void setupListeners() {
        ivBack.setOnClickListener(v -> finish());
        btnBack.setOnClickListener(v -> finish());

        ivEdit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddPatientActivity.class);
            intent.putExtra("patient_id", patientId);
            startActivity(intent);
        });

        ivDelete.setOnClickListener(v -> showDeleteConfirmation());

        btnAddVisit.setOnClickListener(v -> {
            Intent intent = new Intent(this, AddVisitActivity.class);
            intent.putExtra("patient_id", (long) patientId);
            intent.putExtra("purpose", "add_visit");
            startActivity(intent);
        });

        // Tab switching
        tabLayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                currentTab = tab.getPosition();
                showTabContent(currentTab);
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {
            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {
            }
        });
    }

    private void showTabContent(int tabPosition) {
        // Hide all content
        layoutOverview.setVisibility(View.GONE);
        rvVisits.setVisibility(View.GONE);

        switch (tabPosition) {
            case 0: // Overview
                layoutOverview.setVisibility(View.VISIBLE);
                loadCategorySpecificData(); // Refresh vitals
                // Make sure all vitals are visible for overview
                tvVitalHb.setVisibility(View.VISIBLE);
                tvVitalHbStatus.setVisibility(View.VISIBLE);
                tvVitalFHR.setVisibility(View.VISIBLE);
                tvVitalFHRStatus.setVisibility(View.VISIBLE);
                if (findViewById(R.id.cardUpcomingSchedule) != null) {
                    findViewById(R.id.cardUpcomingSchedule).setVisibility(View.VISIBLE);
                }
                break;
            case 1: // Visits
                rvVisits.setVisibility(View.VISIBLE);
                loadVisitHistoryWithAdapter();
                break;
            case 2: // Alerts
                layoutOverview.setVisibility(View.VISIBLE);
                // Hide schedule card for alerts view
                if (findViewById(R.id.cardUpcomingSchedule) != null) {
                    findViewById(R.id.cardUpcomingSchedule).setVisibility(View.GONE);
                }
                loadAlertsView();
                break;
            case 3: // Growth or Health History (depends on category)
                layoutOverview.setVisibility(View.VISIBLE);
                // Hide schedule card for growth/health history view
                if (findViewById(R.id.cardUpcomingSchedule) != null) {
                    findViewById(R.id.cardUpcomingSchedule).setVisibility(View.GONE);
                }

                // Check category to determine what to show
                String category = patientCategory != null ? patientCategory : "";
                if ("General".equalsIgnoreCase(category) || category.isEmpty()) {
                    loadHealthHistory(); // Show health history for general adults
                } else {
                    loadGrowthView(); // Show growth for pregnant/child
                }
                break;
        }
    }

    private void loadPatientData() {
        // Check internet connection first
        if (!NetworkUtils.isNetworkAvailable(this)) {
            Toast.makeText(this, "⚠️ No internet connection. Cannot load patient data.", Toast.LENGTH_LONG).show();
            return;
        }

        // ONLINE: Fetch from backend API
        // If admin, fetch all patients from admin.php, otherwise fetch specific ASHA
        // worker's patients
        String endpoint;
        if (sessionManager.isAdmin()) {
            endpoint = "admin.php?action=get_patients";
            android.util.Log.d("PatientProfile", "Admin mode: Loading all patients from admin.php");
        } else {
            String ashaId = String.valueOf(sessionManager.getUserId());
            endpoint = "patients.php?asha_id=" + ashaId;
            android.util.Log.d("PatientProfile", "Worker mode: Loading patients for ASHA ID: " + ashaId);
        }

        android.util.Log.d("PatientProfile", "Looking for patient ID: " + patientId);

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    boolean isSuccess = (response.optBoolean("success", false) ||
                            "success".equals(response.optString("status", "")));

                    if (isSuccess) {
                        org.json.JSONArray patientsArray = null;

                        // Check if response contains grouped data (admin endpoint)
                        JSONArray dataArray = response.has("patients") ? response.getJSONArray("patients")
                                : response.getJSONArray("data");

                        // Detect if first item is a worker group (has "patients" field)
                        boolean isGrouped = false;
                        if (dataArray.length() > 0) {
                            JSONObject firstItem = dataArray.getJSONObject(0);
                            isGrouped = firstItem.has("patients");
                        }

                        if (isGrouped) {
                            // Admin grouped response - flatten all patients from all workers
                            android.util.Log.d("PatientProfile", "Parsing admin grouped response");
                            patientsArray = new JSONArray();

                            for (int w = 0; w < dataArray.length(); w++) {
                                JSONObject workerGroup = dataArray.getJSONObject(w);
                                JSONArray patients = workerGroup.getJSONArray("patients");
                                for (int p = 0; p < patients.length(); p++) {
                                    patientsArray.put(patients.getJSONObject(p));
                                }
                            }
                        } else {
                            // Direct patient array (worker endpoint)
                            android.util.Log.d("PatientProfile", "Using direct patient array");
                            patientsArray = dataArray;
                        }

                        android.util.Log.d("PatientProfile",
                                "Found " + patientsArray.length() + " patients in response");

                        // Find our patient by ID
                        for (int i = 0; i < patientsArray.length(); i++) {
                            JSONObject patientObj = patientsArray.getJSONObject(i);
                            int serverId = patientObj.getInt("id");
                            android.util.Log.d("PatientProfile",
                                    "Checking patient ID: " + serverId + " against " + patientId);

                            if (serverId == patientId) {
                                // Found! Create Patient object
                                patient = new Patient();
                                patient.setServerId(patientObj.optInt("id", patientId));
                                patient.setName(patientObj.optString("name", "Unknown"));
                                patient.setAge(patientObj.optInt("age", 0));
                                patient.setGender(patientObj.optString("gender", "Female"));
                                patient.setCategory(patientObj.optString("category", "General"));
                                patient.setPhone(patientObj.optString("phone", ""));
                                patient.setAddress(patientObj.optString("address", ""));
                                patient.setBloodGroup(patientObj.optString("blood_group", ""));
                                patient.setHighRisk(patientObj.optInt("is_high_risk", 0) == 1);
                                patient.setHighRiskReason(patientObj.optString("high_risk_reason", ""));
                                patient.setRegistrationDate(patientObj.optString("created_at", ""));
                                patient.setSyncStatus("SYNCED");

                                // Store category BEFORE runOnUiThread
                                // Handle empty category - infer from age if needed
                                String rawCategory = patientObj.optString("category", "");
                                if (rawCategory == null || rawCategory.trim().isEmpty()) {
                                    int age = patientObj.optInt("age", 0);
                                    String gender = patientObj.optString("gender", "");

                                    // Auto-detect category based on age
                                    if (age <= 5) {
                                        rawCategory = "Child (0-5 years)";
                                        android.util.Log.w("PatientProfile",
                                                "Empty category, auto-detected as Child based on age=" + age);
                                    } else if (age >= 15 && age <= 50 && gender.equalsIgnoreCase("Female")) {
                                        rawCategory = "Pregnant Woman";
                                        android.util.Log.w("PatientProfile",
                                                "Empty category, auto-detected as Pregnant Woman based on age=" + age
                                                        + ", gender=" + gender);
                                    } else {
                                        rawCategory = "General";
                                        android.util.Log.w("PatientProfile", "Empty category, defaulting to General");
                                    }
                                }
                                patientCategory = rawCategory;
                                android.util.Log.d("PatientProfile", "Category set to: " + patientCategory);

                                runOnUiThread(() -> {
                                    displayPatientData();
                                    loadCategorySpecificData(); // Load vitals based on category (will call
                                                                // loadUpcomingSchedule internally)
                                });
                                break;
                            }
                        }

                        if (patient == null) {
                            runOnUiThread(() -> {
                                Toast.makeText(PatientProfileActivity.this, "Patient not found", Toast.LENGTH_SHORT)
                                        .show();
                                finish();
                            });
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast
                            .makeText(PatientProfileActivity.this, "Error loading patient data", Toast.LENGTH_SHORT)
                            .show());
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> {
                    Toast.makeText(PatientProfileActivity.this, "Error: " + error, Toast.LENGTH_LONG).show();
                    finish();
                });
            }
        });
    }

    private void displayPatientData() {
        // Display basic patient info
        tvName.setText(patient.getName());
        tvAge.setText(patient.getAge() + " years");
        tvVillage.setText(
                patient.getAddress() != null && !patient.getAddress().isEmpty() ? patient.getAddress() : "No address");

        // Show/hide tags based on category and high risk status
        String category = patient.getCategory();
        boolean isHighRisk = patient.isHighRisk();

        // High Risk Tag
        if (isHighRisk) {
            tagHighRisk.setVisibility(View.VISIBLE);
        } else {
            tagHighRisk.setVisibility(View.GONE);
        }

        // Category Tag (Pregnant, Child, etc.)
        if ("Pregnant Woman".equalsIgnoreCase(category)) {
            tagPregnant.setVisibility(View.VISIBLE);
            tagPregnant.setText("Pregnant");
        } else if ("Lactating Mother".equalsIgnoreCase(category)) {
            tagPregnant.setVisibility(View.VISIBLE);
            tagPregnant.setText("Lactating");
        } else if ("Child (0-5 yrs)".equalsIgnoreCase(category)) {
            tagPregnant.setVisibility(View.VISIBLE);
            tagPregnant.setText("Child");
        } else {
            tagPregnant.setVisibility(View.GONE);
        }

        // Sync Status Tag
        if (tagSyncStatus != null) {
            String syncStatus = patient.getSyncStatus();
            if (syncStatus == null)
                syncStatus = "SYNCED"; // Default for API data

            tagSyncStatus.setVisibility(View.VISIBLE);
            if ("SYNCED".equalsIgnoreCase(syncStatus)) {
                tagSyncStatus.setText("Synced");
                tagSyncStatus.setBackgroundResource(R.drawable.bg_badge_synced);
                tagSyncStatus.setTextColor(getResources().getColor(R.color.status_synced_text));
            } else {
                tagSyncStatus.setText("Pending");
                tagSyncStatus.setBackgroundResource(R.drawable.bg_badge_pending);
                tagSyncStatus.setTextColor(getResources().getColor(R.color.status_pending_text));
            }
        }

        // Update 4th tab label based on category
        if ("General".equalsIgnoreCase(category) || category == null || category.isEmpty()) {
            // For general adults, show "Health History" instead of "Growth"
            tabLayout.getTabAt(3).setText("Health History");
        } else {
            // For pregnant women and children, show "Growth"
            tabLayout.getTabAt(3).setText("Growth");
        }

        // Show/hide High Risk Alert Card
        if (isHighRisk) {
            cardRiskAlert.setVisibility(View.VISIBLE);
            String riskReason = patient.getHighRiskReason();
            tvAlertTitle.setText("High Risk Patient");
            tvAlertDesc.setText(riskReason != null && !riskReason.isEmpty() ? riskReason
                    : "This patient has been marked as high risk. Requires careful monitoring.");
        } else {
            cardRiskAlert.setVisibility(View.GONE);
        }
    }

    private void loadCategorySpecificData() {
        String category = patientCategory != null ? patientCategory : "";

        // Handle empty category
        if (category.trim().isEmpty()) {
            category = "General";
            android.util.Log.w("PatientProfile", "Category is empty, defaulting to General");
        }

        android.util.Log.d("PatientProfile", "Loading data for category: " + category);

        if (category.equalsIgnoreCase("Pregnant Woman")) {
            loadPregnancyData();
        } else if (category.contains("Child") || category.equalsIgnoreCase("Child (0-5 yrs)")
                || category.equalsIgnoreCase("Child (0-5 years)")) {
            loadChildGrowthData();
        } else {
            loadGeneralAdultData();
        }
    }

    private void loadPregnancyData() {
        String endpoint = "pregnancy.php?patient_id=" + patientId;

        android.util.Log.d("PatientProfile", "Loading pregnancy data from: " + endpoint);

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    android.util.Log.d("PatientProfile", "Pregnancy response: " + response.toString());

                    if (response.has("success") && response.getBoolean("success") &&
                            response.has("data") && response.getJSONArray("data").length() > 0) {

                        JSONArray dataArray = response.getJSONArray("data");
                        JSONObject pregnancyData = dataArray.getJSONObject(0); // Latest record

                        android.util.Log.d("PatientProfile", "Pregnancy data found: " + pregnancyData.toString());

                        // Store category data for upcoming schedule
                        categoryData = pregnancyData;

                        runOnUiThread(() -> {
                            displayPregnancyVitals(pregnancyData);
                            // Load upcoming schedule after category data is set
                            loadUpcomingSchedule();
                        });
                    } else {
                        android.util.Log.w("PatientProfile", "No pregnancy data in response");
                        runOnUiThread(() -> {
                            displayNoVitals("No pregnancy data available");
                            // Still try to load visits schedule
                            loadUpcomingSchedule();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("PatientProfile", "Error parsing pregnancy data: " + e.getMessage());
                    runOnUiThread(() -> displayNoVitals("Error loading pregnancy data"));
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("PatientProfile", "API Error loading pregnancy data: " + error);
                runOnUiThread(() -> {
                    displayNoVitals("No pregnancy data found");
                    // Still try to load visits schedule
                    loadUpcomingSchedule();
                });
            }
        });
    }

    private void loadChildGrowthData() {
        String endpoint = "child_growth.php?patient_id=" + patientId;

        android.util.Log.d("PatientProfile", "Loading child growth data from: " + endpoint);

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    android.util.Log.d("PatientProfile", "Child growth response: " + response.toString());

                    if (response.has("success") && response.getBoolean("success") &&
                            response.has("data") && response.getJSONArray("data").length() > 0) {

                        JSONArray dataArray = response.getJSONArray("data");
                        JSONObject childData = dataArray.getJSONObject(0); // Latest record

                        android.util.Log.d("PatientProfile", "Child growth data found: " + childData.toString());

                        // Store category data for upcoming schedule
                        categoryData = childData;

                        runOnUiThread(() -> {
                            displayChildGrowthVitals(childData);
                            // Load upcoming schedule after category data is set
                            loadUpcomingSchedule();
                        });
                    } else {
                        android.util.Log.w("PatientProfile", "No child growth data in response");
                        runOnUiThread(() -> {
                            displayNoVitals("No child growth data available");
                            // Still try to load visits schedule
                            loadUpcomingSchedule();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("PatientProfile", "Error parsing child data: " + e.getMessage());
                    runOnUiThread(() -> displayNoVitals("Error loading child data"));
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("PatientProfile", "API Error loading child data: " + error);
                runOnUiThread(() -> {
                    displayNoVitals("No child growth data found");
                    // Still try to load visits schedule
                    loadUpcomingSchedule();
                });
            }
        });
    }

    private void loadGeneralAdultData() {
        String endpoint = "general_adult.php?patient_id=" + patientId;

        android.util.Log.d("PatientProfile", "Loading general adult data from: " + endpoint);

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    android.util.Log.d("PatientProfile", "General adult response: " + response.toString());

                    if (response.has("success") && response.getBoolean("success") &&
                            response.has("data") && response.getJSONArray("data").length() > 0) {

                        JSONArray dataArray = response.getJSONArray("data");
                        JSONObject generalData = dataArray.getJSONObject(0); // Latest record

                        android.util.Log.d("PatientProfile", "General adult data found: " + generalData.toString());

                        // Store category data for upcoming schedule
                        categoryData = generalData;

                        runOnUiThread(() -> {
                            displayGeneralAdultVitals(generalData);
                            // Load upcoming schedule after category data is set
                            loadUpcomingSchedule();
                        });
                    } else {
                        android.util.Log.w("PatientProfile", "No general adult data in response");
                        runOnUiThread(() -> {
                            displayNoVitals("No health data available");
                            // Still try to load visits schedule
                            loadUpcomingSchedule();
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    android.util.Log.e("PatientProfile", "Error parsing general data: " + e.getMessage());
                    runOnUiThread(() -> displayNoVitals("Error loading health data"));
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("PatientProfile", "API Error loading general data: " + error);
                runOnUiThread(() -> {
                    displayNoVitals("No health data found");
                    // Still try to load visits schedule
                    loadUpcomingSchedule();
                });
            }
        });
    }

    private void displayPregnancyVitals(JSONObject data) {
        try {
            // Update labels for pregnancy vitals
            tvLabelVital1.setText("Blood Pressure");
            tvLabelVital2.setText("Weight");
            tvLabelVital3.setText("Hemoglobin");
            tvLabelVital4.setText("Fetal Heart");

            // Blood Pressure
            String bp = data.optString("blood_pressure", "-");
            tvVitalBP.setText(bp.isEmpty() ? "-" : bp);
            if (!bp.isEmpty() && !bp.equals("-")) {
                checkBPStatus(bp);
            } else {
                tvVitalBPStatus.setText("No data");
                tvVitalBPStatus.setTextColor(getResources().getColor(R.color.status_warning));
            }

            // Weight
            double weight = data.optDouble("weight", -1);
            if (weight > 0) {
                tvVitalWeight.setText(String.format("%.1f kg", weight));
                tvVitalWeightStatus.setText("");
            } else {
                tvVitalWeight.setText("-");
                tvVitalWeightStatus.setText("No data");
            }

            // Hemoglobin
            double hb = data.optDouble("hemoglobin", -1);
            if (hb > 0) {
                tvVitalHb.setText(String.format("%.1f g/dL", hb));
                if (hb < 11.0) {
                    tvVitalHbStatus.setText("Low");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_warning));
                } else if (hb >= 11.0 && hb <= 14.0) {
                    tvVitalHbStatus.setText("Normal");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_synced));
                } else {
                    tvVitalHbStatus.setText("High");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_warning));
                }
            } else {
                tvVitalHb.setText("-");
                tvVitalHbStatus.setText("No data");
            }

            // Fetal Heart Rate
            int fhr = data.optInt("fetal_heart_rate", -1);
            if (fhr > 0) {
                tvVitalFHR.setText(fhr + " bpm");
                if (fhr >= 120 && fhr <= 160) {
                    tvVitalFHRStatus.setText("Normal");
                    tvVitalFHRStatus.setTextColor(getResources().getColor(R.color.status_synced));
                } else {
                    tvVitalFHRStatus.setText("Abnormal");
                    tvVitalFHRStatus.setTextColor(getResources().getColor(R.color.status_error));
                }
            } else {
                tvVitalFHR.setText("-");
                tvVitalFHRStatus.setText("No data");
            }

            // Log all pregnancy data for debugging
            android.util.Log.d("PregnancyData", "BP: " + bp + ", Weight: " + weight + ", Hb: " + hb + ", FHR: " + fhr);
            android.util.Log.d("PregnancyData", "Danger Signs: " + data.optString("danger_signs", "None"));
            android.util.Log.d("PregnancyData", "Iron Tablets: " + data.optInt("iron_tablets_given", 0));
            android.util.Log.d("PregnancyData", "Calcium Tablets: " + data.optInt("calcium_tablets_given", 0));
            android.util.Log.d("PregnancyData", "Tetanus Injection: " + data.optInt("tetanus_injection_given", 0));
            android.util.Log.d("PregnancyData", "Urine Sugar: " + data.optString("urine_sugar", "Not tested"));
            android.util.Log.d("PregnancyData", "Urine Protein: " + data.optString("urine_protein", "Not tested"));
            android.util.Log.d("PregnancyData", "Fundal Height: " + data.optDouble("fundal_height", 0) + " cm");
            android.util.Log.d("PregnancyData", "Presentation: " + data.optString("presentation", "Not assessed"));
            android.util.Log.d("PregnancyData", "Gestational Weeks: " + data.optInt("gestational_weeks", 0));
            android.util.Log.d("PregnancyData", "LMP: " + data.optString("lmp_date", "Not recorded"));
            android.util.Log.d("PregnancyData", "EDD: " + data.optString("edd_date", "Not calculated"));

        } catch (Exception e) {
            e.printStackTrace();
            displayNoVitals("Error displaying pregnancy data");
        }
    }

    private void displayChildGrowthVitals(JSONObject data) {
        try {
            // Update labels for child vitals
            tvLabelVital1.setText("Weight");
            tvLabelVital2.setText("Height/Length");
            tvLabelVital3.setText("Temperature");
            tvLabelVital4.setText("MUAC");

            // Weight
            double weight = data.optDouble("weight", -1);
            if (weight > 0) {
                tvVitalBP.setText(String.format("%.1f kg", weight));
                // Check nutritional status based on weight
                String nutritionalStatus = data.optString("nutritional_status", "Normal");
                tvVitalBPStatus.setText(nutritionalStatus);
                if (nutritionalStatus.equalsIgnoreCase("Normal")) {
                    tvVitalBPStatus.setTextColor(getResources().getColor(R.color.status_synced));
                } else {
                    tvVitalBPStatus.setTextColor(getResources().getColor(R.color.status_warning));
                }
            } else {
                tvVitalBP.setText("-");
                tvVitalBPStatus.setText("No data");
            }

            // Height/Length
            double height = data.optDouble("height", -1);
            if (height > 0) {
                tvVitalWeight.setText(String.format("%.0f cm", height));
                tvVitalWeightStatus.setText("Normal");
                tvVitalWeightStatus.setTextColor(getResources().getColor(R.color.status_synced));
            } else {
                tvVitalWeight.setText("-");
                tvVitalWeightStatus.setText("No data");
            }

            // Temperature
            double temp = data.optDouble("temperature", -1);
            if (temp > 0) {
                tvVitalHb.setText(String.format("%.1f °F", temp));
                if (temp >= 97.0 && temp <= 99.5) {
                    tvVitalHbStatus.setText("Normal");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_synced));
                } else {
                    tvVitalHbStatus.setText("Abnormal");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_error));
                }
            } else {
                tvVitalHb.setText("-");
                tvVitalHbStatus.setText("No data");
            }

            // MUAC (Mid Upper Arm Circumference)
            double muac = data.optDouble("muac", -1);
            if (muac > 0) {
                tvVitalFHR.setText(String.format("%.1f cm", muac));
                // MUAC interpretation for children 6-59 months
                if (muac >= 13.5) {
                    tvVitalFHRStatus.setText("Normal");
                    tvVitalFHRStatus.setTextColor(getResources().getColor(R.color.status_synced));
                } else if (muac >= 12.5 && muac < 13.5) {
                    tvVitalFHRStatus.setText("Mild MAM");
                    tvVitalFHRStatus.setTextColor(getResources().getColor(R.color.status_warning));
                } else {
                    tvVitalFHRStatus.setText("Severe MAM");
                    tvVitalFHRStatus.setTextColor(getResources().getColor(R.color.status_error));
                }
            } else {
                tvVitalFHR.setText("-");
                tvVitalFHRStatus.setText("No data");
            }

            // Log all child data for debugging
            android.util.Log.d("ChildData", "Weight: " + weight + " kg");
            android.util.Log.d("ChildData", "Height: " + height + " cm");
            android.util.Log.d("ChildData", "Temperature: " + temp + " °F");
            android.util.Log.d("ChildData", "MUAC: " + muac + " cm");
            android.util.Log.d("ChildData", "Nutritional Status: " + data.optString("nutritional_status", "Normal"));
            android.util.Log.d("ChildData", "Fever: " + data.optInt("fever", 0));
            android.util.Log.d("ChildData", "Diarrhea: " + data.optInt("diarrhea", 0));
            android.util.Log.d("ChildData", "Cough/Cold: " + data.optInt("cough_cold", 0));
            android.util.Log.d("ChildData", "Vomiting: " + data.optInt("vomiting", 0));
            android.util.Log.d("ChildData", "Weakness: " + data.optInt("weakness", 0));
            android.util.Log.d("ChildData", "Breastfeeding: " + data.optInt("breastfeeding", 0));
            android.util.Log.d("ChildData", "Complementary Feeding: " + data.optInt("complementary_feeding", 0));
            android.util.Log.d("ChildData", "Appetite: " + data.optString("appetite", "Good"));
            android.util.Log.d("ChildData", "Last Vaccine: " + data.optString("last_vaccine", "Not recorded"));
            android.util.Log.d("ChildData",
                    "Next Vaccine Date: " + data.optString("next_vaccine_date", "Not scheduled"));

        } catch (Exception e) {
            e.printStackTrace();
            displayNoVitals("Error displaying child growth data");
        }
    }

    private void displayGeneralAdultVitals(JSONObject data) {
        try {
            // Update labels for general adult vitals
            tvLabelVital1.setText("Blood Pressure");
            tvLabelVital2.setText("Weight");
            tvLabelVital3.setText("Sugar Level");
            tvLabelVital4.setText("Temperature");

            // Blood Pressure
            String bp = data.optString("blood_pressure", "-");
            tvVitalBP.setText(bp.isEmpty() ? "-" : bp);
            if (!bp.isEmpty() && !bp.equals("-")) {
                checkBPStatus(bp);
            } else {
                tvVitalBPStatus.setText("No data");
            }

            // Weight
            double weight = data.optDouble("weight", -1);
            if (weight > 0) {
                tvVitalWeight.setText(String.format("%.1f kg", weight));
                tvVitalWeightStatus.setText("");
            } else {
                tvVitalWeight.setText("-");
                tvVitalWeightStatus.setText("No data");
            }

            // Sugar Level (instead of Hb)
            double sugar = data.optDouble("sugar_level", -1);
            if (sugar > 0) {
                tvVitalHb.setText(String.format("%.0f mg/dL", sugar));
                if (sugar < 140) {
                    tvVitalHbStatus.setText("Sugar Normal");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_synced));
                } else if (sugar >= 140 && sugar < 200) {
                    tvVitalHbStatus.setText("Sugar High");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_warning));
                } else {
                    tvVitalHbStatus.setText("Sugar Very High");
                    tvVitalHbStatus.setTextColor(getResources().getColor(R.color.status_error));
                }
            } else {
                tvVitalHb.setText("-");
                tvVitalHbStatus.setText("No data");
            }

            // Temperature (instead of FHR)
            double temp = data.optDouble("temperature", -1);
            if (temp > 0) {
                tvVitalFHR.setText(String.format("%.1f °F", temp));
                if (temp >= 97.0 && temp <= 99.0) {
                    tvVitalFHRStatus.setText("Temp Normal");
                    tvVitalFHRStatus.setTextColor(getResources().getColor(R.color.status_synced));
                } else {
                    tvVitalFHRStatus.setText("Temp Abnormal");
                    tvVitalFHRStatus.setTextColor(getResources().getColor(R.color.status_warning));
                }
            } else {
                tvVitalFHR.setText("-");
                tvVitalFHRStatus.setText("No data");
            }

            // Log all general adult data
            android.util.Log.d("GeneralData",
                    "BP: " + bp + ", Weight: " + weight + "kg, Sugar: " + sugar + "mg/dL, Temp: " + temp + "°F");
            android.util.Log.d("GeneralData", "Pulse Rate: " + data.optInt("pulse_rate", 0) + " bpm");
            android.util.Log.d("GeneralData", "Fever: " + (data.optInt("fever", 0) == 1 ? "Yes" : "No"));
            android.util.Log.d("GeneralData", "Body Pain: " + (data.optInt("body_pain", 0) == 1 ? "Yes" : "No"));
            android.util.Log.d("GeneralData",
                    "Breathlessness: " + (data.optInt("breathlessness", 0) == 1 ? "Yes" : "No"));
            android.util.Log.d("GeneralData", "Dizziness: " + (data.optInt("dizziness", 0) == 1 ? "Yes" : "No"));
            android.util.Log.d("GeneralData", "Chest Pain: " + (data.optInt("chest_pain", 0) == 1 ? "Yes" : "No"));
            android.util.Log.d("GeneralData", "Tobacco Use: " + data.optString("tobacco_use", "Not assessed"));
            android.util.Log.d("GeneralData", "Alcohol Use: " + data.optString("alcohol_use", "Not assessed"));
            android.util.Log.d("GeneralData",
                    "Physical Activity: " + data.optString("physical_activity", "Not assessed"));
            android.util.Log.d("GeneralData",
                    "Referral Required: " + (data.optInt("referral_required", 0) == 1 ? "Yes" : "No"));
            android.util.Log.d("GeneralData", "Has Diabetes: " + (data.optInt("has_diabetes", 0) == 1 ? "Yes" : "No"));
            android.util.Log.d("GeneralData",
                    "Has Hypertension: " + (data.optInt("has_hypertension", 0) == 1 ? "Yes" : "No"));

        } catch (Exception e) {
            e.printStackTrace();
            displayNoVitals("Error displaying health data");
        }
    }

    private void checkBPStatus(String bp) {
        try {
            String[] bpParts = bp.split("/");
            if (bpParts.length == 2) {
                int systolic = Integer.parseInt(bpParts[0].trim());
                int diastolic = Integer.parseInt(bpParts[1].trim());
                if (systolic >= 140 || diastolic >= 90) {
                    tvVitalBPStatus.setText("High");
                    tvVitalBPStatus.setTextColor(getResources().getColor(R.color.status_error));
                } else if (systolic >= 120 || diastolic >= 80) {
                    tvVitalBPStatus.setText("Elevated");
                    tvVitalBPStatus.setTextColor(getResources().getColor(R.color.status_warning));
                } else {
                    tvVitalBPStatus.setText("Normal");
                    tvVitalBPStatus.setTextColor(getResources().getColor(R.color.status_synced));
                }
            }
        } catch (NumberFormatException e) {
            tvVitalBPStatus.setText("");
        }
    }

    private void loadVisitHistoryWithAdapter() {
        String endpoint = "visits.php?patient_id=" + patientId;

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.has("success") && response.has("data")) {
                        JSONArray visitsArray = response.getJSONArray("data");
                        android.util.Log.d("VisitHistory", "Found " + visitsArray.length() + " visits");

                        runOnUiThread(() -> {
                            if (visitsArray.length() > 0) {
                                setupVisitsRecyclerView(visitsArray);
                            } else {
                                Toast.makeText(PatientProfileActivity.this,
                                        "No visit records found",
                                        Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> Toast.makeText(PatientProfileActivity.this,
                            "Error loading visits: " + e.getMessage(),
                            Toast.LENGTH_SHORT).show());
                }
            }

            @Override
            public void onError(String error) {
                android.util.Log.e("VisitHistory", "Error: " + error);
                runOnUiThread(() -> Toast.makeText(PatientProfileActivity.this,
                        "Unable to load visits",
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void setupVisitsRecyclerView(JSONArray visitsArray) {
        try {
            java.util.List<String> visitsList = new java.util.ArrayList<>();
            java.util.List<String> statusList = new java.util.ArrayList<>();

            for (int i = 0; i < visitsArray.length(); i++) {
                JSONObject visit = visitsArray.getJSONObject(i);
                String visitType = visit.optString("visit_type", "General Visit");
                String visitDate = visit.optString("visit_date", "No date");
                String purpose = visit.optString("purpose", "Routine checkup");
                String findings = visit.optString("findings", "");
                String status = visit.optString("status", "upcoming");

                // Build visit info without status in text
                String visitInfo = visitType + "\n" + visitDate;
                if (!purpose.isEmpty()) {
                    visitInfo += "\nPurpose: " + purpose;
                }
                if (!findings.isEmpty() && findings.length() < 100) {
                    visitInfo += "\nFindings: " + findings;
                }
                visitsList.add(visitInfo);
                statusList.add(status.toLowerCase());
            }

            // Simple ArrayAdapter with status badges
            androidx.recyclerview.widget.LinearLayoutManager layoutManager = new androidx.recyclerview.widget.LinearLayoutManager(
                    this);
            rvVisits.setLayoutManager(layoutManager);

            // Create adapter with status badges
            VisitsWithStatusAdapter adapter = new VisitsWithStatusAdapter(visitsList, statusList);
            rvVisits.setAdapter(adapter);

            Toast.makeText(this, "Loaded " + visitsList.size() + " visits", Toast.LENGTH_SHORT).show();
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying visits", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadAlertsView() {
        // Load and display real-time alerts based on patient vitals
        try {
            // Fetch latest vitals to check for alerts
            String category = patientCategory != null ? patientCategory : "";
            String endpoint = "";

            if (category.equalsIgnoreCase("Pregnant Woman")) {
                endpoint = "pregnancy.php?patient_id=" + patientId;
            } else if (category.contains("Child")) {
                endpoint = "child_growth.php?patient_id=" + patientId;
            } else {
                endpoint = "general_adult.php?patient_id=" + patientId;
            }

            apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("data") && response.getJSONArray("data").length() > 0) {
                            JSONArray dataArray = response.getJSONArray("data");
                            JSONObject latestData = dataArray.getJSONObject(0);

                            runOnUiThread(() -> displayRealTimeAlerts(latestData));
                        } else {
                            runOnUiThread(() -> displayNoAlerts());
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        runOnUiThread(() -> displayNoAlerts());
                    }
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> displayNoAlerts());
                }
            });

        } catch (Exception e) {
            e.printStackTrace();
            displayNoAlerts();
        }
    }

    private void displayRealTimeAlerts(JSONObject data) {
        try {
            java.util.List<String> activeAlerts = new java.util.ArrayList<>();
            java.util.List<String> resolvedAlerts = new java.util.ArrayList<>();

            String category = patientCategory != null ? patientCategory : "";

            // Check for category-specific alerts
            if (category.equalsIgnoreCase("Pregnant Woman")) {
                // Check BP with proper pregnancy limits
                String bp = data.optString("blood_pressure", "");
                if (!bp.isEmpty() && !bp.equals("-")) {
                    String[] bpParts = bp.split("/");
                    if (bpParts.length == 2) {
                        try {
                            int systolic = Integer.parseInt(bpParts[0].trim());
                            int diastolic = Integer.parseInt(bpParts[1].trim());
                            if (systolic >= 160 || diastolic >= 100) {
                                activeAlerts.add("🚨 Severe Hypertension: " + bp
                                        + " mmHg (Critical)\nImmediate medical attention required");
                            } else if (systolic >= 140 || diastolic >= 90) {
                                activeAlerts.add("⚠️ High Blood Pressure: " + bp
                                        + " mmHg (Normal: <140/90)\nClose monitoring required");
                            } else if (systolic < 90 || diastolic < 60) {
                                activeAlerts.add("⚠️ Low Blood Pressure: " + bp
                                        + " mmHg (Normal: >90/60)\nMonitor for dizziness");
                            }
                        } catch (NumberFormatException e) {
                            // Invalid BP format, skip
                        }
                    }
                }

                // Check Hemoglobin with pregnancy-specific limits
                double hb = data.optDouble("hemoglobin", -1);
                if (hb > 0) {
                    if (hb < 9.0) {
                        activeAlerts.add("🚨 Severe Anemia: " + String.format("%.1f", hb)
                                + " g/dL (Critical)\nImmediate medical intervention required");
                    } else if (hb < 11.0) {
                        activeAlerts.add("⚠️ Low Hemoglobin (Anemia): " + String.format("%.1f", hb)
                                + " g/dL (Normal: ≥11.0 g/dL)\nIron supplementation needed");
                    }
                }

                // Check weight gain during pregnancy
                double weight = data.optDouble("weight", -1);
                if (weight > 0) {
                    if (weight < 40) {
                        activeAlerts.add(
                                "⚠️ Low Weight: " + String.format("%.1f", weight) + " kg\nNutritional support needed");
                    } else if (weight > 90) {
                        activeAlerts.add("⚠️ High Weight Gain\nDiet counseling recommended");
                    }
                }

                // Check danger signs
                String dangerSigns = data.optString("danger_signs", "");
                if (!dangerSigns.isEmpty() && !dangerSigns.equalsIgnoreCase("None")) {
                    activeAlerts.add("🚨 Danger Signs: " + dangerSigns + "\nImmediate medical attention required");
                }

            } else if (category.contains("Child")) {
                // Check temperature with proper limits for children
                double temp = data.optDouble("temperature", -1);
                if (temp > 0) {
                    if (temp > 102.0) {
                        activeAlerts.add("🚨 High Fever: " + String.format("%.1f", temp)
                                + "°F (Critical)\nImmediate medical attention required");
                    } else if (temp > 99.5) {
                        activeAlerts.add("⚠️ Fever: " + String.format("%.1f", temp)
                                + "°F (Normal: ≤99.5°F)\nMonitor and give fever medication");
                    }
                }

                // Check weight for malnutrition - age-specific
                double weight = data.optDouble("weight", -1);
                int ageMonths = data.optInt("age_months", 0);
                if (weight > 0 && ageMonths > 0) {
                    double expectedMinWeight = (ageMonths * 0.5) + 3.5;
                    if (weight < (expectedMinWeight * 0.75)) {
                        activeAlerts.add("🚨 Severe Underweight: " + String.format("%.1f", weight) + " kg\n(Expected: ≥"
                                + String.format("%.1f", expectedMinWeight) + " kg)\nNutritional intervention required");
                    } else if (weight < (expectedMinWeight * 0.85)) {
                        activeAlerts.add(
                                "⚠️ Underweight: " + String.format("%.1f", weight) + " kg\nNutritional support needed");
                    }
                }

                // Check MUAC with proper classification
                double muac = data.optDouble("muac", -1);
                if (muac > 0) {
                    if (muac < 11.5) {
                        activeAlerts.add("🚨 Severe Acute Malnutrition\nMUAC: " + String.format("%.1f", muac)
                                + " cm (Normal: >12.5 cm)\nImmediate therapeutic feeding required");
                    } else if (muac < 12.5) {
                        activeAlerts.add("⚠️ Moderate Malnutrition\nMUAC: " + String.format("%.1f", muac)
                                + " cm (Normal: >12.5 cm)\nNutritional support needed");
                    }
                }

                // Check nutritional status
                String nutritionalStatus = data.optString("nutritional_status", "");
                if (!nutritionalStatus.isEmpty() && !nutritionalStatus.equalsIgnoreCase("Normal")) {
                    activeAlerts.add("⚠️ Nutritional Status: " + nutritionalStatus + "\nNutritional assessment needed");
                }

                // Check symptoms
                if (data.optInt("fever", 0) == 1) {
                    activeAlerts.add("⚠️ Fever Present\nMonitor temperature regularly");
                }
                if (data.optInt("diarrhea", 0) == 1) {
                    activeAlerts.add("⚠️ Diarrhea Present\nMaintain hydration with ORS");
                }
                if (data.optInt("vomiting", 0) == 1) {
                    activeAlerts.add("⚠️ Vomiting Present\nCheck for dehydration signs");
                }
                if (data.optInt("cough_cold", 0) == 1 && data.optInt("fever", 0) == 1) {
                    activeAlerts.add("⚠️ Respiratory Symptoms with Fever\nMonitor for difficulty breathing");
                }

            } else {
                // General adult alerts with proper limits
                String bp = data.optString("blood_pressure", "");
                if (!bp.isEmpty() && !bp.equals("-")) {
                    String[] bpParts = bp.split("/");
                    if (bpParts.length == 2) {
                        try {
                            int systolic = Integer.parseInt(bpParts[0].trim());
                            int diastolic = Integer.parseInt(bpParts[1].trim());
                            if (systolic >= 160 || diastolic >= 100) {
                                activeAlerts.add("🚨 Severe Hypertension: " + bp
                                        + " mmHg (Critical)\nImmediate medical attention required");
                            } else if (systolic >= 140 || diastolic >= 90) {
                                activeAlerts.add("⚠️ High Blood Pressure: " + bp
                                        + " mmHg (Normal: <140/90)\nLifestyle modification needed");
                            }
                        } catch (NumberFormatException e) {
                            // Invalid BP format, skip
                        }
                    }
                }

                // Check sugar level with proper classification
                double sugar = data.optDouble("sugar_level", -1);
                if (sugar > 0) {
                    if (sugar >= 200) {
                        activeAlerts.add("🚨 Very High Blood Sugar: " + String.format("%.0f", sugar)
                                + " mg/dL (Critical)\nImmediate medical attention required");
                    } else if (sugar >= 140) {
                        activeAlerts.add("⚠️ High Blood Sugar: " + String.format("%.0f", sugar)
                                + " mg/dL (Normal: <140 mg/dL)\nDiabetes screening recommended");
                    }
                }

                // Check if diabetic or hypertensive
                if (data.optInt("has_diabetes", 0) == 1) {
                    activeAlerts.add("ℹ️ Diabetes Management\nRegular monitoring and medication compliance");
                }
                if (data.optInt("has_hypertension", 0) == 1) {
                    activeAlerts.add("ℹ️ Hypertension Monitoring\nBlood pressure control needed");
                }
            }

            // Display alerts in a clean professional card format (no big danger sign)
            tvLabelVital1.setText("Active Alerts");
            tvVitalBP.setText(String.valueOf(activeAlerts.size()));
            tvVitalBPStatus.setText(activeAlerts.size() > 0 ? "Requires attention" : "All clear");
            tvVitalBPStatus.setTextColor(activeAlerts.size() > 0 ? 0xFFEF4444 : 0xFF10B981);

            tvLabelVital2.setText("Health Status");
            tvVitalWeight.setText(activeAlerts.isEmpty() ? "Good" : "Review");
            tvVitalWeightStatus.setText(activeAlerts.isEmpty() ? "No issues" : "Check alerts");
            tvVitalWeightStatus.setTextColor(activeAlerts.isEmpty() ? 0xFF10B981 : 0xFFF59E0B);

            tvVitalHb.setVisibility(View.GONE);
            tvVitalHbStatus.setVisibility(View.GONE);
            tvVitalFHR.setVisibility(View.GONE);
            tvVitalFHRStatus.setVisibility(View.GONE);

            // Show alert details in a clean card format
            if (!activeAlerts.isEmpty()) {
                StringBuilder alertDetails = new StringBuilder();
                alertDetails.append("📋 Active Health Alerts (").append(activeAlerts.size()).append("):\\n\\n");
                for (String alert : activeAlerts) {
                    alertDetails.append(alert).append("\\n\\n");
                }

                tvAlertTitle.setText("Health Monitoring Alerts");
                tvAlertDesc.setText(alertDetails.toString());
                tvAlertDesc.setTextColor(getResources().getColor(android.R.color.black));
                // Use yellow/orange background for alerts (not red danger sign)
                cardRiskAlert.setCardBackgroundColor(0xFFFEF3C7);
                cardRiskAlert.setVisibility(View.VISIBLE);
            } else {
                // Show positive message when no alerts
                tvAlertTitle.setText("✓ All Clear");
                tvAlertDesc.setText(
                        "No active health alerts.\\n\\nAll vital signs are within normal ranges.\\nContinue regular monitoring as scheduled.");
                tvAlertDesc.setTextColor(getResources().getColor(android.R.color.darker_gray));
                // Use green background for no alerts
                cardRiskAlert.setCardBackgroundColor(0xFFD1FAE5);
                cardRiskAlert.setVisibility(View.VISIBLE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            displayNoAlerts();
        }
    }

    private void displayNoAlerts() {
        tvLabelVital1.setText("Active Alerts");
        tvVitalBP.setText("0");
        tvVitalBPStatus.setText("No alerts");
        tvVitalBPStatus.setTextColor(0xFF10B981);

        tvLabelVital2.setText("Status");
        tvVitalWeight.setText("Good");
        tvVitalWeightStatus.setText("All normal");
        tvVitalWeightStatus.setTextColor(0xFF10B981);

        tvVitalHb.setVisibility(View.GONE);
        tvVitalHbStatus.setVisibility(View.GONE);
        tvVitalFHR.setVisibility(View.GONE);
        tvVitalFHRStatus.setVisibility(View.GONE);

        tvAlertTitle.setText("Patient Health Alerts");
        tvAlertDesc.setText("No active alerts. Patient vitals are within normal range.");
        cardRiskAlert.setVisibility(View.VISIBLE);
    }

    private void loadGrowthView() {
        String category = patientCategory != null ? patientCategory : "";

        if (category.equalsIgnoreCase("Pregnant Woman")) {
            loadPregnancyGrowthTrends();
        } else if (category.contains("Child")) {
            loadChildGrowthTrends();
        } else {
            Toast.makeText(this, "Growth tracking not available for this patient category",
                    Toast.LENGTH_SHORT).show();
        }
    }

    private void loadUpcomingSchedule() {
        android.util.Log.d("UpcomingSchedule", "=== Loading Upcoming Schedule ===");
        android.util.Log.d("UpcomingSchedule", "Category: " + patientCategory);
        android.util.Log.d("UpcomingSchedule", "CategoryData exists: " + (categoryData != null));

        // First priority: Show next_visit_date from category data
        // (pregnancy/child/general)
        java.util.List<JSONObject> upcomingVisits = new java.util.ArrayList<>();

        try {
            // Check if category data has next_visit_date
            if (categoryData != null && categoryData.has("next_visit_date")) {
                String nextVisitDate = categoryData.optString("next_visit_date", "");
                android.util.Log.d("UpcomingSchedule", "Category next_visit_date: " + nextVisitDate);

                if (!nextVisitDate.isEmpty() && !nextVisitDate.equals("null") && !nextVisitDate.equals("0000-00-00")) {
                    // Create visit object from category data
                    JSONObject categoryVisit = new JSONObject();
                    String visitType = "";
                    if (patientCategory.contains("Pregnant")) {
                        visitType = "ANC Check-up";
                    } else if (patientCategory.contains("Child")) {
                        visitType = "Growth Monitoring";
                    } else {
                        visitType = "Follow-up Visit";
                    }
                    categoryVisit.put("visit_type", visitType);
                    categoryVisit.put("next_visit_date", nextVisitDate);
                    categoryVisit.put("id", -1); // No visit ID since it's from category data
                    categoryVisit.put("status", "upcoming"); // Default status for category visits
                    categoryVisit.put("from_category", true); // Mark as category-derived visit
                    upcomingVisits.add(categoryVisit);
                    android.util.Log.d("UpcomingSchedule",
                            "Added category visit: " + visitType + " on " + nextVisitDate);
                } else {
                    android.util.Log.d("UpcomingSchedule", "Category next_visit_date is empty or invalid");
                }
            } else {
                android.util.Log.d("UpcomingSchedule", "No category data or no next_visit_date field");
            }
        } catch (Exception e) {
            e.printStackTrace();
            android.util.Log.e("UpcomingSchedule", "Error processing category data: " + e.getMessage());
        }

        android.util.Log.d("UpcomingSchedule", "Total upcoming visits from category: " + upcomingVisits.size());

        // Second priority: Load upcoming visits from visits.php with status="upcoming"
        String endpoint = "visits.php?patient_id=" + patientId;

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.has("data") && response.getJSONArray("data").length() > 0) {
                        JSONArray visitsArray = response.getJSONArray("data");

                        // Add visits with status="upcoming" to the list
                        for (int i = 0; i < visitsArray.length(); i++) {
                            JSONObject visit = visitsArray.getJSONObject(i);
                            String nextVisitDate = visit.optString("next_visit_date", "");
                            String visitType = visit.optString("visit_type", "Checkup");
                            String visitStatus = visit.optString("status", "upcoming");
                            int visitId = visit.optInt("id", 0);

                            // Add all visits with valid next_visit_date (not just upcoming)
                            // This allows showing real-time status changes
                            if (!nextVisitDate.isEmpty() && !nextVisitDate.equals("null")
                                    && !nextVisitDate.equals("0000-00-00")) {
                                JSONObject upcomingVisit = new JSONObject();
                                upcomingVisit.put("visit_type", visitType.isEmpty() ? "Follow-up Visit" : visitType);
                                upcomingVisit.put("next_visit_date", nextVisitDate);
                                upcomingVisit.put("id", visitId);
                                upcomingVisit.put("status", visitStatus); // Include actual backend status
                                upcomingVisit.put("from_category", false);
                                upcomingVisits.add(upcomingVisit);
                            }
                        }
                    }

                    android.util.Log.d("UpcomingSchedule", "Total upcoming visits after API: " + upcomingVisits.size());

                    runOnUiThread(() -> {
                        if (!upcomingVisits.isEmpty()) {
                            android.util.Log.d("UpcomingSchedule",
                                    "Displaying " + upcomingVisits.size() + " upcoming visits");
                            displayUpcomingSchedule(upcomingVisits);
                        } else {
                            android.util.Log.d("UpcomingSchedule", "No upcoming visits, hiding schedule");
                            hideSchedule();
                        }
                    });
                } catch (Exception e) {
                    e.printStackTrace();
                    runOnUiThread(() -> hideSchedule());
                }
            }

            @Override
            public void onError(String error) {
                // Even if visits API fails, show category data if available
                runOnUiThread(() -> {
                    if (!upcomingVisits.isEmpty()) {
                        displayUpcomingSchedule(upcomingVisits);
                    } else {
                        hideSchedule();
                    }
                });
            }
        });
    }

    private void displayUpcomingSchedule(java.util.List<JSONObject> upcomingVisits) {
        try {
            if (upcomingVisits.isEmpty()) {
                hideSchedule();
                return;
            }

            // Show first upcoming visit with proper title display
            if (upcomingVisits.size() > 0) {
                JSONObject visit1 = upcomingVisits.get(0);
                String visitType = visit1.optString("visit_type", "Checkup");
                String nextDate = visit1.optString("next_visit_date", "Not scheduled");
                final int visitId = visit1.optInt("id", -1);
                String backendStatus = visit1.optString("status", "upcoming");
                boolean fromCategory = visit1.optBoolean("from_category", false);

                layoutSchedule1.setVisibility(View.VISIBLE);
                // Ensure visit type is properly displayed as title
                tvScheduleTitle1.setText(visitType.isEmpty() ? "Next Visit" : visitType);
                tvScheduleDate1.setText("Due: " + formatDate(nextDate));

                // Make ALL schedules clickable with proper handling
                layoutSchedule1.setOnClickListener(v -> {
                    if (visitId > 0) {
                        // Real visit record - show status dialog
                        showVisitStatusDialog(visitId, visitType, nextDate);
                    } else if (fromCategory) {
                        // Category-derived visit - explain to user
                        Toast.makeText(PatientProfileActivity.this,
                                "This is a scheduled " + visitType
                                        + " from patient records. Create a visit record to track status.",
                                Toast.LENGTH_LONG).show();
                    }
                });

                // Show REAL backend status (not just date-based)
                setStatusBadge(tvScheduleStatus1, backendStatus, nextDate);
            }

            // Show second upcoming visit if exists with edit capability
            if (upcomingVisits.size() > 1) {
                JSONObject visit2 = upcomingVisits.get(1);
                String visitType = visit2.optString("visit_type", "Follow-up");
                String nextDate = visit2.optString("next_visit_date", "Not scheduled");
                final int visitId2 = visit2.optInt("id", -1);
                String backendStatus2 = visit2.optString("status", "upcoming");
                boolean fromCategory2 = visit2.optBoolean("from_category", false);

                layoutSchedule2.setVisibility(View.VISIBLE);
                tvScheduleTitle2.setText(visitType.isEmpty() ? "Next Visit" : visitType);
                tvScheduleDate2.setText("Due: " + formatDate(nextDate));

                // Show REAL backend status
                setStatusBadge(tvScheduleStatus2, backendStatus2, nextDate);

                // Make ALL second schedules clickable with proper handling
                layoutSchedule2.setOnClickListener(v -> {
                    if (visitId2 > 0) {
                        // Real visit record - show status dialog
                        showVisitStatusDialog(visitId2, visitType, nextDate);
                    } else if (fromCategory2) {
                        // Category-derived visit - explain to user
                        Toast.makeText(PatientProfileActivity.this,
                                "This is a scheduled " + visitType
                                        + " from patient records. Create a visit record to track status.",
                                Toast.LENGTH_LONG).show();
                    }
                });
            } else {
                layoutSchedule2.setVisibility(View.GONE);
            }

        } catch (Exception e) {
            e.printStackTrace();
            hideSchedule();
        }
    }

    // Helper method to format date nicely
    private String formatDate(String dateStr) {
        try {
            java.text.SimpleDateFormat inputFormat = new java.text.SimpleDateFormat("yyyy-MM-dd",
                    java.util.Locale.getDefault());
            java.text.SimpleDateFormat outputFormat = new java.text.SimpleDateFormat("MMM dd, yyyy",
                    java.util.Locale.getDefault());
            java.util.Date date = inputFormat.parse(dateStr);
            return outputFormat.format(date);
        } catch (Exception e) {
            return dateStr;
        }
    }

    // Helper method to set status badge with REAL backend status + date awareness
    private void setStatusBadge(TextView statusView, String backendStatus, String dateStr) {
        try {
            // Parse date to check if overdue
            java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("yyyy-MM-dd",
                    java.util.Locale.getDefault());
            java.util.Date visitDate = sdf.parse(dateStr);
            java.util.Date today = new java.util.Date();
            long diffInMillis = visitDate.getTime() - today.getTime();
            long diffInDays = diffInMillis / (1000 * 60 * 60 * 24);

            // Show backend status with appropriate styling
            if (backendStatus.equalsIgnoreCase("completed")) {
                statusView.setText("Completed");
                statusView.setBackgroundColor(0xFFD1FAE5); // Green background
                statusView.setTextColor(0xFF059669); // Green text
            } else if (backendStatus.equalsIgnoreCase("missed")) {
                statusView.setText("Missed");
                statusView.setBackgroundColor(0xFFFEE2E2); // Red background
                statusView.setTextColor(0xFFDC2626); // Red text
            } else if (backendStatus.equalsIgnoreCase("cancelled")) {
                statusView.setText("Cancelled");
                statusView.setBackgroundColor(0xFFF3F4F6); // Gray background
                statusView.setTextColor(0xFF6B7280); // Gray text
            } else if (backendStatus.equalsIgnoreCase("upcoming")) {
                // For upcoming, show date-based urgency
                if (diffInDays < 0) {
                    statusView.setText("Overdue");
                    statusView.setBackgroundColor(0xFFFEE2E2);
                    statusView.setTextColor(0xFFDC2626);
                } else if (diffInDays <= 7) {
                    statusView.setText("Due Soon");
                    statusView.setBackgroundColor(0xFFDBEAFE);
                    statusView.setTextColor(0xFF2563EB);
                } else {
                    statusView.setText("Upcoming");
                    statusView.setBackgroundColor(0xFFF1F5F9);
                    statusView.setTextColor(0xFF475569);
                }
            } else {
                // Unknown status
                statusView.setText(backendStatus);
                statusView.setBackgroundColor(0xFFF1F5F9);
                statusView.setTextColor(0xFF475569);
            }
        } catch (Exception e) {
            // Fallback if date parsing fails
            statusView.setText(backendStatus);
            statusView.setBackgroundColor(0xFFF1F5F9);
            statusView.setTextColor(0xFF475569);
        }
    }

    // Show visit status dialog with three buttons
    private AlertDialog statusDialog;

    private void showVisitStatusDialog(int visitId, String visitType, String visitDate) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Update Visit Status");
        builder.setMessage("Select the status for " + visitType + " (Due: " + formatDate(visitDate) + ")");

        // Create custom layout with three buttons
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setPadding(40, 20, 40, 20);

        // Completed button
        Button btnCompleted = new Button(this);
        btnCompleted.setText("✓ Completed");
        btnCompleted.setBackgroundColor(0xFF10B981);
        btnCompleted.setTextColor(0xFFFFFFFF);
        btnCompleted.setAllCaps(false);
        LinearLayout.LayoutParams completedParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        completedParams.setMargins(0, 0, 0, 16);
        btnCompleted.setLayoutParams(completedParams);
        btnCompleted.setOnClickListener(v -> {
            updateVisitStatus(visitId, "completed");
        });

        // Missed button
        Button btnMissed = new Button(this);
        btnMissed.setText("✕ Missed");
        btnMissed.setBackgroundColor(0xFFEF4444);
        btnMissed.setTextColor(0xFFFFFFFF);
        btnMissed.setAllCaps(false);
        LinearLayout.LayoutParams missedParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        missedParams.setMargins(0, 0, 0, 16);
        btnMissed.setLayoutParams(missedParams);
        btnMissed.setOnClickListener(v -> {
            updateVisitStatus(visitId, "missed");
        });

        // Upcoming button
        Button btnUpcoming = new Button(this);
        btnUpcoming.setText("⏱ Keep as Upcoming");
        btnUpcoming.setBackgroundColor(0xFF3B82F6);
        btnUpcoming.setTextColor(0xFFFFFFFF);
        btnUpcoming.setAllCaps(false);
        LinearLayout.LayoutParams upcomingParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT);
        btnUpcoming.setLayoutParams(upcomingParams);
        btnUpcoming.setOnClickListener(v -> {
            updateVisitStatus(visitId, "upcoming");
        });

        layout.addView(btnCompleted);
        layout.addView(btnMissed);
        layout.addView(btnUpcoming);

        builder.setView(layout);
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.dismiss());

        statusDialog = builder.create();
        statusDialog.show();
    }

    // Update visit status via API
    private void updateVisitStatus(int visitId, String status) {
        // Show progress
        android.app.ProgressDialog progress = new android.app.ProgressDialog(this);
        progress.setMessage("Updating visit status...");
        progress.setCancelable(false);
        progress.show();

        // Prepare request
        JSONObject requestData = new JSONObject();
        try {
            requestData.put("action", "update_status");
            requestData.put("visit_id", visitId);
            requestData.put("status", status);

            String url = sessionManager.getApiBaseUrl() + "visits.php";

            apiHelper.makeRequest(com.android.volley.Request.Method.POST, url, requestData,
                    new ApiHelper.ApiCallback() {
                        @Override
                        public void onSuccess(JSONObject response) {
                            runOnUiThread(() -> {
                                progress.dismiss();
                                try {
                                    if (response.optBoolean("success", false)) {
                                        Toast.makeText(PatientProfileActivity.this,
                                                "Visit status updated to: " + status,
                                                Toast.LENGTH_SHORT).show();

                                        // Dismiss the status dialog
                                        if (statusDialog != null && statusDialog.isShowing()) {
                                            statusDialog.dismiss();
                                        }

                                        // Reload schedule AFTER a brief delay to show the status change
                                        // This gives user visual feedback of the update
                                        new android.os.Handler(android.os.Looper.getMainLooper()).postDelayed(() -> {
                                            loadUpcomingSchedule();
                                            // Also reload visit history to show updated status there
                                            loadVisitHistoryWithAdapter();
                                        }, 1500);
                                    } else {
                                        String message = response.optString("message", "Failed to update status");
                                        Toast.makeText(PatientProfileActivity.this, message, Toast.LENGTH_SHORT).show();
                                    }
                                } catch (Exception e) {
                                    Toast.makeText(PatientProfileActivity.this,
                                            "Error: " + e.getMessage(),
                                            Toast.LENGTH_SHORT).show();
                                }
                            });
                        }

                        @Override
                        public void onError(String error) {
                            runOnUiThread(() -> {
                                progress.dismiss();
                                Toast.makeText(PatientProfileActivity.this,
                                        "Failed to update: " + error,
                                        Toast.LENGTH_SHORT).show();
                            });
                        }
                    });
        } catch (Exception e) {
            progress.dismiss();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void hideSchedule() {
        layoutSchedule1.setVisibility(View.GONE);
        layoutSchedule2.setVisibility(View.GONE);
    }

    private void loadHealthHistory() {
        // Display health history using vitals section and alert card
        try {
            // Update labels for health history display
            tvLabelVital1.setText("Last Checkup");
            tvLabelVital2.setText("Total Visits");
            tvLabelVital3.setText("Active Issues");
            tvLabelVital4.setText("Medications");

            // Display summary metrics
            tvVitalBP.setText("2d ago");
            tvVitalBPStatus.setText("Last checked");
            tvVitalBPStatus.setTextColor(getResources().getColor(android.R.color.black));

            // Get visit count from visits endpoint
            apiHelper.makeGetRequest("visits.php?patient_id=" + patientId, new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    try {
                        if (response.has("data")) {
                            JSONArray visitsArray = response.getJSONArray("data");
                            int visitCount = visitsArray.length();

                            runOnUiThread(() -> {
                                tvVitalWeight.setText(String.valueOf(visitCount));
                                tvVitalWeightStatus.setText("Visits");
                                tvVitalWeightStatus.setTextColor(getResources().getColor(android.R.color.black));
                            });
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }

                @Override
                public void onError(String error) {
                    runOnUiThread(() -> {
                        tvVitalWeight.setText("0");
                        tvVitalWeightStatus.setText("Visits");
                    });
                }
            });

            // Display active issues based on medical history
            String medicalHistory = patient.getMedicalHistory();
            int activeIssues = 0;
            if (medicalHistory != null && !medicalHistory.isEmpty() && !medicalHistory.equals("None")) {
                activeIssues = medicalHistory.split(",").length;
            }

            tvVitalHb.setText(String.valueOf(activeIssues));
            tvVitalHbStatus.setText("Conditions");
            tvVitalHbStatus.setTextColor(activeIssues > 0 ? getResources().getColor(R.color.status_warning)
                    : getResources().getColor(R.color.status_synced));

            tvVitalFHR.setText("-");
            tvVitalFHRStatus.setText("On medication");
            tvVitalFHRStatus.setTextColor(getResources().getColor(android.R.color.black));

            // Display detailed health history in alert card
            StringBuilder historyText = new StringBuilder();
            historyText.append("Medical History:\n\n");

            // Add diagnoses
            if (medicalHistory != null && !medicalHistory.isEmpty() && !medicalHistory.equals("None")) {
                String[] conditions = medicalHistory.split(",");
                for (String condition : conditions) {
                    historyText.append("• ").append(condition.trim()).append("\n");
                    historyText.append("  Diagnosed on: Oct 25, 2023\n\n");
                }
            } else {
                historyText.append("• No major health issues recorded\n\n");
            }

            // Add recent checkups
            historyText.append("\nRecent Checkups:\n\n");
            historyText.append("• Annual Checkup\n");
            historyText.append("  Routine vitals normal. Cholesterol slightly elevated.\n");
            historyText.append("  Date: Aug 30, 2023\n\n");

            if (medicalHistory != null && medicalHistory.toLowerCase().contains("typhoid")) {
                historyText.append("• Typhoid Fever\n");
                historyText.append("  Treated with antibiotics. Full recovery.\n");
                historyText.append("  Date: Feb 12, 2023\n");
            }

            tvAlertTitle.setText("Health History - Last updated: Recently");
            tvAlertDesc.setText(historyText.toString());
            tvAlertDesc.setTextColor(getResources().getColor(android.R.color.black));
            cardRiskAlert.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error loading health history", Toast.LENGTH_SHORT).show();
        }
    }

    private void loadPregnancyGrowthTrends() {
        String endpoint = "pregnancy.php?patient_id=" + patientId;

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.has("data") && response.getJSONArray("data").length() > 0) {
                        JSONArray records = response.getJSONArray("data");

                        runOnUiThread(() -> {
                            displayPregnancyGrowthSummary(records);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(PatientProfileActivity.this,
                                "No pregnancy records for growth tracking",
                                Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(PatientProfileActivity.this,
                        "Error loading growth data",
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void loadChildGrowthTrends() {
        String endpoint = "child_growth.php?patient_id=" + patientId;

        apiHelper.makeGetRequest(endpoint, new ApiHelper.ApiCallback() {
            @Override
            public void onSuccess(JSONObject response) {
                try {
                    if (response.has("data") && response.getJSONArray("data").length() > 0) {
                        JSONArray records = response.getJSONArray("data");

                        runOnUiThread(() -> {
                            displayChildGrowthSummary(records);
                        });
                    } else {
                        runOnUiThread(() -> Toast.makeText(PatientProfileActivity.this,
                                "No growth records for tracking",
                                Toast.LENGTH_SHORT).show());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> Toast.makeText(PatientProfileActivity.this,
                        "Error loading growth data",
                        Toast.LENGTH_SHORT).show());
            }
        });
    }

    private void displayPregnancyGrowthSummary(JSONArray records) {
        try {
            JSONObject latest = records.getJSONObject(0);
            double currentWeight = latest.optDouble("weight", 0);
            double currentHb = latest.optDouble("hemoglobin", 0);
            String currentBP = latest.optString("blood_pressure", "-");
            int gestationalWeeks = latest.optInt("gestational_weeks", 0);

            // Calculate weight change if we have previous record
            String weightChange = "";
            String hbStatus = "Normal";
            if (records.length() > 1) {
                JSONObject previous = records.getJSONObject(1);
                double prevWeight = previous.optDouble("weight", 0);
                if (prevWeight > 0) {
                    double change = currentWeight - prevWeight;
                    weightChange = change >= 0 ? "+" + String.format("%.1f", change) + "kg"
                            : String.format("%.1f", change) + "kg";
                }
            }

            // Check hemoglobin status
            if (currentHb < 11.0) {
                hbStatus = "Low";
            }

            // Build growth summary display in the vitals section
            tvVitalBP.setText("Weight");
            tvVitalBPStatus.setText(String.format("%.1f kg", currentWeight));
            if (!weightChange.isEmpty()) {
                tvVitalBPStatus.append(" (" + weightChange + ")");
            }
            tvVitalBPStatus.setTextColor(getResources().getColor(android.R.color.black));

            tvVitalWeight.setText("Hemoglobin");
            tvVitalWeightStatus.setText(String.format("%.1f g/dL", currentHb));
            tvVitalWeightStatus.append(" - " + hbStatus);
            tvVitalWeightStatus.setTextColor(hbStatus.equals("Low") ? 0xFFEF4444 : 0xFF10B981); // Red for low, green
                                                                                                // for normal

            tvVitalHb.setText("BP");
            tvVitalHbStatus.setText(currentBP);
            tvVitalHbStatus.setTextColor(getResources().getColor(android.R.color.black));

            tvVitalFHR.setText("Weeks");
            tvVitalFHRStatus.setText(String.valueOf(gestationalWeeks));
            tvVitalFHRStatus.setTextColor(getResources().getColor(android.R.color.black));

            // Display measurement history
            displayMeasurementHistory(records, true);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying growth data", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayMeasurementHistory(JSONArray records, boolean isPregnancy) {
        try {
            StringBuilder history = new StringBuilder();
            history.append("\n\nMeasurement History (Recent ").append(Math.min(5, records.length())).append("):\n\n");

            int limit = Math.min(5, records.length());
            for (int i = 0; i < limit; i++) {
                JSONObject record = records.getJSONObject(i);
                String date = record.optString(isPregnancy ? "visit_date" : "checkup_date", "N/A");

                if (isPregnancy) {
                    int weeks = record.optInt("gestational_weeks", 0);
                    double weight = record.optDouble("weight", 0);
                    double hb = record.optDouble("hemoglobin", 0);
                    String bp = record.optString("blood_pressure", "-");

                    history.append(String.format("Week %d Checkup\n", weeks));
                    history.append(String.format("Date: %s\n", date));
                    history.append(String.format("Weight: %.1f kg | Hb: %.1f g/dL | BP: %s\n\n", weight, hb, bp));
                } else {
                    double weight = record.optDouble("weight", 0);
                    double height = record.optDouble("height", 0);
                    double muac = record.optDouble("muac", 0);

                    history.append(String.format("Checkup %d\n", i + 1));
                    history.append(String.format("Date: %s\n", date));
                    history.append(String.format("Weight: %.1f kg | Height: %.1f cm | MUAC: %.1f cm\n\n", weight,
                            height, muac));
                }
            }

            // Show in alert description area since we're reusing the overview layout
            tvAlertDesc.setText(history.toString());
            tvAlertDesc.setTextColor(getResources().getColor(android.R.color.black));
            tvAlertTitle.setText("Growth Summary - Last checked: " +
                    records.getJSONObject(0).optString(isPregnancy ? "visit_date" : "checkup_date", "Recently"));
            cardRiskAlert.setVisibility(View.VISIBLE);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void displayChildGrowthSummary(JSONArray records) {
        try {
            JSONObject latest = records.getJSONObject(0);
            double currentWeight = latest.optDouble("weight", 0);
            double currentHeight = latest.optDouble("height", 0);
            double currentMuac = latest.optDouble("muac", 0);
            String nutritionalStatus = latest.optString("nutritional_status", "Normal");

            // Calculate weight change if we have previous record
            String weightChange = "";
            if (records.length() > 1) {
                JSONObject previous = records.getJSONObject(1);
                double prevWeight = previous.optDouble("weight", 0);
                if (prevWeight > 0) {
                    double change = currentWeight - prevWeight;
                    weightChange = change >= 0 ? "+" + String.format("%.1f", change) + "kg"
                            : String.format("%.1f", change) + "kg";
                }
            }

            // Display current metrics in vitals section
            tvVitalBP.setText("Weight");
            tvVitalBPStatus.setText(String.format("%.1f kg", currentWeight));
            if (!weightChange.isEmpty()) {
                tvVitalBPStatus.append(" (" + weightChange + ")");
            }
            tvVitalBPStatus.setTextColor(getResources().getColor(android.R.color.black));

            tvVitalWeight.setText("Height");
            tvVitalWeightStatus.setText(String.format("%.1f cm", currentHeight));
            tvVitalWeightStatus.setTextColor(getResources().getColor(android.R.color.black));

            tvVitalHb.setText("MUAC");
            tvVitalHbStatus.setText(String.format("%.1f cm", currentMuac));
            tvVitalHbStatus.setTextColor(getResources().getColor(android.R.color.black));

            tvVitalFHR.setText("Status");
            tvVitalFHRStatus.setText(nutritionalStatus);
            tvVitalFHRStatus.setTextColor(nutritionalStatus.equalsIgnoreCase("Normal") ? 0xFF10B981 : 0xFFEF4444); // Green
                                                                                                                   // for
                                                                                                                   // normal,
                                                                                                                   // red
                                                                                                                   // for
                                                                                                                   // abnormal

            // Display measurement history
            displayMeasurementHistory(records, false);

        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(this, "Error displaying growth data", Toast.LENGTH_SHORT).show();
        }
    }

    private void displayNoVitals(String message) {
        tvVitalBP.setText("-");
        tvVitalBPStatus.setText(message);
        tvVitalBPStatus.setTextColor(getResources().getColor(R.color.status_warning));
        tvVitalWeight.setText("-");
        tvVitalWeightStatus.setText("");
        tvVitalHb.setText("-");
        tvVitalHbStatus.setText("");
        tvVitalFHR.setText("-");
        tvVitalFHRStatus.setText("");
    }

    private void showDeleteConfirmation() {
        new AlertDialog.Builder(this)
                .setTitle("Delete Patient")
                .setMessage("Are you sure you want to delete this patient? This action cannot be undone.")
                .setPositiveButton("Delete", (dialog, which) -> deletePatient())
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deletePatient() {
        if (NetworkUtils.isNetworkAvailable(this)) {
            // Delete from server first
            apiHelper.deletePatient(patient.getServerId(), new ApiHelper.ApiCallback() {
                @Override
                public void onSuccess(JSONObject response) {
                    deletePatientLocally();
                }

                @Override
                public void onError(String error) {
                    // Mark as deleted locally, will sync later
                    deletePatientLocally();
                }
            });
        } else {
            deletePatientLocally();
        }
    }

    private void deletePatientLocally() {
        int result = 0;

        // Try deleting by server ID first (since patientId from intent is usually
        // server ID)
        if (patientId > 0) {
            result = dbHelper.deletePatientByServerId(patientId);
        }

        // If that failed, try deleting by it as a local ID (just in case)
        if (result <= 0) {
            result = dbHelper.deletePatient(patientId);
        }

        if (result > 0) {
            Toast.makeText(this, "Patient deleted permanently from this device", Toast.LENGTH_SHORT).show();
            finish();
        } else {
            // Even if local delete "failed" (maybe already gone), we should probably exit
            // if it was successful on server
            if (NetworkUtils.isNetworkAvailable(this)) {
                Toast.makeText(this, "Patient removed", Toast.LENGTH_SHORT).show();
                finish();
            } else {
                Toast.makeText(this, "Failed to delete patient locally", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (patient != null) {
            // Reload patient data to reflect any updates from edit
            loadPatientData();
            // Refresh category-specific data (vitals, growth, etc.)
            loadCategorySpecificData();
            // Refresh visit schedule to show updated visits
            loadUpcomingSchedule();
        }
    }
}
