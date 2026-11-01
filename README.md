# SC2002 Restaurant Rush

Turn-based restaurant management game (Java, command line).

Repository: https://github.com/rheaaa0407/SC2002-Project-

## Build and run
Requires JDK 17 or newer.

    javac -d out src/restaurantrush/*.java
    java -cp out restaurantrush.Main

Optional graphical window (needs a desktop; the command line is the default):

    java -cp out restaurantrush.Main --gui

Replay the recorded Stage 3 run (prints each command):

    java -cp out restaurantrush.Main --echo < tests/stage3_run.txt

Regression checks for earlier stages. Compile the tests too:

    javac -d out src/restaurantrush/*.java tests/restaurantrush/*.java

Replay the Stage 1 runs (all-Regular schedule):

    java -cp out restaurantrush.Stage1RegressionRunner --echo < tests/victory_run.txt
    java -cp out restaurantrush.Stage1RegressionRunner --echo < tests/defeat_run.txt

Replay the Stage 2 runs (VIP and Critic, no Host, no combos, no Happy Hour):

    java -cp out restaurantrush.Stage2RegressionRunner --echo < tests/stage2_victory_run.txt
    java -cp out restaurantrush.Stage2RegressionRunner --echo < tests/stage2_defeat_run.txt

## Commands
Waiter: `S <id>` seat, `O <id>` take order, `V <id>` serve, `W` wait.
Chef: `C <id>` cook one unit of that customer's order, `W` wait.
Host (Stage 3): `S <id>` seat one waiting customer, `W` wait.
Happy Hour (Stage 3): at the start of a turn, `Y` starts it (once per game) or `N` skips it.

## Food choice rule
Customers choose their own food, never the manager. The rule is deterministic:
the customer's arrival number cycles through the menu, so C1 Salad, C2 Burger,
C3 Pasta, C4 Salad, C5 Burger, C6 Pasta.

## Stage 1
Regular customers, three menu items, Waiter, Chef, Cashier, two tables, waiting
queue, the six-step turn, abandonment, payment, and the Victory/Defeat check.
Test notes: tests/TEST_NOTES.md. Tag: `stage-1`.

## Stage 2
Adds two new customer types, VIP and Critic, next to the Regular customer. The turn flow, tables, queue and win targets are unchanged. Tag: `stage-2`.

- Regular: loses 8 satisfaction per waiting turn, pays full price.
- VIP: loses 5 per waiting turn, gets 10% off the bill (rounded to the nearest cent).
- Critic: loses 12 per waiting turn, pays full price, always orders Pasta.

The controller, Waiter and Cashier never check a customer's type. Each customer class supplies its own waiting loss, bill and food choice, so adding a new type only needs a new class.

Stage 2 customer mix (six arrivals, same turns as Stage 1): C2 (turn 4) is a VIP and C4 (turn 10) is a Critic; the other four are Regular.

Test notes and recorded Stage 2 runs: tests/TEST_NOTES.md.

## Stage 1 → Stage 2
Compare: https://github.com/rheaaa0407/SC2002-Project-/compare/stage-1...stage-2

### New files
- `RegularCustomer.java`, `VIPCustomer.java`, `CriticCustomer.java`: one class per customer type, each with its own waiting loss, bill and food choice.
- `tests/restaurantrush/Stage2Checks.java`: automatic checks of the new Stage 2 rules.
- `tests/restaurantrush/Stage1RegressionRunner.java`: replays the Stage 1 scripts with an all-Regular schedule to show Stage 1 behaviour did not break.
- `tests/MixedTypeResultsCheck.java`: checks revenue and average satisfaction with a mix of customer types.
- `tests/stage2_victory_run.txt`, `tests/stage2_defeat_run.txt` and their saved outputs: recorded Stage 2 runs.

### Changed files
- `Customer.java`: now abstract. Waiting loss, bill and type name are abstract methods each subclass fills in; the food choice has a default that subclasses can override. This replaces type checks in the controller.
- `ArrivalSchedule.java`: creates the right customer type for each arrival. The old all-Regular constructor is kept for the Stage 1 regression check.
- `Money.java`: added `percentOff` for the VIP discount.
- `Waiter.java`: takes an order using `customer.chooseItem(menu)` and `customer.calculateBill(price)`, so the bill is still fixed when the order is taken.
- `Cashier.java`: payment message now shows the customer's type.
- `ConsoleUI.java`: shows the Stage 2 rules and prints each customer as type plus ID (for example "VIP C2").
- `Menu.java` and `Order.java`: small cleanups (the menu list is read-only; an order marks itself ready when its cooking is done).
- `tests/TEST_NOTES.md`: added Stage 2 test notes.

### Deleted files
None. `Customer.java` was kept and made abstract instead of being removed.

### Note on Stage 1 scripts
Replaying `tests/victory_run.txt` on the Stage 2 schedule ends in DEFEAT, because the VIP and Critic change the game. The regression runner uses an all-Regular schedule, which matches the saved Stage 1 outputs apart from the banner and customer labels.

## Stage 3
Adds a Host, ComboMeal and Happy Hour. Tag: `stage-3`.

- Host: seats at most one queued customer per turn, in addition to the Waiter's task. An invalid Host choice shows the reason and does not use up the Host's turn. A customer the Host seats leaves the queue, so the Waiter cannot seat them again.
- ComboMeal: made of two existing items. Preparation units are the sum of the two, and the price is 90% of the combined price rounded to the nearest cent (Salad + Pasta = $24.30, 3 units; Burger + Salad = $21.60, 2 units).
- Combo choice rule: every customer whose arrival number is a multiple of 3 orders a combo, cycling through the combos (C3 gets Salad + Pasta, C6 gets Burger + Salad). The Critic never orders a combo and keeps Pasta. The choice is shown when the order is placed.
- Happy Hour: can be started once per game, at the start of a turn, and lasts that turn and the next (the remaining duration is displayed). Orders taken during it get 20% off after the customer's own discount (VIP Burger: $15.00 -> $13.50 -> $10.80). The price is locked when the order is taken. Customers still waiting for a table or food recover 15 satisfaction at the start of each active turn (maximum 100); customers who have left are not affected.
- Optional Swing GUI: `--gui`. It only shows the state and sends the manager's choices; all rules stay in the game classes.

Test notes and the recorded run (victory, 5 paid, $77.10, average satisfaction 83.2): `tests/TEST_NOTES.md`, `tests/stage3_run.txt`, `tests/stage3_output.txt`.

## Stage 2 → Stage 3
Compare: https://github.com/rheaaa0407/SC2002-Project-/compare/stage-2...stage-3

### New files
- `Host.java`: the Host role and its one seating task per turn.
- `HappyHourPolicy.java`: Happy Hour activation, duration, discount and recovery rules.
- `ComboMeal.java`: a MenuItem made of two items, so Order, Chef and Cashier need no type checks.
- `ComboChoiceRule.java`: the combo choice rule, kept separate so the Stage 1 `FoodChoiceRule` is not edited.
- `GameView.java`: the interface the game uses to talk to the player. Both `ConsoleUI` and the GUI implement it, so `TurnController` no longer depends on the console.
- `GuiView.java`, `GameWindow.java`, `FloorPanel.java`, `ActionPanel.java`, `OrdersPanel.java`: the optional Swing GUI.
- Tests: `ComboMealChecks.java`, `HappyHourChecks.java`, `HappyHourWaiterChecks.java`, `Stage2RegressionRunner.java`, `stage3_run.txt`, `stage3_output.txt`.

### Changed files
- `Customer.java`: added `recoverSatisfaction(amount)` (capped at 100) for Happy Hour and `acceptsCombos()` (default true).
- `CriticCustomer.java`: `acceptsCombos()` returns false, because the Critic always orders Pasta.
- `Menu.java`: added `getCombos()` and `Menu.withCombos()`. The default `Menu()` is unchanged, so Stage 1 and 2 games behave as before.
- `Waiter.java`: order-taking uses the combo choice rule and asks `HappyHourPolicy` for the final price after the customer's own discount; the price is locked when the order is taken.
- `TurnController.java`: added the Host's seating step, Happy Hour activation, countdown and recovery, and it now uses `GameView` instead of `ConsoleUI`. The Stage 1 and 2 constructor is kept (no Host, Happy Hour never offered), which the regression runners use.
- `ConsoleUI.java`: implements `GameView`; shows the Host's task, combos and the Happy Hour prompt and remaining duration.
- `Main.java`: builds the Stage 3 game (Host, combo menu, Happy Hour) and adds the `--gui` option.
- `tests/TEST_NOTES.md`: added the Stage 3 notes.

### Deleted files
None.

### Note on earlier stage runs
`Main` is now the Stage 3 game, so the saved Stage 1 and Stage 2 runs are replayed with `Stage1RegressionRunner` and `Stage2RegressionRunner`. They use the earlier schedules and rules and give the same results as before.

The wording on screen is the Stage 3 wording (the banner, labels such as "Regular C1", and the Host and Happy Hour lines), so a line-by-line comparison with the saved Stage 1 and Stage 2 outputs shows only label differences. The game results are identical: the paid count, revenue and average satisfaction in the final result match in all four saved runs. To check the results, compare the result lines only:

    R='GAME OVER|Paid customers|Revenue|Average satisfaction'
    java -cp out restaurantrush.Stage1RegressionRunner --echo < tests/victory_run.txt | grep -E "$R"

The saved outputs are also reproduced by the plain game when the `stage-1` and `stage-2` tags are checked out (apart from the typed commands, which the saved files echo).
