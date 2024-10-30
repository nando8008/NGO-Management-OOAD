import javax.swing.*;
import java.awt.*;
import java.util.List;

public class AdminPage extends JFrame {
    private List<String> volunteers;
    private JButton manageEventsButton;
    private JButton manageVolunteersButton;
    private JButton checkSubmittedProjectsButton;
    private JButton answerQueryButton;

    public AdminPage(List<String> volunteers) {
        this.volunteers = volunteers;
        
        setTitle("Admin Page");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        // Create buttons with consistent size and appearance
        manageEventsButton = createSizedButton("Manage Events", 150, 40);
        manageVolunteersButton = createSizedButton("Manage Volunteers", 150, 40);
        checkSubmittedProjectsButton = createSizedButton("Check Projects", 150, 40);
        answerQueryButton = createSizedButton("Answer Query", 150, 40);

        // Action for managing events
        manageEventsButton.addActionListener(e -> new ManageEventPage().setVisible(true));
        
        // Action for managing volunteers
        manageVolunteersButton.addActionListener(e -> new ManageVolunteerPage().setVisible(true));
        
        // Action for checking submitted projects
        checkSubmittedProjectsButton.addActionListener(e -> new ProposedProject().setVisible(true));

        // Action for answering queries
        answerQueryButton.addActionListener(e -> new ManageQueries().setVisible(true));

        // Add buttons to the frame
        add(manageEventsButton);
        add(manageVolunteersButton);
        add(checkSubmittedProjectsButton);
        add(answerQueryButton);
    }

    // Helper method to create buttons with consistent size and appearance
    private JButton createSizedButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }
}
