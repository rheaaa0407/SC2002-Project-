package restaurantrush;

import java.awt.GraphicsEnvironment;
import java.util.concurrent.CancellationException;

/** Builds the game and launches the CLI or optional Swing GUI. */
public class Main {
    public static void main(String[] args) {
        boolean echo = false;
        boolean gui = false;

        for (String argument : args) {
            switch (argument) {
                case "--echo":
                    echo = true;
                    break;
                case "--gui":
                    gui = true;
                    break;
                default:
                    System.err.println("Unknown option: " + argument);
                    System.err.println(
                            "Usage: java -cp out restaurantrush.Main [--echo] [--gui]");
                    return;
            }
        }

        if (gui && GraphicsEnvironment.isHeadless()) {
            System.err.println(
                    "The GUI requires a graphical desktop. Run without --gui for the CLI.");
            return;
        }

        GameConfig config = GameConfig.baseSetting();
        Restaurant restaurant = new Restaurant(config.getTableCount());
        ArrivalSchedule arrivals = new ArrivalSchedule(config);
        Menu menu = Menu.withCombos();

        HappyHourPolicy happyHour = new HappyHourPolicy();
        Waiter waiter = new Waiter(menu, happyHour);
        Host host = new Host();
        Chef chef = new Chef();
        Cashier cashier = new Cashier();
        ResultEvaluator evaluator = new ResultEvaluator(config);

        // Main runs outside Swing's UI thread.
        GameView view = gui
                ? new GuiView(config, evaluator, restaurant)
                : new ConsoleUI(
                        System.in, System.out, config, evaluator, echo);

        TurnController controller = new TurnController(
                restaurant, arrivals, host, waiter, chef, cashier,
                happyHour, evaluator, view, config.getMaxTurns());

        try {
            view.showWelcome(config, menu);
            controller.runGame();
        } catch (CancellationException exception) {
            if (!gui) {
                throw exception;
            }
            // Closing the GUI ends a pending manager choice normally.
        }
    }
}