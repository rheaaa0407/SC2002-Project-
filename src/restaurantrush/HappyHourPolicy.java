package restaurantrush;

/**
 * Owns the once-per-game Happy Hour.
 * The controller starts each turn before any staff actions.
 */
public final class HappyHourPolicy {
    private static final int DURATION = 2;
    private static final int DISCOUNT_PERCENT = 20;

    private int currentTurn;
    private int activationTurn;
    private boolean used;

    /**
     * Starts a new turn and optionally activates Happy Hour.
     *
     * Activation is accepted only through this turn-start method.
     * A failed request leaves the policy unchanged.
     * Call once per turn, before recovery and staff actions.
     */
    public ActionResult beginTurn(int turn, boolean activate) {
        if (turn < 1 || turn <= currentTurn) {
            return ActionResult.failure(
                    "Start a positive turn after the previous turn.");
        }

        if (activate && used) {
            return ActionResult.failure(
                    "Happy Hour has already been used this game.");
        }

        currentTurn = turn;

        if (activate) {
            used = true;
            activationTurn = turn;
            return ActionResult.success(
                    "Happy Hour activated for this turn and the next.");
        }

        return ActionResult.success(
                isActive() ? "Happy Hour is active."
                           : "Happy Hour is off.");
    }

    /** Whether activation is still available for a future turn start. */
    public boolean canActivate() {
        return !used;
    }

    public boolean hasBeenUsed() {
        return used;
    }

    public boolean isActive() {
        return getRemainingTurns() > 0;
    }

    /**
     * Includes the current turn: 2 on activation, then 1, then 0.
     * Repeated calls do not consume duration.
     */
    public int getRemainingTurns() {
        if (!used) {
            return 0;
        }

        return Math.max(
                0, DURATION - (currentTurn - activationTurn));
    }

    /**
     * Applies Happy Hour to an already customer-discounted price.
     * Uses whole cents and Money's nearest-cent rounding.
     */
    public int calculateBill(int customerBillCents) {
        if (customerBillCents < 0) {
            throw new IllegalArgumentException(
                    "Customer bill cannot be negative.");
        }

        return isActive()
                ? Money.percentOff(customerBillCents, DISCOUNT_PERCENT)
                : customerBillCents;
    }
}