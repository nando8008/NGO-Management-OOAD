import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;

public class ManagePastEvents extends JFrame {
    private JTable eventsTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;

    public ManagePastEvents() {
        setTitle("Manage Past Events");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create table model and table for events
        String[] columnNames = {"Event Name", "Event Date", "Event Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventsTable = new JTable(tableModel);

        loadEventStatusData();

        JScrollPane scrollPane = new JScrollPane(eventsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Add, Delete, and Update buttons
        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        updateButton = new JButton("Update");

        addButton.addActionListener(this::handleAddEvent);
        deleteButton.addActionListener(this::handleDeleteEvent);
        updateButton.addActionListener(this::handleUpdateEvent);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadEventStatusData() {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT event_name, event_occured_date, event_x_status FROM event_status";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String eventName = rs.getString("event_name");
                    Date eventDate = rs.getDate("event_occured_date");
                    String eventStatus = rs.getString("event_x_status");
                    tableModel.addRow(new Object[]{eventName, eventDate.toString(), eventStatus});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    private void handleAddEvent(ActionEvent e) {
        JTextField eventNameField = new JTextField();
        JTextField eventDateField = new JTextField();
        JTextField eventStatusField = new JTextField();
        Object[] message = {"Event Name:", eventNameField, "Event Date (YYYY-MM-DD):", eventDateField, "Event Status:", eventStatusField};

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Event", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String eventName = eventNameField.getText().trim();
            String eventDateStr = eventDateField.getText().trim();
            String eventStatus = eventStatusField.getText().trim();

            try {
                Date eventDate = Date.valueOf(eventDateStr); // Convert String to java.sql.Date

                Connection conn = DatabaseConnection.connect();
                if (conn != null) {
                    String sql = "INSERT INTO event_status (event_name, event_occured_date, event_x_status) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, eventName);
                        stmt.setDate(2, eventDate);
                        stmt.setString(3, eventStatus);
                        stmt.executeUpdate();
                        tableModel.addRow(new Object[]{eventName, eventDate.toString(), eventStatus});
                        JOptionPane.showMessageDialog(this, "Event added successfully.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error adding event: " + ex.getMessage());
                    } finally {
                        DatabaseConnection.disconnect(conn);
                    }
                }
            } catch (IllegalArgumentException ex) {
                JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.");
            }
        }
    }

    private void handleDeleteEvent(ActionEvent e) {
        int selectedRow = eventsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String eventName = (String) tableModel.getValueAt(selectedRow, 0);

            Connection conn = DatabaseConnection.connect();
            if (conn != null) {
                String sql = "DELETE FROM event_status WHERE event_name = ?";
                try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                    stmt.setString(1, eventName);
                    stmt.executeUpdate();
                    tableModel.removeRow(selectedRow);
                    JOptionPane.showMessageDialog(this, "Event deleted successfully.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting event: " + ex.getMessage());
                } finally {
                    DatabaseConnection.disconnect(conn);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.");
        }
    }

    private void handleUpdateEvent(ActionEvent e) {
        int selectedRow = eventsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String oldEventName = (String) tableModel.getValueAt(selectedRow, 0);
            JTextField eventNameField = new JTextField(oldEventName);
            JTextField eventDateField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            JTextField eventStatusField = new JTextField((String) tableModel.getValueAt(selectedRow, 2));
            Object[] message = {"Event Name:", eventNameField, "Event Date (YYYY-MM-DD):", eventDateField, "Event Status:", eventStatusField};

            int option = JOptionPane.showConfirmDialog(this, message, "Update Event", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String newEventName = eventNameField.getText().trim();
                String newEventDateStr = eventDateField.getText().trim();
                String newEventStatus = eventStatusField.getText().trim();

                try {
                    Date newEventDate = Date.valueOf(newEventDateStr); // Convert String to java.sql.Date

                    Connection conn = DatabaseConnection.connect();
                    if (conn != null) {
                        String sql = "UPDATE event_status SET event_name = ?, event_occured_date = ?, event_x_status = ? WHERE event_name = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, newEventName);
                            stmt.setDate(2, newEventDate);
                            stmt.setString(3, newEventStatus);
                            stmt.setString(4, oldEventName);
                            stmt.executeUpdate();

                            tableModel.setValueAt(newEventName, selectedRow, 0);
                            tableModel.setValueAt(newEventDate.toString(), selectedRow, 1);
                            tableModel.setValueAt(newEventStatus, selectedRow, 2);

                            JOptionPane.showMessageDialog(this, "Event updated successfully.");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Error updating event: " + ex.getMessage());
                        } finally {
                            DatabaseConnection.disconnect(conn);
                        }
                    }
                } catch (IllegalArgumentException ex) {
                    JOptionPane.showMessageDialog(this, "Invalid date format. Please use YYYY-MM-DD.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to update.");
        }
    }
}
