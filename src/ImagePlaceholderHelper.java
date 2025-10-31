package src;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Utility methods for loading placeholder images used when record photos are missing.
 */
public final class ImagePlaceholderHelper {
    private static final String[] FALLBACK_IMAGES = {
        "resources/imageError/EI1.png",
        "resources/imageError/EI2.png",
        "resources/imageError/EI3.png",
        "resources/imageError/EI4.png"
    };

    private static final String NO_RESULTS_IMAGE = "resources/imageError/RE1.png";

    private ImagePlaceholderHelper() {
        // Utility class; prevent instantiation.
    }

    public static ImageIcon loadRandomPlaceholder(int width, int height) {
        List<File> availableImages = new ArrayList<>();
        for (String imagePath : FALLBACK_IMAGES) {
            File candidate = new File(imagePath);
            if (candidate.exists()) {
                availableImages.add(candidate);
            }
        }

        if (availableImages.isEmpty()) {
            return null;
        }

        File selected = availableImages.get(ThreadLocalRandom.current().nextInt(availableImages.size()));
        return loadScaledImage(selected, width, height);
    }

    public static ImageIcon loadNoResultsPlaceholder(int width, int height) {
        File placeholder = new File(NO_RESULTS_IMAGE);
        if (!placeholder.exists()) {
            return null;
        }
        return loadScaledImage(placeholder, width, height);
    }

    private static ImageIcon loadScaledImage(File source, int width, int height) {
        try {
            BufferedImage image = ImageIO.read(source);
            if (image == null) {
                return null;
            }
            Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
            return new ImageIcon(scaled);
        } catch (IOException ex) {
            return null;
        }
    }
}
