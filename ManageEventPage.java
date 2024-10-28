import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageEventPage extends JFrame {
    private List<Event> events;
    private DefaultTableModel tableModel;
    private JTable eventTable;

    public ManageEventPage(List<Event> events) {
        this.events = events;
        
        setTitle("Manage Events");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

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

    private void displayEvents() {
        tableModel.setRowCount(0); // Clear the table
        for (Event event : events) {
            tableModel.addRow(new Object[]{event.getName(), event.getDate(), event.getType()});
        }
    }

    private void addEvent() {
        Event newEvent = getEventDetails(null);
        if (newEvent != null) {
            events.add(newEvent);
            displayEvents();
        }
    }

    private void updateEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow >= 0) {
            Event event = events.get(selectedRow);
            Event updatedEvent = getEventDetails(event);
            if (updatedEvent != null) {
                events.set(selectedRow, updatedEvent);
                displayEvents();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select an event to update.");
        }
    }

    private void deleteEvent() {
        int selectedRow = eventTable.getSelectedRow();
        if (selectedRow >= 0) {
            events.remove(selectedRow);
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
            return new Event(nameField.getText(), dateField.getText(), typeField.getText());
        } else {
            return null;
        }
    }
}



