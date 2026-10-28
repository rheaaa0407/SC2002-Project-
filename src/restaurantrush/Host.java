package restaurantrush;

import java.util.Objects;

/** Stage 3 staff member: can seat one waiting customer in addition to Waiter. */
public final class Host {
    private boolean turnStarted;
    private boolean taskUsed;
    private int currentTurn;
    private Restaurant turnRestaurant;

    /** Start the Host's task allowance for a new turn. */
    public void beginTurn(int turn, Restaurant restaurant) {
        Objects.requireNonNull(restaurant, "restaurant");
        if (turn < 1 || (turnStarted && turn < currentTurn))
            throw new IllegalArgumentException("Turn must be positive and cannot go backwards.");
        if (turnStarted && turn == currentTurn) {
            if (turnRestaurant != restaurant)
                throw new IllegalArgumentException("Restaurant changed during this turn.");
            return; // repeated call cannot reset the task allowance
        }
        turnStarted = true;
        currentTurn = turn;
        turnRestaurant = restaurant;
        taskUsed = false;
    }

    public boolean hasUsedTask() { return taskUsed; }

    /** Seat the selected customer; invalid attempts do not consume the task. */
    public ActionResult seat(int customerId, Restaurant restaurant) {
        if (!turnStarted) return ActionResult.failure("Host turn has not started.");
        if (restaurant != turnRestaurant)
            return ActionResult.failure("Use this turn's restaurant.");
        if (taskUsed) return ActionResult.failure("Host has already used this turn's task.");
        Customer customer = restaurant.getWaitingQueue().findById(customerId);
        if (customer == null) return ActionResult.failure("Customer is not in the waiting queue.");
        if (customer.getStatus() != CustomerStatus.WAITING || customer.getTable() != null)
            return ActionResult.failure("Customer is not available for seating.");
        if (customer.hasReachedZero())
            return ActionResult.failure("Customer has no satisfaction remaining.");
        Table free = restaurant.findFreeTable();
        if (free == null) return ActionResult.failure("No free table.");
        free.seat(customer);
        restaurant.getWaitingQueue().remove(customer);
        taskUsed = true;
        return ActionResult.success("Host seated Customer " + customerId
                + " at Table " + free.getNumber() + ".");
    }

    /** Choosing to wait is a valid Host task and consumes the turn's allowance. */
    public ActionResult waitTurn(Restaurant restaurant) {
        if (!turnStarted) return ActionResult.failure("Host turn has not started.");
        if (restaurant != turnRestaurant) return ActionResult.failure("Use this turn's restaurant.");
        if (taskUsed) return ActionResult.failure("Host has already used this turn's task.");
        taskUsed = true;
        return ActionResult.success("Host waits.");
    }
}
