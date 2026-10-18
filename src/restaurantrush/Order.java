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
        status = (remainingUnits == 0) ? OrderStatus.READY : OrderStatus.IN_PROGRESS;
    }

    public void markReady() { /* TODO */ }
    public void markServed() { /* TODO */ }
    public void markPaid() { /* TODO */ }
    public void cancel() { /* TODO */ }
}
