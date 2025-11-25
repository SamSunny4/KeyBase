package src;

import javax.swing.*;
import javax.swing.border.AbstractBorder;
import javax.swing.plaf.basic.BasicButtonUI;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.geom.Ellipse2D;

/**
 * Modern styled dialog utilities for showing messages and errors
 */
public class ModernDialog {
    
    private static final Color ERROR_COLOR = new Color(220, 53, 69);
    private static final Color WARNING_COLOR = new Color(255, 193, 7);
    private static final Color INFO_COLOR = new Color(23, 162, 184);
    private static final Color SURFACE_COLOR = new Color(250, 252, 254);
    private static final Color BORDER_COLOR = new Color(230, 234, 239);
    private static final Font MESSAGE_FONT = new Font("Segoe UI", Font.PLAIN, 13);
    private static final int MIN_DIALOG_WIDTH = 360;
    private static final int MESSAGE_BODY_WIDTH = 280;
    
    /**
     * Show a modern error dialog
     */
    public static void showError(Component parent, String message, String title) {
        showModernDialog(parent, message, title, ERROR_COLOR, JOptionPane.ERROR_MESSAGE);
    }
    
    /**
     * Show a modern warning dialog
     */
    public static void showWarning(Component parent, String message, String title) {
        showModernDialog(parent, message, title, WARNING_COLOR, JOptionPane.WARNING_MESSAGE);
    }
    
    /**
     * Show a modern info dialog
     */
    public static void showInfo(Component parent, String message, String title) {
        showModernDialog(parent, message, title, INFO_COLOR, JOptionPane.INFORMATION_MESSAGE);
    }
    
    /**
     * Show a modern confirmation dialog
     */
    public static int showConfirm(Component parent, String message, String title) {
        return showModernConfirmDialog(parent, message, title, WARNING_COLOR);
    }
    
    private static void showModernDialog(Component parent, String message, String title, Color accentColor, int messageType) {
        JDialog dialog = buildDialog(parent, title);

        JPanel content = buildContentPanel(message, accentColor, messageType);
        JButton okButton = createAccentButton("OK", accentColor);
        okButton.addActionListener(e -> dialog.dispose());

        JPanel buttonRow = createButtonRow(okButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURFACE_COLOR);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttonRow, BorderLayout.SOUTH);

        dialog.setContentPane(wrapper);
        dialog.getRootPane().setDefaultButton(okButton);
        installEscapeToClose(dialog);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(MIN_DIALOG_WIDTH, dialog.getHeight()));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);
    }

    private static int showModernConfirmDialog(Component parent, String message, String title, Color accentColor) {
        final int[] result = {JOptionPane.CLOSED_OPTION};
        JDialog dialog = buildDialog(parent, title);

        JPanel content = buildContentPanel(message, accentColor, JOptionPane.QUESTION_MESSAGE);

        JButton yesButton = createAccentButton("Yes", accentColor);
        yesButton.addActionListener(e -> {
            result[0] = JOptionPane.YES_OPTION;
            dialog.dispose();
        });

        JButton noButton = createSecondaryButton("No");
        noButton.addActionListener(e -> {
            result[0] = JOptionPane.NO_OPTION;
            dialog.dispose();
        });

        JPanel buttonRow = createButtonRow(noButton, yesButton);

        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(SURFACE_COLOR);
        wrapper.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(BORDER_COLOR),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)));
        wrapper.add(content, BorderLayout.CENTER);
        wrapper.add(buttonRow, BorderLayout.SOUTH);

        dialog.setContentPane(wrapper);
        dialog.getRootPane().setDefaultButton(yesButton);
        installEscapeToClose(dialog);
        dialog.pack();
        dialog.setMinimumSize(new Dimension(MIN_DIALOG_WIDTH, dialog.getHeight()));
        dialog.setLocationRelativeTo(parent);
        dialog.setVisible(true);

        return result[0];
    }

    private static JPanel buildContentPanel(String message, Color accentColor, int messageType) {
        JPanel content = new JPanel(new BorderLayout(15, 0));
        content.setBackground(SURFACE_COLOR);

        JLabel iconLabel = new JLabel(createAccentIcon(accentColor, messageType));
        iconLabel.setPreferredSize(new Dimension(46, 46));
        iconLabel.setHorizontalAlignment(SwingConstants.CENTER);
        content.add(iconLabel, BorderLayout.WEST);

        JLabel messageLabel = new JLabel("<html><div style='width:" + MESSAGE_BODY_WIDTH + "px;'>" +
            message.replace("\n", "<br>") + "</div></html>");
        messageLabel.setFont(MESSAGE_FONT);
        messageLabel.setForeground(new Color(55, 65, 81));
        content.add(messageLabel, BorderLayout.CENTER);

        return content;
    }

    private static JDialog buildDialog(Component parent, String title) {
        Window owner = parent == null ? null : SwingUtilities.getWindowAncestor(parent);
        JDialog dialog;
        if (owner instanceof Frame frame) {
            dialog = new JDialog(frame, title, true);
        } else if (owner instanceof Dialog dlg) {
            dialog = new JDialog(dlg, title, true);
        } else {
            dialog = new JDialog((Frame) null, title, true);
        }
        dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        dialog.setResizable(false);
        return dialog;
    }

    private static JPanel createButtonRow(JButton... buttons) {
        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        buttonRow.setOpaque(false);
        for (JButton button : buttons) {
            buttonRow.add(button);
        }
        return buttonRow;
    }

    private static JButton createAccentButton(String text, Color accentColor) {
        JButton button = new JButton(text) {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                Color bg = accentColor;
                if (getModel().isPressed()) {
                    bg = accentColor.darker().darker();
                } else if (getModel().isRollover()) {
                    bg = accentColor.darker();
                }
                g2.setColor(bg);
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 18, 18);
                g2.dispose();
                super.paintComponent(g);
            }

            @Override
            public void setContentAreaFilled(boolean b) {
                // Prevent default background painting
            }
        };
        button.setContentAreaFilled(false);
        button.setOpaque(false);
        button.setUI(new BasicButtonUI());
        button.setFont(new Font("Segoe UI", Font.BOLD, 12));
        button.setForeground(Color.WHITE);
        button.setBorder(new RoundedBorder(accentColor.darker()));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(92, 34));
        return button;
    }

    private static JButton createSecondaryButton(String text) {
        JButton button = new JButton(text);
        button.setFont(new Font("Segoe UI", Font.PLAIN, 12));
        button.setForeground(new Color(75, 85, 99));
        button.setBackground(Color.WHITE);
        button.setBorder(new RoundedBorder(BORDER_COLOR));
        button.setFocusPainted(false);
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
        button.setPreferredSize(new Dimension(90, 34));
        return button;
    }

    private static Icon createAccentIcon(Color accentColor, int messageType) {
        final Color fill = accentColor;
        final String symbol = switch (messageType) {
            case JOptionPane.INFORMATION_MESSAGE -> "i";
            case JOptionPane.QUESTION_MESSAGE -> "?";
            default -> "!";
        };
        return new Icon() {
            private final int size = 38;

            @Override
            public void paintIcon(Component c, Graphics g, int x, int y) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(fill);
                g2.fill(new Ellipse2D.Double(x, y, size, size));
                g2.setColor(new Color(255, 255, 255));
                g2.setFont(new Font("Segoe UI", Font.BOLD, 18));
                FontMetrics fm = g2.getFontMetrics();
                int textX = x + (size - fm.stringWidth(symbol)) / 2;
                int textY = y + ((size - fm.getHeight()) / 2) + fm.getAscent();
                g2.drawString(symbol, textX, textY);
                g2.dispose();
            }

            @Override
            public int getIconWidth() {
                return size;
            }

            @Override
            public int getIconHeight() {
                return size;
            }
        };
    }

    private static void installEscapeToClose(JDialog dialog) {
        JRootPane root = dialog.getRootPane();
        KeyStroke escape = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0);
        root.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(escape, "escape");
        root.getActionMap().put("escape", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                dialog.dispose();
            }
        });
    }

    /** Simple rounded border used for buttons. */
    private static class RoundedBorder extends AbstractBorder {
        private final Color borderColor;

        private RoundedBorder(Color borderColor) {
            this.borderColor = borderColor;
        }

        @Override
        public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
            Graphics2D g2 = (Graphics2D) g.create();
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setColor(borderColor);
            g2.drawRoundRect(x, y, width - 1, height - 1, 12, 12);
            g2.dispose();
        }

        @Override
        public Insets getBorderInsets(Component c) {
            return new Insets(5, 10, 5, 10);
        }
    }
}
