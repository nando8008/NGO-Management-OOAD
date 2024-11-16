import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.Vector;

public class AcceptRegistration extends JFrame {
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> searchOptions;
    private DefaultTableModel tableModel;

    public AcceptRegistration() {
        setTitle("Accept Registration Requests");
        setSize(700, 400); // Updated size to accommodate the additional column
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchOptions = new JComboBox<>(new String[]{"Username", "Gender", "Date of Birth"});
        JButton searchButton = new JButton("Search");
        searchButton.addActionListener(e -> searchRegistrations());
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchOptions);
        searchPanel.add(searchButton);

        // Table
        tableModel = new DefaultTableModel(new String[]{"Username", "Gender", "Date of Birth", "Accept", "Reject"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3 || column == 4; // Only the "Accept" and "Reject" columns are editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);

        // Add custom renderer and editor for "Accept" and "Reject" columns
        table.getColumn("Accept").setCellRenderer(new ButtonRenderer());
        table.getColumn("Accept").setCellEditor(new ButtonEditor(new JCheckBox(), true)); // Accept action
        table.getColumn("Reject").setCellRenderer(new ButtonRenderer());
        table.getColumn("Reject").setCellEditor(new ButtonEditor(new JCheckBox(), false)); // Reject action

        JScrollPane scrollPane = new JScrollPane(table);

        // Load initial data
        loadPendingRegistrations();

        // Add components to the frame
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadPendingRegistrations() {
        tableModel.setRowCount(0); // Clear existing rows
        String query = "SELECT username, gender, date_of_birth FROM user_pending_login_credentials";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String username = rs.getString("username");
                String gender = rs.getString("gender");
                Date dob = rs.getDate("date_of_birth");

                tableModel.addRow(new Object[]{username, gender, dob, "Accept", "Reject"});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error loading pending registrations.");
        }
    }

    private void searchRegistrations() {
        String searchText = searchField.getText().trim().toLowerCase();
        String searchOption = (String) searchOptions.getSelectedItem();

        String column = switch (searchOption) {
            case "Username" -> "username";
            case "Gender" -> "gender";
            case "Date of Birth" -> "CAST(date_of_birth AS TEXT)";
            default -> throw new IllegalStateException("Unexpected value: " + searchOption);
        };

        String query = "SELECT username, gender, date_of_birth FROM user_pending_login_credentials WHERE " +
                column + " ILIKE ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setString(1, "%" + searchText + "%");
            ResultSet rs = stmt.executeQuery();
            tableModel.setRowCount(0); // Clear existing rows

            while (rs.next()) {
                String username = rs.getString("username");
                String gender = rs.getString("gender");
                Date dob = rs.getDate("date_of_birth");

                tableModel.addRow(new Object[]{username, gender, dob, "Accept", "Reject"});
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error performing search.");
        }
    }

    private void acceptRegistration(String username) {
        String insertQuery = "INSERT INTO user_login_credentials (username, password, gender, date_of_birth) " +
                "SELECT username, password, gender, date_of_birth FROM user_pending_login_credentials WHERE username = ?";
        String deleteQuery = "DELETE FROM user_pending_login_credentials WHERE username = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement insertStmt = conn.prepareStatement(insertQuery);
             PreparedStatement deleteStmt = conn.prepareStatement(deleteQuery)) {

            insertStmt.setString(1, username);
            deleteStmt.setString(1, username);

            insertStmt.executeUpdate();
            deleteStmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "User accepted successfully.");
            loadPendingRegistrations(); // Refresh the table

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error accepting registration.");
        }
    }

    private void rejectRegistration(String username) {
        String deleteQuery = "DELETE FROM user_pending_login_credentials WHERE username = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {

            stmt.setString(1, username);
            stmt.executeUpdate();

            JOptionPane.showMessageDialog(this, "User rejected successfully.");
            loadPendingRegistrations(); // Refresh the table

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error rejecting registration.");
        }
    }

    // Button renderer for the "Action" columns
    private class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
            setText(value == null ? "" : value.toString());
            return this;
        }
    }

    // Button editor for the "Action" columns
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private String username;
        private boolean isAcceptAction;

        public ButtonEditor(JCheckBox checkBox, boolean isAcceptAction) {
            super(checkBox);
            this.isAcceptAction = isAcceptAction;
            button = new JButton();
            button.setOpaque(true);
            button.addActionListener(e -> fireEditingStopped());
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            username = (String) table.getValueAt(row, 0);
            label = (value == null) ? "" : value.toString();
            button.setText(label);
            clicked = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (clicked) {
                if (isAcceptAction) {
                    acceptRegistration(username);
                } else {
                    rejectRegistration(username);
                }
            }
            clicked = false;
            return label;
        }

        @Override
        public boolean stopCellEditing() {
            clicked = false;
            return super.stopCellEditing();
        }

        @Override
        protected void fireEditingStopped() {
            super.fireEditingStopped();
        }
    }

    // For testing purposes
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            AcceptRegistration frame = new AcceptRegistration();
            frame.setVisible(true);
        });
    }
}
