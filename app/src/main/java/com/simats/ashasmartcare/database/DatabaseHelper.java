package com.simats.ashasmartcare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.simats.ashasmartcare.models.ChildGrowth;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.PregnancyVisit;
import com.simats.ashasmartcare.models.SyncRecord;
import com.simats.ashasmartcare.models.Vaccination;
import com.simats.ashasmartcare.models.Visit;
import com.simats.ashasmartcare.utils.Constants;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for ASHA Healthcare App
 * Handles all local database operations with sync status tracking
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "asha_healthcare.db";
    private static final int DATABASE_VERSION = 11;

    // Sync Status Constants
    public static final String SYNC_PENDING = "PENDING";
    public static final String SYNC_SYNCED = "SYNCED";
    public static final String SYNC_FAILED = "FAILED";

    // Table Names
    public static final String TABLE_PATIENTS = "patients";
    public static final String TABLE_PREGNANCY_VISITS = "pregnancy_visits";
    public static final String TABLE_CHILD_GROWTH = "child_growth";
    public static final String TABLE_VACCINATIONS = "vaccinations";
    public static final String TABLE_VISITS = "visits";
    public static final String TABLE_SYNC_QUEUE = "sync_queue";
    public static final String TABLE_USERS = "users";
    public static final String TABLE_ALERTS_REVIEWED = "alerts_reviewed";

    // Common Column Names
    public static final String COL_LOCAL_ID = "local_id";
    public static final String COL_SERVER_ID = "server_id";
    public static final String COL_SYNC_STATUS = "sync_status";
    public static final String COL_LAST_UPDATED = "last_updated";
    public static final String COL_CREATED_AT = "created_at";

    // Patients Table Columns
    public static final String COL_NAME = "name";
    public static final String COL_AGE = "age";
    public static final String COL_DOB = "dob";
    public static final String COL_GENDER = "gender";
    public static final String COL_PHONE = "phone";
    public static final String COL_ADDRESS = "address";
    public static final String COL_BLOOD_GROUP = "blood_group";
    public static final String COL_CATEGORY = "category";
    public static final String COL_MEDICAL_NOTES = "medical_notes";
    public static final String COL_PHOTO_PATH = "photo_path";
    public static final String COL_IS_HIGH_RISK = "is_high_risk";
    public static final String COL_HIGH_RISK_REASON = "high_risk_reason";
    public static final String COL_ABHA_ID = "abha_id";

    // Pregnancy Visits Table Columns
    public static final String COL_PATIENT_ID = "patient_id";
    public static final String COL_VISIT_DATE = "visit_date";
    public static final String COL_EXPECTED_DELIVERY = "expected_delivery";
    public static final String COL_BP_SYSTOLIC = "bp_systolic";
    public static final String COL_BP_DIASTOLIC = "bp_diastolic";
    public static final String COL_WEIGHT = "weight";
    public static final String COL_COMPLAINTS = "complaints";
    public static final String COL_NOTES = "notes";
    public static final String COL_TRIMESTER = "trimester";

    // Child Growth Table Columns
    public static final String COL_HEIGHT = "height";
    public static final String COL_HEAD_CIRCUMFERENCE = "head_circumference";
    public static final String COL_GROWTH_STATUS = "growth_status";
    public static final String COL_RECORD_DATE = "record_date";

    // Vaccinations Table Columns
    public static final String COL_VACCINE_NAME = "vaccine_name";
    public static final String COL_DUE_DATE = "due_date";
    public static final String COL_GIVEN_DATE = "given_date";
    public static final String COL_STATUS = "status";
    public static final String COL_BATCH_NUMBER = "batch_number";

    // Visits Table Columns
    public static final String COL_VISIT_TYPE = "visit_type";
    public static final String COL_DESCRIPTION = "description";

    // Sync Queue Table Columns
    public static final String COL_TABLE_NAME = "table_name";
    public static final String COL_RECORD_ID = "record_id";
    public static final String COL_ACTION = "action";
    public static final String COL_DATA_JSON = "data_json";
    public static final String COL_ERROR_MESSAGE = "error_message";
    public static final String COL_RETRY_COUNT = "retry_count";

    // Users Table Columns
    public static final String COL_USER_ID = "user_id";
    public static final String COL_EMAIL = "email";
    public static final String COL_PASSWORD = "password";
    public static final String COL_WORKER_ID = "worker_id";
    public static final String COL_VILLAGE = "village";
    public static final String COL_PHC = "phc";
    public static final String COL_STATE = "state";
    public static final String COL_DISTRICT = "district";
    public static final String COL_AREA = "area";
    public static final String COL_IS_LOGGED_IN = "is_logged_in";

    // Create Table Statements
    private static final String CREATE_TABLE_PATIENTS = "CREATE TABLE " + TABLE_PATIENTS + "("
            + COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_SERVER_ID + " INTEGER,"
            + COL_NAME + " TEXT NOT NULL,"
            + COL_AGE + " INTEGER,"
            + COL_DOB + " TEXT,"
            + COL_GENDER + " TEXT,"
            + COL_PHONE + " TEXT,"
            + COL_ADDRESS + " TEXT,"
            + COL_BLOOD_GROUP + " TEXT,"
            + COL_CATEGORY + " TEXT,"
            + COL_MEDICAL_NOTES + " TEXT,"
            + COL_PHOTO_PATH + " TEXT,"
            + COL_IS_HIGH_RISK + " INTEGER DEFAULT 0,"
            + COL_HIGH_RISK_REASON + " TEXT,"
            + COL_ABHA_ID + " TEXT,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_PREGNANCY_VISITS = "CREATE TABLE " + TABLE_PREGNANCY_VISITS + "("
            + COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_SERVER_ID + " INTEGER,"
            + COL_PATIENT_ID + " INTEGER NOT NULL,"
            + COL_VISIT_DATE + " TEXT NOT NULL,"
            + COL_EXPECTED_DELIVERY + " TEXT,"
            + COL_BP_SYSTOLIC + " INTEGER,"
            + COL_BP_DIASTOLIC + " INTEGER,"
            + COL_WEIGHT + " REAL,"
            + COL_COMPLAINTS + " TEXT,"
            + COL_NOTES + " TEXT,"
            + COL_TRIMESTER + " INTEGER,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COL_PATIENT_ID + ") REFERENCES " + TABLE_PATIENTS + "(" + COL_LOCAL_ID + ")"
            + ")";

    private static final String CREATE_TABLE_CHILD_GROWTH = "CREATE TABLE " + TABLE_CHILD_GROWTH + "("
            + COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_SERVER_ID + " INTEGER,"
            + COL_PATIENT_ID + " INTEGER NOT NULL,"
            + COL_RECORD_DATE + " TEXT NOT NULL,"
            + COL_WEIGHT + " REAL,"
            + COL_HEIGHT + " REAL,"
            + COL_HEAD_CIRCUMFERENCE + " REAL,"
            + COL_GROWTH_STATUS + " TEXT,"
            + COL_NOTES + " TEXT,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COL_PATIENT_ID + ") REFERENCES " + TABLE_PATIENTS + "(" + COL_LOCAL_ID + ")"
            + ")";

    private static final String CREATE_TABLE_VACCINATIONS = "CREATE TABLE " + TABLE_VACCINATIONS + "("
            + COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_SERVER_ID + " INTEGER,"
            + COL_PATIENT_ID + " INTEGER NOT NULL,"
            + COL_VACCINE_NAME + " TEXT NOT NULL,"
            + COL_DUE_DATE + " TEXT,"
            + COL_GIVEN_DATE + " TEXT,"
            + COL_STATUS + " TEXT,"
            + COL_BATCH_NUMBER + " TEXT,"
            + COL_NOTES + " TEXT,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COL_PATIENT_ID + ") REFERENCES " + TABLE_PATIENTS + "(" + COL_LOCAL_ID + ")"
            + ")";

    private static final String CREATE_TABLE_VISITS = "CREATE TABLE " + TABLE_VISITS + "("
            + COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_SERVER_ID + " INTEGER,"
            + COL_PATIENT_ID + " INTEGER NOT NULL,"
            + COL_VISIT_DATE + " TEXT NOT NULL,"
            + COL_VISIT_TYPE + " TEXT,"
            + COL_DESCRIPTION + " TEXT,"
            + COL_NOTES + " TEXT,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + "FOREIGN KEY(" + COL_PATIENT_ID + ") REFERENCES " + TABLE_PATIENTS + "(" + COL_LOCAL_ID + ")"
            + ")";

    private static final String CREATE_TABLE_SYNC_QUEUE = "CREATE TABLE " + TABLE_SYNC_QUEUE + "("
            + COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_TABLE_NAME + " TEXT NOT NULL,"
            + COL_RECORD_ID + " INTEGER NOT NULL,"
            + COL_ACTION + " TEXT NOT NULL,"
            + COL_DATA_JSON + " TEXT,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_ERROR_MESSAGE + " TEXT,"
            + COL_RETRY_COUNT + " INTEGER DEFAULT 0,"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_USERS = "CREATE TABLE " + TABLE_USERS + "("
            + COL_LOCAL_ID + " INTEGER PRIMARY KEY AUTOINCREMENT,"
            + COL_SERVER_ID + " INTEGER,"
            + COL_NAME + " TEXT,"
            + COL_EMAIL + " TEXT,"
            + COL_PHONE + " TEXT,"
            + COL_PASSWORD + " TEXT,"
            + COL_WORKER_ID + " TEXT,"
            + COL_AGE + " INTEGER,"
            + COL_GENDER + " TEXT,"
            + COL_VILLAGE + " TEXT,"
            + COL_PHC + " TEXT,"
            + COL_STATE + " TEXT,"
            + COL_DISTRICT + " TEXT,"
            + COL_AREA + " TEXT,"
            + COL_STATUS + " TEXT,"
            + COL_IS_LOGGED_IN + " INTEGER DEFAULT 0,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP"
            + ")";

    private static final String CREATE_TABLE_ALERTS_REVIEWED = "CREATE TABLE " + TABLE_ALERTS_REVIEWED + "("
            + COL_PATIENT_ID + " INTEGER NOT NULL,"
            + COL_VISIT_TYPE + " TEXT NOT NULL," // Reuse visit_type for alert type category
            + "is_reviewed INTEGER DEFAULT 0,"
            + "PRIMARY KEY (" + COL_PATIENT_ID + ", " + COL_VISIT_TYPE + ")"
            + ")";

    // Singleton Instance
    private static DatabaseHelper instance;

    public static synchronized DatabaseHelper getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseHelper(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_PATIENTS);
        db.execSQL(CREATE_TABLE_PREGNANCY_VISITS);
        db.execSQL(CREATE_TABLE_CHILD_GROWTH);
        db.execSQL(CREATE_TABLE_VACCINATIONS);
        db.execSQL(CREATE_TABLE_VISITS);
        db.execSQL(CREATE_TABLE_SYNC_QUEUE);
        db.execSQL(CREATE_TABLE_USERS);
        db.execSQL(CREATE_TABLE_ALERTS_REVIEWED);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            // Version 2 -> 3 Migration (Original attempt)
            // We re-run this logic in case it failed or skipped, but safer to do it in
            // current check
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_IS_HIGH_RISK, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_HIGH_RISK_REASON, "TEXT");
        }

        if (oldVersion < 4) {
            // Version 3 -> 4 Migration (Retry/Ensure columns exist)
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_IS_HIGH_RISK, "INTEGER DEFAULT 0");
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_HIGH_RISK_REASON, "TEXT");
        }

        if (oldVersion < 5) {
            // Version 4 -> 5 Migration
            db.execSQL(CREATE_TABLE_ALERTS_REVIEWED);
        }

        if (oldVersion < 6) {
            // Version 5 -> 6 Migration: Ensure last_updated exists in all sync-relevant
            // tables
            // This column was added to CREATE statements but missed in migrations 3, 4, 5.
            String colDef = "DATETIME DEFAULT CURRENT_TIMESTAMP";
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_LAST_UPDATED, colDef);
            addColumnIfNotExists(db, TABLE_PREGNANCY_VISITS, COL_LAST_UPDATED, colDef);
            addColumnIfNotExists(db, TABLE_CHILD_GROWTH, COL_LAST_UPDATED, colDef);
            addColumnIfNotExists(db, TABLE_VACCINATIONS, COL_LAST_UPDATED, colDef);
            addColumnIfNotExists(db, TABLE_VISITS, COL_LAST_UPDATED, colDef);
            addColumnIfNotExists(db, TABLE_SYNC_QUEUE, COL_LAST_UPDATED, colDef);
            addColumnIfNotExists(db, TABLE_USERS, COL_LAST_UPDATED, colDef);
        }

        if (oldVersion < 7) {
            // Version 6 -> 7 Migration: Add status column to users table
            addColumnIfNotExists(db, TABLE_USERS, COL_STATUS, "TEXT");
        }

        if (oldVersion < 8) {
            // Version 7 -> 8 Migration: Ensure pregnancy_visits table exists
            createTableIfNotExists(db, TABLE_PREGNANCY_VISITS, CREATE_TABLE_PREGNANCY_VISITS);
        }

        if (oldVersion < 10) {
            // Version 9 -> 10 Migration: Add missing visit fields for sync compatibility
            addColumnIfNotExists(db, TABLE_VISITS, "purpose", "TEXT");
            addColumnIfNotExists(db, TABLE_VISITS, "findings", "TEXT");
            addColumnIfNotExists(db, TABLE_VISITS, "recommendations", "TEXT");
            addColumnIfNotExists(db, TABLE_VISITS, "next_visit_date", "TEXT");
        }

        if (oldVersion < 11) {
            // Version 10 -> 11 Migration: Add abha_id column to patients table
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_ABHA_ID, "TEXT");
        }

        if (oldVersion < 9) {
            // Version 8 -> 9 Migration: Schema alignment with backend
            // Add new columns
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_ADDRESS, "TEXT");
            addColumnIfNotExists(db, TABLE_PATIENTS, COL_BLOOD_GROUP, "TEXT");

            // Drop old columns (SQLite doesn't support DROP COLUMN before 3.35.0)
            // Instead, we'll migrate data to new table
            db.execSQL("CREATE TABLE patients_new ("
                    + "local_id INTEGER PRIMARY KEY AUTOINCREMENT,"
                    + "server_id INTEGER,"
                    + "name TEXT NOT NULL,"
                    + "age INTEGER,"
                    + "dob TEXT,"
                    + "gender TEXT,"
                    + "phone TEXT,"
                    + "address TEXT,"
                    + "blood_group TEXT,"
                    + "category TEXT,"
                    + "medical_notes TEXT,"
                    + "photo_path TEXT,"
                    + "is_high_risk INTEGER DEFAULT 0,"
                    + "high_risk_reason TEXT,"
                    + "sync_status TEXT DEFAULT 'PENDING',"
                    + "created_at DATETIME DEFAULT CURRENT_TIMESTAMP,"
                    + "last_updated DATETIME DEFAULT CURRENT_TIMESTAMP)");

            // Copy data (map state+district+area to address if address is null)
            db.execSQL("INSERT INTO patients_new SELECT "
                    + "local_id, server_id, name, age, dob, gender, phone, "
                    + "COALESCE(address, area || ', ' || district || ', ' || state, area, ''), "
                    + "NULL as blood_group, "
                    + "category, medical_notes, photo_path, is_high_risk, high_risk_reason, "
                    + "sync_status, created_at, last_updated FROM patients");

            // Replace old table
            db.execSQL("DROP TABLE patients");
            db.execSQL("ALTER TABLE patients_new RENAME TO patients");
        }
    }

    private void createTableIfNotExists(SQLiteDatabase db, String tableName, String createStatement) {
        Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name=?",
                new String[] { tableName });
        if (cursor.getCount() == 0) {
            db.execSQL(createStatement);
        }
        cursor.close();
    }

    private void addColumnIfNotExists(SQLiteDatabase db, String tableName, String columnName, String columnType) {
        try {
            db.execSQL("ALTER TABLE " + tableName + " ADD COLUMN " + columnName + " " + columnType);
        } catch (Exception e) {
            // Column likely already exists or other error.
            // SQLite doesn't support "IF NOT EXISTS" for ADD COLUMN in older versions
            // standardly,
            // but try-catch is the standard workaround.
        }
    }

    // ==================== PATIENT OPERATIONS ====================

    /**
     * Insert a new patient
     */
    public long insertPatient(Patient patient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, patient.getName());
        values.put(COL_AGE, patient.getAge());
        values.put(COL_DOB, patient.getDob());
        values.put(COL_GENDER, patient.getGender());
        values.put(COL_PHONE, patient.getPhone());
        values.put(COL_ADDRESS, patient.getAddress());
        values.put(COL_BLOOD_GROUP, patient.getBloodGroup());
        values.put(COL_CATEGORY, patient.getCategory());
        values.put(COL_MEDICAL_NOTES, patient.getMedicalNotes());
        values.put(COL_PHOTO_PATH, patient.getPhotoPath());
        values.put(COL_IS_HIGH_RISK, patient.isHighRisk() ? 1 : 0);
        values.put(COL_HIGH_RISK_REASON, patient.getHighRiskReason());
        values.put(COL_SYNC_STATUS, patient.getSyncStatus());
        values.put(COL_SERVER_ID, patient.getServerId());

        long id = db.insert(TABLE_PATIENTS, null, values);

        if (id > 0) {
            addToSyncQueue(TABLE_PATIENTS, id, "INSERT");
        }

        return id;
    }

    /**
     * Get all patients
     */
    public List<Patient> getAllPatients() {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " ORDER BY " + COL_NAME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return patients;
    }

    /**
     * Get patients by category
     */
    public List<Patient> getPatientsByCategory(String category) {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_CATEGORY + " = ? ORDER BY " + COL_NAME
                + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { category });

        if (cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return patients;
    }

    /**
     * Get patient by ID
     */
    public Patient getPatientById(long localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_LOCAL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(localId) });

        Patient patient = null;
        if (cursor.moveToFirst()) {
            patient = cursorToPatient(cursor);
        }
        cursor.close();
        return patient;
    }

    /**
     * Update patient
     */
    /**
     * Update patient
     */
    public int updatePatient(Patient patient) {
        return updatePatient(patient, true);
    }

    public int updatePatient(Patient patient, boolean addToSyncQueue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, patient.getName());
        values.put(COL_AGE, patient.getAge());
        values.put(COL_DOB, patient.getDob());
        values.put(COL_GENDER, patient.getGender());
        values.put(COL_PHONE, patient.getPhone());
        values.put(COL_ADDRESS, patient.getAddress());
        values.put(COL_BLOOD_GROUP, patient.getBloodGroup());
        values.put(COL_CATEGORY, patient.getCategory());
        values.put(COL_MEDICAL_NOTES, patient.getMedicalNotes());
        values.put(COL_PHOTO_PATH, patient.getPhotoPath());
        values.put(COL_IS_HIGH_RISK, patient.isHighRisk() ? 1 : 0);
        values.put(COL_HIGH_RISK_REASON, patient.getHighRiskReason());
        values.put(COL_ABHA_ID, patient.getAbhaId());
        values.put(COL_SYNC_STATUS, patient.getSyncStatus());
        values.put(COL_SERVER_ID, patient.getServerId());
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        int rows = db.update(TABLE_PATIENTS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(patient.getLocalId()) });

        if (rows > 0 && addToSyncQueue) {
            String dataJson = null;
            try {
                org.json.JSONObject json = new org.json.JSONObject();
                json.put("name", patient.getName());
                dataJson = json.toString();
            } catch (Exception e) {
            }
            addToSyncQueue(TABLE_PATIENTS, patient.getLocalId(), "UPDATE", dataJson);
        }

        return rows;
    }

    /**
     * Delete patient
     */
    public int deletePatient(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        // Also delete related data
        db.delete(TABLE_CHILD_GROWTH, COL_PATIENT_ID + " = ?", new String[] { String.valueOf(localId) });
        db.delete(TABLE_VACCINATIONS, COL_PATIENT_ID + " = ?", new String[] { String.valueOf(localId) });
        db.delete(TABLE_VISITS, COL_PATIENT_ID + " = ?", new String[] { String.valueOf(localId) });
        db.delete(TABLE_PREGNANCY_VISITS, COL_PATIENT_ID + " = ?", new String[] { String.valueOf(localId) });
        db.delete(TABLE_SYNC_QUEUE, COL_TABLE_NAME + " = ? AND " + COL_RECORD_ID + " = ?",
                new String[] { TABLE_PATIENTS, String.valueOf(localId) });

        int rows = db.delete(TABLE_PATIENTS, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(localId) });
        return rows;
    }

    /**
     * Delete patient by server ID
     */
    public int deletePatientByServerId(int serverId) {
        Patient patient = getPatientByServerId(serverId);
        if (patient != null) {
            return deletePatient(patient.getLocalId());
        }
        return 0;
    }

    /**
     * Delete Child Growth record
     */
    public int deleteChildGrowth(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_TABLE_NAME + " = ? AND " + COL_RECORD_ID + " = ?",
                new String[] { TABLE_CHILD_GROWTH, String.valueOf(localId) });
        return db.delete(TABLE_CHILD_GROWTH, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(localId) });
    }

    /**
     * Delete Vaccination record
     */
    public int deleteVaccinationRecord(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_TABLE_NAME + " = ? AND " + COL_RECORD_ID + " = ?",
                new String[] { TABLE_VACCINATIONS, String.valueOf(localId) });
        return db.delete(TABLE_VACCINATIONS, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(localId) });
    }

    /**
     * Delete Visit record
     */
    public int deleteVisit(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_TABLE_NAME + " = ? AND " + COL_RECORD_ID + " = ?",
                new String[] { TABLE_VISITS, String.valueOf(localId) });
        return db.delete(TABLE_VISITS, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(localId) });
    }

    /**
     * Delete Pregnancy Visit record
     */
    public int deletePregnancyVisit(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_TABLE_NAME + " = ? AND " + COL_RECORD_ID + " = ?",
                new String[] { TABLE_PREGNANCY_VISITS, String.valueOf(localId) });
        return db.delete(TABLE_PREGNANCY_VISITS, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(localId) });
    }

    /**
     * Insert or update patient
     */
    public long insertOrUpdatePatient(Patient patient) {
        if (patient.getLocalId() > 0) {
            updatePatient(patient);
            return patient.getLocalId();
        } else if (patient.getServerId() > 0) {
            // Check if exists by server ID
            Patient existing = getPatientByServerId(patient.getServerId());
            if (existing != null) {
                patient.setLocalId(existing.getLocalId());
                updatePatient(patient);
                return patient.getLocalId();
            }
        }
        return insertPatient(patient);
    }

    /**
     * Get patient by server ID
     */
    public Patient getPatientByServerId(int serverId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_SERVER_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(serverId) });

        Patient patient = null;
        if (cursor.moveToFirst()) {
            patient = cursorToPatient(cursor);
        }
        cursor.close();
        return patient;
    }

    /**
     * Get pending patients for sync
     */
    public List<Patient> getPendingPatients() {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_SYNC_STATUS + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { SYNC_PENDING });

        if (cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return patients;
    }

    /**
     * Mark patient as synced
     */
    public void markPatientSynced(long localId, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_SERVER_ID, serverId);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_PATIENTS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localId) });
    }

    public int getPatientCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS, null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Get patient count
     */
    public int getHighRiskPatientsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_PATIENTS, new String[] { COL_LOCAL_ID },
                COL_IS_HIGH_RISK + " = 1", null, null, null, null);
        int count = cursor.getCount();
        cursor.close();
        return count;
    }

    /**
     * Get list of overdue vaccination alerts
     */
    public List<com.simats.ashasmartcare.models.HighRiskAlert> getOverdueVaccinationAlerts() {
        List<com.simats.ashasmartcare.models.HighRiskAlert> alerts = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT p." + COL_NAME + ", p." + COL_AREA + ", v." + COL_VACCINE_NAME + ", v." + COL_DUE_DATE
                + ", p." + COL_LOCAL_ID
                + " FROM " + TABLE_VACCINATIONS + " v "
                + " JOIN " + TABLE_PATIENTS + " p ON v." + COL_PATIENT_ID + " = p." + COL_LOCAL_ID
                + " WHERE (v." + COL_STATUS + " = 'Upcoming' OR v." + COL_STATUS + " = 'Scheduled')"
                + " AND v." + COL_DUE_DATE + " < date('now')";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String area = cursor.getString(1);
                String vaccine = cursor.getString(2);
                String dueDate = cursor.getString(3);
                long patientId = cursor.getLong(4);

                String alertType = "Overdue Vaccine: " + vaccine + " (Due: " + dueDate + ")";
                com.simats.ashasmartcare.models.HighRiskAlert alert = new com.simats.ashasmartcare.models.HighRiskAlert(
                        patientId, name, area, alertType);
                alert.setReviewed(isAlertReviewed(patientId, alertType));
                alerts.add(alert);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alerts;
    }

    /**
     * Get list of children with potential growth risks based on weight/height
     */
    public List<com.simats.ashasmartcare.models.HighRiskAlert> getChildGrowthRiskAlerts() {
        List<com.simats.ashasmartcare.models.HighRiskAlert> alerts = new java.util.ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        // Simplified Logic: Weight < 2.5kg for any child or significant drop (complex)
        String query = "SELECT p." + COL_NAME + ", p." + COL_AREA + ", g." + COL_WEIGHT + ", g." + COL_GROWTH_STATUS
                + ", p." + COL_LOCAL_ID
                + " FROM " + TABLE_CHILD_GROWTH + " g "
                + " JOIN " + TABLE_PATIENTS + " p ON g." + COL_PATIENT_ID + " = p." + COL_LOCAL_ID
                + " WHERE CAST(g." + COL_WEIGHT + " AS REAL) < 2.5";

        Cursor cursor = db.rawQuery(query, null);
        if (cursor.moveToFirst()) {
            do {
                String name = cursor.getString(0);
                String area = cursor.getString(1);
                float weight = cursor.getFloat(2);
                long patientId = cursor.getLong(4);

                String alertType = "Low Weight Alert: " + weight + "kg";
                com.simats.ashasmartcare.models.HighRiskAlert alert = new com.simats.ashasmartcare.models.HighRiskAlert(
                        patientId, name, area, alertType);
                alert.setReviewed(isAlertReviewed(patientId, alertType));
                alerts.add(alert);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return alerts;
    }

    /**
     * Search patients by name
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_NAME + " LIKE ? ORDER BY " + COL_NAME
                + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { "%" + searchTerm + "%" });

        if (cursor.moveToFirst()) {
            do {
                patients.add(cursorToPatient(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return patients;
    }

    private Patient cursorToPatient(Cursor cursor) {
        Patient patient = new Patient();
        patient.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCAL_ID)));
        patient.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
        patient.setName(cursor.getString(cursor.getColumnIndexOrThrow(COL_NAME)));
        patient.setAge(cursor.getInt(cursor.getColumnIndexOrThrow(COL_AGE)));
        patient.setDob(cursor.getString(cursor.getColumnIndexOrThrow(COL_DOB)));
        patient.setGender(cursor.getString(cursor.getColumnIndexOrThrow(COL_GENDER)));
        patient.setPhone(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHONE)));

        // New schema columns
        int addressIndex = cursor.getColumnIndex(COL_ADDRESS);
        if (addressIndex != -1) {
            patient.setAddress(cursor.getString(addressIndex));
        }

        int bloodGroupIndex = cursor.getColumnIndex(COL_BLOOD_GROUP);
        if (bloodGroupIndex != -1) {
            patient.setBloodGroup(cursor.getString(bloodGroupIndex));
        }

        patient.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        patient.setMedicalNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDICAL_NOTES)));
        patient.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_PATH)));

        // Handle new columns with safety checks for older DB versions/migrations if
        // column doesn't exist
        int highRiskIndex = cursor.getColumnIndex(COL_IS_HIGH_RISK);
        if (highRiskIndex != -1) {
            patient.setHighRisk(cursor.getInt(highRiskIndex) == 1);
        }

        int reasonIndex = cursor.getColumnIndex(COL_HIGH_RISK_REASON);
        if (reasonIndex != -1) {
            patient.setHighRiskReason(cursor.getString(reasonIndex));
        }

        int abhaIdIndex = cursor.getColumnIndex(COL_ABHA_ID);
        if (abhaIdIndex != -1) {
            patient.setAbhaId(cursor.getString(abhaIdIndex));
        }

        patient.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
        patient.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));

        int lastUpdatedIndex = cursor.getColumnIndex(COL_LAST_UPDATED);
        if (lastUpdatedIndex != -1) {
            patient.setLastUpdated(cursor.getString(lastUpdatedIndex));
        } else {
            patient.setLastUpdated(patient.getCreatedAt()); // Fallback
        }

        return patient;
    }

    // ==================== PREGNANCY VISIT OPERATIONS ====================

    public long insertPregnancyVisit(PregnancyVisit visit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, visit.getPatientId());
        values.put(COL_VISIT_DATE, visit.getVisitDate());
        values.put(COL_WEIGHT, visit.getWeight());
        values.put(COL_NOTES, visit.getNotes());
        values.put(COL_SYNC_STATUS, visit.getSyncStatus());

        return db.insert(TABLE_PREGNANCY_VISITS, null, values);
    }

    public List<PregnancyVisit> getPregnancyVisitsByPatient(long patientId) {
        List<PregnancyVisit> visits = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PREGNANCY_VISITS + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY "
                + COL_VISIT_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(patientId) });

        if (cursor.moveToFirst()) {
            do {
                visits.add(cursorToPregnancyVisit(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }

    /**
     * Get pregnancy visits by patient ID (alias)
     */
    public List<PregnancyVisit> getPregnancyVisitsByPatientId(long patientId) {
        return getPregnancyVisitsByPatient(patientId);
    }

    /**
     * Insert or update pregnancy visit
     */
    public long insertOrUpdatePregnancyVisit(PregnancyVisit visit) {
        if (visit.getLocalId() > 0) {
            updatePregnancyVisit(visit);
            return visit.getLocalId();
        } else {
            return insertPregnancyVisit(visit);
        }
    }

    public List<PregnancyVisit> getAllPregnancyVisits() {
        List<PregnancyVisit> visits = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PREGNANCY_VISITS + " ORDER BY " + COL_VISIT_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                visits.add(cursorToPregnancyVisit(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }

    public List<PregnancyVisit> getPendingPregnancyVisits() {
        List<PregnancyVisit> visits = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PREGNANCY_VISITS + " WHERE " + COL_SYNC_STATUS + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { SYNC_PENDING });

        if (cursor.moveToFirst()) {
            do {
                visits.add(cursorToPregnancyVisit(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }

    public void markPregnancyVisitSynced(long localId, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_SERVER_ID, serverId);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_PREGNANCY_VISITS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localId) });
    }

    private PregnancyVisit cursorToPregnancyVisit(Cursor cursor) {
        PregnancyVisit visit = new PregnancyVisit();
        visit.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCAL_ID)));
        visit.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
        visit.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_PATIENT_ID)));
        visit.setVisitDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_VISIT_DATE)));
        visit.setWeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_WEIGHT)));
        visit.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
        visit.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
        return visit;
    }

    // ==================== CHILD GROWTH OPERATIONS ====================

    public long insertChildGrowth(ChildGrowth growth) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, growth.getPatientId());
        values.put(COL_RECORD_DATE, growth.getRecordDate());
        values.put(COL_WEIGHT, growth.getWeight());
        values.put(COL_HEIGHT, growth.getHeight());
        values.put(COL_HEAD_CIRCUMFERENCE, growth.getHeadCircumference());
        values.put(COL_GROWTH_STATUS, growth.getGrowthStatus());
        values.put(COL_NOTES, growth.getNotes());
        values.put(COL_SYNC_STATUS, growth.getSyncStatus());

        return db.insert(TABLE_CHILD_GROWTH, null, values);
    }

    public List<ChildGrowth> getChildGrowthByPatient(long patientId) {
        List<ChildGrowth> records = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CHILD_GROWTH + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY "
                + COL_RECORD_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(patientId) });

        if (cursor.moveToFirst()) {
            do {
                records.add(cursorToChildGrowth(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    public List<ChildGrowth> getPendingChildGrowth() {
        List<ChildGrowth> records = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_CHILD_GROWTH + " WHERE " + COL_SYNC_STATUS + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { SYNC_PENDING });

        if (cursor.moveToFirst()) {
            do {
                records.add(cursorToChildGrowth(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    public void markChildGrowthSynced(long localId, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_SERVER_ID, serverId);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_CHILD_GROWTH, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localId) });
    }

    private ChildGrowth cursorToChildGrowth(Cursor cursor) {
        ChildGrowth growth = new ChildGrowth();
        growth.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCAL_ID)));
        growth.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
        growth.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_PATIENT_ID)));
        growth.setRecordDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_RECORD_DATE)));
        growth.setWeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_WEIGHT)));
        growth.setHeight(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_HEIGHT)));
        growth.setHeadCircumference(cursor.getFloat(cursor.getColumnIndexOrThrow(COL_HEAD_CIRCUMFERENCE)));
        growth.setGrowthStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_GROWTH_STATUS)));
        growth.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
        growth.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
        return growth;
    }

    // ==================== VACCINATION OPERATIONS ====================

    public long insertVaccination(Vaccination vaccination) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, vaccination.getPatientId());
        values.put(COL_VACCINE_NAME, vaccination.getVaccineName());
        values.put(COL_DUE_DATE, vaccination.getDueDate());
        values.put(COL_GIVEN_DATE, vaccination.getGivenDate());
        values.put(COL_STATUS, vaccination.getStatus());
        values.put(COL_BATCH_NUMBER, vaccination.getBatchNumber());
        values.put(COL_NOTES, vaccination.getNotes());
        values.put(COL_SYNC_STATUS, vaccination.getSyncStatus());

        return db.insert(TABLE_VACCINATIONS, null, values);
    }

    public List<Vaccination> getUpcomingVaccinations(long patientId) {
        List<Vaccination> vaccinations = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_PATIENT_ID + " = ? AND (" + COL_STATUS
                + " = 'Upcoming' OR " + COL_STATUS + " = 'Scheduled' OR " + COL_STATUS + " = 'Pending') ORDER BY "
                + COL_DUE_DATE + " ASC LIMIT 2";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(patientId) });

        if (cursor.moveToFirst()) {
            do {
                vaccinations.add(cursorToVaccination(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vaccinations;
    }

    public List<Vaccination> getVaccinationsByPatient(long patientId) {
        List<Vaccination> vaccinations = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY "
                + COL_DUE_DATE + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(patientId) });

        if (cursor.moveToFirst()) {
            do {
                vaccinations.add(cursorToVaccination(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vaccinations;
    }

    /**
     * Get vaccinations by patient ID (alias for getVaccinationsByPatient)
     */
    public List<Vaccination> getVaccinationsByPatientId(long patientId) {
        return getVaccinationsByPatient(patientId);
    }

    public List<Vaccination> getVaccinationsByStatus(String status) {
        List<Vaccination> vaccinations = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_STATUS + " = ? ORDER BY " + COL_DUE_DATE
                + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { status });

        if (cursor.moveToFirst()) {
            do {
                vaccinations.add(cursorToVaccination(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vaccinations;
    }

    public List<Vaccination> getAllVaccinations() {
        List<Vaccination> vaccinations = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " ORDER BY " + COL_DUE_DATE + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                vaccinations.add(cursorToVaccination(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vaccinations;
    }

    public int updateVaccination(Vaccination vaccination) {
        return updateVaccination(vaccination, true);
    }

    public int updateVaccination(Vaccination vaccination, boolean addToSyncQueue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GIVEN_DATE, vaccination.getGivenDate());
        values.put(COL_STATUS, vaccination.getStatus());
        values.put(COL_BATCH_NUMBER, vaccination.getBatchNumber());
        values.put(COL_NOTES, vaccination.getNotes());
        values.put(COL_SYNC_STATUS, vaccination.getSyncStatus());
        values.put(COL_SERVER_ID, vaccination.getServerId()); // Add Server ID update
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        int rows = db.update(TABLE_VACCINATIONS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(vaccination.getLocalId()) });

        // Add to sync queue if needed (Vaccinations usually do need sync on update)
        if (rows > 0 && addToSyncQueue) {
            addToSyncQueue(TABLE_VACCINATIONS, vaccination.getLocalId(), "UPDATE");
        }
        return rows;
    }

    public List<Vaccination> getPendingVaccinations() {
        List<Vaccination> vaccinations = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_SYNC_STATUS + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { SYNC_PENDING });

        if (cursor.moveToFirst()) {
            do {
                vaccinations.add(cursorToVaccination(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return vaccinations;
    }

    public void markVaccinationSynced(long localId, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_SERVER_ID, serverId);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_VACCINATIONS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localId) });
    }

    private Vaccination cursorToVaccination(Cursor cursor) {
        Vaccination vaccination = new Vaccination();
        vaccination.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCAL_ID)));
        vaccination.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
        vaccination.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_PATIENT_ID)));
        vaccination.setVaccineName(cursor.getString(cursor.getColumnIndexOrThrow(COL_VACCINE_NAME)));
        vaccination.setDueDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_DUE_DATE)));
        vaccination.setGivenDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_GIVEN_DATE)));
        vaccination.setStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATUS)));
        vaccination.setBatchNumber(cursor.getString(cursor.getColumnIndexOrThrow(COL_BATCH_NUMBER)));
        vaccination.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
        vaccination.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
        return vaccination;
    }

    // ==================== VISIT OPERATIONS ====================

    public long insertVisit(Visit visit) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, visit.getPatientId());
        values.put(COL_VISIT_DATE, visit.getVisitDate());
        values.put(COL_VISIT_TYPE, visit.getVisitType());
        values.put(COL_DESCRIPTION, visit.getDescription());
        values.put(COL_NOTES, visit.getNotes());
        values.put(COL_SYNC_STATUS, visit.getSyncStatus());

        return db.insert(TABLE_VISITS, null, values);
    }

    public List<Visit> getVisitsByPatient(long patientId) {
        List<Visit> visits = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VISITS + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY " + COL_VISIT_DATE
                + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(patientId) });

        if (cursor.moveToFirst()) {
            do {
                visits.add(cursorToVisit(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }

    /**
     * Get visits by patient ID (alias for getVisitsByPatient)
     */
    public List<Visit> getVisitsByPatientId(long patientId) {
        return getVisitsByPatient(patientId);
    }

    public List<Visit> getAllVisits() {
        List<Visit> visits = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VISITS + " ORDER BY " + COL_VISIT_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                visits.add(cursorToVisit(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }

    public List<Visit> getPendingVisits() {
        List<Visit> visits = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VISITS + " WHERE " + COL_SYNC_STATUS + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { SYNC_PENDING });

        if (cursor.moveToFirst()) {
            do {
                visits.add(cursorToVisit(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return visits;
    }

    public void markVisitSynced(long localId, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_SERVER_ID, serverId);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_VISITS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localId) });
    }

    private Visit cursorToVisit(Cursor cursor) {
        Visit visit = new Visit();
        visit.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCAL_ID)));
        visit.setServerId(cursor.getInt(cursor.getColumnIndexOrThrow(COL_SERVER_ID)));
        visit.setPatientId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_PATIENT_ID)));
        visit.setVisitDate(cursor.getString(cursor.getColumnIndexOrThrow(COL_VISIT_DATE)));
        visit.setVisitType(cursor.getString(cursor.getColumnIndexOrThrow(COL_VISIT_TYPE)));
        visit.setDescription(cursor.getString(cursor.getColumnIndexOrThrow(COL_DESCRIPTION)));
        visit.setNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_NOTES)));
        visit.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
        return visit;
    }

    // ==================== SYNC QUEUE OPERATIONS ====================

    public long addToSyncQueue(String tableName, long recordId, String action, String dataJson) {
        SQLiteDatabase db = this.getWritableDatabase();

        // Check if ANY record exists for this entity (Pending or Failed)
        // We update any existing record to PENDING to avoid duplicates in the queue
        String selection = COL_TABLE_NAME + "=? AND " + COL_RECORD_ID + "=?";
        String[] selectionArgs = { tableName, String.valueOf(recordId) };

        Cursor cursor = db.query(TABLE_SYNC_QUEUE, new String[] { COL_LOCAL_ID }, selection, selectionArgs, null, null,
                null);

        if (cursor != null && cursor.moveToFirst()) {
            // Record exists (regardless of status), update it to PENDING instead of
            // inserting new one
            long queueLocalId = cursor.getLong(0);
            cursor.close();

            ContentValues updateValues = new ContentValues();
            updateValues.put(COL_ACTION, action);
            updateValues.put(COL_SYNC_STATUS, SYNC_PENDING); // Reset to pending
            if (dataJson != null) {
                updateValues.put(COL_DATA_JSON, dataJson);
            }
            updateValues.put(COL_ERROR_MESSAGE, (String) null); // Clear old error
            updateValues.put(COL_LAST_UPDATED, getCurrentTimestamp());

            return db.update(TABLE_SYNC_QUEUE, updateValues, COL_LOCAL_ID + " = ?",
                    new String[] { String.valueOf(queueLocalId) });
        }

        if (cursor != null) {
            cursor.close();
        }

        ContentValues values = new ContentValues();
        values.put(COL_TABLE_NAME, tableName);
        values.put(COL_RECORD_ID, recordId);
        values.put(COL_ACTION, action);
        values.put(COL_DATA_JSON, dataJson);
        values.put(COL_SYNC_STATUS, SYNC_PENDING);
        values.put(COL_CREATED_AT, getCurrentTimestamp()); // Ensure created_at is set
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        return db.insert(TABLE_SYNC_QUEUE, null, values);
    }

    /**
     * Add to sync queue (without data JSON)
     */
    public long addToSyncQueue(String tableName, long recordId, String action) {
        return addToSyncQueue(tableName, recordId, action, null);
    }

    public List<SyncRecord> getPendingSyncRecords() {
        List<SyncRecord> records = new ArrayList<>();
        // Select both PENDING and FAILED records for retry
        String query = "SELECT * FROM " + TABLE_SYNC_QUEUE + " WHERE " + COL_SYNC_STATUS + " = ? OR " + COL_SYNC_STATUS
                + " = ? ORDER BY "
                + COL_CREATED_AT + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { SYNC_PENDING, SYNC_FAILED });

        if (cursor.moveToFirst()) {
            do {
                SyncRecord record = new SyncRecord();
                record.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCAL_ID)));
                record.setTableName(cursor.getString(cursor.getColumnIndexOrThrow(COL_TABLE_NAME)));
                record.setRecordId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECORD_ID)));
                record.setAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTION)));
                record.setDataJson(cursor.getString(cursor.getColumnIndexOrThrow(COL_DATA_JSON)));
                record.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
                record.setErrorMessage(cursor.getString(cursor.getColumnIndexOrThrow(COL_ERROR_MESSAGE)));
                record.setRetryCount(cursor.getInt(cursor.getColumnIndexOrThrow(COL_RETRY_COUNT)));
                record.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
                records.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    public void markSyncRecordComplete(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());
        db.update(TABLE_SYNC_QUEUE, values, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(localId) });
    }

    public void markSyncRecordFailed(long localId, String errorMessage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_FAILED);
        values.put(COL_ERROR_MESSAGE, errorMessage);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_SYNC_QUEUE, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localId) });
    }

    public int getPendingSyncCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SYNC_QUEUE + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[] { SYNC_PENDING });
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Delete sync record
     */
    public SyncRecord getSyncRecordById(long localId) {
        String query = "SELECT * FROM " + TABLE_SYNC_QUEUE + " WHERE " + COL_LOCAL_ID + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(localId) });
        SyncRecord record = null;
        if (cursor.moveToFirst()) {
            record = new SyncRecord();
            record.setLocalId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_LOCAL_ID)));
            record.setTableName(cursor.getString(cursor.getColumnIndexOrThrow(COL_TABLE_NAME)));
            record.setRecordId(cursor.getLong(cursor.getColumnIndexOrThrow(COL_RECORD_ID)));
            record.setAction(cursor.getString(cursor.getColumnIndexOrThrow(COL_ACTION)));
            record.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
        }
        cursor.close();
        return record;
    }

    public void updateSourceSyncStatus(String tableName, long recordId, String status) {
        if (tableName == null || tableName.isEmpty())
            return;
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, status);
        db.update(tableName, values, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(recordId) });
    }

    /**
     * Get the sync status of a record in its source table
     */
    public String getSourceSyncStatus(String tableName, long recordId) {
        if (tableName == null || tableName.isEmpty())
            return SYNC_PENDING;

        // Special case for tables without sync_status if any
        if (tableName.equalsIgnoreCase(TABLE_ALERTS_REVIEWED))
            return SYNC_SYNCED;

        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT " + COL_SYNC_STATUS + " FROM " + tableName + " WHERE " + COL_LOCAL_ID + " = ?";
        Cursor cursor = null;
        try {
            cursor = db.rawQuery(query, new String[] { String.valueOf(recordId) });
            if (cursor != null && cursor.moveToFirst()) {
                return cursor.getString(0);
            }
        } catch (Exception e) {
            Log.e("DatabaseHelper", "Error getting source sync status: " + e.getMessage());
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return SYNC_PENDING;
    }

    public void deleteSyncRecord(long localId) {
        // First, clear the source record status to hide the "Pending" badge
        SyncRecord record = getSyncRecordById(localId);
        if (record != null) {
            updateSourceSyncStatus(record.getTableName(), record.getRecordId(), SYNC_SYNCED);
        }

        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(localId) });
    }

    public void clearAllPendingRecords() {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Get only records with PENDING/FAILED status to clear their source status
        List<SyncRecord> pending = getPendingSyncRecords();
        for (SyncRecord record : pending) {
            updateSourceSyncStatus(record.getTableName(), record.getRecordId(), SYNC_SYNCED);
        }

        // 2. Clear only non-synced entries from the queue (case-insensitive check for
        // reliability)
        db.delete(TABLE_SYNC_QUEUE, "UPPER(" + COL_SYNC_STATUS + ") != ?", new String[] { SYNC_SYNCED.toUpperCase() });
    }

    /**
     * Delete ALL sync records for a specific entity
     */
    public void deleteSyncRecordsForEntity(String tableName, long recordId) {
        if (tableName == null)
            return;
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_TABLE_NAME + " = ? AND " + COL_RECORD_ID + " = ?",
                new String[] { tableName, String.valueOf(recordId) });
    }

    /**
     * Clean up old SYNCED records from sync queue (older than 5 minutes)
     * This prevents the sync status UI from showing stale "recently synced" items
     */
    public int cleanupOldSyncedRecords() {
        SQLiteDatabase db = this.getWritableDatabase();
        
        // Calculate timestamp for 5 minutes ago
        long fiveMinutesAgo = System.currentTimeMillis() - (5 * 60 * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault());
        String cutoffTime = sdf.format(new java.util.Date(fiveMinutesAgo));
        
        // Delete SYNCED records older than 5 minutes
        int deleted = db.delete(TABLE_SYNC_QUEUE, 
                COL_SYNC_STATUS + " = ? AND " + COL_LAST_UPDATED + " < ?",
                new String[] { "SYNCED", cutoffTime });
        
        if (deleted > 0) {
            android.util.Log.d("DatabaseHelper", "Cleaned up " + deleted + " old SYNCED records from sync queue");
        }
        
        return deleted;
    }

    /**
     * Get all sync records
     */
    public List<SyncRecord> getAllSyncRecords() {
        List<SyncRecord> records = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_SYNC_QUEUE + " ORDER BY " + COL_CREATED_AT + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            int localIdIndex = cursor.getColumnIndex(COL_LOCAL_ID);
            int tableNameIndex = cursor.getColumnIndex(COL_TABLE_NAME);
            int recordIdIndex = cursor.getColumnIndex(COL_RECORD_ID);
            int actionIndex = cursor.getColumnIndex(COL_ACTION);
            int dataJsonIndex = cursor.getColumnIndex(COL_DATA_JSON);
            int syncStatusIndex = cursor.getColumnIndex(COL_SYNC_STATUS);
            int errorMessageIndex = cursor.getColumnIndex(COL_ERROR_MESSAGE);
            int retryCountIndex = cursor.getColumnIndex(COL_RETRY_COUNT);
            int createdAtIndex = cursor.getColumnIndex(COL_CREATED_AT);
            int lastUpdatedIndex = cursor.getColumnIndex(COL_LAST_UPDATED);

            do {
                SyncRecord record = new SyncRecord();
                if (localIdIndex != -1)
                    record.setLocalId(cursor.getLong(localIdIndex));
                if (tableNameIndex != -1)
                    record.setTableName(cursor.getString(tableNameIndex));
                if (recordIdIndex != -1)
                    record.setRecordId(cursor.getLong(recordIdIndex));
                if (actionIndex != -1)
                    record.setAction(cursor.getString(actionIndex));
                if (dataJsonIndex != -1)
                    record.setDataJson(cursor.getString(dataJsonIndex));
                if (syncStatusIndex != -1)
                    record.setSyncStatus(cursor.getString(syncStatusIndex));
                if (errorMessageIndex != -1)
                    record.setErrorMessage(cursor.getString(errorMessageIndex));
                if (retryCountIndex != -1)
                    record.setRetryCount(cursor.getInt(retryCountIndex));
                if (createdAtIndex != -1)
                    record.setCreatedAt(cursor.getString(createdAtIndex));
                if (lastUpdatedIndex != -1)
                    record.setLastUpdated(cursor.getString(lastUpdatedIndex));
                records.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    /**
     * Update sync record
     */
    public int updateSyncRecord(SyncRecord record) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, record.getSyncStatus());
        values.put(COL_ERROR_MESSAGE, record.getErrorMessage());
        values.put(COL_RETRY_COUNT, record.getRetryCount());
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        return db.update(TABLE_SYNC_QUEUE, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(record.getLocalId()) });
    }

    public int getTotalPendingRecords() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Count pending patients
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[] { SYNC_PENDING });
        if (cursor.moveToFirst())
            count += cursor.getInt(0);
        cursor.close();

        // Count pending pregnancy visits
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PREGNANCY_VISITS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[] { SYNC_PENDING });
        if (cursor.moveToFirst())
            count += cursor.getInt(0);
        cursor.close();

        // Count pending child growth
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CHILD_GROWTH + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[] { SYNC_PENDING });
        if (cursor.moveToFirst())
            count += cursor.getInt(0);
        cursor.close();

        // Count pending vaccinations
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VACCINATIONS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[] { SYNC_PENDING });
        if (cursor.moveToFirst())
            count += cursor.getInt(0);
        cursor.close();

        // Count pending visits
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VISITS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[] { SYNC_PENDING });
        if (cursor.moveToFirst())
            count += cursor.getInt(0);
        cursor.close();

        return count;
    }

    // ==================== USER OPERATIONS ====================

    public long insertUser(String name, String email, String phone, String password, String workerId,
            String state, String district, String area) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_EMAIL, email);
        values.put(COL_PHONE, phone);
        values.put(COL_PASSWORD, password);
        values.put(COL_WORKER_ID, workerId);
        values.put(COL_STATE, state);
        values.put(COL_DISTRICT, district);
        values.put(COL_AREA, area);
        values.put(COL_STATUS, "pending"); // Point 1: New workers are pending by default
        values.put(COL_IS_LOGGED_IN, 0);

        return db.insert(TABLE_USERS, null, values);
    }

    public boolean validateUser(String phoneOrWorkerId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Allow login with either phone number OR worker ID, BUT only if status is
        // active or approved
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE (" + COL_PHONE + " = ? OR " + COL_WORKER_ID
                + " = ?) AND " + COL_PASSWORD + " = ? AND (" + COL_STATUS + " = 'active' OR " + COL_STATUS
                + " = 'approved')";
        Cursor cursor = db.rawQuery(query, new String[] { phoneOrWorkerId, phoneOrWorkerId, password });
        boolean valid = cursor.moveToFirst();
        cursor.close();
        return valid;
    }

    public void setUserLoggedIn(String phoneOrWorkerId, boolean loggedIn) {
        SQLiteDatabase db = this.getWritableDatabase();
        // First, log out all users
        ContentValues logoutAll = new ContentValues();
        logoutAll.put(COL_IS_LOGGED_IN, 0);
        db.update(TABLE_USERS, logoutAll, null, null);

        if (loggedIn) {
            ContentValues values = new ContentValues();
            values.put(COL_IS_LOGGED_IN, 1);
            // Update by phone OR worker_id
            db.update(TABLE_USERS, values, COL_PHONE + " = ? OR " + COL_WORKER_ID + " = ?",
                    new String[] { phoneOrWorkerId, phoneOrWorkerId });
        }
    }

    public Cursor getLoggedInUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1", null);
    }

    public String getLoggedInUserName() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db
                .rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1", null);
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public String getLoggedInUserArea() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db
                .rawQuery("SELECT " + COL_AREA + " FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1", null);
        String area = null;
        if (cursor.moveToFirst()) {
            area = cursor.getString(0);
        }
        cursor.close();
        return area;
    }

    public boolean isAnyUserLoggedIn() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1",
                null);
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count > 0;
    }

    // ==================== UTILITY METHODS ====================

    private String getCurrentTimestamp() {
        return new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss", java.util.Locale.getDefault())
                .format(new java.util.Date());
    }

    public void closeDB() {
        SQLiteDatabase db = this.getReadableDatabase();
        if (db != null && db.isOpen()) {
            db.close();
        }
    }

    // ==================== DASHBOARD STATISTICS METHODS ====================

    /**
     * Get count of visits today
     */
    public int getVisitsCountToday() {
        SQLiteDatabase db = this.getReadableDatabase();
        String today = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault())
                .format(new java.util.Date());

        int count = 0;

        // Count general visits
        String queryVisits = "SELECT COUNT(*) FROM " + TABLE_VISITS +
                " WHERE date(" + COL_VISIT_DATE + ") = ?";
        Cursor cursor = db.rawQuery(queryVisits, new String[] { today });
        if (cursor.moveToFirst()) {
            count += cursor.getInt(0);
        }
        cursor.close();

        // Count pregnancy visits
        String queryPregnancy = "SELECT COUNT(*) FROM " + TABLE_PREGNANCY_VISITS +
                " WHERE date(" + COL_VISIT_DATE + ") = ?";
        cursor = db.rawQuery(queryPregnancy, new String[] { today });
        if (cursor.moveToFirst()) {
            count += cursor.getInt(0);
        }
        cursor.close();

        return count;
    }

    // ==================== ADDITIONAL METHODS ====================

    /**
     * Get PregnancyVisit by ID
     */
    public PregnancyVisit getPregnancyVisitById(long localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_PREGNANCY_VISITS + " WHERE " + COL_LOCAL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(localId) });

        PregnancyVisit visit = null;
        if (cursor.moveToFirst()) {
            visit = cursorToPregnancyVisit(cursor);
        }
        cursor.close();
        return visit;
    }

    /**
     * Update PregnancyVisit
     */
    public int updatePregnancyVisit(PregnancyVisit visit) {
        return updatePregnancyVisit(visit, true);
    }

    public int updatePregnancyVisit(PregnancyVisit visit, boolean addToSyncQueue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("visit_date", visit.getVisitDate());
        values.put("gestational_weeks", visit.getGestationalWeeks());
        values.put("weight", visit.getWeight());
        values.put("blood_pressure", visit.getBloodPressure());
        values.put("hemoglobin", visit.getHemoglobin());
        values.put("fetal_heart_rate", visit.getFetalHeartRate());
        values.put("urine_protein", visit.getUrineProtein());
        values.put("urine_sugar", visit.getUrineSugar());
        values.put("is_high_risk", visit.isHighRisk() ? 1 : 0);
        values.put("high_risk_reason", visit.getHighRiskReason());
        values.put("next_visit_date", visit.getNextVisitDate());
        values.put("notes", visit.getNotes());
        values.put(COL_SYNC_STATUS, visit.getSyncStatus());
        values.put(COL_SERVER_ID, visit.getServerId());
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        int result = db.update(TABLE_PREGNANCY_VISITS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(visit.getLocalId()) });

        if (result > 0 && addToSyncQueue) {
            addToSyncQueue(TABLE_PREGNANCY_VISITS, visit.getLocalId(), Constants.ACTION_UPDATE);
        }
        return result;
    }

    /**
     * Get Vaccination by ID
     */
    public Vaccination getVaccinationById(long localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_LOCAL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(localId) });

        Vaccination vaccination = null;
        if (cursor.moveToFirst()) {
            vaccination = cursorToVaccination(cursor);
        }
        cursor.close();
        return vaccination;
    }

    /**
     * Get Visit by ID
     */
    public Visit getVisitById(long localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_VISITS + " WHERE " + COL_LOCAL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(localId) });

        Visit visit = null;
        if (cursor.moveToFirst()) {
            visit = cursorToVisit(cursor);
        }
        cursor.close();
        return visit;
    }

    /**
     * Update Visit
     */
    public int updateVisit(Visit visit) {
        return updateVisit(visit, true);
    }

    public int updateVisit(Visit visit, boolean addToSyncQueue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("visit_type", visit.getVisitType());
        values.put("visit_date", visit.getVisitDate());
        values.put("purpose", visit.getPurpose());
        values.put("findings", visit.getFindings());
        values.put("recommendations", visit.getRecommendations());
        values.put("next_visit_date", visit.getNextVisitDate());
        values.put("notes", visit.getNotes());
        values.put(COL_SYNC_STATUS, visit.getSyncStatus());
        values.put(COL_SERVER_ID, visit.getServerId());
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        int result = db.update(TABLE_VISITS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(visit.getLocalId()) });

        if (result > 0 && addToSyncQueue) {
            addToSyncQueue(TABLE_VISITS, visit.getLocalId(), Constants.ACTION_UPDATE);
        }
        return result;
    }

    /**
     * Get ChildGrowth by ID
     */
    public ChildGrowth getChildGrowthById(long localId) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT * FROM " + TABLE_CHILD_GROWTH + " WHERE " + COL_LOCAL_ID + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(localId) });

        ChildGrowth growth = null;
        if (cursor.moveToFirst()) {
            growth = cursorToChildGrowth(cursor);
        }
        cursor.close();
        return growth;
    }

    /**
     * Update ChildGrowth
     */
    public int updateChildGrowth(ChildGrowth growth) {
        return updateChildGrowth(growth, true);
    }

    public int updateChildGrowth(ChildGrowth growth, boolean addToSyncQueue) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("record_date", growth.getRecordDate());
        values.put("age_months", growth.getAgeMonths());
        values.put("weight", growth.getWeight());
        values.put("height", growth.getHeight());
        values.put("head_circumference", growth.getHeadCircumference());
        values.put("muac", growth.getMuac());
        values.put("nutritional_status", growth.getNutritionalStatus());
        values.put("milestones", growth.getMilestones());
        values.put("notes", growth.getNotes());
        values.put(COL_SYNC_STATUS, growth.getSyncStatus());
        values.put(COL_SERVER_ID, growth.getServerId());
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        int result = db.update(TABLE_CHILD_GROWTH, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(growth.getLocalId()) });

        if (result > 0 && addToSyncQueue) {
            addToSyncQueue(TABLE_CHILD_GROWTH, growth.getLocalId(), Constants.ACTION_UPDATE);
        }
        return result;
    }

    // ==================== SIMPLIFIED METHODS FOR ADD PATIENT SCREEN
    // ====================

    /**
     * Simplified method to add patient with basic details
     */
    // Overloaded method - defaults to adding to sync queue for backward
    // compatibility
    public long addPatient(String name, int age, String gender, String category,
            String address, String phone, String abhaId, String bloodGroup,
            String ashaPhone, String currentDate, boolean isHighRisk, String highRiskReason) {
        return addPatient(name, age, gender, category, address, phone, abhaId, bloodGroup,
                ashaPhone, currentDate, isHighRisk, highRiskReason, true, null);
    }

    // New method with shouldAddToSyncQueue parameter for online-first architecture
    public long addPatient(String name, int age, String gender, String category,
            String address, String phone, String abhaId, String bloodGroup,
            String ashaPhone, String currentDate, boolean isHighRisk, String highRiskReason,
            boolean shouldAddToSyncQueue, String dataJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_AGE, age);
        values.put(COL_GENDER, gender);
        values.put(COL_CATEGORY, category);
        values.put(COL_ADDRESS, address);
        values.put(COL_BLOOD_GROUP, bloodGroup);
        values.put(COL_PHONE, phone);
        values.put(COL_ABHA_ID, abhaId);
        values.put(COL_IS_HIGH_RISK, isHighRisk ? 1 : 0);
        values.put(COL_HIGH_RISK_REASON, highRiskReason);
        values.put(COL_SYNC_STATUS, shouldAddToSyncQueue ? SYNC_PENDING : SYNC_SYNCED);
        values.put(COL_CREATED_AT, currentDate);
        values.put(COL_LAST_UPDATED, currentDate);

        long patientId = db.insert(TABLE_PATIENTS, null, values);

        if (patientId > 0 && shouldAddToSyncQueue) {
            if (dataJson == null) {
                try {
                    org.json.JSONObject json = new org.json.JSONObject();
                    json.put("name", name);
                    dataJson = json.toString();
                } catch (Exception e) {
                }
            }
            addToSyncQueue(TABLE_PATIENTS, patientId, "INSERT", dataJson);
        }

        return patientId;
    }

    /**
     * Update patient with server ID after successful backend sync
     */
    public void updatePatientServerId(long localPatientId, int serverId) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SERVER_ID, serverId);
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_PATIENTS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localPatientId) });
    }

    /**
     * Update patient data from backend sync
     */
    public void updatePatientFromBackend(long localPatientId, String name, int age, String gender,
            String category, String village, String phone,
            int isHighRisk, String highRiskReason) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_AGE, age);
        values.put(COL_GENDER, gender);
        values.put(COL_CATEGORY, category);
        values.put(COL_AREA, village);
        values.put(COL_PHONE, phone);
        values.put(COL_ABHA_ID, ""); // Placeholder, adjust if needed
        values.put(COL_IS_HIGH_RISK, isHighRisk);
        values.put(COL_HIGH_RISK_REASON, highRiskReason);
        values.put(COL_SYNC_STATUS, SYNC_SYNCED);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_PATIENTS, values, COL_LOCAL_ID + " = ?",
                new String[] { String.valueOf(localPatientId) });
    }

    /**
     * Add pregnancy visit data
     */
    // Overloaded method - defaults to adding to sync queue
    public long addPregnancyVisit(long patientId, String lmpDate, String edd,
            String bloodPressure, String weight, String hemoglobin,
            String dangerSigns, String medicines, String nextVisitDate) {
        return addPregnancyVisit(patientId, lmpDate, edd, bloodPressure, weight,
                hemoglobin, dangerSigns, medicines, nextVisitDate, true, null);
    }

    // New method with shouldAddToSyncQueue parameter for online-first architecture
    public long addPregnancyVisit(long patientId, String lmpDate, String edd,
            String bloodPressure, String weight, String hemoglobin,
            String dangerSigns, String medicines, String nextVisitDate,
            boolean shouldAddToSyncQueue, String dataJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, patientId);
        values.put(COL_VISIT_DATE, getCurrentTimestamp());
        values.put(COL_EXPECTED_DELIVERY, edd);
        values.put(COL_WEIGHT, weight);

        // Store additional data in notes
        StringBuilder notes = new StringBuilder();
        notes.append("LMP Date: ").append(lmpDate).append("\n");
        notes.append("Blood Pressure: ").append(bloodPressure).append("\n");
        notes.append("Hemoglobin: ").append(hemoglobin).append("\n");
        if (!dangerSigns.isEmpty()) {
            notes.append("Danger Signs: ").append(dangerSigns).append("\n");
        }
        if (!medicines.isEmpty()) {
            notes.append("Medicines: ").append(medicines).append("\n");
        }
        notes.append("Next Visit Date: ").append(nextVisitDate);

        values.put(COL_NOTES, notes.toString());
        values.put(COL_SYNC_STATUS, shouldAddToSyncQueue ? SYNC_PENDING : SYNC_SYNCED);

        long visitId = db.insert(TABLE_PREGNANCY_VISITS, null, values);

        if (visitId > 0 && shouldAddToSyncQueue) {
            addToSyncQueue(TABLE_PREGNANCY_VISITS, visitId, "INSERT", dataJson);

            // ALSO: Create vaccination record if Tetanus Injection was given
            if (medicines != null && medicines.contains("Tetanus Injection")) {
                com.simats.ashasmartcare.models.Vaccination v = new com.simats.ashasmartcare.models.Vaccination();
                v.setPatientId(patientId);
                v.setVaccineName("Tetanus Injection");
                v.setStatus("Given");
                v.setGivenDate(getCurrentTimestamp());
                v.setSyncStatus(SYNC_PENDING);
                insertVaccination(v);
            }
        }

        return visitId;
    }

    /**
     * Add child growth data
     */
    // Overloaded method - defaults to adding to sync queue
    public long addChildGrowth(long patientId, String weight, String height, String muac,
            String temperature, String breastfeeding, String complementaryFeeding,
            String appetite, String symptoms, String lastVaccine, String nextVaccineDate) {
        return addChildGrowth(patientId, weight, height, muac, temperature, breastfeeding,
                complementaryFeeding, appetite, symptoms, lastVaccine, nextVaccineDate, true, null);
    }

    // New method with shouldAddToSyncQueue parameter for online-first architecture
    public long addChildGrowth(long patientId, String weight, String height, String muac,
            String temperature, String breastfeeding, String complementaryFeeding,
            String appetite, String symptoms, String lastVaccine, String nextVaccineDate,
            boolean shouldAddToSyncQueue, String dataJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, patientId);
        values.put(COL_RECORD_DATE, getCurrentTimestamp());
        values.put(COL_WEIGHT, weight);
        values.put(COL_HEIGHT, height);
        // Store MUAC in head_circumference column temporarily
        values.put(COL_HEAD_CIRCUMFERENCE, muac);

        // Store additional data in growth_status or notes column
        StringBuilder notes = new StringBuilder();
        notes.append("Temperature: ").append(temperature).append("\n");
        notes.append("Breastfeeding: ").append(breastfeeding).append("\n");
        notes.append("Complementary Feeding: ").append(complementaryFeeding).append("\n");
        notes.append("Appetite: ").append(appetite).append("\n");
        if (!symptoms.isEmpty()) {
            notes.append("Symptoms: ").append(symptoms).append("\n");
        }
        notes.append("Last Vaccine: ").append(lastVaccine).append("\n");
        notes.append("Next Vaccine Date: ").append(nextVaccineDate);

        values.put(COL_GROWTH_STATUS, notes.toString());
        values.put(COL_SYNC_STATUS, shouldAddToSyncQueue ? SYNC_PENDING : SYNC_SYNCED);

        long growthId = db.insert(TABLE_CHILD_GROWTH, null, values);

        if (growthId > 0 && shouldAddToSyncQueue) {
            addToSyncQueue(TABLE_CHILD_GROWTH, growthId, "INSERT", dataJson);

            // ALSO: Create vaccination records if provided
            if (lastVaccine != null && !lastVaccine.isEmpty() && !lastVaccine.equals("Select Vaccine")) {
                if (!isVaccinationExists(patientId, lastVaccine)) {
                    com.simats.ashasmartcare.models.Vaccination v = new com.simats.ashasmartcare.models.Vaccination();
                    v.setPatientId(patientId);
                    v.setVaccineName(lastVaccine);
                    v.setStatus("Given");
                    v.setGivenDate(getCurrentTimestamp());
                    v.setSyncStatus(SYNC_PENDING);
                    insertVaccination(v);
                }
            }

            if (nextVaccineDate != null && !nextVaccineDate.isEmpty()) {
                if (!isVaccinationExists(patientId, "Scheduled Dose")) {
                    com.simats.ashasmartcare.models.Vaccination v = new com.simats.ashasmartcare.models.Vaccination();
                    v.setPatientId(patientId);
                    v.setVaccineName("Scheduled Dose"); // Generic name if specific not selected
                    v.setStatus("Scheduled");
                    v.setScheduledDate(convertDateToDB(nextVaccineDate));
                    v.setSyncStatus(SYNC_PENDING);
                    insertVaccination(v);
                }
            }
        }

        return growthId;
    }

    public boolean isVaccinationExists(long patientId, String vaccineName) {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.query(TABLE_VACCINATIONS, new String[] { COL_LOCAL_ID },
                COL_PATIENT_ID + " = ? AND " + COL_VACCINE_NAME + " = ?",
                new String[] { String.valueOf(patientId), vaccineName },
                null, null, null);
        boolean exists = cursor.getCount() > 0;
        cursor.close();
        return exists;
    }

    /**
     * Migration logic to populate vaccinations table from existing notes in other
     * tables.
     * This handles cases where vaccinations were "mentioned" in notes but no record
     * was created.
     */
    public void syncMissingVaccinations() {
        SQLiteDatabase db = this.getWritableDatabase();

        // 1. Sync from pregnancy_visits
        String pregQuery = "SELECT " + COL_PATIENT_ID + ", " + COL_NOTES + " FROM " + TABLE_PREGNANCY_VISITS;
        Cursor pregCursor = db.rawQuery(pregQuery, null);
        if (pregCursor.moveToFirst()) {
            do {
                long patientId = pregCursor.getLong(0);
                String notes = pregCursor.getString(1);
                if (notes != null && notes.contains("Medicines: ") && notes.contains("Tetanus Injection")) {
                    if (!isVaccinationExists(patientId, "Tetanus Injection")) {
                        com.simats.ashasmartcare.models.Vaccination v = new com.simats.ashasmartcare.models.Vaccination();
                        v.setPatientId(patientId);
                        v.setVaccineName("Tetanus Injection");
                        v.setStatus("Given");
                        v.setGivenDate(getCurrentTimestamp());
                        v.setSyncStatus(SYNC_PENDING);
                        insertVaccination(v);
                    }
                }
            } while (pregCursor.moveToNext());
        }
        pregCursor.close();

        // 2. Sync from child_growth
        String childQuery = "SELECT " + COL_PATIENT_ID + ", " + COL_GROWTH_STATUS + " FROM " + TABLE_CHILD_GROWTH;
        Cursor childCursor = db.rawQuery(childQuery, null);
        if (childCursor.moveToFirst()) {
            do {
                long patientId = childCursor.getLong(0);
                String notes = childCursor.getString(1);
                if (notes != null) {
                    // Extract last vaccine
                    if (notes.contains("Last Vaccine: ")) {
                        String lastVaccineLine = extractNoteLine(notes, "Last Vaccine: ");
                        if (lastVaccineLine != null && !lastVaccineLine.isEmpty()
                                && !lastVaccineLine.equals("Select Vaccine")) {
                            if (!isVaccinationExists(patientId, lastVaccineLine)) {
                                com.simats.ashasmartcare.models.Vaccination v = new com.simats.ashasmartcare.models.Vaccination();
                                v.setPatientId(patientId);
                                v.setVaccineName(lastVaccineLine);
                                v.setStatus("Given");
                                v.setGivenDate(getCurrentTimestamp());
                                v.setSyncStatus(SYNC_PENDING);
                                insertVaccination(v);
                            }
                        }
                    }
                    // Extract next vaccine date
                    if (notes.contains("Next Vaccine Date: ")) {
                        String nextDateStr = extractNoteLine(notes, "Next Vaccine Date: ");
                        if (nextDateStr != null && !nextDateStr.isEmpty()) {
                            if (!isVaccinationExists(patientId, "Scheduled Dose")) {
                                com.simats.ashasmartcare.models.Vaccination v = new com.simats.ashasmartcare.models.Vaccination();
                                v.setPatientId(patientId);
                                v.setVaccineName("Scheduled Dose");
                                v.setStatus("Scheduled");
                                v.setScheduledDate(convertDateToDB(nextDateStr));
                                v.setSyncStatus(SYNC_PENDING);
                                insertVaccination(v);
                            }
                        }
                    }
                }
            } while (childCursor.moveToNext());
        }
        childCursor.close();
    }

    private String extractNoteLine(String notes, String prefix) {
        String[] lines = notes.split("\n");
        for (String line : lines) {
            if (line.startsWith(prefix)) {
                return line.substring(prefix.length()).trim();
            }
        }
        return null;
    }

    private String convertDateToDB(String uiDate) {
        if (uiDate == null || uiDate.isEmpty())
            return null;
        // uiDate is MM/dd/yyyy
        try {
            java.text.SimpleDateFormat uiFormat = new java.text.SimpleDateFormat("MM/dd/yyyy", java.util.Locale.US);
            java.text.SimpleDateFormat dbFormat = new java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.US);
            return dbFormat.format(uiFormat.parse(uiDate));
        } catch (Exception e) {
            return uiDate; // Fallback
        }
    }

    /**
     * Add general visit data
     */
    // Default version - adds to sync queue (backward compatibility)
    public long addGeneralVisit(long patientId, String bloodPressure, String weight,
            String sugar, String symptoms, String tobacco, String alcohol,
            String physicalActivity, String referral, String followUpDate) {
        return addGeneralVisit(patientId, bloodPressure, weight, sugar, symptoms,
                tobacco, alcohol, physicalActivity, referral, followUpDate, true, null);
    }

    // Overloaded version with shouldAddToSyncQueue parameter (online-first support)
    public long addGeneralVisit(long patientId, String bloodPressure, String weight,
            String sugar, String symptoms, String tobacco, String alcohol,
            String physicalActivity, String referral, String followUpDate,
            boolean shouldAddToSyncQueue, String dataJson) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, patientId);
        values.put(COL_VISIT_DATE, getCurrentTimestamp());
        values.put(COL_VISIT_TYPE, "General Health Visit");

        // Store all data in description
        StringBuilder description = new StringBuilder();
        description.append("Blood Pressure: ").append(bloodPressure).append("\n");
        description.append("Weight: ").append(weight).append(" kg\n");
        if (!sugar.isEmpty()) {
            description.append("Blood Sugar: ").append(sugar).append(" mg/dL\n");
        }
        if (!symptoms.isEmpty()) {
            description.append("Symptoms: ").append(symptoms).append("\n");
        }
        description.append("Tobacco Use: ").append(tobacco).append("\n");
        description.append("Alcohol Use: ").append(alcohol).append("\n");
        description.append("Physical Activity: ").append(physicalActivity).append("\n");
        description.append("Referral Required: ").append(referral).append("\n");
        description.append("Follow-up Date: ").append(followUpDate);

        values.put(COL_DESCRIPTION, description.toString());
        // Set sync status: SYNCED if online (don't add to queue), PENDING if offline
        values.put(COL_SYNC_STATUS, shouldAddToSyncQueue ? SYNC_PENDING : SYNC_SYNCED);

        long visitId = db.insert(TABLE_VISITS, null, values);

        // Only add to sync queue if offline
        if (visitId > 0 && shouldAddToSyncQueue) {
            addToSyncQueue(TABLE_VISITS, visitId, "INSERT", dataJson);
        }

        return visitId;
    }

    // ==================== WORKER OPERATIONS ====================

    /**
     * Insert a new ASHA worker
     */
    public long insertWorker(com.simats.ashasmartcare.models.Worker worker) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, worker.getName());
        values.put(COL_PHONE, worker.getPhone());
        values.put(COL_PASSWORD, worker.getPassword());
        values.put(COL_AGE, worker.getAge());
        values.put(COL_GENDER, worker.getGender());
        values.put(COL_VILLAGE, worker.getVillage());
        values.put(COL_PHC, worker.getPhc());
        values.put(COL_WORKER_ID, worker.getWorkerId());
        values.put(COL_STATUS, worker.getStatus());
        values.put(COL_SYNC_STATUS, SYNC_PENDING);

        long id = db.insert(TABLE_USERS, null, values);
        if (id > 0) {
            addToSyncQueue(TABLE_USERS, id, "INSERT");
        }
        return id;
    }

    /**
     * Update worker status
     */
    public boolean updateWorkerStatus(long id, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        values.put(COL_SYNC_STATUS, SYNC_PENDING); // Mark for sync

        int rows = db.update(TABLE_USERS, values, COL_LOCAL_ID + " = ?", new String[] { String.valueOf(id) });
        if (rows > 0) {
            addToSyncQueue(TABLE_USERS, id, "UPDATE");
            return true;
        }
        return false;
    }

    /**
     * Update worker status by server ID
     */
    public boolean updateWorkerStatusByServerId(int serverId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        int rows = db.update(TABLE_USERS, values, COL_SERVER_ID + " = ?", new String[] { String.valueOf(serverId) });
        return rows > 0;
    }

    /**
     * Update worker status by phone or worker ID
     */
    public boolean updateUserStatus(String phoneOrId, String status) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_STATUS, status);
        int rows = db.update(TABLE_USERS, values, COL_PHONE + " = ? OR " + COL_WORKER_ID + " = ?",
                new String[] { phoneOrId, phoneOrId });
        return rows > 0;
    }

    /**
     * Get all ASHA workers
     */
    public List<com.simats.ashasmartcare.models.Worker> getAllWorkers() {
        List<com.simats.ashasmartcare.models.Worker> workers = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE " + COL_WORKER_ID + " IS NOT NULL ORDER BY " + COL_NAME
                + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, null);

        if (cursor.moveToFirst()) {
            do {
                workers.add(cursorToWorker(cursor));
            } while (cursor.moveToNext());
        }
        cursor.close();
        return workers;
    }

    private com.simats.ashasmartcare.models.Worker cursorToWorker(Cursor cursor) {
        com.simats.ashasmartcare.models.Worker worker = new com.simats.ashasmartcare.models.Worker();

        int idIdx = cursor.getColumnIndex(COL_LOCAL_ID);
        int nameIdx = cursor.getColumnIndex(COL_NAME);
        int workerIdIdx = cursor.getColumnIndex(COL_WORKER_ID);
        int phoneIdx = cursor.getColumnIndex(COL_PHONE);
        int ageIdx = cursor.getColumnIndex(COL_AGE);
        int genderIdx = cursor.getColumnIndex(COL_GENDER);
        int villageIdx = cursor.getColumnIndex(COL_VILLAGE);
        int phcIdx = cursor.getColumnIndex(COL_PHC);
        int statusIdx = cursor.getColumnIndex(COL_STATUS);
        int createdAtIdx = cursor.getColumnIndex(COL_CREATED_AT);

        if (idIdx != -1)
            worker.setId(cursor.getLong(idIdx));
        if (nameIdx != -1)
            worker.setName(cursor.getString(nameIdx));
        if (workerIdIdx != -1)
            worker.setWorkerId(cursor.getString(workerIdIdx));
        if (phoneIdx != -1)
            worker.setPhone(cursor.getString(phoneIdx));
        if (ageIdx != -1)
            worker.setAge(cursor.getInt(ageIdx));
        if (genderIdx != -1)
            worker.setGender(cursor.getString(genderIdx));
        if (villageIdx != -1)
            worker.setVillage(cursor.getString(villageIdx));
        if (phcIdx != -1)
            worker.setPhc(cursor.getString(phcIdx));
        if (statusIdx != -1)
            worker.setStatus(cursor.getString(statusIdx));
        if (createdAtIdx != -1)
            worker.setCreatedAt(cursor.getString(createdAtIdx));

        return worker;
    }

    /**
     * Mark an AI alert as reviewed to persist its state
     */
    public void markAlertReviewed(long patientId, String alertType, boolean isReviewed) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_PATIENT_ID, patientId);
        values.put(COL_VISIT_TYPE, alertType);
        values.put("is_reviewed", isReviewed ? 1 : 0);

        db.insertWithOnConflict(TABLE_ALERTS_REVIEWED, null, values, SQLiteDatabase.CONFLICT_REPLACE);
    }

    /**
     * Check if an AI alert has been reviewed
     */
    public boolean isAlertReviewed(long patientId, String alertType) {
        SQLiteDatabase db = this.getReadableDatabase();
        String query = "SELECT is_reviewed FROM " + TABLE_ALERTS_REVIEWED + " WHERE " + COL_PATIENT_ID + " = ? AND "
                + COL_VISIT_TYPE + " = ?";
        Cursor cursor = db.rawQuery(query, new String[] { String.valueOf(patientId), alertType });

        boolean isReviewed = false;
        if (cursor.moveToFirst()) {
            isReviewed = cursor.getInt(0) == 1;
        }
        cursor.close();
        return isReviewed;
    }
}
