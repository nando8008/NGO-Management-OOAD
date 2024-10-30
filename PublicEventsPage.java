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
            JButton donateButton = new JButton("Donate");

    
            donateButton.addActionListener(e -> JOptionPane.showMessageDialog(this, "Donated to " + event.getName()));

            add(eventLabel);
            add(donateButton);
        }
    }
}