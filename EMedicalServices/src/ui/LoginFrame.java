package ui;

// Imports for DAOs and Models
import dao.AdminDAO;
import dao.DoctorDAO;
import dao.PatientDAO;
import model.Admin;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent; // For inner class
import java.awt.event.ActionListener; // For inner class
import java.util.concurrent.ExecutionException; // For SwingWorker get()

public class LoginFrame extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> userTypeComboBox;
    private JButton loginButton;
    private JButton registerButton;
    private JLabel statusLabel; // For "Logging in..." message

    public LoginFrame() {
        setTitle("Login - E-Servicii Medicale");
        setSize(400, 280); // Slightly increased height for status label
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setResizable(false);

        JPanel mainPanel = new JPanel(new BorderLayout(10, 10));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel inputPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.anchor = GridBagConstraints.WEST;

        // User Type
        gbc.gridx = 0; gbc.gridy = 0;
        inputPanel.add(new JLabel("Tip Utilizator:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        String[] userTypes = {"Pacient", "Medic", "Administrator"};
        userTypeComboBox = new JComboBox<>(userTypes);
        inputPanel.add(userTypeComboBox, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; // Reset

        // Username/CNP
        gbc.gridx = 0; gbc.gridy = 1;
        inputPanel.add(new JLabel("Username/CNP:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        usernameField = new JTextField(15);
        inputPanel.add(usernameField, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; // Reset

        // Password
        gbc.gridx = 0; gbc.gridy = 2;
        inputPanel.add(new JLabel("Parola:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        passwordField = new JPasswordField(15);
        inputPanel.add(passwordField, gbc);
        gbc.weightx = 0; gbc.fill = GridBagConstraints.NONE; // Reset

        // Status Label
        gbc.gridx = 0; gbc.gridy = 3; gbc.gridwidth = 2; gbc.anchor = GridBagConstraints.CENTER;
        statusLabel = new JLabel(" "); // Initial empty space
        inputPanel.add(statusLabel, gbc);
        gbc.gridwidth = 1; gbc.anchor = GridBagConstraints.WEST; // Reset

        mainPanel.add(inputPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        loginButton = new JButton("Login");
        registerButton = new JButton("Creare Cont Pacient");
        buttonPanel.add(loginButton);
        buttonPanel.add(registerButton);
        mainPanel.add(buttonPanel, BorderLayout.SOUTH);

        add(mainPanel);

        // Using lambdas for these ActionListeners (meets one part of requirement #7)
        loginButton.addActionListener(e -> attemptLogin());
        passwordField.addActionListener(e -> attemptLogin()); // Enter in password field triggers login

        registerButton.addActionListener(e -> openPatientRegistrationDialog());
    }

    // Wrapper method to initiate the SwingWorker
    private void attemptLogin() {
        String username = usernameField.getText().trim();
        String password = new String(passwordField.getPassword());
        String selectedUserType = (String) userTypeComboBox.getSelectedItem();

        if (username.isEmpty() || password.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Username/CNP și parola nu pot fi goale.", "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        statusLabel.setText("Autentificare în curs...");
        loginButton.setEnabled(false);
        registerButton.setEnabled(false);
        usernameField.setEnabled(false);
        passwordField.setEnabled(false);
        userTypeComboBox.setEnabled(false);

        // Create and execute the SwingWorker
        LoginWorker loginWorker = new LoginWorker(username, password, selectedUserType);
        loginWorker.execute();
    }


    // Inner class for SwingWorker to handle login in background
    private class LoginWorker extends SwingWorker<Object, Void> {
        private String username;
        private String password;
        private String userType;
        private String actualUserRoleForMessage; // To store the role for the success message

        public LoginWorker(String username, String password, String userType) {
            this.username = username;
            this.password = password;
            this.userType = userType;
        }

        @Override
        protected Object doInBackground() throws Exception {
            // This runs on a background thread
            Object user = null;
            actualUserRoleForMessage = userType; // Default to selected type

            if ("Pacient".equals(userType)) {
                PatientDAO patientDAO = new PatientDAO();
                user = patientDAO.authenticate(username, password);
            } else if ("Medic".equals(userType)) {
                DoctorDAO doctorDAO = new DoctorDAO();
                user = doctorDAO.authenticate(username, password);
            } else if ("Administrator".equals(userType)) {
                AdminDAO adminDAO = new AdminDAO();
                user = adminDAO.authenticate(username, password);
            }
            return user; // Return the authenticated user object (Patient, Doctor, or Admin) or null
        }

        @Override
        protected void done() {
            // This runs on the EDT after doInBackground completes
            try {
                Object loggedInUser = get(); // Retrieve the result from doInBackground()

                if (loggedInUser != null) {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Login reușit ca " + actualUserRoleForMessage + "!", "Succes", JOptionPane.INFORMATION_MESSAGE);
                    LoginFrame.this.dispose(); // Close the login window

                    if (loggedInUser instanceof Patient) {
                        new PatientDashboardFrame((Patient) loggedInUser).setVisible(true);
                    } else if (loggedInUser instanceof Doctor) {
                        new DoctorDashboardFrame((Doctor) loggedInUser).setVisible(true);
                    } else if (loggedInUser instanceof Admin) {
                        new AdminDashboardFrame((Admin) loggedInUser).setVisible(true);
                    }
                } else {
                    JOptionPane.showMessageDialog(LoginFrame.this, "Username/CNP sau parola incorectă pentru tipul selectat.", "Eroare Login", JOptionPane.ERROR_MESSAGE);
                    passwordField.setText(""); // Clear password field on failure
                    usernameField.requestFocus();
                }
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt(); // Restore interrupt status
                JOptionPane.showMessageDialog(LoginFrame.this, "Procesul de autentificare a fost întrerupt.", "Eroare", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } catch (ExecutionException e) {

                JOptionPane.showMessageDialog(LoginFrame.this, "Eroare în timpul autentificării: " + e.getCause().getMessage(), "Eroare Server", JOptionPane.ERROR_MESSAGE);
                e.printStackTrace();
            } finally {

                statusLabel.setText(" ");
                loginButton.setEnabled(true);
                registerButton.setEnabled(true);
                usernameField.setEnabled(true);
                passwordField.setEnabled(true);
                userTypeComboBox.setEnabled(true);
            }
        }
    }


    private void openPatientRegistrationDialog() {

        PatientRegistrationDialog registrationDialog = new PatientRegistrationDialog(this);
        registrationDialog.setVisible(true);
    }


}