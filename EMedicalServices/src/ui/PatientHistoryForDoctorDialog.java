package ui;

import model.Patient;
import model.Doctor; // To display current doctor's context if needed
import model.ConsultationRecord;
import dao.PatientDAO; // To get patient's full name
import dao.ConsultationRecordDAO;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.sql.Timestamp; // For displaying consultation date


public class PatientHistoryForDoctorDialog extends JDialog {
    private Patient patient; // The patient whose history is being viewed
    private Doctor currentViewingDoctor; // The doctor viewing this history

    private PatientDAO patientDAO;
    private ConsultationRecordDAO consultationRecordDAO;

    private JTable historyTable;
    private DefaultTableModel historyTableModel;
    private JTextArea detailsArea; // To show full diagnostic/prescription of selected record

    public PatientHistoryForDoctorDialog(Frame owner, int patientId, Doctor viewingDoctor) {
        super(owner, "Istoric Medical Pacient (cu Dr. " + viewingDoctor.getPrenume() + " " + viewingDoctor.getNume() + ")", true);
        this.currentViewingDoctor = viewingDoctor;

        this.patientDAO = new PatientDAO();
        this.consultationRecordDAO = new ConsultationRecordDAO();

        this.patient = patientDAO.getPatientById(patientId); // Fetch patient details

        if (this.patient == null) {
            JOptionPane.showMessageDialog(owner, "Pacientul nu a putut fi găsit.", "Eroare Pacient", JOptionPane.ERROR_MESSAGE);
            SwingUtilities.invokeLater(this::dispose); // Close dialog if patient not found
            return;
        }

        setTitle("Istoric Medical: " + patient.getPrenume() + " " + patient.getNume() + " (cu Dr. " + viewingDoctor.getPrenume() + ")");


        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // --- Patient Info Panel (North) ---
        JPanel patientInfoPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        patientInfoPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        JLabel patientNameLabel = new JLabel("Istoric pentru pacient: " + patient.getPrenume() + " " + patient.getNume() + " (CNP: " + patient.getCnp() + ")");
        patientNameLabel.setFont(patientNameLabel.getFont().deriveFont(Font.BOLD));
        patientInfoPanel.add(patientNameLabel);
        add(patientInfoPanel, BorderLayout.NORTH);

        // --- Consultation History Table (Center) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columnHeaders = {"Data Consultație", "Diagnostic (Sumar)", "Rețetă (Sumar)"};
        historyTableModel = new DefaultTableModel(columnHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        historyTable = new JTable(historyTableModel);
        historyTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        historyTable.getColumnModel().getColumn(0).setMinWidth(140); // Data
        // Adjust other column widths as needed

        JScrollPane scrollPane = new JScrollPane(historyTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // --- Selected Record Details Area (South of Table Panel) ---
        detailsArea = new JTextArea(7, 20); // Increased rows for more detail
        detailsArea.setEditable(false);
        detailsArea.setLineWrap(true);
        detailsArea.setWrapStyleWord(true);
        detailsArea.setFont(UIManager.getFont("Label.font"));
        JScrollPane detailsScrollPane = new JScrollPane(detailsArea);
        detailsScrollPane.setBorder(BorderFactory.createTitledBorder("Detalii Complete Consultație Selectată"));
        detailsScrollPane.setPreferredSize(new Dimension(detailsScrollPane.getPreferredSize().width, 150)); // Give it some height

        JPanel centerContentPanel = new JPanel(new BorderLayout(0, 10));
        centerContentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        centerContentPanel.add(tablePanel, BorderLayout.CENTER);
        centerContentPanel.add(detailsScrollPane, BorderLayout.SOUTH);
        add(centerContentPanel, BorderLayout.CENTER);

        // --- Close Button (South) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Închide");
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        closeButton.addActionListener(e -> dispose());

        historyTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && historyTable.getSelectedRow() != -1) {
                displaySelectedRecordDetails();
            } else if (historyTable.getSelectedRow() == -1) {
                detailsArea.setText("Selectați o consultație din istoric pentru detalii complete.");
            }
        });

        loadPatientHistory();
    }

    private List<ConsultationRecord> displayedRecords; // To store full objects

    private void loadPatientHistory() {
        historyTableModel.setRowCount(0); // Clear table
        detailsArea.setText("Selectați o consultație din istoric pentru detalii complete."); // Clear details

        displayedRecords = consultationRecordDAO.getConsultationRecordsForPatientWithDoctor(patient.getId(), currentViewingDoctor.getId());

        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy");

        if (displayedRecords.isEmpty()) {
            historyTableModel.addRow(new Object[]{"N/A", "Pacientul nu are istoric de consultații finalizate cu dvs.", ""});
        } else {
            for (ConsultationRecord record : displayedRecords) {
                String diagnosticSummary = record.getDiagnostic();
                if (diagnosticSummary != null && diagnosticSummary.length() > 70) {
                    diagnosticSummary = diagnosticSummary.substring(0, 67) + "...";
                }
                String prescriptionSummary = record.getPrescriptie();
                if (prescriptionSummary != null && prescriptionSummary.length() > 70) {
                    prescriptionSummary = prescriptionSummary.substring(0, 67) + "...";
                }

                historyTableModel.addRow(new Object[]{
                        record.getDataConsultatie() != null ? sdf.format(record.getDataConsultatie()) : "Dată Necunoscută",
                        diagnosticSummary != null ? diagnosticSummary : "N/A",
                        prescriptionSummary != null ? prescriptionSummary : "N/A"
                });
            }
        }
    }

    private void displaySelectedRecordDetails() {
        int selectedRow = historyTable.getSelectedRow();
        if (selectedRow >= 0 && displayedRecords != null && selectedRow < displayedRecords.size()) {
            ConsultationRecord record = displayedRecords.get(selectedRow);

            SimpleDateFormat sdfDateTime = new SimpleDateFormat("dd MMMM yyyy, HH:mm");
            SimpleDateFormat sdfDateOnly = new SimpleDateFormat("dd MMMM yyyy");
            Timestamp consultationDate = record.getDataConsultatie();

            StringBuilder detailsText = new StringBuilder();
            detailsText.append("Dată Consultație: ");
            if (consultationDate != null) {
                // Check if time part is significant (not midnight) before deciding format
                Calendar cal = Calendar.getInstance();
                cal.setTimeInMillis(consultationDate.getTime());
                if (cal.get(Calendar.HOUR_OF_DAY) == 0 && cal.get(Calendar.MINUTE) == 0 && cal.get(Calendar.SECOND) == 0) {
                    detailsText.append(sdfDateOnly.format(consultationDate));
                } else {
                    detailsText.append(sdfDateTime.format(consultationDate));
                }
            } else {
                detailsText.append("Nespecificată");
            }
            detailsText.append("\n\n");
            detailsText.append("Diagnostic Complet:\n");
            detailsText.append(record.getDiagnostic() != null && !record.getDiagnostic().isEmpty() ? record.getDiagnostic() : "N/A");
            detailsText.append("\n\n");
            detailsText.append("Rețetă / Indicații Medicale:\n");
            detailsText.append(record.getPrescriptie() != null && !record.getPrescriptie().isEmpty() ? record.getPrescriptie() : "N/A");

            detailsArea.setText(detailsText.toString());
            detailsArea.setCaretPosition(0); // Scroll to top
        } else {
            detailsArea.setText("Selectați o consultație din istoric pentru detalii complete.");
        }
    }
}