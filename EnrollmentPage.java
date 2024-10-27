import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import java.util.List;
import java.util.ArrayList;


public class EnrollmentPage {

    JFrame frame = new JFrame();
    JLabel successLabel = new JLabel("Successfully enrolled in the event!");
    JButton homeButton = new JButton("Go to Home");

    EnrollmentPage(String userID, List<Event> events) {
        // Setup the frame
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(300, 200);
        frame.setLayout(new FlowLayout());

        // Add success label
        successLabel.setFont(new Font(null, Font.BOLD, 16));
        frame.add(successLabel);

        // Home button action
        homeButton.addActionListener(e -> {
            frame.dispose();
            new WelcomePage(userID, events);  // Go back to the WelcomePage
        });
        
        // Add home button
        frame.add(homeButton);
        
        // Make frame visible
        frame.setVisible(true);
    }
}




