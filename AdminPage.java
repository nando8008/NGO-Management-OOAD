import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class AdminPage extends JFrame {
    private List<String> volunteers;
    private JButton manageEventsButton;
    private JButton manageVolunteersButton;

    public AdminPage(List<String> volunteers) {
        this.volunteers = volunteers;
        
        setTitle("Admin Page");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new FlowLayout());

        manageEventsButton = createSizedButton("Manage Events", 150, 40);
        manageVolunteersButton = createSizedButton("Manage Volunteers", 150, 40);

        // Open ManageEventPage without passing events, as it fetches events directly from the database
        manageEventsButton.addActionListener(e -> new ManageEventPage().setVisible(true));
        
        // Open ManageVolunteerPage, passing the volunteers list
        manageVolunteersButton.addActionListener(e -> new ManageVolunteerPage(volunteers).setVisible(true));

        add(manageEventsButton);
        add(manageVolunteersButton);
    }

    private JButton createSizedButton(String text, int width, int height) {
        JButton button = new JButton(text);
        button.setPreferredSize(new Dimension(width, height));
        return button;
    }
}
