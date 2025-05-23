package ui;

import model.Doctor;
import model.Appointment;
import model.Patient;
import dao.AppointmentDAO;
import dao.PatientDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class DoctorDashboardFrame extends JFrame {
    private Doctor currentDoctor;
    private AppointmentDAO appointmentDAO;
    private PatientDAO patientDAO;

    // UI Elements
    private JTable appointmentsTable;
    private DefaultTableModel appointmentsTableModel;
    private JButton viewPatientDetailsButton;
    private JButton completeConsultationButton;
    private JButton viewFeedbackButton;
    private JLabel statusLabel;

    // Filter components
    private JTextField patientNameFilterField;
    private JTextField dateFilterField;
    private JButton filterAppointmentsButton;
    private JButton clearFiltersButton;

    private Map<Integer, String> patientNameCache;

    public DoctorDashboardFrame(Doctor doctor) {
        this.currentDoctor = doctor;
        this.appointmentDAO = new AppointmentDAO();
        this.patientDAO = new PatientDAO();
        this.patientNameCache = new HashMap<>();

        setTitle("Portal Medic - Dr. " + doctor.getPrenume() + " " + doctor.getNume());
        setSize(900, 650);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout(10, 10));

        // --- Welcome Panel (North) ---
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel welcomeMessage = new JLabel("Bun venit, Dr. " + currentDoctor.getPrenume() + " " + currentDoctor.getNume() +
                " (" + currentDoctor.getSpecializarea() + ")");
        welcomeMessage.setFont(new Font("Arial", Font.BOLD, 18));
        add(welcomePanel, BorderLayout.NORTH);

        // --- Appointments Outer Panel (Center) ---
        JPanel appointmentsOuterPanel = new JPanel(new BorderLayout(0, 5));
        appointmentsOuterPanel.setBorder(BorderFactory.createTitledBorder("Programările Dvs."));

        // --- Filter Panel ---
        JPanel filterPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        filterPanel.add(new JLabel("Nume Pacient:"));
        patientNameFilterField = new JTextField(15);
        filterPanel.add(patientNameFilterField);

        filterPanel.add(new JLabel("Data (YYYY-MM-DD):"));
        dateFilterField = new JTextField(10);
        filterPanel.add(dateFilterField);

        filterAppointmentsButton = new JButton("Filtrează");
        filterPanel.add(filterAppointmentsButton);
        clearFiltersButton = new JButton("Șterge Filtre");
        filterPanel.add(clearFiltersButton);

        appointmentsOuterPanel.add(filterPanel, BorderLayout.NORTH);

        // --- Appointments Table ---
        String[] columnHeaders = {"ID Programare", "Data și Ora", "Pacient", "Descriere Afecțiune", "Status"};
        appointmentsTableModel = new DefaultTableModel(columnHeaders, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        appointmentsTable = new JTable(appointmentsTableModel);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.getColumnModel().getColumn(0).setMaxWidth(100);
        appointmentsTable.getColumnModel().getColumn(1).setMinWidth(140);
        appointmentsTable.getColumnModel().getColumn(2).setMinWidth(140);
        appointmentsTable.getColumnModel().getColumn(3).setMinWidth(200);
        JScrollPane scrollPane = new JScrollPane(appointmentsTable);
        appointmentsOuterPanel.add(scrollPane, BorderLayout.CENTER);

        // --- Actions for selected appointment ---
        JPanel appointmentActionButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewPatientDetailsButton = new JButton("Vezi Detalii Pacient");
        completeConsultationButton = new JButton("Finalizează Consultația");
        appointmentActionButtonsPanel.add(viewPatientDetailsButton);
        appointmentActionButtonsPanel.add(completeConsultationButton);
        appointmentsOuterPanel.add(appointmentActionButtonsPanel, BorderLayout.SOUTH);
        add(appointmentsOuterPanel, BorderLayout.CENTER);

        // --- Main Doctor Actions & Status Label ---
        JPanel southPanel = new JPanel(new BorderLayout(0,5));
        JPanel mainActionsPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        viewFeedbackButton = new JButton("Vezi Feedback Primit");
        mainActionsPanel.add(viewFeedbackButton);
        southPanel.add(mainActionsPanel, BorderLayout.CENTER);
        statusLabel = new JLabel(" ", SwingConstants.CENTER);
        southPanel.add(statusLabel, BorderLayout.SOUTH);
        add(southPanel, BorderLayout.SOUTH);

        viewPatientDetailsButton.setEnabled(false);
        completeConsultationButton.setEnabled(false);
        viewFeedbackButton.setEnabled(true);

        // Add Listeners
        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appointmentsTable.getSelectedRow() != -1) {
                String status = (String) appointmentsTableModel.getValueAt(appointmentsTable.getSelectedRow(), 4);
                viewPatientDetailsButton.setEnabled(true);
                completeConsultationButton.setEnabled("SCHEDULED".equalsIgnoreCase(status));
            } else {
                viewPatientDetailsButton.setEnabled(false);
                completeConsultationButton.setEnabled(false);
            }
        });

        viewPatientDetailsButton.addActionListener(e -> viewSelectedPatientDetails());
        completeConsultationButton.addActionListener(e -> completeSelectedConsultation());
        viewFeedbackButton.addActionListener(e -> openViewFeedbackDialog());
        filterAppointmentsButton.addActionListener(e -> startLoadingDoctorAppointments());
        clearFiltersButton.addActionListener(e -> {
            patientNameFilterField.setText("");
            dateFilterField.setText("");
            startLoadingDoctorAppointments();
        });

        startLoadingDoctorAppointments();
    }

    private static class DoctorAppointmentsData {
        List<Appointment> appointments; Map<Integer, String> patientNames;
        DoctorAppointmentsData(List<Appointment> appointments, Map<Integer, String> patientNames) {
            this.appointments = appointments; this.patientNames = patientNames;
        }
    }

    private void startLoadingDoctorAppointments() {
        statusLabel.setText("Încărcare programări...");
        setAllButtonsEnabled(false);

        String patientNameFilter = patientNameFilterField.getText().trim();
        String dateFilter = dateFilterField.getText().trim();

        if (!dateFilter.isEmpty()) {
            try {
                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
                sdf.setLenient(false);
                sdf.parse(dateFilter);
            } catch (ParseException e) {
                JOptionPane.showMessageDialog(this, "Formatul datei pentru filtrare este invalid. Folosiți YYYY-MM-DD.", "Format Dată Invalid", JOptionPane.ERROR_MESSAGE);
                statusLabel.setText(" ");
                setAllButtonsEnabled(true);
                dateFilterField.requestFocus();
                return;
            }
        }

        SwingWorker<DoctorAppointmentsData, Void> worker = new SwingWorker<DoctorAppointmentsData, Void>() {
            @Override
            protected DoctorAppointmentsData doInBackground() throws Exception {
                List<Appointment> apps = appointmentDAO.getAppointmentsForDoctorFiltered(
                        currentDoctor.getId(),
                        patientNameFilter.isEmpty() ? null : patientNameFilter,
                        dateFilter.isEmpty() ? null : dateFilter
                );
                Map<Integer, String> fetchedPatientNames = new HashMap<>();
                for (Appointment app : apps) {
                    if (!fetchedPatientNames.containsKey(app.getPatientId())) {
                        Patient patient = patientDAO.getPatientById(app.getPatientId());
                        if (patient != null) {
                            fetchedPatientNames.put(app.getPatientId(), patient.getPrenume() + " " + patient.getNume());
                        } else {
                            fetchedPatientNames.put(app.getPatientId(), "Pacient Necunoscut (" + app.getPatientId() + ")");
                        }
                    }
                }
                return new DoctorAppointmentsData(apps, fetchedPatientNames);
            }

            @Override
            protected void done() {
                try {
                    DoctorAppointmentsData data = get();
                    patientNameCache.clear();
                    if (data.patientNames != null) patientNameCache.putAll(data.patientNames);
                    appointmentsTableModel.setRowCount(0);
                    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
                    if (data.appointments != null && !data.appointments.isEmpty()) {
                        for (Appointment app : data.appointments) {
                            String patientName = patientNameCache.getOrDefault(app.getPatientId(), "Pacient Necunoscut");
                            appointmentsTableModel.addRow(new Object[]{
                                    app.getId(), dateTimeFormatter.format(app.getDataProgramare()),
                                    patientName, app.getDescriereAfectiune(), app.getStatus() });
                        }
                    }
                    appointmentsTable.clearSelection();
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this, "Eroare la încărcarea programărilor: " + e.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                } finally {
                    statusLabel.setText(" ");
                    setAllButtonsEnabled(true);
                }
            }
        };
        worker.execute();
    }

    private void setAllButtonsEnabled(boolean enabled) {
        appointmentsTable.setEnabled(enabled);
        if (!enabled) {
            viewPatientDetailsButton.setEnabled(false);
            completeConsultationButton.setEnabled(false);
        } else {
            int selectedRow = appointmentsTable.getSelectedRow();
            if (selectedRow != -1 && appointmentsTableModel.getRowCount() > selectedRow) { // Check rowCount
                String status = (String) appointmentsTableModel.getValueAt(selectedRow, 4);
                viewPatientDetailsButton.setEnabled(true);
                completeConsultationButton.setEnabled("SCHEDULED".equalsIgnoreCase(status));
            } else {
                viewPatientDetailsButton.setEnabled(false);
                completeConsultationButton.setEnabled(false);
            }
        }
        viewFeedbackButton.setEnabled(enabled);
        filterAppointmentsButton.setEnabled(enabled);
        clearFiltersButton.setEnabled(enabled);
        patientNameFilterField.setEnabled(enabled);
        dateFilterField.setEnabled(enabled);
    }

    private void viewSelectedPatientDetails() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vă rugăm selectați o programare din listă.", "Nicio Selecție", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int appointmentId = (Integer) appointmentsTableModel.getValueAt(selectedRow, 0);
        Appointment selectedAppointment = appointmentDAO.getAppointmentById(appointmentId);

        if (selectedAppointment == null) {
            JOptionPane.showMessageDialog(this, "Detaliile programării nu au putut fi încărcate.", "Eroare", JOptionPane.ERROR_MESSAGE);
            return;
        }
        int patientId = selectedAppointment.getPatientId();
        PatientHistoryForDoctorDialog historyDialog = new PatientHistoryForDoctorDialog(this, patientId, currentDoctor);
        historyDialog.setVisible(true);
    }

    private void completeSelectedConsultation() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vă rugăm selectați o programare pentru a o finaliza.", "Nicio Selecție", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int appointmentId = (Integer) appointmentsTableModel.getValueAt(selectedRow, 0);

        statusLabel.setText("Verificare programare...");
        setAllButtonsEnabled(false);

        new SwingWorker<Appointment, Void>() {
            @Override
            protected Appointment doInBackground() throws Exception {
                return appointmentDAO.getAppointmentById(appointmentId);
            }

            @Override
            protected void done() {
                try {
                    Appointment selectedAppointment = get();
                    if (selectedAppointment == null) {
                        JOptionPane.showMessageDialog(DoctorDashboardFrame.this, "Programarea selectată nu mai există.", "Eroare", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    if (!"SCHEDULED".equalsIgnoreCase(selectedAppointment.getStatus())) {
                        JOptionPane.showMessageDialog(DoctorDashboardFrame.this, "Doar programările active ('SCHEDULED') pot fi finalizate.", "Acțiune Invalidă", JOptionPane.ERROR_MESSAGE);
                        return;
                    }
                    CompleteConsultationDialog consultationDialog = new CompleteConsultationDialog(DoctorDashboardFrame.this, selectedAppointment, currentDoctor);
                    consultationDialog.setVisible(true);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(DoctorDashboardFrame.this, "Eroare la verificarea programării: " + e.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                } finally {
                    statusLabel.setText(" ");
                    setAllButtonsEnabled(true);
                    startLoadingDoctorAppointments();
                }
            }
        }.execute();
    }

    private void openViewFeedbackDialog() {
        ViewDoctorFeedbackDialog feedbackDialog = new ViewDoctorFeedbackDialog(this, currentDoctor);
        feedbackDialog.setVisible(true);
    }
}