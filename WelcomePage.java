import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class WelcomePage extends JFrame {
    private JLabel welcomeLabel = new JLabel("Welcome to the NGO Management System!");
    private JTextField searchField = new JTextField();
    private JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"Name", "Date", "Type"});
    private JButton searchButton = new JButton("Search");
    private JButton publicViewButton = new JButton("Donate");

    private DefaultTableModel tableModel;
    private JTable eventTable;
    private List<String> volunteers;

    public WelcomePage(String userID, List<String> volunteersList) {
        this.volunteers = volunteersList;

        setupUI();
        displayEvents(getEventsFromDatabase());  // Fetch events from the DB
    }

    private void setupUI() {
        welcomeLabel.setBounds(20, 10, 400, 25);
        welcomeLabel.setFont(new Font(null, Font.PLAIN, 20));

        searchField.setBounds(20, 50, 200, 25);
        filterComboBox.setBounds(230, 50, 100, 25);
        searchButton.setBounds(340, 50, 100, 25);
        searchButton.addActionListener(e -> performSearch());

        publicViewButton.setBounds(450, 50, 120, 25);
        publicViewButton.addActionListener(e -> new PublicEventsPage(getEventsFromDatabase()).setVisible(true));

        String[] columnNames = {"Name", "Date", "Type", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel);
        eventTable.getColumn("Action").setCellRenderer(new ButtonRenderer());
        eventTable.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox(), this));

        JScrollPane scrollPane = new JScrollPane(eventTable);
        scrollPane.setBounds(20, 90, 960, 300);

        add(welcomeLabel);
        add(searchField);
        add(filterComboBox);
        add(searchButton);
        add(publicViewButton);
        add(scrollPane);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(1000, 500);
        setLayout(null);
    }

    private List<Event> getEventsFromDatabase() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT event_name, event_date, event_type FROM events";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("event_name");
                String date = rs.getString("event_date");
                String type = rs.getString("event_type");
                events.add(new Event(name, date, type));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return events;
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
        String filter = (String) filterComboBox.getSelectedItem();

        List<Event> filteredEvents = getEventsFromDatabase().stream()
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

        displayEvents(filteredEvents);
    }

    public void showEnrollmentPopup(String eventName) {
        JOptionPane.showMessageDialog(this, 
            "You have successfully enrolled in the event: " + eventName, 
            "Enrollment Success", 
            JOptionPane.INFORMATION_MESSAGE);
    }
}

