package src;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
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
        List<BufferedImage> availableImages = new ArrayList<>();
        for (String imagePath : FALLBACK_IMAGES) {
            BufferedImage image = ResourceHelper.loadImage(imagePath);
            if (image != null) {
                availableImages.add(image);
            }
        }

        if (availableImages.isEmpty()) {
            return null;
        }

        BufferedImage selected = availableImages.get(ThreadLocalRandom.current().nextInt(availableImages.size()));
        return loadScaledImage(selected, width, height);
    }

    public static ImageIcon loadNoResultsPlaceholder(int width, int height) {
        BufferedImage placeholder = ResourceHelper.loadImage(NO_RESULTS_IMAGE);
        if (placeholder == null) {
            return null;
        }
        return loadScaledImage(placeholder, width, height);
    }

    private static ImageIcon loadScaledImage(BufferedImage source, int width, int height) {
        if (source == null) {
            return null;
        }
        Image scaled = source.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }
}
