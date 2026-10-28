package restaurantrush;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.IntConsumer;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

/** Displays choices; the controller and domain objects enforce game rules. */
public final class ActionPanel extends JPanel {
    private static final long serialVersionUID = 1L;

    private final JLabel prompt = new JLabel("Waiting for the game.");
    private final JComboBox<String> choices = new JComboBox<>();
    private final JButton confirm = new JButton("Confirm");
    private final JLabel happyHourStatus = new JLabel("Happy Hour: OFF");
    private final JButton activate = new JButton("Activate Happy Hour");
    private final JButton continueButton = new JButton("Continue");
    private final JLabel feedback = new JLabel(" ");

    private transient IntConsumer choiceHandler;
    private transient Consumer<Boolean> happyHourHandler;

    public ActionPanel() {
        super(new BorderLayout(8, 8));
        setBorder(BorderFactory.createTitledBorder("Manager actions"));

        JPanel staffRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        staffRow.add(prompt);
        staffRow.add(choices);
        staffRow.add(confirm);

        JPanel offerRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        offerRow.add(happyHourStatus);
        offerRow.add(activate);
        offerRow.add(continueButton);

        JPanel controls = new JPanel(new BorderLayout());
        controls.add(staffRow, BorderLayout.NORTH);
        controls.add(offerRow, BorderLayout.SOUTH);

        add(controls, BorderLayout.CENTER);
        add(feedback, BorderLayout.SOUTH);

        confirm.addActionListener(event -> {
            IntConsumer handler = choiceHandler;
            int index = choices.getSelectedIndex();
            if (handler != null && index >= 0) {
                disableInputs();
                handler.accept(index);
            }
        });

        activate.addActionListener(event -> submitHappyHour(true));
        continueButton.addActionListener(event -> submitHappyHour(false));
        disableInputs();
    }

    /** The adapter maps the selected index to a staff choice. */
    public void requestChoice(
            String message, String[] labels, IntConsumer handler) {
        Objects.requireNonNull(labels, "labels");
        Objects.requireNonNull(handler, "handler");
        if (labels.length == 0) {
            throw new IllegalArgumentException("Provide at least one choice.");
        }

        disableInputs();
        prompt.setText(message);
        choices.removeAllItems();
        for (String label : labels) {
            choices.addItem(label);
        }
        choiceHandler = handler;
        choices.setEnabled(true);
        confirm.setEnabled(true);
    }

    public void requestHappyHour(
            boolean canActivate, int remainingTurns,
            Consumer<Boolean> handler) {
        Objects.requireNonNull(handler, "handler");
        disableInputs();
        prompt.setText("Turn start: Happy Hour");
        setHappyHourStatus(remainingTurns);
        happyHourHandler = handler;
        activate.setEnabled(canActivate);
        continueButton.setEnabled(true);
    }

    public void setHappyHourStatus(int remainingTurns) {
        happyHourStatus.setText(remainingTurns > 0
                ? "Happy Hour: ON — " + remainingTurns + " turn(s) remaining"
                : "Happy Hour: OFF");
    }

    public void showFeedback(String message) {
        feedback.setText(message == null || message.isBlank() ? " " : message);
    }

    public void finishGame() {
        disableInputs();
        prompt.setText("Game over.");
    }

    private void submitHappyHour(boolean enabled) {
        Consumer<Boolean> handler = happyHourHandler;
        if (handler != null) {
            disableInputs();
            handler.accept(enabled);
        }
    }

    private void disableInputs() {
        choices.setEnabled(false);
        confirm.setEnabled(false);
        activate.setEnabled(false);
        continueButton.setEnabled(false);
        choiceHandler = null;
        happyHourHandler = null;
    }
}
