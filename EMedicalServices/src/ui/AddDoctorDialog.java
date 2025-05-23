package ui;

import dao.ClinicDAO;
import dao.DoctorDAO;
import model.Clinic;
import model.Doctor;

import javax.swing.*;
import java.awt.*;
import java.sql.Date; // For data_nastere
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.List; // For dynamic collection of clinics

public class AddDoctorDialog extends JDialog {
    private JTextField numeField;
    private JTextField prenumeField;
    private JTextField dataNastereField; // Will be parsed to Date
    private JTextField cnpField;
    private JTextField adresaDomiciliuField;
    private JTextField numarTelefonField;
    private JTextField adresaEmailField;
    private JPasswordField parolaField; // For doctor's password
    private JTextField specializareaField;
    private JComboBox<Clinic> clinicComboBox; // To select clinic

    private JButton saveButton;
    private JButton cancelButton;

    private DoctorDAO doctorDAO;
    private ClinicDAO clinicDAO;

    public AddDoctorDialog(Frame owner) {
        super(owner, "Adaugă Medic Nou", true); // Modal
        setSize(500, 550); // May need adjustment
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));

        doctorDAO = new DoctorDAO();
        clinicDAO = new ClinicDAO();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0; // Row counter

        // Nume
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Nume:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        numeField = new JTextField(20);
        formPanel.add(numeField, gbc);
        gbc.weightx = 0; y++;

        // Prenume
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Prenume:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        prenumeField = new JTextField(20);
        formPanel.add(prenumeField, gbc);
        gbc.weightx = 0; y++;

        // Data Nastere (Format YYYY-MM-DD)
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Data Naștere (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        dataNastereField = new JTextField(20);
        formPanel.add(dataNastereField, gbc);
        gbc.weightx = 0; y++;

        // CNP
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("CNP:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        cnpField = new JTextField(20);
        formPanel.add(cnpField, gbc);
        gbc.weightx = 0; y++;

        // Adresa Domiciliu
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Adresă Domiciliu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        adresaDomiciliuField = new JTextField(20);
        formPanel.add(adresaDomiciliuField, gbc);
        gbc.weightx = 0; y++;

        // Numar Telefon
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Număr Telefon:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        numarTelefonField = new JTextField(20);
        formPanel.add(numarTelefonField, gbc);
        gbc.weightx = 0; y++;

        // Adresa Email
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Adresă Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        adresaEmailField = new JTextField(20);
        formPanel.add(adresaEmailField, gbc);
        gbc.weightx = 0; y++;

        // Parola
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Parolă:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        parolaField = new JPasswordField(20);
        formPanel.add(parolaField, gbc);
        gbc.weightx = 0; y++;

        // Specializarea
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Specializarea:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        specializareaField = new JTextField(20);
        formPanel.add(specializareaField, gbc);
        gbc.weightx = 0; y++;

        // Clinica
        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Clinica:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0;
        clinicComboBox = new JComboBox<>();
        loadClinics(); // Populate the JComboBox
        formPanel.add(clinicComboBox, gbc);
        gbc.weightx = 0; y++;


        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        saveButton = new JButton("Salvează Medic");
        cancelButton = new JButton("Anulează");
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        saveButton.addActionListener(e -> saveDoctor());
        cancelButton.addActionListener(e -> dispose());
    }

    private void loadClinics() {
        List<Clinic> clinics = clinicDAO.getAllClinics();
        if (clinics.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nu există clinici înregistrate. Adăugați mai întâi o clinică.", "Atenție", JOptionPane.WARNING_MESSAGE);

        }

        for (Clinic clinic : clinics) {
            clinicComboBox.addItem(clinic);
        }
    }

    private void saveDoctor() {
        String nume = numeField.getText().trim();
        String prenume = prenumeField.getText().trim();
        String dataNastereStr = dataNastereField.getText().trim();
        String cnp = cnpField.getText().trim();
        String adresaDomiciliu = adresaDomiciliuField.getText().trim();
        String numarTelefon = numarTelefonField.getText().trim();
        String adresaEmail = adresaEmailField.getText().trim();
        String parola = new String(parolaField.getPassword());
        String specializarea = specializareaField.getText().trim();
        Clinic selectedClinic = (Clinic) clinicComboBox.getSelectedItem();


        if (nume.isEmpty() || prenume.isEmpty() || cnp.isEmpty() || parola.isEmpty() || specializarea.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nume, prenume, CNP, parolă și specializare sunt obligatorii.", "Eroare Validare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        if (selectedClinic == null) {

            JOptionPane.showMessageDialog(this, "Vă rugăm selectați o clinică. Dacă nu există clinici, adăugați una mai întâi.", "Eroare Validare", JOptionPane.ERROR_MESSAGE);
            return;
        }



        Date dataNastere = null; // java.sql.Date
        if (!dataNastereStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false); // Disallow invalid dates like 2023-02-30
                java.util.Date parsedUtilDate = sdf.parse(dataNastereStr); // Parses to java.util.Date
                dataNastere = new Date(parsedUtilDate.getTime()); // Convert java.util.Date to java.sql.Date
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Formatul datei nașterii este invalid. Folosiți YYYY-MM-DD.", "Eroare Dată", JOptionPane.ERROR_MESSAGE);
                return;
            }
        }

        Doctor newDoctor = new Doctor(0, nume, prenume, dataNastere, cnp, adresaDomiciliu,
                numarTelefon, adresaEmail, specializarea, selectedClinic.getId());

        boolean success = doctorDAO.addDoctor(newDoctor, parola);

        if (success) {
            JOptionPane.showMessageDialog(this, "Medicul a fost adăugat cu succes!", "Succes", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            JOptionPane.showMessageDialog(this, "Eroare la adăugarea medicului. Verificați consola sau unicitatea CNP/Email.", "Eroare Salvare", JOptionPane.ERROR_MESSAGE);
        }
    }
}