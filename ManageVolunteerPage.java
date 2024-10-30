import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ManageVolunteerPage extends JFrame {
    private List<Volunteer> volunteers = new ArrayList<>();
    private DefaultTableModel tableModel;
    private JTable volunteerTable;

    public ManageVolunteerPage() {
        setTitle("Manage Volunteers");
        setSize(800, 400); // Adjust size for more fields
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Fetch and load volunteers from the database
        fetchVolunteersFromDatabase();

        // Set up table and model with all details except user_id
        String[] columnNames = {"Username", "Password", "Gender", "Date of Birth"};
        tableModel = new DefaultTableModel(columnNames, 0);
        volunteerTable = new JTable(tableModel);
        displayVolunteers();
        JScrollPane scrollPane = new JScrollPane(volunteerTable);

        // Add, Update, and Delete buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Volunteer");
        JButton updateButton = new JButton("Update Volunteer");
        JButton deleteButton = new JButton("Delete Volunteer");

        addButton.addActionListener(e -> addVolunteer());
        updateButton.addActionListener(e -> updateVolunteer());
        deleteButton.addActionListener(e -> deleteVolunteer());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void fetchVolunteersFromDatabase() {
        volunteers.clear();
        String query = "SELECT username, password, gender, date_of_birth FROM user_login_credentials";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {
            while (rs.next()) {
                String username = rs.getString("username");
                String password = rs.getString("password");
                String gender = rs.getString("gender");
                Date dateOfBirth = rs.getDate("date_of_birth");
                volunteers.add(new Volunteer(username, password, gender, dateOfBirth));
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while fetching volunteers.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    private void displayVolunteers() {
        tableModel.setRowCount(0); // Clear the table
        for (Volunteer volunteer : volunteers) {
            tableModel.addRow(new Object[]{
                volunteer.getUsername(),
                volunteer.getPassword(),
                volunteer.getGender(),
                volunteer.getDateOfBirth()
            });
        }
    }

    private void addVolunteer() {
        Volunteer volunteer = getVolunteerDetails(null);
        if (volunteer != null) {
            if (saveVolunteerToDatabase(volunteer)) {
                fetchVolunteersFromDatabase();
                displayVolunteers();
            }
        }
    }

    private Volunteer getVolunteerDetails(Volunteer volunteer) {
        JTextField usernameField = new JTextField(volunteer != null ? volunteer.getUsername() : "");
        JTextField passwordField = new JTextField(volunteer != null ? volunteer.getPassword() : "");
        JTextField genderField = new JTextField(volunteer != null ? volunteer.getGender() : "");
        JTextField dobField = new JTextField(volunteer != null ? volunteer.getDateOfBirth().toString() : "");

        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Gender:", genderField,
            "Date of Birth (YYYY-MM-DD):", dobField
        };

        int option = JOptionPane.showConfirmDialog(this, message, 
                        volunteer == null ? "Add Volunteer" : "Update Volunteer", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            return new Volunteer(usernameField.getText(), passwordField.getText(), genderField.getText(),
                                 Date.valueOf(dobField.getText()));
        } else {
            return null;
        }
    }

    private boolean saveVolunteerToDatabase(Volunteer volunteer) {
        String insertQuery = "INSERT INTO user_login_credentials (username, password, gender, date_of_birth) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, volunteer.getUsername());
            stmt.setString(2, volunteer.getPassword());
            stmt.setString(3, volunteer.getGender());
            stmt.setDate(4, volunteer.getDateOfBirth());
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while adding the volunteer.");
            return false;
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    private void updateVolunteer() {
        int selectedRow = volunteerTable.getSelectedRow();
        if (selectedRow >= 0) {
            Volunteer volunteer = volunteers.get(selectedRow);
            Volunteer updatedVolunteer = getVolunteerDetails(volunteer);
            if (updatedVolunteer != null) {
                if (updateVolunteerInDatabase(volunteer.getUsername(), updatedVolunteer)) {
                    fetchVolunteersFromDatabase();
                    displayVolunteers();
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a volunteer to update.");
        }
    }

    private boolean updateVolunteerInDatabase(String oldUsername, Volunteer newVolunteer) {
        String updateQuery = "UPDATE user_login_credentials SET username = ?, password = ?, gender = ?, date_of_birth = ? WHERE username = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, newVolunteer.getUsername());
            stmt.setString(2, newVolunteer.getPassword());
            stmt.setString(3, newVolunteer.getGender());
            stmt.setDate(4, newVolunteer.getDateOfBirth());
            stmt.setString(5, oldUsername);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while updating the volunteer.");
            return false;
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    private void deleteVolunteer() {
        int selectedRow = volunteerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String username = volunteers.get(selectedRow).getUsername();
            if (deleteVolunteerFromDatabase(username)) {
                fetchVolunteersFromDatabase();
                displayVolunteers();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a volunteer to delete.");
        }
    }

    private boolean deleteVolunteerFromDatabase(String username) {
        String deleteQuery = "DELETE FROM user_login_credentials WHERE username = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(deleteQuery)) {
            stmt.setString(1, username);
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while deleting the volunteer.");
            return false;
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    // Volunteer class to hold volunteer details
    private static class Volunteer {
        private String username;
        private String password;
        private String gender;
        private Date dateOfBirth;

        public Volunteer(String username, String password, String gender, Date dateOfBirth) {
            this.username = username;
            this.password = password;
            this.gender = gender;
            this.dateOfBirth = dateOfBirth;
        }

        public String getUsername() {
            return username;
        }

        public String getPassword() {
            return password;
        }

        public String getGender() {
            return gender;
        }

        public Date getDateOfBirth() {
            return dateOfBirth;
        }
    }
}
