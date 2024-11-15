import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ManageVolunteerPage extends JFrame {
    private List<String[]> volunteers = new ArrayList<>(); // List to store volunteer details
    private DefaultTableModel tableModel;
    private JTable volunteerTable;
    private JTextField searchField;
    private JComboBox<String> searchCriteriaComboBox;

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
        displayVolunteers(volunteers);
        JScrollPane scrollPane = new JScrollPane(volunteerTable);

        // Search panel with search field and criteria combo box
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchField = new JTextField(15);
        searchCriteriaComboBox = new JComboBox<>(new String[]{"Username", "Gender", "Date of Birth"});
        JButton searchButton = new JButton("Search");

        searchButton.addActionListener(e -> searchVolunteers());

        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(new JLabel("by"));
        searchPanel.add(searchCriteriaComboBox);
        searchPanel.add(searchButton);

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

        add(searchPanel, BorderLayout.NORTH);
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

                // Store the details as an array
                volunteers.add(new String[]{username, password, gender, dateOfBirth.toString()});
            }
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(this, "Database error occurred while fetching volunteers.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
    }

    private void displayVolunteers(List<String[]> volunteerList) {
        tableModel.setRowCount(0); // Clear the table
        for (String[] volunteer : volunteerList) {
            tableModel.addRow(volunteer); // Add each volunteer's details as a row
        }
    }

    private void searchVolunteers() {
        String searchTerm = searchField.getText().trim().toLowerCase();
        String criteria = searchCriteriaComboBox.getSelectedItem().toString();

        List<String[]> filteredVolunteers = new ArrayList<>();
        for (String[] volunteer : volunteers) {
            boolean matches = switch (criteria) {
                case "Username" -> volunteer[0].toLowerCase().contains(searchTerm);
                case "Gender" -> volunteer[2].toLowerCase().contains(searchTerm);
                case "Date of Birth" -> volunteer[3].contains(searchTerm);
                default -> false;
            };

            if (matches) {
                filteredVolunteers.add(volunteer);
            }
        }

        displayVolunteers(filteredVolunteers);
    }

    private void addVolunteer() {
        String[] volunteerDetails = getVolunteerDetails(null);
        if (volunteerDetails != null) {
            if (saveVolunteerToDatabase(volunteerDetails)) {
                fetchVolunteersFromDatabase();
                displayVolunteers(volunteers);
            }
        }
    }

    private String[] getVolunteerDetails(String[] existingDetails) {
        JTextField usernameField = new JTextField(existingDetails != null ? existingDetails[0] : "");
        JTextField passwordField = new JTextField(existingDetails != null ? existingDetails[1] : "");

        // Gender JComboBox with Male, Female, and Other options
        JComboBox<String> genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        // Set initial selection for gender if existing details are provided
        if (existingDetails != null) {
            genderComboBox.setSelectedItem(existingDetails[2]);
        }

        // Day, Month, and Year JComboBoxes for date of birth
        JComboBox<String> dayComboBox = new JComboBox<>(generateDays());
        JComboBox<String> monthComboBox = new JComboBox<>(generateMonths());
        JComboBox<String> yearComboBox = new JComboBox<>(generateYears());

        // Set initial selections based on volunteer data if available
        if (existingDetails != null) {
            LocalDate dob = LocalDate.parse(existingDetails[3]);
            dayComboBox.setSelectedItem(String.format("%02d", dob.getDayOfMonth()));
            monthComboBox.setSelectedItem(dob.getMonth().name().substring(0, 3));
            yearComboBox.setSelectedItem(String.valueOf(dob.getYear()));
        }

        // Displaying the form to collect volunteer details
        Object[] message = {
            "Username:", usernameField,
            "Password:", passwordField,
            "Gender:", genderComboBox,
            "Date of Birth (Day, Month, Year):", dayComboBox, monthComboBox, yearComboBox
        };

        int option = JOptionPane.showConfirmDialog(this, message,
                existingDetails == null ? "Add Volunteer" : "Update Volunteer", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            // Convert selected day, month, and year into a Date object
            int day = Integer.parseInt((String) dayComboBox.getSelectedItem());
            int month = monthToNumber((String) monthComboBox.getSelectedItem());
            int year = Integer.parseInt((String) yearComboBox.getSelectedItem());

            return new String[]{
                usernameField.getText(),
                passwordField.getText(),
                (String) genderComboBox.getSelectedItem(),
                LocalDate.of(year, month, day).toString()
            };
        } else {
            return null;
        }
    }

    private boolean saveVolunteerToDatabase(String[] volunteerDetails) {
        String insertQuery = "INSERT INTO user_login_credentials (username, password, gender, date_of_birth) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(insertQuery)) {
            stmt.setString(1, volunteerDetails[0]);
            stmt.setString(2, volunteerDetails[1]);
            stmt.setString(3, volunteerDetails[2]);
            stmt.setDate(4, Date.valueOf(volunteerDetails[3])); // Convert string to Date
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
            String[] volunteerDetails = volunteers.get(selectedRow);
            String[] updatedDetails = getVolunteerDetails(volunteerDetails);
            if (updatedDetails != null) {
                if (updateVolunteerInDatabase(volunteerDetails[0], updatedDetails)) {
                    fetchVolunteersFromDatabase();
                    displayVolunteers(volunteers);
                }
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a volunteer to update.");
        }
    }

    private boolean updateVolunteerInDatabase(String oldUsername, String[] newVolunteerDetails) {
        String updateQuery = "UPDATE user_login_credentials SET username = ?, password = ?, gender = ?, date_of_birth = ? WHERE username = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(updateQuery)) {
            stmt.setString(1, newVolunteerDetails[0]);
            stmt.setString(2, newVolunteerDetails[1]);
            stmt.setString(3, newVolunteerDetails[2]);
            stmt.setDate(4, Date.valueOf(newVolunteerDetails[3]));
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
            String username = volunteers.get(selectedRow)[0];
            if (deleteVolunteerFromDatabase(username)) {
                fetchVolunteersFromDatabase();
                displayVolunteers(volunteers);
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

    // Helper methods to generate day, month, and year values
    private String[] generateDays() {
        String[] days = new String[31];
        for (int i = 0; i < 31; i++) {
            days[i] = String.format("%02d", i + 1);
        }
        return days;
    }

    private String[] generateMonths() {
        return new String[]{"Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec"};
    }

    private String[] generateYears() {
        int currentYear = LocalDate.now().getYear();
        String[] years = new String[100]; // Example: range of 100 years
        for (int i = 0; i < 100; i++) {
            years[i] = String.valueOf(currentYear - i);
        }
        return years;
    }

    // Convert month abbreviation to numeric value (Jan -> 1, Feb -> 2, etc.)
    private int monthToNumber(String month) {
        switch (month) {
            case "Jan": return 1;
            case "Feb": return 2;
            case "Mar": return 3;
            case "Apr": return 4;
            case "May": return 5;
            case "Jun": return 6;
            case "Jul": return 7;
            case "Aug": return 8;
            case "Sep": return 9;
            case "Oct": return 10;
            case "Nov": return 11;
            case "Dec": return 12;
            default: throw new IllegalArgumentException("Invalid month: " + month);
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            ManageVolunteerPage page = new ManageVolunteerPage();
            page.setVisible(true);
        });
    }
}
