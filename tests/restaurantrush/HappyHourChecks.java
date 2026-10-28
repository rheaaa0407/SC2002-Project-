package restaurantrush;

public final class HappyHourChecks {
    private static int checks;

    private static void check(boolean condition, String message) {
        checks++;
        if (!condition) {
            throw new AssertionError(message);
        }
    }

    public static void main(String[] args) {
        HappyHourPolicy policy = new HappyHourPolicy();

        check(policy.canActivate(), "Initially available");
        check(!policy.hasBeenUsed(), "Initially unused");
        check(!policy.isActive(), "Initially inactive");
        check(policy.getRemainingTurns() == 0, "Initially zero duration");
        check(policy.calculateBill(1500) == 1500, "No initial discount");

        check(!policy.beginTurn(0, true).isSuccess(),
                "Reject invalid turn");
        check(policy.canActivate(), "Invalid turn preserves activation");

        check(policy.beginTurn(1, false).isSuccess(), "Start turn 1");
        check(!policy.isActive(), "Off when not activated");

        check(!policy.beginTurn(1, true).isSuccess(),
                "Cannot activate by restarting the current turn");
        check(policy.canActivate(), "Rejected restart preserves activation");

        check(policy.beginTurn(2, true).isSuccess(), "Activate on turn 2");
        check(policy.hasBeenUsed(), "Activation recorded");
        check(!policy.canActivate(), "Cannot activate again");
        check(policy.isActive(), "Active on activation turn");
        check(policy.getRemainingTurns() == 2, "Two turns remaining");
        check(policy.getRemainingTurns() == 2, "Reading does not decrement");
        check(policy.calculateBill(1500) == 1200, "Regular discount");

        VIPCustomer vip = new VIPCustomer(2);
        int vipPrice = vip.calculateBill(1500);
        check(vipPrice == 1350, "VIP discount applied first");
        check(policy.calculateBill(vipPrice) == 1080,
                "VIP plus Happy Hour is $10.80");
        check(policy.calculateBill(903) == 722, "Nearest-cent rounding");
        check(policy.calculateBill(0) == 0, "Zero price");

        MenuItem burger = new MenuItem("Burger", 1500, 1);
        Order lockedOrder = new Order(
                vip, burger, policy.calculateBill(vipPrice));

        check(policy.beginTurn(3, false).isSuccess(), "Start next turn");
        check(policy.isActive(), "Active on following turn");
        check(policy.getRemainingTurns() == 1, "One turn remaining");

        check(!policy.beginTurn(4, true).isSuccess(),
                "Reject second activation");
        check(policy.getRemainingTurns() == 1,
                "Rejected request leaves state unchanged");

        check(policy.beginTurn(4, false).isSuccess(),
                "Retry turn without activation");
        check(!policy.isActive(), "Expired after two turns");
        check(policy.getRemainingTurns() == 0, "Expired duration is zero");
        check(policy.calculateBill(vipPrice) == 1350,
                "New bills lose Happy Hour after expiry");
        check(lockedOrder.getBillCents() == 1080,
                "Existing order keeps its locked price");

        check(!policy.beginTurn(3, false).isSuccess(),
                "Reject backwards turn");
        check(!policy.canActivate(), "Expiry does not restore activation");

        boolean rejectedNegative = false;
        try {
            policy.calculateBill(-1);
        } catch (IllegalArgumentException expected) {
            rejectedNegative = true;
        }
        check(rejectedNegative, "Reject negative bill");

        System.out.println("PASS: " + checks + " Happy Hour checks.");
    }
}