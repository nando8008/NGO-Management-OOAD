import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.List;

public class PublicEventsPage extends JFrame {
    private JTable eventTable;
    private List<Event> events;
    private JButton submitButton;

    public PublicEventsPage(List<Event> events) {
        this.events = events;
        setTitle("Upcoming Events");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Define column names for the event table
        String[] columnNames = {"Event Name", "Event Date", "Event Type", "Action"};

        // Create a table model
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel) {
            // Disable editing for all cells
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only allow the action button to be editable
            }
        };

        // Populate the table with event data and buttons
        for (Event event : events) {
            Object[] rowData = {
                event.getName(),
                event.getDate(),
                event.getType(),
                "Donate" // Placeholder for the button
            };
            tableModel.addRow(rowData);
        }

        // Set the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(eventTable);
        eventTable.setFillsViewportHeight(true);

        // Set custom cell renderer for the action button
        eventTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        eventTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), events));

        // Add Submit Project button
        submitButton = new JButton("Submit Project");
        submitButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openSubmissionDialog();
            }
        });

        // Add components to the frame
        add(scrollPane, BorderLayout.CENTER); // Add the table
        add(submitButton, BorderLayout.SOUTH); // Add the Submit Project button
    }

    // Method to open the submission dialog and collect event details
    private void openSubmissionDialog() {
        JTextField eventNameField = new JTextField();
        JTextField eventTypeField = new JTextField();

        Object[] message = {
            "Event Name:", eventNameField,
            "Event Type:", eventTypeField
        };

        int option = JOptionPane.showConfirmDialog(
                this, message, "Enter Event Details", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String eventName = eventNameField.getText().trim();
            String eventType = eventTypeField.getText().trim();

            if (!eventName.isEmpty() && !eventType.isEmpty()) {
                insertProjectIntoDatabase(eventName, eventType);
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            }
        }
    }

    // Method to insert event details into the database
    private void insertProjectIntoDatabase(String eventName, String eventType) {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "INSERT INTO event_submission (event_s_name, event_s_type) VALUES (?, ?)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, eventName);
                stmt.setString(2, eventType);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    JOptionPane.showMessageDialog(this, "Project submitted successfully!");
                } else {
                    JOptionPane.showMessageDialog(this, "Project submission failed.");
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    // Custom renderer to display a button in the cell
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(UIManager.getColor("Button.background")); // Use default button color
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Donate" : value.toString());
            return this;
        }
    }

    // Custom editor to handle button clicks
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private List<Event> events;

        public ButtonEditor(JCheckBox checkBox, List<Event> events) {
            super(checkBox);
            this.events = events;
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(UIManager.getColor("Button.background")); // Use default button color
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped(); // Stop editing and notify listeners
                    int row = eventTable.getSelectedRow();
                    if (row >= 0) {
                        Event event = events.get(row);
                        JOptionPane.showMessageDialog(button, "Donated to " + event.getName());
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "Donate" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            isPushed = false;
            return label;
        }
    }
}
