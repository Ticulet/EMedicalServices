package ui;

import dao.ClinicDAO; // We'll create this DAO next
import model.Clinic;   // We'll create this model next

import javax.swing.*;
import java.awt.*;

public class AddClinicDialog extends JDialog {
    private JTextField clinicNameField;
    private JTextArea clinicDetailsArea; // For "detalii despre ea"
    private JButton saveButton;
    private JButton cancelButton;

    private ClinicDAO clinicDAO;

    public AddClinicDialog(Frame owner) { // owner is the AdminDashboardFrame
        super(owner, "Adaugă Clinică Nouă", true); // true for modal
        setSize(400, 300);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        clinicDAO = new ClinicDAO(); // Initialize DAO

        JPanel formPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // Clinic Name
        gbc.gridx = 0; gbc.gridy = 0;
        formPanel.add(new JLabel("Nume Clinică:"), gbc);
        gbc.gridx = 1; gbc.gridy = 0;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.weightx = 1.0;
        clinicNameField = new JTextField(20);
        formPanel.add(clinicNameField, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;

        // Clinic Details
        gbc.gridx = 0; gbc.gridy = 1;
        gbc.anchor = GridBagConstraints.NORTHWEST; // Align label to top-left of text area
        formPanel.add(new JLabel("Detalii Clinică:"), gbc);
        gbc.gridx = 1; gbc.gridy = 1;
        gbc.fill = GridBagConstraints.BOTH; // Text area should expand
        gbc.weightx = 1.0;
        gbc.weighty = 1.0;
        clinicDetailsArea = new JTextArea(5, 20);
        clinicDetailsArea.setLineWrap(true);
        clinicDetailsArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(clinicDetailsArea);
        formPanel.add(scrollPane, gbc);
        gbc.fill = GridBagConstraints.NONE;
        gbc.weightx = 0;
        gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST; // Reset anchor


        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Salvează");
        cancelButton = new JButton("Anulează");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveClinic());
        cancelButton.addActionListener(e -> dispose()); // Simply close the dialog
    }

    private void saveClinic() {
        String name = clinicNameField.getText().trim();
        String details = clinicDetailsArea.getText().trim();

        if (name.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Numele clinicii este obligatoriu.", "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Clinic newClinic = new Clinic(0, name, details); // ID is 0 as it's auto-generated by DB

        boolean success = clinicDAO.addClinic(newClinic);

        if (success) {
            JOptionPane.showMessageDialog(this, "Clinica a fost adăugată cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close dialog on success
        } else {
            JOptionPane.showMessageDialog(this, "Eroare la adăugarea clinicii. Verificați consola.", "Eroare Salvare", JOptionPane.ERROR_MESSAGE);
        }
    }
}