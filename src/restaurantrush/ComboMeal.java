package restaurantrush;

import java.util.Objects;

/**
 * A combo is a MenuItem made of two existing items, so Order, Chef, Waiter and the
 * Cashier use it exactly like a normal dish and need no type checks.
 *
 * Preparation units = sum of the two components.
 * Price = 90% of the combined price, rounded to the nearest cent
 * (Salad + Pasta = ($9.00 + $18.00) x 0.9 = $24.30, 3 units).
 */
public class ComboMeal extends MenuItem {
    private static final int COMBO_PERCENT_OFF = 10;

    private final MenuItem first;
    private final MenuItem second;

    public ComboMeal(MenuItem first, MenuItem second) {
        super(first.getName() + " + " + second.getName() + " Combo",
                Money.percentOff(first.getPriceCents() + second.getPriceCents(), COMBO_PERCENT_OFF),
                first.getPrepUnits() + second.getPrepUnits());
        this.first = Objects.requireNonNull(first, "first");
        this.second = Objects.requireNonNull(second, "second");
    }

    public MenuItem getFirst() { return first; }
    public MenuItem getSecond() { return second; }
}
