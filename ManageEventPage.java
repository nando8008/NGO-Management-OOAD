import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ManageEventPage extends JFrame {
    private List<Event> events = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable eventTable;

    public ManageEventPage() {
        setTitle("Manage Events");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Initialize and load events from the database
        fetchEventsFromDatabase();

        // Set up table and model without Actions column
        String[] columnNames = {"Name", "Date", "Type"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel);
        displayEvents();
        JScrollPane scrollPane = new JScrollPane(eventTable);

        // Add, Update, and Delete buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Event");
        JButton updateButton = new JButton("Update Event");
        JButton deleteButton = new JButton("Delete Event");

        addButton.addActionListener(e -> addEvent());
        updateButton.addActionListener(e -> updateEvent());
        deleteButton.addActionListener(e -> deleteEvent());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchEventsFromDatabase() {
        // Clear any existing events in the list
        events.clear();

        // SQL query to fetch events
        String query = "SELECT event_id, event_name, event_date, event_type FROM events";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("event_id");
                String name = rs.getString("event_name");
                String date = rs.getString("event_date");
                String type = rs.getString("event_type");
                
                events.add(new Event(name, date, type, id));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while fetching events.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    private void displayEvents() {
        tableModel.setRowCount(0); // Clear the table
        for (Event event : events) {
            tableModel.addRow(new Object[]{event.getName(), event.getDate(), event.getType()});
        }
    }

    private void addEvent() {
        Event newEvent = getEventDetails(null);
        if (newEvent != null) {
            saveEventToDatabase(newEvent);
            fetchEventsFromDatabase();
            displayEvents();
        }
    }

    private void updateEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow >= 0) {
            Event event = events.get(selectedRow);
            Event updatedEvent = getEventDetails(event);
            if (updatedEvent != null) {
                updateEventInDatabase(event.getId(), updatedEvent);
                fetchEventsFromDatabase();
                displayEvents();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to update.");
        }
    }

    private void deleteEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow >= 0) {
            Event event = events.get(selectedRow);
            deleteEventFromDatabase(event.getId());
            fetchEventsFromDatabase();
            displayEvents();
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to delete.");
        }
    }

    private Event getEventDetails(Event event) {
        JTextField nameField = new JTextField(event != null ? event.getName() : "");
        JTextField dateField = new JTextField(event != null ? event.getDate() : "");
        JTextField typeField = new JTextField(event != null ? event.getType() : "");
        
        Object[] message = {
            "Name:", nameField,
            "Date:", dateField,
            "Type:", typeField
        };

        int option = JOptionPane.showConfirmDialog(this, message, 
                        event == null ? "Add Event" : "Update Event", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            return new Event(nameField.getText(), dateField.getText(), typeField.getText(), event != null ? event.getId() : 0);
        } else {
            return null;
        }
    }

    private void saveEventToDatabase(Event event) {
        String insertQuery = "INSERT INTO events (event_name, event_date, event_type) VALUES (?, ?, ?)";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, event.getName());
            stmt.setString(2, event.getDate());
            stmt.setString(3, event.getType());
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while adding the event.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    private void updateEventInDatabase(int eventId, Event event) {
        String updateQuery = "UPDATE events SET event_name = ?, event_date = ?, event_type = ? WHERE event_id = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, event.getName());
            stmt.setString(2, event.getDate());
            stmt.setString(3, event.getType());
            stmt.setInt(4, eventId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while updating the event.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    private void deleteEventFromDatabase(int eventId) {
        String deleteQuery = "DELETE FROM events WHERE event_id = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setInt(1, eventId);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while deleting the event.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }
}




