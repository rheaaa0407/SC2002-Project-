package restaurantrush;

public class Stage2Checks {
    private static int checks;

    private static void check(boolean condition, String message) {
        if (!condition) {
            throw new AssertionError(message);
        }
        checks++;
    }

    private static void checkWaitingLoss(Customer customer, int loss) {
        customer.loseSatisfaction();

        check(customer.getSatisfaction() == 100 - loss,
                "Incorrect waiting loss for " + customer.getTypeName());

        for (int i = 0; i < 25; i++) {
            customer.loseSatisfaction();
        }

        check(customer.getSatisfaction() == 0,
                "Satisfaction must stop at zero");
    }

    private static void checkOrder(Customer customer, String expectedDish,
                                   int expectedBill) {
        Restaurant restaurant = new Restaurant(2);
        Waiter waiter = new Waiter(new Menu());
        int id = customer.getId();

        restaurant.addArrival(customer);
        waiter.beginTurn(1, restaurant);

        check(waiter.perform(WaiterChoice.seat(id), restaurant).isSuccess(),
                "Customer should be seated");

        waiter.beginTurn(2, restaurant);
        ActionResult result =
                waiter.perform(WaiterChoice.takeOrder(id), restaurant);

        check(result.isSuccess(), "Order should succeed");

        Order order = customer.getOrder();

        check(order != null, "Order must be linked to customer");
        check(restaurant.getOrders().contains(order),
                "Order must be registered in restaurant");
        check(order.getItem().getName().equals(expectedDish),
                "Incorrect customer food choice");
        check(order.getBillCents() == expectedBill,
                "Incorrect locked bill");

        check(result.getMessage().contains(expectedDish),
                "Order message must show chosen dish");
        check(result.getMessage().contains(Money.format(expectedBill)),
                "Order message must show final price");

        // The locked bill must survive preparation, service and payment.
        while (order.canAdvance()) {
            order.advance();
        }

        waiter.beginTurn(3, restaurant);
        check(waiter.perform(WaiterChoice.serve(id), restaurant).isSuccess(),
                "Ready order should be served");

        int satisfaction = customer.getSatisfaction();
        customer.loseSatisfaction();

        check(customer.getSatisfaction() == satisfaction,
                "Served customers must not lose satisfaction");
        check(order.getBillCents() == expectedBill,
                "Serving must not change the locked bill");

        customer.finishEating();
        restaurant.addToPaymentQueue(customer);
        new Cashier().processPayment(restaurant);

        check(order.getStatus() == OrderStatus.PAID,
                "Order should be paid");
        check(restaurant.getRevenueCents() == expectedBill,
                "Cashier must collect the locked bill");
        check(restaurant.getPaidCount() == 1,
                "Payment must be counted once");
    }

    public static void main(String[] args) {
        checkWaitingLoss(new RegularCustomer(1), 8);
        checkWaitingLoss(new VIPCustomer(2), 5);
        checkWaitingLoss(new CriticCustomer(3), 12);

        check(new RegularCustomer(1).calculateBill(1500) == 1500,
                "Regular customer pays full price");
        check(new VIPCustomer(2).calculateBill(1500) == 1350,
                "VIP receives 10 percent off");
        check(new CriticCustomer(3).calculateBill(1500) == 1500,
                "Critic pays full price");

        check(Money.percentOff(905, 10) == 815,
                "814.5 cents must round to 815");
        check(new VIPCustomer(2).calculateBill(905) == 815,
                "VIP pricing must apply rounding");
        check(Money.percentOff(0, 10) == 0,
                "Zero price stays zero");
        check(Money.percentOff(1500, 0) == 1500,
                "Zero discount preserves price");
        check(Money.percentOff(1500, 100) == 0,
                "Full discount gives zero");

        // Regular/VIP retain the arrival-order food cycle.
        checkOrder(new RegularCustomer(1), "Salad", 900);
        checkOrder(new VIPCustomer(2), "Burger", 1350);

        // ID 1 would normally choose Salad: this proves Critic preference.
        checkOrder(new CriticCustomer(1), "Pasta", 1800);

        System.out.println("PASS: " + checks + " Stage 2 checks.");
    }
}