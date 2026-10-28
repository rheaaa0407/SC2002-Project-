package restaurantrush;

public final class ComboMealChecks {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) throw new AssertionError(message);
    }

    private static Order takeOrder(Customer customer, Menu menu) {
        Restaurant restaurant = new Restaurant(2);
        Waiter waiter = new Waiter(menu);
        restaurant.addArrival(customer);
        waiter.beginTurn(1, restaurant);
        check(waiter.perform(WaiterChoice.seat(customer.getId()), restaurant).isSuccess(), "Seat");
        waiter.beginTurn(2, restaurant);
        ActionResult result = waiter.perform(WaiterChoice.takeOrder(customer.getId()), restaurant);
        check(result.isSuccess(), "Take order");
        return customer.getOrder();
    }

    public static void main(String[] args) {
        Menu menu = Menu.withCombos();
        MenuItem salad = menu.getItems().get(0);
        MenuItem burger = menu.getItems().get(1);
        MenuItem pasta = menu.getItems().get(2);

        ComboMeal saladPasta = new ComboMeal(salad, pasta);
        check(saladPasta.getPriceCents() == 2430, "Salad + Pasta is $24.30");
        check(saladPasta.getPrepUnits() == 3, "Salad + Pasta is 3 units");
        ComboMeal burgerSalad = new ComboMeal(burger, salad);
        check(burgerSalad.getPriceCents() == 2160, "Burger + Salad is $21.60");
        check(burgerSalad.getPrepUnits() == 2, "Burger + Salad is 2 units");
        check(saladPasta.getName().equals("Salad + Pasta Combo"), "Combo name");
        check(new ComboMeal(new MenuItem("A", 1001, 1), new MenuItem("B", 1000, 1)).getPriceCents() == 1801,
                "Rounds to the nearest cent ($20.01 x 0.9 = 18.009 -> $18.01)");
        check(new ComboMeal(new MenuItem("A", 505, 1), new MenuItem("B", 0, 1)).getPriceCents() == 455,
                "Half a cent rounds up (454.5 -> 455)");

        check(new Menu().getCombos().isEmpty(), "The default menu has no combos");
        check(new Menu().getItems().size() == 3, "The default menu has 3 dishes");
        check(menu.getItems().size() == 3 && menu.getCombos().size() == 2, "Stage 3 menu: 3 dishes + 2 combos");

        ComboChoiceRule rule = new ComboChoiceRule();
        check(rule.choose(new RegularCustomer(1), menu) == salad, "C1 still orders Salad");
        check(rule.choose(new VIPCustomer(2), menu) == burger, "C2 still orders Burger");
        check(rule.choose(new RegularCustomer(3), menu).getName().equals("Salad + Pasta Combo"), "C3 orders the first combo");
        check(rule.choose(new CriticCustomer(4), menu) == pasta, "Critic C4 keeps Pasta");
        check(rule.choose(new RegularCustomer(5), menu) == burger, "C5 still orders Burger");
        check(rule.choose(new RegularCustomer(6), menu).getName().equals("Burger + Salad Combo"), "C6 orders the second combo");
        check(rule.choose(new VIPCustomer(9), menu).getName().equals("Salad + Pasta Combo"), "C9 cycles to the first combo");
        check(rule.choose(new CriticCustomer(3), menu) == pasta, "A Critic on a combo arrival still keeps Pasta");
        Menu plain = new Menu();
        check(rule.choose(new RegularCustomer(3), plain) == plain.getItems().get(2), "No combos on the menu: C3 orders Pasta as in Stage 2");

        Order regular = takeOrder(new RegularCustomer(3), Menu.withCombos());
        check(regular.getBillCents() == 2430, "Regular combo bill $24.30");
        check(regular.getItem().getPrepUnits() == 3, "Order takes the combo's 3 units");
        Order vip = takeOrder(new VIPCustomer(3), Menu.withCombos());
        check(vip.getBillCents() == 2187, "VIP combo bill $24.30 x 0.9 = $21.87");
        check(takeOrder(new RegularCustomer(3), new Menu()).getBillCents() == 1800, "Stage 2 menu: C3 pays $18.00 for Pasta");

        System.out.println("PASS: " + checks + " ComboMeal checks.");
    }
}
