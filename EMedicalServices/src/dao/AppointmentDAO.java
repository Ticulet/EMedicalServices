package dao;

import db.DatabaseConnection;
import model.Appointment;
// No need to import Patient model here if joins are handled correctly in SQL

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentDAO {

    public boolean createAppointment(Appointment appointment) {
        String sql = "INSERT INTO Appointments (patient_id, doctor_id, data_programare, " +
                "descriere_afectiune, status) VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

            pstmt.setInt(1, appointment.getPatientId());
            pstmt.setInt(2, appointment.getDoctorId());
            pstmt.setTimestamp(3, appointment.getDataProgramare());
            pstmt.setString(4, appointment.getDescriereAfectiune());
            pstmt.setString(5, appointment.getStatus() != null ? appointment.getStatus() : "SCHEDULED");

            int affectedRows = pstmt.executeUpdate();

            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        appointment.setId(generatedKeys.getInt(1));
                    }
                }
                return true;
            } else {
                return false;
            }
        } catch (SQLException e) {
            System.err.println("Error creating appointment: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Appointment> getAppointmentsForPatient(int patientId) {
        List<Appointment> appointments = new ArrayList<>();
        String sql = "SELECT id, patient_id, doctor_id, data_programare, descriere_afectiune, status " +
                "FROM Appointments WHERE patient_id = ? ORDER BY data_programare DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                appointments.add(new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getTimestamp("data_programare"),
                        rs.getString("descriere_afectiune"),
                        rs.getString("status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointments for patient " + patientId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }

    public boolean cancelAppointment(int appointmentId) {
        String sql = "UPDATE Appointments SET status = ? WHERE id = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, "CANCELLED");
            pstmt.setInt(2, appointmentId);
            pstmt.setString(3, "SCHEDULED");

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error cancelling appointment " + appointmentId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public boolean updateAppointmentDescription(int appointmentId, String newDescription) {
        String sql = "UPDATE Appointments SET descriere_afectiune = ? WHERE id = ? AND status = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newDescription);
            pstmt.setInt(2, appointmentId);
            pstmt.setString(3, "SCHEDULED");
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment description for ID " + appointmentId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public Appointment getAppointmentById(int appointmentId) {
        String sql = "SELECT id, patient_id, doctor_id, data_programare, descriere_afectiune, status " +
                "FROM Appointments WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Appointment(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getTimestamp("data_programare"),
                        rs.getString("descriere_afectiune"),
                        rs.getString("status")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching appointment by ID " + appointmentId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public List<Appointment> getAppointmentsForDoctor(int doctorId) {
        return getAppointmentsForDoctorFiltered(doctorId, null, null); // Pass null for dateFilter
    }

    public boolean updateAppointmentStatus(int appointmentId, String newStatus) {
        String sql = "UPDATE Appointments SET status = ? WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, newStatus);
            pstmt.setInt(2, appointmentId);
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error updating appointment status for ID " + appointmentId + ": " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    // MODIFIED METHOD: getAppointmentsForDoctorFiltered to include dateFilter
    public List<Appointment> getAppointmentsForDoctorFiltered(int doctorId, String patientNameFilter, String dateFilter) {
        List<Appointment> appointments = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT a.id, a.patient_id, a.doctor_id, a.data_programare, a.descriere_afectiune, a.status " +
                        "FROM Appointments a "
        );
        List<Object> params = new ArrayList<>();

        if (patientNameFilter != null && !patientNameFilter.trim().isEmpty()) {
            sqlBuilder.append("JOIN Patients p ON a.patient_id = p.id ");
        }

        sqlBuilder.append("WHERE a.doctor_id = ? ");
        params.add(doctorId);

        if (patientNameFilter != null && !patientNameFilter.trim().isEmpty()) {
            sqlBuilder.append("AND (LOWER(p.nume) LIKE LOWER(?) OR LOWER(p.prenume) LIKE LOWER(?)) ");
            String likePattern = "%" + patientNameFilter.trim() + "%";
            params.add(likePattern);
            params.add(likePattern);
        }

        if (dateFilter != null && !dateFilter.trim().isEmpty()) {
            // Assuming dateFilter is in "YYYY-MM-DD" format
            // We need to compare the DATE part of the data_programare (which is DATETIME)
            sqlBuilder.append("AND DATE(a.data_programare) = ? ");
            params.add(dateFilter.trim());
        }

        sqlBuilder.append("ORDER BY CASE a.status WHEN 'SCHEDULED' THEN 1 WHEN 'COMPLETED' THEN 2 WHEN 'CANCELLED' THEN 3 ELSE 4 END, a.data_programare ASC");

        System.out.println("Executing filtered appointment search for doctor SQL: " + sqlBuilder.toString());
        System.out.println("Parameters: " + params);

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;
            for (Object param : params) {
                if (param instanceof java.sql.Date) { // If we were passing java.sql.Date objects
                    pstmt.setDate(paramIndex++, (java.sql.Date) param);
                } else {
                    pstmt.setObject(paramIndex++, param);
                }
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                appointments.add(new Appointment(
                        rs.getInt("a.id"),
                        rs.getInt("a.patient_id"),
                        rs.getInt("a.doctor_id"),
                        rs.getTimestamp("a.data_programare"),
                        rs.getString("a.descriere_afectiune"),
                        rs.getString("a.status")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching filtered appointments for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return appointments;
    }
}