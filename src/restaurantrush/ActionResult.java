package restaurantrush;

/**
 * Outcome of one staff action (Waiter, Chef, Cashier).
 * A failure carries the reason so the UI can show it and ask again
 * without using up the staff task (brief 3.4).
 */
public final class ActionResult {
    private final boolean success;
    private final String message;

    private ActionResult(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public static ActionResult success(String message) {
        return new ActionResult(true, message);
    }

    public static ActionResult failure(String reason) {
        return new ActionResult(false, reason);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }
}
