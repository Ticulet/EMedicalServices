package dao;

import db.DatabaseConnection;
import model.Clinic;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class ClinicDAO {

    public boolean addClinic(Clinic clinic) {
        String sql = "INSERT INTO Clinics (nume_clinica, detalii) VALUES (?, ?)";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, clinic.getNumeClinica());
            pstmt.setString(2, clinic.getDetalii());
            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;
        } catch (SQLException e) {
            System.err.println("Error adding clinic: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }

    public List<Clinic> getAllClinics() {
        List<Clinic> clinics = new ArrayList<>();
        String sql = "SELECT id, nume_clinica, detalii FROM Clinics ORDER BY nume_clinica";
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            while (rs.next()) {
                clinics.add(new Clinic(
                        rs.getInt("id"),
                        rs.getString("nume_clinica"),
                        rs.getString("detalii")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error fetching clinics: " + e.getMessage());
            e.printStackTrace();
        }
        return clinics; // Fulfills dynamic collection requirement
    }

    public Clinic getClinicById(int id) {
        String sql = "SELECT id, nume_clinica, detalii FROM Clinics WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, id);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new Clinic(
                        rs.getInt("id"),
                        rs.getString("nume_clinica"),
                        rs.getString("detalii")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching clinic by ID (" + id + "): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
    }