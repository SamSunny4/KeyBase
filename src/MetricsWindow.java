package src;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.*;
import java.sql.*;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.*;
import java.time.temporal.TemporalAdjusters;
import java.util.*;
import java.util.List;

/**
 * Sales metrics and analytics window with interactive charts.
 * Features: Line charts, period navigation (Day/Month/Year), sales breakdowns,
 * avg/peak annotations, % change indicators, and swipe animations.
 */
public class MetricsWindow extends JFrame {
    
    // Period enumeration
    private enum Period {
        DAY("Day"), MONTH("Month"), YEAR("Year");
        private final String label;
        Period(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    
    // Metric type
    private enum MetricType {
        TOTAL_SALES("Total Sales"),
        BY_KEY_TYPE("Sales by Key Type"),
        BY_PURPOSE("Sales by Purpose"),
        BY_QUANTITY("Sales by Quantity");
        
        private final String label;
        MetricType(String label) { this.label = label; }
        @Override public String toString() { return label; }
    }
    
    // State
    private MetricType currentMetric = MetricType.TOTAL_SALES;
    private Period currentPeriod = Period.DAY;
    private LocalDate currentDate = LocalDate.now();
    
    // UI components
    private JPanel chartPanel;
    private JButton btnPrevPeriod;
    private JButton btnNextPeriod;
    private JToggleButton btnDay;
    private JToggleButton btnMonth;
    private JToggleButton btnYear;
    private JLabel lblPeriodTitle;
    private JLabel lblAverage;
    private JLabel lblChange;
    
    // Chart data
    private List<DataPoint> chartData = new ArrayList<>();
    private double averageValue;
    private double changePercent;
    
    public MetricsWindow(Frame owner) {
        setTitle("Sales Metrics & Analytics");
        setSize(1100, 650);
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
        
        // Left sidebar with metric options
        JPanel sidebar = createSidebar();
        add(sidebar, BorderLayout.WEST);
        
        // Right panel with chart and controls
        JPanel rightPanel = new JPanel(new BorderLayout(10, 10));
        rightPanel.setBackground(Color.WHITE);
        rightPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        // Top controls (period selector)
        JPanel topControls = createTopControls();
        rightPanel.add(topControls, BorderLayout.NORTH);
        
        // Chart panel with navigation
        JPanel chartContainer = new JPanel(new BorderLayout(5, 5));
        chartContainer.setBackground(Color.WHITE);
        
        btnPrevPeriod = new JButton("<");
        btnPrevPeriod.setFont(new Font("Arial", Font.BOLD, 20));
        btnPrevPeriod.setPreferredSize(new Dimension(40, 0));
        btnPrevPeriod.setToolTipText("Previous period");
        btnPrevPeriod.setFocusPainted(false);
        btnPrevPeriod.setBorderPainted(false);
        btnPrevPeriod.setContentAreaFilled(false);
        btnPrevPeriod.setForeground(new Color(25, 118, 140));
        btnPrevPeriod.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnPrevPeriod.addActionListener(e -> navigatePeriod(-1));
        
        btnNextPeriod = new JButton(">");
        btnNextPeriod.setFont(new Font("Arial", Font.BOLD, 20));
        btnNextPeriod.setPreferredSize(new Dimension(40, 0));
        btnNextPeriod.setToolTipText("Next period");
        btnNextPeriod.setFocusPainted(false);
        btnNextPeriod.setBorderPainted(false);
        btnNextPeriod.setContentAreaFilled(false);
        btnNextPeriod.setForeground(new Color(25, 118, 140));
        btnNextPeriod.setCursor(new Cursor(Cursor.HAND_CURSOR));
        btnNextPeriod.addActionListener(e -> navigatePeriod(1));
        
        chartPanel = new ChartPanel();
        chartPanel.setBackground(Color.WHITE);
        chartPanel.setBorder(BorderFactory.createLineBorder(new Color(109, 193, 210), 1));
        
        chartContainer.add(btnPrevPeriod, BorderLayout.WEST);
        chartContainer.add(chartPanel, BorderLayout.CENTER);
        chartContainer.add(btnNextPeriod, BorderLayout.EAST);
        
        rightPanel.add(chartContainer, BorderLayout.CENTER);
        
        // Bottom stats
        JPanel statsPanel = createStatsPanel();
        rightPanel.add(statsPanel, BorderLayout.SOUTH);
        
        add(rightPanel, BorderLayout.CENTER);
    }
    
    private JPanel createSidebar() {
        JPanel sidebar = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                
                // Gradient background from deep teal to lighter cyan
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
        
        JLabel title = new JLabel("Sales Analytics");
        title.setFont(new Font("Segoe UI", Font.BOLD, 20));
        title.setForeground(Color.WHITE);
        title.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(title);
        
        JLabel subtitle = new JLabel("Choose a metric");
        subtitle.setFont(new Font("Segoe UI", Font.PLAIN, 11));
        subtitle.setForeground(new Color(180, 220, 230));
        subtitle.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(subtitle);
        sidebar.add(Box.createVerticalStrut(30));
        
        // Modern selection buttons (no radio button appearance)
        List<JPanel> metricButtons = new ArrayList<>();
        for (MetricType type : MetricType.values()) {
            JPanel btn = createModernSelectButton(type.toString(), type, type == currentMetric, metricButtons);
            metricButtons.add(btn);
            sidebar.add(btn);
            sidebar.add(Box.createVerticalStrut(8));
        }
        
        sidebar.add(Box.createVerticalGlue());
        
        // Footer accent
        JLabel footerLabel = new JLabel("KeyBase Analytics");
        footerLabel.setFont(new Font("Segoe UI", Font.ITALIC, 10));
        footerLabel.setForeground(new Color(150, 200, 210));
        footerLabel.setAlignmentX(Component.LEFT_ALIGNMENT);
        sidebar.add(footerLabel);
        
        return sidebar;
    }
    
    private JPanel createModernSelectButton(String text, MetricType type, boolean initialSelected, List<JPanel> allButtons) {
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
                currentMetric = type;
                loadData();
            }
        });
        
        return btnPanel;
    }
    
    private JPanel createTopControls() {
        JPanel panel = new JPanel(new BorderLayout(10, 10));
        panel.setBackground(Color.WHITE);
        
        lblPeriodTitle = new JLabel("Today", SwingConstants.CENTER);
        lblPeriodTitle.setFont(new Font("Arial", Font.BOLD, 16));
        lblPeriodTitle.setForeground(new Color(60, 62, 128));
        panel.add(lblPeriodTitle, BorderLayout.CENTER);
        
        // Period selector (Day/Month/Year)
        JPanel periodSelector = new JPanel(new FlowLayout(FlowLayout.RIGHT, 5, 0));
        periodSelector.setBackground(Color.WHITE);
        
        ButtonGroup periodGroup = new ButtonGroup();
        
        btnDay = new JToggleButton("Day");
        btnMonth = new JToggleButton("Month");
        btnYear = new JToggleButton("Year");
        
        JToggleButton[] periodButtons = {btnDay, btnMonth, btnYear};
        Period[] periods = {Period.DAY, Period.MONTH, Period.YEAR};
        
        for (int i = 0; i < periodButtons.length; i++) {
            JToggleButton btn = periodButtons[i];
            Period period = periods[i];
            btn.setFont(new Font("Arial", Font.PLAIN, 12));
            btn.setBackground(new Color(240, 240, 240));
            btn.setForeground(new Color(60, 62, 128));
            btn.setFocusPainted(false);
            btn.setSelected(period == currentPeriod);
            final Period p = period;
            btn.addActionListener(e -> {
                currentPeriod = p;
                loadData();
            });
            periodGroup.add(btn);
            periodSelector.add(btn);
        }
        
        panel.add(periodSelector, BorderLayout.EAST);
        return panel;
    }
    
    private JPanel createStatsPanel() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 30, 10));
        panel.setBackground(Color.WHITE);
        panel.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(109, 193, 210), 1),
            BorderFactory.createEmptyBorder(10, 20, 10, 20)
        ));
        
        lblAverage = new JLabel("Average: ₹0.00");
        lblAverage.setFont(new Font("Arial", Font.BOLD, 14));
        lblAverage.setForeground(new Color(60, 62, 128));
        
        lblChange = new JLabel("Change: 0%");
        lblChange.setFont(new Font("Arial", Font.BOLD, 14));
        lblChange.setForeground(new Color(60, 62, 128));
        
        panel.add(lblAverage);
        panel.add(lblChange);
        
        return panel;
    }
    
    private void navigatePeriod(int direction) {
        switch (currentPeriod) {
            case DAY:
                currentDate = currentDate.plusDays(direction);
                break;
            case MONTH:
                currentDate = currentDate.plusMonths(direction);
                break;
            case YEAR:
                currentDate = currentDate.plusYears(direction);
                break;
        }
        
        // Animate slide (simple repaint for now; can add Timer-based translation later)
        loadData();
    }
    
    private void loadData() {
        chartData.clear();
        
        try (Connection conn = DatabaseConnection.getConnection()) {
            switch (currentMetric) {
                case TOTAL_SALES:
                    loadTotalSales(conn);
                    break;
                case BY_KEY_TYPE:
                    loadSalesByKeyType(conn);
                    break;
                case BY_PURPOSE:
                    loadSalesByPurpose(conn);
                    break;
                case BY_QUANTITY:
                    loadSalesByQuantity(conn);
                    break;
            }
            
            calculateStats();
            updateUI();
            chartPanel.repaint();
            
        } catch (SQLException e) {
            JOptionPane.showMessageDialog(this,
                "Error loading sales data: " + e.getMessage(),
                "Database Error",
                JOptionPane.ERROR_MESSAGE);
        }
    }
    
    private void loadTotalSales(Connection conn) throws SQLException {
        String sql = buildTotalSalesQuery();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setDateParameters(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String label = rs.getString(1);
                double value = rs.getDouble(2);
                chartData.add(new DataPoint(label, value));
            }
        }
    }
    
    private void loadSalesByKeyType(Connection conn) throws SQLException {
        String sql = buildSalesByDimensionQuery("key_type");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setDateParameters(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String label = rs.getString(1);
                if (label == null || label.trim().isEmpty()) label = "Unknown";
                // Exclude "Vehicles" from key type breakdown
                if ("Vehicles".equalsIgnoreCase(label)) continue;
                double value = rs.getDouble(2);
                chartData.add(new DataPoint(label, value));
            }
        }
    }
    
    private void loadSalesByPurpose(Connection conn) throws SQLException {
        String sql = buildSalesByDimensionQuery("purpose");
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setDateParameters(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                String label = rs.getString(1);
                if (label == null || label.trim().isEmpty()) label = "Unknown";
                double value = rs.getDouble(2);
                chartData.add(new DataPoint(label, value));
            }
        }
    }
    
    private void loadSalesByQuantity(Connection conn) throws SQLException {
        String sql = "SELECT quantity, SUM(amount) FROM duplicator WHERE date_added >= ? AND date_added <= ? GROUP BY quantity ORDER BY quantity";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            setDateParameters(ps);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int qty = rs.getInt(1);
                double value = rs.getDouble(2);
                chartData.add(new DataPoint("Qty " + qty, value));
            }
        }
    }
    
    private String buildTotalSalesQuery() {
        switch (currentPeriod) {
            case DAY:
                // Hourly breakdown for a single day (H2 uses FORMATDATETIME)
                return "SELECT FORMATDATETIME(date_added, 'HH:00'), SUM(amount) FROM duplicator WHERE date_added >= ? AND date_added <= ? GROUP BY FORMATDATETIME(date_added, 'HH:00') ORDER BY 1";
            case MONTH:
                // Daily breakdown for a month
                return "SELECT FORMATDATETIME(date_added, 'dd'), SUM(amount) FROM duplicator WHERE date_added >= ? AND date_added <= ? GROUP BY FORMATDATETIME(date_added, 'dd') ORDER BY CAST(FORMATDATETIME(date_added, 'dd') AS INT)";
            case YEAR:
                // Monthly breakdown for a year
                return "SELECT FORMATDATETIME(date_added, 'MMM'), SUM(amount) FROM duplicator WHERE date_added >= ? AND date_added <= ? GROUP BY FORMATDATETIME(date_added, 'MM'), FORMATDATETIME(date_added, 'MMM') ORDER BY FORMATDATETIME(date_added, 'MM')";
            default:
                return "SELECT 'Total', SUM(amount) FROM duplicator WHERE date_added >= ? AND date_added <= ?";
        }
    }
    
    private String buildSalesByDimensionQuery(String column) {
        return "SELECT " + column + ", SUM(amount) FROM duplicator WHERE date_added >= ? AND date_added <= ? GROUP BY " + column + " ORDER BY 2 DESC";
    }
    
    private void setDateParameters(PreparedStatement ps) throws SQLException {
        LocalDate start, end;
        
        switch (currentPeriod) {
            case DAY:
                start = currentDate;
                end = currentDate;
                break;
            case MONTH:
                start = currentDate.with(TemporalAdjusters.firstDayOfMonth());
                end = currentDate.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case YEAR:
                start = currentDate.with(TemporalAdjusters.firstDayOfYear());
                end = currentDate.with(TemporalAdjusters.lastDayOfYear());
                break;
            default:
                start = currentDate;
                end = currentDate;
        }
        
        ps.setDate(1, java.sql.Date.valueOf(start));
        ps.setDate(2, java.sql.Date.valueOf(end));
    }
    
    private void calculateStats() {
        if (chartData.isEmpty()) {
            averageValue = 0;
            changePercent = 0;
            return;
        }
        
        double sum = 0;
        for (DataPoint dp : chartData) {
            sum += dp.value;
        }
        averageValue = sum / chartData.size();
        
        // Calculate % change vs previous period
        try (Connection conn = DatabaseConnection.getConnection()) {
            double previousSum = getPreviousPeriodTotal(conn);
            double currentSum = sum;
            if (previousSum > 0) {
                changePercent = ((currentSum - previousSum) / previousSum) * 100.0;
            } else {
                changePercent = currentSum > 0 ? 100.0 : 0.0;
            }
        } catch (SQLException e) {
            changePercent = 0;
        }
    }
    
    private double getPreviousPeriodTotal(Connection conn) throws SQLException {
        LocalDate prevStart, prevEnd;
        
        switch (currentPeriod) {
            case DAY:
                prevStart = currentDate.minusDays(1);
                prevEnd = currentDate.minusDays(1);
                break;
            case MONTH:
                LocalDate prevMonth = currentDate.minusMonths(1);
                prevStart = prevMonth.with(TemporalAdjusters.firstDayOfMonth());
                prevEnd = prevMonth.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case YEAR:
                LocalDate prevYear = currentDate.minusYears(1);
                prevStart = prevYear.with(TemporalAdjusters.firstDayOfYear());
                prevEnd = prevYear.with(TemporalAdjusters.lastDayOfYear());
                break;
            default:
                return 0;
        }
        
        String sql = "SELECT SUM(amount) FROM duplicator WHERE date_added >= ? AND date_added <= ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, java.sql.Date.valueOf(prevStart));
            ps.setDate(2, java.sql.Date.valueOf(prevEnd));
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble(1);
            }
        }
        return 0;
    }
    
    private void updateUI() {
        // Update period title
        SimpleDateFormat sdf;
        switch (currentPeriod) {
            case DAY:
                sdf = new SimpleDateFormat("EEEE, MMM dd, yyyy");
                lblPeriodTitle.setText(sdf.format(java.sql.Date.valueOf(currentDate)));
                break;
            case MONTH:
                sdf = new SimpleDateFormat("MMMM yyyy");
                lblPeriodTitle.setText(sdf.format(java.sql.Date.valueOf(currentDate.with(TemporalAdjusters.firstDayOfMonth()))));
                break;
            case YEAR:
                lblPeriodTitle.setText("Year " + currentDate.getYear());
                break;
        }
        
        // Update stats
        DecimalFormat df = new DecimalFormat("#,##0.00");
        lblAverage.setText("Average: ₹" + df.format(averageValue));
        
        String changeText = String.format("Change: %.1f%%", changePercent);
        lblChange.setText(changeText);
        if (changePercent > 0) {
            lblChange.setForeground(new Color(34, 139, 34)); // green
        } else if (changePercent < 0) {
            lblChange.setForeground(new Color(220, 20, 60)); // red
        } else {
            lblChange.setForeground(new Color(60, 62, 128)); // neutral
        }
        
        // Enable/disable navigation
        btnPrevPeriod.setEnabled(true); // Always allow going back
        btnNextPeriod.setEnabled(!currentDate.isAfter(LocalDate.now())); // Don't go into future
    }
    
    // Chart rendering panel
    private class ChartPanel extends JPanel {
        private static final int PADDING = 60;
        
        // Tooltip state for chart hover
        private int hoveredPointIndex = -1;
        private Point mousePosition = null;
        
        public ChartPanel() {
            // Add mouse motion listener for tooltip
            addMouseMotionListener(new MouseAdapter() {
                @Override
                public void mouseMoved(MouseEvent e) {
                    mousePosition = e.getPoint();
                    updateHoveredPoint();
                    repaint();
                }
            });
            
            addMouseListener(new MouseAdapter() {
                @Override
                public void mouseExited(MouseEvent e) {
                    hoveredPointIndex = -1;
                    mousePosition = null;
                    repaint();
                }
            });
        }
        
        private void updateHoveredPoint() {
            if (chartData.isEmpty() || mousePosition == null) {
                hoveredPointIndex = -1;
                return;
            }
            
            int width = getWidth();
            int height = getHeight();
            double maxValue = 0;
            for (DataPoint dp : chartData) {
                if (dp.value > maxValue) maxValue = dp.value;
            }
            if (maxValue == 0) maxValue = 1;
            
            int chartWidth = width - 2 * PADDING;
            int chartHeight = height - 2 * PADDING;
            double xStep = chartWidth / (double)(Math.max(1, chartData.size() - 1));
            
            // Find closest point within threshold
            double minDistance = Double.MAX_VALUE;
            int closestIndex = -1;
            double threshold = 30; // pixels
            
            for (int i = 0; i < chartData.size(); i++) {
                DataPoint dp = chartData.get(i);
                double px = PADDING + i * xStep;
                double py = height - PADDING - (dp.value / maxValue) * chartHeight;
                
                double distance = Math.sqrt(Math.pow(mousePosition.x - px, 2) + Math.pow(mousePosition.y - py, 2));
                if (distance < threshold && distance < minDistance) {
                    minDistance = distance;
                    closestIndex = i;
                }
            }
            
            hoveredPointIndex = closestIndex;
        }
        
        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            Graphics2D g2 = (Graphics2D) g;
            g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            g2.setRenderingHint(RenderingHints.KEY_STROKE_CONTROL, RenderingHints.VALUE_STROKE_PURE);
            
            if (chartData.isEmpty()) {
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 16));
                g2.setColor(new Color(150, 150, 150));
                String msg = "No data available for this period";
                FontMetrics fm = g2.getFontMetrics();
                int x = (getWidth() - fm.stringWidth(msg)) / 2;
                int y = getHeight() / 2;
                g2.drawString(msg, x, y);
                return;
            }
            
            int width = getWidth();
            int height = getHeight();
            
            // Find max value and peak
            double maxValue = 0;
            int peakIndex = 0;
            for (int i = 0; i < chartData.size(); i++) {
                if (chartData.get(i).value > maxValue) {
                    maxValue = chartData.get(i).value;
                    peakIndex = i;
                }
            }
            
            if (maxValue == 0) maxValue = 1; // Avoid division by zero
            
            // Draw subtle grid
            g2.setColor(new Color(240, 245, 250));
            int gridLines = 5;
            for (int i = 0; i <= gridLines; i++) {
                int y = height - PADDING - (int)((height - 2 * PADDING) * i / (double)gridLines);
                g2.drawLine(PADDING, y, width - PADDING, y);
            }
            
            // Draw axes with softer color
            g2.setColor(new Color(200, 210, 220));
            g2.setStroke(new BasicStroke(2));
            g2.drawLine(PADDING, height - PADDING, width - PADDING, height - PADDING); // X axis
            g2.drawLine(PADDING, PADDING, PADDING, height - PADDING); // Y axis
            
            // Y-axis labels
            g2.setColor(new Color(120, 130, 140));
            g2.setFont(new Font("Segoe UI", Font.PLAIN, 11));
            for (int i = 0; i <= gridLines; i++) {
                int y = height - PADDING - (int)((height - 2 * PADDING) * i / (double)gridLines);
                String label = String.format("%.0f", maxValue * i / gridLines);
                g2.drawString(label, 5, y + 5);
            }
            
            // Build gradient fill area path
            int chartWidth = width - 2 * PADDING;
            int chartHeight = height - 2 * PADDING;
            double xStep = chartWidth / (double)(Math.max(1, chartData.size() - 1));
            
            Path2D.Double fillPath = new Path2D.Double();
            fillPath.moveTo(PADDING, height - PADDING);
            
            for (int i = 0; i < chartData.size(); i++) {
                DataPoint dp = chartData.get(i);
                double x = PADDING + i * xStep;
                double y = height - PADDING - (dp.value / maxValue) * chartHeight;
                fillPath.lineTo(x, y);
            }
            
            fillPath.lineTo(PADDING + (chartData.size() - 1) * xStep, height - PADDING);
            fillPath.closePath();
            
            // Fill gradient (cyan to transparent)
            GradientPaint gradientFill = new GradientPaint(
                0, PADDING, new Color(109, 193, 210, 80),
                0, height - PADDING, new Color(109, 193, 210, 5)
            );
            g2.setPaint(gradientFill);
            g2.fill(fillPath);
            
            // Draw line chart with thicker stroke
            g2.setColor(new Color(25, 118, 140));
            g2.setStroke(new BasicStroke(3, BasicStroke.CAP_ROUND, BasicStroke.JOIN_ROUND));
            
            Path2D.Double linePath = new Path2D.Double();
            for (int i = 0; i < chartData.size(); i++) {
                DataPoint dp = chartData.get(i);
                double x = PADDING + i * xStep;
                double y = height - PADDING - (dp.value / maxValue) * chartHeight;
                
                if (i == 0) {
                    linePath.moveTo(x, y);
                } else {
                    linePath.lineTo(x, y);
                }
            }
            g2.draw(linePath);
            
            // Draw points and labels
            g2.setStroke(new BasicStroke(1));
            for (int i = 0; i < chartData.size(); i++) {
                DataPoint dp = chartData.get(i);
                double x = PADDING + i * xStep;
                double y = height - PADDING - (dp.value / maxValue) * chartHeight;
                
                boolean isHovered = (i == hoveredPointIndex);
                
                // Point circle with subtle shadow (larger if hovered)
                int pointSize = isHovered ? 14 : 10;
                g2.setColor(new Color(0, 0, 0, 30));
                g2.fillOval((int)x - pointSize/2, (int)y - pointSize/2 + 1, pointSize + 1, pointSize + 1);
                
                g2.setColor(new Color(25, 118, 140));
                g2.fillOval((int)x - pointSize/2, (int)y - pointSize/2, pointSize, pointSize);
                
                g2.setColor(Color.WHITE);
                int innerSize = isHovered ? 8 : 6;
                g2.fillOval((int)x - innerSize/2, (int)y - innerSize/2, innerSize, innerSize);
                
                // X-axis label
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(100, 110, 120));
                FontMetrics fm = g2.getFontMetrics();
                int labelWidth = fm.stringWidth(dp.label);
                g2.drawString(dp.label, (int)x - labelWidth / 2, height - PADDING + 20);
                
                // Annotate peak with modern badge
                if (i == peakIndex && hoveredPointIndex == -1) {
                    DecimalFormat df = new DecimalFormat("#,##0.00");
                    String valueLabel = "₹" + df.format(dp.value);
                    
                    g2.setFont(new Font("Segoe UI", Font.BOLD, 12));
                    FontMetrics vfm = g2.getFontMetrics();
                    int vWidth = vfm.stringWidth(valueLabel);
                    int vHeight = vfm.getHeight();
                    
                    int badgeX = (int)x - vWidth / 2 - 8;
                    int badgeY = (int)y - vHeight - 18;
                    int badgeWidth = vWidth + 16;
                    int badgeHeight = vHeight + 8;
                    
                    // Badge shadow
                    g2.setColor(new Color(0, 0, 0, 20));
                    g2.fillRoundRect(badgeX + 2, badgeY + 2, badgeWidth, badgeHeight, 8, 8);
                    
                    // Badge background
                    g2.setColor(new Color(220, 20, 60));
                    g2.fillRoundRect(badgeX, badgeY, badgeWidth, badgeHeight, 8, 8);
                    
                    // Badge text
                    g2.setColor(Color.WHITE);
                    g2.drawString(valueLabel, badgeX + 8, badgeY + vHeight - 2);
                }
            }
            
            // Draw hover tooltip
            if (hoveredPointIndex >= 0 && hoveredPointIndex < chartData.size()) {
                DataPoint dp = chartData.get(hoveredPointIndex);
                double px = PADDING + hoveredPointIndex * xStep;
                double py = height - PADDING - (dp.value / maxValue) * chartHeight;
                
                DecimalFormat df = new DecimalFormat("#,##0.00");
                String valueText = "₹" + df.format(dp.value);
                String labelText = dp.label;
                
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                FontMetrics vfm = g2.getFontMetrics();
                int vWidth = vfm.stringWidth(valueText);
                
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                FontMetrics lfm = g2.getFontMetrics();
                int lWidth = lfm.stringWidth(labelText);
                
                int tooltipWidth = Math.max(vWidth, lWidth) + 20;
                int tooltipHeight = 48;
                int tooltipX = (int)px - tooltipWidth / 2;
                int tooltipY = (int)py - tooltipHeight - 15;
                
                // Keep tooltip within bounds
                if (tooltipX < 10) tooltipX = 10;
                if (tooltipX + tooltipWidth > width - 10) tooltipX = width - tooltipWidth - 10;
                if (tooltipY < 10) tooltipY = (int)py + 20;
                
                // Tooltip shadow
                g2.setColor(new Color(0, 0, 0, 40));
                g2.fillRoundRect(tooltipX + 2, tooltipY + 2, tooltipWidth, tooltipHeight, 10, 10);
                
                // Tooltip background with gradient
                GradientPaint tooltipGrad = new GradientPaint(
                    tooltipX, tooltipY, new Color(25, 118, 140),
                    tooltipX, tooltipY + tooltipHeight, new Color(15, 76, 92)
                );
                g2.setPaint(tooltipGrad);
                g2.fillRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 10, 10);
                
                // Border
                g2.setColor(new Color(109, 193, 210));
                g2.setStroke(new BasicStroke(1.5f));
                g2.drawRoundRect(tooltipX, tooltipY, tooltipWidth, tooltipHeight, 10, 10);
                
                // Text
                g2.setFont(new Font("Segoe UI", Font.BOLD, 11));
                g2.setColor(Color.WHITE);
                g2.drawString(valueText, tooltipX + (tooltipWidth - vWidth) / 2, tooltipY + 20);
                
                g2.setFont(new Font("Segoe UI", Font.PLAIN, 10));
                g2.setColor(new Color(200, 230, 240));
                g2.drawString(labelText, tooltipX + (tooltipWidth - lWidth) / 2, tooltipY + 36);
            }
        }
    }
    
    // Data point model
    private static class DataPoint {
        String label;
        double value;
        
        DataPoint(String label, double value) {
            this.label = label;
            this.value = value;
        }
    }
}
