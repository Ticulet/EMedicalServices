package ui;

import model.Admin;

import javax.swing.*;
import java.awt.*;

public class AdminDashboardFrame extends JFrame {
    private Admin currentAdmin;

    private JButton addClinicButton;
    private JButton addDoctorButton;

    public AdminDashboardFrame(Admin admin) {
        this.currentAdmin = admin;

        setTitle("Admin Dashboard - E-Servicii Medicale (" + admin.getUsername() + ")");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JLabel welcomeLabel = new JLabel("Bun venit, Administrator " + admin.getUsername() + "!");
        welcomeLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcomeLabel.setFont(new Font("Arial", Font.BOLD, 16));

        addClinicButton = new JButton("Adaugă Clinică Nouă");
        addDoctorButton = new JButton("Adaugă Medic Nou");

        Dimension buttonSize = new Dimension(200, 40);
        addClinicButton.setPreferredSize(buttonSize);
        addClinicButton.setMaximumSize(buttonSize);
        addClinicButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        addDoctorButton.setPreferredSize(buttonSize);
        addDoctorButton.setMaximumSize(buttonSize);
        addDoctorButton.setAlignmentX(Component.CENTER_ALIGNMENT);

        mainPanel.add(welcomeLabel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 20)));
        mainPanel.add(addClinicButton);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(addDoctorButton);

        add(mainPanel, BorderLayout.CENTER);

        addClinicButton.addActionListener(e -> openAddClinicDialog());
        addDoctorButton.addActionListener(e -> openAddDoctorDialog()); // << MODIFIED HERE


    }

    private void openAddClinicDialog() {
        AddClinicDialog addClinicDialog = new AddClinicDialog(this);
        addClinicDialog.setVisible(true);
    }


    private void openAddDoctorDialog() {
        AddDoctorDialog addDoctorDialog = new AddDoctorDialog(this); // 'this' is AdminDashboardFrame
        addDoctorDialog.setVisible(true);

    }
}