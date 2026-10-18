package restaurantrush;

import java.util.OptionalDouble;

/**
 * Checks the objective after the last turn: Victory only if all three targets
 * are met (brief 2.1). Rules live here, not in the UI.
 */
public class ResultEvaluator {
    private final int targetPaid;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;

    public ResultEvaluator(GameConfig config) {
        this.targetPaid = config.getTargetPaidCustomers();
        this.targetRevenueCents = config.getTargetRevenueCents();
        this.targetAverageSatisfaction = config.getTargetAverageSatisfaction();
    }

    public boolean meetsPaidTarget(Restaurant restaurant) {
        return restaurant.getPaidCount() >= targetPaid;
    }

    public boolean meetsRevenueTarget(Restaurant restaurant) {
        return restaurant.getRevenueCents() >= targetRevenueCents;
    }

    /** No paid customers means no average, which fails the target. */
    public boolean meetsSatisfactionTarget(Restaurant restaurant) {
        OptionalDouble average = restaurant.getAverageSatisfaction();
        return average.isPresent() && average.getAsDouble() >= targetAverageSatisfaction;
    }

    public boolean isVictory(Restaurant restaurant) {
        return meetsPaidTarget(restaurant)
                && meetsRevenueTarget(restaurant)
                && meetsSatisfactionTarget(restaurant);
    }

    public int getTargetPaid() { return targetPaid; }
    public int getTargetRevenueCents() { return targetRevenueCents; }
    public int getTargetAverageSatisfaction() { return targetAverageSatisfaction; }
}
