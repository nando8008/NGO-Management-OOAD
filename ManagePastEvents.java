import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManagePastEvents extends JFrame {
    private JTable eventsTable;
    private DefaultTableModel tableModel;
    private JButton addButton;
    private JButton deleteButton;
    private JButton updateButton;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaComboBox;
    private List<Event> events;

    public ManagePastEvents() {
        setTitle("Manage Past Events");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        events = new ArrayList<>();

        // Create table model and table for events
        String[] columnNames = {"Event Name", "Event Date", "Event Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventsTable = new JTable(tableModel);

        loadEventStatusData();

        JScrollPane scrollPane = new JScrollPane(eventsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Search panel with search field and criteria combo box
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchCriteriaComboBox = new JComboBox<>(new String[]{"Event Name", "Event Date", "Event Status"});
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> searchEvents());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("by"));
        searchPanel.add(searchCriteriaComboBox);
        searchPanel.add(searchButton);

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        addButton = new JButton("Add");
        deleteButton = new JButton("Delete");
        updateButton = new JButton("Update");

        addButton.addActionListener(this::handleAddEvent);
        deleteButton.addActionListener(this::handleDeleteEvent);
        updateButton.addActionListener(this::handleUpdateEvent);

        buttonPanel.add(addButton);
        buttonPanel.add(deleteButton);
        buttonPanel.add(updateButton);

        // Add panels to frame
        add(searchPanel, BorderLayout.NORTH);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadEventStatusData() {
        events.clear();  // Clear the list to avoid duplicates when reloading
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT event_name, event_occured_date, event_x_status FROM event_status";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String eventName = rs.getString("event_name");
                    Date eventDate = rs.getDate("event_occured_date");
                    String eventStatus = rs.getString("event_x_status");
                    events.add(new Event(eventName, eventDate, eventStatus));
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

    private void displayEvents(List<Event> eventList) {
        tableModel.setRowCount(0); // Clear the table
        for (Event event : eventList) {
            tableModel.addRow(new Object[]{event.getName(), event.getDate().toString(), event.getStatus()});
        }
    }

    private void searchEvents() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        String criteria = (String) searchCriteriaComboBox.getSelectedItem();

        List<Event> filteredEvents = new ArrayList<>();
        for (Event event : events) {
            boolean matches = switch (criteria) {
                case "Event Name" -> event.getName().toLowerCase().contains(searchTerm);
                case "Event Date" -> event.getDate().toString().contains(searchTerm);
                case "Event Status" -> event.getStatus().toLowerCase().contains(searchTerm);
                default -> false;
            };

            if (matches) {
                filteredEvents.add(event);
            }
        }
        displayEvents(filteredEvents);
    }

    // Existing methods (handleAddEvent, handleDeleteEvent, handleUpdateEvent) remain the same.
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
                Date eventDate = Date.valueOf(eventDateStr);

                Connection conn = DatabaseConnection.connect();
                if (conn != null) {
                    String sql = "INSERT INTO event_status (event_name, event_occured_date, event_x_status) VALUES (?, ?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, eventName);
                        stmt.setDate(2, eventDate);
                        stmt.setString(3, eventStatus);
                        stmt.executeUpdate();
                        events.add(new Event(eventName, eventDate, eventStatus));
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
                    events.removeIf(event -> event.getName().equals(eventName));
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
                    Date newEventDate = Date.valueOf(newEventDateStr);

                    Connection conn = DatabaseConnection.connect();
                    if (conn != null) {
                        String sql = "UPDATE event_status SET event_name = ?, event_occured_date = ?, event_x_status = ? WHERE event_name = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, newEventName);
                            stmt.setDate(2, newEventDate);
                            stmt.setString(3, newEventStatus);
                            stmt.setString(4, oldEventName);
                            stmt.executeUpdate();

                            Event updatedEvent = events.stream().filter(event -> event.getName().equals(oldEventName)).findFirst().orElse(null);
                            if (updatedEvent != null) {
                                updatedEvent.setName(newEventName);
                                updatedEvent.setDate(newEventDate);
                                updatedEvent.setStatus(newEventStatus);
                            }

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

    // Event class to store event information in memory
    private static class Event {
        private String name;
        private Date date;
        private String status;

        public Event(String name, Date date, String status) {
            this.name = name;
            this.date = date;
            this.status = status;
        }

        public String getName() {
            return name;
        }

        public Date getDate() {
            return date;
        }

        public String getStatus() {
            return status;
        }

        public void setName(String name) {
            this.name = name;
        }

        public void setDate(Date date) {
            this.date = date;
        }

        public void setStatus(String status) {
            this.status = status;
        }
    }
}

