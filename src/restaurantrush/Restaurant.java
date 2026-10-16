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
        // TODO: return the first table where table.isFree()
        return null;
    }

    /** @return the table this customer sits at, or null if not seated. */
    public Table findTableOf(Customer customer) {
        // TODO: return the table whose getCustomer() == customer
        return null;
    }

    // ---------- Waiting queue ----------

    public WaitingQueue getWaitingQueue() {
        return waitingQueue;
    }

    /** Step 1: a new customer joins the back of the waiting queue. */
    public void addArrival(Customer customer) {
        // TODO: waitingQueue.add(customer)
    }

    // ---------- Orders ----------

    public List<Order> getOrders() {
        return Collections.unmodifiableList(orders);
    }

    /** Called by the Waiter after taking an order. */
    public void addOrder(Order order) {
        // TODO: orders.add(order)
    }

    /** @return this customer's order, or null if they have not ordered. */
    public Order findOrderFor(Customer customer) {
        // TODO: return the order whose getCustomer() == customer
        return null;
    }

    // ---------- Payment ----------

    /** Step 4: a customer who finished eating waits for the Cashier. */
    public void addToPaymentQueue(Customer customer) {
        // TODO: paymentQueue.addLast(customer)
    }

    /** @return the oldest READY_TO_PAY customer (removed from the queue), or null if none. */
    public Customer pollNextPayer() {
        // TODO: return paymentQueue.pollFirst()
        return null;
    }

    /**
     * Step 4: the Cashier has charged this customer.
     * Adds the bill to revenue, records satisfaction, marks customer and
     * order PAID, and frees the table.
     */
    public void recordPayment(Customer customer, Order order) {
        // TODO: revenueCents += order.getBillCents();
        //       paidSatisfactions.add(customer.getSatisfaction());
        //       customer.markPaid(); order.markPaid();
        //       free the customer's table
    }

    // ---------- Abandonment ----------

    /**
     * Step 5: a customer reached 0 satisfaction and leaves without paying.
     * Cancels any unfinished order, frees the table or removes them from
     * the queue, and counts an unhappy departure.
     */
    public void recordDeparture(Customer customer) {
        // TODO: cancel findOrderFor(customer) if it exists;
        //       free their table OR waitingQueue.remove(customer);
        //       customer.leave(); unhappyDepartures++
    }

    // ---------- Who is in the restaurant ----------

    /** @return everyone waiting in the queue plus everyone seated at a table. */
    public List<Customer> getCustomersInRestaurant() {
        // TODO: waiting queue customers + table.getCustomer() of every occupied table
        return new ArrayList<>();
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
        // TODO: count orders whose status is SERVED or PAID
        return 0;
    }

    public int getUnhappyDepartures() {
        return unhappyDepartures;
    }
}
