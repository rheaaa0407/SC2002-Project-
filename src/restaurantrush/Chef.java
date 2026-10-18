package restaurantrush;

/** Advances at most one preparation unit per turn. No console IO. */
public class Chef {
    private boolean turnStarted;
    private boolean taskUsed;
    private int currentTurn;

    /** Call before staff act. Calling again for the same turn cannot grant extra work. */
    public void beginTurn(int turn) {
        if (turn < 1 || (turnStarted && turn < currentTurn))
            throw new IllegalArgumentException("Turn numbers must be positive and cannot go backwards.");
        if (turnStarted && turn == currentTurn) return;
        currentTurn = turn;
        turnStarted = true;
        taskUsed = false;
    }
    public boolean hasUsedTask() { return taskUsed; }

    /** Null is the manager's explicit Wait choice, matching TurnController's contract. */
    public ActionResult prepare(Order order) {
        if (!turnStarted) return ActionResult.failure("Chef turn has not been started.");
        if (taskUsed) return ActionResult.failure("Chef has already completed this turn's task.");
        if (order == null) {
            taskUsed = true;
            return ActionResult.success("Chef waits.");
        }
        if (order.getStatus() != OrderStatus.PLACED && order.getStatus() != OrderStatus.IN_PROGRESS)
            return ActionResult.failure("Chef can only prepare PLACED or IN_PROGRESS orders.");
        Customer customer = order.getCustomer();
        if (customer == null || customer.getStatus() != CustomerStatus.ORDERED
                || customer.getOrder() != order || customer.hasReachedZero())
            return ActionResult.failure("Order has no eligible customer waiting for food.");
        if (order.getRemainingUnits() <= 0)
            return ActionResult.failure("Order has no preparation units remaining.");
        order.advance();
        taskUsed = true;
        return ActionResult.success("Chef prepared one unit of " + order.getItem().getName()
                + "; " + order.getRemainingUnits() + " remaining (" + order.getStatus() + ").");
    }
}
