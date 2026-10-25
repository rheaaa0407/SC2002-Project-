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
