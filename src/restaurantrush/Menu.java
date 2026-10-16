package restaurantrush;

import java.util.ArrayList;
import java.util.List;

public class Menu {
    private final List<MenuItem> items = new ArrayList<>();

    public Menu() {
        items.add(new MenuItem("Salad", 900, 1));
        items.add(new MenuItem("Burger", 1500, 1));
        items.add(new MenuItem("Pasta", 1800, 2));
    }

    public List<MenuItem> getItems() { return items; }
}
