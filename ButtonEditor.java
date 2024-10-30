import javax.swing.*;
import javax.swing.table.TableCellEditor;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ButtonEditor extends DefaultCellEditor implements TableCellEditor {
    private JButton button;
    private String label;
    private boolean isPushed;
    private WelcomePage welcomePage;
    private int eventId;
    private String eventName;

    public ButtonEditor(JCheckBox checkBox, WelcomePage welcomePage) {
        super(checkBox);
        this.welcomePage = welcomePage;
        button = new JButton();
        button.setOpaque(true);
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                fireEditingStopped(); // Stop editing when button is pressed
                // Handle the button click action
                if (eventId != -1 && welcomePage.isEnrolled(eventId)) {
                    JOptionPane.showMessageDialog(button, "You are already enrolled.");
                } else if (eventName != null && welcomePage.isEnrolled(eventName)) {
                    JOptionPane.showMessageDialog(button, "You are already enrolled.");
                } else {
                    // Add enrollment logic
                    if (eventId != -1) {
                        welcomePage.updateEnrollmentStatus(eventId);
                    } else if (eventName != null) {
                        welcomePage.updateEnrollmentStatus(eventName);
                    }
                }
            }
        });
    }

    @Override
    public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
        label = (value == null) ? "Enroll" : value.toString();
        button.setText(label);

        // Reset variables
        eventId = -1;
        eventName = null;

        // Get the event ID or name for the current row
        Object cellValue = table.getValueAt(row, 0);
        if (cellValue instanceof Integer) {
            eventId = (Integer) cellValue;
            System.out.println("Event ID: " + eventId);
        } else if (cellValue instanceof String) {
            eventName = (String) cellValue;
            System.out.println("Event Name: " + eventName);
        }

        isPushed = true;
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        if (isPushed) {
            // Perform action if button was pushed
        }
        isPushed = false;
        return label;
    }

    @Override
    public boolean stopCellEditing() {
        isPushed = false;
        return super.stopCellEditing();
    }
}