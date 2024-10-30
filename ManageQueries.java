import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ManageQueries extends JFrame {
    private JTable queryTable;
    private DefaultTableModel tableModel;

    public ManageQueries() {
        setTitle("Manage Queries");
        setSize(500, 300);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

        String[] columnNames = {"Question", "Action"};
        tableModel = new DefaultTableModel(columnNames, 0);
        queryTable = new JTable(tableModel) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 1; // Only the action button should be editable
            }
        };

        // Load unanswered queries from the database
        loadUnansweredQueries();

        // Set custom cell renderer and editor for the Answer button
        TableColumn actionColumn = queryTable.getColumnModel().getColumn(1);
        actionColumn.setCellRenderer(new ButtonRenderer());
        actionColumn.setCellEditor(new ButtonEditor(new JCheckBox()));

        JScrollPane scrollPane = new JScrollPane(queryTable);
        add(scrollPane, BorderLayout.CENTER);
    }

    private void loadUnansweredQueries() {
        Connection conn = DatabaseConnection.connect();
        if (conn != null) {
            String sql = "SELECT question FROM public_query WHERE answer IS NULL OR answer = ''";

            try (PreparedStatement stmt = conn.prepareStatement(sql); ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    String question = rs.getString("question");
                    tableModel.addRow(new Object[]{question, "Answer"});
                }
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
            } finally {
                DatabaseConnection.disconnect(conn);
            }
        }
    }

    private void answerQuery(String question) {
        JTextField answerField = new JTextField();
        Object[] message = {"Answer:", answerField};

        int option = JOptionPane.showConfirmDialog(this, message, "Answer Query", JOptionPane.OK_CANCEL_OPTION);
        if (option == JOptionPane.OK_OPTION) {
            String answer = answerField.getText().trim();

            if (!answer.isEmpty()) {
                Connection conn = DatabaseConnection.connect();
                if (conn != null) {
                    String sql = "UPDATE public_query SET answer = ? WHERE question = ?";

                    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
                        stmt.setString(1, answer);
                        stmt.setString(2, question);

                        int rowsUpdated = stmt.executeUpdate();
                        if (rowsUpdated > 0) {
                            JOptionPane.showMessageDialog(this, "Query answered successfully.");
                            tableModel.removeRow(queryTable.getSelectedRow());
                        } else {
                            JOptionPane.showMessageDialog(this, "Failed to update the answer.");
                        }
                    } catch (SQLException ex) {
                        ex.printStackTrace();
                        JOptionPane.showMessageDialog(this, "Database error: " + ex.getMessage());
                    } finally {
                        DatabaseConnection.disconnect(conn);
                    }
                }
            } else {
                JOptionPane.showMessageDialog(this, "Answer cannot be empty.");
            }
        }
    }

    class ButtonRenderer extends JButton implements TableCellRenderer {
        public ButtonRenderer() {
            setOpaque(true);
            setBackground(UIManager.getColor("Button.background"));
        }

        @Override
        public Component getTableCellRendererComponent(JTable table, Object value,
                                                       boolean isSelected, boolean hasFocus, int row, int column) {
            setText((value == null) ? "" : value.toString());
            return this;
        }
    }

    class ButtonEditor extends DefaultCellEditor {
        private JButton button;
        private String label;
        private boolean isPushed;

        public ButtonEditor(JCheckBox checkBox) {
            super(checkBox);
            button = new JButton();
            button.setOpaque(true);
            button.setBackground(UIManager.getColor("Button.background"));
            button.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    fireEditingStopped();
                    int row = queryTable.getSelectedRow();
                    String question = (String) tableModel.getValueAt(row, 0);
                    answerQuery(question);
                }
            });
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value,
                                                     boolean isSelected, int row, int column) {
            label = (value == null) ? "" : value.toString();
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
