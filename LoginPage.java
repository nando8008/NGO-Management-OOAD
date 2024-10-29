import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private List<String> volunteers;
    private List<Event> events;  // Store events for later use

    public LoginPage(List<Event> events, List<String> volunteers) {
        this.events = events;
        this.volunteers = volunteers;

        setTitle("Login Page");
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(4, 2));

        JLabel usernameLabel = new JLabel("Username:");
        usernameField = new JTextField();
        JLabel passwordLabel = new JLabel("Password:");
        passwordField = new JPasswordField();

        JButton loginButton = new JButton("Login");
        JButton guestButton = new JButton("Continue as Guest");

        // ActionListener for Login Button
        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                if (validateCredentials(username, password)) {
                    if (isAdmin(username)) {
                        new AdminPage(events, volunteers).setVisible(true);
                    } else {
                        new WelcomePage(username, volunteers).setVisible(true);
                    }
                    dispose();  // Close the login page
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials");
                }
            }
        });

        // Lambda for Guest Button
        guestButton.addActionListener(e -> 
            new PublicEventsPage(getEventsFromDatabase()).setVisible(true)
        );

        // Add components to the layout
        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(guestButton);
    }

    // Validate user or admin credentials
    private boolean validateCredentials(String username, String password) {
        String userQuery = "SELECT * FROM user_login_credentials WHERE username = ? AND password = ?";
        String adminQuery = "SELECT * FROM admin_credentials WHERE admin_username = ? AND admin_password = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement userStmt = conn.prepareStatement(userQuery);
             PreparedStatement adminStmt = conn.prepareStatement(adminQuery)) {

            userStmt.setString(1, username);
            userStmt.setString(2, password);
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                return true;  // User found
            }

            adminStmt.setString(1, username);
            adminStmt.setString(2, password);
            ResultSet adminRs = adminStmt.executeQuery();
            if (adminRs.next()) {
                return true;  // Admin found
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return false;  // No matching user or admin found
    }

    // Check if the user is an admin
    private boolean isAdmin(String username) {
        String query = "SELECT * FROM admin_credentials WHERE admin_username = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();  // If a record is found, user is an admin

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return false;
    }

    // Retrieve events from the database
    private List<Event> getEventsFromDatabase() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT event_name, event_date, event_type FROM events";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                String name = rs.getString("event_name");
                String date = rs.getString("event_date");
                String type = rs.getString("event_type");
                events.add(new Event(name, date, type));
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return events;
    }
}






