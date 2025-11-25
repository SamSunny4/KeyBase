package src;

import javax.swing.*;
import java.awt.*;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;

/**
 * Modal dialog that prompts the user for a start and end date to use when exporting records.
 */
public class ExportDateRangeDialog extends JDialog {
    private final JDateChooser startChooser = new JDateChooser();
    private final JDateChooser endChooser = new JDateChooser();
    private final JLabel countLabel = new JLabel("Calculating...");
    private final JButton okButton = new JButton("Export");
    private boolean confirmed = false;

    public ExportDateRangeDialog(Frame owner) {
        super(owner, "Select Date Range", true);
        init();
    }

    public ExportDateRangeDialog(Dialog owner) {
        super(owner, "Select Date Range", true);
        init();
    }

    private void init() {
        setLayout(new BorderLayout(0, 0));
        setBackground(Color.WHITE);
        
        JPanel mainPanel = new JPanel(new GridBagLayout());
        mainPanel.setBackground(Color.WHITE);
        mainPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 10, 20));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.anchor = GridBagConstraints.WEST;
        
        // Start Date
        gbc.gridx = 0;
        gbc.gridy = 0;
        JLabel startLabel = new JLabel("Start Date:");
        startLabel.setFont(new Font("Arial", Font.BOLD, 12));
        startLabel.setForeground(new Color(60, 62, 128));
        mainPanel.add(startLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        startChooser.setDate(new Date());
        mainPanel.add(startChooser, gbc);

        // End Date
        gbc.gridx = 0;
        gbc.gridy = 1;
        gbc.weightx = 0.0;
        JLabel endLabel = new JLabel("End Date:");
        endLabel.setFont(new Font("Arial", Font.BOLD, 12));
        endLabel.setForeground(new Color(60, 62, 128));
        mainPanel.add(endLabel, gbc);

        gbc.gridx = 1;
        gbc.weightx = 1.0;
        endChooser.setDate(new Date());
        mainPanel.add(endChooser, gbc);
        
        // Count Label
        gbc.gridx = 0;
        gbc.gridy = 2;
        gbc.gridwidth = 2;
        gbc.insets = new Insets(15, 5, 5, 5);
        countLabel.setFont(new Font("Arial", Font.BOLD, 13));
        countLabel.setForeground(new Color(60, 62, 128));
        mainPanel.add(countLabel, gbc);

        add(mainPanel, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(230, 230, 230)));

        okButton.setFont(new Font("Arial", Font.BOLD, 12));
        okButton.setBackground(new Color(109, 193, 210));
        okButton.setForeground(new Color(60, 62, 128));
        okButton.setFocusPainted(false);
        okButton.addActionListener(e -> {
            if (validateRange()) {
                confirmed = true;
                dispose();
            }
        });

        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(240, 240, 240));
        cancelButton.setForeground(new Color(60, 62, 128));
        cancelButton.setFocusPainted(false);
        cancelButton.addActionListener(e -> {
            confirmed = false;
            dispose();
        });

        buttonPanel.add(okButton);
        buttonPanel.add(cancelButton);

        add(buttonPanel, BorderLayout.SOUTH);

        // Add listeners for date changes
        startChooser.addPropertyChangeListener("date", e -> updateRecordCount());
        endChooser.addPropertyChangeListener("date", e -> updateRecordCount());
        
        // Initial update
        updateRecordCount();

        pack();
        setLocationRelativeTo(getOwner());
    }

    private void updateRecordCount() {
        Date start = startChooser.getDate();
        Date end = endChooser.getDate();
        
        if (start != null && end != null) {
            if (end.before(start)) {
                countLabel.setText("<html><font color='red'>Invalid range: End date before start date</font></html>");
                okButton.setEnabled(false);
            } else {
                int count = Duplicator.countRecordsByDateRange(start, end);
                countLabel.setText("Records found: " + count);
                okButton.setEnabled(true);
            }
        } else {
            countLabel.setText("Select dates to see record count");
            okButton.setEnabled(false);
        }
    }

    private boolean validateRange() {
        Date start = startChooser.getDate();
        Date end = endChooser.getDate();
        if (start == null || end == null) {
            ModernDialog.showWarning(this,
                "Select both a start and end date.",
                "Validation Error");
            return false;
        }
        if (end.before(start)) {
            ModernDialog.showWarning(this,
                "End date must be on or after the start date.",
                "Validation Error");
            return false;
        }
        return true;
    }

    public boolean isConfirmed() {
        return confirmed;
    }

    public java.sql.Date getStartDate() {
        return toSqlDate(startChooser.getDate());
    }

    public java.sql.Date getEndDate() {
        return toSqlDate(endChooser.getDate());
    }

    private java.sql.Date toSqlDate(Date date) {
        if (date == null) {
            return java.sql.Date.valueOf(LocalDate.now());
        }
        LocalDate local = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        return java.sql.Date.valueOf(local);
    }
}
