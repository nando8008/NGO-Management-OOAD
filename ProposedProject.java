import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Calendar;

public class ProposedProject extends JFrame {
    private JTable projectTable;
    private JButton acceptEventButton;
    private JButton rejectEventButton;
    private DefaultTableModel tableModel;

    // Search components
    private JTextField searchField;
    private JButton searchButton;

    public ProposedProject() {
        setTitle("Submitted Projects");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Column names for the table
        String[] columnNames = {"Event Name", "Event Type"};

        // Table model to hold data
        tableModel = new DefaultTableModel(columnNames, 0);
        projectTable = new JTable(tableModel);

        // Fetch submitted projects from database and add to table
        fetchSubmittedProjects();

        // Add table to scroll pane and then to frame
        JScrollPane scrollPane = new JScrollPane(projectTable);
        add(scrollPane, BorderLayout.CENTER);

        // Panel for buttons
        JPanel buttonPanel = new JPanel(new FlowLayout());

        // Accept event button
        acceptEventButton = new JButton("Accept");
        acceptEventButton.setPreferredSize(new Dimension(150, 40));
        acceptEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                acceptSelectedEvent();
            }
        });

        // Reject event button
        rejectEventButton = new JButton("Reject");
        rejectEventButton.setPreferredSize(new Dimension(150, 40));
        rejectEventButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                rejectSelectedEvent();
            }
        });

        // Add buttons to the panel
        buttonPanel.add(acceptEventButton);
        buttonPanel.add(rejectEventButton);

        // Create a panel for the search bar
        JPanel searchPanel = new JPanel(new FlowLayout());
        searchField = new JTextField(20);
        searchButton = new JButton("Search");
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterProjects();
            }
        });

        searchPanel.add(new JLabel("Search by Event Name/Type:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Add search panel to the top of the frame
        add(searchPanel, BorderLayout.NORTH);

        // Add button panel to the frame
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchSubmittedProjects() {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT event_s_name, event_s_type FROM event_submission";
            try (PreparedStatement stmt = conn.prepareStatement(sql);
                 ResultSet rs = stmt.executeQuery()) {

                // Loop through the result set and add rows to the table model
                while (rs.next()) {
                    String eventName = rs.getString("event_s_name");
                    String eventType = rs.getString("event_s_type");
                    tableModel.addRow(new Object[]{eventName, eventType});
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    private void filterProjects() {
        String searchText = searchField.getText().toLowerCase();
        // Clear existing rows and re-fetch data based on search criteria
        tableModel.setRowCount(0);

        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT event_s_name, event_s_type FROM event_submission WHERE LOWER(event_s_name) LIKE ? OR LOWER(event_s_type) LIKE ?";
            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                String likeSearchText = "%" + searchText + "%";
                stmt.setString(1, likeSearchText);
                stmt.setString(2, likeSearchText);
                ResultSet rs = stmt.executeQuery();

                // Loop through the result set and add rows to the table model
                while (rs.next()) {
                    String eventName = rs.getString("event_s_name");
                    String eventType = rs.getString("event_s_type");
                    tableModel.addRow(new Object[]{eventName, eventType});
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    private void acceptSelectedEvent() {
        int selectedRow = projectTable.getSelectedRow();

        if (selectedRow >= 0) {
            String eventName = (String) tableModel.getValueAt(selectedRow, 0);
            String eventType = (String) tableModel.getValueAt(selectedRow, 1);

            // Prompt for event date using separate dropdowns for day, month, and year
            JPanel datePanel = new JPanel(new GridLayout(3, 2));
            JComboBox<String> dayComboBox = new JComboBox<>(generateDays());
            JComboBox<String> monthComboBox = new JComboBox<>(generateMonths());
            JComboBox<String> yearComboBox = new JComboBox<>(generateYears());

            datePanel.add(new JLabel("Day:"));
            datePanel.add(dayComboBox);
            datePanel.add(new JLabel("Month:"));
            datePanel.add(monthComboBox);
            datePanel.add(new JLabel("Year:"));
            datePanel.add(yearComboBox);

            int option = JOptionPane.showConfirmDialog(
                    this, datePanel, "Select Event Date", JOptionPane.OK_CANCEL_OPTION);

            if (option == JOptionPane.OK_OPTION) {
                String day = (String) dayComboBox.getSelectedItem();
                String month = (String) monthComboBox.getSelectedItem();
                String year = (String) yearComboBox.getSelectedItem();

                try {
                    // Convert month abbreviation to numeric month (Jan -> 1, Feb -> 2, etc.)
                    int monthNumber = monthToNumber(month);

                    LocalDate eventDate = LocalDate.of(
                            Integer.parseInt(year), monthNumber, Integer.parseInt(day));

                    // Insert event into the events table
                    insertEventIntoDatabase(eventName, eventDate, eventType);

                    // Remove event from event_submission table
                    deleteSubmittedProject(eventName, eventType);

                    // Remove row from the table model
                    tableModel.removeRow(selectedRow);

                    JOptionPane.showMessageDialog(this, "Event has been successfully scheduled!");
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Invalid date selection.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to accept.");
        }
    }

    private void rejectSelectedEvent() {
        int selectedRow = projectTable.getSelectedRow();

        if (selectedRow >= 0) {
            String eventName = (String) tableModel.getValueAt(selectedRow, 0);
            String eventType = (String) tableModel.getValueAt(selectedRow, 1);

            // Remove event from event_submission table
            deleteSubmittedProject(eventName, eventType);

            // Remove row from the table model
            tableModel.removeRow(selectedRow);

            JOptionPane.showMessageDialog(this, "Event has been rejected.");
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to reject.");
        }
    }

    private void insertEventIntoDatabase(String eventName, LocalDate eventDate, String eventType) {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "INSERT INTO events (event_name, event_date, event_type) VALUES (?, ?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, eventName);
                stmt.setDate(2, Date.valueOf(eventDate));
                stmt.setString(3, eventType);

                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    private void deleteSubmittedProject(String eventName, String eventType) {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "DELETE FROM event_submission WHERE event_s_name = ? AND event_s_type = ?";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, eventName);
                stmt.setString(2, eventType);

                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + e.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    // Helper methods to generate days, months, and years
    private String[] generateDays() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format("%02d", i + 1);
        }
        return days;
    }

    private String[] generateMonths() {
        return new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    }

    private String[] generateYears() {
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[10]; // Example: range of 10 years
        for (int i = 0; i < 10; i++) {
            years[i] = String.valueOf(currentYear + i);
        }
        return years;
    }

    // Convert month abbreviation to numeric value
    private int monthToNumber(String month) {
        Calendar cal = Calendar.getInstance();
        cal.set(Calendar.MONTH, getMonthIndex(month));
        return cal.get(Calendar.MONTH) + 1; // Calendar.MONTH is zero-based
    }

    private int getMonthIndex(String month) {
        switch (month) {
            case "Jan": return 0;
            case "Feb": return 1;
            case "Mar": return 2;
            case "Apr": return 3;
            case "May": return 4;
            case "Jun": return 5;
            case "Jul": return 6;
            case "Aug": return 7;
            case "Sep": return 8;
            case "Oct": return 9;
            case "Nov": return 10;
            case "Dec": return 11;
            default: throw new IllegalArgumentException("Invalid month: " + month);
        }
    }
}
