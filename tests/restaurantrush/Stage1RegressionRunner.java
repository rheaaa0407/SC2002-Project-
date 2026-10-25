package restaurantrush;

/** Runs the current code with the original all-Regular schedule. */
public class Stage1RegressionRunner {
    public static void main(String[] args) {
        GameConfig config = GameConfig.baseSetting();
        Restaurant restaurant = new Restaurant(config.getTableCount());

        ArrivalSchedule arrivals =
                new ArrivalSchedule(config.getArrivalTurns());

        ResultEvaluator evaluator = new ResultEvaluator(config);
        ConsoleUI ui = new ConsoleUI(
                System.in, System.out, config, evaluator, true);

        ui.showWelcome(config);

        TurnController controller = new TurnController(
                restaurant, arrivals, new Waiter(), new Chef(),
                new Cashier(), evaluator, ui, config.getMaxTurns());

        controller.runGame();
    }
}