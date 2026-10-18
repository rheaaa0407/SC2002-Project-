package restaurantrush;

import java.util.List;

/**
 * Deterministic food choice (the customer picks, never the manager).
 * Rule: arrival number 1, 2, 3, 4, 5, 6 orders Salad, Burger, Pasta,
 * Salad, Burger, Pasta, i.e. the menu is cycled in order.
 */
public class FoodChoiceRule {
    public MenuItem choose(int arrivalIndex, Menu menu) {
        if (arrivalIndex <= 0) throw new IllegalArgumentException("Arrival index must be positive");
        List<MenuItem> items = menu.getItems();
        return items.get((arrivalIndex - 1) % items.size());
    }
}
