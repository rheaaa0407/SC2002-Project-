package restaurantrush;

public class Order {
    private final Customer customer;
    private final MenuItem item;
    private final int billCents;      // locked when the order is placed
    private int remainingUnits;
    private OrderStatus status;

    public Order(Customer customer, MenuItem item, int billCents) {
        this.customer = customer;
        this.item = item;
        this.billCents = billCents;
        this.remainingUnits = item.getPrepUnits();
        this.status = OrderStatus.PLACED;
    }

    public Customer getCustomer() { return customer; }
    public MenuItem getItem() { return item; }
    public int getBillCents() { return billCents; }
    public int getRemainingUnits() { return remainingUnits; }
    public OrderStatus getStatus() { return status; }

    public void advance() { /* TODO: remainingUnits--, IN_PROGRESS, READY when 0 */ }
    public void markReady() { /* TODO */ }
    public void markServed() { /* TODO */ }
    public void markPaid() { /* TODO */ }
    public void cancel() { /* TODO */ }
}
