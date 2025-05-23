package dao;

import db.DatabaseConnection;
import model.Feedback;

import java.sql.*;
import java.util.List; // For fetching feedback
import java.util.ArrayList; // For fetching feedback

public class FeedbackDAO {

    // ... (existing addFeedback and hasFeedbackForAppointment methods) ...
    public boolean addFeedback(Feedback feedback) { /* ... as before ... */
        if (hasFeedbackForAppointment(feedback.getAppointmentId())) {
            System.err.println("Feedback already submitted for appointment ID: " + feedback.getAppointmentId());
            return false;
        }
        String sql = "INSERT INTO Feedbacks (patient_id, doctor_id, appointment_id, comentariu, nota, data_feedback) " +
                "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            pstmt.setInt(1, feedback.getPatientId());
            pstmt.setInt(2, feedback.getDoctorId());
            pstmt.setInt(3, feedback.getAppointmentId());
            pstmt.setString(4, feedback.getComentariu());
            pstmt.setInt(5, feedback.getNota());
            pstmt.setTimestamp(6, new Timestamp(System.currentTimeMillis()));
            int affectedRows = pstmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet generatedKeys = pstmt.getGeneratedKeys()) {
                    if (generatedKeys.next()) {
                        feedback.setId(generatedKeys.getInt(1));
                    }
                } return true;
            } return false;
        } catch (SQLException e) { System.err.println("Error adding feedback: " + e.getMessage()); e.printStackTrace(); return false; }
    }

    public boolean hasFeedbackForAppointment(int appointmentId) { /* ... as before ... */
        String sql = "SELECT COUNT(*) FROM Feedbacks WHERE appointment_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, appointmentId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt(1) > 0;
            }
        } catch (SQLException e) { System.err.println("Error checking for existing feedback: " + e.getMessage()); e.printStackTrace(); }
        return false;
    }

    // NEW METHOD: getFeedbackForDoctor
    public List<Feedback> getFeedbackForDoctor(int doctorId) {
        List<Feedback> feedbackList = new ArrayList<>();
        // We might also want patient name and appointment date for context
        // For now, let's get core feedback and resolve patient name in UI if needed
        String sql = "SELECT id, patient_id, doctor_id, appointment_id, comentariu, nota, data_feedback " +
                "FROM Feedbacks WHERE doctor_id = ? ORDER BY data_feedback DESC";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();

            while (rs.next()) {
                feedbackList.add(new Feedback(
                        rs.getInt("id"),
                        rs.getInt("patient_id"),
                        rs.getInt("doctor_id"),
                        rs.getInt("appointment_id"),
                        rs.getString("comentariu"),
                        rs.getInt("nota"),
                        rs.getTimestamp("data_feedback")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching feedback for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return feedbackList;
    }

    // Optional: Method to calculate average rating (could be used by Doctor or Patient when viewing Doctor profile)
    public float getAverageRatingForDoctor(int doctorId) {
        String sql = "SELECT AVG(nota) as average_rating FROM Feedbacks WHERE doctor_id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getFloat("average_rating");
            }
        } catch (SQLException e) {
            System.err.println("Error calculating average rating for doctor " + doctorId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return 0.0f; // Default if no ratings or error
    }
}