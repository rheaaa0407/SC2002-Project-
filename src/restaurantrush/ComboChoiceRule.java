package restaurantrush;

import java.util.List;
import java.util.Objects;

/**
 * Deterministic combo choice (the customer picks, never the manager).
 *
 * Rule: if the menu has combos, every customer whose arrival number is a multiple
 * of 3 orders a combo, cycling through the combos in menu order
 * (C3 gets the first combo, C6 gets the second, C9 the first again, ...).
 * Every other customer orders as before.
 *
 * A customer who does not accept combos (the Critic, who always orders Pasta)
 * keeps their own choice. This is decided by the customer object, not by a type check.
 *
 * This is a separate class so the Stage 1 FoodChoiceRule is not edited. With a
 * menu that has no combos it returns exactly what the customer would have chosen.
 */
public class ComboChoiceRule {
    private static final int EVERY_NTH_ARRIVAL = 3;

    public MenuItem choose(Customer customer, Menu menu) {
        Objects.requireNonNull(customer, "customer");
        Objects.requireNonNull(menu, "menu");

        MenuItem ownChoice = customer.chooseItem(menu);
        List<ComboMeal> combos = menu.getCombos();
        int id = customer.getId();

        if (combos.isEmpty() || id % EVERY_NTH_ARRIVAL != 0 || !customer.acceptsCombos()) {
            return ownChoice;
        }
        return combos.get((id / EVERY_NTH_ARRIVAL - 1) % combos.size());
    }
}
