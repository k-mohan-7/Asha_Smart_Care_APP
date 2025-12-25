package com.simats.ashasmartcare.network;

import android.content.Context;
import android.util.Log;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.gson.Gson;
import com.simats.ashasmartcare.models.ChildGrowth;
import com.simats.ashasmartcare.models.Patient;
import com.simats.ashasmartcare.models.PregnancyVisit;
import com.simats.ashasmartcare.models.Vaccination;
import com.simats.ashasmartcare.models.Visit;
import com.simats.ashasmartcare.utils.Constants;
import com.simats.ashasmartcare.utils.SessionManager;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * API Helper class for making network requests to PHP backend
 */
public class ApiHelper {

    private static final String TAG = "ApiHelper";
    private static final int TIMEOUT_MS = 30000; // 30 seconds

    private static ApiHelper instance;
    private RequestQueue requestQueue;
    private Context context;
    private Gson gson;

    public interface ApiCallback {
        void onSuccess(JSONObject response);
        void onError(String errorMessage);
    }

    public static synchronized ApiHelper getInstance(Context context) {
        if (instance == null) {
            instance = new ApiHelper(context.getApplicationContext());
        }
        return instance;
    }

    private ApiHelper(Context context) {
        this.context = context;
        this.requestQueue = Volley.newRequestQueue(context);
        this.gson = new Gson();
    }

    private String getBaseUrl() {
        return SessionManager.getInstance(context).getApiBaseUrl();
    }

    /**
     * Get all patients for an ASHA worker
     */
    public void getPatients(String ashaId, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_PATIENTS + "?asha_id=" + ashaId;
        makeGetRequest(Constants.API_PATIENTS + "?asha_id=" + ashaId, callback);
    }
    
    /**
     * Get pregnancy visits for a patient
     */
    public void getPregnancyVisits(int serverId, ApiCallback callback) {
        makeGetRequest(Constants.API_PREGNANCY_VISITS + "?patient_id=" + serverId, callback);
    }
    
    /**
     * Update pregnancy visit
     */
    public void updatePregnancyVisit(PregnancyVisit visit, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_PREGNANCY_VISITS;

        JSONObject params = new JSONObject();
        try {
            params.put("_method", "PUT");
            params.put("server_id", visit.getServerId());
            params.put("local_id", visit.getLocalId());
            params.put("patient_id", visit.getPatientId());
            params.put("visit_date", visit.getVisitDate());
            params.put("gestational_weeks", visit.getGestationalWeeks());
            params.put("weight", visit.getWeight());
            params.put("blood_pressure", visit.getBloodPressure());
            params.put("hemoglobin", visit.getHemoglobin());
            params.put("fetal_heart_rate", visit.getFetalHeartRate());
            params.put("urine_protein", visit.getUrineProtein());
            params.put("urine_sugar", visit.getUrineSugar());
            params.put("is_high_risk", visit.isHighRisk());
            params.put("high_risk_reason", visit.getHighRiskReason());
            params.put("notes", visit.getNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }
    
    /**
     * Delete patient
     */
    public void deletePatient(int serverId, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_PATIENTS + "?id=" + serverId;
        
        JSONObject params = new JSONObject();
        try {
            params.put("_method", "DELETE");
            params.put("id", serverId);
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }
        
        makePostRequest(url, params, callback);
    }

    /**
     * Login user
     */
    public void login(String phone, String password, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_LOGIN;

        JSONObject params = new JSONObject();
        try {
            params.put("action", "login");
            params.put("phone", phone);
            params.put("password", password);
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Register new user
     */
    public void register(String name, String email, String phone, String password,
                        String workerId, String state, String district, String area, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_REGISTER;

        JSONObject params = new JSONObject();
        try {
            params.put("action", "register");
            params.put("name", name);
            params.put("email", email);
            params.put("phone", phone);
            params.put("password", password);
            params.put("worker_id", workerId);
            params.put("state", state);
            params.put("district", district);
            params.put("area", area);
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Add new patient/person
     */
    public void addPatient(Patient patient, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_PATIENTS;

        JSONObject params = new JSONObject();
        try {
            params.put("local_id", patient.getLocalId());
            params.put("name", patient.getName());
            params.put("age", patient.getAge());
            params.put("dob", patient.getDob());
            params.put("gender", patient.getGender());
            params.put("phone", patient.getPhone());
            params.put("state", patient.getState());
            params.put("district", patient.getDistrict());
            params.put("area", patient.getArea());
            params.put("category", patient.getCategory());
            params.put("medical_notes", patient.getMedicalNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Update patient
     */
    public void updatePatient(Patient patient, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_PATIENTS;

        JSONObject params = new JSONObject();
        try {
            params.put("server_id", patient.getServerId());
            params.put("local_id", patient.getLocalId());
            params.put("name", patient.getName());
            params.put("age", patient.getAge());
            params.put("dob", patient.getDob());
            params.put("gender", patient.getGender());
            params.put("phone", patient.getPhone());
            params.put("state", patient.getState());
            params.put("district", patient.getDistrict());
            params.put("area", patient.getArea());
            params.put("category", patient.getCategory());
            params.put("medical_notes", patient.getMedicalNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Add pregnancy visit
     */
    public void addPregnancyVisit(PregnancyVisit visit, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_PREGNANCY_VISITS;

        JSONObject params = new JSONObject();
        try {
            params.put("local_id", visit.getLocalId());
            params.put("patient_id", visit.getPatientId());
            params.put("visit_date", visit.getVisitDate());
            params.put("gestational_weeks", visit.getGestationalWeeks());
            params.put("weight", visit.getWeight());
            params.put("blood_pressure", visit.getBloodPressure());
            params.put("hemoglobin", visit.getHemoglobin());
            params.put("fetal_heart_rate", visit.getFetalHeartRate());
            params.put("urine_protein", visit.getUrineProtein());
            params.put("urine_sugar", visit.getUrineSugar());
            params.put("is_high_risk", visit.isHighRisk());
            params.put("high_risk_reason", visit.getHighRiskReason());
            params.put("notes", visit.getNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Add child growth record
     */
    public void addChildGrowth(ChildGrowth growth, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_CHILD_GROWTH;

        JSONObject params = new JSONObject();
        try {
            params.put("local_id", growth.getLocalId());
            params.put("patient_id", growth.getPatientId());
            params.put("record_date", growth.getRecordDate());
            params.put("weight", growth.getWeight());
            params.put("height", growth.getHeight());
            params.put("head_circumference", growth.getHeadCircumference());
            params.put("growth_status", growth.getGrowthStatus());
            params.put("notes", growth.getNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Add vaccination record
     */
    public void addVaccination(Vaccination vaccination, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_VACCINATIONS;

        JSONObject params = new JSONObject();
        try {
            params.put("local_id", vaccination.getLocalId());
            params.put("patient_id", vaccination.getPatientId());
            params.put("vaccine_name", vaccination.getVaccineName());
            params.put("due_date", vaccination.getDueDate());
            params.put("given_date", vaccination.getGivenDate());
            params.put("status", vaccination.getStatus());
            params.put("batch_number", vaccination.getBatchNumber());
            params.put("notes", vaccination.getNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Update vaccination status
     */
    public void updateVaccination(Vaccination vaccination, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_VACCINATIONS;

        JSONObject params = new JSONObject();
        try {
            params.put("server_id", vaccination.getServerId());
            params.put("local_id", vaccination.getLocalId());
            params.put("given_date", vaccination.getGivenDate());
            params.put("status", vaccination.getStatus());
            params.put("batch_number", vaccination.getBatchNumber());
            params.put("notes", vaccination.getNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Add visit record
     */
    public void addVisit(Visit visit, ApiCallback callback) {
        String url = getBaseUrl() + Constants.API_VISITS;

        JSONObject params = new JSONObject();
        try {
            params.put("local_id", visit.getLocalId());
            params.put("patient_id", visit.getPatientId());
            params.put("visit_date", visit.getVisitDate());
            params.put("visit_type", visit.getVisitType());
            params.put("description", visit.getDescription());
            params.put("notes", visit.getNotes());
        } catch (JSONException e) {
            callback.onError("Error creating request");
            return;
        }

        makePostRequest(url, params, callback);
    }

    /**
     * Generic request method with configurable HTTP method
     */
    public void makeRequest(int method, String url, JSONObject params, ApiCallback callback) {
        Log.d(TAG, "Request: " + url + " Method: " + method);
        if (params != null) {
            Log.d(TAG, "Params: " + params.toString());
        }

        JsonObjectRequest request = new JsonObjectRequest(
                method,
                url,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = getErrorMessage(error);
                        Log.e(TAG, "Error: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    /**
     * Generic POST request
     */
    private void makePostRequest(String url, JSONObject params, ApiCallback callback) {
        Log.d(TAG, "POST Request: " + url);
        Log.d(TAG, "Params: " + params.toString());

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.POST,
                url,
                params,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = getErrorMessage(error);
                        Log.e(TAG, "Error: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                }
        ) {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String, String> headers = new HashMap<>();
                headers.put("Content-Type", "application/json");
                return headers;
            }
        };

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    /**
     * Generic GET request
     */
    public void makeGetRequest(String endpoint, ApiCallback callback) {
        String url = getBaseUrl() + endpoint;
        Log.d(TAG, "GET Request: " + url);

        JsonObjectRequest request = new JsonObjectRequest(
                Request.Method.GET,
                url,
                null,
                new Response.Listener<JSONObject>() {
                    @Override
                    public void onResponse(JSONObject response) {
                        Log.d(TAG, "Response: " + response.toString());
                        callback.onSuccess(response);
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        String errorMessage = getErrorMessage(error);
                        Log.e(TAG, "Error: " + errorMessage);
                        callback.onError(errorMessage);
                    }
                }
        );

        request.setRetryPolicy(new DefaultRetryPolicy(
                TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
        ));

        requestQueue.add(request);
    }

    /**
     * Get error message from Volley error
     */
    private String getErrorMessage(VolleyError error) {
        if (error.networkResponse != null) {
            int statusCode = error.networkResponse.statusCode;
            switch (statusCode) {
                case 400:
                    return "Bad Request";
                case 401:
                    return "Unauthorized";
                case 403:
                    return "Forbidden";
                case 404:
                    return "Not Found";
                case 500:
                    return "Server Error";
                default:
                    return "Network Error (" + statusCode + ")";
            }
        }

        if (error instanceof com.android.volley.TimeoutError) {
            return "Request Timeout";
        }

        if (error instanceof com.android.volley.NoConnectionError) {
            return "No Internet Connection";
        }

        return "Network Error";
    }

    /**
     * Cancel all pending requests
     */
    public void cancelAllRequests() {
        if (requestQueue != null) {
            requestQueue.cancelAll(new RequestQueue.RequestFilter() {
                @Override
                public boolean apply(Request<?> request) {
                    return true;
                }
            });
        }
    }
}
