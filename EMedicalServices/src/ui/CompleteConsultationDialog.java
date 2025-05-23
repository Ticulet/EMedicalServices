package ui;

import model.Appointment;
import model.ConsultationRecord;
import model.Doctor;
import model.Patient;
import dao.ConsultationRecordDAO;
import dao.AppointmentDAO;
import dao.PatientDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

public class CompleteConsultationDialog extends JDialog {
    private Appointment appointmentToComplete;


    private ConsultationRecordDAO consultationRecordDAO;
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;

    // UI Components
    private JLabel patientNameLabel;
    private JLabel appointmentDateTimeLabel;
    private JTextArea diagnosticTextArea;
    private JTextArea prescriptionTextArea;
    private JButton saveConsultationButton;
    private JButton cancelButton;

    public CompleteConsultationDialog(Frame owner, Appointment appointment, Doctor doctor /*, PatientDAO patientDAO (can pass if preferred over new instance)*/) {
        super(owner, "Finalizează Consultație", true); // Modal
        this.appointmentToComplete = appointment;
        // this.currentDoctor = doctor; // Store if needed

        this.consultationRecordDAO = new ConsultationRecordDAO();
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO(); // Or use a passed instance

        setSize(500, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel infoPanel = new JPanel(new GridBagLayout());
        infoPanel.setBorder(BorderFactory.createTitledBorder("Detalii Programare"));
        GridBagConstraints gbcInfo = new GridBagConstraints();
        gbcInfo.insets = new Insets(5, 5, 5, 5);
        gbcInfo.anchor = GridBagConstraints.WEST;

        gbcInfo.gridx = 0;
        gbcInfo.gridy = 0;
        infoPanel.add(new JLabel("Pacient:"), gbcInfo);
        gbcInfo.gridx = 1;
        gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        gbcInfo.weightx = 1.0;
        patientNameLabel = new JLabel();
        infoPanel.add(patientNameLabel, gbcInfo);
        gbcInfo.fill = GridBagConstraints.NONE;
        gbcInfo.weightx = 0.0;


        gbcInfo.gridx = 0;
        gbcInfo.gridy = 1;
        infoPanel.add(new JLabel("Data Programării:"), gbcInfo);
        gbcInfo.gridx = 1;
        gbcInfo.fill = GridBagConstraints.HORIZONTAL;
        gbcInfo.weightx = 1.0;
        appointmentDateTimeLabel = new JLabel();
        infoPanel.add(appointmentDateTimeLabel, gbcInfo);
        gbcInfo.fill = GridBagConstraints.NONE;
        gbcInfo.weightx = 0.0;

        add(infoPanel, BorderLayout.NORTH);


        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10)); // Padding adjusted
        GridBagConstraints gbcForm = new GridBagConstraints();
        gbcForm.insets = new Insets(5, 5, 5, 5);
        gbcForm.anchor = GridBagConstraints.WEST;

        int y = 0;

        // Diagnostic
        gbcForm.gridx = 0;
        gbcForm.gridy = y;
        gbcForm.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Diagnostic:"), gbcForm);
        gbcForm.gridx = 1;
        gbcForm.gridy = y;
        gbcForm.fill = GridBagConstraints.BOTH;
        gbcForm.weightx = 1.0;
        gbcForm.weighty = 1.0;
        diagnosticTextArea = new JTextArea(5, 30);
        diagnosticTextArea.setLineWrap(true);
        diagnosticTextArea.setWrapStyleWord(true);
        JScrollPane diagnosticScrollPane = new JScrollPane(diagnosticTextArea);
        formPanel.add(diagnosticScrollPane, gbcForm);
        y++;

        // Prescription
        gbcForm.gridx = 0;
        gbcForm.gridy = y;
        gbcForm.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Rețetă/Indicații:"), gbcForm);
        gbcForm.gridx = 1;
        gbcForm.gridy = y;
        gbcForm.fill = GridBagConstraints.BOTH;
        gbcForm.weightx = 1.0;
        gbcForm.weighty = 1.0;
        prescriptionTextArea = new JTextArea(5, 30);
        prescriptionTextArea.setLineWrap(true);
        prescriptionTextArea.setWrapStyleWord(true);
        JScrollPane prescriptionScrollPane = new JScrollPane(prescriptionTextArea);
        formPanel.add(prescriptionScrollPane, gbcForm);

        add(formPanel, BorderLayout.CENTER);


        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveConsultationButton = new JButton("Salvează și Finalizează");
        cancelButton = new JButton("Anulează");
        buttonPanel.add(saveConsultationButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        populateAppointmentInfo();

        saveConsultationButton.addActionListener(e -> saveConsultation());
        cancelButton.addActionListener(e -> dispose());
    }

    private void populateAppointmentInfo() {
        Patient patient = patientDAO.getPatientById(appointmentToComplete.getPatientId());
        if (patient != null) {
            patientNameLabel.setText(patient.getPrenume() + " " + patient.getNume() + " (CNP: " + patient.getCnp() + ")");
        } else {
            patientNameLabel.setText("Pacient Necunoscut (ID: " + appointmentToComplete.getPatientId() + ")");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
        appointmentDateTimeLabel.setText(sdf.format(appointmentToComplete.getDataProgramare()));
    }

    private void saveConsultation() {
        String diagnostic = diagnosticTextArea.getText().trim();
        String prescription = prescriptionTextArea.getText().trim();

        if (diagnostic.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Diagnosticul este obligatoriu.", "Eroare Validare", JOptionPane.ERROR_MESSAGE);
            diagnosticTextArea.requestFocus();
            return;
        }

        Timestamp consultationTimestamp = appointmentToComplete.getDataProgramare();

        ConsultationRecord record = new ConsultationRecord(
                appointmentToComplete.getId(),
                diagnostic,
                prescription,
                consultationTimestamp // Use the appointment's scheduled date/time
        );

        boolean recordAdded = consultationRecordDAO.addConsultationRecord(record);

        if (recordAdded) {
            boolean statusUpdated = appointmentDAO.updateAppointmentStatus(appointmentToComplete.getId(), "COMPLETED");
            if (statusUpdated) {
                JOptionPane.showMessageDialog(this, "Consultația a fost finalizată și salvată cu succes.", "Succes", JOptionPane.INFORMATION_MESSAGE);
                dispose();
            } else {
                JOptionPane.showMessageDialog(this, "Înregistrarea consultației a fost salvată, dar a apărut o eroare la actualizarea statusului programării. Contactați administratorul.", "Atenționare", JOptionPane.WARNING_MESSAGE);
                dispose();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Eroare la salvarea detaliilor consultației. Verificați consola.", "Eroare Salvare", JOptionPane.ERROR_MESSAGE);
        }
    }
}