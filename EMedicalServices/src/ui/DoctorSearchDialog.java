package ui;

import dao.DoctorDAO;
import model.Doctor;
import model.Patient;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class DoctorSearchDialog extends JDialog {
    private Patient currentPatient;
    private DoctorDAO doctorDAO;
    private JTextField searchField;
    private JComboBox<String> specializationComboBox;
    private JButton searchButton;
    private JTable doctorsTable;
    private DefaultTableModel tableModel;
    private JButton viewProfileButton;
    private JButton bookAppointmentButton;

    public DoctorSearchDialog(Frame owner, Patient patient) {
        super(owner, "Caută Medici și Programează", true);
        this.currentPatient = patient;
        this.doctorDAO = new DoctorDAO();

        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10,10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // --- Search Panel (North) ---
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        searchPanel.add(new JLabel("Nume medic (parțial):"));
        searchField = new JTextField(15);
        searchPanel.add(searchField);

        searchPanel.add(new JLabel("Specializare:"));
        specializationComboBox = new JComboBox<>();
        loadSpecializations();
        searchPanel.add(specializationComboBox);

        searchButton = new JButton("Caută");
        searchPanel.add(searchButton);
        add(searchPanel, BorderLayout.NORTH);

        // --- Results Panel (Center) ---
        String[] columnNames = {"ID", "Nume", "Prenume", "Specializare", "Clinica (ID)"};
        tableModel = new DefaultTableModel(columnNames, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        doctorsTable = new JTable(tableModel);
        doctorsTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        doctorsTable.getColumnModel().getColumn(0).setMaxWidth(50);
        doctorsTable.getColumnModel().getColumn(0).setMinWidth(30);

        JScrollPane scrollPane = new JScrollPane(doctorsTable);
        add(scrollPane, BorderLayout.CENTER);

        // --- Actions Panel (South) ---
        JPanel actionsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        viewProfileButton = new JButton("Vezi Profil Medic");
        bookAppointmentButton = new JButton("Programează Consultație");
        actionsPanel.add(viewProfileButton);
        actionsPanel.add(bookAppointmentButton);
        add(actionsPanel, BorderLayout.SOUTH);

        // Action Listeners
        searchButton.addActionListener(e -> performSearch());
        viewProfileButton.addActionListener(e -> viewSelectedDoctorProfile());

        // MODIFIED ACTION for bookAppointmentButton
        bookAppointmentButton.addActionListener(e -> bookAppointmentWithSelectedDoctor());

        viewProfileButton.setEnabled(false);
        bookAppointmentButton.setEnabled(false);

        doctorsTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && doctorsTable.getSelectedRow() != -1) {
                viewProfileButton.setEnabled(true);
                bookAppointmentButton.setEnabled(true);
            } else if (doctorsTable.getSelectedRow() == -1) {
                viewProfileButton.setEnabled(false);
                bookAppointmentButton.setEnabled(false);
            }
        });
    }

    private void loadSpecializations() {
        specializationComboBox.addItem("Toate Specializările");
        specializationComboBox.addItem("Cardiologie");
        specializationComboBox.addItem("Dermatologie");
        specializationComboBox.addItem("Pediatrie");
        // ... add more ...
    }

    private void performSearch() {
        // ... (existing performSearch logic) ...
        String nameQuery = searchField.getText().trim();
        String selectedSpecialization = (String) specializationComboBox.getSelectedItem();

        tableModel.setRowCount(0);

        List<Doctor> doctors = doctorDAO.searchDoctors(nameQuery, selectedSpecialization);

        if (doctors.isEmpty() && (!nameQuery.isEmpty() || (selectedSpecialization != null && !"Toate Specializările".equals(selectedSpecialization)))) {
            JOptionPane.showMessageDialog(this, "Nu s-au găsit medici conform criteriilor.", "Căutare Fără Rezultate", JOptionPane.INFORMATION_MESSAGE);
        } else {
            for (Doctor doc : doctors) {
                Object[] row = { doc.getId(), doc.getNume(), doc.getPrenume(), doc.getSpecializarea(), doc.getClinicId() };
                tableModel.addRow(row);
            }
        }
        doctorsTable.clearSelection();
        viewProfileButton.setEnabled(false);
        bookAppointmentButton.setEnabled(false);
    }

    private void viewSelectedDoctorProfile() {
        // ... (existing viewSelectedDoctorProfile logic) ...
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow == -1) { /* ... */ return; }
        int doctorId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Doctor selectedDoctor = doctorDAO.getDoctorById(doctorId);
        if (selectedDoctor != null) {
            DoctorProfileDialog profileDialog = new DoctorProfileDialog(this, selectedDoctor);
            profileDialog.setVisible(true);
        } else { /* ... */ }
    }

    private void bookAppointmentWithSelectedDoctor() {
        int selectedRow = doctorsTable.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Vă rugăm selectați un medic din listă pentru programare.", "Niciun Medic Selectat", JOptionPane.WARNING_MESSAGE);
            return;
        }
        int doctorId = (Integer) tableModel.getValueAt(selectedRow, 0);
        Doctor selectedDoctor = doctorDAO.getDoctorById(doctorId);

        if (selectedDoctor != null) {
            AppointmentBookingDialog bookingDialog = new AppointmentBookingDialog(this, currentPatient, selectedDoctor); // 'this' is DoctorSearchDialog
            bookingDialog.setVisible(true);
        } else {
            JOptionPane.showMessageDialog(this, "Nu s-au putut încărca detaliile medicului pentru programare.", "Eroare", JOptionPane.ERROR_MESSAGE);
        }
    }

}