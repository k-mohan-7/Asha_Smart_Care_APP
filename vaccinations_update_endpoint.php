<?php
// IMPORTANT: Copy this file to C:\xampp\htdocs\asha_api\vaccinations.php
// This file handles all vaccination-related API requests

header('Content-Type: application/json');
header('Access-Control-Allow-Origin: *');
header('Access-Control-Allow-Methods: GET, POST, PUT, DELETE');
header('Access-Control-Allow-Headers: Content-Type');

// Enable error reporting for debugging
error_reporting(E_ALL);
ini_set('display_errors', 0); // Don't display errors in response
ini_set('log_errors', 1);

// Database connection
$host = 'localhost';
$dbname = 'asha_smartcare';
$username = 'root';
$password = '';

try {
    $pdo = new PDO("mysql:host=$host;dbname=$dbname", $username, $password);
    $pdo->setAttribute(PDO::ATTR_ERRMODE, PDO::ERRMODE_EXCEPTION);
} catch(PDOException $e) {
    echo json_encode(['success' => false, 'message' => 'Database connection failed: ' . $e->getMessage()]);
    exit;
}

// Parse input data from both JSON and form data
$input = [];
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    // Try to get JSON input first
    $json_input = file_get_contents('php://input');
    $decoded = json_decode($json_input, true);
    
    if (json_last_error() === JSON_ERROR_NONE && is_array($decoded)) {
        $input = $decoded;
    } else {
        // Fall back to POST form data
        $input = $_POST;
    }
}

// Determine action
$action = '';
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $action = isset($input['action']) ? $input['action'] : '';
} else if ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $action = isset($_GET['action']) ? $_GET['action'] : 'get_vaccinations';
}

// Handle GET request - retrieve vaccinations
if ($_SERVER['REQUEST_METHOD'] === 'GET' && isset($_GET['asha_id'])) {
    try {
        $asha_id = $_GET['asha_id'];
        
        $sql = "SELECT v.*, p.name as patient_name 
                FROM vaccinations v 
                LEFT JOIN patients p ON v.patient_id = p.id 
                WHERE v.asha_id = :asha_id 
                ORDER BY v.scheduled_date ASC";
        
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':asha_id', $asha_id);
        $stmt->execute();
        
        $vaccinations = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode([
            'success' => true,
            'data' => $vaccinations
        ]);
        
    } catch(PDOException $e) {
        echo json_encode([
            'success' => false,
            'message' => 'Error fetching vaccinations: ' . $e->getMessage()
        ]);
    }
    exit;
}

// Handle GET request with patient_id filter
if ($_SERVER['REQUEST_METHOD'] === 'GET' && isset($_GET['patient_id'])) {
    try {
        $patient_id = $_GET['patient_id'];
        
        $sql = "SELECT v.*, p.name as patient_name 
                FROM vaccinations v 
                LEFT JOIN patients p ON v.patient_id = p.id 
                WHERE v.patient_id = :patient_id 
                ORDER BY v.scheduled_date ASC";
        
        $stmt = $pdo->prepare($sql);
        $stmt->bindParam(':patient_id', $patient_id);
        $stmt->execute();
        
        $vaccinations = $stmt->fetchAll(PDO::FETCH_ASSOC);
        
        echo json_encode([
            'success' => true,
            'data' => $vaccinations
        ]);
        
    } catch(PDOException $e) {
        echo json_encode([
            'success' => false,
            'message' => 'Error fetching vaccinations: ' . $e->getMessage()
        ]);
    }
    exit;
}

// Handle POST request based on action
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    
    // Auto-detect action if not specified
    if (empty($action)) {
        // If vaccination_id is present, it's an update
        if (isset($input['vaccination_id'])) {
            $action = 'update_status';
        }
        // If patient_id, vaccine_name, and scheduled_date are present, it's an add
        else if (isset($input['patient_id']) && isset($input['vaccine_name']) && isset($input['scheduled_date'])) {
            $action = 'add_vaccination';
        }
    }
    
    // UPDATE VACCINATION STATUS - THIS IS THE NEW ENDPOINT
    if ($action === 'update_status') {
        try {
            $vaccination_id = isset($input['vaccination_id']) ? $input['vaccination_id'] : null;
            $status = isset($input['status']) ? $input['status'] : null;
            $given_date = isset($input['given_date']) ? $input['given_date'] : null;
            
            if (!$vaccination_id || !$status) {
                echo json_encode([
                    'success' => false,
                    'message' => 'Vaccination ID and status are required'
                ]);
                exit;
            }
            
            // Prepare update query
            $sql = "UPDATE vaccinations SET status = :status";
            
            if ($given_date && $status === 'Given') {
                $sql .= ", given_date = :given_date";
            }
            
            $sql .= ", updated_at = NOW() WHERE id = :vaccination_id";
            
            $stmt = $pdo->prepare($sql);
            $stmt->bindParam(':status', $status);
            $stmt->bindParam(':vaccination_id', $vaccination_id);
            
            if ($given_date && $status === 'Given') {
                $stmt->bindParam(':given_date', $given_date);
            }
            
            $stmt->execute();
            
            // Fetch updated vaccination
            $stmt = $pdo->prepare("SELECT v.*, p.name as patient_name 
                                  FROM vaccinations v 
                                  LEFT JOIN patients p ON v.patient_id = p.id 
                                  WHERE v.id = :vaccination_id");
            $stmt->bindParam(':vaccination_id', $vaccination_id);
            $stmt->execute();
            $updated_vaccination = $stmt->fetch(PDO::FETCH_ASSOC);
            
            echo json_encode([
                'success' => true,
                'message' => 'Vaccination status updated successfully',
                'data' => $updated_vaccination
            ]);
            
        } catch(PDOException $e) {
            echo json_encode([
                'success' => false,
                'message' => 'Error updating vaccination status: ' . $e->getMessage()
            ]);
        }
        exit;
    }
    
    // ADD NEW VACCINATION
    if ($action === 'add_vaccination') {
        try {
            $patient_id = isset($input['patient_id']) ? $input['patient_id'] : null;
            $vaccine_name = isset($input['vaccine_name']) ? $input['vaccine_name'] : null;
            $scheduled_date = isset($input['scheduled_date']) ? $input['scheduled_date'] : null;
            $asha_id = isset($input['asha_id']) ? $input['asha_id'] : null;
            $batch_number = isset($input['batch_number']) ? $input['batch_number'] : null;
            $notes = isset($input['notes']) ? $input['notes'] : null;
            $side_effects = isset($input['side_effects']) ? $input['side_effects'] : null;
            $status = isset($input['status']) ? $input['status'] : 'Scheduled';
            $given_date = isset($input['given_date']) ? $input['given_date'] : null;
            
            if (!$patient_id || !$vaccine_name || !$scheduled_date || !$asha_id) {
                echo json_encode([
                    'success' => false,
                    'message' => 'Required fields missing: patient_id, vaccine_name, scheduled_date, asha_id'
                ]);
                exit;
            }
            
            $sql = "INSERT INTO vaccinations (patient_id, vaccine_name, scheduled_date, given_date, asha_id, batch_number, side_effects, notes, status, sync_status, created_at, updated_at) 
                    VALUES (:patient_id, :vaccine_name, :scheduled_date, :given_date, :asha_id, :batch_number, :side_effects, :notes, :status, 'SYNCED', NOW(), NOW())";
            
            $stmt = $pdo->prepare($sql);
            $stmt->bindParam(':patient_id', $patient_id);
            $stmt->bindParam(':vaccine_name', $vaccine_name);
            $stmt->bindParam(':scheduled_date', $scheduled_date);
            $stmt->bindParam(':given_date', $given_date);
            $stmt->bindParam(':asha_id', $asha_id);
            $stmt->bindParam(':batch_number', $batch_number);
            $stmt->bindParam(':side_effects', $side_effects);
            $stmt->bindParam(':notes', $notes);
            $stmt->bindParam(':status', $status);
            
            $stmt->execute();
            $new_id = $pdo->lastInsertId();
            
            echo json_encode([
                'success' => true,
                'message' => 'Vaccination added successfully',
                'vaccination_id' => $new_id
            ]);
            
        } catch(PDOException $e) {
            echo json_encode([
                'success' => false,
                'message' => 'Error adding vaccination: ' . $e->getMessage()
            ]);
        }
        exit;
    }
}

// Default response
echo json_encode([
    'success' => false,
    'message' => 'Invalid request'
]);
?>
