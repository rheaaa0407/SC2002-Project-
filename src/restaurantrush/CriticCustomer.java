package restaurantrush;

/**
 * Stage 2 food critic. Loses satisfaction fastest (12 per waiting turn) and
 * pays full price. Documented behaviour: a Critic always orders Pasta, the
 * dish that takes the Chef longest (2 units), whatever their arrival number.
 */
public class CriticCustomer extends Customer {
    private static final int WAITING_LOSS = 12;
    private static final String CRITIC_DISH = "Pasta";

    public CriticCustomer(int id) {
        super(id);
    }

    @Override
    public int getWaitingLoss() { return WAITING_LOSS; }

    /** Critics get no discount. */
    @Override
    public int calculateBill(int priceCents) { return priceCents; }

    /** The Critic always orders Pasta, so a combo is never offered. */
    @Override
    public boolean acceptsCombos() { return false; }

    /** Always picks Pasta from the menu, ignoring the arrival-order rule. */
    @Override
    public MenuItem chooseItem(Menu menu) {
        for (MenuItem item : menu.getItems()) {
            if (item.getName().equals(CRITIC_DISH)) return item;
        }
        throw new IllegalStateException("Critic needs " + CRITIC_DISH + " on the menu");
    }

    @Override
    public String getTypeName() { return "Critic"; }
}
