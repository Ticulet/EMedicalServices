package ui;

import model.Doctor;
import model.Clinic; // To display clinic name
import dao.ClinicDAO; // To fetch clinic details

import javax.swing.*;
import java.awt.*;


public class DoctorProfileDialog extends JDialog {
    private Doctor doctor;
    private ClinicDAO clinicDAO; // To get clinic name

    private JLabel nameLabelValue;
    private JLabel specializationLabelValue;
    private JLabel clinicLabelValue;
    private JLabel emailLabelValue;
    private JLabel phoneLabelValue;

    private JLabel ratingLabelValue;
    private JTextArea availabilityTextArea;

    public DoctorProfileDialog(Dialog owner, Doctor doctor) { // Can be owned by another Dialog
        super(owner, "Profil Medic: " + doctor.getPrenume() + " " + doctor.getNume(), true); // Modal
        this.doctor = doctor;
        this.clinicDAO = new ClinicDAO();

        setSize(500, 450); // Adjust as needed
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0; // Row counter

        // Doctor Name
        gbc.gridx = 0; gbc.gridy = y; detailsPanel.add(new JLabel("Nume Medic:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        nameLabelValue = new JLabel();
        detailsPanel.add(nameLabelValue, gbc);
        y++;

        // Specialization
        gbc.gridx = 0; gbc.gridy = y; detailsPanel.add(new JLabel("Specializare:"), gbc);
        gbc.gridx = 1;
        specializationLabelValue = new JLabel();
        detailsPanel.add(specializationLabelValue, gbc);
        y++;

        // Clinic
        gbc.gridx = 0; gbc.gridy = y; detailsPanel.add(new JLabel("Clinică:"), gbc);
        gbc.gridx = 1;
        clinicLabelValue = new JLabel();
        detailsPanel.add(clinicLabelValue, gbc);
        y++;

        // Email (Optional display)
        gbc.gridx = 0; gbc.gridy = y; detailsPanel.add(new JLabel("Email:"), gbc);
        gbc.gridx = 1;
        emailLabelValue = new JLabel();
        detailsPanel.add(emailLabelValue, gbc);
        y++;

        // Phone (Optional display)
        gbc.gridx = 0; gbc.gridy = y; detailsPanel.add(new JLabel("Telefon:"), gbc);
        gbc.gridx = 1;
        phoneLabelValue = new JLabel();
        detailsPanel.add(phoneLabelValue, gbc);
        y++;

        // Rating (Placeholder)
        gbc.gridx = 0; gbc.gridy = y; detailsPanel.add(new JLabel("Rating Servicii:"), gbc);
        gbc.gridx = 1;
        ratingLabelValue = new JLabel();
        detailsPanel.add(ratingLabelValue, gbc);
        y++;

        // Availability (Placeholder)
        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.NORTHWEST;
        detailsPanel.add(new JLabel("Zile Disponibile Programări:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weighty = 1.0; // Allow text area to expand
        availabilityTextArea = new JTextArea(5, 20);
        availabilityTextArea.setEditable(false);
        availabilityTextArea.setFont(UIManager.getFont("Label.font")); // Match label font
        availabilityTextArea.setOpaque(false); // Make it look like a multi-line label
        availabilityTextArea.setLineWrap(true);
        availabilityTextArea.setWrapStyleWord(true);
        JScrollPane availabilityScrollPane = new JScrollPane(availabilityTextArea);
        availabilityScrollPane.setBorder(BorderFactory.createEmptyBorder()); // Remove scroll pane border if desired
        detailsPanel.add(availabilityScrollPane, gbc);
        gbc.weighty = 0; // Reset weighty
        gbc.fill = GridBagConstraints.HORIZONTAL; // Reset fill
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor
        y++;


        add(detailsPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Închide");
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        closeButton.addActionListener(e -> dispose());

        populateDoctorDetails();
    }

    private void populateDoctorDetails() {
        nameLabelValue.setText(doctor.getPrenume() + " " + doctor.getNume());
        specializationLabelValue.setText(doctor.getSpecializarea() != null ? doctor.getSpecializarea() : "N/A");

        Clinic clinic = clinicDAO.getClinicById(doctor.getClinicId());
        clinicLabelValue.setText(clinic != null ? clinic.getNumeClinica() : "N/A (ID: " + doctor.getClinicId() + ")");

        emailLabelValue.setText(doctor.getAdresaEmail() != null && !doctor.getAdresaEmail().isEmpty() ? doctor.getAdresaEmail() : "N/A");
        phoneLabelValue.setText(doctor.getNumarTelefon() != null && !doctor.getNumarTelefon().isEmpty() ? doctor.getNumarTelefon() : "N/A");

        ratingLabelValue.setText("4.5 / 5 (Exemplu)");


        availabilityTextArea.setText(
                "Luni: 09:00 - 12:00 (Exemplu)\n" +
                        "Miercuri: 14:00 - 17:00 (Exemplu)\n" +
                        "Vineri: 09:00 - 11:00 (Exemplu)\n\n" +
                        "(Acesta este un calendar exemplu. Funcționalitatea reală va fi implementată.)"
        );
    }

}