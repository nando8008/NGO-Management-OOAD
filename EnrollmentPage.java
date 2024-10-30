import javax.swing.*;

public class EnrollmentPage extends JFrame {
    public EnrollmentPage(Event event) {
        setTitle("Enroll in " + event.getName());
        setSize(300, 200);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BoxLayout(getContentPane(), BoxLayout.Y_AXIS));

        JLabel eventLabel = new JLabel("Enroll in " + event.getName());
        eventLabel.setAlignmentX(CENTER_ALIGNMENT);

        JButton confirmButton = new JButton("Confirm Enrollment");
        confirmButton.setAlignmentX(CENTER_ALIGNMENT);
        confirmButton.addActionListener(e -> {
            JOptionPane.showMessageDialog(this, "You are enrolled in " + event.getName() + "!");
            dispose(); // Close window after confirmation
        });

        add(eventLabel);
        add(Box.createVerticalStrut(20));
        add(confirmButton);
    }
}



