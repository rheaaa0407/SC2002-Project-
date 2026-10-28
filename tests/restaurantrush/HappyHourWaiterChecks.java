package restaurantrush;

public final class HappyHourWaiterChecks {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    private static void checkOrder(
            Customer customer, String expectedDish,
            boolean activate, int expectedBill) {

        HappyHourPolicy policy = new HappyHourPolicy();
        Restaurant restaurant = new Restaurant(2);
        Waiter waiter = new Waiter(new Menu(), policy);
        int id = customer.getId();

        restaurant.addArrival(customer);

        check(policy.beginTurn(1, false).isSuccess(), "Start turn 1");
        waiter.beginTurn(1, restaurant);
        check(waiter.perform(WaiterChoice.seat(id), restaurant).isSuccess(),
                "Seat customer");

        check(policy.beginTurn(2, activate).isSuccess(), "Start order turn");
        waiter.beginTurn(2, restaurant);

        ActionResult result =
                waiter.perform(WaiterChoice.takeOrder(id), restaurant);

        check(result.isSuccess(), "Take order successfully");

        Order order = customer.getOrder();
        check(order != null, "Customer has an order");
        check(restaurant.getOrders().contains(order),
                "Restaurant has the order");
        check(order.getItem().getName().equals(expectedDish),
                "Customer chooses expected dish");
        check(order.getBillCents() == expectedBill,
                "Waiter locks correct final price");
        check(result.getMessage().contains(expectedDish),
                "Message displays chosen dish");
        check(result.getMessage().contains(Money.format(expectedBill)),
                "Message displays final price");

        while (order.canAdvance()) {
            order.advance();
        }

        check(policy.beginTurn(3, false).isSuccess(), "Start turn 3");
        waiter.beginTurn(3, restaurant);

        check(waiter.perform(WaiterChoice.serve(id), restaurant).isSuccess(),
                "Serve ready order");
        check(order.getBillCents() == expectedBill,
                "Serving preserves bill");

        check(policy.beginTurn(4, false).isSuccess(), "Start turn 4");
        check(!policy.isActive(), "Happy Hour is off before payment");

        customer.finishEating();
        restaurant.addToPaymentQueue(customer);
        new Cashier().processPayment(restaurant);

        check(order.getStatus() == OrderStatus.PAID, "Order is paid");
        check(order.getBillCents() == expectedBill,
                "Expiry and payment preserve bill");
        check(restaurant.getRevenueCents() == expectedBill,
                "Cashier collects locked price");
        check(restaurant.getPaidCount() == 1,
                "Payment counted once");
    }

    public static void main(String[] args) {
        // Happy Hour applies after each customer's own pricing.
        checkOrder(new RegularCustomer(1), "Salad", true, 720);
        checkOrder(new VIPCustomer(2), "Burger", true, 1080);
        checkOrder(new CriticCustomer(1), "Pasta", true, 1440);

        // The same shared-policy constructor preserves pricing when off.
        checkOrder(new RegularCustomer(1), "Salad", false, 900);
        checkOrder(new VIPCustomer(2), "Burger", false, 1350);
        checkOrder(new CriticCustomer(1), "Pasta", false, 1800);

        System.out.println(
                "PASS: " + checks + " Happy Hour Waiter checks.");
    }
}
