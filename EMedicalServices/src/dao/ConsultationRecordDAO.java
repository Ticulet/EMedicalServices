package dao;

import db.DatabaseConnection;
import model.ConsultationRecord;
// No need to import Appointment model here unless you also return appointment details directly
// from these specific methods, which we are not currently doing (relying on joins or UI layer fetching).

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ConsultationRecordDAO {

    // Method to add a consultation record (likely used by Doctor functionality)
    public boolean addConsultationRecord(ConsultationRecord record) {
        String sql = "INSERT INTO ConsultationRecords (appointment_id, diagnostic, prescriptie, data_consultatie) " +
                "VALUES (?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, record.getAppointmentId());
            pstmt.setString(2, record.getDiagnostic());
            pstmt.setString(3, record.getPrescriptie());

            // Use provided consultation date, or current time if null
            if (record.getDataConsultatie() != null) {
                pstmt.setTimestamp(4, record.getDataConsultatie());
            } else {
                pstmt.setTimestamp(4, new Timestamp(System.currentTimeMillis()));
            }

            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        record.setId(generatedKeys.getInt(1)); // Set the auto-generated ID back to the object
                    }
                }
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.err.println("Error adding consultation record: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // Method to get all consultation records for a specific patient (used by PatientDashboardFrame)
    // This joins with Appointments to filter by patient_id and status.
    public List<ConsultationRecord> getConsultationRecordsForPatient(int patientId) {
        List<ConsultationRecord> records = new ArrayList<>();
        // The SQL query joins to filter by patient and ensure appointment was completed.
        // It selects all columns from ConsultationRecords.
        String sql = "SELECT cr.id, cr.appointment_id, cr.diagnostic, cr.prescriptie, cr.data_consultatie " +
                "FROM ConsultationRecords cr " +
                "JOIN Appointments a ON cr.appointment_id = a.id " +
                "WHERE a.patient_id = ? AND a.status = 'COMPLETED' " +
                "ORDER BY cr.data_consultatie DESC";

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                ConsultationRecord record = new ConsultationRecord(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        rs.getString("diagnostic"),
                        rs.getString("prescriptie"),
                        rs.getTimestamp("data_consultatie")
                );
                records.add(record);
            }
        } catch (SQLException e) {
            System.err.println("Error fetching consultation records for patient " + patientId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }

    // NEW METHOD: Get consultation records for a specific patient WITH a specific doctor
    // (used by Doctor when viewing a patient's history with them)
    public List<ConsultationRecord> getConsultationRecordsForPatientWithDoctor(int patientId, int doctorId) {
        List<ConsultationRecord> records = new ArrayList<>();
        // This SQL query joins ConsultationRecords with Appointments
        // and filters by both patient_id and doctor_id, and completed status.
        String sql = "SELECT cr.id, cr.appointment_id, cr.diagnostic, cr.prescriptie, cr.data_consultatie " +
                "FROM ConsultationRecords cr " +
                "JOIN Appointments a ON cr.appointment_id = a.id " +
                "WHERE a.patient_id = ? AND a.doctor_id = ? AND a.status = 'COMPLETED' " +
                "ORDER BY cr.data_consultatie DESC"; // Show most recent first

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            pstmt.setInt(2, doctorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                records.add(new ConsultationRecord(
                        rs.getInt("id"),
                        rs.getInt("appointment_id"),
                        rs.getString("diagnostic"),
                        rs.getString("prescriptie"),
                        rs.getTimestamp("data_consultatie")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching consultation records for patient " + patientId + " with doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return records;
    }
}