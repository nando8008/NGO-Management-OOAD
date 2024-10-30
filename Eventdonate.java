

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public class Eventdonate extends JFrame {
    private JTable eventTable;
    private List<Event> events;

    public Eventdonate(List<Event> events) {
        this.events = events;
        setTitle("Upcoming Events");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Define column names for the event table
        String[] columnNames = {"Event Name", "Event Date", "Event Type", "Action"};

        // Create a table model
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel) {
            // Disable editing for all cells
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only allow the action button to be editable
            }
        };

        // Populate the table with event data and buttons
        for (Event event : events) {
            Object[] rowData = {
                event.getName(),
                event.getDate(),
                event.getType(),
                "Donate" // Placeholder for the button
            };
            tableModel.addRow(rowData);
        }

        // Set the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(eventTable);
        eventTable.setFillsViewportHeight(true);

        // Set custom cell renderer for the action button
        eventTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        eventTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), events));

        // Add components to the frame
        add(scrollPane, BorderLayout.CENTER); // Add the table
    }

    // Custom renderer to display a button in the cell
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(UIManager.getColor("Button.background")); // Use default button color
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "Donate" : value.toString());
            return this;
        }
    }

    // Custom editor to handle button clicks
    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;
        private List<Event> events;

        public ButtonEditor(JCheckBox checkBox, List<Event> events) {
            super(checkBox);
            this.events = events;
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(UIManager.getColor("Button.background")); // Use default button color
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped(); // Stop editing and notify listeners
                    int row = eventTable.getSelectedRow();
                    if (row >= 0) {
                        Event event = events.get(row);
                        JOptionPane.showMessageDialog(button, "Donated to " + event.getName());
                    }
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "Donate" : value.toString();
            button.setText(label);
            isPushed = true;
            return button;
        }

        @Override
        public Object getCellEditorValue() {
            if (isPushed) {
                //JOptionPane.showMessageDialog(button, "Donated to " + label);
            }
            isPushed = false;
            return label;
        }
    }
}