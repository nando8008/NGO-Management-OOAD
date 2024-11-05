import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class PublicQuery extends JFrame {
    private JTable queryTable;
    private JButton askQueryButton;
    private JTextField searchField;

    public PublicQuery() {
        setTitle("Public Queries");
        setSize(500, 350);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout());

        // Define table model and column names
        String[] columnNames = {"Question", "Answer"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        queryTable = new JTable(tableModel);

        // Load only questions with non-empty answers from the database
        loadAnsweredQueriesFromDatabase(tableModel);

        // Scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(queryTable);

        // Initialize "Ask a Query" button
        askQueryButton = new JButton("Ask a Query");
        askQueryButton.setPreferredSize(new Dimension(120, 30));
        askQueryButton.addActionListener(e -> openAskQueryDialog());

        // Search bar components
        searchField = new JTextField(20);
        JButton searchButton = new JButton("Search");

        // Add action listener for the search button
        searchButton.addActionListener(e -> filterQueries(tableModel));

        // Panel to hold the search bar
        JPanel searchPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        searchPanel.add(new JLabel("Search:"));
        searchPanel.add(searchField);
        searchPanel.add(searchButton);

        // Panel to hold the "Ask a Query" button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(askQueryButton);

        // Add components to frame
        add(searchPanel, BorderLayout.NORTH);
        add(scrollPane, BorderLayout.CENTER);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    private void openAskQueryDialog() {
        JTextField questionField = new JTextField();

        Object[] message = {
            "Your Question:", questionField
        };

        int option = JOptionPane.showConfirmDialog(this, message, "Ask a Query", JOptionPane.OK_CANCEL_OPTION);

        if (option == JOptionPane.OK_OPTION) {
            String question = questionField.getText().trim();

            if (!question.isEmpty()) {
                insertQueryIntoDatabase(question);
                JOptionPane.showMessageDialog(this, "Your question has been submitted.");
            } else {
                JOptionPane.showMessageDialog(this, "Please enter a question.");
            }
        }
    }

    private void insertQueryIntoDatabase(String question) {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "INSERT INTO public_query (question, answer) VALUES (?, NULL)";

            try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                stmt.setString(1, question);
                stmt.executeUpdate();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    private void loadAnsweredQueriesFromDatabase(DefaultTableModel tableModel) {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT question, answer FROM public_query WHERE answer IS NOT NULL AND answer <> ''";

            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String question = rs.getString("question");
                    String answer = rs.getString("answer");
                    tableModel.addRow(new Object[]{question, answer});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    // Method to filter queries based on search text
    private void filterQueries(DefaultTableModel tableModel) {
        String searchText = searchField.getText().trim().toLowerCase();

        // Clear the current rows in the table
        tableModel.setRowCount(0);

        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT question, answer FROM public_query WHERE answer IS NOT NULL AND answer <> ''";

            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String question = rs.getString("question").toLowerCase();
                    String answer = rs.getString("answer");

                    // Add rows that match the search criteria
                    if (question.contains(searchText)) {
                        tableModel.addRow(new Object[]{rs.getString("question"), answer});
                    }
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }
}

