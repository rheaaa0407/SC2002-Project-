package restaurantrush;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;

/** Displays the waiting queue and tables without changing game state. */
public final class FloorPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel queueModel = readOnlyModel(
            new String[] {"Customer", "Type", "Satisfaction"});

    private final DefaultTableModel tablesModel = readOnlyModel(
            new String[] {
                "Table", "Customer", "Type", "Satisfaction", "Status"
            });

    public FloorPanel() {
        super(new GridLayout(2, 1, 0, 12));
        setBorder(BorderFactory.createEmptyBorder(12, 12, 12, 12));

        add(tableSection("Waiting queue", queueModel));
        add(tableSection("Restaurant tables", tablesModel));
    }

    /**
     * Call on Swing's Event Dispatch Thread.
     * The controller must not mutate the restaurant during this refresh.
     */
    public void refresh(Restaurant restaurant) {
        queueModel.setRowCount(0);

        for (Customer customer :
                restaurant.getWaitingQueue().getCustomers()) {
            queueModel.addRow(new Object[] {
                "C" + customer.getId(),
                customer.getTypeName(),
                customer.getSatisfaction()
            });
        }

        tablesModel.setRowCount(0);

        for (Table table : restaurant.getTables()) {
            Customer customer = table.getCustomer();

            if (customer == null) {
                tablesModel.addRow(new Object[] {
                    table.getNumber(), "Empty", "-", "-", "-"
                });
            } else {
                tablesModel.addRow(new Object[] {
                    table.getNumber(),
                    "C" + customer.getId(),
                    customer.getTypeName(),
                    customer.getSatisfaction(),
                    customer.getStatus()
                });
            }
        }
    }

    private static DefaultTableModel readOnlyModel(String[] columns) {
        return new DefaultTableModel(columns, 0) {
            private static final long serialVersionUID = 1L;

            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    private static JPanel tableSection(
            String title, DefaultTableModel model) {
        JTable table = new JTable(model);
        table.setFillsViewportHeight(true);
        table.setRowHeight(26);
        table.getTableHeader().setReorderingAllowed(false);
        table.getAccessibleContext().setAccessibleName(title);

        JPanel section = new JPanel(new BorderLayout());
        section.setBorder(BorderFactory.createTitledBorder(title));
        section.add(new JScrollPane(table), BorderLayout.CENTER);
        return section;
    }
}