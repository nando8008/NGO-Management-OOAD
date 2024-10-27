import java.awt.*;
import javax.swing.*;
import javax.swing.table.TableCellRenderer;

public class ButtonRenderer extends JButton implements TableCellRenderer {
    public ButtonRenderer() {
        setOpaque(true);
    }

    @Override
    public Component getTableCellRendererComponent(
        JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column
    ) {
        setText((value == null) ? "Enroll" : value.toString());
        return this;
    }
}


