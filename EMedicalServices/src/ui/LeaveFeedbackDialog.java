package ui;

import model.Patient;
import model.Doctor;
import model.Appointment;
import model.Feedback;
import dao.FeedbackDAO;

import javax.swing.*;
import java.awt.*; // Keep this for Frame

public class LeaveFeedbackDialog extends JDialog {
    private Patient patient;
    private Appointment completedAppointment;
    private Doctor doctor;

    private FeedbackDAO feedbackDAO;

    private JTextArea commentArea;
    private JComboBox<Integer> ratingComboBox;
    private JButton submitButton;
    private JButton cancelButton;

    // MODIFIED CONSTRUCTOR to accept Frame as owner
    public LeaveFeedbackDialog(Frame owner, Patient patient, Appointment completedAppointment, Doctor doctor) {
        super(owner, "Lasă Feedback pentru Dr. " + doctor.getPrenume() + " " + doctor.getNume(), true); // JFrame is a Frame
        this.patient = patient;
        this.completedAppointment = completedAppointment;
        this.doctor = doctor;
        this.feedbackDAO = new FeedbackDAO();

        setSize(450, 350);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // ... (rest of the constructor code for UI components remains the same) ...
        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(BorderFactory.createEmptyBorder(15,15,15,15));
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5,5,5,5);
        gbc.anchor = GridBagConstraints.WEST;

        int y = 0;

        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Feedback pentru consultația cu:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.HORIZONTAL; gbc.weightx = 1.0;
        JLabel doctorInfoLabel = new JLabel("Dr. " + doctor.getPrenume() + " " + doctor.getNume() +
                " (" + doctor.getSpecializarea() + ")");
        doctorInfoLabel.setFont(doctorInfoLabel.getFont().deriveFont(Font.BOLD));
        formPanel.add(doctorInfoLabel, gbc);
        y++;

        gbc.gridx = 0; gbc.gridy = y;
        formPanel.add(new JLabel("Notă (1-Cel mai slab, 5-Excelent):"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.NONE;
        Integer[] ratings = {1, 2, 3, 4, 5};
        ratingComboBox = new JComboBox<>(ratings);
        ratingComboBox.setSelectedItem(5);
        formPanel.add(ratingComboBox, gbc);
        y++;

        gbc.fill = GridBagConstraints.HORIZONTAL;

        gbc.gridx = 0; gbc.gridy = y; gbc.anchor = GridBagConstraints.NORTHWEST;
        formPanel.add(new JLabel("Comentariu:"), gbc);
        gbc.gridx = 1; gbc.fill = GridBagConstraints.BOTH; gbc.weightx = 1.0; gbc.weighty = 1.0;
        commentArea = new JTextArea(5, 20);
        commentArea.setLineWrap(true);
        commentArea.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(commentArea);
        formPanel.add(scrollPane, gbc);
        gbc.fill = GridBagConstraints.NONE; gbc.weightx = 0; gbc.weighty = 0;
        gbc.anchor = GridBagConstraints.WEST;
        y++;

        add(formPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        submitButton = new JButton("Trimite Feedback");
        cancelButton = new JButton("Anulează");
        buttonPanel.add(submitButton);
        buttonPanel.add(cancelButton);
        add(buttonPanel, BorderLayout.SOUTH);

        submitButton.addActionListener(e -> submitFeedback());
        cancelButton.addActionListener(e -> dispose());
    }

    // ... (submitFeedback method remains the same) ...
    private void submitFeedback() {
        String comment = commentArea.getText().trim();
        Integer selectedRating = (Integer) ratingComboBox.getSelectedItem();

        if (selectedRating == null) {
            JOptionPane.showMessageDialog(this, "Vă rugăm selectați o notă.", "Eroare Validare", JOptionPane.ERROR_MESSAGE);
            return;
        }

        Feedback newFeedback = new Feedback(
                patient.getId(),
                doctor.getId(),
                completedAppointment.getId(),
                comment,
                selectedRating
        );

        boolean success = feedbackDAO.addFeedback(newFeedback);

        if (success) {
            JOptionPane.showMessageDialog(this, "Feedback-ul a fost trimis cu succes!", "Mulțumim!", JOptionPane.INFORMATION_MESSAGE);
            dispose();
        } else {
            if (feedbackDAO.hasFeedbackForAppointment(completedAppointment.getId())) {
                JOptionPane.showMessageDialog(this, "Ați trimis deja feedback pentru această consultație.", "Feedback Existent", JOptionPane.WARNING_MESSAGE);
            } else {
                JOptionPane.showMessageDialog(this, "Eroare la trimiterea feedback-ului. Verificați consola.", "Eroare", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}