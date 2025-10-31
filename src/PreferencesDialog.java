package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.File;
import java.util.ArrayList;
import com.github.sarxos.webcam.Webcam;

public class PreferencesDialog extends JDialog {
    private JComboBox<String> webcamSelector;
    private JTextField imagePathField;
    private JCheckBox disableCameraCheckBox;
    private boolean settingsChanged = false;

    public PreferencesDialog(JFrame parent) {
        super(parent, "Preferences", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(500, 250);
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
                saveSettings();
                dispose();
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

    private void saveSettings() {
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

        if (settingsChanged) {
            JOptionPane.showMessageDialog(this, 
                "Settings saved successfully.", 
                "Preferences", 
                JOptionPane.INFORMATION_MESSAGE);
        }
    }
    
    public boolean isSettingsChanged() {
        return settingsChanged;
    }
}