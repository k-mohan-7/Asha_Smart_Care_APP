# Backend Vaccination API Fix Guide

## Error Description
When posting vaccination data to the backend, the API returns:
```
Error: Server response error - Invalid data format
```

## Request Details
**Endpoint:** `http://192.168.1.69/asha_api/vaccinations.php`
**Method:** POST
**Data Sent:**
```json
{
  "patient_id": 1,
  "vaccine_name": "BCG",
  "scheduled_date": "2026-02-21",
  "status": "Scheduled",
  "given_date": "",
  "batch_number": "r673u2",
  "side_effects": "no",
  "notes": "no",
  "asha_id": 1
}
```

## Backend Fix Required

### File: `/var/www/html/asha_api/vaccinations.php`

The backend PHP file needs to accept JSON POST requests properly. Here's the fix:

```php
<?php
header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE, OPTIONS');
header('Access-Control-Allow-Headers: Content-Type');

// Handle preflight OPTIONS request
if ($_SERVER['REQUEST_METHOD'] === 'OPTIONS') {
    http_response_code(200);
    exit();
}

require_once 'db_config.php';

// Get request method
$method = $_SERVER['REQUEST_METHOD'];

// Get JSON input
$input = file_get_contents('php://input');
$data = json_decode($input, true);

// If JSON decode failed, try to get from $_POST
if ($data === null && !empty($_POST)) {
    $data = $_POST;
}

// CREATE - Add new vaccination
if ($method === 'POST' && !isset($data['id'])) {
    // Validate required fields
    $required_fields = ['patient_id', 'vaccine_name', 'scheduled_date', 'status', 'asha_id'];
    foreach ($required_fields as $field) {
        if (!isset($data[$field]) || $data[$field] === '') {
            if ($field === 'given_date' || $field === 'batch_number' || $field === 'side_effects' || $field === 'notes') {
                continue; // Optional fields
            }
            echo json_encode([
                'success' => false,
                'message' => "Missing required field: $field"
            ]);
            exit();
        }
    }
    
    // Prepare data
    $patient_id = mysqli_real_escape_string($conn, $data['patient_id']);
    $vaccine_name = mysqli_real_escape_string($conn, $data['vaccine_name']);
    $scheduled_date = mysqli_real_escape_string($conn, $data['scheduled_date']);
    $status = mysqli_real_escape_string($conn, $data['status']);
    $given_date = isset($data['given_date']) && $data['given_date'] !== '' 
        ? "'" . mysqli_real_escape_string($conn, $data['given_date']) . "'" 
        : 'NULL';
    $batch_number = isset($data['batch_number']) 
        ? mysqli_real_escape_string($conn, $data['batch_number']) 
        : '';
    $side_effects = isset($data['side_effects']) 
        ? mysqli_real_escape_string($conn, $data['side_effects']) 
        : '';
    $notes = isset($data['notes']) 
        ? mysqli_real_escape_string($conn, $data['notes']) 
        : '';
    $asha_id = mysqli_real_escape_string($conn, $data['asha_id']);
    
    // Insert query
    $sql = "INSERT INTO vaccinations 
            (patient_id, vaccine_name, scheduled_date, given_date, status, 
             batch_number, side_effects, notes, asha_id, created_at, updated_at) 
            VALUES 
            ('$patient_id', '$vaccine_name', '$scheduled_date', $given_date, '$status', 
             '$batch_number', '$side_effects', '$notes', '$asha_id', NOW(), NOW())";
    
    if (mysqli_query($conn, $sql)) {
        $vaccination_id = mysqli_insert_id($conn);
        echo json_encode([
            'success' => true,
            'message' => 'Vaccination added successfully',
            'vaccination_id' => $vaccination_id
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Failed to add vaccination: ' . mysqli_error($conn)
        ]);
    }
    exit();
}

// UPDATE - Update existing vaccination
if ($method === 'PUT' || (isset($data['id']) && $method === 'POST')) {
    if (!isset($data['id'])) {
        echo json_encode([
            'success' => false,
            'message' => 'Vaccination ID required for update'
        ]);
        exit();
    }
    
    $id = mysqli_real_escape_string($conn, $data['id']);
    $updates = [];
    
    if (isset($data['patient_id'])) {
        $updates[] = "patient_id = '" . mysqli_real_escape_string($conn, $data['patient_id']) . "'";
    }
    if (isset($data['vaccine_name'])) {
        $updates[] = "vaccine_name = '" . mysqli_real_escape_string($conn, $data['vaccine_name']) . "'";
    }
    if (isset($data['scheduled_date'])) {
        $updates[] = "scheduled_date = '" . mysqli_real_escape_string($conn, $data['scheduled_date']) . "'";
    }
    if (isset($data['given_date'])) {
        if ($data['given_date'] === '' || $data['given_date'] === null) {
            $updates[] = "given_date = NULL";
        } else {
            $updates[] = "given_date = '" . mysqli_real_escape_string($conn, $data['given_date']) . "'";
        }
    }
    if (isset($data['status'])) {
        $updates[] = "status = '" . mysqli_real_escape_string($conn, $data['status']) . "'";
    }
    if (isset($data['batch_number'])) {
        $updates[] = "batch_number = '" . mysqli_real_escape_string($conn, $data['batch_number']) . "'";
    }
    if (isset($data['side_effects'])) {
        $updates[] = "side_effects = '" . mysqli_real_escape_string($conn, $data['side_effects']) . "'";
    }
    if (isset($data['notes'])) {
        $updates[] = "notes = '" . mysqli_real_escape_string($conn, $data['notes']) . "'";
    }
    
    $updates[] = "updated_at = NOW()";
    
    $sql = "UPDATE vaccinations SET " . implode(', ', $updates) . " WHERE id = '$id'";
    
    if (mysqli_query($conn, $sql)) {
        echo json_encode([
            'success' => true,
            'message' => 'Vaccination updated successfully'
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Failed to update vaccination: ' . mysqli_error($conn)
        ]);
    }
    exit();
}

// READ - Get vaccinations
if ($method === 'GET') {
    // Get single vaccination by ID
    if (isset($_GET['id'])) {
        $id = mysqli_real_escape_string($conn, $_GET['id']);
        $sql = "SELECT v.*, p.name as patient_name 
                FROM vaccinations v 
                LEFT JOIN patients p ON v.patient_id = p.id 
                WHERE v.id = '$id'";
        
        $result = mysqli_query($conn, $sql);
        if ($row = mysqli_fetch_assoc($result)) {
            echo json_encode([
                'success' => true,
                'data' => $row
            ]);
        } else {
            echo json_encode([
                'success' => false,
                'message' => 'Vaccination not found'
            ]);
        }
        exit();
    }
    
    // Get vaccinations by patient_id
    if (isset($_GET['patient_id'])) {
        $patient_id = mysqli_real_escape_string($conn, $_GET['patient_id']);
        $sql = "SELECT * FROM vaccinations WHERE patient_id = '$patient_id' ORDER BY scheduled_date DESC";
        
        $result = mysqli_query($conn, $sql);
        $vaccinations = [];
        while ($row = mysqli_fetch_assoc($result)) {
            $vaccinations[] = $row;
        }
        
        echo json_encode([
            'success' => true,
            'data' => $vaccinations
        ]);
        exit();
    }
    
    // Get all vaccinations by ASHA ID
    if (isset($_GET['asha_id'])) {
        $asha_id = mysqli_real_escape_string($conn, $_GET['asha_id']);
        $sql = "SELECT v.*, p.name as patient_name 
                FROM vaccinations v 
                LEFT JOIN patients p ON v.patient_id = p.id 
                WHERE v.asha_id = '$asha_id' 
                ORDER BY v.scheduled_date DESC";
        
        $result = mysqli_query($conn, $sql);
        $vaccinations = [];
        while ($row = mysqli_fetch_assoc($result)) {
            $vaccinations[] = $row;
        }
        
        echo json_encode([
            'success' => true,
            'data' => $vaccinations
        ]);
        exit();
    }
    
    echo json_encode([
        'success' => false,
        'message' => 'Missing parameter: id, patient_id, or asha_id required'
    ]);
    exit();
}

// DELETE - Delete vaccination
if ($method === 'DELETE') {
    if (!isset($data['id'])) {
        echo json_encode([
            'success' => false,
            'message' => 'Vaccination ID required'
        ]);
        exit();
    }
    
    $id = mysqli_real_escape_string($conn, $data['id']);
    $sql = "DELETE FROM vaccinations WHERE id = '$id'";
    
    if (mysqli_query($conn, $sql)) {
        echo json_encode([
            'success' => true,
            'message' => 'Vaccination deleted successfully'
        ]);
    } else {
        echo json_encode([
            'success' => false,
            'message' => 'Failed to delete vaccination: ' . mysqli_error($conn)
        ]);
    }
    exit();
}

// Invalid method
echo json_encode([
    'success' => false,
    'message' => 'Invalid request method'
]);
?>
```

## Database Table Schema

Ensure your `vaccinations` table has the following structure:

```sql
CREATE TABLE IF NOT EXISTS `vaccinations` (
  `id` int(11) NOT NULL AUTO_INCREMENT,
  `patient_id` int(11) NOT NULL,
  `vaccine_name` varchar(100) NOT NULL,
  `scheduled_date` date NOT NULL,
  `given_date` date DEFAULT NULL,
  `status` varchar(20) DEFAULT 'Scheduled',
  `batch_number` varchar(50) DEFAULT NULL,
  `side_effects` text DEFAULT NULL,
  `notes` text DEFAULT NULL,
  `asha_id` int(11) NOT NULL,
  `created_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`),
  KEY `patient_id` (`patient_id`),
  KEY `asha_id` (`asha_id`),
  FOREIGN KEY (`patient_id`) REFERENCES `patients`(`id`) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

## Steps to Apply Fix

1. **Connect to your server:**
   ```bash
   ssh your-server-ip
   ```

2. **Backup existing file:**
   ```bash
   cd /var/www/html/asha_api/
   cp vaccinations.php vaccinations.php.backup
   ```

3. **Edit the file:**
   ```bash
   nano vaccinations.php
   ```

4. **Replace content with the fixed code above**

5. **Save and test:**
   - Press `Ctrl + X`, then `Y`, then `Enter`
   - Test from the app

## Common Issues

### Issue: "Invalid data format"
**Cause:** Backend not properly reading JSON POST data
**Fix:** Use `file_get_contents('php://input')` and `json_decode()`

### Issue: Empty given_date causing errors
**Cause:** MySQL doesn't accept empty strings for DATE fields
**Fix:** Use NULL for empty date fields

### Issue: CORS errors
**Cause:** Missing CORS headers
**Fix:** Add proper CORS headers at the top of PHP file

## Testing

After applying the fix, test with the app:
1. Open VaccinationListActivity
2. Click "Add Vaccination" button
3. Select a patient (should now load from API)
4. Fill in vaccination details
5. Save
6. Should see "Vaccination saved successfully!"

## Verification

Check backend logs:
```bash
tail -f /var/log/apache2/error.log
```

Check MySQL:
```bash
mysql -u root -p
USE asha_smartcare;
SELECT * FROM vaccinations ORDER BY id DESC LIMIT 5;
```
