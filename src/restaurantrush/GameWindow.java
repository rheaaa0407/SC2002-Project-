package restaurantrush;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.util.Locale;
import java.util.OptionalDouble;
import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSplitPane;

/**
 * Main Swing window. Displays state; does not execute game rules.
 * Construct and update on Swing's Event Dispatch Thread.
 */
public final class GameWindow extends JFrame {
    private static final long serialVersionUID = 1L;

    private final transient GameConfig config;
    private final transient ResultEvaluator evaluator;
    private final FloorPanel floorPanel = new FloorPanel();
    private final OrdersPanel ordersPanel = new OrdersPanel();
    private final JLabel statusBar = new JLabel("Restaurant Rush");
    private final JLabel summaryBar = new JLabel("Ready to start.");
    private final JPanel actionArea = new JPanel(new BorderLayout());

    public GameWindow(GameConfig config, ResultEvaluator evaluator) {
        super("Restaurant Rush");
        this.config = java.util.Objects.requireNonNull(config, "config");
        this.evaluator =
                java.util.Objects.requireNonNull(evaluator, "evaluator");

        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setLayout(new BorderLayout(8, 8));

        statusBar.setBorder(
                BorderFactory.createEmptyBorder(12, 16, 4, 16));
        add(statusBar, BorderLayout.NORTH);

        JSplitPane mainSplit = new JSplitPane(
                JSplitPane.HORIZONTAL_SPLIT, floorPanel, ordersPanel);
        mainSplit.setResizeWeight(0.4);
        mainSplit.setContinuousLayout(true);
        add(mainSplit, BorderLayout.CENTER);

        actionArea.setBorder(
                BorderFactory.createEmptyBorder(0, 12, 8, 12));

        summaryBar.setBorder(
                BorderFactory.createEmptyBorder(4, 16, 12, 16));

        JPanel bottom = new JPanel(new BorderLayout());
        bottom.add(actionArea, BorderLayout.CENTER);
        bottom.add(summaryBar, BorderLayout.SOUTH);
        add(bottom, BorderLayout.SOUTH);

        setMinimumSize(new Dimension(900, 650));
        setSize(1200, 800);
        setLocationRelativeTo(null);
    }

    /** Installs the manager's controls without coupling to their implementation. */
    public void setActionPanel(JPanel panel) {
        actionArea.removeAll();
        actionArea.add(
                java.util.Objects.requireNonNull(panel, "panel"),
                BorderLayout.CENTER);
        actionArea.revalidate();
        actionArea.repaint();
    }

    /** Refresh only while the controller's game state is stable. */
    public void refresh(int turn, Restaurant restaurant) {
        floorPanel.refresh(restaurant);
        ordersPanel.refresh(restaurant);

        statusBar.setText(
                "Turn " + turn + "/" + config.getMaxTurns()
                + "    |    Revenue: "
                + Money.format(restaurant.getRevenueCents())
                + "    |    Paid: " + restaurant.getPaidCount()
                + "    |    Paid customer satisfaction: "
                + averageText(restaurant));
    }

    /** Call before logging arrivals or other events for this turn. */
    public void beginTurn(int turn, Restaurant restaurant) {
        ordersPanel.beginTurn(turn);
        summaryBar.setText("Turn " + turn + " in progress.");
        refresh(turn, restaurant);
    }

    public void appendEvent(String message) {
        ordersPanel.appendEvent(message);
    }

    public void showTurnEnd(int turn, Restaurant restaurant) {
        refresh(turn, restaurant);

        String summary =
                "End of turn " + turn
                + " | Served: " + restaurant.getServedCount()
                + " | Unhappy departures: "
                + restaurant.getUnhappyDepartures();

        summaryBar.setText(summary);
        appendEvent(summary);
    }

    /** The controller supplies the final outcome. */
    public void showGameOver(boolean victory, Restaurant restaurant) {
        refresh(config.getMaxTurns(), restaurant);

        String outcome = victory ? "VICTORY" : "DEFEAT";
        summaryBar.setText("GAME OVER — " + outcome);
        appendEvent("GAME OVER — " + outcome);

        appendEvent(targetLine(
                evaluator.meetsPaidTarget(restaurant),
                "Paid customers: " + restaurant.getPaidCount()
                + " (target " + evaluator.getTargetPaid() + ")"));

        appendEvent(targetLine(
                evaluator.meetsRevenueTarget(restaurant),
                "Revenue: " + Money.format(restaurant.getRevenueCents())
                + " (target "
                + Money.format(evaluator.getTargetRevenueCents()) + ")"));

        appendEvent(targetLine(
                evaluator.meetsSatisfactionTarget(restaurant),
                "Average satisfaction: " + averageText(restaurant)
                + " (target "
                + evaluator.getTargetAverageSatisfaction() + ")"));
    }

    private static String targetLine(boolean met, String text) {
        return (met ? "[OK] " : "[MISSED] ") + text;
    }

    private static String averageText(Restaurant restaurant) {
        OptionalDouble average = restaurant.getAverageSatisfaction();
        return average.isPresent()
                ? String.format(Locale.ROOT, "%.1f", average.getAsDouble())
                : "N/A";
    }
}