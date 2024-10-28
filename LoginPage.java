import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.List;

public class LoginPage extends JFrame {
    private JTextField usernameField;
    private JPasswordField passwordField;
    private HashMap<String, String> loginInfo;
    private List<Event> events;
    private List<String> volunteers;

    public LoginPage(HashMap<String, String> loginInfo, List<Event> events, List<String> volunteers) {
        this.loginInfo = loginInfo;
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

                if (loginInfo.containsKey(username) && loginInfo.get(username).equals(password)) {
                    if (username.equals("admin")) {
                        // Pass both events and volunteers to AdminPage
                        new AdminPage(events, volunteers).setVisible(true);
                    } else {
                        new WelcomePage(username, events, volunteers).setVisible(true);
                    }
                    dispose(); // Close the login page
                } else {
                    JOptionPane.showMessageDialog(null, "Invalid credentials");
                }
            }
        });

        guestButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                new PublicEventsPage(events).setVisible(true);
                dispose(); // Close the login page
            }
        });

        add(usernameLabel);
        add(usernameField);
        add(passwordLabel);
        add(passwordField);
        add(loginButton);
        add(guestButton);
    }
}

