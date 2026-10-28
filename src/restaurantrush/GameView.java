package restaurantrush;

/**
 * Everything the TurnController needs from the player's screen. ConsoleUI
 * implements it for the command line; a GUI can implement the same methods,
 * so the controller never depends on one particular display.
 *
 * A view only shows state and reads choices. It holds no game rules: the
 * staff objects and HappyHourPolicy decide whether a choice is valid.
 */
public interface GameView {

    // ----- game start -----

    /** Setting, menu (including combos), customer types and commands. */
    void showWelcome(GameConfig config, Menu menu);

    // ----- start of turn -----

    void showArrival(Customer customer);

    /**
     * Revenue, queue, tables, Happy Hour status and the tasks each staff
     * member can do this turn.
     */
    void showTurnStart(int turn, int maxTurns, Restaurant restaurant, HappyHourPolicy happyHour);

    /**
     * Asks whether to start Happy Hour this turn. Only asks while the policy
     * still allows it; otherwise returns false without asking.
     */
    boolean askHappyHour(HappyHourPolicy happyHour);

    // ----- staff choices -----

    /** @return the ID of the queued customer the Host should seat, or null to skip. */
    Integer askHostChoice(Restaurant restaurant);

    WaiterChoice askWaiterChoice(Restaurant restaurant);

    /** @return the order the Chef should cook one unit of, or null for Wait. */
    Order askChefChoice(Restaurant restaurant);

    /** Shows a success message, or a failure reason (the player is then asked again). */
    void showActionResult(ActionResult result);

    void showDeparture(Customer customer);

    // ----- end of turn and game -----

    void showTurnEnd(int turn, Restaurant restaurant);

    void showFinalResult(boolean victory, Restaurant restaurant);
}
