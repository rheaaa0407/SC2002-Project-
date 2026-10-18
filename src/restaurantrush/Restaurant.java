package restaurantrush;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.OptionalDouble;

/**
 * Holds the whole game state: tables, waiting queue, orders, the payment
 * queue, revenue and the counters shown in the end-of-turn results.
 * It keeps the state consistent; WHEN things happen is decided by TurnController.
 */
public class Restaurant {
    private final List<Table> tables;
    private final WaitingQueue waitingQueue;
    private final List<Order> orders;
    /** Customers who are READY_TO_PAY, oldest first. */
    private final Deque<Customer> paymentQueue;
    /** Satisfaction recorded at the moment each customer paid. */
    private final List<Integer> paidSatisfactions;
    private int revenueCents;
    private int unhappyDepartures;

    public Restaurant(int tableCount) {
        tables = new ArrayList<>();
        for (int number = 1; number <= tableCount; number++) {
            tables.add(new Table(number));
        }
        waitingQueue = new WaitingQueue();
        orders = new ArrayList<>();
        paymentQueue = new ArrayDeque<>();
        paidSatisfactions = new ArrayList<>();
    }

    // ---------- Tables ----------

    public List<Table> getTables() {
        return Collections.unmodifiableList(tables);
    }

    /** @return the first free table, or null if every table is occupied. */
    public Table findFreeTable() {
        for (Table table : tables) {
            if (table.isFree()) {
                return table;
            }
        }
        return null;
    }

    /** @return the table this customer sits at, or null if not seated. */
    public Table findTableOf(Customer customer) {
        for (Table table : tables) {
            if (table.getCustomer() == customer) {
                return table;
            }
        }
        return null;
    }

    // ---------- Waiting queue ----------

    public WaitingQueue getWaitingQueue() {
        return waitingQueue;
    }

    /** Step 1: a new customer joins the back of the waiting queue. */
    public void addArrival(Customer customer) {
        waitingQueue.add(customer);
    }

    // ---------- Orders ----------

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    /** Called by the Waiter after taking an order. */
    public void addOrder(Order order) {
        orders.add(order);
    }

    /** @return this customer's order, or null if they have not ordered. */
    public Order findOrderFor(Customer customer) {
        for (Order order : orders) {
            if (order.getCustomer() == customer) {
                return order;
            }
        }
        return null;
    }

    // ---------- Payment ----------

    /** Step 4: a customer who finished eating waits for the Cashier. */
    public void addToPaymentQueue(Customer customer) {
        paymentQueue.addLast(customer);
    }

    /** @return the oldest READY_TO_PAY customer (removed from the queue), or null if none. */
    public Customer pollNextPayer() {
        return paymentQueue.pollFirst();
    }

    /**
     * Step 4: the Cashier has charged this customer.
     * Adds the bill to revenue, records satisfaction, marks customer and
     * order PAID, and frees the table.
     */
    public void recordPayment(Customer customer, Order order) {
        revenueCents += order.getBillCents();
        paidSatisfactions.add(customer.getSatisfaction());
        customer.markPaid();
        order.markPaid();
        Table table = findTableOf(customer);
        if (table != null) {
            table.free();
        }
    }

    // ---------- Abandonment ----------

    /**
     * Step 5: a customer reached 0 satisfaction and leaves without paying.
     * Cancels their order (it cannot have been served, since served customers
     * lose no satisfaction), frees the table or removes them from the queue,
     * and counts an unhappy departure.
     */
    public void recordDeparture(Customer customer) {
        Order order = findOrderFor(customer);
        if (order != null) {
            order.cancel();
        }
        Table table = findTableOf(customer);
        if (table != null) {
            table.free();
        } else {
            waitingQueue.remove(customer);
        }
        customer.leave();
        unhappyDepartures++;
    }

    // ---------- Who is in the restaurant ----------

    /** @return everyone waiting in the queue plus everyone seated at a table. */
    public List<Customer> getCustomersInRestaurant() {
        List<Customer> customers = new ArrayList<>(waitingQueue.getCustomers());
        for (Table table : tables) {
            if (!table.isFree()) {
                customers.add(table.getCustomer());
            }
        }
        return customers;
    }

    /** @return the customer with this ID if they are queued or seated, otherwise null. */
    public Customer findCustomerById(int id) {
        for (Customer customer : getCustomersInRestaurant()) {
            if (customer.getId() == id) {
                return customer;
            }
        }
        return null;
    }

    // ---------- Staff tasks available (for the start-of-turn display) ----------

    /** @return seated customers who have not ordered yet (Waiter can take their order). */
    public List<Customer> getCustomersAwaitingOrder() {
        List<Customer> result = new ArrayList<>();
        for (Table table : tables) {
            Customer customer = table.getCustomer();
            if (customer != null && customer.getStatus() == CustomerStatus.SEATED) {
                result.add(customer);
            }
        }
        return result;
    }

    /** @return orders that are READY and waiting to be served (Waiter can serve them). */
    public List<Order> getReadyOrders() {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.READY) {
                result.add(order);
            }
        }
        return result;
    }

    /** @return orders the Chef can still advance (PLACED or IN_PROGRESS). */
    public List<Order> getOrdersInKitchen() {
        List<Order> result = new ArrayList<>();
        for (Order order : orders) {
            if (order.canAdvance()) {
                result.add(order);
            }
        }
        return result;
    }

    // ---------- Results (brief 3.5) ----------

    public int getRevenueCents() {
        return revenueCents;
    }

    public int getPaidCount() {
        return paidSatisfactions.size();
    }

    /** @return average satisfaction of paid customers, or empty if no one has paid (show "N/A"). */
    public OptionalDouble getAverageSatisfaction() {
        return paidSatisfactions.stream().mapToInt(Integer::intValue).average();
    }

    /** @return number of orders that have been served (status SERVED or PAID). */
    public int getServedCount() {
        int count = 0;
        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.SERVED || order.getStatus() == OrderStatus.PAID) {
                count++;
            }
        }
        return count;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }
}
