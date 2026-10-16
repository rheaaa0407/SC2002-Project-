package restaurantrush;

public class FoodChoiceRule {
    // TODO: deterministic rule, e.g. cycle Salad, Burger, Pasta by arrival order
    public MenuItem choose(int arrivalIndex, Menu menu) {
        return menu.getItems().get(0);   // placeholder
    }
}
