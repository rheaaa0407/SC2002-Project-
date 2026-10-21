package restaurantrush;

import java.util.Objects;

public class Order {
    private final Customer customer;
    private final MenuItem item;
    private final int billCents;      // locked when the order is placed
    private int remainingUnits;
    private OrderStatus status;

    public Order(Customer customer, MenuItem item, int billCents) {
        this.customer = Objects.requireNonNull(customer, "customer");
        this.item = Objects.requireNonNull(item, "item");
        if (billCents < 0) throw new IllegalArgumentException("Bill cannot be negative");
        this.billCents = billCents;
        this.remainingUnits = item.getPrepUnits();
        this.status = OrderStatus.PLACED;
    }

    public Customer getCustomer() { return customer; }
    public MenuItem getItem() { return item; }
    public int getBillCents() { return billCents; }
    public int getRemainingUnits() { return remainingUnits; }
    public OrderStatus getStatus() { return status; }

    /** True while the Chef can still work on this order (PLACED or IN_PROGRESS). */
    public boolean canAdvance() {
        return status == OrderStatus.PLACED || status == OrderStatus.IN_PROGRESS;
    }

    /** One prep unit done. The order becomes READY when no units are left. */
    public void advance() {
        if (!canAdvance()) throw new IllegalStateException("Cannot prepare an order that is " + status);
        remainingUnits--;
        if (remainingUnits == 0) {
            markReady();
        } else {
            status = OrderStatus.IN_PROGRESS;
        }
    }

    /** Only allowed once every prep unit is done. Called by advance() on the last unit. */
    public void markReady() {
        if (remainingUnits != 0 || !canAdvance()) {
            throw new IllegalStateException("Order is not fully prepared (status " + status + ")");
        }
        status = OrderStatus.READY;
    }

    public void markServed() {
        requireStatus(OrderStatus.READY);
        status = OrderStatus.SERVED;
    }

    public void markPaid() {
        requireStatus(OrderStatus.SERVED);
        status = OrderStatus.PAID;
    }

    /** Used when a customer abandons: only unserved orders can be cancelled. */
    public void cancel() {
        if (status != OrderStatus.PLACED && status != OrderStatus.IN_PROGRESS && status != OrderStatus.READY) {
            throw new IllegalStateException("Cannot cancel an order that is " + status);
        }
        status = OrderStatus.CANCELLED;
    }

    private void requireStatus(OrderStatus expected) {
        if (status != expected) throw new IllegalStateException("Expected " + expected + " but was " + status);
    }
}
