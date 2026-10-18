package restaurantrush;

import java.util.Arrays;

/**
 * The base setting from brief section 2.1, kept in one place so a TA can
 * reproduce any run. Staff counts are recorded here for documentation;
 * Stage 1 always uses exactly one Waiter, one Chef and one Cashier.
 */
public final class GameConfig {
    private final int maxTurns;
    private final int tableCount;
    private final int waiterCount;
    private final int chefCount;
    private final int cashierCount;
    private final int[] arrivalTurns;
    private final int targetPaidCustomers;
    private final int targetRevenueCents;
    private final int targetAverageSatisfaction;

    private GameConfig(int maxTurns, int tableCount, int waiterCount, int chefCount, int cashierCount,
                       int[] arrivalTurns, int targetPaidCustomers, int targetRevenueCents,
                       int targetAverageSatisfaction) {
        this.maxTurns = maxTurns;
        this.tableCount = tableCount;
        this.waiterCount = waiterCount;
        this.chefCount = chefCount;
        this.cashierCount = cashierCount;
        this.arrivalTurns = arrivalTurns.clone();
        this.targetPaidCustomers = targetPaidCustomers;
        this.targetRevenueCents = targetRevenueCents;
        this.targetAverageSatisfaction = targetAverageSatisfaction;
    }

    /** 18 turns, 2 tables, 1 Waiter / Chef / Cashier, arrivals on turns 1, 4, 7, 10, 13, 16. */
    public static GameConfig baseSetting() {
        return new GameConfig(18, 2, 1, 1, 1,
                new int[] {1, 4, 7, 10, 13, 16},
                4, 4500, 60);
    }

    public int getMaxTurns() { return maxTurns; }
    public int getTableCount() { return tableCount; }
    public int getWaiterCount() { return waiterCount; }
    public int getChefCount() { return chefCount; }
    public int getCashierCount() { return cashierCount; }
    public int[] getArrivalTurns() { return arrivalTurns.clone(); }
    public int getTargetPaidCustomers() { return targetPaidCustomers; }
    public int getTargetRevenueCents() { return targetRevenueCents; }
    public int getTargetAverageSatisfaction() { return targetAverageSatisfaction; }

    @Override public String toString() {
        return maxTurns + " turns, " + tableCount + " tables, Waiter x" + waiterCount
                + ", Chef x" + chefCount + ", Cashier x" + cashierCount
                + ", arrivals on turns " + Arrays.toString(arrivalTurns);
    }
}
