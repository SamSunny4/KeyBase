package src;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import javax.imageio.ImageIO;
import javax.swing.*;

import com.github.sarxos.webcam.Webcam;
import com.github.sarxos.webcam.WebcamPanel;
import com.github.sarxos.webcam.WebcamResolution;

public class WebcamCapture extends JDialog {
    private Webcam webcam;
    private WebcamPanel webcamPanel;
    private JPanel mainPanel;
    private BufferedImage capturedImage;
    private boolean imageCaptured = false;
    private String savedImagePath;
    
    public WebcamCapture(JFrame parent) {
        super(parent, "Capture Image", true);
        initComponents();
        setupWebcam();
    }

    // Overload to support callers that are JDialog/Dialog owners
    public WebcamCapture(Dialog parent) {
        super(parent, "Capture Image", true);
        initComponents();
        setupWebcam();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout());
        setSize(640, 520);
        setLocationRelativeTo(null);
        
        mainPanel = new JPanel(new BorderLayout());
        
        JButton captureButton = new JButton("Capture");
        captureButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                captureImage();
            }
        });
        
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(captureButton);
        
        add(mainPanel, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
        
        // Close webcam when dialog is closed
        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWebcam();
            }
        });
    }
    
    private void setupWebcam() {
        try {
            // Get the webcam device index from configuration
            int deviceIndex = Integer.parseInt(AppConfig.getWebcamDevice());
            
            // Get all webcams
            java.util.List<Webcam> webcams = Webcam.getWebcams();
            
            if (webcams.isEmpty()) {
                JOptionPane.showMessageDialog(this, 
                    "No webcams detected on your system", 
                    "Webcam Error", 
                    JOptionPane.ERROR_MESSAGE);
                return;
            }
            
            // Use the configured webcam or the default one if index is out of range
            webcam = deviceIndex < webcams.size() ? webcams.get(deviceIndex) : webcams.get(0);
            
            webcam.setViewSize(WebcamResolution.VGA.getSize());
            webcam.open();
            
            webcamPanel = new WebcamPanel(webcam);
            webcamPanel.setFPSDisplayed(true);
            webcamPanel.setMirrored(true);
            
            mainPanel.add(webcamPanel, BorderLayout.CENTER);
            
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, 
                "Error initializing webcam: " + e.getMessage(), 
                "Webcam Error", 
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void captureImage() {
        if (webcam != null) {
            capturedImage = webcam.getImage();
            imageCaptured = true;
            
            // Save the image to cache directory
            try {
                String imageDir = AppConfig.getImagesDirectory();
                File cacheDir = new File(imageDir, "cache");
                if (!cacheDir.exists()) {
                    cacheDir.mkdirs();
                }
                
                String fileName = "image_" + System.currentTimeMillis() + ".jpg";
                File outputFile = new File(cacheDir, fileName);
                
                ImageIO.write(capturedImage, "JPG", outputFile);
                savedImagePath = outputFile.getAbsolutePath();
                
                JOptionPane.showMessageDialog(this, 
                    "Image captured successfully", 
                    "Capture Complete", 
                    JOptionPane.INFORMATION_MESSAGE);
                
                closeWebcam();
                dispose();
                
            } catch (IOException e) {
                JOptionPane.showMessageDialog(this, 
                    "Error saving image: " + e.getMessage(), 
                    "File Error", 
                    JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    
    private void closeWebcam() {
        if (webcamPanel != null) {
            webcamPanel.stop();
        }
        
        if (webcam != null) {
            webcam.close();
        }
    }
    
    public boolean isImageCaptured() {
        return imageCaptured;
    }
    
    public BufferedImage getCapturedImage() {
        return capturedImage;
    }
    
    public String getSavedImagePath() {
        return savedImagePath;
    }
}