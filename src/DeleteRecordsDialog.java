package src;

import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Date;
import javax.swing.*;

public class DeleteRecordsDialog extends JDialog {
    private JDateChooser fromDateChooser;
    private JDateChooser toDateChooser;
    private JButton btnDelete;
    private JButton btnCancel;
    private MainForm parentForm;
    
    public DeleteRecordsDialog(MainForm parent) {
        super(parent, "Delete Records by Date Range", true);
        this.parentForm = parent;
        initComponents();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        setSize(450, 250);
        setLocationRelativeTo(parentForm);
        
        // Main panel
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        mainPanel.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        // From Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.weightx = 0.0;
        JLabel lblFromDate = new JLabel("From Date:");
        lblFromDate.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblFromDate, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        fromDateChooser = new JDateChooser();
        fromDateChooser.setFont(new Font("Arial", Font.PLAIN, 12));
        mainPanel.add(fromDateChooser, gbc);
        
        // To Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel lblToDate = new JLabel("To Date:");
        lblToDate.setFont(new Font("Arial", Font.BOLD, 12));
        mainPanel.add(lblToDate, gbc);
        
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        toDateChooser = new JDateChooser();
        toDateChooser.setFont(new Font("Arial", Font.PLAIN, 12));
        toDateChooser.setDate(new Date()); // Default to today
        mainPanel.add(toDateChooser, gbc);
        
        // Warning label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        JLabel lblWarning = new JLabel("<html><i>⚠ This action cannot be undone!</i></html>");
        lblWarning.setFont(new Font("Arial", Font.PLAIN, 11));
        lblWarning.setForeground(Color.RED);
        mainPanel.add(lblWarning, gbc);
        
        add(mainPanel, BorderLayout.CENTER);
        
        // Button panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(0, 10, 10, 10));
        
        btnDelete = new JButton("Delete Records");
        btnDelete.setBackground(new Color(220, 53, 69));
        btnDelete.setForeground(Color.RED);
        btnDelete.setFont(new Font("Arial", Font.BOLD, 12));
        btnDelete.setFocusPainted(false);
        btnDelete.addActionListener(e -> handleDelete());
        
        btnCancel = new JButton("Cancel");
        btnCancel.setFont(new Font("Arial", Font.PLAIN, 12));
        btnCancel.addActionListener(e -> dispose());
        
        buttonPanel.add(btnDelete);
        buttonPanel.add(btnCancel);
        add(buttonPanel, BorderLayout.SOUTH);
    }
    
    private void handleDelete() {
        Date fromDate = fromDateChooser.getDate();
        Date toDate = toDateChooser.getDate();
        
        if (fromDate == null || toDate == null) {
            ModernDialog.showWarning(this,
                "Please select both From Date and To Date.",
                "Missing Dates");
            return;
        }
        
        if (fromDate.after(toDate)) {
            ModernDialog.showWarning(this,
                "From Date must be before or equal to To Date.",
                "Invalid Date Range");
            return;
        }
        
        // Require typing "confirm" to delete
        SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");
        
        JPanel confirmPanel = new JPanel(new GridBagLayout());
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.gridwidth = GridBagConstraints.REMAINDER;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        JLabel warningLabel = new JLabel("<html><b>⚠ WARNING!</b><br>" +
            "You are about to delete all records from <b>" + sdf.format(fromDate) + "</b>" +
            " to <b>" + sdf.format(toDate) + "</b>.<br><br>" +
            "This action cannot be undone!<br><br>" +
            "Type <b>confirm</b> below to proceed:</html>");
        warningLabel.setForeground(new Color(220, 53, 69));
        confirmPanel.add(warningLabel, gbc);
        
        JTextField confirmField = new JTextField(20);
        confirmField.setFont(new Font("Arial", Font.PLAIN, 12));
        gbc.gridy = 1;
        confirmPanel.add(confirmField, gbc);
        
        int result = JOptionPane.showConfirmDialog(this,
            confirmPanel,
            "Confirm Deletion",
            JOptionPane.OK_CANCEL_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (result == JOptionPane.OK_OPTION) {
            String confirmText = confirmField.getText().trim();
            
            if (confirmText.equals("confirm")) {
                int deletedCount = deleteRecordsByDateRange(fromDate, toDate);
                if (deletedCount >= 0) {
                    ModernDialog.showInfo(this,
                        deletedCount + " record(s) deleted successfully.",
                        "Success");
                    parentForm.refreshTable(); // Refresh the parent table
                    dispose();
                }
            } else {
                ModernDialog.showError(this,
                    "Deletion cancelled. You must type 'confirm' exactly to proceed.",
                    "Incorrect Confirmation");
            }
        }
    }
    
    private int deleteRecordsByDateRange(Date fromDate, Date toDate) {
        String sql = "DELETE FROM duplicator WHERE date_added BETWEEN ? AND ?";
        
        try (Connection conn = DatabaseConnection.getConnection();
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            
            pstmt.setDate(1, new java.sql.Date(fromDate.getTime()));
            pstmt.setDate(2, new java.sql.Date(toDate.getTime()));
            
            int deletedCount = pstmt.executeUpdate();
            return deletedCount;
            
        } catch (SQLException e) {
            ModernDialog.showError(this,
                "Error deleting records: " + e.getMessage(),
                "Database Error");
            return -1;
        }
    }
}
