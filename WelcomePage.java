import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
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
    private Set<Integer> enrolledEventIds;
    private Set<String> enrolledEventNames;
    private int userId;

    public WelcomePage(int userId, List<String> volunteersList) {
        this.userId = userId;
        this.volunteers = volunteersList;
        this.enrolledEventIds = getUserEnrollmentsFromDatabase(userId);
        this.enrolledEventNames = new HashSet<>(); // Initialize the set for names
        setupUI();
        displayEvents(getEventsFromDatabase());
    }

    private void setupUI() {
        welcomeLabel.setBounds(20, 10, 400, 25);
        welcomeLabel.setFont(new Font(null, Font.PLAIN, 20));
        searchField.setBounds(20, 50, 200, 25);
        filterComboBox.setBounds(230, 50, 100, 25);
        searchButton.setBounds(340, 50, 100, 25);
        searchButton.addActionListener(e -> performSearch());
        publicViewButton.setBounds(450, 50, 120, 25);
        publicViewButton.addActionListener(e -> new Eventdonate(getEventsFromDatabase()).setVisible(true));

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

    private Set<Integer> getUserEnrollmentsFromDatabase(int userId) {
        Set<Integer> enrolledIds = new HashSet<>();
        String query = "SELECT event_id FROM user_enrollments WHERE user_id = ?";
        Connection conn = DatabaseConnection.connect();
        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                enrolledIds.add(rs.getInt("event_id"));
            }
            System.out.println("Enrolled Event IDs: " + enrolledIds);
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return enrolledIds;
    }

    private List<Event> getEventsFromDatabase() {
        List<Event> events = new ArrayList<>();
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
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return events;
    }

    private void displayEvents(List<Event> eventsToShow) {
        tableModel.setRowCount(0);
        for (Event event : eventsToShow) {
            String actionText = enrolledEventIds.contains(event.getId()) || enrolledEventNames.contains(event.getName()) ? "Enrolled" : "Enroll";
            tableModel.addRow(new Object[]{event.getName(), event.getDate(), event.getType(), actionText});
        }
    }

    public boolean isEnrolled(int eventId) {
        System.out.println("Event ID checked: " + eventId);
        System.out.println("Enrolled Event IDs: " + enrolledEventIds);
        return enrolledEventIds.contains(eventId);
    }

    public boolean isEnrolled(String eventName) {
        System.out.println("Event Name checked: " + eventName);
        System.out.println("Enrolled Event Names: " + enrolledEventNames);
        return enrolledEventNames.contains(eventName);
    }

    public void updateEnrollmentStatus(int eventId) {
        if (!isEnrolled(eventId)) {
            enrolledEventIds.add(eventId);
            System.out.println("Updated Enrolled Event IDs: " + enrolledEventIds);

            // Update the database
            String query = "INSERT INTO user_enrollments (user_id, event_id) VALUES (?, ?)";
            Connection conn = DatabaseConnection.connect();
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setInt(2, eventId);
                stmt.executeUpdate();
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database update error occurred.");
            } finally {
                DatabaseConnection.disconnect(conn);
            }

            displayEvents(getEventsFromDatabase());
        } else {
            JOptionPane.showMessageDialog(null, "You are already enrolled.");
        }
    }

    public void updateEnrollmentStatus(String eventName) {
        if (!isEnrolled(eventName)) {
            enrolledEventNames.add(eventName);
            System.out.println("Updated Enrolled Event Names: " + enrolledEventNames);

            String getIdQuery = "SELECT event_id FROM events WHERE event_name = ?";
            Connection conn = DatabaseConnection.connect();
            try (PreparedStatement stmt = conn.prepareStatement(getIdQuery)) {
                stmt.setString(1, eventName);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    int eventId = rs.getInt("event_id");

                    // Check if the (user_id, event_id) combination already exists
                    String checkEnrollmentQuery = "SELECT 1 FROM user_enrollments WHERE user_id = ? AND event_id = ?";
                    try (PreparedStatement checkStmt = conn.prepareStatement(checkEnrollmentQuery)) {
                        checkStmt.setInt(1, userId);
                        checkStmt.setInt(2, eventId);
                        ResultSet checkRs = checkStmt.executeQuery();

                        if (!checkRs.next()) {
                            // Insert into user_enrollments since it doesn't exist
                            String updateQuery = "INSERT INTO user_enrollments (user_id, event_id) VALUES (?, ?)";
                            try (PreparedStatement updateStmt = conn.prepareStatement(updateQuery)) {
                                updateStmt.setInt(1, userId);
                                updateStmt.setInt(2, eventId);
                                updateStmt.executeUpdate();
                            }
                        } else {
                            JOptionPane.showMessageDialog(null, "You are already enrolled in this event.");
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
                JOptionPane.showMessageDialog(null, "Database update error occurred.");
            } finally {
                DatabaseConnection.disconnect(conn);
            }

            displayEvents(getEventsFromDatabase());
        } else {
            JOptionPane.showMessageDialog(null, "You are already enrolled.");
        }
    }

    public int getUserId() {
        return userId;
    }

    private void performSearch() {
        String searchText = searchField.getText().trim();
        String filterType = (String) filterComboBox.getSelectedItem();
        List<Event> filteredEvents = getEventsFromDatabase().stream()
                .filter(event -> {
                    switch (filterType) {
                        case "Name":
                            return event.getName().contains(searchText);
                        case "Date":
                            return event.getDate().contains(searchText);
                        case "Type":
                            return event.getType().contains(searchText);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());
        displayEvents(filteredEvents);
    }
}
