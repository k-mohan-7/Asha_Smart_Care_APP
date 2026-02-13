<?php
header("Content-Type: application/json; charset=UTF-8");
require_once 'db_connect.php';

$method = $_SERVER['REQUEST_METHOD'];

if ($method == 'GET') {
    $asha_id = isset($_GET['asha_id']) ? $_GET['asha_id'] : null;
    $id = isset($_GET['id']) ? $_GET['id'] : null;
    if ($id) { getPatientById($conn, $id); }
    else if ($asha_id) { getPatientsByAsha($conn, $asha_id); }
    else { echo json_encode(["status" => "error", "message" => "Missing asha_id or id parameter"]); }
}

if ($method == 'POST') {
    $input = json_decode(file_get_contents("php://input"), true);
    $action_method = isset($input['_method']) ? strtoupper($input['_method']) : 'POST';
    if ($action_method == 'DELETE') { deletePatient($conn, $input); }
    else if ($action_method == 'PUT' || isset($input['server_id'])) { updatePatient($conn, $input); }
    else { addPatient($conn, $input); }
}

function getPatientsByAsha($conn, $asha_id) {
    // Updated to use 'address' instead of 'village', and include blood_group
    // Removed district, state, emergency_contact
    $sql = "SELECT id, asha_id, name, age, gender, phone, abha_id, address, category, blood_group, 
            is_high_risk, high_risk_reason, photo_url, sync_status, created_at, updated_at 
            FROM patients WHERE asha_id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $asha_id);
    $stmt->execute();
    $result = $stmt->get_result();
    $patients = [];
    while ($row = $result->fetch_assoc()) { $patients[] = $row; }
    echo json_encode(["status" => "success", "patients" => $patients]);
}

function getPatientById($conn, $id) {
    // Updated to use 'address' instead of 'village', and include blood_group
    $sql = "SELECT id, asha_id, name, age, gender, phone, abha_id, address, category, blood_group,
            is_high_risk, high_risk_reason, photo_url, sync_status, created_at, updated_at 
            FROM patients WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $result = $stmt->get_result();
    if ($result->num_rows > 0) {
        echo json_encode(["status" => "success", "patient" => $result->fetch_assoc()]);
    } else {
        echo json_encode(["status" => "error", "message" => "Patient not found"]);
    }
}

function addPatient($conn, $data) {
    $asha_id = isset($data['asha_id']) ? $data['asha_id'] : 1;
    $local_id = isset($data['local_id']) ? $data['local_id'] : '';
    $name = $data['name'];
    $age = isset($data['age']) ? $data['age'] : 0;
    $gender = isset($data['gender']) ? $data['gender'] : '';
    $phone = isset($data['phone']) ? $data['phone'] : '';
    $abha_id = isset($data['abha_id']) ? $data['abha_id'] : '';
    
    // CHANGED: Use 'address' instead of 'village'
    $address = isset($data['address']) ? $data['address'] : (isset($data['village']) ? $data['village'] : '');
    
    $category = isset($data['category']) ? $data['category'] : '';
    
    // NEW: blood_group field
    $blood_group = isset($data['blood_group']) ? $data['blood_group'] : '';
    
    $is_high_risk = isset($data['is_high_risk']) ? intval($data['is_high_risk']) : 0;
    $high_risk_reason = isset($data['high_risk_reason']) ? $data['high_risk_reason'] : '';
    
    // UPDATED: Use 'address' and add 'blood_group'
    $sql = "INSERT INTO patients (asha_id, name, age, gender, phone, abha_id, address, category, blood_group, is_high_risk, high_risk_reason) 
            VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("isisssssssi", $asha_id, $name, $age, $gender, $phone, $abha_id, $address, $category, $blood_group, $is_high_risk, $high_risk_reason);
    
    if ($stmt->execute()) {
        $server_id = $conn->insert_id;
        echo json_encode([
            "success" => true, 
            "status" => "success", 
            "message" => "Patient created successfully", 
            "id" => $server_id, 
            "server_id" => $server_id, 
            "local_id" => $local_id, 
            "data" => ["id" => $server_id]
        ]);
    } else {
        echo json_encode(["status" => "error", "message" => "Error adding patient: " . $conn->error]);
    }
}

function updatePatient($conn, $data) {
    $id = isset($data['server_id']) ? $data['server_id'] : (isset($data['id']) ? $data['id'] : null);
    if (!$id) { echo json_encode(["status" => "error", "message" => "Missing server_id"]); return; }
    
    $name = $data['name'];
    $age = $data['age'];
    $gender = $data['gender'];
    $phone = $data['phone'];
    $abha_id = isset($data['abha_id']) ? $data['abha_id'] : '';
    
    // CHANGED: Use 'address' instead of 'village'
    $address = isset($data['address']) ? $data['address'] : (isset($data['village']) ? $data['village'] : '');
    
    $category = $data['category'];
    
    // NEW: blood_group field
    $blood_group = isset($data['blood_group']) ? $data['blood_group'] : '';
    
    $is_high_risk = isset($data['is_high_risk']) ? intval($data['is_high_risk']) : 0;
    $high_risk_reason = isset($data['high_risk_reason']) ? $data['high_risk_reason'] : '';
    
    // UPDATED: Use 'address' and add 'blood_group'
    $sql = "UPDATE patients SET name=?, age=?, gender=?, phone=?, abha_id=?, address=?, category=?, blood_group=?, is_high_risk=?, high_risk_reason=? 
            WHERE id=?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("sissssssssi", $name, $age, $gender, $phone, $abha_id, $address, $category, $blood_group, $is_high_risk, $high_risk_reason, $id);
    
    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Patient updated successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Error updating patient: " . $conn->error]);
    }
}

function deletePatient($conn, $data) {
    $id = isset($data['id']) ? $data['id'] : null;
    if (!$id) { echo json_encode(["status" => "error", "message" => "Missing id"]); return; }
    
    $sql = "DELETE FROM patients WHERE id = ?";
    $stmt = $conn->prepare($sql);
    $stmt->bind_param("i", $id);
    
    if ($stmt->execute()) {
        echo json_encode(["status" => "success", "message" => "Patient deleted successfully"]);
    } else {
        echo json_encode(["status" => "error", "message" => "Error deleting patient: " . $conn->error]);
    }
}

$conn->close();
?>
