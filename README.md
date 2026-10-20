# SC2002 Restaurant Rush

Turn-based restaurant management game (Java, command line).

## Build and run
Requires JDK 17 or newer.

    javac -d out src/restaurantrush/*.java
    java -cp out restaurantrush.Main

Replay a saved run (prints each command):

    java -cp out restaurantrush.Main --echo < tests/victory_run.txt
    java -cp out restaurantrush.Main --echo < tests/defeat_run.txt

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
