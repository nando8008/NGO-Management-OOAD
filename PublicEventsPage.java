import javax.swing.*;
import java.awt.*;
import java.util.List;

public class PublicEventsPage extends JFrame {
    public PublicEventsPage(List<Event> events) {
        setTitle("Upcoming Events");
        setSize(400, 300);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new GridLayout(events.size(), 3));

        for (Event event : events) {
            JLabel eventLabel = new JLabel(event.getName());
            JButton enrollButton = new JButton("Enroll");
            JButton donateButton = new JButton("Donate");

            enrollButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Enrolled in " + event.getName()));
            donateButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Donated to " + event.getName()));

            add(eventLabel);
            add(enrollButton);
            add(donateButton);
        }
    }
}
