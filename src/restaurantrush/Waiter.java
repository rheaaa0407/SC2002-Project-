package restaurantrush;

import java.util.Collections;
import java.util.IdentityHashMap;
import java.util.Objects;
import java.util.Set;

/** Executes one valid assignment per turn, before the Chef acts. No console IO. */
public class Waiter {
    private final Menu menu;
    private final FoodChoiceRule foodChoiceRule;
    private final Set<Order> readyAtTurnStart =
            Collections.newSetFromMap(new IdentityHashMap<Order, Boolean>());
    private boolean turnStarted;
    private boolean taskUsed;
    private int currentTurn;
    private Restaurant turnRestaurant;

    public Waiter() { this(new Menu(), new FoodChoiceRule()); }
    public Waiter(Menu menu, FoodChoiceRule foodChoiceRule) {
        this.menu = Objects.requireNonNull(menu, "menu");
        this.foodChoiceRule = Objects.requireNonNull(foodChoiceRule, "foodChoiceRule");
    }

    /** Call once after arrivals, before either staff member acts. Same-turn calls do not reset. */
    public void beginTurn(int turn, Restaurant restaurant) {
        Objects.requireNonNull(restaurant, "restaurant");
        if (turn < 1 || (turnStarted && turn < currentTurn))
            throw new IllegalArgumentException("Turn numbers must be positive and cannot go backwards.");
        if (turnStarted && turn == currentTurn) {
            if (restaurant != turnRestaurant) throw new IllegalArgumentException("Restaurant changed within a turn.");
            return;
        }
        currentTurn = turn;
        turnStarted = true;
        turnRestaurant = restaurant;
        taskUsed = false;
        readyAtTurnStart.clear();
        for (Order order : restaurant.getOrders())
            if (order.getStatus() == OrderStatus.READY) readyAtTurnStart.add(order);
    }

    public boolean hasUsedTask() { return taskUsed; }

    public ActionResult perform(WaiterChoice choice, Restaurant restaurant) {
        if (!turnStarted) return ActionResult.failure("Waiter turn has not been started.");
        if (restaurant != turnRestaurant) return ActionResult.failure("Use this turn's restaurant.");
        if (taskUsed) return ActionResult.failure("Waiter has already completed this turn's task.");
        if (choice == null) return ActionResult.failure("Choose a Waiter task.");
        if (choice.getTask() == WaiterChoice.Task.WAIT) {
            taskUsed = true;
            return ActionResult.success("Waiter waits.");
        }
        ActionResult result;
        switch (choice.getTask()) {
            case SEAT: result = seat(choice.getCustomerId(), restaurant); break;
            case TAKE_ORDER: result = takeOrder(choice.getCustomerId(), restaurant); break;
            case SERVE: result = serve(choice.getCustomerId(), restaurant); break;
            default: return ActionResult.failure("Unknown Waiter task.");
        }
        if (result.isSuccess()) taskUsed = true;
        return result;
    }

    private ActionResult seat(int id, Restaurant restaurant) {
        Customer customer = restaurant.getWaitingQueue().findById(id);
        if (customer == null) return ActionResult.failure("Customer is not in the waiting queue.");
        if (customer.getStatus() != CustomerStatus.WAITING || customer.getTable() != null)
            return ActionResult.failure("Customer is not available for seating.");
        if (customer.hasReachedZero()) return ActionResult.failure("Customer has no satisfaction remaining.");
        Table free = restaurant.findFreeTable();
        if (free == null) return ActionResult.failure("No free table.");
        free.seat(customer);
        restaurant.getWaitingQueue().remove(customer);
        return ActionResult.success("Waiter seated Customer " + id + " at Table " + free.getNumber() + ".");
    }

    private Customer seatedCustomer(int id, Restaurant restaurant) {
        for (Table table : restaurant.getTables()) {
            Customer customer = table.getCustomer();
            if (customer != null && customer.getId() == id) return customer;
        }
        return null;
    }

    private ActionResult takeOrder(int id, Restaurant restaurant) {
        Customer customer = seatedCustomer(id, restaurant);
        if (customer == null) return ActionResult.failure("Customer is not seated.");
        if (customer.getStatus() != CustomerStatus.SEATED || customer.getOrder() != null)
            return ActionResult.failure("Customer has already ordered or cannot order now.");
        if (customer.hasReachedZero()) return ActionResult.failure("Customer has no satisfaction remaining.");
        // Customer IDs represent the 1-based arrival sequence. Member B owns this rule.
        MenuItem item = foodChoiceRule.choose(customer.getId(), menu);
        if (item == null || item.getPrepUnits() < 1 || item.getPriceCents() < 0)
            return ActionResult.failure("The food-choice rule returned an invalid menu item.");
        Order order = new Order(customer, item, item.getPriceCents());
        customer.placeOrder(order);
        restaurant.addOrder(order);
        return ActionResult.success("Customer " + id + " ordered " + item.getName()
                + " for " + Money.format(order.getBillCents()) + ".");
    }

    private ActionResult serve(int id, Restaurant restaurant) {
        Customer customer = seatedCustomer(id, restaurant);
        if (customer == null || customer.getStatus() != CustomerStatus.ORDERED)
            return ActionResult.failure("Customer is not waiting for an ordered dish.");
        Order order = customer.getOrder();
        if (order == null || !restaurant.getOrders().contains(order)
                || order.getStatus() != OrderStatus.READY)
            return ActionResult.failure("Customer's order is not READY.");
        if (!readyAtTurnStart.contains(order))
            return ActionResult.failure("Food completed this turn can only be served next turn.");
        order.markServed();
        customer.markServed();
        // The existing controller tracks EATING customers at the following turn's start.
        customer.startEating();
        return ActionResult.success("Waiter served " + order.getItem().getName() + " to Customer " + id + ".");
    }
}
