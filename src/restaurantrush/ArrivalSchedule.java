package restaurantrush;

import java.util.HashMap;
import java.util.Map;

/**
 * Decides who arrives on which turn. In Stage 1 every arrival is a regular
 * Customer. Customer IDs follow arrival order (1 to 6), which the
 * FoodChoiceRule uses to pick each customer's dish.
 */
public class ArrivalSchedule {
    private final Map<Integer, Integer> arrivalIdByTurn = new HashMap<>();

    public ArrivalSchedule(int[] arrivalTurns) {
        int nextId = 1;
        for (int turn : arrivalTurns) {
            if (turn < 1) throw new IllegalArgumentException("Arrival turns must be positive");
            if (arrivalIdByTurn.containsKey(turn)) {
                throw new IllegalArgumentException("Only one arrival per turn: turn " + turn);
            }
            arrivalIdByTurn.put(turn, nextId++);
        }
    }

    public ArrivalSchedule(GameConfig config) {
        this(config.getArrivalTurns());
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
        Integer id = arrivalIdByTurn.get(turn);
        return id == null ? null : new Customer(id);
    }

    public int getTotalArrivals() {
        return arrivalIdByTurn.size();
    }
}
