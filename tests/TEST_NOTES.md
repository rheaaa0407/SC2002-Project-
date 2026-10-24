# Stage 1 test notes

Both runs use the base setting (18 turns, 2 tables, arrivals on turns 1, 4, 7, 10, 13, 16)
and need no randomness: the food choice is deterministic.

## Victory run
Replay: `java -cp out restaurantrush.Main --echo < tests/victory_run.txt`
The manager gives each customer full attention: seat, take order, cook, serve, in that order.
Result: 5 paid customers, revenue $66.00, average satisfaction 79.2 -> VICTORY.
C6 arrives on turn 16 and cannot finish paying within 18 turns (intended).
Saved output: tests/victory_output.txt

## Defeat run
Replay: `java -cp out restaurantrush.Main --echo < tests/defeat_run.txt`
The manager chooses Wait every turn.
Result: 0 paid customers, revenue $0.00, average satisfaction N/A -> DEFEAT.
C1 leaves after turn 13 and C2 after turn 16 (satisfaction reaches 0).
Saved output: tests/defeat_output.txt

# Stage 2 test notes

Arrivals are still on turns 1, 4, 7, 10, 13, 16. C2 is a VIP and C4 is a Critic; the rest are Regular.

| Type | Loss per waiting turn | Bill | Dish |
|---|---|---|---|
| Regular | 8 | full price | arrival rule (Salad, Burger, Pasta, ...) |
| VIP | 5 | 10% off, rounded to the cent | arrival rule |
| Critic | 12 | full price | always Pasta |

## Type-check audit (TurnController, Cashier, Restaurant)
Different customer behaviour is dispatched through the customer objects, not through type checks:

- Satisfaction loss: `TurnController` calls `customer.loseSatisfaction()`, which uses the customer's own `getWaitingLoss()`.
- Bill: the Waiter locks `customer.calculateBill(price)` into the `Order` when the order is taken; `Cashier` and
  `Restaurant` only read `order.getBillCents()`.
- Display: `Cashier` names the payer with `payer.getTypeName()`.

Proof (no output, exit code 1 = no matches):

    grep -nE "instanceof|getClass\(\)|RegularCustomer|VIPCustomer|CriticCustomer|getTypeName\(\)\.equals" \
        src/restaurantrush/TurnController.java src/restaurantrush/Cashier.java src/restaurantrush/Restaurant.java

None of these three files needed a rule change for Stage 2. The only edit is the Cashier message showing the customer's type.

## Mixed-type revenue and satisfaction check

    javac -d out src/restaurantrush/*.java tests/MixedTypeResultsCheck.java
    java -cp out MixedTypeResultsCheck

Drives a Critic, a Regular and a VIP to payment through the real `Restaurant` and `Cashier` and checks:
loss rates 12 / 8 / 5, bills $18.00 / $15.00 / $13.50, oldest-first payment, revenue $46.50,
average satisfaction (76 + 84 + 90) / 3 = 83.3, and that an abandoned VIP adds no revenue and is not averaged.
All 14 checks pass.

## Stage 2 victory run (with a VIP and a Critic)
Replay: `java -cp out restaurantrush.Main --echo < tests/stage2_victory_run.txt`

Same strategy as Stage 1: seat, take order, cook, serve, in arrival order.

| Customer | Type | Dish | Bill | Seated | Ordered | Served | Paid | Satisfaction at payment |
|---|---|---|---|---|---|---|---|---|
| C1 | Regular | Salad | $9.00 | T1 | T2 | T3 | T5 | 84 |
| C2 | VIP | Burger | $13.50 | T4 | T5 | T6 | T8 | 90 |
| C3 | Regular | Pasta | $18.00 | T7 | T8 | T10 | T12 | 76 |
| C4 | Critic | Pasta (arrival rule would give Salad) | $18.00 | T11 | T12 | T14 | T16 | 52 |
| C5 | Regular | Burger | $15.00 | T13 | T15 | T16 | T18 | 76 |
| C6 | Regular | Pasta | — | T17 | T18 | — | — | (cannot finish within 18 turns) |

Result: 5 paid, revenue $73.50, average satisfaction (84 + 90 + 76 + 52 + 76) / 5 = 75.6 -> VICTORY.
The Critic loses the most: waiting on turn 10 while the Waiter serves C3 costs 12, not 8.
Saved output: tests/stage2_victory_output.txt

## Stage 2 defeat run
Replay: `java -cp out restaurantrush.Main --echo < tests/stage2_defeat_run.txt`

The manager chooses Wait every turn. Result: 0 paid, revenue $0.00, average N/A -> DEFEAT.
The three loss rates show in who leaves:
- Regular C1 (arrives T1) leaves after turn 13.
- Critic C4 (arrives T10) leaves after turn 18: 9 turns x 12 reaches 0.
- VIP C2 (arrives T4) is still there at turn 18 with satisfaction 30. As a Regular in Stage 1 it left after turn 16.

Saved output: tests/stage2_defeat_output.txt
