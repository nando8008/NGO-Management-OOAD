import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class SponsorPage extends JFrame {
    private JTable sponsorsTable;
    private DefaultTableModel tableModel;
    private JButton sendInfoToSponsorsButton;
    private JButton addSponsorButton;
    private JButton deleteSponsorButton;
    private JButton updateSponsorButton;

    public SponsorPage() {
        setTitle("Sponsors");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Create table model and table for sponsors
        String[] columnNames = {"Name", "Email"};
        tableModel = new DefaultTableModel(columnNames, 0);
        sponsorsTable = new JTable(tableModel);
        sponsorsTable.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION); // Allow multiple rows to be selected

        loadSponsorData();

        JScrollPane scrollPane = new JScrollPane(sponsorsTable);
        add(scrollPane, BorderLayout.CENTER);

        // Send info to sponsors button
        sendInfoToSponsorsButton = new JButton("Send info");
        sendInfoToSponsorsButton.setPreferredSize(new Dimension(180, 40));
        sendInfoToSponsorsButton.addActionListener(e -> {
            int[] selectedRows = sponsorsTable.getSelectedRows();
            if (selectedRows.length > 0) {
                JOptionPane.showMessageDialog(this, "Message sent to all selected sponsors");
            } else {
                JOptionPane.showMessageDialog(this, "Please select one or more sponsors to send the message.");
            }
        });

        // Add sponsor button
        addSponsorButton = new JButton("Add Sponsor");
        addSponsorButton.setPreferredSize(new Dimension(180, 40));
        addSponsorButton.addActionListener(e -> handleAddSponsor());

        // Delete sponsor button
        deleteSponsorButton = new JButton("Delete Sponsor");
        deleteSponsorButton.setPreferredSize(new Dimension(180, 40));
        deleteSponsorButton.addActionListener(e -> handleDeleteSponsor());

        // Update sponsor button
        updateSponsorButton = new JButton("Update Sponsor");
        updateSponsorButton.setPreferredSize(new Dimension(180, 40));
        updateSponsorButton.addActionListener(e -> handleUpdateSponsor());

        // Panel for buttons
        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new GridLayout(1, 4, 10, 10)); // Adjust layout to ensure buttons are visible
        buttonPanel.add(sendInfoToSponsorsButton);
        buttonPanel.add(addSponsorButton);
        buttonPanel.add(deleteSponsorButton);
        buttonPanel.add(updateSponsorButton);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void loadSponsorData() {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT name, emailid FROM sponsors";
            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String email = rs.getString("emailid");
                    tableModel.addRow(new Object[]{name, email});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error loading data: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    private void handleAddSponsor() {
        JTextField nameField = new JTextField();
        JTextField emailField = new JTextField();
        Object[] message = {"Name:", nameField, "Email:", emailField};

        int option = JOptionPane.showConfirmDialog(this, message, "Add New Sponsor", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String name = nameField.getText().trim();
            String email = emailField.getText().trim();

            if (!name.isEmpty() && !email.isEmpty()) {
                Connection conn = DatabaseConnection.connect();
                if (conn != null) {
                    String sql = "INSERT INTO sponsors (name, emailid) VALUES (?, ?)";
                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, name);
                        stmt.setString(2, email);
                        stmt.executeUpdate();
                        tableModel.addRow(new Object[]{name, email});
                        JOptionPane.showMessageDialog(this, "Sponsor added successfully.");
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Error adding sponsor: " + ex.getMessage());
                    } finally {
                        DatabaseConnection.disconnect(conn);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Please fill in all fields.");
            }
        }
    }

    private void handleDeleteSponsor() {
        int[] selectedRows = sponsorsTable.getSelectedRows();
        if (selectedRows.length > 0) {
            Connection conn = DatabaseConnection.connect();
            if (conn != null) {
                try {
                    for (int selectedRow : selectedRows) {
                        String name = (String) tableModel.getValueAt(selectedRow, 0);
                        String sql = "DELETE FROM sponsors WHERE name = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, name);
                            stmt.executeUpdate();
                        }
                    }
                    for (int i = selectedRows.length - 1; i >= 0; i--) {
                        tableModel.removeRow(selectedRows[i]);
                    }
                    JOptionPane.showMessageDialog(this, "Sponsor(s) deleted successfully.");
                } catch (SQLException ex) {
                    ex.printStackTrace();
                    JOptionPane.showMessageDialog(this, "Error deleting sponsor(s): " + ex.getMessage());
                } finally {
                    DatabaseConnection.disconnect(conn);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select one or more sponsors to delete.");
        }
    }

    private void handleUpdateSponsor() {
        int selectedRow = sponsorsTable.getSelectedRow();
        if (selectedRow >= 0) {
            String oldName = (String) tableModel.getValueAt(selectedRow, 0);
            JTextField nameField = new JTextField(oldName);
            JTextField emailField = new JTextField((String) tableModel.getValueAt(selectedRow, 1));
            Object[] message = {"Name:", nameField, "Email:", emailField};

            int option = JOptionPane.showConfirmDialog(this, message, "Update Sponsor", JOptionPane.OK_CANCEL_OPTION);
            if (option == JOptionPane.OK_OPTION) {
                String newName = nameField.getText().trim();
                String newEmail = emailField.getText().trim();

                if (!newName.isEmpty() && !newEmail.isEmpty()) {
                    Connection conn = DatabaseConnection.connect();
                    if (conn != null) {
                        String sql = "UPDATE sponsors SET name = ?, emailid = ? WHERE name = ?";
                        try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                            stmt.setString(1, newName);
                            stmt.setString(2, newEmail);
                            stmt.setString(3, oldName);
                            stmt.executeUpdate();

                            tableModel.setValueAt(newName, selectedRow, 0);
                            tableModel.setValueAt(newEmail, selectedRow, 1);

                            JOptionPane.showMessageDialog(this, "Sponsor updated successfully.");
                        } catch (SQLException ex) {
                            ex.printStackTrace();
                            JOptionPane.showMessageDialog(this, "Error updating sponsor: " + ex.getMessage());
                        } finally {
                            DatabaseConnection.disconnect(conn);
                        }
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "Please fill in all fields.");
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a sponsor to update.");
        }
    }
}
