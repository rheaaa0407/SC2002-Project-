# SC2002 Restaurant Rush

Turn-based restaurant management game (Java, command line).

Repository: https://github.com/rheaaa0407/SC2002-Project-

## Build and run
Requires JDK 17 or newer.

    javac -d out src/restaurantrush/*.java
    java -cp out restaurantrush.Main

Replay a saved Stage 2 run (prints each command):

    java -cp out restaurantrush.Main --echo < tests/stage2_victory_run.txt
    java -cp out restaurantrush.Main --echo < tests/stage2_defeat_run.txt

Replay the Stage 1 runs on the all-Regular schedule (Stage 1 regression check):

    java -cp out restaurantrush.Stage1RegressionRunner --echo < tests/victory_run.txt
    java -cp out restaurantrush.Stage1RegressionRunner --echo < tests/defeat_run.txt

To run the regression runner, compile the tests too:

    javac -d out src/restaurantrush/*.java tests/restaurantrush/*.java

## Commands
Waiter: `S <id>` seat, `O <id>` take order, `V <id>` serve, `W` wait.
Chef: `C <id>` cook one unit of that customer's order, `W` wait.

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