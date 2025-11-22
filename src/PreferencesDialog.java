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
    private final Map<String, JCheckBox> requiredFieldCheckBoxes = new LinkedHashMap<>();
    private boolean settingsChanged = false;
    
    // Modern UI components
    private JPanel contentPanel;
    private CardLayout cardLayout;
    
    // Section names
    private static final String SECTION_CAMERA = "Camera Settings";
    private static final String SECTION_FIELDS = "Required Fields";
    private static final String SECTION_DATABASE = "Database";
    private static final String SECTION_EXPORT = "Export Settings";
    private static final String SECTION_CATALOG = "Key Catalog";

    public PreferencesDialog(JFrame parent) {
        super(parent, "Preferences", true);
        initComponents();
    }

    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(950, 700);
        setLocationRelativeTo(null);
        getContentPane().setBackground(Color.WHITE);
        
        // Create modern sidebar
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Create content area with CardLayout
        cardLayout = new CardLayout();
        contentPanel = new JPanel(cardLayout);
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));
        
        // Add all section panels
        contentPanel.add(createCameraPanel(), SECTION_CAMERA);
        contentPanel.add(createRequiredFieldsPanelModern(), SECTION_FIELDS);
        contentPanel.add(createDatabasePanelModern(), SECTION_DATABASE);
        contentPanel.add(createExportPanelModern(), SECTION_EXPORT);
        contentPanel.add(createKeyCatalogPanel(), SECTION_CATALOG);
        
        add(contentPanel, BorderLayout.CENTER);

        // Buttons panel
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 15, 15));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createMatteBorder(1, 0, 0, 0, new Color(220, 220, 220)));
        
        JButton saveButton = new JButton("Save");
        saveButton.setFont(new Font("Segoe UI", Font.BOLD, 13));
        saveButton.setBackground(new Color(25, 118, 140));
        saveButton.setForeground(Color.WHITE);
        saveButton.setFocusPainted(false);
        saveButton.setBorderPainted(false);
        saveButton.setPreferredSize(new Dimension(100, 36));
        saveButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        saveButton.addActionListener(e -> {
            if (saveSettings()) {
                dispose();
            }
        });
        
        JButton cancelButton = new JButton("Cancel");
        cancelButton.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        cancelButton.setBackground(new Color(240, 240, 240));
        cancelButton.setForeground(new Color(80, 80, 80));
        cancelButton.setFocusPainted(false);
        cancelButton.setBorderPainted(false);
        cancelButton.setPreferredSize(new Dimension(100, 36));
        cancelButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        cancelButton.addActionListener(e -> dispose());
        
        buttonPanel.add(saveButton);
        buttonPanel.add(cancelButton);
        
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Show first section by default
        cardLayout.show(contentPanel, SECTION_CAMERA);
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
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background matching MetricsWindow
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 118, 140),
                    0, getHeight(), new Color(15, 76, 92)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(109, 193, 210)),
            BorderFactory.createEmptyBorder(25, 15, 25, 15)
        ));
        
        JLabel title = new JLabel("Preferences");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(title);
        
        JLabel subtitle = new JLabel("Configure your settings");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(180, 220, 230));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(30));
        
        // Create section buttons
        List<JPanel> sectionButtons = new ArrayList<>();
        String[] sections = {SECTION_CAMERA, SECTION_FIELDS, SECTION_DATABASE, SECTION_EXPORT, SECTION_CATALOG};
        for (int i = 0; i < sections.length; i++) {
            JPanel btn = createSectionButton(sections[i], i == 0, sectionButtons);
            sectionButtons.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(8));
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        // Footer
        JLabel footerLabel = new JLabel("KeyBase v.4.0");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footerLabel.setForeground(new Color(150, 200, 210));
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footerLabel);
        
        return sidebar;
    }
    
    private JPanel createSectionButton(String sectionName, boolean initialSelected, List<JPanel> allButtons) {
        class SelectablePanel extends JPanel {
            private boolean selected = false;
            private boolean hovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                if (selected) {
                    g2.setColor(new Color(10, 60, 75, 220));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    g2.setColor(new Color(109, 193, 210));
                    g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                } else if (hovered) {
                    g2.setColor(new Color(25, 118, 140, 60));
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                }
            }
            
            public void setSelected(boolean sel) {
                this.selected = sel;
                repaint();
            }
            
            public void setHovered(boolean hov) {
                this.hovered = hov;
                repaint();
            }
        }
        
        SelectablePanel btnPanel = new SelectablePanel();
        btnPanel.setLayout(new BorderLayout());
        btnPanel.setOpaque(false);
        btnPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 40));
        btnPanel.setPreferredSize(new Dimension(200, 40));
        btnPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        btnPanel.setSelected(initialSelected);
        
        JLabel label = new JLabel(sectionName);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 10, 0, 10));
        btnPanel.add(label, BorderLayout.CENTER);
        
        // Mouse listeners
        btnPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (!btnPanel.selected) {
                    btnPanel.setHovered(true);
                }
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnPanel.setHovered(false);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Deselect all buttons
                for (JPanel btn : allButtons) {
                    if (btn instanceof SelectablePanel) {
                        ((SelectablePanel) btn).setSelected(false);
                    }
                }
                // Select this button
                btnPanel.setSelected(true);
                // Show corresponding panel
                cardLayout.show(contentPanel, sectionName);
            }
        });
        
        return btnPanel;
    }
    
    private JPanel createCameraPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Camera Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 118, 140));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        JLabel descLabel = new JLabel("Configure webcam and image storage settings");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(25));
        
        // Webcam Device
        JPanel webcamContainer = createModernSection("Webcam Device");
        
        webcamSelector = new JComboBox<>();
        webcamSelector.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        webcamSelector.setAlignmentX(Component.LEFT_ALIGNMENT);
        webcamSelector.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        loadWebcamDevices();
        webcamContainer.add(webcamSelector);
        webcamContainer.add(Box.createVerticalStrut(10));
        
        disableCameraCheckBox = new JCheckBox("Disable camera features");
        disableCameraCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        disableCameraCheckBox.setBackground(Color.WHITE);
        disableCameraCheckBox.setForeground(new Color(80, 80, 80));
        disableCameraCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        disableCameraCheckBox.setSelected(AppConfig.isCameraDisabled());
        disableCameraCheckBox.addActionListener(e -> webcamSelector.setEnabled(!disableCameraCheckBox.isSelected()));
        webcamSelector.setEnabled(!disableCameraCheckBox.isSelected());
        webcamContainer.add(disableCameraCheckBox);
        
        panel.add(webcamContainer);
        panel.add(Box.createVerticalStrut(20));
        
        // Image Storage
        JPanel imageContainer = createModernSection("Image Storage Location");
        
        imagePathField = new JTextField(AppConfig.getImagesDirectory());
        imagePathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        imagePathField.setAlignmentX(Component.LEFT_ALIGNMENT);
        imagePathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        JButton browseBtn = new JButton("Browse...");
        browseBtn.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        browseBtn.setBackground(new Color(25, 118, 140));
        browseBtn.setForeground(Color.WHITE);
        browseBtn.setFocusPainted(false);
        browseBtn.setBorderPainted(false);
        browseBtn.setAlignmentX(Component.LEFT_ALIGNMENT);
        browseBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        browseBtn.addActionListener(e -> browseForImagePath());
        
        JPanel pathPanel = new JPanel(new BorderLayout(10, 0));
        pathPanel.setBackground(Color.WHITE);
        pathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        pathPanel.add(imagePathField, BorderLayout.CENTER);
        pathPanel.add(browseBtn, BorderLayout.EAST);
        
        imageContainer.add(pathPanel);
        panel.add(imageContainer);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createModernSection(String title) {
        JPanel section = new JPanel();
        section.setLayout(new BoxLayout(section, BoxLayout.Y_AXIS));
        section.setBackground(Color.WHITE);
        section.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(15, 15, 15, 15)
        ));
        section.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.setMaximumSize(new Dimension(Integer.MAX_VALUE, 2000));
        
        JLabel titleLabel = new JLabel(title);
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 14));
        titleLabel.setForeground(new Color(60, 60, 60));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        section.add(titleLabel);
        section.add(Box.createVerticalStrut(12));
        
        return section;
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

    private JPanel createRequiredFieldsPanelModern() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Required Fields");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 118, 140));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        JLabel descLabel = new JLabel("Select which fields must be filled before saving a record");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(25));
        
        // Fields container
        JPanel fieldsContainer = createModernSection("Form Field Requirements");
        
        // Get current required fields
        Set<String> requiredFields = AppConfig.getRequiredFields();
        
        // Define all available fields
        String[] formFields = {
            "NAME", "PHONE", "ID_NO", "VEHICLE_NO", "KEY_NO", 
            "PURPOSE", "REMARKS", "QUANTITY", "AMOUNT", "DATE"
        };
        
        String[] formFieldLabels = {
            "Name", "Phone Number", "ID Number", "Vehicle Number", "Key Number",
            "Purpose", "Remarks", "Quantity", "Amount", "Date"
        };
        
        JPanel checkBoxGrid = new JPanel(new GridLayout(0, 2, 15, 10));
        checkBoxGrid.setBackground(Color.WHITE);
        checkBoxGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBoxGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        
        for (int i = 0; i < formFields.length; i++) {
            JCheckBox checkBox = new JCheckBox(formFieldLabels[i]);
            checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            checkBox.setBackground(Color.WHITE);
            checkBox.setForeground(new Color(80, 80, 80));
            checkBox.setSelected(requiredFields.contains(formFields[i]));
            requiredFieldCheckBoxes.put(formFields[i], checkBox);
            checkBoxGrid.add(checkBox);
        }
        
        fieldsContainer.add(checkBoxGrid);
        panel.add(fieldsContainer);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createRequiredFieldsPanel() {
        // Old method - kept for compatibility, redirects to modern version
        return createRequiredFieldsPanelModern();
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
    
    private JPanel createDatabasePanelModern() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Database Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 118, 140));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        JLabel descLabel = new JLabel("Configure database connection");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(25));
        
        // Database container
        JPanel dbContainer = createModernSection("Custom Database");
        
        useCustomDbCheckBox = new JCheckBox("Use existing H2 database file");
        useCustomDbCheckBox.setBackground(Color.WHITE);
        useCustomDbCheckBox.setForeground(new Color(80, 80, 80));
        useCustomDbCheckBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        useCustomDbCheckBox.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbContainer.add(useCustomDbCheckBox);
        dbContainer.add(Box.createVerticalStrut(12));
        
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
        customDbPathField.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        customDbPathField.setAlignmentX(Component.LEFT_ALIGNMENT);
        customDbPathField.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        
        dbBrowseButton = new JButton("Browse...");
        dbBrowseButton.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        dbBrowseButton.setBackground(new Color(25, 118, 140));
        dbBrowseButton.setForeground(Color.WHITE);
        dbBrowseButton.setFocusPainted(false);
        dbBrowseButton.setBorderPainted(false);
        dbBrowseButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        dbBrowseButton.addActionListener(e -> browseForDatabasePath());
        
        JPanel pathPanel = new JPanel(new BorderLayout(10, 0));
        pathPanel.setBackground(Color.WHITE);
        pathPanel.setAlignmentX(Component.LEFT_ALIGNMENT);
        pathPanel.setMaximumSize(new Dimension(Integer.MAX_VALUE, 35));
        pathPanel.add(customDbPathField, BorderLayout.CENTER);
        pathPanel.add(dbBrowseButton, BorderLayout.EAST);
        
        dbContainer.add(pathPanel);
        dbContainer.add(Box.createVerticalStrut(10));
        
        JLabel helperLabel = new JLabel("<html><i>Select the H2 database (.mv.db) you want this app to use.</i></html>");
        helperLabel.setFont(new Font("Segoe UI", Font.ITALIC, 11));
        helperLabel.setForeground(new Color(120, 120, 120));
        helperLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        dbContainer.add(helperLabel);
        
        boolean hasCustomPath = savedPath != null && !savedPath.isEmpty();
        useCustomDbCheckBox.setSelected(hasCustomPath);
        toggleCustomDatabaseInputs(hasCustomPath);
        
        useCustomDbCheckBox.addActionListener(e -> {
            boolean enabled = useCustomDbCheckBox.isSelected();
            toggleCustomDatabaseInputs(enabled);
        });
        
        panel.add(dbContainer);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }
    
    private JPanel createExportPanelModern() {
        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(Color.WHITE);
        
        // Title
        JLabel titleLabel = new JLabel("Export Settings");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 118, 140));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(titleLabel);
        panel.add(Box.createVerticalStrut(5));
        
        JLabel descLabel = new JLabel("Configure CSV export preferences");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        panel.add(descLabel);
        panel.add(Box.createVerticalStrut(25));
        
        // Orientation section
        JPanel orientationContainer = createModernSection("Page Orientation");
        
        ButtonGroup orientationGroup = new ButtonGroup();
        orientationAutoRadio = createOrientationRadio("Auto", "AUTO", orientationGroup);
        orientationPortraitRadio = createOrientationRadio("Portrait", "PORTRAIT", orientationGroup);
        orientationLandscapeRadio = createOrientationRadio("Landscape", "LANDSCAPE", orientationGroup);
        
        orientationAutoRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        orientationPortraitRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        orientationLandscapeRadio.setAlignmentX(Component.LEFT_ALIGNMENT);
        
        orientationContainer.add(orientationAutoRadio);
        orientationContainer.add(Box.createVerticalStrut(8));
        orientationContainer.add(orientationPortraitRadio);
        orientationContainer.add(Box.createVerticalStrut(8));
        orientationContainer.add(orientationLandscapeRadio);
        
        selectOrientationRadio(AppConfig.getExportOrientation());
        
        panel.add(orientationContainer);
        panel.add(Box.createVerticalStrut(20));
        
        // Export fields section
        JPanel fieldsContainer = createModernSection("Fields to Export");
        
        JPanel checkBoxGrid = new JPanel(new GridLayout(0, 2, 15, 10));
        checkBoxGrid.setBackground(Color.WHITE);
        checkBoxGrid.setAlignmentX(Component.LEFT_ALIGNMENT);
        checkBoxGrid.setMaximumSize(new Dimension(Integer.MAX_VALUE, 400));
        
        Set<String> selectedFields = new HashSet<>(AppConfig.getExportFields());
        for (ExportField field : ExportField.values()) {
            JCheckBox checkBox = new JCheckBox(field.getHeader());
            checkBox.setBackground(Color.WHITE);
            checkBox.setForeground(new Color(80, 80, 80));
            checkBox.setFont(new Font("Segoe UI", Font.PLAIN, 13));
            checkBox.setSelected(selectedFields.contains(field.getKey()));
            fieldCheckBoxes.put(field, checkBox);
            checkBoxGrid.add(checkBox);
        }
        
        fieldsContainer.add(checkBoxGrid);
        panel.add(fieldsContainer);
        panel.add(Box.createVerticalGlue());
        
        return panel;
    }

    private JPanel createKeyCatalogPanel() {
        JPanel panel = new JPanel();
        panel.setLayout(new BorderLayout());
        panel.setBackground(Color.WHITE);
        
        // Header
        JPanel headerPanel = new JPanel();
        headerPanel.setLayout(new BoxLayout(headerPanel, BoxLayout.Y_AXIS));
        headerPanel.setBackground(Color.WHITE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 20, 0));
        
        JLabel titleLabel = new JLabel("Key Catalog");
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 22));
        titleLabel.setForeground(new Color(25, 118, 140));
        titleLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(titleLabel);
        headerPanel.add(Box.createVerticalStrut(5));
        
        JLabel descLabel = new JLabel("Manage key categories and sub-categories");
        descLabel.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        descLabel.setForeground(new Color(120, 120, 120));
        descLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        headerPanel.add(descLabel);
        
        panel.add(headerPanel, BorderLayout.NORTH);
        
        // Content
        JSplitPane splitPane = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
        splitPane.setResizeWeight(0.5);
        splitPane.setBorder(null);
        splitPane.setDividerSize(10);
        splitPane.setBackground(Color.WHITE);
        
        // Parent Categories List
        DefaultListModel<String> parentModel = new DefaultListModel<>();
        for (String p : AppConfig.getParentCategories()) {
            parentModel.addElement(p);
        }
        JList<String> parentList = new JList<>(parentModel);
        parentList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Child Categories List
        DefaultListModel<String> childModel = new DefaultListModel<>();
        JList<String> childList = new JList<>(childModel);
        childList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        
        // Parent Panel
        JPanel parentPanel = createCategoryListPanel("Parent Categories", parentList, parentModel, null, childModel, true);
        
        // Child Panel
        JPanel childPanel = createCategoryListPanel("Sub-Categories", childList, childModel, parentList, null, false);
        
        splitPane.setLeftComponent(parentPanel);
        splitPane.setRightComponent(childPanel);
        
        panel.add(splitPane, BorderLayout.CENTER);
        
        // Selection Listener for Parent List
        parentList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                String selected = parentList.getSelectedValue();
                childModel.clear();
                if (selected != null) {
                    for (String child : AppConfig.getChildCategories(selected)) {
                        childModel.addElement(child);
                    }
                    // Update title of child panel if possible, or just rely on context
                }
            }
        });
        
        // Select first parent by default
        if (parentModel.getSize() > 0) {
            parentList.setSelectedIndex(0);
        }
        
        return panel;
    }
    
    private JPanel createCategoryListPanel(String title, JList<String> list, DefaultListModel<String> model, 
                                           JList<String> parentList, DefaultListModel<String> childModel, boolean isParent) {
        JPanel panel = new JPanel(new BorderLayout(0, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 10, 10, 10)
        ));
        
        JLabel lblTitle = new JLabel(title);
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 14));
        lblTitle.setForeground(new Color(60, 60, 60));
        panel.add(lblTitle, BorderLayout.NORTH);
        
        list.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200)));
        panel.add(scrollPane, BorderLayout.CENTER);
        
        // Buttons
        JPanel btnPanel = new JPanel(new GridLayout(2, 3, 5, 5));
        btnPanel.setBackground(Color.WHITE);
        
        JButton btnAdd = new JButton("Add");
        JButton btnRename = new JButton("Rename");
        JButton btnDelete = new JButton("Delete");
        JButton btnUp = new JButton("Up");
        JButton btnDown = new JButton("Down");
        JButton btnDefault = new JButton("Set Default");
        
        btnPanel.add(btnAdd);
        btnPanel.add(btnRename);
        btnPanel.add(btnDelete);
        btnPanel.add(btnUp);
        btnPanel.add(btnDown);
        btnPanel.add(btnDefault);
        
        panel.add(btnPanel, BorderLayout.SOUTH);
        
        // Actions
        btnAdd.addActionListener(e -> {
            String name = JOptionPane.showInputDialog(this, "Enter name:");
            if (name != null && !name.trim().isEmpty()) {
                name = name.trim();
                if (model.contains(name)) {
                    JOptionPane.showMessageDialog(this, "Already exists!");
                    return;
                }
                model.addElement(name);
                saveChanges(isParent, parentList, model);
                settingsChanged = true;
            }
        });
        
        btnRename.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                String oldName = model.getElementAt(idx);
                String newName = JOptionPane.showInputDialog(this, "Enter new name:", oldName);
                if (newName != null && !newName.trim().isEmpty() && !newName.equals(oldName)) {
                    newName = newName.trim();
                    if (model.contains(newName)) {
                        JOptionPane.showMessageDialog(this, "Already exists!");
                        return;
                    }
                    model.set(idx, newName);
                    if (isParent) {
                        AppConfig.renameParentCategory(oldName, newName);
                        // Reload children for the renamed parent (which is now selected)
                        // Actually, renameParentCategory handles the property move.
                        // But we need to ensure the UI stays consistent.
                    }
                    saveChanges(isParent, parentList, model);
                    settingsChanged = true;
                }
            }
        });
        
        btnDelete.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                String val = model.getElementAt(idx);
                if (JOptionPane.showConfirmDialog(this, "Delete '" + val + "'?", "Confirm", JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {
                    model.remove(idx);
                    if (isParent) {
                        AppConfig.deleteParentCategory(val);
                        if (childModel != null) childModel.clear();
                    }
                    saveChanges(isParent, parentList, model);
                    settingsChanged = true;
                }
            }
        });
        
        btnUp.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx > 0) {
                String val = model.remove(idx);
                model.add(idx - 1, val);
                list.setSelectedIndex(idx - 1);
                saveChanges(isParent, parentList, model);
                settingsChanged = true;
            }
        });
        
        btnDown.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1 && idx < model.getSize() - 1) {
                String val = model.remove(idx);
                model.add(idx + 1, val);
                list.setSelectedIndex(idx + 1);
                saveChanges(isParent, parentList, model);
                settingsChanged = true;
            }
        });
        
        btnDefault.addActionListener(e -> {
            int idx = list.getSelectedIndex();
            if (idx != -1) {
                String val = model.getElementAt(idx);
                if (isParent) {
                    AppConfig.setDefaultParentCategory(val);
                } else {
                    String parent = parentList.getSelectedValue();
                    if (parent != null) {
                        AppConfig.setDefaultChildCategory(parent, val);
                    }
                }
                JOptionPane.showMessageDialog(this, "Default set to: " + val);
                settingsChanged = true;
            }
        });
        
        return panel;
    }
    
    private void saveChanges(boolean isParent, JList<String> parentList, DefaultListModel<String> model) {
        List<String> items = new ArrayList<>();
        for (int i = 0; i < model.getSize(); i++) {
            items.add(model.getElementAt(i));
        }
        
        if (isParent) {
            AppConfig.setParentCategories(items);
        } else {
            String parent = parentList.getSelectedValue();
            if (parent != null) {
                AppConfig.setChildCategories(parent, items);
            }
        }
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
        radio.setForeground(new Color(80, 80, 80));
        radio.setFont(new Font("Segoe UI", Font.PLAIN, 13));
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

        // Save required fields
        Set<String> newRequiredFields = new HashSet<>();
        for (Map.Entry<String, JCheckBox> entry : requiredFieldCheckBoxes.entrySet()) {
            if (entry.getValue().isSelected()) {
                newRequiredFields.add(entry.getKey());
            }
        }
        
        Set<String> currentRequired = AppConfig.getRequiredFields();
        if (!newRequiredFields.equals(currentRequired)) {
            AppConfig.setRequiredFields(newRequiredFields);
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