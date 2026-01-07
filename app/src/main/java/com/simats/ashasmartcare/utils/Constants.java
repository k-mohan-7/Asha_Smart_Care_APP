package com.simats.ashasmartcare.utils;

/**
 * Constants class for API endpoints and other app constants
 */
public class Constants {

    // API Base URL - update with your server IP
    public static final String API_BASE_URL = "http://10.190.92.63/asha_api/";  // Your local network IP

    // API Endpoints
    public static final String API_LOGIN = "auth.php";
    public static final String API_REGISTER = "auth.php";
    public static final String API_PATIENTS = "patients.php";
    public static final String API_PREGNANCY_VISITS = "pregnancy_visits.php";
    public static final String API_CHILD_GROWTH = "child_growth.php";
    public static final String API_VACCINATIONS = "vaccinations.php";
    public static final String API_VISITS = "visits.php";
    public static final String API_SYNC = "sync.php";
    public static final String API_DASHBOARD = "dashboard.php";

    // Patient Categories
    public static final String CATEGORY_CHILD = "Child";
    public static final String CATEGORY_PREGNANT = "Pregnant Woman";
    public static final String CATEGORY_LACTATING = "Lactating Mother";
    public static final String CATEGORY_ADOLESCENT = "Adolescent Girl";
    public static final String CATEGORY_GENERAL = "General";
    
    // Patient Categories Array for Spinner
    public static final String[] PATIENT_CATEGORIES = {
            "Pregnant Woman",
            "Child",
            "Lactating Mother",
            "Adolescent Girl",
            "General"
    };

    // Vaccination Status
    public static final String VACCINATION_DUE = "DUE";
    public static final String VACCINATION_UPCOMING = "UPCOMING";
    public static final String VACCINATION_OVERDUE = "OVERDUE";
    public static final String VACCINATION_COMPLETED = "COMPLETED";

    // Sync Status
    public static final String SYNC_PENDING = "PENDING";
    public static final String SYNC_SYNCED = "SYNCED";
    public static final String SYNC_FAILED = "FAILED";

    // Sync Actions
    public static final String ACTION_INSERT = "INSERT";
    public static final String ACTION_UPDATE = "UPDATE";
    public static final String ACTION_DELETE = "DELETE";

    // Growth Status
    public static final String GROWTH_NORMAL = "Normal";
    public static final String GROWTH_UNDERWEIGHT = "Underweight";
    public static final String GROWTH_OVERWEIGHT = "Overweight";
    public static final String GROWTH_STUNTED = "Stunted";

    // Visit Types
    public static final String VISIT_ROUTINE = "Routine Checkup";
    public static final String VISIT_PREGNANCY = "Pregnancy Visit";
    public static final String VISIT_CHILD_GROWTH = "Child Growth Checkup";
    public static final String VISIT_VACCINATION = "Vaccination";
    public static final String VISIT_EMERGENCY = "Emergency";
    public static final String VISIT_FOLLOW_UP = "Follow-up";

    // Date Formats
    public static final String DATE_FORMAT_DISPLAY = "dd MMM yyyy";
    public static final String DATE_FORMAT_DB = "yyyy-MM-dd";
    public static final String DATETIME_FORMAT_DB = "yyyy-MM-dd HH:mm:ss";

    // Intent Extras
    public static final String EXTRA_PATIENT_ID = "patient_id";
    public static final String EXTRA_PATIENT = "patient";
    public static final String EXTRA_VISIT_ID = "visit_id";
    public static final String EXTRA_CATEGORY = "category";
    public static final String EXTRA_MODE = "mode";

    // Request Codes
    public static final int REQUEST_ADD_PATIENT = 1001;
    public static final int REQUEST_EDIT_PATIENT = 1002;
    public static final int REQUEST_ADD_VISIT = 1003;
    public static final int REQUEST_ADD_VACCINATION = 1004;
    public static final int REQUEST_ADD_GROWTH = 1005;

    // Standard Vaccines Schedule (India National Immunization Schedule)
    public static final String[] CHILD_VACCINES = {
            "BCG",
            "OPV-0 (Birth dose)",
            "Hepatitis B (Birth dose)",
            "OPV-1",
            "Pentavalent-1",
            "Rotavirus-1",
            "PCV-1",
            "IPV-1",
            "OPV-2",
            "Pentavalent-2",
            "Rotavirus-2",
            "OPV-3",
            "Pentavalent-3",
            "Rotavirus-3",
            "IPV-2",
            "PCV-2",
            "Measles/MR-1",
            "Vitamin A (1st dose)",
            "JE-1",
            "PCV Booster",
            "DPT Booster-1",
            "OPV Booster",
            "Measles/MR-2",
            "JE-2",
            "Vitamin A (2nd-9th dose)",
            "DPT Booster-2",
            "Td (10 years)",
            "Td (16 years)"
    };

    // Indian States
    public static final String[] INDIAN_STATES = {
            "Andhra Pradesh",
            "Arunachal Pradesh",
            "Assam",
            "Bihar",
            "Chhattisgarh",
            "Goa",
            "Gujarat",
            "Haryana",
            "Himachal Pradesh",
            "Jharkhand",
            "Karnataka",
            "Kerala",
            "Madhya Pradesh",
            "Maharashtra",
            "Manipur",
            "Meghalaya",
            "Mizoram",
            "Nagaland",
            "Odisha",
            "Punjab",
            "Rajasthan",
            "Sikkim",
            "Tamil Nadu",
            "Telangana",
            "Tripura",
            "Uttar Pradesh",
            "Uttarakhand",
            "West Bengal",
            "Andaman and Nicobar Islands",
            "Chandigarh",
            "Dadra and Nagar Haveli and Daman and Diu",
            "Delhi",
            "Jammu and Kashmir",
            "Ladakh",
            "Lakshadweep",
            "Puducherry"
    };
}
