package ui;

import model.Patient;
import model.Doctor;
import model.Appointment;
import dao.AppointmentDAO;

import javax.swing.*;
import java.awt.*;
import java.sql.Timestamp; // For dataProgramare
import java.text.ParseException;
import java.text.SimpleDateFormat; // For parsing date and time

public class AppointmentBookingDialog extends JDialog {
    private Patient patient;
    private Doctor doctor;
    private AppointmentDAO appointmentDAO;

    // UI Components
    private JTextField dateField; // For YYYY-MM-DD
    private JTextField timeField; // For HH:MM (24-hour format)
    private JTextArea descriptionArea;
    private JButton bookButton;
    private JButton cancelButton;

    public AppointmentBookingDialog(Dialog owner, Patient patient, Doctor doctor) {
        super(owner, "Programare Consultație cu Dr. " + doctor.getPrenume() + " " + doctor.getNume(), true);
        this.patient = patient;
        this.doctor = doctor;
        this.appointmentDAO = new AppointmentDAO();

        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        // Date
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Data (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        dateField = new JTextField(10);
        formPanel.add(dateField, gbc);
        y++;

        // Time
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Ora (HH:MM, format 24h):"), gbc);
        gbc.gridx = 1;
        timeField = new JTextField(5);
        formPanel.add(timeField, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; // Reset
        y++;

        // Description
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Descriere Afecțiune:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        descriptionArea = new JTextArea(5, 20);
        descriptionArea.setLineWrap(true);
        descriptionArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(descriptionArea);
        formPanel.add(scrollPane, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0; // Reset
        gbc.anchor = GridBagConstraints.WEST; // Reset
        y++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        bookButton = new JButton("Finalizează Programarea");
        cancelButton = new JButton("Anulează");
        buttonPanel.add(bookButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        bookButton.addActionListener(e -> performBooking());
        cancelButton.addActionListener(e -> dispose());
    }

    private void performBooking() {
        String dateStr = dateField.getText().trim();
        String timeStr = timeField.getText().trim();
        String description = descriptionArea.getText().trim();

        if (dateStr.isEmpty() || timeStr.isEmpty() || description.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Data, ora și descrierea afecțiunii sunt obligatorii.", "Eroare Validare", JOptionPane.ERROR_MESSAGE);
            return;
        }


        Timestamp appointmentTimestamp;
        try {
            SimpleDateFormat dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");
            dateTimeFormat.setLenient(false);
            java.util.Date parsedDateTime = dateTimeFormat.parse(dateStr + " " + timeStr);

            if (parsedDateTime.before(new java.util.Date())) {
                JOptionPane.showMessageDialog(this, "Data și ora programării nu pot fi în trecut.", "Eroare Dată/Oră", JOptionPane.ERROR_MESSAGE);
                return;
            }
            appointmentTimestamp = new Timestamp(parsedDateTime.getTime());

        } catch (ParseException ex) {
            JOptionPane.showMessageDialog(this, "Formatul datei (YYYY-MM-DD) sau orei (HH:MM) este invalid.", "Eroare Format", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Appointment newAppointment = new Appointment(
                patient.getId(),
                doctor.getId(),
                appointmentTimestamp,
                description
        );


        boolean success = appointmentDAO.createAppointment(newAppointment);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Programarea a fost realizată cu succes!\nVeți putea vedea detaliile în istoricul programărilor.",
                    "Programare Reușită",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Eroare la realizarea programării. Verificați consola.", "Eroare Programare", JOptionPane.ERROR_MESSAGE);
        }
    }
}