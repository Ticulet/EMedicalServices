package ui;

import model.Doctor;
import model.Feedback;
import model.Patient; // To display patient names
import dao.FeedbackDAO;
import dao.PatientDAO; // To get patient names

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.text.SimpleDateFormat;
import java.util.HashMap; // For patient name cache
import java.util.List;
import java.util.Map;

public class ViewDoctorFeedbackDialog extends JDialog {
    private Doctor currentDoctor;
    private FeedbackDAO feedbackDAO;
    private PatientDAO patientDAO; // To resolve patient IDs to names

    private JTable feedbackTable;
    private DefaultTableModel feedbackTableModel;
    private JTextArea selectedFeedbackCommentArea; // To show full comment
    private JLabel averageRatingLabel;

    private Map<Integer, String> patientNameCache; // Cache patient names

    public ViewDoctorFeedbackDialog(Frame owner, Doctor doctor) {
        super(owner, "Feedback Primit - Dr. " + doctor.getPrenume() + " " + doctor.getNume(), true);
        this.currentDoctor = doctor;
        this.feedbackDAO = new FeedbackDAO();
        this.patientDAO = new PatientDAO();
        this.patientNameCache = new HashMap<>();

        setSize(700, 500);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        // --- Average Rating Panel (North) ---
        JPanel ratingPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        ratingPanel.setBorder(BorderFactory.createEmptyBorder(5,5,0,5));
        averageRatingLabel = new JLabel("Rating Mediu: Calculare...");
        averageRatingLabel.setFont(averageRatingLabel.getFont().deriveFont(Font.BOLD));
        ratingPanel.add(averageRatingLabel);
        add(ratingPanel, BorderLayout.NORTH);


        // --- Feedback Table Panel (Center) ---
        JPanel tablePanel = new JPanel(new BorderLayout());
        String[] columnHeaders = {"Data Feedback", "Pacient", "Notă", "Comentariu (Sumar)"};
        feedbackTableModel = new DefaultTableModel(columnHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) { return false; }
        };
        feedbackTable = new JTable(feedbackTableModel);
        feedbackTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        feedbackTable.getColumnModel().getColumn(0).setMinWidth(140); // Data
        feedbackTable.getColumnModel().getColumn(1).setMinWidth(120); // Pacient
        feedbackTable.getColumnModel().getColumn(2).setMaxWidth(60);  // Nota

        JScrollPane scrollPane = new JScrollPane(feedbackTable);
        tablePanel.add(scrollPane, BorderLayout.CENTER);

        // --- Selected Feedback Comment Area (South of Table Panel) ---
        selectedFeedbackCommentArea = new JTextArea(5, 20);
        selectedFeedbackCommentArea.setEditable(false);
        selectedFeedbackCommentArea.setLineWrap(true);
        selectedFeedbackCommentArea.setWrapStyleWord(true);
        selectedFeedbackCommentArea.setFont(UIManager.getFont("Label.font"));
        JScrollPane commentScrollPane = new JScrollPane(selectedFeedbackCommentArea);
        commentScrollPane.setBorder(BorderFactory.createTitledBorder("Comentariu Complet Selectat"));
        commentScrollPane.setPreferredSize(new Dimension(commentScrollPane.getPreferredSize().width, 120));

        // Add tablePanel and commentScrollPane to a main center panel
        JPanel centerContentPanel = new JPanel(new BorderLayout(0,10));
        centerContentPanel.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        centerContentPanel.add(tablePanel, BorderLayout.CENTER);
        centerContentPanel.add(commentScrollPane, BorderLayout.SOUTH);
        add(centerContentPanel, BorderLayout.CENTER);


        // --- Close Button (South) ---
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        JButton closeButton = new JButton("Închide");
        buttonPanel.add(closeButton);
        add(buttonPanel, BorderLayout.SOUTH);

        // Listeners
        closeButton.addActionListener(e -> dispose());

        feedbackTable.getSelectionModel().addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting() && feedbackTable.getSelectedRow() != -1) {
                displaySelectedFeedbackComment();
            } else if (feedbackTable.getSelectedRow() == -1) {
                selectedFeedbackCommentArea.setText(""); // Clear if no selection
            }
        });

        loadFeedback();
    }

    private void loadFeedback() {
        feedbackTableModel.setRowCount(0); // Clear table
        patientNameCache.clear(); // Clear cache
        selectedFeedbackCommentArea.setText(""); // Clear comment area

        List<Feedback> feedbackList = feedbackDAO.getFeedbackForDoctor(currentDoctor.getId());
        float avgRating = feedbackDAO.getAverageRatingForDoctor(currentDoctor.getId());

        if (avgRating > 0) {
            averageRatingLabel.setText(String.format("Rating Mediu: %.1f / 5", avgRating));
        } else {
            averageRatingLabel.setText("Rating Mediu: N/A (Nu există încă evaluări)");
        }


        SimpleDateFormat sdf = new SimpleDateFormat("dd MMMM yyyy, HH:mm");

        if (feedbackList.isEmpty()) {
            feedbackTableModel.addRow(new Object[]{"N/A", "Nu ați primit încă feedback.", "", ""});
        } else {
            for (Feedback fb : feedbackList) {
                String patientName = patientNameCache.computeIfAbsent(fb.getPatientId(), pId -> {
                    Patient p = patientDAO.getPatientById(pId);
                    return p != null ? p.getPrenume() + " " + p.getNume() : "Pacient Anonim";
                });
                String commentSummary = fb.getComentariu();
                if (commentSummary != null && commentSummary.length() > 70) {
                    commentSummary = commentSummary.substring(0, 67) + "...";
                }
                feedbackTableModel.addRow(new Object[]{
                        sdf.format(fb.getDataFeedback()),
                        patientName,
                        fb.getNota() + " \u2605", // Unicode star for rating
                        commentSummary != null ? commentSummary : ""
                });
            }
        }
    }

    private void displaySelectedFeedbackComment() {
        int selectedRow = feedbackTable.getSelectedRow();
        if (selectedRow != -1) {

            List<Feedback> feedbackList = feedbackDAO.getFeedbackForDoctor(currentDoctor.getId()); // Re-fetch for simplicity
            if (selectedRow < feedbackList.size()) {
                Feedback selectedFb = feedbackList.get(selectedRow); // Assumes table order matches list order
                selectedFeedbackCommentArea.setText(selectedFb.getComentariu() != null ? selectedFb.getComentariu() : "Niciun comentariu.");
                selectedFeedbackCommentArea.setCaretPosition(0); // Scroll to top
            } else {
                selectedFeedbackCommentArea.setText("Eroare la afișarea comentariului.");
            }
        } else {
            selectedFeedbackCommentArea.setText("");
        }
    }
}