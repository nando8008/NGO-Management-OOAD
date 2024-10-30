import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;

public class ProposedProject extends JFrame {
    private JTable projectTable;
    private JButton acceptEventButton;
    private JButton rejectEventButton;
    private DefaultTableModel tableModel;

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

    private void acceptSelectedEvent() {
        int selectedRow = projectTable.getSelectedRow();

        if (selectedRow >= 0) {
            String eventName = (String) tableModel.getValueAt(selectedRow, 0);
            String eventType = (String) tableModel.getValueAt(selectedRow, 1);

            // Prompt for event date
            String eventDateStr = JOptionPane.showInputDialog(
                    this, "Enter the date for the event (YYYY-MM-DD):", "Event Date", JOptionPane.PLAIN_MESSAGE);

            if (eventDateStr != null && !eventDateStr.trim().isEmpty()) {
                try {
                    LocalDate eventDate = LocalDate.parse(eventDateStr, DateTimeFormatter.ISO_LOCAL_DATE);

                    // Insert event into the events table
                    insertEventIntoDatabase(eventName, eventDate, eventType);

                    // Remove event from event_submission table
                    deleteSubmittedProject(eventName, eventType);

                    // Remove row from the table model
                    tableModel.removeRow(selectedRow);

                    JOptionPane.showMessageDialog(this, "Event has been successfully scheduled!");
                } catch (DateTimeParseException e) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please enter the date as YYYY-MM-DD.");
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
}
