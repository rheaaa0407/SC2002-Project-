package restaurantrush;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.Objects;

/** First-in, first-out queue of customers waiting for tables. */
public class WaitingQueue {
    private final Deque<Customer> customers = new ArrayDeque<>();

    public void add(Customer customer) {
        Objects.requireNonNull(customer, "customer");
        if (customer.getStatus() != CustomerStatus.WAITING) {
            throw new IllegalStateException("Only WAITING customers may join the queue");
        }
        if (customers.contains(customer)) throw new IllegalArgumentException("Customer already queued");
        customers.addLast(customer);
    }

    public Customer peek() { return customers.peekFirst(); }
    public Customer poll() { return customers.pollFirst(); }
    public boolean remove(Customer customer) { return customers.remove(customer); }
    public boolean isEmpty() { return customers.isEmpty(); }
    public int size() { return customers.size(); }
    public List<Customer> getCustomers() {
        return Collections.unmodifiableList(new ArrayList<>(customers));
    }
    public Customer findById(int id) {
        for (Customer customer : customers) {
            if (customer.getId() == id) return customer;
        }
        return null;
    }
}
