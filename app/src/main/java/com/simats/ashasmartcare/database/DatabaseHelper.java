package com.simats.ashasmartcare.database;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import com.simats.ashasmartcare.models.ChildGrowth;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.PregnancyVisit;
import com.simats.ashasmartcare.models.SyncRecord;
import com.simats.ashasmartcare.models.Vaccination;
import com.simats.ashasmartcare.models.Visit;
import com.simats.ashasmartcare.utils.Constants;

import java.util.ArrayList;
import java.util.List;

/**
 * SQLite Database Helper for ASHA Healthcare App
 * Handles all local database operations with sync status tracking
 */
public class DatabaseHelper extends SQLiteOpenHelper {

    // Database Info
    private static final String DATABASE_NAME = "asha_healthcare.db";
    private static final int DATABASE_VERSION = 1;

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
    public static final String COL_STATE = "state";
    public static final String COL_DISTRICT = "district";
    public static final String COL_AREA = "area";
    public static final String COL_CATEGORY = "category";
    public static final String COL_MEDICAL_NOTES = "medical_notes";
    public static final String COL_PHOTO_PATH = "photo_path";

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
            + COL_STATE + " TEXT,"
            + COL_DISTRICT + " TEXT,"
            + COL_AREA + " TEXT,"
            + COL_CATEGORY + " TEXT,"
            + COL_MEDICAL_NOTES + " TEXT,"
            + COL_PHOTO_PATH + " TEXT,"
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
            + COL_STATE + " TEXT,"
            + COL_DISTRICT + " TEXT,"
            + COL_AREA + " TEXT,"
            + COL_IS_LOGGED_IN + " INTEGER DEFAULT 0,"
            + COL_SYNC_STATUS + " TEXT DEFAULT '" + SYNC_PENDING + "',"
            + COL_CREATED_AT + " DATETIME DEFAULT CURRENT_TIMESTAMP,"
            + COL_LAST_UPDATED + " DATETIME DEFAULT CURRENT_TIMESTAMP"
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
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        // Drop older tables if existed
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SYNC_QUEUE);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VISITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_VACCINATIONS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_CHILD_GROWTH);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PREGNANCY_VISITS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_PATIENTS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_USERS);
        onCreate(db);
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
        values.put(COL_STATE, patient.getState());
        values.put(COL_DISTRICT, patient.getDistrict());
        values.put(COL_AREA, patient.getArea());
        values.put(COL_CATEGORY, patient.getCategory());
        values.put(COL_MEDICAL_NOTES, patient.getMedicalNotes());
        values.put(COL_PHOTO_PATH, patient.getPhotoPath());
        values.put(COL_SYNC_STATUS, patient.getSyncStatus());
        values.put(COL_SERVER_ID, patient.getServerId());

        long id = db.insert(TABLE_PATIENTS, null, values);
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
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_CATEGORY + " = ? ORDER BY " + COL_NAME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{category});

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
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(localId)});

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
    public int updatePatient(Patient patient) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, patient.getName());
        values.put(COL_AGE, patient.getAge());
        values.put(COL_DOB, patient.getDob());
        values.put(COL_GENDER, patient.getGender());
        values.put(COL_PHONE, patient.getPhone());
        values.put(COL_STATE, patient.getState());
        values.put(COL_DISTRICT, patient.getDistrict());
        values.put(COL_AREA, patient.getArea());
        values.put(COL_CATEGORY, patient.getCategory());
        values.put(COL_MEDICAL_NOTES, patient.getMedicalNotes());
        values.put(COL_PHOTO_PATH, patient.getPhotoPath());
        values.put(COL_SYNC_STATUS, patient.getSyncStatus());
        values.put(COL_SERVER_ID, patient.getServerId());
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        return db.update(TABLE_PATIENTS, values, COL_LOCAL_ID + " = ?",
                new String[]{String.valueOf(patient.getLocalId())});
    }

    /**
     * Delete patient
     */
    public int deletePatient(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        return db.delete(TABLE_PATIENTS, COL_LOCAL_ID + " = ?", new String[]{String.valueOf(localId)});
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
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(serverId)});
        
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
        Cursor cursor = db.rawQuery(query, new String[]{SYNC_PENDING});

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
                new String[]{String.valueOf(localId)});
    }

    /**
     * Get patient count
     */
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
     * Search patients by name
     */
    public List<Patient> searchPatients(String searchTerm) {
        List<Patient> patients = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_PATIENTS + " WHERE " + COL_NAME + " LIKE ? ORDER BY " + COL_NAME + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{"%" + searchTerm + "%"});

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
        patient.setState(cursor.getString(cursor.getColumnIndexOrThrow(COL_STATE)));
        patient.setDistrict(cursor.getString(cursor.getColumnIndexOrThrow(COL_DISTRICT)));
        patient.setArea(cursor.getString(cursor.getColumnIndexOrThrow(COL_AREA)));
        patient.setCategory(cursor.getString(cursor.getColumnIndexOrThrow(COL_CATEGORY)));
        patient.setMedicalNotes(cursor.getString(cursor.getColumnIndexOrThrow(COL_MEDICAL_NOTES)));
        patient.setPhotoPath(cursor.getString(cursor.getColumnIndexOrThrow(COL_PHOTO_PATH)));
        patient.setSyncStatus(cursor.getString(cursor.getColumnIndexOrThrow(COL_SYNC_STATUS)));
        patient.setCreatedAt(cursor.getString(cursor.getColumnIndexOrThrow(COL_CREATED_AT)));
        patient.setLastUpdated(cursor.getString(cursor.getColumnIndexOrThrow(COL_LAST_UPDATED)));
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
        String query = "SELECT * FROM " + TABLE_PREGNANCY_VISITS + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY " + COL_VISIT_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

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
        Cursor cursor = db.rawQuery(query, new String[]{SYNC_PENDING});

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
                new String[]{String.valueOf(localId)});
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
        String query = "SELECT * FROM " + TABLE_CHILD_GROWTH + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY " + COL_RECORD_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

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
        Cursor cursor = db.rawQuery(query, new String[]{SYNC_PENDING});

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
                new String[]{String.valueOf(localId)});
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

    public List<Vaccination> getVaccinationsByPatient(long patientId) {
        List<Vaccination> vaccinations = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY " + COL_DUE_DATE + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

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
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_STATUS + " = ? ORDER BY " + COL_DUE_DATE + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{status});

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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_GIVEN_DATE, vaccination.getGivenDate());
        values.put(COL_STATUS, vaccination.getStatus());
        values.put(COL_BATCH_NUMBER, vaccination.getBatchNumber());
        values.put(COL_NOTES, vaccination.getNotes());
        values.put(COL_SYNC_STATUS, SYNC_PENDING);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        return db.update(TABLE_VACCINATIONS, values, COL_LOCAL_ID + " = ?",
                new String[]{String.valueOf(vaccination.getLocalId())});
    }

    public List<Vaccination> getPendingVaccinations() {
        List<Vaccination> vaccinations = new ArrayList<>();
        String query = "SELECT * FROM " + TABLE_VACCINATIONS + " WHERE " + COL_SYNC_STATUS + " = ?";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{SYNC_PENDING});

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
                new String[]{String.valueOf(localId)});
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
        String query = "SELECT * FROM " + TABLE_VISITS + " WHERE " + COL_PATIENT_ID + " = ? ORDER BY " + COL_VISIT_DATE + " DESC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(patientId)});

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
        Cursor cursor = db.rawQuery(query, new String[]{SYNC_PENDING});

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
                new String[]{String.valueOf(localId)});
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
        ContentValues values = new ContentValues();
        values.put(COL_TABLE_NAME, tableName);
        values.put(COL_RECORD_ID, recordId);
        values.put(COL_ACTION, action);
        values.put(COL_DATA_JSON, dataJson);
        values.put(COL_SYNC_STATUS, SYNC_PENDING);

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
        String query = "SELECT * FROM " + TABLE_SYNC_QUEUE + " WHERE " + COL_SYNC_STATUS + " = ? ORDER BY " + COL_CREATED_AT + " ASC";
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery(query, new String[]{SYNC_PENDING});

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
                records.add(record);
            } while (cursor.moveToNext());
        }
        cursor.close();
        return records;
    }

    public void markSyncRecordComplete(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_LOCAL_ID + " = ?", new String[]{String.valueOf(localId)});
    }

    public void markSyncRecordFailed(long localId, String errorMessage) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_SYNC_STATUS, SYNC_FAILED);
        values.put(COL_ERROR_MESSAGE, errorMessage);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());

        db.update(TABLE_SYNC_QUEUE, values, COL_LOCAL_ID + " = ?",
                new String[]{String.valueOf(localId)});
    }

    public int getPendingSyncCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_SYNC_QUEUE + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[]{SYNC_PENDING});
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
    public void deleteSyncRecord(long localId) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SYNC_QUEUE, COL_LOCAL_ID + " = ?", new String[]{String.valueOf(localId)});
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
                new String[]{String.valueOf(record.getLocalId())});
    }

    public int getTotalPendingRecords() {
        int count = 0;
        SQLiteDatabase db = this.getReadableDatabase();

        // Count pending patients
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PATIENTS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[]{SYNC_PENDING});
        if (cursor.moveToFirst()) count += cursor.getInt(0);
        cursor.close();

        // Count pending pregnancy visits
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_PREGNANCY_VISITS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[]{SYNC_PENDING});
        if (cursor.moveToFirst()) count += cursor.getInt(0);
        cursor.close();

        // Count pending child growth
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_CHILD_GROWTH + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[]{SYNC_PENDING});
        if (cursor.moveToFirst()) count += cursor.getInt(0);
        cursor.close();

        // Count pending vaccinations
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VACCINATIONS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[]{SYNC_PENDING});
        if (cursor.moveToFirst()) count += cursor.getInt(0);
        cursor.close();

        // Count pending visits
        cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_VISITS + " WHERE " + COL_SYNC_STATUS + " = ?",
                new String[]{SYNC_PENDING});
        if (cursor.moveToFirst()) count += cursor.getInt(0);
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
        values.put(COL_IS_LOGGED_IN, 0);

        return db.insert(TABLE_USERS, null, values);
    }

    public boolean validateUser(String phoneOrWorkerId, String password) {
        SQLiteDatabase db = this.getReadableDatabase();
        // Allow login with either phone number OR worker ID
        String query = "SELECT * FROM " + TABLE_USERS + " WHERE (" + COL_PHONE + " = ? OR " + COL_WORKER_ID + " = ?) AND " + COL_PASSWORD + " = ?";
        Cursor cursor = db.rawQuery(query, new String[]{phoneOrWorkerId, phoneOrWorkerId, password});
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
            db.update(TABLE_USERS, values, COL_PHONE + " = ? OR " + COL_WORKER_ID + " = ?", new String[]{phoneOrWorkerId, phoneOrWorkerId});
        }
    }

    public Cursor getLoggedInUser() {
        SQLiteDatabase db = this.getReadableDatabase();
        return db.rawQuery("SELECT * FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1", null);
    }

    public String getLoggedInUserName() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_NAME + " FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1", null);
        String name = null;
        if (cursor.moveToFirst()) {
            name = cursor.getString(0);
        }
        cursor.close();
        return name;
    }

    public String getLoggedInUserArea() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT " + COL_AREA + " FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1", null);
        String area = null;
        if (cursor.moveToFirst()) {
            area = cursor.getString(0);
        }
        cursor.close();
        return area;
    }

    public boolean isAnyUserLoggedIn() {
        SQLiteDatabase db = this.getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + TABLE_USERS + " WHERE " + COL_IS_LOGGED_IN + " = 1", null);
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
        
        String query = "SELECT COUNT(*) FROM " + TABLE_VISITS + 
                      " WHERE date(" + COL_VISIT_DATE + ") = ?";
        Cursor cursor = db.rawQuery(query, new String[]{today});
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
        }
        cursor.close();
        return count;
    }

    /**
     * Get count of high-risk patients (pregnant women)
     * Uses medical_notes to identify high-risk cases
     */
    public int getHighRiskPatientsCount() {
        SQLiteDatabase db = this.getReadableDatabase();
        
        // Count pregnant patients - simplified query
        String query = "SELECT COUNT(*) FROM " + TABLE_PATIENTS + 
                      " WHERE " + COL_CATEGORY + " = 'Pregnant'";
        
        Cursor cursor = db.rawQuery(query, null);
        
        int count = 0;
        if (cursor.moveToFirst()) {
            count = cursor.getInt(0);
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
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(localId)});
        
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
        values.put(COL_SYNC_STATUS, Constants.SYNC_PENDING);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());
        
        int result = db.update(TABLE_PREGNANCY_VISITS, values, COL_LOCAL_ID + " = ?",
                new String[]{String.valueOf(visit.getLocalId())});
        
        if (result > 0) {
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
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(localId)});
        
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
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(localId)});
        
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
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put("visit_type", visit.getVisitType());
        values.put("visit_date", visit.getVisitDate());
        values.put("purpose", visit.getPurpose());
        values.put("findings", visit.getFindings());
        values.put("recommendations", visit.getRecommendations());
        values.put("next_visit_date", visit.getNextVisitDate());
        values.put("notes", visit.getNotes());
        values.put(COL_SYNC_STATUS, Constants.SYNC_PENDING);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());
        
        int result = db.update(TABLE_VISITS, values, COL_LOCAL_ID + " = ?",
                new String[]{String.valueOf(visit.getLocalId())});
        
        if (result > 0) {
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
        Cursor cursor = db.rawQuery(query, new String[]{String.valueOf(localId)});
        
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
        values.put(COL_SYNC_STATUS, Constants.SYNC_PENDING);
        values.put(COL_LAST_UPDATED, getCurrentTimestamp());
        
        int result = db.update(TABLE_CHILD_GROWTH, values, COL_LOCAL_ID + " = ?",
                new String[]{String.valueOf(growth.getLocalId())});
        
        if (result > 0) {
            addToSyncQueue(TABLE_CHILD_GROWTH, growth.getLocalId(), Constants.ACTION_UPDATE);
        }
        return result;
    }

    // ==================== SIMPLIFIED METHODS FOR ADD PATIENT SCREEN ====================

    /**
     * Simplified method to add patient with basic details
     */
    public long addPatient(String name, int age, String gender, String category, 
                           String village, String phone, String abhaId, 
                           String ashaPhone, String currentDate) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(COL_NAME, name);
        values.put(COL_AGE, age);
        values.put(COL_GENDER, gender);
        values.put(COL_CATEGORY, category);
        values.put(COL_AREA, village);
        values.put(COL_PHONE, phone);
        // Store ABHA ID in medical_notes for now
        if (abhaId != null && !abhaId.isEmpty()) {
            values.put(COL_MEDICAL_NOTES, "ABHA ID: " + abhaId);
        }
        values.put(COL_SYNC_STATUS, SYNC_PENDING);
        values.put(COL_CREATED_AT, currentDate);
        values.put(COL_LAST_UPDATED, currentDate);

        long patientId = db.insert(TABLE_PATIENTS, null, values);
        
        if (patientId > 0) {
            addToSyncQueue(TABLE_PATIENTS, patientId, "INSERT");
        }
        
        return patientId;
    }

    /**
     * Add pregnancy visit data
     */
    public long addPregnancyVisit(long patientId, String lmpDate, String edd, 
                                   String bloodPressure, String weight, String hemoglobin,
                                   String dangerSigns, String medicines, String nextVisitDate) {
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
        values.put(COL_SYNC_STATUS, SYNC_PENDING);

        long visitId = db.insert(TABLE_PREGNANCY_VISITS, null, values);
        
        if (visitId > 0) {
            addToSyncQueue(TABLE_PREGNANCY_VISITS, visitId, "INSERT");
        }
        
        return visitId;
    }

    /**
     * Add child growth data
     */
    public long addChildGrowth(long patientId, String weight, String height, String muac, 
                               String temperature, String breastfeeding, String complementaryFeeding,
                               String appetite, String symptoms, String lastVaccine, String nextVaccineDate) {
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
        values.put(COL_SYNC_STATUS, SYNC_PENDING);

        long growthId = db.insert(TABLE_CHILD_GROWTH, null, values);
        
        if (growthId > 0) {
            addToSyncQueue(TABLE_CHILD_GROWTH, growthId, "INSERT");
        }
        
        return growthId;
    }

    /**
     * Add general visit data
     */
    public long addGeneralVisit(long patientId, String bloodPressure, String weight, 
                                String sugar, String symptoms, String tobacco, String alcohol,
                                String physicalActivity, String referral, String followUpDate) {
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
        values.put(COL_SYNC_STATUS, SYNC_PENDING);

        long visitId = db.insert(TABLE_VISITS, null, values);
        
        if (visitId > 0) {
            addToSyncQueue(TABLE_VISITS, visitId, "INSERT");
        }
        
        return visitId;
    }
}

