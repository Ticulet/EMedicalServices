package model;

import java.sql.Timestamp; // Using Timestamp for DATETIME precision (date and time)

public class Appointment {
    private int id;
    private int patientId;
    private int doctorId;
    private Timestamp dataProgramare; // Stores both date and time
    private String descriereAfectiune;
    private String status; // e.g., "SCHEDULED", "COMPLETED", "CANCELLED"

    // Full constructor for fetching from DB
    public Appointment(int id, int patientId, int doctorId, Timestamp dataProgramare, String descriereAfectiune, String status) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dataProgramare = dataProgramare;
        this.descriereAfectiune = descriereAfectiune;
        this.status = status;
    }

    // Constructor for creating a new appointment (ID is auto-generated, status defaults to SCHEDULED)
    public Appointment(int patientId, int doctorId, Timestamp dataProgramare, String descriereAfectiune) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.dataProgramare = dataProgramare;
        this.descriereAfectiune = descriereAfectiune;
        this.status = "SCHEDULED"; // Default status for new appointments
    }

    public Appointment() { // Default constructor
    }

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public Timestamp getDataProgramare() { return dataProgramare; }
    public void setDataProgramare(Timestamp dataProgramare) { this.dataProgramare = dataProgramare; }
    public String getDescriereAfectiune() { return descriereAfectiune; }
    public void setDescriereAfectiune(String descriereAfectiune) { this.descriereAfectiune = descriereAfectiune; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    @Override
    public String toString() {
        return "Appointment{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", dataProgramare=" + dataProgramare +
                ", status='" + status + '\'' +
                '}';
    }
}