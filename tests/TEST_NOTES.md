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

# Stage 3 test notes

## Reproduce the recorded run

Compile from the repository root:

    javac -Xlint:all -d out-stage3 src/restaurantrush/*.java

Replay through the CLI:

    java -cp out-stage3 restaurantrush.Main --echo < tests/stage3_run.txt

Recorded output: `tests/stage3_output.txt`.

To compare a fresh replay without overwriting the recording:

    java -cp out-stage3 restaurantrush.Main --echo < tests/stage3_run.txt > /tmp/sc2002-stage3-replay.txt
    diff -u tests/stage3_output.txt /tmp/sc2002-stage3-replay.txt

The replay matched the saved output with no differences.

## Setting and result

The run uses 18 turns, two tables and arrivals on turns 1, 4, 7, 10, 13 and 16.
C2 is a VIP, C4 is a Critic, and the other customers are Regular.
Food and combo choices are deterministic.

| Measure | Recorded result | Target |
|---|---|---|
| Paid customers | 5 | At least 4 |
| Revenue | $77.10 | At least $45.00 |
| Average satisfaction of paid customers | 83.2 | At least 60 |
| Served customers | 6 | — |
| Unhappy departures | 0 | — |
| Turns completed | 18 | 18 |

Outcome: VICTORY. C6 is served on turn 18 but has not paid by the end,
so C6's bill and satisfaction are excluded from the payment statistics.

## Host and invalid-action handling

- Turn 1: the Host seats C1. The Waiter then attempts to seat C1 again.
  The action is rejected because C1 is no longer in the waiting queue.
  The Waiter can still take C1's order during the same turn.
- Turn 10: the Host seats C4 while the Waiter serves C3, demonstrating
  that the Host's seating task is additional to the Waiter's task.
- Turn 13: the Host attempts to seat already-seated C4. The action is
  rejected without consuming the Host's task; the Host then seats C5.

The log contains two deliberate invalid seating attempts. These are expected
test cases, not failures of the recorded strategy.

## Happy Hour

Happy Hour is activated at the start of turn 5.

- Turn 5 reports two turns remaining, including the current turn.
- Turn 6 reports one turn remaining.
- Turn 7 reports Happy Hour off and already used.
- Eligible waiting customers receive 15 satisfaction at the start of each
  active turn, capped at 100. The log records recovery for VIP C2 on both
  active turns.

The VIP Burger ordered on turn 5 costs:

    $15.00 -> VIP discount: $13.50 -> Happy Hour discount: $10.80

The final bill is stored when the order is created. Happy Hour expires
before C2 pays on turn 8, but the Cashier still collects $10.80.

## Combos and preparation

| Customer | Ordered | Choice | Locked bill | Preparation |
|---|---|---|---|---|
| C3 | Turn 7 | Salad + Pasta Combo | $24.30 | 3 units, turns 7–9 |
| C6 | Turn 16 | Burger + Salad Combo | $21.60 | 2 units, turns 16–17 |

Combo prices are 90% of the combined item prices:

- Salad + Pasta: ($9.00 + $18.00) × 0.9 = $24.30.
- Burger + Salad: ($15.00 + $9.00) × 0.9 = $21.60.

C3's combo becomes READY on turn 9 and is served on turn 10.
C6's combo becomes READY on turn 17 and is served on turn 18.
Neither is served on the turn its preparation finishes.

## Locked-price payment evidence

| Customer | Payment turn | Amount collected |
|---|---|---|
| C1 Regular | 4 | $9.00 |
| C2 VIP | 8 | $10.80 |
| C3 Regular | 12 | $24.30 |
| C4 Critic | 15 | $18.00 |
| C5 Regular | 17 | $15.00 |

Total collected: $77.10. The unpaid C6 combo is not included.

## Focused Happy Hour checks

Compile and run:

    javac -Xlint:all -d out-stage3 src/restaurantrush/*.java tests/restaurantrush/HappyHourChecks.java tests/restaurantrush/HappyHourWaiterChecks.java
    java -cp out-stage3 restaurantrush.HappyHourChecks
    java -cp out-stage3 restaurantrush.HappyHourWaiterChecks

Previously verified results:

- 35 Happy Hour policy checks passed.
- 114 Happy Hour Waiter checks passed.

The policy checks cover activation, duration, repeat-activation rejection,
rounding, invalid requests and expiry. The Waiter checks cover Regular,
VIP and Critic pricing with Happy Hour on and off, displayed prices,
locked bills and payment after expiry.

These focused checks and the recorded run complement each other.
The recorded run does not exercise every boundary case.