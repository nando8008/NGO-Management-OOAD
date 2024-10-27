import java.awt.*;
import javax.swing.*;

public class ButtonEditor extends DefaultCellEditor {
    private JButton button;
    private String eventName;
    private WelcomePage welcomePage;  // Reference to WelcomePage

    public ButtonEditor(JCheckBox checkBox, WelcomePage welcomePage) {
        super(checkBox);
        this.welcomePage = welcomePage;

        button = new JButton();
        button.setOpaque(true);

        // Add action listener for button clicks
        button.addActionListener(e -> fireEditingStopped());
    }

    @Override
    public Component getTableCellEditorComponent(
        JTable table, Object value, boolean isSelected, int row, int column
    ) {
        eventName = table.getValueAt(row, 0).toString();  // Get event name
        button.setText((value == null) ? "Enroll" : value.toString());
        return button;
    }

    @Override
    public Object getCellEditorValue() {
        welcomePage.showEnrollmentPopup(eventName);  // Show popup
        return eventName;
    }
}



