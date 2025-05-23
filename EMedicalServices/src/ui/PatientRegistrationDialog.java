package ui;

import dao.PatientDAO;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent; // Import for ActionEvent
import java.awt.event.ActionListener; // Import for ActionListener
import java.sql.Date; // For data_nastere (java.sql.Date)
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class PatientRegistrationDialog extends JDialog {
    private JTextField numeField;
    private JTextField prenumeField;
    private JTextField dataNastereField; // Input as YYYY-MM-DD
    private JTextField cnpField;
    private JTextField adresaDomiciliuField;
    private JTextField numarTelefonField;
    private JTextField adresaEmailField;
    private JPasswordField parolaField;
    private JPasswordField confirmParolaField;

    private JButton registerButton;
    private JButton cancelButton;

    private PatientDAO patientDAO;

    public PatientRegistrationDialog(Frame owner) {
        super(owner, "Înregistrare Cont Pacient Nou", true); // Modal dialog
        setSize(500, 480); // Adjusted size
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE); // Important for dialogs

        patientDAO = new PatientDAO();

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;

        int y = 0; // Row counter for GridBagLayout

        // Nume
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Nume:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; numeField = new JTextField(20); formPanel.add(numeField, gbc);
        gbc.weightx = 0; y++;

        // Prenume
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Prenume:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; prenumeField = new JTextField(20); formPanel.add(prenumeField, gbc);
        gbc.weightx = 0; y++;

        // Data Nastere
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Data Naștere (YYYY-MM-DD):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; dataNastereField = new JTextField(20); formPanel.add(dataNastereField, gbc);
        gbc.weightx = 0; y++;

        // CNP
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("CNP (13 cifre):"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; cnpField = new JTextField(20); formPanel.add(cnpField, gbc);
        gbc.weightx = 0; y++;

        // Adresa Domiciliu
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Adresă Domiciliu:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; adresaDomiciliuField = new JTextField(20); formPanel.add(adresaDomiciliuField, gbc);
        gbc.weightx = 0; y++;

        // Numar Telefon
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Număr Telefon:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; numarTelefonField = new JTextField(20); formPanel.add(numarTelefonField, gbc);
        gbc.weightx = 0; y++;

        // Adresa Email
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Adresă Email:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; adresaEmailField = new JTextField(20); formPanel.add(adresaEmailField, gbc);
        gbc.weightx = 0; y++;

        // Parola
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Parolă:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; parolaField = new JPasswordField(20); formPanel.add(parolaField, gbc);
        gbc.weightx = 0; y++;

        // Confirm Parola
        gbc.gridx = 0; gbc.gridy = y; formPanel.add(new JLabel("Confirmă Parola:"), gbc);
        gbc.gridx = 1; gbc.weightx = 1.0; confirmParolaField = new JPasswordField(20); formPanel.add(confirmParolaField, gbc);
        gbc.weightx = 0; y++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        registerButton = new JButton("Înregistrează Cont");
        cancelButton = new JButton("Anulează");
        buttonPanel.add(registerButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        registerButton.addActionListener(e -> performRegistration());

        // MODIFIED: Using an anonymous inner class for cancelButton's ActionListener
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose(); // Simply closes the dialog
            }
        });
    }

    private void performRegistration() {
        String nume = numeField.getText().trim();
        String prenume = prenumeField.getText().trim();
        String dataNastereStr = dataNastereField.getText().trim();
        String cnp = cnpField.getText().trim();
        String adresaDomiciliu = adresaDomiciliuField.getText().trim();
        String numarTelefon = numarTelefonField.getText().trim();
        String adresaEmail = adresaEmailField.getText().trim();
        String parola = new String(parolaField.getPassword());
        String confirmParola = new String(confirmParolaField.getPassword());

        // Basic Validations
        if (nume.isEmpty() || prenume.isEmpty() || cnp.isEmpty() || adresaEmail.isEmpty() || parola.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Nume, prenume, CNP, email și parolă sunt câmpuri obligatorii.", "Eroare Validare", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (!parola.equals(confirmParola)) {
            JOptionPane.showMessageDialog(this, "Parolele introduse nu se potrivesc.", "Eroare Parolă", JOptionPane.ERROR_MESSAGE);
            parolaField.setText("");
            confirmParolaField.setText("");
            parolaField.requestFocus();
            return;
        }
        if (cnp.length() != 13 || !cnp.matches("\\d+")) {
            JOptionPane.showMessageDialog(this, "CNP-ul trebuie să conțină exact 13 cifre.", "Eroare CNP", JOptionPane.ERROR_MESSAGE);
            cnpField.requestFocus();
            return;
        }
        if (!adresaEmail.matches("^[A-Za-z0-9+_.-]+@(.+)$")) { // Simple regex for email format
            JOptionPane.showMessageDialog(this, "Adresa de email nu are un format valid.", "Eroare Email", JOptionPane.ERROR_MESSAGE);
            adresaEmailField.requestFocus();
            return;
        }

        Date sqlDataNastere = null; // java.sql.Date
        if (!dataNastereStr.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false); // Important: do not allow invalid dates e.g. 2023-02-30
                java.util.Date parsedUtilDate = sdf.parse(dataNastereStr); // Parses to java.util.Date
                sqlDataNastere = new Date(parsedUtilDate.getTime()); // Convert to java.sql.Date
            } catch (ParseException ex) {
                JOptionPane.showMessageDialog(this, "Formatul datei nașterii este invalid. Folosiți YYYY-MM-DD.", "Eroare Dată", JOptionPane.ERROR_MESSAGE);
                dataNastereField.requestFocus();
                return;
            }
        }

        // Create Patient object; ID is 0 because it's auto-generated by the database
        Patient newPatient = new Patient(0, nume, prenume, sqlDataNastere, cnp, adresaDomiciliu, numarTelefon, adresaEmail);

        // The password (String parola) is passed as a separate argument to the DAO method
        // TODO: Wrap this DAO call in a SwingWorker for better responsiveness
        boolean success = patientDAO.createPatient(newPatient, parola);

        if (success) {
            JOptionPane.showMessageDialog(this,
                    "Contul de pacient a fost creat cu succes!\nAcum vă puteți autentifica folosind CNP-ul și parola.",
                    "Înregistrare Reușită",
                    JOptionPane.INFORMATION_MESSAGE);
            dispose(); // Close the registration dialog
        } else {
            JOptionPane.showMessageDialog(this,
                    "Eroare la crearea contului.\nEste posibil ca CNP-ul sau adresa de email să fie deja înregistrate.\nVerificați consola pentru detalii.",
                    "Eroare Înregistrare",
                    JOptionPane.ERROR_MESSAGE);
        }
    }
}