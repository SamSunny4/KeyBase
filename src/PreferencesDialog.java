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
        mainPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Webcam Device Selection
        JPanel webcamPanel = new JPanel(new BorderLayout(5, 0));
        webcamPanel.setBorder(BorderFactory.createTitledBorder("Webcam Device"));

        webcamSelector = new JComboBox<>();
        loadWebcamDevices();
        webcamPanel.add(webcamSelector, BorderLayout.CENTER);

        // Image Storage Location
        JPanel imagePathPanel = new JPanel(new BorderLayout(5, 0));
        imagePathPanel.setBorder(BorderFactory.createTitledBorder("Image Storage Location"));

        imagePathField = new JTextField(AppConfig.getImagesDirectory());
        imagePathPanel.add(imagePathField, BorderLayout.CENTER);

        JButton browseButton = new JButton("Browse...");
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
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        
        JButton saveButton = new JButton("Save");
        saveButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                saveSettings();
                dispose();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
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
            int selectedIndex = Integer.parseInt(AppConfig.getWebcamDevice());
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
        int selectedWebcamIndex = webcamSelector.getSelectedIndex();
        if (selectedWebcamIndex >= 0) {
            AppConfig.setWebcamDevice(String.valueOf(selectedWebcamIndex));
            settingsChanged = true;
        }
        
        // Save image directory
        String imagePath = imagePathField.getText().trim();
        if (!imagePath.isEmpty() && !imagePath.equals(AppConfig.getImagesDirectory())) {
            AppConfig.setImagesDirectory(imagePath);
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