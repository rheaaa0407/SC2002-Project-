package restaurantrush;

/**
 * Entry point. Builds every object for the base setting and starts the game.
 *
 * Run interactively:      java -cp out restaurantrush.Main
 * Replay a saved run:     java -cp out restaurantrush.Main --echo < tests/victory_run.txt
 * (--echo prints each command so the log of a piped run is readable.)
 */
public class Main {
    public static void main(String[] args) {
        boolean echo = args.length > 0 && args[0].equals("--echo");

        GameConfig config = GameConfig.baseSetting();
        Restaurant restaurant = new Restaurant(config.getTableCount());
        ArrivalSchedule arrivals = new ArrivalSchedule(config);
        Waiter waiter = new Waiter();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        ResultEvaluator evaluator = new ResultEvaluator(config);
        ConsoleUI ui = new ConsoleUI(System.in, System.out, config, evaluator, echo);

        ui.showWelcome(config);
        TurnController controller = new TurnController(restaurant, arrivals, waiter, chef,
                cashier, evaluator, ui, config.getMaxTurns());
        controller.runGame();
    }
}
