package src;

import java.awt.*;
import java.awt.event.*;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import javax.swing.*;
import javax.swing.border.LineBorder;

/**
 * A date chooser component with visual calendar picker
 */
public class JDateChooser extends JPanel {
    private static final Color CYAN = new Color(109, 193, 210); // #6DC1D2
    private static final Color DARK_PURPLE = new Color(60, 62, 128); // #3C3E80
    
    private final JTextField dateField = new JTextField(10);
    private final JButton calendarButton = new JButton("📅") {{
        setFont(getFont().deriveFont(20f));
        setPreferredSize(new Dimension(40, 30));
    }};
    private Date selectedDate = null;
    private JDialog calendarDialog;
    
    public JDateChooser() {
        setLayout(new BorderLayout());
        setBackground(Color.WHITE);
        
        dateField.setEditable(false);
        dateField.setBackground(Color.WHITE);
        
        calendarButton.setBackground(CYAN);
        calendarButton.setForeground(DARK_PURPLE);
        calendarButton.setFocusPainted(false);
        calendarButton.setBorder(BorderFactory.createLineBorder(DARK_PURPLE, 1));
        
        add(dateField, BorderLayout.CENTER);
        add(calendarButton, BorderLayout.EAST);
        
        calendarButton.addActionListener(e -> showCalendar());
    }
    
    private void showCalendar() {
        Window parentWindow = SwingUtilities.getWindowAncestor(this);
        if (parentWindow instanceof Frame) {
            calendarDialog = new JDialog((Frame) parentWindow, "Select Date", true);
        } else if (parentWindow instanceof Dialog) {
            calendarDialog = new JDialog((Dialog) parentWindow, "Select Date", true);
        } else {
            calendarDialog = new JDialog((Frame) null, "Select Date", true);
        }
        calendarDialog.setLayout(new BorderLayout());
        calendarDialog.setBackground(Color.WHITE);
        
        Calendar cal = Calendar.getInstance();
        if (selectedDate != null) {
            cal.setTime(selectedDate);
        }
        
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(DARK_PURPLE);
        headerPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        
        JButton prevMonth = new JButton("◀"){{
        setFont(getFont().deriveFont(20f));
        setPreferredSize(new Dimension(40, 30));
    }};
        JButton nextMonth = new JButton("▶"){{
        setFont(getFont().deriveFont(20f));
        setPreferredSize(new Dimension(40, 30));
    }};
        
        // Center panel with month and year on two lines
        JPanel centerPanel = new JPanel();
        centerPanel.setLayout(new BoxLayout(centerPanel, BoxLayout.Y_AXIS));
        centerPanel.setBackground(DARK_PURPLE);
        
        JLabel monthLabel = new JLabel();
        monthLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        monthLabel.setForeground(Color.WHITE);
        monthLabel.setFont(new Font("Arial", Font.BOLD, 15));
        
        // Transparent year button overlay
        JButton yearButton = new JButton();
        yearButton.setAlignmentX(Component.CENTER_ALIGNMENT);
        yearButton.setOpaque(false);
        yearButton.setContentAreaFilled(false);
        yearButton.setBorderPainted(false);
        yearButton.setFocusPainted(false);
        yearButton.setForeground(Color.WHITE);
        yearButton.setFont(new Font("Arial", Font.PLAIN, 13));
        yearButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        yearButton.setMargin(new Insets(0, 0, 0, 0));
        
        centerPanel.add(monthLabel);
        centerPanel.add(Box.createVerticalStrut(2)); // Minimal spacing
        centerPanel.add(yearButton);
        
        prevMonth.setBackground(CYAN);
        prevMonth.setForeground(DARK_PURPLE);
        prevMonth.setFocusPainted(false);
        prevMonth.setPreferredSize(new Dimension(40, 40));
        prevMonth.setFont(new Font("Arial", Font.BOLD, 14));
        nextMonth.setBackground(CYAN);
        nextMonth.setForeground(DARK_PURPLE);
        nextMonth.setFocusPainted(false);
        nextMonth.setPreferredSize(new Dimension(40, 40));
        nextMonth.setFont(new Font("Arial", Font.BOLD, 14));
        
        headerPanel.add(prevMonth, BorderLayout.WEST);
        headerPanel.add(centerPanel, BorderLayout.CENTER);
        headerPanel.add(nextMonth, BorderLayout.EAST);
        
        JPanel calendarPanel = new JPanel(new GridLayout(0, 7, 3, 3)); // Slightly larger gaps
        calendarPanel.setBackground(Color.WHITE);
        calendarPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        final int[] currentMonth = {cal.get(Calendar.MONTH)};
        final int[] currentYear = {cal.get(Calendar.YEAR)};
        
        updateCalendar(calendarPanel, currentMonth[0], currentYear[0], monthLabel, yearButton);
        
        prevMonth.addActionListener(e -> {
            currentMonth[0]--;
            if (currentMonth[0] < 0) {
                currentMonth[0] = 11;
                currentYear[0]--;
            }
            updateCalendar(calendarPanel, currentMonth[0], currentYear[0], monthLabel, yearButton);
        });
        
        nextMonth.addActionListener(e -> {
            currentMonth[0]++;
            if (currentMonth[0] > 11) {
                currentMonth[0] = 0;
                currentYear[0]++;
            }
            updateCalendar(calendarPanel, currentMonth[0], currentYear[0], monthLabel, yearButton);
        });
        
        yearButton.addActionListener(e -> {
            String[] years = new String[100];
            int startYear = currentYear[0] - 50;
            for (int i = 0; i < 100; i++) {
                years[i] = String.valueOf(startYear + i);
            }
            
            String selectedYear = (String) JOptionPane.showInputDialog(
                calendarDialog,
                "Select Year:",
                "Year",
                JOptionPane.PLAIN_MESSAGE,
                null,
                years,
                String.valueOf(currentYear[0])
            );
            
            if (selectedYear != null) {
                currentYear[0] = Integer.parseInt(selectedYear);
                updateCalendar(calendarPanel, currentMonth[0], currentYear[0], monthLabel, yearButton);
            }
        });
        
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        buttonPanel.setBackground(Color.WHITE);
        buttonPanel.setBorder(BorderFactory.createEmptyBorder(5, 10, 10, 10));
        
        JButton todayButton = new JButton("Today");
        JButton clearButton = new JButton("Clear");
        
        todayButton.setBackground(CYAN);
        todayButton.setForeground(DARK_PURPLE);
        todayButton.setFocusPainted(false);
        clearButton.setBackground(CYAN);
        clearButton.setForeground(DARK_PURPLE);
        clearButton.setFocusPainted(false);
        
        todayButton.addActionListener(e -> {
            setDate(new Date());
            calendarDialog.dispose();
        });
        
        clearButton.addActionListener(e -> {
            setDate(null);
            calendarDialog.dispose();
        });
        
        buttonPanel.add(todayButton);
        buttonPanel.add(clearButton);
        
        calendarDialog.add(headerPanel, BorderLayout.NORTH);
        calendarDialog.add(calendarPanel, BorderLayout.CENTER);
        calendarDialog.add(buttonPanel, BorderLayout.SOUTH);
        
        calendarDialog.pack();
        calendarDialog.setLocationRelativeTo(this);
        calendarDialog.setVisible(true);
    }
    
    private void updateCalendar(JPanel calendarPanel, int month, int year, JLabel monthLabel, JButton yearButton) {
        calendarPanel.removeAll();
        
        // Short month names
        String[] monthNames = {"January", "February", "March", "April", "May", "June", 
                               "July", "August", "September", "October", "November", "December"};
        monthLabel.setText(monthNames[month]);
        yearButton.setText(String.valueOf(year));
        
        String[] dayNames = {"S", "M", "T", "W", "T", "F", "S"};
        for (String dayName : dayNames) {
            JLabel dayLabel = new JLabel(dayName, SwingConstants.CENTER);
            dayLabel.setFont(new Font("Arial", Font.BOLD, 13));
            dayLabel.setForeground(DARK_PURPLE);
            dayLabel.setPreferredSize(new Dimension(35, 30));
            calendarPanel.add(dayLabel);
        }
        
        Calendar cal = Calendar.getInstance();
        cal.set(year, month, 1);
        
        int firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK) - 1;
        int daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH);
        
        for (int i = 0; i < firstDayOfWeek; i++) {
            calendarPanel.add(new JLabel(""));
        }
        
        for (int day = 1; day <= daysInMonth; day++) {
            final int selectedDay = day;
            JButton dayButton = new JButton(String.valueOf(day));
            dayButton.setPreferredSize(new Dimension(35, 30));
            dayButton.setFont(new Font("Arial", Font.PLAIN, 13));
            dayButton.setBackground(Color.WHITE);
            dayButton.setForeground(DARK_PURPLE);
            dayButton.setBorder(new LineBorder(CYAN, 1));
            dayButton.setFocusPainted(false);
            
            Calendar today = Calendar.getInstance();
            if (day == today.get(Calendar.DAY_OF_MONTH) && 
                month == today.get(Calendar.MONTH) && 
                year == today.get(Calendar.YEAR)) {
                dayButton.setBackground(Color.RED);
                dayButton.setForeground(Color.BLACK);
                dayButton.setFont(new Font("Eras Bold ITC", Font.BOLD, 13));
            }
            
            dayButton.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseEntered(MouseEvent e) {
                    if (!dayButton.getBackground().equals(DARK_PURPLE)) {
                        dayButton.setBackground(new Color(109, 193, 210, 100));
                    }
                }
                
                @Override
                public void mouseExited(MouseEvent e) {
                    Calendar today = Calendar.getInstance();
                    if (!(selectedDay == today.get(Calendar.DAY_OF_MONTH) && 
                          month == today.get(Calendar.MONTH) && 
                          year == today.get(Calendar.YEAR))) {
                        dayButton.setBackground(Color.WHITE);
                    }
                }
            });
            
            dayButton.addActionListener(e -> {
                Calendar selected = Calendar.getInstance();
                selected.set(year, month, selectedDay, 0, 0, 0);
                selected.set(Calendar.MILLISECOND, 0);
                setDate(selected.getTime());
                calendarDialog.dispose();
            });
            
            calendarPanel.add(dayButton);
        }
        
        calendarPanel.revalidate();
        calendarPanel.repaint();
    }
    
    public Date getDate() {
        return selectedDate;
    }
    
    public void setDate(Date date) {
        selectedDate = date;
        if (date != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("dd MMM yyyy");
            dateField.setText(sdf.format(date));
        } else {
            dateField.setText("");
        }
    }
    
    public JTextField getDateField() {
        return dateField;
    }
}