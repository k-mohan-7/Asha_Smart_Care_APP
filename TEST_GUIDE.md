# ASHA SmartCare - Complete System Test Guide

## Admin Credentials
- **Phone:** 1234567890
- **Password:** admin123
- **Role:** admin

## Test Flow

### 1. Splash Screen  Welcome Screen
- App starts  Shows splash animation
- After 2.5s  Navigate to Welcome Screen (role selection)

### 2. Worker Registration (Pending Approval)
1. Click "ASHA Worker Login" on Welcome Screen
2. Click "Create New Account"
3. Fill in worker details
4. Submit registration
5. **Expected:** "Account request submitted. Please wait for admin approval."
6. Try to login  **Expected:** "Your account is pending admin approval"

### 3. Admin Login
1. On Welcome Screen, click "Admin Login"
2. Enter:
   - Phone: 1234567890
   - Password: admin123
3. Click Login
4. **Expected:** Navigate to Admin Dashboard

### 4. Admin Dashboard Features
- View total employees
- View total patients  
- View high-risk alerts
- View pending sync
- Navigate to:
  * Add ASHA Worker
  * Workers List
  * Patients List
  * High Risk Alerts
  * Sync Status

### 5. Approve Worker Account
1. Go to "Workers" from Admin Dashboard
2. View pending requests
3. Approve worker account
4. Worker can now login successfully

### 6. Worker Login (After Approval)
1. Logout from admin
2. Go to Welcome  ASHA Worker Login
3. Login with worker credentials
4. **Expected:** Navigate to Worker Home Dashboard

## Database Status
 MySQL Running
 Database: asha_smartcare
 Admin account created
 All tables exist
 PHP backend updated

## Android App Status
 IP Address: 10.139.130.64
 Database: asha_smartcare.db
 Role-based authentication
 Pending approval flow
 Admin dashboard
 Worker dashboard

## API Endpoints
- Login: http://10.139.130.64/asha_api/auth.php
- Admin: http://10.139.130.64/asha_api/admin.php
- Dashboard: http://10.139.130.64/asha_api/dashboard.php

## Next: Build and Test
