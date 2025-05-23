package model;

import java.sql.Timestamp;

public class Feedback {
    private int id;
    private int patientId;
    private int doctorId;
    private int appointmentId; // Link to the specific consultation for which feedback is given
    private String comentariu;
    private int nota; // Rating (e.g., 1-5)
    private Timestamp dataFeedback;


    public Feedback(int id, int patientId, int doctorId, int appointmentId, String comentariu, int nota, Timestamp dataFeedback) {
        this.id = id;
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.comentariu = comentariu;
        this.nota = nota;
        this.dataFeedback = dataFeedback;
    }


    public Feedback(int patientId, int doctorId, int appointmentId, String comentariu, int nota) {
        this.patientId = patientId;
        this.doctorId = doctorId;
        this.appointmentId = appointmentId;
        this.comentariu = comentariu;
        this.nota = nota;

    }

    public Feedback(){} // Default constructor

    // Getters and Setters
    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public int getPatientId() { return patientId; }
    public void setPatientId(int patientId) { this.patientId = patientId; }
    public int getDoctorId() { return doctorId; }
    public void setDoctorId(int doctorId) { this.doctorId = doctorId; }
    public int getAppointmentId() { return appointmentId; }
    public void setAppointmentId(int appointmentId) { this.appointmentId = appointmentId; }
    public String getComentariu() { return comentariu; }
    public void setComentariu(String comentariu) { this.comentariu = comentariu; }
    public int getNota() { return nota; }
    public void setNota(int nota) { this.nota = nota; }
    public Timestamp getDataFeedback() { return dataFeedback; }
    public void setDataFeedback(Timestamp dataFeedback) { this.dataFeedback = dataFeedback; }

    @Override
    public String toString() {
        return "Feedback{" +
                "id=" + id +
                ", patientId=" + patientId +
                ", doctorId=" + doctorId +
                ", appointmentId=" + appointmentId +
                ", nota=" + nota +
                '}';
    }
}