import java.awt.*;
import java.awt.event.*;
import java.util.List;
import java.util.stream.Collectors;
import javax.swing.*;
import javax.swing.table.DefaultTableModel;

public class WelcomePage {

    JFrame frame = new JFrame();
    JLabel welcomeLabel = new JLabel("Welcome to the NGO Management System!");

    JTextField searchField = new JTextField();
    JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"Name", "Date", "Type"});
    JButton searchButton = new JButton("Search");

    DefaultTableModel tableModel;
    JTable eventTable;
    List<Event> events;

    public WelcomePage(String userID, List<Event> eventsList) {
        this.events = eventsList;

        setupUI();
        displayEvents(events);

        // Set custom renderer and editor for the "Enroll" button
        eventTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        eventTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), this));
    }

    private void setupUI() {
        welcomeLabel.setBounds(20, 10, 400, 25);
        welcomeLabel.setFont(new Font(null, Font.PLAIN, 20));

        // Set up search components
        searchField.setBounds(20, 50, 200, 25);
        filterComboBox.setBounds(230, 50, 100, 25);
        searchButton.setBounds(340, 50, 100, 25);
        searchButton.addActionListener(e -> performSearch());

        String[] columnNames = {"Name", "Date", "Type", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel);

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setBounds(20, 90, 960, 300);

        frame.add(welcomeLabel);
        frame.add(searchField);
        frame.add(filterComboBox);
        frame.add(searchButton);
        frame.add(scrollPane);

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 500);
        frame.setLayout(null);
        frame.setVisible(true);
    }

    private void displayEvents(List<Event> eventsToShow) {
        tableModel.setRowCount(0);  // Clear the table

        for (Event event : eventsToShow) {
            tableModel.addRow(new Object[]{
                event.getName(), event.getDate(), event.getType(), "Enroll"
            });
        }
    }

    private void performSearch() {
        String query = searchField.getText().toLowerCase();
        String filter = filterComboBox.getSelectedItem().toString();

        // Filter the events based on the selected filter and query
        List<Event> filteredEvents = events.stream()
                .filter(event -> {
                    switch (filter) {
                        case "Name":
                            return event.getName().toLowerCase().contains(query);
                        case "Date":
                            return event.getDate().contains(query);
                        case "Type":
                            return event.getType().toLowerCase().contains(query);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        // Display the filtered events
        displayEvents(filteredEvents);
    }

    public void showEnrollmentPopup(String eventName) {
        JOptionPane.showMessageDialog(
            frame,
            "Successfully enrolled in " + eventName + "!",
            "Enrollment Successful",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}








