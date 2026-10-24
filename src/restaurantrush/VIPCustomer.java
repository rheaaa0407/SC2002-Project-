package restaurantrush;

/** VIPs are more patient and receive a 10% discount. */
public class VIPCustomer extends Customer {
    public VIPCustomer(int id) { super(id); }

    @Override public int getWaitingLoss() { return 5; }

    @Override public int calculateBill(int priceCents) {
        return Money.percentOff(priceCents, 10);
    }

    @Override public String getTypeName() { return "VIP"; }
}
