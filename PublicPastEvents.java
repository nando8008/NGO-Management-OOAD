import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;

public class PublicPastEvents extends JFrame {
    private JTable eventsTable;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchOptions;

    public PublicPastEvents() {
        setTitle("Past Events");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Table model and table for events
        String[] columnNames = {"Event Name", "Event Date", "Event Status"};
        tableModel = new DefaultTableModel(columnNames, 0);
        eventsTable = new JTable(tableModel);

        loadEventStatusData();

        JScrollPane scrollPane = new JScrollPane(eventsTable);
        
        // Search components
        searchField = new JTextField(15);
        searchOptions = new JComboBox<>(new String[]{"Event Name", "Event Date", "Event Status"});
        JButton searchButton = new JButton("Search");

        // Add action listener for the search button
        searchButton.addActionListener(e -> filterTable());

        // Panel for search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchOptions);
        searchPanel.add(searchButton);

        // Add components to frame
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadEventStatusData() {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT event_name, event_occured_date, event_x_status FROM event_status";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String eventName = rs.getString("event_name");
                    String eventDate = rs.getString("event_occured_date");
                    String eventStatus = rs.getString("event_x_status");
                    tableModel.addRow(new Object[]{eventName, eventDate, eventStatus});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    // Filter table data based on search criteria
    private void filterTable() {
        String searchText = searchField.getText().trim().toLowerCase();
        String searchOption = (String) searchOptions.getSelectedItem();

        // Clear the table model and reload only the rows that match the search criteria
        tableModel.setRowCount(0);  // Clear current rows

        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT event_name, event_occured_date, event_x_status FROM event_status";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String eventName = rs.getString("event_name").toLowerCase();
                    String eventDate = rs.getString("event_occured_date").toLowerCase();
                    String eventStatus = rs.getString("event_x_status").toLowerCase();

                    boolean matches = false;
                    switch (searchOption) {
                        case "Event Name":
                            matches = eventName.contains(searchText);
                            break;
                        case "Event Date":
                            matches = eventDate.contains(searchText);
                            break;
                        case "Event Status":
                            matches = eventStatus.contains(searchText);
                            break;
                    }

                    if (matches) {
                        tableModel.addRow(new Object[]{rs.getString("event_name"), rs.getString("event_occured_date"), rs.getString("event_x_status")});
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }
}

