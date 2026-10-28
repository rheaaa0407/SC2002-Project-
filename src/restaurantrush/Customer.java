package restaurantrush;

import java.util.Objects;

/** Shared customer lifecycle; subclasses supply their own behaviour. */
public abstract class Customer {
    private final int id;
    private int satisfaction;
    private CustomerStatus status;
    private Table table;
    private Order order;

    protected Customer(int id) {
        if (id <= 0) throw new IllegalArgumentException("Customer ID must be positive");
        this.id = id;
        this.satisfaction = 100;
        this.status = CustomerStatus.WAITING;
    }

    public int getId() { return id; }
    public int getSatisfaction() { return satisfaction; }
    public CustomerStatus getStatus() { return status; }
    public Table getTable() { return table; }
    public Order getOrder() { return order; }

    /** Only unserved waiting/seated/ordered customers lose satisfaction. */
    public void loseSatisfaction() {
        if (status == CustomerStatus.WAITING || status == CustomerStatus.SEATED || status == CustomerStatus.ORDERED) {
            satisfaction = Math.max(0, satisfaction - getWaitingLoss());
        }
    }

    /** Satisfaction points lost each unserved turn. */
    public abstract int getWaitingLoss();

    /** Price in cents after this customer's own discount. */
    public abstract int calculateBill(int priceCents);

    /** Customer chooses food; manager does not choose it. */
    public MenuItem chooseItem(Menu menu) {
        Objects.requireNonNull(menu, "menu");
        return new FoodChoiceRule().choose(id, menu);
    }

    /** Whether this customer may be offered a combo. Customers with a fixed dish say no. */
    public boolean acceptsCombos() { return true; }

    public abstract String getTypeName();

    public boolean hasReachedZero() { return satisfaction == 0; }

    public void seatAt(Table table) {
        requireStatus(CustomerStatus.WAITING);
        Objects.requireNonNull(table, "table");
        if (table.getCustomer() != this) {
            throw new IllegalStateException("Table must be assigned to this customer first");
        }
        this.table = table;
        this.status = CustomerStatus.SEATED;
    }

    public void placeOrder(Order order) {
        requireStatus(CustomerStatus.SEATED);
        Objects.requireNonNull(order, "order");
        if (order.getCustomer() != this) throw new IllegalArgumentException("Order belongs to another customer");
        this.order = order;
        this.status = CustomerStatus.ORDERED;
    }

    public void markServed() {
        requireStatus(CustomerStatus.ORDERED);
        this.status = CustomerStatus.SERVED;
    }

    public void startEating() {
        requireStatus(CustomerStatus.SERVED);
        this.status = CustomerStatus.EATING;
    }

    public void finishEating() {
        requireStatus(CustomerStatus.EATING);
        this.status = CustomerStatus.READY_TO_PAY;
    }

    public void markPaid() {
        requireStatus(CustomerStatus.READY_TO_PAY);
        this.status = CustomerStatus.PAID;
        this.table = null;
    }

    public void leave() {
        if (status != CustomerStatus.WAITING && status != CustomerStatus.SEATED && status != CustomerStatus.ORDERED) {
            throw new IllegalStateException("Only unserved customers can abandon the restaurant");
        }
        this.status = CustomerStatus.LEFT;
        this.table = null;
    }

    private void requireStatus(CustomerStatus expected) {
        if (status != expected) throw new IllegalStateException("Expected " + expected + " but was " + status);
    }

    @Override public String toString() {
        return "Customer " + id + " (" + status + ", satisfaction=" + satisfaction + ")";
    }
}
