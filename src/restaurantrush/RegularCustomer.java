package restaurantrush;

/** Stage 1 behaviour preserved as a concrete customer subtype. */
public class RegularCustomer extends Customer {
    public RegularCustomer(int id) { super(id); }

    @Override public int getWaitingLoss() { return 8; }

    @Override public int calculateBill(int priceCents) {
        if (priceCents < 0) throw new IllegalArgumentException("Price cannot be negative");
        return priceCents;
    }

    @Override public String getTypeName() { return "Regular"; }
}
