package ui;

import model.Appointment;
import model.Doctor;
import dao.AppointmentDAO;
import dao.DoctorDAO;

import javax.swing.*;
import java.awt.*; // Ensure java.awt.Frame can be resolved (though Dialog also in java.awt)
import java.text.SimpleDateFormat;

public class ModifyAppointmentDialog extends JDialog {
    private Appointment appointmentToModify;
    private AppointmentDAO appointmentDAO;
    private DoctorDAO doctorDAO;

    private JLabel doctorNameLabel;
    private JLabel appointmentDateLabel;
    private JTextArea descriptionArea;
    private JButton saveButton;
    private JButton cancelButton;

    // MODIFIED CONSTRUCTOR to accept Frame as owner
    public ModifyAppointmentDialog(Frame owner, Appointment appointment, DoctorDAO doctorDAO) {
        super(owner, "Modifică Detalii Programare", true); // JFrame (PatientDashboardFrame) is a Frame
        this.appointmentToModify = appointment;
        this.appointmentDAO = new AppointmentDAO();
        this.doctorDAO = doctorDAO; // Use passed DAO

        setSize(450, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ... rest of the constructor code for UI components remains the same ...
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Medic:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        doctorNameLabel = new JLabel();
        formPanel.add(doctorNameLabel, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Data Programare:"), gbc);
        gbc.gridx = 1;
        appointmentDateLabel = new JLabel();
        formPanel.add(appointmentDateLabel, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Descriere Afecțiune:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        y++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Salvează Modificările");
        cancelButton = new JButton("Anulează");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        populateForm();

        saveButton.addActionListener(e -> saveChanges());
        cancelButton.addActionListener(e -> dispose());
    }

    // ... (populateForm and saveChanges methods remain the same) ...
    private void populateForm() {
        Doctor doctor = doctorDAO.getDoctorById(appointmentToModify.getDoctorId());
        if (doctor != null) {
            doctorNameLabel.setText("Dr. " + doctor.getPrenume() + " " + doctor.getNume());
        } else {
            doctorNameLabel.setText("Medic Necunoscut");
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
        appointmentDateLabel.setText(sdf.format(appointmentToModify.getDataProgramare()));
        descriptionArea.setText(appointmentToModify.getDescriereAfectiune());
    }

    private void saveChanges() {
        String newDescription = descriptionArea.getText().trim();
        if (newDescription.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Descrierea afecțiunii nu poate fi goală.", "Eroare Validare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        boolean success = appointmentDAO.updateAppointmentDescription(appointmentToModify.getId(), newDescription);

        if (success) {
            JOptionPane.showMessageDialog(this, "Descrierea programării a fost actualizată.", "Succes", JOptionPane.INFORMATION_MESSAGE);
            appointmentToModify.setDescriereAfectiune(newDescription);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Eroare la actualizarea programării. Este posibil ca programarea să nu mai fie activă.", "Eroare Salvare", JOptionPane.ERROR_MESSAGE);
        }
    }
}