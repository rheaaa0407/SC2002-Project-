package restaurantrush;

import java.util.Objects;

/** A single restaurant table, occupied by at most one customer. */
public class Table {
    private final int number;
    private Customer customer;

    public Table(int number) {
        if (number <= 0) throw new IllegalArgumentException("Table number must be positive");
        this.number = number;
    }

    public int getNumber() { return number; }
    public boolean isFree() { return customer == null; }
    public Customer getCustomer() { return customer; }

    /** Seat one waiting customer. The Table and Customer are updated together. */
    public void seat(Customer customer) {
        Objects.requireNonNull(customer, "customer");
        if (!isFree()) throw new IllegalStateException("Table is occupied");
        if (customer.getStatus() != CustomerStatus.WAITING || customer.getTable() != null) {
            throw new IllegalStateException("Customer is not available for seating");
        }
        this.customer = customer;
        customer.seatAt(this);
    }

    /** Free the table after payment or abandonment. */
    public void free() {
        customer = null;
    }

    @Override public String toString() {
        return "Table " + number + ": " + (isFree() ? "FREE" : "Customer " + customer.getId());
    }
}
