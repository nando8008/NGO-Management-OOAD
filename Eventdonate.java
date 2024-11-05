import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;
import java.util.stream.Collectors;

public class Eventdonate extends JFrame {
    private JTable eventTable;
    private List<Event> events;
    private DefaultTableModel tableModel;
    private JTextField searchField;
    private JComboBox<String> searchOptions;

    public Eventdonate(List<Event> events) {
        this.events = events;
        setTitle("Upcoming Events");
        setSize(600, 450);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        // Define column names for the event table
        String[] columnNames = {"Event Name", "Event Date", "Event Type", "Action"};

        // Create a table model
        tableModel = new DefaultTableModel(columnNames, 0);
        eventTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 3; // Only allow the action button to be editable
            }
        };

        // Populate the table with event data and buttons
        populateTable(events);

        // Set the table to a scroll pane
        JScrollPane scrollPane = new JScrollPane(eventTable);
        eventTable.setFillsViewportHeight(true);

        // Set custom cell renderer for the action button
        eventTable.getColumnModel().getColumn(3).setCellRenderer(new ButtonRenderer());
        eventTable.getColumnModel().getColumn(3).setCellEditor(new ButtonEditor(new JCheckBox(), events));

        // Search components
        searchField = new JTextField(15);
        searchOptions = new JComboBox<>(new String[]{"Event Name", "Event Date", "Event Type"});
        JButton searchButton = new JButton("Search");

        // Search panel to hold the search bar and options
        JPanel searchPanel = new JPanel();
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchOptions);
        searchPanel.add(searchButton);

        // Add action listener to search button
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                filterTable();
            }
        });

        // Add components to the frame
        add(searchPanel, BorderLayout.NORTH); // Add the search panel at the top
        add(scrollPane, BorderLayout.CENTER); // Add the table
    }

    // Populate table with event data
    private void populateTable(List<Event> eventList) {
        tableModel.setRowCount(0); // Clear existing rows
        for (Event event : eventList) {
            Object[] rowData = {
                event.getName(),
                event.getDate(),
                event.getType(),
                "Donate"
            };
            tableModel.addRow(rowData);
        }
    }

    // Filter the table based on the search criteria
    private void filterTable() {
        String searchText = searchField.getText().toLowerCase();
        String searchOption = (String) searchOptions.getSelectedItem();

        List<Event> filteredEvents = events.stream()
                .filter(event -> {
                    switch (searchOption) {
                        case "Event Name":
                            return event.getName().toLowerCase().contains(searchText);
                        case "Event Date":
                            return event.getDate().toLowerCase().contains(searchText);
                        case "Event Type":
                            return event.getType().toLowerCase().contains(searchText);
                        default:
                            return false;
                    }
                })
                .collect(Collectors.toList());

        populateTable(filteredEvents); // Update table with filtered events
    }

    // Custom renderer to display a button in the cell
    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(UIManager.getColor("Button.background"));
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
            button.setBackground(UIManager.getColor("Button.background"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
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
            isPushed = false;
            return label;
        }
    }
}
