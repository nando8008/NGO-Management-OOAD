import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.ArrayList;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private List<String> volunteers;
    private List<Event> events;

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

        loginButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                String username = usernameField.getText();
                String password = new String(passwordField.getPassword());

                int userId = validateCredentials(username, password);
                if (userId != -1) {
                    if (isAdmin(username)) {
                        new AdminPage(volunteers).setVisible(true);
                    } else {
                        new WelcomePage(userId, volunteers).setVisible(true);
                    }
                    dispose();
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials");
                }
            }
        });

        guestButton.addActionListener(e -> 
            new PublicEventsPage(getEventsFromDatabase()).setVisible(true)
        );

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(guestButton);
    }

    private int validateCredentials(String username, String password) {
        String userQuery = "SELECT user_id FROM user_login_credentials WHERE username = ? AND password = ?";
        String adminQuery = "SELECT admin_id FROM admin_credentials WHERE admin_username = ? AND admin_password = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement userStmt = conn.prepareStatement(userQuery);
             PreparedStatement adminStmt = conn.prepareStatement(adminQuery)) {

            userStmt.setString(1, username);
            userStmt.setString(2, password);
            ResultSet userRs = userStmt.executeQuery();
            if (userRs.next()) {
                return userRs.getInt("user_id");
            }

            adminStmt.setString(1, username);
            adminStmt.setString(2, password);
            ResultSet adminRs = adminStmt.executeQuery();
            if (adminRs.next()) {
                return adminRs.getInt("admin_id");
            }

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return -1; // Return -1 for invalid credentials
    }

    private boolean isAdmin(String username) {
        String query = "SELECT * FROM admin_credentials WHERE admin_username = ?";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, username);
            ResultSet rs = stmt.executeQuery();
            return rs.next();

        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Database error occurred.");
        } finally {
            DatabaseConnection.disconnect(conn);
        }
        return false;
    }

    private List<Event> getEventsFromDatabase() {
        List<Event> events = new ArrayList<>();
        String query = "SELECT event_id, event_name, event_date, event_type FROM events";
        Connection conn = DatabaseConnection.connect();

        try (PreparedStatement stmt = conn.prepareStatement(query);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                int id = rs.getInt("event_id");
                String name = rs.getString("event_name");
                String date = rs.getString("event_date");
                String type = rs.getString("event_type");
                events.add(new Event(name, date, type, id));
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