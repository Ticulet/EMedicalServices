package dao;

import db.DatabaseConnection;
import model.Patient;

import java.sql.*;

public class PatientDAO {

    public Patient authenticate(String cnp, String plainPassword) {
        String sql = "SELECT id, nume, prenume, data_nastere, cnp, adresa_domiciliu, numar_telefon, adresa_email FROM Patients WHERE cnp = ? AND parola = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cnp);
            pstmt.setString(2, plainPassword);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Patient(
                        rs.getInt("id"),
                        rs.getString("nume"),
                        rs.getString("prenume"),
                        rs.getDate("data_nastere"),
                        rs.getString("cnp"),
                        rs.getString("adresa_domiciliu"),
                        rs.getString("numar_telefon"),
                        rs.getString("adresa_email")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error during Patient authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    public boolean createPatient(Patient patient, String plainPassword) {
        String sql = "INSERT INTO Patients (nume, prenume, data_nastere, cnp, adresa_domiciliu, " +
                "numar_telefon, adresa_email, parola) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, patient.getNume());
            pstmt.setString(2, patient.getPrenume());
            if (patient.getDataNastere() != null) {
                pstmt.setDate(3, patient.getDataNastere());
            } else {
                pstmt.setNull(3, Types.DATE);
            }
            pstmt.setString(4, patient.getCnp());
            pstmt.setString(5, patient.getAdresaDomiciliu());
            pstmt.setString(6, patient.getNumarTelefon());
            pstmt.setString(7, patient.getAdresaEmail());
            pstmt.setString(8, plainPassword);

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error creating patient account: " + e.getMessage());
            if (e.getErrorCode() == 1062 || e.getMessage().toLowerCase().contains("duplicate entry")) {
                System.err.println("Database constraint violation: Possible duplicate CNP or Email for patient.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    // NEW METHOD: getPatientById
    public Patient getPatientById(int patientId) {
        String sql = "SELECT id, nume, prenume, data_nastere, cnp, adresa_domiciliu, " +
                "numar_telefon, adresa_email FROM Patients WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, patientId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Patient(
                        rs.getInt("id"),
                        rs.getString("nume"),
                        rs.getString("prenume"),
                        rs.getDate("data_nastere"),
                        rs.getString("cnp"),
                        rs.getString("adresa_domiciliu"),
                        rs.getString("numar_telefon"),
                        rs.getString("adresa_email")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching patient by ID " + patientId + ": " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}