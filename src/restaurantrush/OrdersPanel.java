package restaurantrush;

import java.awt.BorderLayout;
import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.table.DefaultTableModel;

/** Displays orders and turn events without changing game state. */
public final class OrdersPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final DefaultTableModel ordersModel =
            new DefaultTableModel(
                    new String[] {
                        "Customer", "Type", "Dish / combo",
                        "Locked bill", "Prep left", "Status"
                    }, 0) {
                private static final long serialVersionUID = 1L;

                @Override
                public boolean isCellEditable(int row, int column) {
                    return false;
                }
            };

    private final JTextArea eventLog = new JTextArea(8, 40);

    public OrdersPanel() {
        super(new BorderLayout());
        setBorder(BorderFactory.createEmptyBorder(12, 0, 12, 12));

        JTable ordersTable = new JTable(ordersModel);
        ordersTable.setFillsViewportHeight(true);
        ordersTable.setRowHeight(26);
        ordersTable.getTableHeader().setReorderingAllowed(false);
        ordersTable.getAccessibleContext().setAccessibleName("Orders");

        JPanel ordersSection = new JPanel(new BorderLayout());
        ordersSection.setBorder(
                BorderFactory.createTitledBorder("Orders"));
        ordersSection.add(
                new JScrollPane(ordersTable), BorderLayout.CENTER);

        eventLog.setEditable(false);
        eventLog.setLineWrap(true);
        eventLog.setWrapStyleWord(true);
        eventLog.getAccessibleContext().setAccessibleName("Turn events");

        JPanel eventsSection = new JPanel(new BorderLayout());
        eventsSection.setBorder(
                BorderFactory.createTitledBorder("Turn events"));
        eventsSection.add(
                new JScrollPane(eventLog), BorderLayout.CENTER);

        JSplitPane split = new JSplitPane(
                JSplitPane.VERTICAL_SPLIT,
                ordersSection, eventsSection);
        split.setResizeWeight(0.65);
        split.setContinuousLayout(true);

        add(split, BorderLayout.CENTER);
    }

    /** Call on Swing's Event Dispatch Thread while game state is stable. */
    public void refresh(Restaurant restaurant) {
        ordersModel.setRowCount(0);

        for (Order order : restaurant.getOrders()) {
            Customer customer = order.getCustomer();

            ordersModel.addRow(new Object[] {
                "C" + customer.getId(),
                customer.getTypeName(),
                order.getItem().getName(),
                Money.format(order.getBillCents()),
                order.getRemainingUnits(),
                order.getStatus()
            });
        }
    }

    /** Start the next turn's log before displaying its arrival events. */
    public void beginTurn(int turn) {
        eventLog.setText("Turn " + turn + "\n");
    }

    public void appendEvent(String message) {
        if (message == null || message.isBlank()) {
            return;
        }

        eventLog.append(message + "\n");
        eventLog.setCaretPosition(eventLog.getDocument().getLength());
    }
}
