package restaurantrush;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.IntFunction;

/**
 * Decides who arrives on which turn and what kind of customer they are.
 * Customer IDs follow arrival order (1 to 6). The ID is also the arrival number
 * that the FoodChoiceRule uses to pick a dish for Regular and VIP customers.
 *
 * Stage 2 base setting (built from a GameConfig): the 2nd arrival (turn 4) is a VIP
 * and the 4th arrival (turn 10) is a Critic; everyone else is Regular.
 */
public class ArrivalSchedule {
    private final Map<Integer, Integer> arrivalIdByTurn = new HashMap<>();
    private final Map<Integer, IntFunction<Customer>> makerByTurn = new HashMap<>();

    /** One customer maker per arrival, in arrival order, e.g. VIPCustomer::new. */
    public ArrivalSchedule(int[] arrivalTurns, List<IntFunction<Customer>> makers) {
        if (makers.size() != arrivalTurns.length) {
            throw new IllegalArgumentException("Need exactly one customer type per arrival");
        }
        int nextId = 1;
        for (int i = 0; i < arrivalTurns.length; i++) {
            int turn = arrivalTurns[i];
            if (turn < 1) throw new IllegalArgumentException("Arrival turns must be positive");
            if (arrivalIdByTurn.containsKey(turn)) {
                throw new IllegalArgumentException("Only one arrival per turn: turn " + turn);
            }
            arrivalIdByTurn.put(turn, nextId++);
            makerByTurn.put(turn, makers.get(i));
        }
    }

    /** Every arrival is a Regular customer (the Stage 1 setting, kept for regression checks). */
    public ArrivalSchedule(int[] arrivalTurns) {
        this(arrivalTurns, regulars(arrivalTurns.length));
    }

    /** The Stage 2 base setting: Regulars plus one VIP and one Critic. */
    public ArrivalSchedule(GameConfig config) {
        this(config.getArrivalTurns(), stage2Mix(config.getArrivalTurns().length));
    }

    private static List<IntFunction<Customer>> regulars(int count) {
        List<IntFunction<Customer>> makers = new ArrayList<>();
        for (int i = 0; i < count; i++) makers.add(RegularCustomer::new);
        return makers;
    }

    private static List<IntFunction<Customer>> stage2Mix(int count) {
        List<IntFunction<Customer>> makers = regulars(count);
        if (count > 1) makers.set(1, VIPCustomer::new);      // 2nd arrival
        if (count > 3) makers.set(3, CriticCustomer::new);   // 4th arrival
        return makers;
    }

    /** @return true if a customer is scheduled to arrive on this turn. */
    public boolean hasArrival(int turn) {
        return arrivalIdByTurn.containsKey(turn);
    }

    /**
     * @return a new customer (satisfaction 100, WAITING) for this turn, or null if
     *         nobody arrives. Call once per turn; each call creates a new object.
     */
    public Customer getArrival(int turn) {
        IntFunction<Customer> maker = makerByTurn.get(turn);
        return maker == null ? null : maker.apply(arrivalIdByTurn.get(turn));
    }

    public int getTotalArrivals() {
        return arrivalIdByTurn.size();
    }
}
