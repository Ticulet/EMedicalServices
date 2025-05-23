package dao;

import db.DatabaseConnection;
import model.Doctor;
// No need to import Clinic model/DAO here unless you directly join tables in these queries,
// which we are not doing in this version for simplicity.

import java.sql.*; // Includes Date, Types, PreparedStatement, ResultSet, etc.
import java.util.ArrayList;
import java.util.List;

public class DoctorDAO {

    // Existing method: authenticate
    public Doctor authenticate(String cnp, String password) {
        String sql = "SELECT id, nume, prenume, data_nastere, cnp, adresa_domiciliu, " +
                "numar_telefon, adresa_email, specializarea, clinic_id " +
                "FROM Doctors WHERE cnp = ? AND parola = ?"; // Assuming 'password' is the column name for doctor's password
        // If it's 'parola' like for Patients, update here.
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, cnp);
            pstmt.setString(2, password);

            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Doctor(
                        rs.getInt("id"),
                        rs.getString("nume"),
                        rs.getString("prenume"),
                        rs.getDate("data_nastere"),
                        rs.getString("cnp"),
                        rs.getString("adresa_domiciliu"),
                        rs.getString("numar_telefon"),
                        rs.getString("adresa_email"),
                        rs.getString("specializarea"),
                        rs.getInt("clinic_id")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error during Doctor authentication: " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }

    // Existing method: addDoctor (used by Admin)
    public boolean addDoctor(Doctor doctor, String plainPassword) {
        String sql = "INSERT INTO Doctors (nume, prenume, data_nastere, cnp, adresa_domiciliu, " +
                "numar_telefon, adresa_email, parola, specializarea, clinic_id) " +
                "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?)"; // Assuming 'parola' is the password column name
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setString(1, doctor.getNume());
            pstmt.setString(2, doctor.getPrenume());

            if (doctor.getDataNastere() != null) {
                pstmt.setDate(3, doctor.getDataNastere());
            } else {
                pstmt.setNull(3, Types.DATE);
            }

            pstmt.setString(4, doctor.getCnp());
            pstmt.setString(5, doctor.getAdresaDomiciliu());
            pstmt.setString(6, doctor.getNumarTelefon());
            pstmt.setString(7, doctor.getAdresaEmail());
            pstmt.setString(8, plainPassword); // Storing plain password into 'parola' column
            pstmt.setString(9, doctor.getSpecializarea());
            pstmt.setInt(10, doctor.getClinicId());

            int affectedRows = pstmt.executeUpdate();
            return affectedRows > 0;

        } catch (SQLException e) {
            System.err.println("Error adding doctor: " + e.getMessage());
            if (e.getErrorCode() == 1062 || e.getMessage().toLowerCase().contains("duplicate entry")) {
                System.err.println("Database constraint violation: Possible duplicate CNP or Email for doctor.");
            } else {
                e.printStackTrace();
            }
            return false;
        }
    }

    // NEW METHOD: searchDoctors (used by Patient)
    public List<Doctor> searchDoctors(String nameQuery, String specialization) {
        List<Doctor> doctors = new ArrayList<>();
        StringBuilder sqlBuilder = new StringBuilder(
                "SELECT id, nume, prenume, data_nastere, cnp, adresa_domiciliu, " +
                        "numar_telefon, adresa_email, specializarea, clinic_id FROM Doctors WHERE 1=1");

        List<Object> params = new ArrayList<>(); // To hold parameters for PreparedStatement

        if (nameQuery != null && !nameQuery.trim().isEmpty()) {
            sqlBuilder.append(" AND (LOWER(nume) LIKE LOWER(?) OR LOWER(prenume) LIKE LOWER(?))");
            params.add("%" + nameQuery.trim() + "%");
            params.add("%" + nameQuery.trim() + "%");
        }
        if (specialization != null && !specialization.trim().isEmpty() && !"Toate Specializările".equalsIgnoreCase(specialization.trim())) {
            sqlBuilder.append(" AND LOWER(specializarea) = LOWER(?)");
            params.add(specialization.trim());
        }
        sqlBuilder.append(" ORDER BY nume, prenume");

        System.out.println("Executing Doctor Search SQL: " + sqlBuilder.toString()); // For debugging
        System.out.println("Parameters: " + params); // For debugging

        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sqlBuilder.toString())) {

            int paramIndex = 1;
            for (Object param : params) {
                pstmt.setObject(paramIndex++, param);
            }

            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                doctors.add(new Doctor(
                        rs.getInt("id"),
                        rs.getString("nume"),
                        rs.getString("prenume"),
                        rs.getDate("data_nastere"), // Can be null
                        rs.getString("cnp"),
                        rs.getString("adresa_domiciliu"), // Can be null
                        rs.getString("numar_telefon"),    // Can be null
                        rs.getString("adresa_email"),     // Can be null
                        rs.getString("specializarea"),
                        rs.getInt("clinic_id")
                ));
            }
        } catch (SQLException e) {
            System.err.println("Error searching doctors: " + e.getMessage());
            e.printStackTrace();
        }
        return doctors;
    }

    // NEW or UPDATED METHOD: getDoctorById (used by Patient when selecting from search)
    public Doctor getDoctorById(int doctorId) {
        String sql = "SELECT id, nume, prenume, data_nastere, cnp, adresa_domiciliu, " +
                "numar_telefon, adresa_email, specializarea, clinic_id " +
                "FROM Doctors WHERE id = ?";
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {

            pstmt.setInt(1, doctorId);
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                return new Doctor(
                        rs.getInt("id"),
                        rs.getString("nume"),
                        rs.getString("prenume"),
                        rs.getDate("data_nastere"),
                        rs.getString("cnp"),
                        rs.getString("adresa_domiciliu"),
                        rs.getString("numar_telefon"),
                        rs.getString("adresa_email"),
                        rs.getString("specializarea"),
                        rs.getInt("clinic_id")
                );
            }
        } catch (SQLException e) {
            System.err.println("Error fetching doctor by ID ("+ doctorId +"): " + e.getMessage());
            e.printStackTrace();
        }
        return null;
    }
}