package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.List;

/**
 * Key statistics window showing total keys and breakdowns.
 * Features: Total keys count, breakdown by key type, purpose, and quantity.
 */
public class KeyStatisticsWindow extends JFrame {
    
    // Statistic type
    private enum StatType {
        TOTAL_KEYS("Total Keys Done"),
        BY_CATEGORY("By Category"),
        BY_KEY_TYPE("By Key Type"),
        BY_PURPOSE("By Purpose"),
        BY_QUANTITY("By Quantity");
        
        private final String label;
        StatType(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    
    // State
    private StatType currentStat = StatType.TOTAL_KEYS;
    
    // UI components
    private JPanel contentPanel;
    
    // Data
    private Map<String, Integer> statData = new LinkedHashMap<>();
    private int totalCount = 0;
    
    public KeyStatisticsWindow(Frame owner) {
        setTitle("Key Statistics");
        setSize(900, 600);
        setLocationRelativeTo(owner);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        
        // Set window icon
        try {
            java.io.File logoFile = new java.io.File("resources/splash.png");
            if (logoFile.exists()) {
                ImageIcon icon = new ImageIcon(logoFile.getAbsolutePath());
                setIconImage(icon.getImage());
            }
        } catch (Exception e) {
            // Icon not critical, continue without it
        }
        
        initComponents();
        loadData();
    }
    
    private void initComponents() {
        setLayout(new BorderLayout(10, 10));
        getContentPane().setBackground(Color.WHITE);
        
        // Left sidebar with statistic options
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Right panel with statistics display
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        contentPanel = new JPanel();
        contentPanel.setBackground(Color.WHITE);
        contentPanel.setLayout(new BoxLayout(contentPanel, BoxLayout.Y_AXIS));
        
        JScrollPane scrollPane = new JScrollPane(contentPanel);
        scrollPane.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        
        rightPanel.add(scrollPane, BorderLayout.CENTER);
        
        add(rightPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
                
                // Gradient background
                GradientPaint gradient = new GradientPaint(
                    0, 0, new Color(25, 118, 140),
                    0, getHeight(), new Color(15, 76, 92)
                );
                g2.setPaint(gradient);
                g2.fillRect(0, 0, getWidth(), getHeight());
                
                // Right border accent
                g2.setColor(new Color(109, 193, 210));
                g2.fillRect(getWidth() - 3, 0, 3, getHeight());
            }
        };
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setPreferredSize(new Dimension(200, 0));
        sidebar.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createMatteBorder(0, 0, 0, 3, new Color(109, 193, 210)),
            BorderFactory.createEmptyBorder(25, 15, 25, 15)
        ));
        
        JLabel title = new JLabel("Key Statistics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(title);
        
        JLabel subtitle = new JLabel("Choose a view");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(180, 220, 230));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(30));
        
        // Modern selection buttons
        List<JPanel> statButtons = new ArrayList<>();
        for (StatType type : StatType.values()) {
            JPanel btn = createModernSelectButton(type.toString(), type, type == currentStat, statButtons);
            statButtons.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(8));
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        // Footer
        JLabel footerLabel = new JLabel("KeyBase Analytics");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footerLabel.setForeground(new Color(150, 200, 210));
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footerLabel);
        
        return sidebar;
    }
    
    private JPanel createModernSelectButton(String text, StatType type, boolean initialSelected, List<JPanel> allButtons) {
        // Custom panel class with selection state
        class SelectablePanel extends JPanel {
            private boolean selected = false;
            private boolean hovered = false;
            
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Background based on state - selected is darker
                if (selected) {
                    g2.setColor(new Color(10, 60, 75, 220)); // Darker when selected
                    g2.fillRoundRect(0, 0, getWidth(), getHeight(), 10, 10);
                    
                    // Left accent bar
                    g2.setColor(new Color(109, 193, 210));
                    g2.fillRoundRect(0, 0, 4, getHeight(), 4, 4);
                } else if (hovered) {
                    g2.setColor(new Color(25, 118, 140, 60)); // Subtle hover
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
        
        JLabel label = new JLabel(text);
        label.setFont(new Font("Segoe UI", Font.PLAIN, 13));
        label.setForeground(Color.WHITE);
        label.setBorder(BorderFactory.createEmptyBorder(0, 12, 0, 12));
        
        btnPanel.add(label, BorderLayout.CENTER);
        
        // Mouse interaction
        btnPanel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                btnPanel.setHovered(true);
            }
            
            @Override
            public void mouseExited(MouseEvent e) {
                btnPanel.setHovered(false);
            }
            
            @Override
            public void mouseClicked(MouseEvent e) {
                // Deselect all buttons
                for (JPanel otherBtn : allButtons) {
                    if (otherBtn instanceof SelectablePanel) {
                        ((SelectablePanel) otherBtn).setSelected(false);
                    }
                }
                
                // Select this button
                btnPanel.setSelected(true);
                currentStat = type;
                loadData();
            }
        });
        
        return btnPanel;
    }
    
    private void loadData() {
        statData.clear();
        totalCount = 0;
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            switch (currentStat) {
                case TOTAL_KEYS:
                    loadTotalKeys(conn);
                    break;
                case BY_CATEGORY:
                    loadByCategory(conn);
                    break;
                case BY_KEY_TYPE:
                    loadByKeyType(conn);
                    break;
                case BY_PURPOSE:
                    loadByPurpose(conn);
                    break;
                case BY_QUANTITY:
                    loadByQuantity(conn);
                    break;
            }
            updateUI();
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading statistics: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
            e.printStackTrace();
        }
    }
    
    private void loadTotalKeys(Connection conn) throws SQLException {
        String sql = "SELECT COUNT(*), SUM(quantity) FROM duplicator";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                int recordCount = rs.getInt(1);
                int keyCount = rs.getInt(2);
                totalCount = keyCount;
                statData.put("Total Records", recordCount);
                statData.put("Total Keys Made", keyCount);
            }
        }
    }
    
    private void loadByKeyType(Connection conn) throws SQLException {
        String sql = "SELECT key_type, SUM(quantity) FROM duplicator GROUP BY key_type ORDER BY 2 DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String type = rs.getString(1);
                int count = rs.getInt(2);
                if (type == null || type.trim().isEmpty()) type = "Not Specified";
                // Exclude "Vehicles" as per requirement
                if (!"Vehicles".equalsIgnoreCase(type)) {
                    statData.put(type, count);
                    totalCount += count;
                }
            }
        }
    }
    
    private void loadByPurpose(Connection conn) throws SQLException {
        String sql = "SELECT purpose, SUM(quantity) FROM duplicator GROUP BY purpose ORDER BY 2 DESC";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                String purpose = rs.getString(1);
                int count = rs.getInt(2);
                if (purpose == null || purpose.trim().isEmpty()) purpose = "Not Specified";
                statData.put(purpose, count);
                totalCount += count;
            }
        }
    }
    
    private void loadByQuantity(Connection conn) throws SQLException {
        String sql = "SELECT quantity, COUNT(*) FROM duplicator GROUP BY quantity ORDER BY quantity";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                int qty = rs.getInt(1);
                int count = rs.getInt(2);
                statData.put("Quantity " + qty, count);
                totalCount += qty * count; // Total keys = quantity × record count
            }
        }
    }
    
    private void loadByCategory(Connection conn) throws SQLException {
        String sql = "SELECT key_type, SUM(quantity) FROM duplicator GROUP BY key_type";
        try (Statement stmt = conn.createStatement(); ResultSet rs = stmt.executeQuery(sql)) {
            Map<String, Integer> categoryCounts = new HashMap<>();
            
            while (rs.next()) {
                String keyType = rs.getString(1);
                int count = rs.getInt(2);
                if (keyType == null || keyType.trim().isEmpty()) keyType = "Unknown";
                
                // Find parent category
                String parent = "Other";
                for (String p : AppConfig.getParentCategories()) {
                    for (String child : AppConfig.getChildCategories(p)) {
                        if (child.equalsIgnoreCase(keyType)) {
                            parent = p;
                            break;
                        }
                    }
                    if (!"Other".equals(parent)) break;
                }
                
                categoryCounts.put(parent, categoryCounts.getOrDefault(parent, 0) + count);
                totalCount += count;
            }
            
            // Sort by count desc
            categoryCounts.entrySet().stream()
                .sorted(Map.Entry.<String, Integer>comparingByValue().reversed())
                .forEach(e -> statData.put(e.getKey(), e.getValue()));
        }
    }
    
    private void updateUI() {
        contentPanel.removeAll();
        
        // Title card
        JPanel titleCard = createTitleCard();
        contentPanel.add(titleCard);
        contentPanel.add(Box.createVerticalStrut(20));
        
        // Statistics cards
        if (statData.isEmpty()) {
            JLabel noDataLabel = new JLabel("No data available");
            noDataLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
            noDataLabel.setForeground(new Color(150, 150, 150));
            noDataLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
            contentPanel.add(noDataLabel);
        } else {
            for (Map.Entry<String, Integer> entry : statData.entrySet()) {
                JPanel card = createStatCard(entry.getKey(), entry.getValue());
                contentPanel.add(card);
                contentPanel.add(Box.createVerticalStrut(10));
            }
        }
        
        contentPanel.revalidate();
        contentPanel.repaint();
    }
    
    private JPanel createTitleCard() {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 2),
            BorderFactory.createEmptyBorder(20, 20, 20, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 150));
        
        JLabel titleLabel = new JLabel(currentStat.toString());
        titleLabel.setFont(new Font("Segoe UI", Font.BOLD, 24));
        titleLabel.setForeground(new Color(25, 118, 140));
        titleLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        JLabel countLabel = new JLabel("");
        if (currentStat == StatType.TOTAL_KEYS) {
            countLabel.setText("Total Keys: " + new DecimalFormat("#,###").format(totalCount));
        } else {
            countLabel.setText("Total: " + new DecimalFormat("#,###").format(totalCount) + " keys");
        }
        countLabel.setFont(new Font("Segoe UI", Font.PLAIN, 18));
        countLabel.setForeground(new Color(60, 62, 128));
        countLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        card.add(titleLabel);
        card.add(Box.createVerticalStrut(10));
        card.add(countLabel);
        
        return card;
    }
    
    private JPanel createStatCard(String label, int value) {
        JPanel card = new JPanel();
        card.setLayout(new BorderLayout(15, 0));
        card.setBackground(new Color(245, 250, 252));
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(200, 220, 230), 1),
            BorderFactory.createEmptyBorder(15, 20, 15, 20)
        ));
        card.setMaximumSize(new Dimension(Integer.MAX_VALUE, 60));
        
        JLabel nameLabel = new JLabel(label);
        nameLabel.setFont(new Font("Segoe UI", Font.PLAIN, 16));
        nameLabel.setForeground(new Color(40, 40, 40));
        
        JLabel valueLabel = new JLabel(new DecimalFormat("#,###").format(value));
        valueLabel.setFont(new Font("Segoe UI", Font.BOLD, 18));
        valueLabel.setForeground(new Color(25, 118, 140));
        
        // Progress bar visual
        JPanel progressPanel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                if (totalCount > 0) {
                    Graphics2D g2 = (Graphics2D) g;
                    g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                    
                    int barWidth = (int) ((value / (double) totalCount) * getWidth());
                    g2.setColor(new Color(109, 193, 210, 60));
                    g2.fillRoundRect(0, 0, barWidth, getHeight(), 5, 5);
                }
            }
        };
        progressPanel.setOpaque(false);
        progressPanel.setPreferredSize(new Dimension(200, 30));
        
        JPanel leftPanel = new JPanel(new BorderLayout());
        leftPanel.setOpaque(false);
        leftPanel.add(nameLabel, BorderLayout.WEST);
        
        JPanel rightPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 10, 0));
        rightPanel.setOpaque(false);
        rightPanel.add(valueLabel);
        
        card.add(leftPanel, BorderLayout.WEST);
        card.add(progressPanel, BorderLayout.CENTER);
        card.add(rightPanel, BorderLayout.EAST);
        
        return card;
    }
    
    // Data holder class
    private static class StatItem {
        String label;
        int value;
        
        StatItem(String label, int value) {
            this.label = label;
            this.value = value;
        }
    }
}
