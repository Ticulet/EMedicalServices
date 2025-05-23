package model;

import java.sql.Timestamp; // Or java.sql.Date if only date is needed for consultation

public class ConsultationRecord {
    private int id;
    private int appointmentId; // Links to the Appointment table
    private String diagnostic;
    private String prescriptie; // Prescription details
    private Timestamp dataConsultatie; // When the consultation actually occurred or was recorded

    // Constructor for fetching from DB
    public ConsultationRecord(int id, int appointmentId, String diagnostic, String prescriptie, Timestamp dataConsultatie) {
        this.id = id;
        this.appointmentId = appointmentId;
        this.diagnostic = diagnostic;
        this.prescriptie = prescriptie;
        this.dataConsultatie = dataConsultatie;
    }

    public ConsultationRecord(int appointmentId, String diagnostic, String prescriptie, Timestamp dataConsultatie) {
        this.appointmentId = appointmentId;
        this.diagnostic = diagnostic;
        this.prescriptie = prescriptie;
        this.dataConsultatie = dataConsultatie;
    }

    public ConsultationRecord(){} // Default constructor


    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getDiagnostic() { return diagnostic; }
    public void setDiagnostic(String diagnostic) { this.diagnostic = diagnostic; }
    public String getPrescriptie() { return prescriptie; }
    public void setPrescriptie(String prescriptie) { this.prescriptie = prescriptie; }
    public Timestamp getDataConsultatie() { return dataConsultatie; }
    public void setDataConsultatie(Timestamp dataConsultatie) { this.dataConsultatie = dataConsultatie; }

    @Override
    public String toString() {
        return "ConsultationRecord{" +
                "id=" + id +
                ", appointmentId=" + appointmentId +
                ", diagnostic='" + diagnostic + '\'' +
                // Potentially long prescription, might not want it fully in toString
                '}';
    }
}