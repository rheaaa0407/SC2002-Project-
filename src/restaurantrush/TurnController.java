package restaurantrush;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the game: every turn goes through the six steps of brief section 3.2,
 * then the result is checked after the last turn.
 * All game state lives in Restaurant; the player is reached only through the GameView.
 *
 * Stage 3 adds two things at the start of each turn, before any staff act:
 * the manager may switch on Happy Hour (once per game), and while it is active
 * everyone still waiting for a table or food recovers satisfaction. The Host then
 * seats at most one queued customer, in addition to the Waiter's task.
 */
public class TurnController {
    /** Satisfaction recovered at turn start by customers waiting during Happy Hour (brief 4.3). */
    private static final int HAPPY_HOUR_RECOVERY = 15;

    private final Restaurant restaurant;
    private final ArrivalSchedule arrivalSchedule;
    private final Host host;                         // null when the setting has no Host
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final HappyHourPolicy happyHourPolicy;   // null when Happy Hour is not offered
    private final ResultEvaluator resultEvaluator;
    private final GameView view;
    private final int maxTurns;
    private int currentTurn;

    /**
     * Stage 3 setting. The Waiter must have been built with the same
     * HappyHourPolicy instance so that orders get the Happy Hour price.
     */
    public TurnController(Restaurant restaurant, ArrivalSchedule arrivalSchedule,
                          Host host, Waiter waiter, Chef chef, Cashier cashier,
                          HappyHourPolicy happyHourPolicy, ResultEvaluator resultEvaluator,
                          GameView view, int maxTurns) {
        this.restaurant = restaurant;
        this.arrivalSchedule = arrivalSchedule;
        this.host = host;
        this.waiter = waiter;
        this.chef = chef;
        this.cashier = cashier;
        this.happyHourPolicy = happyHourPolicy;
        this.resultEvaluator = resultEvaluator;
        this.view = view;
        this.maxTurns = maxTurns;
    }

    /**
     * Stage 1 and 2 setting: no Host and Happy Hour is never offered, so the
     * earlier recorded runs replay unchanged (regression check).
     */
    public TurnController(Restaurant restaurant, ArrivalSchedule arrivalSchedule,
                          Waiter waiter, Chef chef, Cashier cashier,
                          ResultEvaluator resultEvaluator, GameView view, int maxTurns) {
        this(restaurant, arrivalSchedule, null, waiter, chef, cashier, null,
                resultEvaluator, view, maxTurns);
    }

    /** Plays all turns (no early ending), then shows Victory or Defeat. */
    public void runGame() {
        for (currentTurn = 1; currentTurn <= maxTurns; currentTurn++) {
            playTurn();
        }
        view.showFinalResult(resultEvaluator.isVictory(restaurant), restaurant);
    }

    private void playTurn() {
        handleArrivals();                                      // step 1
        waiter.beginTurn(currentTurn, restaurant);             // resets the task and snapshots READY orders
        chef.beginTurn(currentTurn);
        if (host != null) {
            host.beginTurn(currentTurn, restaurant);
        }
        if (happyHourPolicy != null) {
            startHappyHourTurn();                              // activation, countdown, recovery
        }
        view.showTurnStart(currentTurn, maxTurns, restaurant, happyHourPolicy);  // null: no Happy Hour line
        List<Customer> eatingAtTurnStart = customersEating();  // served on an earlier turn
        if (host != null) {
            runHostTask();                                     // step 2: Host seats before the Waiter acts
        }
        runWaiterTask();                                       // steps 2 + 3: Waiter acts before Chef
        runChefTask();                                         // so the Chef can pick the order just taken
        handleEatingAndPayment(eatingAtTurnStart);             // step 4
        handleWaitingAndAbandonment();                         // step 5
        view.showTurnEnd(currentTurn, restaurant);
    }

    /** Step 1: the scheduled customer (if any) joins the queue with satisfaction 100. */
    private void handleArrivals() {
        Customer arrival = arrivalSchedule.getArrival(currentTurn);
        if (arrival != null) {
            restaurant.addArrival(arrival);
            view.showArrival(arrival);
        }
    }

    /**
     * Turn start for Happy Hour, before the turn display so it shows the
     * up-to-date status and satisfaction. The view only asks while Happy Hour
     * can still be activated. The policy is told about every turn so its
     * countdown runs; while it is active, customers still waiting for a table
     * or food recover satisfaction (capped at 100 by Customer). Customers who
     * left are no longer in the restaurant, so they are not affected.
     */
    private void startHappyHourTurn() {
        boolean activate = view.askHappyHour(happyHourPolicy);
        ActionResult result = happyHourPolicy.beginTurn(currentTurn, activate);
        if (!result.isSuccess()) {
            throw new IllegalStateException(result.getMessage());   // only on a controller bug
        }
        if (activate) {
            view.showActionResult(result);
        }
        if (!happyHourPolicy.isActive()) {
            return;
        }
        List<String> recovered = new ArrayList<>();
        for (Customer customer : restaurant.getCustomersInRestaurant()) {
            if (isWaitingForFood(customer)) {
                customer.recoverSatisfaction(HAPPY_HOUR_RECOVERY);
                recovered.add(customer.getTypeName() + " C" + customer.getId());
            }
        }
        if (!recovered.isEmpty()) {
            view.showActionResult(ActionResult.success("Happy Hour: " + String.join(", ", recovered)
                    + " recover " + HAPPY_HOUR_RECOVERY + " satisfaction (max 100)."));
        }
    }

    /** Customers whose status is EATING right now, i.e. served on an earlier turn. */
    private List<Customer> customersEating() {
        List<Customer> eating = new ArrayList<>();
        for (Customer customer : restaurant.getCustomersInRestaurant()) {
            if (customer.getStatus() == CustomerStatus.EATING) {
                eating.add(customer);
            }
        }
        return eating;
    }

    /**
     * Step 2 for the Host: seat at most one queued customer, or Wait.
     * An invalid choice shows its reason and does not use up the Host's turn.
     * A customer the Host seats leaves the queue, so the Waiter cannot seat them again.
     */
    private void runHostTask() {
        ActionResult result;
        do {
            Integer customerId = view.askHostChoice(restaurant);   // null means Wait
            result = (customerId == null)
                    ? host.waitTurn(restaurant)
                    : host.seat(customerId, restaurant);
            view.showActionResult(result);
        } while (!result.isSuccess());
    }

    /**
     * Steps 2-3 for the Waiter: ask until the choice is valid.
     * An invalid choice shows its reason and does not use up the task.
     * Serving only works on orders already READY, and the Chef has not
     * acted yet, so a dish finished this turn cannot be served this turn.
     */
    private void runWaiterTask() {
        ActionResult result;
        do {
            WaiterChoice choice = view.askWaiterChoice(restaurant);
            result = waiter.perform(choice, restaurant);
            view.showActionResult(result);
        } while (!result.isSuccess());
    }

    /** Steps 2-3 for the Chef: advance one order by one preparation unit, or Wait. */
    private void runChefTask() {
        ActionResult result;
        do {
            Order order = view.askChefChoice(restaurant);    // null means Wait
            result = chef.prepare(order);
            view.showActionResult(result);
        } while (!result.isSuccess());
    }

    /**
     * Step 4. The Cashier goes first so that a customer who becomes
     * READY_TO_PAY this turn cannot pay until next turn.
     */
    private void handleEatingAndPayment(List<Customer> eatingAtTurnStart) {
        view.showActionResult(cashier.processPayment(restaurant));
        for (Customer customer : eatingAtTurnStart) {
            customer.finishEating();                   // EATING -> READY_TO_PAY
            restaurant.addToPaymentQueue(customer);
        }
    }

    /**
     * Step 5. Everyone waiting or seated who has not been served loses
     * satisfaction; anyone who hits 0 leaves without paying.
     * Served, eating and ready-to-pay customers lose nothing.
     */
    private void handleWaitingAndAbandonment() {
        // getCustomersInRestaurant() returns a new list, so departures below do not disturb the loop
        for (Customer customer : restaurant.getCustomersInRestaurant()) {
            if (isWaitingForFood(customer)) {
                customer.loseSatisfaction();
                if (customer.hasReachedZero()) {
                    restaurant.recordDeparture(customer);
                    view.showDeparture(customer);
                }
            }
        }
    }

    /** Still waiting for a table (WAITING) or for food (SEATED, ORDERED). */
    private boolean isWaitingForFood(Customer customer) {
        CustomerStatus status = customer.getStatus();
        return status == CustomerStatus.WAITING
                || status == CustomerStatus.SEATED
                || status == CustomerStatus.ORDERED;
    }

    public int getCurrentTurn() {
        return currentTurn;
    }
}
