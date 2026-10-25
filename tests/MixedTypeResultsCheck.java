import restaurantrush.*;

/**
 * Stage 2 check (Member D): revenue and average satisfaction stay correct when
 * Regular, VIP and Critic customers are mixed. Uses the real Restaurant and
 * Cashier; each customer's loss rate, bill and dish come from the customer object.
 *
 * Run from the repository root:
 *   javac -d out src/restaurantrush/*.java tests/MixedTypeResultsCheck.java
 *   java -cp out MixedTypeResultsCheck
 */
public class MixedTypeResultsCheck {
    private static int failures = 0;

    public static void main(String[] args) {
        Menu menu = new Menu();
        Restaurant restaurant = new Restaurant(3);
        Cashier cashier = new Cashier();

        // Each customer waits 2 turns before being seated, then is served.
        Customer regular = new RegularCustomer(2);   // arrival 2 -> Burger, loses 8 per turn
        Customer vip = new VIPCustomer(5);           // arrival 5 -> Burger, loses 5 per turn
        Customer critic = new CriticCustomer(1);     // always Pasta, loses 12 per turn
        // Critic is queued to pay first, so oldest-first order is also checked.
        bringToReadyToPay(restaurant, menu, critic, 2);
        bringToReadyToPay(restaurant, menu, regular, 2);
        bringToReadyToPay(restaurant, menu, vip, 2);

        check("Regular lost 8 per waiting turn", regular.getSatisfaction() == 84);
        check("VIP lost 5 per waiting turn", vip.getSatisfaction() == 90);
        check("Critic lost 12 per waiting turn", critic.getSatisfaction() == 76);
        check("Regular pays full price for Burger ($15.00)", billOf(restaurant, regular) == 1500);
        check("VIP pays 10% off Burger ($13.50)", billOf(restaurant, vip) == 1350);
        check("Critic pays full price for Pasta ($18.00)", billOf(restaurant, critic) == 1800);

        cashier.processPayment(restaurant);
        check("Cashier charges the oldest READY_TO_PAY first (Critic)",
                critic.getStatus() == CustomerStatus.PAID && regular.getStatus() == CustomerStatus.READY_TO_PAY);
        check("Only one payment per call", restaurant.getPaidCount() == 1);
        cashier.processPayment(restaurant);
        cashier.processPayment(restaurant);

        check("Revenue = 18.00 + 15.00 + 13.50 = $46.50", restaurant.getRevenueCents() == 4650);
        check("Paid count is 3", restaurant.getPaidCount() == 3);
        double average = restaurant.getAverageSatisfaction().orElse(-1);
        check("Average satisfaction = (76 + 84 + 90) / 3 = 83.3", Math.abs(average - 250.0 / 3) < 1e-9);

        // A VIP who abandons adds nothing to revenue and is not in the average.
        Customer leaver = new VIPCustomer(6);
        restaurant.addArrival(leaver);
        while (!leaver.hasReachedZero()) leaver.loseSatisfaction();
        restaurant.recordDeparture(leaver);
        check("Abandoned customer adds no revenue", restaurant.getRevenueCents() == 4650);
        check("Abandoned customer is not averaged", restaurant.getPaidCount() == 3
                && Math.abs(restaurant.getAverageSatisfaction().getAsDouble() - 250.0 / 3) < 1e-9);
        check("Unhappy departures is 1", restaurant.getUnhappyDepartures() == 1);

        System.out.println(failures == 0 ? "All mixed-type checks passed." : failures + " check(s) FAILED.");
        if (failures > 0) System.exit(1);
    }

    /** Arrive, wait, get seated, order, cook, serve, eat, and join the payment queue. */
    private static void bringToReadyToPay(Restaurant restaurant, Menu menu, Customer customer, int waitingTurns) {
        restaurant.addArrival(customer);
        for (int i = 0; i < waitingTurns; i++) customer.loseSatisfaction();
        restaurant.getWaitingQueue().remove(customer);
        restaurant.findFreeTable().seat(customer);
        MenuItem item = customer.chooseItem(menu);
        Order order = new Order(customer, item, customer.calculateBill(item.getPriceCents()));
        customer.placeOrder(order);
        restaurant.addOrder(order);
        while (order.canAdvance()) order.advance();
        order.markServed();
        customer.markServed();
        customer.startEating();
        customer.finishEating();
        restaurant.addToPaymentQueue(customer);
    }

    private static int billOf(Restaurant restaurant, Customer customer) {
        return restaurant.findOrderFor(customer).getBillCents();
    }

    private static void check(String name, boolean passed) {
        System.out.println((passed ? "  PASS  " : "  FAIL  ") + name);
        if (!passed) failures++;
    }
}
