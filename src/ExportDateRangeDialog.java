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
        setLayout(new BorderLayout(10, 10));
        setBackground(Color.WHITE);
        JPanel content = new JPanel();
        content.setLayout(new GridBagLayout());
        content.setBackground(Color.WHITE);
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(5, 5, 5, 5);
        gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridx = 0;
        gbc.gridy = 0;

        JLabel startLabel = new JLabel("Start date:");
        startLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        startLabel.setForeground(new Color(60, 62, 128));
        content.add(startLabel, gbc);

        gbc.gridx = 1;
        startChooser.setDate(new Date());
        content.add(startChooser, gbc);

        gbc.gridx = 0;
        gbc.gridy = 1;
        JLabel endLabel = new JLabel("End date:");
        endLabel.setFont(new Font("Arial", Font.PLAIN, 12));
        endLabel.setForeground(new Color(60, 62, 128));
        content.add(endLabel, gbc);

        gbc.gridx = 1;
        endChooser.setDate(new Date());
        content.add(endChooser, gbc);

        add(content, BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);

        JButton okButton = new JButton("OK");
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

        pack();
        setLocationRelativeTo(getOwner());
    }

    private boolean validateRange() {
        Date start = startChooser.getDate();
        Date end = endChooser.getDate();
        if (start == null || end == null) {
            JOptionPane.showMessageDialog(this,
                "Select both a start and end date.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }
        if (end.before(start)) {
            JOptionPane.showMessageDialog(this,
                "End date must be on or after the start date.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
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
