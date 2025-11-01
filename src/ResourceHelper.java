package src;

import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.security.CodeSource;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

/**
 * Helper methods for loading resources from the classpath or from the
 * application directory on disk.
 */
public final class ResourceHelper {
    private static final Path APPLICATION_BASE = locateApplicationBase();

    private ResourceHelper() {
        // Not instantiable
    }

    public static BufferedImage loadImage(String resourcePath) {
        BufferedImage image = loadImageFromClasspath(resourcePath);
        if (image != null) {
            return image;
        }

        Path resolved = resolveOnDisk(resourcePath);
        if (resolved != null) {
            try {
                return ImageIO.read(resolved.toFile());
            } catch (IOException ex) {
                return null;
            }
        }
        return null;
    }

    public static Image loadImageAsImage(String resourcePath) {
        BufferedImage image = loadImage(resourcePath);
        return image != null ? image : null;
    }

    public static ImageIcon loadIcon(String resourcePath) {
        BufferedImage image = loadImage(resourcePath);
        return image != null ? new ImageIcon(image) : null;
    }

    public static ImageIcon loadScaledIcon(String resourcePath, int width, int height) {
        BufferedImage image = loadImage(resourcePath);
        if (image == null) {
            return null;
        }
        Image scaled = image.getScaledInstance(width, height, Image.SCALE_SMOOTH);
        return new ImageIcon(scaled);
    }

    private static BufferedImage loadImageFromClasspath(String resourcePath) {
        String normalized = normalizeResourcePath(resourcePath);
        ClassLoader loader = ResourceHelper.class.getClassLoader();
        try (InputStream stream = loader.getResourceAsStream(normalized)) {
            if (stream == null) {
                return null;
            }
            return ImageIO.read(stream);
        } catch (IOException ex) {
            return null;
        }
    }

    private static Path resolveOnDisk(String resourcePath) {
        Path candidate = Paths.get(resourcePath);
        if (!candidate.isAbsolute()) {
            candidate = Paths.get(System.getProperty("user.dir"), resourcePath);
        }
        if (Files.exists(candidate)) {
            return candidate;
        }
        Path baseResolved = APPLICATION_BASE.resolve(resourcePath).normalize();
        if (Files.exists(baseResolved)) {
            return baseResolved;
        }
        Path stagedResolved = APPLICATION_BASE.resolve("KeyBase").resolve(resourcePath).normalize();
        if (Files.exists(stagedResolved)) {
            return stagedResolved;
        }
        Path applicationParent = APPLICATION_BASE.getParent();
        if (applicationParent != null) {
            Path parentResolved = applicationParent.resolve(resourcePath).normalize();
            if (Files.exists(parentResolved)) {
                return parentResolved;
            }
            Path parentStaged = applicationParent.resolve("KeyBase").resolve(resourcePath).normalize();
            if (Files.exists(parentStaged)) {
                return parentStaged;
            }
        }
        return null;
    }

    private static Path locateApplicationBase() {
        try {
            CodeSource codeSource = KeyBase.class.getProtectionDomain().getCodeSource();
            if (codeSource != null && codeSource.getLocation() != null) {
                Path location = Paths.get(codeSource.getLocation().toURI());
                if (Files.isRegularFile(location)) {
                    Path parent = location.getParent();
                    if (parent != null) {
                        return parent;
                    }
                } else if (Files.isDirectory(location)) {
                    return location;
                }
            }
        } catch (URISyntaxException ex) {
            // Fall back to working directory
        }
        return Paths.get(System.getProperty("user.dir"));
    }

    private static String normalizeResourcePath(String resourcePath) {
        String path = resourcePath;
        if (path.startsWith("/")) {
            path = path.substring(1);
        }
        return path.replace('\\', '/');
    }
}
