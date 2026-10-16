package restaurantrush;

import java.util.ArrayList;
import java.util.List;

/**
 * Runs the game: every turn goes through the six steps of brief section 3.2,
 * then the result is checked after the last turn.
 * All game state lives in Restaurant; the player is reached only through ConsoleUI.
 */
public class TurnController {
    private final Restaurant restaurant;
    private final ArrivalSchedule arrivalSchedule;
    private final Waiter waiter;
    private final Chef chef;
    private final Cashier cashier;
    private final ResultEvaluator resultEvaluator;
    private final ConsoleUI ui;
    private final int maxTurns;
    private int currentTurn;

    public TurnController(Restaurant restaurant, ArrivalSchedule arrivalSchedule,
                          Waiter waiter, Chef chef, Cashier cashier,
                          ResultEvaluator resultEvaluator, ConsoleUI ui, int maxTurns) {
        this.restaurant = restaurant;
        this.arrivalSchedule = arrivalSchedule;
        this.waiter = waiter;
        this.chef = chef;
        this.cashier = cashier;
        this.resultEvaluator = resultEvaluator;
        this.ui = ui;
        this.maxTurns = maxTurns;
    }

    /** Plays all turns (no early ending), then shows Victory or Defeat. */
    public void runGame() {
        for (currentTurn = 1; currentTurn <= maxTurns; currentTurn++) {
            playTurn();
        }
        ui.showFinalResult(resultEvaluator.isVictory(restaurant), restaurant);
    }

    private void playTurn() {
        handleArrivals();                                      // step 1
        ui.showTurnStart(currentTurn, maxTurns, restaurant);
        List<Customer> eatingAtTurnStart = customersEating();  // served on an earlier turn
        runWaiterTask();                                       // steps 2 + 3: Waiter acts before Chef
        runChefTask();                                         // so the Chef can pick the order just taken
        handleEatingAndPayment(eatingAtTurnStart);             // step 4
        handleWaitingAndAbandonment();                         // step 5
        ui.showTurnEnd(currentTurn, restaurant);
    }

    /** Step 1: the scheduled customer (if any) joins the queue with satisfaction 100. */
    private void handleArrivals() {
        // TODO: Customer arrival = arrivalSchedule.getArrival(currentTurn);
        //       if not null: restaurant.addArrival(arrival); ui.showArrival(arrival)
    }

    /** Customers whose status is EATING right now, i.e. served on an earlier turn. */
    private List<Customer> customersEating() {
        // TODO: filter restaurant.getCustomersInRestaurant() by status EATING
        return new ArrayList<>();
    }

    /**
     * Steps 2-3 for the Waiter: ask until the choice is valid.
     * An invalid choice shows its reason and does not use up the task.
     * Serving only works on orders already READY, and the Chef has not
     * acted yet, so a dish finished this turn cannot be served this turn.
     */
    private void runWaiterTask() {
        // TODO: ActionResult result;
        //       do {
        //           WaiterChoice choice = ui.askWaiterChoice(restaurant);
        //           result = waiter.perform(choice, restaurant);
        //           ui.showActionResult(result);
        //       } while (!result.isSuccess());
    }

    /** Steps 2-3 for the Chef: advance one order by one preparation unit, or Wait. */
    private void runChefTask() {
        // TODO: same re-ask loop as runWaiterTask, with
        //       Order order = ui.askChefChoice(restaurant);   // null means Wait
        //       result = chef.prepare(order);
    }

    /**
     * Step 4. The Cashier goes first so that a customer who becomes
     * READY_TO_PAY this turn cannot pay until next turn.
     */
    private void handleEatingAndPayment(List<Customer> eatingAtTurnStart) {
        // TODO: ui.showActionResult(cashier.processPayment(restaurant));
        //       for each customer in eatingAtTurnStart:
        //           customer.finishEating();                 // -> READY_TO_PAY
        //           restaurant.addToPaymentQueue(customer);
    }

    /**
     * Step 5. Everyone waiting or seated who has not been served loses
     * satisfaction; anyone who hits 0 leaves without paying.
     * Served, eating and ready-to-pay customers lose nothing.
     */
    private void handleWaitingAndAbandonment() {
        // TODO: for each customer in a COPY of restaurant.getCustomersInRestaurant():
        //           if status is WAITING, SEATED or ORDERED:
        //               customer.loseSatisfaction();
        //               if (customer.hasReachedZero()) {
        //                   restaurant.recordDeparture(customer);
        //                   ui.showDeparture(customer);
        //               }
    }

    public int getCurrentTurn() {
        return currentTurn;
    }
}
