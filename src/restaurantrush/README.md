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
