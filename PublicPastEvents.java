import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PublicPastEvents extends JFrame {
    private JTable eventsTable;
    private DefaultTableModel tableModel;

    public PublicPastEvents() {
        setTitle("Past Events");
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
}
