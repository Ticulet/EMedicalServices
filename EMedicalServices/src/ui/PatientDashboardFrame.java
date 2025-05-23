package ui;

import model.Patient;
import model.Appointment;
import model.Doctor;
import model.ConsultationRecord;
import model.Feedback;
import dao.AppointmentDAO;
import dao.DoctorDAO;
import dao.ConsultationRecordDAO;
import dao.FeedbackDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.sql.Timestamp;
import java.util.concurrent.ExecutionException;

public class PatientDashboardFrame extends JFrame {
    private Patient currentPatient;
    private AppointmentDAO appointmentDAO;
    private DoctorDAO doctorDAO;
    private ConsultationRecordDAO consultationRecordDAO;
    private FeedbackDAO feedbackDAO;

    // Profile Labels
    private JLabel nameLabelValue;
    private JLabel cnpLabelValue;
    private JLabel emailLabelValue;
    private JLabel phoneLabelValue;
    private JLabel dobLabelValue;
    private JLabel addressLabelValue;

    // Doctor Search Button
    private JButton searchDoctorsButton;

    // Appointments UI
    private JTable appointmentsTable;
    private DefaultTableModel appointmentsTableModel;
    private JButton cancelAppointmentButton;
    private JButton modifyAppointmentButton;

    // Consultation History UI
    private JTable consultationHistoryTable;
    private DefaultTableModel consultationHistoryTableModel;
    private JTextArea consultationDetailsArea;
    private JButton leaveFeedbackButton;

    // Caches and data lists
    private Map<Integer, String> doctorNameCache;
    private Map<Integer, Appointment> appointmentCache;
    private List<ConsultationRecord> displayedConsultationRecords;

    // For SwingWorker status
    private JLabel statusLabel;

    public PatientDashboardFrame(Patient patient) {
        this.currentPatient = patient;
        this.appointmentDAO = new AppointmentDAO();
        this.doctorDAO = new DoctorDAO();
        this.consultationRecordDAO = new ConsultationRecordDAO();
        this.feedbackDAO = new FeedbackDAO();
        this.doctorNameCache = new HashMap<>();
        this.appointmentCache = new HashMap<>();
        this.displayedConsultationRecords = new ArrayList<>();

        setTitle("Portal Pacient - " + patient.getPrenume() + " " + patient.getNume());
        setSize(850, 800); // Adjusted size
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        JPanel mainContentPanel = new JPanel();
        mainContentPanel.setLayout(new BoxLayout(mainContentPanel, BoxLayout.Y_AXIS));
        mainContentPanel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        // --- Welcome Panel ---
        JPanel welcomePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        JLabel welcomeMessage = new JLabel("Bun venit, " + currentPatient.getPrenume() + " " + currentPatient.getNume() + "!");
        welcomeMessage.setFont(new Font("Arial", Font.BOLD, 18));
        mainContentPanel.add(welcomePanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 10)));

        // --- Profile Panel ---
        JPanel profilePanel = new JPanel(new GridBagLayout());
        profilePanel.setBorder(BorderFactory.createTitledBorder("Detalii Profil"));
        GridBagConstraints gbcProfile = new GridBagConstraints();
        gbcProfile.insets = new Insets(5, 10, 5, 10);
        gbcProfile.anchor = GridBagConstraints.WEST;
        int yProfile = 0;
        gbcProfile.gridx = 0; gbcProfile.gridy = yProfile; profilePanel.add(new JLabel("Nume Complet:"), gbcProfile);
        gbcProfile.gridx = 1; nameLabelValue = new JLabel(); profilePanel.add(nameLabelValue, gbcProfile); yProfile++;
        gbcProfile.gridx = 0; gbcProfile.gridy = yProfile; profilePanel.add(new JLabel("CNP:"), gbcProfile);
        gbcProfile.gridx = 1; cnpLabelValue = new JLabel(); profilePanel.add(cnpLabelValue, gbcProfile); yProfile++;
        gbcProfile.gridx = 0; gbcProfile.gridy = yProfile; profilePanel.add(new JLabel("Email:"), gbcProfile);
        gbcProfile.gridx = 1; emailLabelValue = new JLabel(); profilePanel.add(emailLabelValue, gbcProfile); yProfile++;
        gbcProfile.gridx = 0; gbcProfile.gridy = yProfile; profilePanel.add(new JLabel("Număr Telefon:"), gbcProfile);
        gbcProfile.gridx = 1; phoneLabelValue = new JLabel(); profilePanel.add(phoneLabelValue, gbcProfile); yProfile++;
        gbcProfile.gridx = 0; gbcProfile.gridy = yProfile; profilePanel.add(new JLabel("Data Nașterii:"), gbcProfile);
        gbcProfile.gridx = 1; dobLabelValue = new JLabel(); profilePanel.add(dobLabelValue, gbcProfile); yProfile++;
        gbcProfile.gridx = 0; gbcProfile.gridy = yProfile; profilePanel.add(new JLabel("Adresă Domiciliu:"), gbcProfile);
        gbcProfile.gridx = 1; gbcProfile.gridwidth = GridBagConstraints.REMAINDER; gbcProfile.fill = GridBagConstraints.HORIZONTAL;
        addressLabelValue = new JLabel(); profilePanel.add(addressLabelValue, gbcProfile);
        gbcProfile.gridwidth = 1; gbcProfile.fill = GridBagConstraints.NONE; // Reset
        profilePanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        profilePanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, profilePanel.getPreferredSize().height + 30));
        mainContentPanel.add(profilePanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        populateProfileDetails();

        // --- Doctor Search Actions Panel ---
        JPanel doctorActionsPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        doctorActionsPanel.setBorder(BorderFactory.createTitledBorder("Medici și Programări Noi"));
        searchDoctorsButton = new JButton("Caută Medici / Programează Consultație");
        doctorActionsPanel.add(searchDoctorsButton);
        doctorActionsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        doctorActionsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, doctorActionsPanel.getPreferredSize().height + 20));
        mainContentPanel.add(doctorActionsPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Appointments History Panel ---
        JPanel appointmentsPanel = new JPanel(new BorderLayout(0, 5));
        appointmentsPanel.setBorder(BorderFactory.createTitledBorder("Programările Mele Active/Recente"));
        String[] appointmentColumnHeaders = {"ID Pr.", "Data și Ora", "Medic", "Descriere", "Status"};
        appointmentsTableModel = new DefaultTableModel(appointmentColumnHeaders, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        appointmentsTable = new JTable(appointmentsTableModel);
        appointmentsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        appointmentsTable.getColumnModel().getColumn(0).setMaxWidth(60);
        appointmentsTable.getColumnModel().getColumn(1).setMinWidth(140);
        appointmentsTable.getColumnModel().getColumn(2).setMinWidth(140);
        JScrollPane appointmentsScrollPane = new JScrollPane(appointmentsTable);
        appointmentsScrollPane.setPreferredSize(new Dimension(appointmentsScrollPane.getPreferredSize().width, 150));
        appointmentsPanel.add(appointmentsScrollPane, BorderLayout.CENTER);
        JPanel appointmentButtonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        modifyAppointmentButton = new JButton("Modifică Programarea");
        cancelAppointmentButton = new JButton("Anulează Programarea");
        appointmentButtonsPanel.add(modifyAppointmentButton);
        appointmentButtonsPanel.add(cancelAppointmentButton);
        appointmentsPanel.add(appointmentButtonsPanel, BorderLayout.SOUTH);
        appointmentsPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        appointmentsPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200 + 40));
        mainContentPanel.add(appointmentsPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 15)));

        // --- Consultation History Panel ---
        JPanel consultationPanel = new JPanel(new BorderLayout(0, 5));
        consultationPanel.setBorder(BorderFactory.createTitledBorder("Istoric Consultații Medicale"));
        String[] consultationColumnHeaders = {"Data Consultație", "Medic", "Diagnostic (Sumar)"};
        consultationHistoryTableModel = new DefaultTableModel(consultationColumnHeaders, 0) {
            @Override public boolean isCellEditable(int row, int column) { return false; }
        };
        consultationHistoryTable = new JTable(consultationHistoryTableModel);
        consultationHistoryTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        consultationHistoryTable.getColumnModel().getColumn(0).setMinWidth(140);
        consultationHistoryTable.getColumnModel().getColumn(1).setMinWidth(140);
        JScrollPane consultationScrollPane = new JScrollPane(consultationHistoryTable);
        consultationScrollPane.setPreferredSize(new Dimension(consultationScrollPane.getPreferredSize().width, 150));
        consultationPanel.add(consultationScrollPane, BorderLayout.CENTER);
        JPanel consultationButtonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        leaveFeedbackButton = new JButton("Lasă Feedback pentru Consultația Selectată");
        consultationButtonPanel.add(leaveFeedbackButton);
        consultationPanel.add(consultationButtonPanel, BorderLayout.SOUTH);
        consultationPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        consultationPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 200 + 40));
        mainContentPanel.add(consultationPanel);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0, 5)));

        // --- Consultation Detail Display Panel ---
        JPanel consultationDetailDisplayPanel = new JPanel(new BorderLayout(0,5));
        consultationDetailDisplayPanel.setBorder(BorderFactory.createTitledBorder("Detalii Consultație Selectată"));
        consultationDetailsArea = new JTextArea(5, 20);
        consultationDetailsArea.setEditable(false);
        consultationDetailsArea.setLineWrap(true);
        consultationDetailsArea.setWrapStyleWord(true);
        consultationDetailsArea.setFont(UIManager.getFont("Label.font"));
        JScrollPane detailsScrollPane = new JScrollPane(consultationDetailsArea);
        detailsScrollPane.setPreferredSize(new Dimension(detailsScrollPane.getPreferredSize().width, 100));
        consultationDetailDisplayPanel.add(detailsScrollPane, BorderLayout.CENTER);
        consultationDetailDisplayPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        consultationDetailDisplayPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        mainContentPanel.add(consultationDetailDisplayPanel);

        // --- Status Label for Loading Indication ---
        statusLabel = new JLabel(" ");
        statusLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        mainContentPanel.add(Box.createRigidArea(new Dimension(0,10)));
        mainContentPanel.add(statusLabel);
        mainContentPanel.add(Box.createVerticalGlue());

        JScrollPane mainScrollPane = new JScrollPane(mainContentPanel);
        mainScrollPane.getVerticalScrollBar().setUnitIncrement(16);
        mainScrollPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED);
        mainScrollPane.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        add(mainScrollPane, BorderLayout.CENTER);

        // Action Listeners
        searchDoctorsButton.addActionListener(e -> openDoctorSearchDialog());
        cancelAppointmentButton.addActionListener(e -> performCancelAppointment());
        modifyAppointmentButton.addActionListener(e -> performModifyAppointment());
        leaveFeedbackButton.addActionListener(e -> performLeaveFeedback());

        appointmentsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && appointmentsTable.getSelectedRow() != -1) {
                String status = (String) appointmentsTableModel.getValueAt(appointmentsTable.getSelectedRow(), 4);
                boolean isScheduled = "SCHEDULED".equalsIgnoreCase(status);
                cancelAppointmentButton.setEnabled(isScheduled);
                modifyAppointmentButton.setEnabled(isScheduled);
            } else {
                cancelAppointmentButton.setEnabled(false);
                modifyAppointmentButton.setEnabled(false);
            }
        });
        cancelAppointmentButton.setEnabled(false);
        modifyAppointmentButton.setEnabled(false);

        consultationHistoryTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && consultationHistoryTable.getSelectedRow() != -1) {
                displaySelectedConsultationDetails();
                ConsultationRecord selectedRecord = getSelectedConsultationRecord();
                if (selectedRecord != null) {
                    Appointment appointment = appointmentCache.get(selectedRecord.getAppointmentId());
                    if (appointment != null && "COMPLETED".equalsIgnoreCase(appointment.getStatus()) &&
                            (feedbackDAO != null && !feedbackDAO.hasFeedbackForAppointment(appointment.getId()))) { // Added null check for feedbackDAO
                        leaveFeedbackButton.setEnabled(true);
                    } else {
                        leaveFeedbackButton.setEnabled(false);
                    }
                } else {
                    leaveFeedbackButton.setEnabled(false);
                }
            } else {
                consultationDetailsArea.setText("Selectați o consultație din listă pentru a vedea detaliile complete.");
                leaveFeedbackButton.setEnabled(false);
            }
        });
        leaveFeedbackButton.setEnabled(false);

        startLoadingInitialData();
    }

    private void populateProfileDetails() {
        nameLabelValue.setText(currentPatient.getPrenume() + " " + currentPatient.getNume());
        cnpLabelValue.setText(currentPatient.getCnp());
        emailLabelValue.setText(currentPatient.getAdresaEmail() != null ? currentPatient.getAdresaEmail() : "N/A");
        phoneLabelValue.setText(currentPatient.getNumarTelefon() != null ? currentPatient.getNumarTelefon() : "N/A");
        addressLabelValue.setText(currentPatient.getAdresaDomiciliu() != null ? currentPatient.getAdresaDomiciliu() : "N/A");
        if (currentPatient.getDataNastere() != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");
            dobLabelValue.setText(sdf.format(currentPatient.getDataNastere()));
        } else {
            dobLabelValue.setText("N/A");
        }
    }

    private void openDoctorSearchDialog() {
        DoctorSearchDialog searchDialog = new DoctorSearchDialog(this, currentPatient);
        searchDialog.setVisible(true);
        startLoadingInitialData();
    }

    private static class InitialData {
        List<Appointment> appointments;
        List<ConsultationRecord> consultationRecords;
        Map<Integer, String> doctorNames;
        Map<Integer, Appointment> appointmentMap;

        InitialData(List<Appointment> appointments, List<ConsultationRecord> consultationRecords, Map<Integer, String> doctorNames, Map<Integer, Appointment> appointmentMap) {
            this.appointments = appointments;
            this.consultationRecords = consultationRecords;
            this.doctorNames = doctorNames;
            this.appointmentMap = appointmentMap;
        }
    }

    private void startLoadingInitialData() {
        if (statusLabel == null) { // Should not happen if constructor order is correct
            System.err.println("statusLabel is null in startLoadingInitialData. UI not fully initialized?");
            return;
        }
        statusLabel.setText("Încărcare date...");
        searchDoctorsButton.setEnabled(false);
        cancelAppointmentButton.setEnabled(false);
        modifyAppointmentButton.setEnabled(false);
        leaveFeedbackButton.setEnabled(false);
        appointmentsTable.setEnabled(false);
        consultationHistoryTable.setEnabled(false);

        SwingWorker<InitialData, Void> worker = new SwingWorker<InitialData, Void>() {
            @Override
            protected InitialData doInBackground() throws Exception {
                List<Appointment> appointments = appointmentDAO.getAppointmentsForPatient(currentPatient.getId());
                List<ConsultationRecord> consultationRecords = consultationRecordDAO.getConsultationRecordsForPatient(currentPatient.getId());

                Map<Integer, String> fetchedDoctorNames = new HashMap<>();
                Map<Integer, Appointment> fetchedAppointmentsMap = new HashMap<>();

                for (Appointment app : appointments) {
                    fetchedAppointmentsMap.put(app.getId(), app);
                    if (!fetchedDoctorNames.containsKey(app.getDoctorId())) {
                        Doctor doctor = doctorDAO.getDoctorById(app.getDoctorId());
                        if (doctor != null) {
                            fetchedDoctorNames.put(app.getDoctorId(), "Dr. " + doctor.getPrenume() + " " + doctor.getNume());
                        } else {
                            fetchedDoctorNames.put(app.getDoctorId(), "Medic Necunoscut (" + app.getDoctorId() + ")");
                        }
                    }
                }
                for (ConsultationRecord cr : consultationRecords) {
                    // Ensure appointment for this consultation record is in the map if not already
                    if (!fetchedAppointmentsMap.containsKey(cr.getAppointmentId())) {
                        Appointment associatedAppFromDB = appointmentDAO.getAppointmentById(cr.getAppointmentId());
                        if (associatedAppFromDB != null) {
                            fetchedAppointmentsMap.put(associatedAppFromDB.getId(), associatedAppFromDB);
                        }
                    }
                    Appointment associatedApp = fetchedAppointmentsMap.get(cr.getAppointmentId());
                    if (associatedApp != null && !fetchedDoctorNames.containsKey(associatedApp.getDoctorId())) {
                        Doctor doctor = doctorDAO.getDoctorById(associatedApp.getDoctorId());
                        if (doctor != null) {
                            fetchedDoctorNames.put(associatedApp.getDoctorId(), "Dr. " + doctor.getPrenume() + " " + doctor.getNume());
                        } else {
                            fetchedDoctorNames.put(associatedApp.getDoctorId(), "Medic Necunoscut (" + associatedApp.getDoctorId() + ")");
                        }
                    }
                }
                return new InitialData(appointments, consultationRecords, fetchedDoctorNames, fetchedAppointmentsMap);
            }

            @Override
            protected void done() {
                try {
                    InitialData data = get();

                    doctorNameCache.clear();
                    if (data.doctorNames != null) doctorNameCache.putAll(data.doctorNames);
                    appointmentCache.clear();
                    if (data.appointmentMap != null) appointmentCache.putAll(data.appointmentMap);

                    appointmentsTableModel.setRowCount(0);
                    SimpleDateFormat dateTimeFormatter = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
                    if(data.appointments != null) {
                        for (Appointment app : data.appointments) {
                            String doctorName = doctorNameCache.getOrDefault(app.getDoctorId(), "Medic Necunoscut");
                            appointmentsTableModel.addRow(new Object[]{
                                    app.getId(), dateTimeFormatter.format(app.getDataProgramare()),
                                    doctorName, app.getDescriereAfectiune(), app.getStatus()
                            });
                        }
                    }
                    appointmentsTable.clearSelection();

                    consultationHistoryTableModel.setRowCount(0);
                    displayedConsultationRecords.clear();
                    if(data.consultationRecords != null) displayedConsultationRecords.addAll(data.consultationRecords);
                    SimpleDateFormat dateFormatter = new SimpleDateFormat("dd MMMM yyyy");

                    for (ConsultationRecord record : displayedConsultationRecords) {
                        Appointment associatedAppointment = appointmentCache.get(record.getAppointmentId());
                        String doctorName = "Medic Nespecificat";
                        Timestamp consultationDateToDisplay = record.getDataConsultatie();
                        if (associatedAppointment != null) {
                            doctorName = doctorNameCache.getOrDefault(associatedAppointment.getDoctorId(), "Medic Necunoscut");
                            if (consultationDateToDisplay == null) consultationDateToDisplay = associatedAppointment.getDataProgramare();
                        }
                        String diagnosticSummary = record.getDiagnostic();
                        if (diagnosticSummary != null && diagnosticSummary.length() > 50) diagnosticSummary = diagnosticSummary.substring(0, 47) + "...";
                        else if (diagnosticSummary == null) diagnosticSummary = "N/A";
                        consultationHistoryTableModel.addRow(new Object[]{
                                consultationDateToDisplay != null ? dateFormatter.format(consultationDateToDisplay) : "Dată Necunoscută",
                                doctorName, diagnosticSummary
                        });
                    }
                    consultationHistoryTable.clearSelection();
                    consultationDetailsArea.setText("Selectați o consultație din listă pentru a vedea detaliile complete.");

                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                    JOptionPane.showMessageDialog(PatientDashboardFrame.this,
                            "Eroare la încărcarea datelor: " + e.getMessage(),
                            "Eroare Încărcare", JOptionPane.ERROR_MESSAGE);
                } finally {
                    if (statusLabel != null) statusLabel.setText(" ");
                    searchDoctorsButton.setEnabled(true);
                    appointmentsTable.setEnabled(true);
                    consultationHistoryTable.setEnabled(true);
                    // Other buttons (cancel, modify, feedback) will be enabled by their respective selection listeners.
                }
            }
        };
        worker.execute();
    }

    private void performCancelAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vă rugăm selectați o programare.", "Nicio Selecție", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int appointmentId = (Integer) appointmentsTableModel.getValueAt(selectedRow, 0);
        String status = (String) appointmentsTableModel.getValueAt(selectedRow, 4);

        if (!"SCHEDULED".equalsIgnoreCase(status)) {
            JOptionPane.showMessageDialog(this, "Doar programările 'SCHEDULED' pot fi anulate.", "Info", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        int confirmation = JOptionPane.showConfirmDialog(this, "Sunteți sigur că doriți să anulați această programare?", "Confirmare Anulare", JOptionPane.YES_NO_OPTION);
        if (confirmation == JOptionPane.YES_OPTION) {
            statusLabel.setText("Anulare în curs...");
            setButtonsEnabled(false); // Disable buttons during operation

            new SwingWorker<Boolean, Void>() {
                @Override
                protected Boolean doInBackground() throws Exception {
                    return appointmentDAO.cancelAppointment(appointmentId);
                }
                @Override
                protected void done() {
                    try {
                        if (get()) {
                            JOptionPane.showMessageDialog(PatientDashboardFrame.this, "Programarea a fost anulată cu succes.", "Anulare Reușită", JOptionPane.INFORMATION_MESSAGE);
                        } else {
                            JOptionPane.showMessageDialog(PatientDashboardFrame.this, "Eroare la anularea programării.", "Eroare Anulare", JOptionPane.ERROR_MESSAGE);
                        }
                    } catch (InterruptedException | ExecutionException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(PatientDashboardFrame.this, "Eroare la anulare: " + ex.getMessage(), "Eroare", JOptionPane.ERROR_MESSAGE);
                    } finally {
                        startLoadingInitialData(); // Refresh all data
                        setButtonsEnabled(true); // Re-enable buttons
                    }
                }
            }.execute();
        }
    }

    private void performModifyAppointment() {
        int selectedRow = appointmentsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vă rugăm selectați o programare pentru a o modifica.", "Nicio Selecție", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int appointmentId = (Integer) appointmentsTableModel.getValueAt(selectedRow, 0);
        Appointment appointmentToModify = appointmentDAO.getAppointmentById(appointmentId);

        if (appointmentToModify == null) {
            JOptionPane.showMessageDialog(this, "Programarea selectată nu a putut fi găsită în baza de date.", "Eroare Programare", JOptionPane.ERROR_MESSAGE);
            startLoadingInitialData();
            return;
        }
        if (!"SCHEDULED".equalsIgnoreCase(appointmentToModify.getStatus())) {
            JOptionPane.showMessageDialog(this, "Doar programările active ('SCHEDULED') pot fi modificate.", "Acțiune Invalidă", JOptionPane.ERROR_MESSAGE);
            return;
        }
        ModifyAppointmentDialog modifyDialog = new ModifyAppointmentDialog(this, appointmentToModify, this.doctorDAO);
        modifyDialog.setVisible(true);
        startLoadingInitialData();
    }

    private void displaySelectedConsultationDetails() {
        int selectedRow = consultationHistoryTable.getSelectedRow();
        if (selectedRow >= 0 && this.displayedConsultationRecords != null && selectedRow < this.displayedConsultationRecords.size()) {
            ConsultationRecord record = this.displayedConsultationRecords.get(selectedRow);
            Appointment associatedAppointment = appointmentCache.get(record.getAppointmentId());
            String doctorName = "Medic Nespecificat";
            Timestamp consultationDateToDisplay = record.getDataConsultatie();

            if (associatedAppointment != null) {
                doctorName = doctorNameCache.getOrDefault(associatedAppointment.getDoctorId(), "Medic Necunoscut ("+associatedAppointment.getDoctorId()+")" );
                if (consultationDateToDisplay == null) {
                    consultationDateToDisplay = associatedAppointment.getDataProgramare();
                }
            }
            SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
            SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd MMMM yyyy");
            StringBuilder details = new StringBuilder();
            details.append("Dată Consultație: ");
            if (consultationDateToDisplay != null) {
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(consultationDateToDisplay.getTime());
                if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
                    details.append(sdfDateOnly.format(consultationDateToDisplay));
                } else {
                    details.append(sdfDateTime.format(consultationDateToDisplay));
                }
            } else {
                details.append("Nespecificată");
            }
            details.append("\n");
            details.append("Medic: ").append(doctorName).append("\n\n");
            details.append("Diagnostic Complet:\n").append(record.getDiagnostic() != null && !record.getDiagnostic().isEmpty() ? record.getDiagnostic() : "N/A").append("\n\n");
            details.append("Rețetă / Indicații Medicale:\n").append(record.getPrescriptie() != null && !record.getPrescriptie().isEmpty() ? record.getPrescriptie() : "N/A");

            consultationDetailsArea.setText(details.toString());
            consultationDetailsArea.setCaretPosition(0);
        } else {
            consultationDetailsArea.setText("Selectați o consultație din listă pentru a vedea detaliile complete.");
        }
    }

    private ConsultationRecord getSelectedConsultationRecord() {
        int selectedRow = consultationHistoryTable.getSelectedRow();
        if (selectedRow >= 0 && displayedConsultationRecords != null && selectedRow < displayedConsultationRecords.size()) {
            return displayedConsultationRecords.get(selectedRow);
        }
        return null;
    }

    private void performLeaveFeedback() {
        ConsultationRecord selectedRecord = getSelectedConsultationRecord();
        if (selectedRecord == null) {
            JOptionPane.showMessageDialog(this, "Vă rugăm selectați o consultație finalizată din istoric.", "Nicio Selecție", JOptionPane.WARNING_MESSAGE);
            return;
        }
        Appointment appointment = appointmentCache.get(selectedRecord.getAppointmentId());
        if (appointment == null || !"COMPLETED".equalsIgnoreCase(appointment.getStatus())) {
            JOptionPane.showMessageDialog(this, "Puteți lăsa feedback doar pentru consultațiile finalizate ('COMPLETED').", "Acțiune Invalidă", JOptionPane.ERROR_MESSAGE);
            return;
        }
        if (feedbackDAO.hasFeedbackForAppointment(appointment.getId())) {
            JOptionPane.showMessageDialog(this, "Ați lăsat deja feedback pentru această consultație.", "Feedback Existent", JOptionPane.INFORMATION_MESSAGE);
            return;
        }
        Doctor doctor = doctorDAO.getDoctorById(appointment.getDoctorId());
        if (doctor == null) {
            JOptionPane.showMessageDialog(this, "Detaliile medicului nu au putut fi încărcate.", "Eroare Medic", JOptionPane.ERROR_MESSAGE);
            return;
        }
        LeaveFeedbackDialog feedbackDialog = new LeaveFeedbackDialog(this, currentPatient, appointment, doctor);
        feedbackDialog.setVisible(true);
        // Refresh button state based on selection listener logic, no need to explicitly disable here
        // as selection listener will run when dialog closes and focus returns.
        // For robustness, could re-check and set:
        // boolean canLeaveFeedback = !feedbackDAO.hasFeedbackForAppointment(appointment.getId());
        // leaveFeedbackButton.setEnabled(canLeaveFeedback);
    }

    // Helper to disable/enable all major action buttons during SwingWorker execution
    private void setButtonsEnabled(boolean enabled) {
        searchDoctorsButton.setEnabled(enabled);
        cancelAppointmentButton.setEnabled(enabled && appointmentsTable.getSelectedRow() != -1 && "SCHEDULED".equalsIgnoreCase((String)appointmentsTableModel.getValueAt(appointmentsTable.getSelectedRow(), 4)));
        modifyAppointmentButton.setEnabled(enabled && appointmentsTable.getSelectedRow() != -1 && "SCHEDULED".equalsIgnoreCase((String)appointmentsTableModel.getValueAt(appointmentsTable.getSelectedRow(), 4)));

        boolean consultationSelected = consultationHistoryTable.getSelectedRow() != -1;
        ConsultationRecord selectedRecord = getSelectedConsultationRecord();
        boolean canLeaveFeedback = false;
        if(enabled && consultationSelected && selectedRecord != null) {
            Appointment appointment = appointmentCache.get(selectedRecord.getAppointmentId());
            if (appointment != null && "COMPLETED".equalsIgnoreCase(appointment.getStatus()) &&
                    !feedbackDAO.hasFeedbackForAppointment(appointment.getId())) {
                canLeaveFeedback = true;
            }
        }
        leaveFeedbackButton.setEnabled(canLeaveFeedback);
    }
}