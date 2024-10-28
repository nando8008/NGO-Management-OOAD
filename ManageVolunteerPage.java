import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

public class ManageVolunteerPage extends JFrame {
    private List<String> volunteers;
    private DefaultTableModel tableModel;
    private JTable volunteerTable;

    public ManageVolunteerPage(List<String> volunteers) {
        this.volunteers = volunteers;

        setTitle("Manage Volunteers");
        setSize(600, 400);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Set up table and model without Actions column
        String[] columnNames = {"Volunteer Name"};
        tableModel = new DefaultTableModel(columnNames, 0);
        volunteerTable = new JTable(tableModel);
        displayVolunteers();
        JScrollPane scrollPane = new JScrollPane(volunteerTable);

        // Add, Update, and Delete buttons
        JPanel buttonPanel = new JPanel();
        JButton addButton = new JButton("Add Volunteer");
        JButton updateButton = new JButton("Update Volunteer");
        JButton deleteButton = new JButton("Delete Volunteer");

        addButton.addActionListener(e -> addVolunteer());
        updateButton.addActionListener(e -> updateVolunteer());
        deleteButton.addActionListener(e -> deleteVolunteer());

        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void displayVolunteers() {
        tableModel.setRowCount(0); // Clear the table
        for (String volunteer : volunteers) {
            tableModel.addRow(new Object[]{volunteer});
        }
    }

    private void addVolunteer() {
        String volunteerName = JOptionPane.showInputDialog(this, "Enter volunteer name:");
        if (volunteerName != null && !volunteerName.trim().isEmpty()) {
            volunteers.add(volunteerName);
            displayVolunteers();
        }
    }

    private void updateVolunteer() {
        int selectedRow = volunteerTable.getSelectedRow();
        if (selectedRow >= 0) {
            String currentName = volunteers.get(selectedRow);
            String newName = JOptionPane.showInputDialog(this, "Update volunteer name:", currentName);
            if (newName != null && !newName.trim().isEmpty()) {
                volunteers.set(selectedRow, newName);
                displayVolunteers();
            }
        } else {
            JOptionPane.showMessageDialog(this, "Please select a volunteer to update.");
        }
    }

    private void deleteVolunteer() {
        int selectedRow = volunteerTable.getSelectedRow();
        if (selectedRow >= 0) {
            volunteers.remove(selectedRow);
            displayVolunteers();
        } else {
            JOptionPane.showMessageDialog(this, "Please select a volunteer to delete.");
        }
    }
}



