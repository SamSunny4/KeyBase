package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import com.github.sarxos.webcam.Webcam;

public class PreferencesDialog extends JDialog {
    private JComboBox<String> webcamSelector;
    private JTextField imagePathField;
    private JCheckBox disableCameraCheckBox;
    private JRadioButton orientationAutoRadio;
    private JRadioButton orientationPortraitRadio;
    private JRadioButton orientationLandscapeRadio;
    private JCheckBox useCustomDbCheckBox;
    private JTextField customDbPathField;
    private JButton dbBrowseButton;
    private final Map<ExportField, JCheckBox> fieldCheckBoxes = new LinkedHashMap<>();
    private boolean settingsChanged = false;

    public PreferencesDialog(JFrame parent) {
        super(parent, "Preferences", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(560, 620);
        setLocationRelativeTo(null);

        JPanel mainPanel = new JPanel();
        mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
        mainPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        mainPanel.setBackground(Color.WHITE);

        // Webcam Device Selection
        JPanel webcamPanel = new JPanel(new BorderLayout(5, 0));
        webcamPanel.setBackground(Color.WHITE);
        webcamPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Webcam Device",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(60, 62, 128)
        ));

    webcamSelector = new JComboBox<>();
    webcamSelector.setFont(new Font("Arial", Font.PLAIN, 12));
    loadWebcamDevices();
    webcamPanel.add(webcamSelector, BorderLayout.CENTER);

    disableCameraCheckBox = new JCheckBox("Disable camera features");
    disableCameraCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
    disableCameraCheckBox.setBackground(Color.WHITE);
    disableCameraCheckBox.setForeground(new Color(60, 62, 128));
    disableCameraCheckBox.setSelected(AppConfig.isCameraDisabled());
    disableCameraCheckBox.addActionListener(e -> webcamSelector.setEnabled(!disableCameraCheckBox.isSelected()));
    webcamPanel.add(disableCameraCheckBox, BorderLayout.SOUTH);
    webcamSelector.setEnabled(!disableCameraCheckBox.isSelected());

        // Image Storage Location
        JPanel imagePathPanel = new JPanel(new BorderLayout(5, 0));
        imagePathPanel.setBackground(Color.WHITE);
        imagePathPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Image Storage Location",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(60, 62, 128)
        ));

        imagePathField = new JTextField(AppConfig.getImagesDirectory());
        imagePathField.setFont(new Font("Arial", Font.PLAIN, 12));
        imagePathPanel.add(imagePathField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse...");
        browseButton.setFont(new Font("Arial", Font.BOLD, 11));
        browseButton.setBackground(new Color(109, 193, 210));
        browseButton.setForeground(new Color(60, 62, 128));
        browseButton.setFocusPainted(false);
        browseButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                browseForImagePath();
            }
        });
        imagePathPanel.add(browseButton, BorderLayout.EAST);

        // Add panels to main panel
        mainPanel.add(webcamPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(imagePathPanel);
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createDatabasePanel());
        mainPanel.add(Box.createRigidArea(new Dimension(0, 10)));
        mainPanel.add(createExportPanel());
        mainPanel.add(Box.createVerticalGlue());

        add(mainPanel, BorderLayout.CENTER);

        // Buttons
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 10));
        buttonPanel.setBackground(Color.WHITE);
        
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Arial", Font.BOLD, 12));
        saveButton.setBackground(new Color(109, 193, 210));
        saveButton.setForeground(new Color(60, 62, 128));
        saveButton.setFocusPainted(false);
        saveButton.setPreferredSize(new Dimension(90, 32));
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (saveSettings()) {
                    dispose();
                }
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Arial", Font.BOLD, 12));
        cancelButton.setBackground(new Color(240, 240, 240));
        cancelButton.setForeground(new Color(60, 62, 128));
        cancelButton.setFocusPainted(false);
        cancelButton.setPreferredSize(new Dimension(90, 32));
        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dispose();
            }
        });
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadWebcamDevices() {
        try {
            java.util.List<Webcam> webcams = Webcam.getWebcams();
            String savedDevice = AppConfig.getWebcamDevice();
            int selectedIndex = 0;
            if (savedDevice != null) {
                try {
                    selectedIndex = Integer.parseInt(savedDevice);
                } catch (NumberFormatException ignored) {
                    selectedIndex = 0;
                }
            }
            int i = 0;
            
            for (Webcam webcam : webcams) {
                webcamSelector.addItem(webcam.getName());
                if (i == selectedIndex) {
                    webcamSelector.setSelectedIndex(i);
                }
                i++;
            }
            
            // If no webcams found, add a placeholder
            if (webcams.isEmpty()) {
                webcamSelector.addItem("No webcams detected");
            }
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error loading webcam devices: " + e.getMessage(), 
                "Webcam Error", 
                JOptionPane.ERROR_MESSAGE);
            webcamSelector.addItem("Default webcam");
        }
    }

    private void browseForImagePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        fileChooser.setDialogTitle("Select Image Storage Directory");
        
        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedDir = fileChooser.getSelectedFile();
            imagePathField.setText(selectedDir.getAbsolutePath());
        }
    }

    private JPanel createExportPanel() {
        JPanel exportPanel = new JPanel();
        exportPanel.setLayout(new BoxLayout(exportPanel, BoxLayout.Y_AXIS));
        exportPanel.setBackground(Color.WHITE);
        exportPanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Export Settings",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(60, 62, 128)
        ));

        JPanel orientationPanel = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 5));
        orientationPanel.setBackground(Color.WHITE);
        orientationPanel.add(new JLabel("Orientation:"));

        ButtonGroup orientationGroup = new ButtonGroup();
        orientationAutoRadio = createOrientationRadio("Auto", "AUTO", orientationGroup);
        orientationPortraitRadio = createOrientationRadio("Portrait", "PORTRAIT", orientationGroup);
        orientationLandscapeRadio = createOrientationRadio("Landscape", "LANDSCAPE", orientationGroup);

        orientationPanel.add(orientationAutoRadio);
        orientationPanel.add(orientationPortraitRadio);
        orientationPanel.add(orientationLandscapeRadio);

        exportPanel.add(orientationPanel);

        JPanel fieldsPanel = new JPanel(new GridLayout(0, 2, 8, 8));
        fieldsPanel.setBackground(Color.WHITE);
        fieldsPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));

        Set<String> selectedFields = new HashSet<>(AppConfig.getExportFields());
        for (ExportField field : ExportField.values()) {
            JCheckBox checkBox = new JCheckBox(field.getHeader());
            checkBox.setBackground(Color.WHITE);
            checkBox.setForeground(new Color(60, 62, 128));
            checkBox.setFont(new Font("Arial", Font.PLAIN, 12));
            checkBox.setSelected(selectedFields.contains(field.getKey()));
            fieldCheckBoxes.put(field, checkBox);
            fieldsPanel.add(checkBox);
        }

        exportPanel.add(Box.createVerticalStrut(5));
        exportPanel.add(fieldsPanel);

        selectOrientationRadio(AppConfig.getExportOrientation());

        return exportPanel;
    }

    private JPanel createDatabasePanel() {
        JPanel databasePanel = new JPanel();
        databasePanel.setLayout(new BoxLayout(databasePanel, BoxLayout.Y_AXIS));
        databasePanel.setBackground(Color.WHITE);
        databasePanel.setBorder(BorderFactory.createTitledBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            "Database",
            javax.swing.border.TitledBorder.LEFT,
            javax.swing.border.TitledBorder.TOP,
            new Font("Arial", Font.BOLD, 12),
            new Color(60, 62, 128)
        ));

        useCustomDbCheckBox = new JCheckBox("Use existing H2 database file");
        useCustomDbCheckBox.setBackground(Color.WHITE);
        useCustomDbCheckBox.setForeground(new Color(60, 62, 128));
        useCustomDbCheckBox.setFont(new Font("Arial", Font.PLAIN, 12));
        databasePanel.add(useCustomDbCheckBox);

        JPanel pathPanel = new JPanel(new BorderLayout(5, 0));
        pathPanel.setBackground(Color.WHITE);

        String savedPath = AppConfig.getCustomDatabasePath();
        String displayPath = savedPath;
        if (displayPath == null) {
            displayPath = "";
        }
        if (!displayPath.isEmpty()) {
            File directFile = new File(displayPath);
            if (directFile.exists() && directFile.isFile()) {
                displayPath = directFile.getAbsolutePath();
            } else {
                File mvCandidate = new File(displayPath + ".mv.db");
                if (mvCandidate.exists()) {
                    displayPath = mvCandidate.getAbsolutePath();
                }
            }
        }

        customDbPathField = new JTextField(displayPath);
        customDbPathField.setFont(new Font("Arial", Font.PLAIN, 12));
        pathPanel.add(customDbPathField, BorderLayout.CENTER);

        dbBrowseButton = new JButton("Browse...");
        dbBrowseButton.setFont(new Font("Arial", Font.BOLD, 11));
        dbBrowseButton.setBackground(new Color(109, 193, 210));
        dbBrowseButton.setForeground(new Color(60, 62, 128));
        dbBrowseButton.setFocusPainted(false);
        dbBrowseButton.addActionListener(e -> browseForDatabasePath());
        pathPanel.add(dbBrowseButton, BorderLayout.EAST);

        databasePanel.add(Box.createVerticalStrut(6));
        databasePanel.add(pathPanel);

    JLabel helperLabel = new JLabel("Select the H2 database (.mv.db) you want this app to use.");
        helperLabel.setFont(new Font("Arial", Font.ITALIC, 11));
        helperLabel.setForeground(new Color(90, 90, 120));
        helperLabel.setBorder(BorderFactory.createEmptyBorder(6, 2, 0, 0));
        databasePanel.add(helperLabel);

        boolean hasCustomPath = savedPath != null && !savedPath.isEmpty();
        useCustomDbCheckBox.setSelected(hasCustomPath);
        toggleCustomDatabaseInputs(hasCustomPath);

        useCustomDbCheckBox.addActionListener(e -> {
            boolean enabled = useCustomDbCheckBox.isSelected();
            toggleCustomDatabaseInputs(enabled);
        });

        return databasePanel;
    }

    private void toggleCustomDatabaseInputs(boolean enabled) {
        if (customDbPathField != null) {
            customDbPathField.setEnabled(enabled);
        }
        if (dbBrowseButton != null) {
            dbBrowseButton.setEnabled(enabled);
        }
    }

    private JRadioButton createOrientationRadio(String label, String value, ButtonGroup group) {
        JRadioButton radio = new JRadioButton(label);
        radio.putClientProperty("orientationValue", value);
        radio.setBackground(Color.WHITE);
        radio.setForeground(new Color(60, 62, 128));
        radio.setFont(new Font("Arial", Font.PLAIN, 12));
        group.add(radio);
        return radio;
    }

    private void selectOrientationRadio(String orientation) {
        String normalized = orientation == null ? "AUTO" : orientation.trim().toUpperCase(Locale.ROOT);
        if ("LANDSCAPE".equals(normalized)) {
            orientationLandscapeRadio.setSelected(true);
        } else if ("PORTRAIT".equals(normalized)) {
            orientationPortraitRadio.setSelected(true);
        } else {
            orientationAutoRadio.setSelected(true);
        }
    }

    private String getSelectedOrientation() {
        if (orientationPortraitRadio.isSelected()) {
            return "PORTRAIT";
        }
        if (orientationLandscapeRadio.isSelected()) {
            return "LANDSCAPE";
        }
        return "AUTO";
    }

    private boolean saveSettings() {
        boolean databasePreferenceChanged = false;
        // Save webcam device selection
        if (!disableCameraCheckBox.isSelected()) {
            int selectedWebcamIndex = webcamSelector.getSelectedIndex();
            if (selectedWebcamIndex >= 0) {
                AppConfig.setWebcamDevice(String.valueOf(selectedWebcamIndex));
                settingsChanged = true;
            }
        }
        
        // Save image directory
        String imagePath = imagePathField.getText().trim();
        if (!imagePath.isEmpty() && !imagePath.equals(AppConfig.getImagesDirectory())) {
            AppConfig.setImagesDirectory(imagePath);
            settingsChanged = true;
        }
        
        boolean disableCamera = disableCameraCheckBox.isSelected();
        if (disableCamera != AppConfig.isCameraDisabled()) {
            AppConfig.setCameraDisabled(disableCamera);
            settingsChanged = true;
        }

        String newOrientation = getSelectedOrientation();
        if (!newOrientation.equals(AppConfig.getExportOrientation())) {
            AppConfig.setExportOrientation(newOrientation);
            settingsChanged = true;
        }

        List<String> selectedFieldKeys = new ArrayList<>();
        for (Map.Entry<ExportField, JCheckBox> entry : fieldCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                selectedFieldKeys.add(entry.getKey().getKey());
            }
        }

        if (selectedFieldKeys.isEmpty()) {
            JOptionPane.showMessageDialog(this,
                "Select at least one field to export.",
                "Validation Error",
                JOptionPane.WARNING_MESSAGE);
            return false;
        }

        List<String> currentFields = AppConfig.getExportFields();
        if (!selectedFieldKeys.equals(currentFields)) {
            AppConfig.setExportFields(selectedFieldKeys);
            settingsChanged = true;
        }

        boolean useCustomDatabase = useCustomDbCheckBox != null && useCustomDbCheckBox.isSelected();
        String storedPath = AppConfig.getCustomDatabasePath();
        if (!useCustomDatabase) {
            if (storedPath != null && !storedPath.isEmpty()) {
                AppConfig.setCustomDatabasePath("");
                settingsChanged = true;
                databasePreferenceChanged = true;
            }
        } else {
            String enteredPath = customDbPathField.getText().trim();
            if (enteredPath.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "Select a database file or uncheck the option.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }

            String normalized = AppConfig.normalizeDatabasePath(enteredPath);
            if (normalized.isEmpty()) {
                JOptionPane.showMessageDialog(this,
                    "The selected database path is invalid.",
                    "Validation Error",
                    JOptionPane.WARNING_MESSAGE);
                return false;
            }

            boolean fileExists = new File(normalized).exists() || new File(normalized + ".mv.db").exists();
            if (!fileExists) {
                int choice = JOptionPane.showConfirmDialog(this,
                    "The selected path does not appear to contain an H2 database. Use it anyway?",
                    "Confirm Database Path",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (choice != JOptionPane.YES_OPTION) {
                    return false;
                }
            }

            if (!normalized.equals(storedPath)) {
                AppConfig.setCustomDatabasePath(normalized);
                settingsChanged = true;
                databasePreferenceChanged = true;
            }
        }

        if (databasePreferenceChanged) {
            DatabaseConnection.closeConnection();
        }

        if (settingsChanged) {
            JOptionPane.showMessageDialog(this, 
                "Settings saved successfully.", 
                "Preferences", 
                JOptionPane.INFORMATION_MESSAGE);
        }
        return true;
    }
    
    public boolean isSettingsChanged() {
        return settingsChanged;
    }

    private void browseForDatabasePath() {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setDialogTitle("Select H2 Database File (.mv.db)");

        if (fileChooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            customDbPathField.setText(selectedFile.getAbsolutePath());
            if (useCustomDbCheckBox != null) {
                useCustomDbCheckBox.setSelected(true);
                toggleCustomDatabaseInputs(true);
            }
        }
    }
}