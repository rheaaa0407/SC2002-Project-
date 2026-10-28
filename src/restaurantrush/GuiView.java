package restaurantrush;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.function.IntConsumer;
import javax.swing.SwingUtilities;

/** Swing adapter. Displays state and returns choices without applying rules. */
public final class GuiView implements GameView {
    private final Restaurant restaurant;
    private final int maxTurns;
    private final List<String> pendingEvents = new ArrayList<>();

    private GameWindow window;
    private ActionPanel actions;
    private int currentTurn;
    private int preparedTurn;

    private volatile boolean closed;
    private volatile CompletableFuture<Integer> pendingChoice;

    /**
     * Construct from the game thread, not Swing's Event Dispatch Thread.
     */
    public GuiView(GameConfig config, ResultEvaluator evaluator,
                   Restaurant restaurant) {
        if (SwingUtilities.isEventDispatchThread()) {
            throw new IllegalStateException(
                    "Launch the controller outside Swing's UI thread.");
        }

        this.restaurant = java.util.Objects.requireNonNull(
                restaurant, "restaurant");
        this.maxTurns = config.getMaxTurns();

        onUi(() -> {
            window = new GameWindow(config, evaluator);
            actions = new ActionPanel();
            window.setActionPanel(actions);
            window.addWindowListener(new WindowAdapter() {
                @Override
                public void windowClosed(WindowEvent event) {
                    closed = true;
                    CompletableFuture<Integer> choice = pendingChoice;
                    if (choice != null) {
                        choice.cancel(false);
                    }
                }
            });
            window.refresh(0, restaurant);
            window.setVisible(true);
        });
    }

    @Override
    public void showWelcome(GameConfig config, Menu menu) {
        onUi(() -> {
            window.appendEvent("Welcome to Restaurant Rush.");
            window.appendEvent(config.toString());
            window.appendEvent("Menu:");
            for (MenuItem item : menu.getItems()) {
                window.appendEvent(
                        item.getName() + " — "
                        + Money.format(item.getPriceCents())
                        + ", " + item.getPrepUnits() + " prep unit(s)");
            }
            window.appendEvent(
                    "Customers choose their food. Select staff tasks below.");
        });

        awaitChoice(handler -> actions.requestChoice(
                "Ready to open?",
                new String[] {"Start game"}, handler));
    }

    @Override
    public void showArrival(Customer customer) {
        // Arrivals precede the controller's turn-start display.
        pendingEvents.add(
                customer.getTypeName() + " C" + customer.getId()
                + " arrived, satisfaction "
                + customer.getSatisfaction() + ".");
    }

    private void prepareTurn(int turn) {
        if (preparedTurn == turn) {
            return;
        }

        List<String> events = new ArrayList<>(pendingEvents);
        pendingEvents.clear();

        onUi(() -> {
            window.beginTurn(turn, restaurant);
            actions.showFeedback("");
            for (String event : events) {
                window.appendEvent(event);
            }
        });
        preparedTurn = turn;
    }

    @Override
    public boolean askHappyHour(HappyHourPolicy happyHour) {
        prepareTurn(currentTurn + 1);

        if (!happyHour.canActivate()) {
            return false;
        }

        int selected = awaitChoice(handler ->
                actions.requestHappyHour(
                        happyHour.canActivate(),
                        happyHour.getRemainingTurns(),
                        activate -> handler.accept(activate ? 1 : 0)));

        return selected == 1;
    }

    @Override
    public void showTurnStart(int turn, int maximum,
                              Restaurant state,
                              HappyHourPolicy happyHour) {
        prepareTurn(turn);
        currentTurn = turn;

        onUi(() -> {
            window.refresh(turn, state);
            actions.setHappyHourStatus(
                    happyHour == null ? 0 : happyHour.getRemainingTurns());
        });
    }

    @Override
    public Integer askHostChoice(Restaurant state) {
        List<Integer> ids = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        ids.add(null);
        labels.add("Wait");

        for (Customer customer : state.getWaitingQueue().getCustomers()) {
            ids.add(customer.getId());
            labels.add("Seat " + customerLabel(customer));
        }

        refresh(state);
        int selected = awaitChoice(handler -> actions.requestChoice(
                "Host:", labels.toArray(new String[0]), handler));
        return ids.get(selected);
    }

    @Override
    public WaiterChoice askWaiterChoice(Restaurant state) {
        List<WaiterChoice> options = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        options.add(WaiterChoice.waitTask());
        labels.add("Wait");

        for (Customer customer : state.getWaitingQueue().getCustomers()) {
            options.add(WaiterChoice.seat(customer.getId()));
            labels.add("Seat " + customerLabel(customer));
        }

        for (Table table : state.getTables()) {
            Customer customer = table.getCustomer();
            if (customer == null) {
                continue;
            }

            // The domain validates each attempted action.
            options.add(WaiterChoice.takeOrder(customer.getId()));
            labels.add("Take order: " + customerLabel(customer));

            options.add(WaiterChoice.serve(customer.getId()));
            labels.add("Serve: " + customerLabel(customer));
        }

        refresh(state);
        int selected = awaitChoice(handler -> actions.requestChoice(
                "Waiter:", labels.toArray(new String[0]), handler));
        return options.get(selected);
    }

    @Override
    public Order askChefChoice(Restaurant state) {
        List<Order> orders = new ArrayList<>();
        List<String> labels = new ArrayList<>();

        orders.add(null);
        labels.add("Wait");

        for (Order order : state.getOrdersInKitchen()) {
            orders.add(order);
            labels.add(
                    "Prepare C" + order.getCustomer().getId()
                    + ": " + order.getItem().getName()
                    + " (" + order.getRemainingUnits() + " unit(s) left)");
        }

        refresh(state);
        int selected = awaitChoice(handler -> actions.requestChoice(
                "Chef:", labels.toArray(new String[0]), handler));
        return orders.get(selected);
    }

    @Override
    public void showActionResult(ActionResult result) {
        onUi(() -> {
            String message = (result.isSuccess() ? "" : "Invalid: ")
                    + result.getMessage();
            actions.showFeedback(message);
            window.appendEvent(message);
            window.refresh(
                    Math.max(currentTurn, preparedTurn), restaurant);
        });
    }

    @Override
    public void showDeparture(Customer customer) {
        onUi(() -> window.appendEvent(
                customerLabel(customer) + " left without paying."));
    }

    @Override
    public void showTurnEnd(int turn, Restaurant state) {
        onUi(() -> window.showTurnEnd(turn, state));

        awaitChoice(handler -> actions.requestChoice(
                "Turn complete:",
                new String[] {
                    turn == maxTurns ? "Show final result" : "Next turn"
                }, handler));
    }

    @Override
    public void showFinalResult(boolean victory, Restaurant state) {
        onUi(() -> {
            actions.finishGame();
            window.showGameOver(victory, state);
        });
    }

    private void refresh(Restaurant state) {
        onUi(() -> window.refresh(currentTurn, state));
    }

    private static String customerLabel(Customer customer) {
        return customer.getTypeName() + " C" + customer.getId()
                + " [" + customer.getStatus() + "]";
    }

    /**
     * Waits on the game thread. Swing remains free to process button clicks.
     */
    private int awaitChoice(
            java.util.function.Consumer<IntConsumer> display) {
        CompletableFuture<Integer> future = new CompletableFuture<>();
        pendingChoice = future;

        try {
            onUi(() -> display.accept(value -> future.complete(value)));
            return future.get();
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Game interrupted.");
        } catch (ExecutionException exception) {
            throw new IllegalStateException(
                    "Could not read GUI choice.", exception.getCause());
        } finally {
            pendingChoice = null;
        }
    }

    /**
     * Synchronous updates keep the controller from mutating state while
     * Swing reads it to refresh the panels.
     */
    private void onUi(Runnable update) {
        if (closed) {
            throw new CancellationException("Window closed.");
        }

        Runnable guardedUpdate = () -> {
            if (closed) {
                throw new CancellationException("Window closed.");
            }
            update.run();
        };

        if (SwingUtilities.isEventDispatchThread()) {
            guardedUpdate.run();
            return;
        }

        try {
            SwingUtilities.invokeAndWait(guardedUpdate);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new CancellationException("Game interrupted.");
        } catch (InvocationTargetException exception) {
            Throwable cause = exception.getCause();
            if (cause instanceof CancellationException) {
                throw (CancellationException) cause;
            }
            throw new IllegalStateException(
                    "Could not update GUI.", cause);
        }
    }
}
