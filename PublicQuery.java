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

    public PublicQuery() {
        setTitle("Public Queries");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        // Define table model and column names
        String[] columnNames = {"Question", "Answer"};
        DefaultTableModel tableModel = new DefaultTableModel(columnNames, 0);
        queryTable = new JTable(tableModel);

        // Load only questions with non-empty answers from the database
        loadAnsweredQueriesFromDatabase(tableModel);

        // Add scroll pane for the table
        JScrollPane scrollPane = new JScrollPane(queryTable);

        // Initialize and add "Ask a Query" button
        askQueryButton = new JButton("Ask a Query");
        askQueryButton.setPreferredSize(new Dimension(120, 30));
        askQueryButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                openAskQueryDialog();
            }
        });

        // Panel to hold the button at the bottom
        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
        buttonPanel.add(askQueryButton);

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
}
