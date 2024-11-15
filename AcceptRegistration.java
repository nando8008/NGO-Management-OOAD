import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.sql.*;
import java.util.List;
import java.util.Vector;
import java.util.stream.Collectors;

public class AcceptRegistration extends JFrame {
    private JTable table;
    private JTextField searchField;
    private JComboBox<String> searchOptions;
    private DefaultTableModel tableModel;

    public AcceptRegistration() {
        setTitle("Accept Registration Requests");
        setSize(600, 400);
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
        tableModel = new DefaultTableModel(new String[]{"Username", "Gender", "Date of Birth", "Action"}, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only the "Action" column is editable
            }
        };

        table = new JTable(tableModel);
        table.setRowHeight(30);

        // Add custom renderer and editor for "Action" column
        table.getColumn("Action").setCellRenderer(new ButtonRenderer());
        table.getColumn("Action").setCellEditor(new ButtonEditor(new JCheckBox()));

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

                tableModel.addRow(new Object[]{username, gender, dob, "Accept"});
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

                tableModel.addRow(new Object[]{username, gender, dob, "Accept"});
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

    // Button renderer for the "Action" column
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

    // Button editor for the "Action" column
    private class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean clicked;
        private String username;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
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
                acceptRegistration(username);
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
