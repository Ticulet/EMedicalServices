package main; // Package declaration

import ui.LoginFrame; // Correct import for your structure

import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException; // More specific import

public class App {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (ClassNotFoundException | InstantiationException | IllegalAccessException | UnsupportedLookAndFeelException e) {
                System.err.println("Failed to set system Look and Feel: " + e.getMessage());
            }
            new LoginFrame().setVisible(true);
        });
    }
}