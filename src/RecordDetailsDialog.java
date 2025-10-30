package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import javax.imageio.ImageIO;
import javax.swing.*;

public class RecordDetailsDialog extends JDialog implements Printable {
    private Duplicator duplicator;
    private JLabel lblImagePreview;
    private JPanel detailsPanel;
    
    public RecordDetailsDialog(Frame owner, Duplicator duplicator) {
        super(owner, "Record Details - ID: " + duplicator.getDuplicatorId(), true);
        this.duplicator = duplicator;
        
        setSize(700, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout(10, 10));
        
        initComponents();
    }
    
    private void initComponents() {
        // Main content panel
        JPanel contentPanel = new JPanel(new BorderLayout(10, 10));
        contentPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        contentPanel.setBackground(Color.WHITE);
        
        // Left side - Details
        detailsPanel = new JPanel(new GridBagLayout());
        detailsPanel.setBackground(Color.WHITE);
        detailsPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Customer Information",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(60, 62, 128)
        ));
        
        GridBagConstraints gbc = new GridBagConstraints();
        gbc.insets = new Insets(8, 10, 8, 10);
        gbc.anchor = GridBagConstraints.WEST;
        gbc.fill = GridBagConstraints.HORIZONTAL;
        
        int row = 0;
        
        // ID
        addDetailRow(detailsPanel, gbc, row++, "Record ID:", String.valueOf(duplicator.getDuplicatorId()));
        
        // Name
        addDetailRow(detailsPanel, gbc, row++, "Name:", duplicator.getName());
        
        // Phone Number
        addDetailRow(detailsPanel, gbc, row++, "Phone Number:", duplicator.getPhoneNumber());
        
        // ID Number
        addDetailRow(detailsPanel, gbc, row++, "ID Number:", duplicator.getIdNo());
        
        // Key Number
        addDetailRow(detailsPanel, gbc, row++, "Key Number:", duplicator.getKeyNo());
        
        // Key For
        String keyFor = duplicator.getKeyType();
        addDetailRow(detailsPanel, gbc, row++, "Key For:", 
            (keyFor != null && !keyFor.trim().isEmpty()) ? keyFor : "N/A");
        
        // Date
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        String dateStr = (duplicator.getDateAdded() != null) 
            ? dateFormat.format(duplicator.getDateAdded()) : "N/A";
        addDetailRow(detailsPanel, gbc, row++, "Date Added:", dateStr);
        
        // Quantity
        addDetailRow(detailsPanel, gbc, row++, "Quantity:", String.valueOf(duplicator.getQuantity()));
        
        // Amount
        addDetailRow(detailsPanel, gbc, row++, "Amount:", 
            String.format("₹ %.2f", duplicator.getAmount()));
        
        // Remarks
        String remarks = duplicator.getRemarks();
        addDetailRow(detailsPanel, gbc, row++, "Remarks:", 
            (remarks != null && !remarks.trim().isEmpty()) ? remarks : "No remarks");
        
        JScrollPane detailsScrollPane = new JScrollPane(detailsPanel);
        detailsScrollPane.setPreferredSize(new Dimension(400, 500));
        detailsScrollPane.setBorder(null);
        
        // Right side - Image
        JPanel imagePanel = new JPanel(new BorderLayout(5, 5));
        imagePanel.setBackground(Color.WHITE);
        imagePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Customer Photo",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 14),
            new Color(60, 62, 128)
        ));
        
        lblImagePreview = new JLabel();
        lblImagePreview.setHorizontalAlignment(JLabel.CENTER);
        lblImagePreview.setVerticalAlignment(JLabel.CENTER);
        lblImagePreview.setPreferredSize(new Dimension(250, 250));
        lblImagePreview.setBackground(new Color(250, 250, 250));
        lblImagePreview.setOpaque(true);
        lblImagePreview.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 1),
            BorderFactory.createEmptyBorder(5, 5, 5, 5)
        ));
        
        // Load image if available
        loadImage();
        
        imagePanel.add(lblImagePreview, BorderLayout.CENTER);
        
        // Split pane
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, 
            detailsScrollPane, imagePanel);
        splitPane.setDividerLocation(400);
        splitPane.setResizeWeight(0.6);
        
        contentPanel.add(splitPane, BorderLayout.CENTER);
        
        // Buttons panel
        JPanel buttonsPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonsPanel.setBackground(Color.WHITE);
        
        JButton btnEdit = new JButton("Edit Details");
        btnEdit.setFont(new Font("Arial", Font.BOLD, 12));
        btnEdit.setBackground(new Color(109, 193, 210));
        btnEdit.setForeground(new Color(60, 62, 128));
        btnEdit.setFocusPainted(false);
        btnEdit.setPreferredSize(new Dimension(120, 32));
        btnEdit.setToolTipText("Edit this record");
        btnEdit.addActionListener(e -> editRecord());
        
        JButton btnDelete = new JButton("Delete Data");
        btnDelete.setFont(new Font("Arial", Font.BOLD, 12));
        btnDelete.setBackground(new Color(220, 80, 80));
        btnDelete.setForeground(Color.RED);
        btnDelete.setFocusPainted(false);
        btnDelete.setPreferredSize(new Dimension(120, 32));
        btnDelete.setToolTipText("Delete all data except Name and ID");
        btnDelete.addActionListener(e -> deleteRecordData());
        
        JButton btnPrint = new JButton("Print");
        btnPrint.setFont(new Font("Arial", Font.BOLD, 12));
        btnPrint.setBackground(new Color(109, 193, 210));
        btnPrint.setForeground(new Color(60, 62, 128));
        btnPrint.setFocusPainted(false);
        btnPrint.setPreferredSize(new Dimension(100, 32));
        btnPrint.setIcon(UIManager.getIcon("FileView.hardDriveIcon"));
        btnPrint.setToolTipText("Print this record");
        btnPrint.addActionListener(e -> printRecord());
        
        JButton btnExportCsv = new JButton("Export CSV");
        btnExportCsv.setFont(new Font("Arial", Font.BOLD, 12));
        btnExportCsv.setBackground(new Color(109, 193, 210));
        btnExportCsv.setForeground(new Color(60, 62, 128));
        btnExportCsv.setFocusPainted(false);
        btnExportCsv.setPreferredSize(new Dimension(120, 32));
        btnExportCsv.setToolTipText("Export this record to CSV");
        btnExportCsv.addActionListener(e -> exportToCsv());
        
        JButton btnClose = new JButton("Close");
        btnClose.setFont(new Font("Arial", Font.BOLD, 12));
        btnClose.setBackground(new Color(240, 240, 240));
        btnClose.setForeground(new Color(60, 62, 128));
        btnClose.setFocusPainted(false);
        btnClose.setPreferredSize(new Dimension(100, 32));
        btnClose.addActionListener(e -> dispose());
        
        buttonsPanel.add(btnEdit);
        buttonsPanel.add(btnDelete);
        buttonsPanel.add(btnPrint);
        buttonsPanel.add(btnExportCsv);
        buttonsPanel.add(btnClose);
        
        contentPanel.add(buttonsPanel, BorderLayout.SOUTH);
        
        add(contentPanel);
    }
    
    private void addDetailRow(JPanel panel, GridBagConstraints gbc, int row, 
                              String label, String value) {
        // Label
        gbc.gridx = 0;
        gbc.gridy = row;
        gbc.weightx = 0.0;
        JLabel lblField = new JLabel(label);
        lblField.setFont(new Font("Arial", Font.BOLD, 13));
        lblField.setForeground(new Color(60, 62, 128));
        panel.add(lblField, gbc);
        
        // Value
        gbc.gridx = 1;
        gbc.weightx = 1.0;
        JLabel lblValue = new JLabel(value);
        lblValue.setFont(new Font("Arial", Font.PLAIN, 13));
        lblValue.setForeground(new Color(50, 50, 50));
        panel.add(lblValue, gbc);
    }
    
    private void loadImage() {
        String imagePath = duplicator.getImagePath();
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    BufferedImage img = ImageIO.read(imageFile);
                    Image scaledImg = img.getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(scaledImg));
                    lblImagePreview.setText("");
                } else {
                    lblImagePreview.setText("<html><center>Image file<br>not found</center></html>");
                }
            } catch (Exception e) {
                lblImagePreview.setText("<html><center>Error loading<br>image</center></html>");
            }
        } else {
            lblImagePreview.setText("<html><center>No image<br>available</center></html>");
        }
    }
    
    private void printRecord() {
        PrinterJob job = PrinterJob.getPrinterJob();
        job.setPrintable(this);
        
        boolean doPrint = job.printDialog();
        if (doPrint) {
            try {
                job.print();
                JOptionPane.showMessageDialog(this,
                    "Record sent to printer successfully!",
                    "Print Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (PrinterException e) {
                JOptionPane.showMessageDialog(this,
                    "Error printing record: " + e.getMessage(),
                    "Print Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    @Override
    public int print(Graphics g, PageFormat pf, int page) throws PrinterException {
        if (page > 0) {
            return NO_SUCH_PAGE;
        }
        
        Graphics2D g2d = (Graphics2D) g;
        g2d.translate(pf.getImageableX(), pf.getImageableY());
        
        // Set font
        Font titleFont = new Font("Arial", Font.BOLD, 16);
        Font labelFont = new Font("Arial", Font.BOLD, 11);
        Font valueFont = new Font("Arial", Font.PLAIN, 11);
        
        int y = 50;
        int labelX = 50;
        int valueX = 200;
        int lineHeight = 25;
        
        // Title
        g2d.setFont(titleFont);
        g2d.drawString("MODERN AUTOMOBILES - Customer Record", labelX, y);
        y += 30;
        
        // Draw line
        g2d.drawLine(labelX, y, 500, y);
        y += 20;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        
        // Print details
        g2d.setFont(labelFont);
        g2d.drawString("Record ID:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(String.valueOf(duplicator.getDuplicatorId()), valueX, y);
        y += lineHeight;
        
        g2d.setFont(labelFont);
        g2d.drawString("Name:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(duplicator.getName(), valueX, y);
        y += lineHeight;
        
        g2d.setFont(labelFont);
        g2d.drawString("Phone Number:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(duplicator.getPhoneNumber(), valueX, y);
        y += lineHeight;
        
        g2d.setFont(labelFont);
        g2d.drawString("ID Number:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(duplicator.getIdNo(), valueX, y);
        y += lineHeight;
        
        g2d.setFont(labelFont);
        g2d.drawString("Key Number:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(duplicator.getKeyNo(), valueX, y);
        y += lineHeight;
        
        String keyFor = duplicator.getKeyType();
        if (keyFor != null && !keyFor.trim().isEmpty()) {
            g2d.setFont(labelFont);
            g2d.drawString("Key For:", labelX, y);
            g2d.setFont(valueFont);
            g2d.drawString(keyFor, valueX, y);
            y += lineHeight;
        }
        
        g2d.setFont(labelFont);
        g2d.drawString("Date Added:", labelX, y);
        g2d.setFont(valueFont);
        String dateStr = (duplicator.getDateAdded() != null) 
            ? dateFormat.format(duplicator.getDateAdded()) : "N/A";
        g2d.drawString(dateStr, valueX, y);
        y += lineHeight;
        
        g2d.setFont(labelFont);
        g2d.drawString("Quantity:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(String.valueOf(duplicator.getQuantity()), valueX, y);
        y += lineHeight;
        
        g2d.setFont(labelFont);
        g2d.drawString("Amount:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(String.format("₹ %.2f", duplicator.getAmount()), valueX, y);
        y += lineHeight;
        
        String remarks = duplicator.getRemarks();
        if (remarks != null && !remarks.trim().isEmpty()) {
            g2d.setFont(labelFont);
            g2d.drawString("Remarks:", labelX, y);
            g2d.setFont(valueFont);
            // Handle long remarks
            if (remarks.length() > 50) {
                remarks = remarks.substring(0, 50) + "...";
            }
            g2d.drawString(remarks, valueX, y);
            y += lineHeight;
        }
        
        // Print image if available
        String imagePath = duplicator.getImagePath();
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    BufferedImage img = ImageIO.read(imageFile);
                    Image scaledImg = img.getScaledInstance(150, 150, Image.SCALE_SMOOTH);
                    g2d.drawImage(scaledImg, 400, 100, null);
                }
            } catch (Exception e) {
                // Skip image if error
            }
        }
        
        // Footer
        y = (int) pf.getImageableHeight() - 30;
        g2d.setFont(new Font("Arial", Font.ITALIC, 9));
        g2d.drawString("Printed on: " + new SimpleDateFormat("dd-MMM-yyyy HH:mm").format(new java.util.Date()), 
            labelX, y);
        
        return PAGE_EXISTS;
    }
    
    private void exportToCsv() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Record to CSV");
        fileChooser.setSelectedFile(new File("record_" + duplicator.getDuplicatorId() + ".csv"));
        
        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();
            
            if (!filePath.toLowerCase().endsWith(".csv")) {
                filePath += ".csv";
            }
            
            try (FileWriter writer = new FileWriter(filePath)) {
                // Write header
                writer.append("Field,Value\n");
                
                // Write data
                writer.append("Record ID,").append(String.valueOf(duplicator.getDuplicatorId())).append("\n");
                writer.append("Name,").append(escapeCsv(duplicator.getName())).append("\n");
                writer.append("Phone Number,").append(escapeCsv(duplicator.getPhoneNumber())).append("\n");
                writer.append("ID Number,").append(escapeCsv(duplicator.getIdNo())).append("\n");
                writer.append("Key Number,").append(escapeCsv(duplicator.getKeyNo())).append("\n");
                
                String keyFor = duplicator.getKeyType();
                writer.append("Key For,").append(escapeCsv(keyFor != null ? keyFor : "N/A")).append("\n");
                
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = (duplicator.getDateAdded() != null) 
                    ? dateFormat.format(duplicator.getDateAdded()) : "N/A";
                writer.append("Date Added,").append(dateStr).append("\n");
                
                writer.append("Quantity,").append(String.valueOf(duplicator.getQuantity())).append("\n");
                writer.append("Amount,").append(String.format("%.2f", duplicator.getAmount())).append("\n");
                
                String remarks = duplicator.getRemarks();
                writer.append("Remarks,").append(escapeCsv(remarks != null ? remarks : "")).append("\n");
                
                String imagePath = duplicator.getImagePath();
                writer.append("Image Path,").append(escapeCsv(imagePath != null ? imagePath : "")).append("\n");
                
                writer.flush();
                
                JOptionPane.showMessageDialog(this,
                    "Record exported successfully to:\n" + filePath,
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
                    
            } catch (Exception e) {
                JOptionPane.showMessageDialog(this,
                    "Error exporting record: " + e.getMessage(),
                    "Export Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private String escapeCsv(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
    
    private void editRecord() {
        EditRecordDialog editDialog = new EditRecordDialog((Frame) getOwner(), duplicator);
        editDialog.setVisible(true);
        
        if (editDialog.isSaved()) {
            // Refresh the dialog with updated data
            dispose();
            
            // Refresh parent window
            Window owner = getOwner();
            if (owner instanceof JFrame) {
                JFrame frame = (JFrame) owner;
                String className = frame.getClass().getSimpleName();
                if ("MainForm".equals(className)) {
                    // Trigger refresh of MainForm table
                    frame.repaint();
                    frame.revalidate();
                } else if ("SearchWindow".equals(className)) {
                    // Trigger refresh of SearchWindow
                    frame.repaint();
                    frame.revalidate();
                }
            }
        }
    }
    
    private void deleteRecordData() {
        int response = JOptionPane.showConfirmDialog(this,
            "Are you sure you want to delete all data except Name and ID?\n\n" +
            "All fields will be set to 'deleted':\n" +
            "- Phone Number → deleted\n" +
            "- Vehicle Type → deleted\n" +
            "- Vehicle No → deleted\n" +
            "- ID Number → deleted\n" +
            "- Key Number → deleted\n" +
            "- Key For → deleted\n" +
            "- Remarks → deleted\n" +
            "- Date → cleared\n" +
            "- Quantity → 0\n" +
            "- Amount → 0.00\n" +
            "- Captured Image → removed\n\n" +
            "Name and ID will be preserved.",
            "Delete Record Data",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.WARNING_MESSAGE);
            
        if (response == JOptionPane.YES_OPTION) {
            // Delete the image file if it exists
            String imagePath = duplicator.getImagePath();
            if (imagePath != null && !imagePath.trim().isEmpty()) {
                try {
                    File imageFile = new File(imagePath);
                    if (imageFile.exists()) {
                        imageFile.delete();
                    }
                } catch (Exception e) {
                    // Ignore file deletion errors
                }
            }
            
            // Clear the data in database
            if (duplicator.clearDataExceptNameAndId()) {
                JOptionPane.showMessageDialog(this,
                    "Record data marked as 'deleted' successfully!\n" +
                    "Name and ID have been preserved.",
                    "Success",
                    JOptionPane.INFORMATION_MESSAGE);
                dispose();
                
                // Refresh parent window if it's MainForm
                Window owner = getOwner();
                if (owner instanceof JFrame) {
                    JFrame frame = (JFrame) owner;
                    if (frame.getClass().getSimpleName().equals("MainForm")) {
                        // Trigger a refresh of the table
                        frame.repaint();
                    } else if (frame.getClass().getSimpleName().equals("SearchWindow")) {
                        frame.repaint();
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this,
                    "Failed to clear record data.",
                    "Error",
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
}
