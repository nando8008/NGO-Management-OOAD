import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;
import java.util.stream.Collectors;

public class WelcomePage extends JFrame {
    private JLabel welcomeLabel = new JLabel("Welcome to the NGO Management System!");
    private JTextField searchField = new JTextField();
    private JComboBox<String> filterComboBox = new JComboBox<>(new String[]{"Name", "Date", "Type"});
    private JButton searchButton = new JButton("Search");
    private JButton publicViewButton = new JButton("Public Login");

    private DefaultTableModel tableModel;
    private JTable eventTable;
    private List<Event> events;
    private List<String> volunteers;

    public WelcomePage(String userID, List<Event> eventsList, List<String> volunteersList) {
        this.events = eventsList;
        this.volunteers = volunteersList; 

        setupUI();
        displayEvents(events);

        // Set custom renderer and editor for the "Enroll" button
        eventTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        eventTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), this));
    }

    private void setupUI() {
        welcomeLabel.setBounds(20, 10, 400, 25);
        welcomeLabel.setFont(new Font(null, Font.PLAIN, 20));

        searchField.setBounds(20, 50, 200, 25);
        filterComboBox.setBounds(230, 50, 100, 25);
        searchButton.setBounds(340, 50, 100, 25);
        searchButton.addActionListener(e -> performSearch());

        publicViewButton.setBounds(450, 50, 120, 25);
        publicViewButton.addActionListener(e -> new PublicEventsPage(events).setVisible(true));

        String[] columnNames = {"Name", "Date", "Type", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel);

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

    private void displayEvents(List<Event> eventsToShow) {
        tableModel.setRowCount(0);

        for (Event event : eventsToShow) {
            tableModel.addRow(new Object[]{
                event.getName(), event.getDate(), event.getType(), "Enroll"
            });
        }
    }

    private void performSearch() {
        String query = searchField.getText().toLowerCase();
        String filter = (String) filterComboBox.getSelectedItem();

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

        displayEvents(filteredEvents);
    }

    public void showEnrollmentPopup(String eventName) {
        JOptionPane.showMessageDialog(
            this,
            "Successfully enrolled in " + eventName + "!",
            "Enrollment Successful",
            JOptionPane.INFORMATION_MESSAGE
        );
    }
}

