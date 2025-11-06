package src;

import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.print.*;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.imageio.ImageIO;
import javax.swing.*;

public class RecordDetailsDialog extends JDialog implements Printable {
    private Duplicator duplicator;
    private JLabel lblImagePreview;
    private JPanel detailsPanel;
    
    public RecordDetailsDialog(Frame owner, Duplicator duplicator) {
        super(owner, "Record Details - SN: " + duplicator.getDuplicatorId(), true);
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
        
    // SN
    addDetailRow(detailsPanel, gbc, row++, "SN:", String.valueOf(duplicator.getDuplicatorId()));
        
        // Name
        addDetailRow(detailsPanel, gbc, row++, "Name:", duplicator.getName());
        
        // Phone Number
        addDetailRow(detailsPanel, gbc, row++, "Phone Number:", duplicator.getPhoneNumber());
        
        // ID Number
        addDetailRow(detailsPanel, gbc, row++, "ID Number:", duplicator.getIdNo());
        
    // Key Type
    String keyType = duplicator.getKeyType();
    addDetailRow(detailsPanel, gbc, row++, "Key Type:", (keyType != null && !keyType.trim().isEmpty()) ? keyType : "N/A");

    // Vehicle Number
    addDetailRow(detailsPanel, gbc, row++, "Vehicle No:", (duplicator.getVehicleNo() != null && !duplicator.getVehicleNo().trim().isEmpty()) ? duplicator.getVehicleNo() : "N/A");

    // Key Number / Model
    addDetailRow(detailsPanel, gbc, row++, "Key No/Model:", duplicator.getKeyNo());
        
        // Purpose
        String purpose = duplicator.getPurpose();
        addDetailRow(detailsPanel, gbc, row++, "Purpose:", 
            (purpose != null && !purpose.trim().isEmpty()) ? purpose : "N/A");

        // Service Type
        ServiceTypeHelper.ServiceType serviceType = ServiceTypeHelper.detectServiceType(duplicator.getRemarks());
        addDetailRow(detailsPanel, gbc, row++, "Service Type:", serviceType.getDisplayName());
        
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
        String remarks = ServiceTypeHelper.stripServiceSuffix(duplicator.getRemarks());
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
        
    JButton btnExportCsv = new JButton("Export XLSX");
    btnExportCsv.setFont(new Font("Arial", Font.BOLD, 12));
    btnExportCsv.setBackground(new Color(109, 193, 210));
    btnExportCsv.setForeground(new Color(60, 62, 128));
    btnExportCsv.setFocusPainted(false);
    btnExportCsv.setPreferredSize(new Dimension(120, 32));
    btnExportCsv.setToolTipText("Export this record to XLSX (Excel)");
    btnExportCsv.addActionListener(e -> exportToXlsx());
        
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
        lblImagePreview.setIcon(null);
        lblImagePreview.setText("");

        String imagePath = duplicator.getImagePath();
        if (imagePath != null && !imagePath.trim().isEmpty()) {
            try {
                File imageFile = new File(imagePath);
                if (imageFile.exists()) {
                    BufferedImage img = ImageIO.read(imageFile);
                    Image scaledImg = img.getScaledInstance(240, 240, Image.SCALE_SMOOTH);
                    lblImagePreview.setIcon(new ImageIcon(scaledImg));
                    return;
                }
            } catch (Exception e) {
                // Fall through to placeholder
            }
        }

        ImageIcon placeholder = ImagePlaceholderHelper.loadRandomPlaceholder(240, 240);
        if (placeholder != null) {
            lblImagePreview.setIcon(placeholder);
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
        g2d.drawString("Customer Records", labelX, y);
        y += 30;
        
        // Draw line
        g2d.drawLine(labelX, y, 500, y);
        y += 20;
        
        SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");
        
        // Print details
    g2d.setFont(labelFont);
    g2d.drawString("SN:", labelX, y);
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
    g2d.drawString("Key Type:", labelX, y);
    g2d.setFont(valueFont);
    g2d.drawString((duplicator.getKeyType() != null ? duplicator.getKeyType() : "N/A"), valueX, y);
    y += lineHeight;

    g2d.setFont(labelFont);
    g2d.drawString("Vehicle No:", labelX, y);
    g2d.setFont(valueFont);
    g2d.drawString((duplicator.getVehicleNo() != null ? duplicator.getVehicleNo() : "N/A"), valueX, y);
    y += lineHeight;

    g2d.setFont(labelFont);
    g2d.drawString("Key No/Model:", labelX, y);
    g2d.setFont(valueFont);
    g2d.drawString(duplicator.getKeyNo(), valueX, y);
    y += lineHeight;
        
        String purpose = duplicator.getPurpose();
        if (purpose != null && !purpose.trim().isEmpty()) {
            g2d.setFont(labelFont);
            g2d.drawString("Purpose:", labelX, y);
            g2d.setFont(valueFont);
            g2d.drawString(purpose, valueX, y);
            y += lineHeight;
        }

        ServiceTypeHelper.ServiceType serviceTypeForPrint = ServiceTypeHelper.detectServiceType(duplicator.getRemarks());
        g2d.setFont(labelFont);
        g2d.drawString("Service Type:", labelX, y);
        g2d.setFont(valueFont);
        g2d.drawString(serviceTypeForPrint.getDisplayName(), valueX, y);
        y += lineHeight;
        
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
        
    String remarks = ServiceTypeHelper.stripServiceSuffix(duplicator.getRemarks());
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
    
    private void exportToXlsx() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("Export Record to XLSX");
        fileChooser.setSelectedFile(new File("record_" + duplicator.getDuplicatorId() + ".xlsx"));

        if (fileChooser.showSaveDialog(this) == JFileChooser.APPROVE_OPTION) {
            File file = fileChooser.getSelectedFile();
            String filePath = file.getAbsolutePath();

            if (!filePath.toLowerCase().endsWith(".xlsx")) {
                filePath += ".xlsx";
            }

            try {
                List<SimpleXlsxExporter.ColumnSpec> columns = Arrays.asList(
                    new SimpleXlsxExporter.ColumnSpec("Field", 30.0, SimpleXlsxExporter.CellType.STRING),
                    new SimpleXlsxExporter.ColumnSpec("Value", 60.0, SimpleXlsxExporter.CellType.STRING)
                );

                List<List<Object>> rows = new ArrayList<>();
                rows.add(Arrays.asList("SN", duplicator.getDuplicatorId()));
                rows.add(Arrays.asList("Name", duplicator.getName()));
                rows.add(Arrays.asList("Phone Number", duplicator.getPhoneNumber()));
                rows.add(Arrays.asList("ID Number", duplicator.getIdNo()));
                rows.add(Arrays.asList("Key Type", duplicator.getKeyType() != null ? duplicator.getKeyType() : "N/A"));
                rows.add(Arrays.asList("Vehicle No", duplicator.getVehicleNo() != null ? duplicator.getVehicleNo() : "N/A"));
                rows.add(Arrays.asList("Key No/Model", duplicator.getKeyNo()));

                String purpose = duplicator.getPurpose();
                rows.add(Arrays.asList("Purpose", purpose != null ? purpose : "N/A"));

                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
                String dateStr = (duplicator.getDateAdded() != null) ? dateFormat.format(duplicator.getDateAdded()) : "N/A";
                rows.add(Arrays.asList("Date Added", dateStr));

                rows.add(Arrays.asList("Quantity", duplicator.getQuantity()));
                rows.add(Arrays.asList("Amount", String.format("%.2f", duplicator.getAmount())));

                String remarks = duplicator.getRemarks();
                rows.add(Arrays.asList("Remarks", remarks != null ? remarks : ""));

                String imagePath = duplicator.getImagePath();
                rows.add(Arrays.asList("Image Path", imagePath != null ? imagePath : ""));

                SimpleXlsxExporter.export(filePath, "Record_" + duplicator.getDuplicatorId(), columns, rows, SimpleXlsxExporter.Orientation.AUTO);

                JOptionPane.showMessageDialog(this,
                    "Record exported successfully to:\n" + filePath,
                    "Export Successful",
                    JOptionPane.INFORMATION_MESSAGE);
            } catch (IOException e) {
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
            "- Key No/Model → deleted\n" +
            "- Purpose → deleted\n" +
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
