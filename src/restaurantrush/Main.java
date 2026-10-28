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
        Menu menu = Menu.withCombos();
        HappyHourPolicy happyHour = new HappyHourPolicy();     // shared: the controller switches it on,
        Waiter waiter = new Waiter(menu, happyHour);           // the Waiter prices orders with it
        Host host = new Host();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        ResultEvaluator evaluator = new ResultEvaluator(config);
        ConsoleUI ui = new ConsoleUI(System.in, System.out, config, evaluator, echo);

        ui.showWelcome(config, menu);
        TurnController controller = new TurnController(restaurant, arrivals, host, waiter, chef,
                cashier, happyHour, evaluator, ui, config.getMaxTurns());
        controller.runGame();
    }
}
