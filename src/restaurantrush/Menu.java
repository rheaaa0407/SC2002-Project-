package restaurantrush;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Menu {
    private final List<MenuItem> items = new ArrayList<>();
    private final List<ComboMeal> combos = new ArrayList<>();

    /** The three base dishes only. Stage 1 and Stage 2 games use this menu. */
    public Menu() {
        items.add(new MenuItem("Salad", 900, 1));
        items.add(new MenuItem("Burger", 1500, 1));
        items.add(new MenuItem("Pasta", 1800, 2));
    }

    /** The Stage 3 menu: the base dishes plus the two combos. */
    public static Menu withCombos() {
        Menu menu = new Menu();
        menu.combos.add(new ComboMeal(menu.items.get(0), menu.items.get(2))); // Salad + Pasta
        menu.combos.add(new ComboMeal(menu.items.get(1), menu.items.get(0))); // Burger + Salad
        return menu;
    }

    /** Read-only view of the base dishes, so other classes cannot change the menu. */
    public List<MenuItem> getItems() { return Collections.unmodifiableList(items); }

    /** Read-only view of the combos (empty unless the menu was made with combos). */
    public List<ComboMeal> getCombos() { return Collections.unmodifiableList(combos); }
}
