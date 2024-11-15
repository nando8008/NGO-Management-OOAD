import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.stream.IntStream;

public class VolunteerRegister extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private JComboBox<String> genderComboBox;
    private JComboBox<Integer> dayComboBox;
    private JComboBox<String> monthComboBox;
    private JComboBox<Integer> yearComboBox;

    public VolunteerRegister() {
        setTitle("Volunteer Register");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new GridLayout(6, 2));

        // Components
        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();

        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JLabel genderLabel = new JLabel("Gender:");
        genderComboBox = new JComboBox<>(new String[]{"Male", "Female", "Other"});

        JLabel dobLabel = new JLabel("Date of Birth:");
        dayComboBox = new JComboBox<>(IntStream.rangeClosed(1, 31).boxed().toArray(Integer[]::new));
        monthComboBox = new JComboBox<>(new String[]{"January", "February", "March", "April", "May", "June",
                "July", "August", "September", "October", "November", "December"});
        yearComboBox = new JComboBox<>(IntStream.rangeClosed(1900, 2024).boxed().toArray(Integer[]::new));
        yearComboBox.setSelectedItem(2000); // Default year selection

        JButton submitButton = new JButton("Submit");

        // Submit button action
        submitButton.addActionListener(e -> {
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String gender = (String) genderComboBox.getSelectedItem();
            int day = (Integer) dayComboBox.getSelectedItem();
            String month = (String) monthComboBox.getSelectedItem();
            int year = (Integer) yearComboBox.getSelectedItem();

            Date dob = java.sql.Date.valueOf(String.format("%04d-%02d-%02d", year, monthToNumber(month), day));

            if (registerVolunteer(username, password, gender, dob)) {
                JOptionPane.showMessageDialog(null, "Registration successful! Await admin approval.");
                dispose();
            } else {
                JOptionPane.showMessageDialog(null, "Registration failed. Please try again.");
            }
        });

        // Add components to the frame
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(genderLabel);
        add(genderComboBox);
        add(dobLabel);
        JPanel dobPanel = new JPanel(new GridLayout(1, 3));
        dobPanel.add(dayComboBox);
        dobPanel.add(monthComboBox);
        dobPanel.add(yearComboBox);
        add(dobPanel);
        add(submitButton);
    }

    private boolean registerVolunteer(String username, String password, String gender, Date dob) {
        String query = "INSERT INTO user_pending_login_credentials (username, password, gender, date_of_birth) VALUES (?, ?, ?, ?)";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            stmt.setString(2, password);
            stmt.setString(3, gender);
            stmt.setDate(4, dob); // Pass date as a java.sql.Date
            stmt.executeUpdate();
            return true;
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return false;
    }

    private int monthToNumber(String month) {
        switch (month) {
            case "January": return 1;
            case "February": return 2;
            case "March": return 3;
            case "April": return 4;
            case "May": return 5;
            case "June": return 6;
            case "July": return 7;
            case "August": return 8;
            case "September": return 9;
            case "October": return 10;
            case "November": return 11;
            case "December": return 12;
            default: throw new IllegalArgumentException("Invalid month: " + month);
        }
    }
}

