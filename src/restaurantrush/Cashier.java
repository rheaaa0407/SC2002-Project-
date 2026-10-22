package restaurantrush;

/**
 * Handles at most one payment per turn, automatically (no manager choice).
 */
public class Cashier {

    /**
     * Charges the oldest READY_TO_PAY customer, if any.
     * TurnController calls this BEFORE moving this turn's eaters to
     * READY_TO_PAY, so a customer who became READY_TO_PAY this turn
     * is never charged until next turn.
     *
     * @return what happened, for the UI to display
     */
    public ActionResult processPayment(Restaurant restaurant) {
        Customer payer = restaurant.pollNextPayer();
        if (payer == null) {
            return ActionResult.success("Cashier: no one is ready to pay.");
        }
        Order order = restaurant.findOrderFor(payer);
        restaurant.recordPayment(payer, order);
        return ActionResult.success("Cashier collected " + Money.format(order.getBillCents())
                + " from " + payer.getTypeName() + " C" + payer.getId() + ".");
    }
}
