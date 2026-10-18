package restaurantrush;

/** A manager's staff assignment; never contains a manager-selected meal. */
public final class WaiterChoice {
    public enum Task { SEAT, TAKE_ORDER, SERVE, WAIT }
    private final Task task;
    private final int customerId;

    private WaiterChoice(Task task, int customerId) {
        this.task = task;
        this.customerId = customerId;
    }
    public static WaiterChoice seat(int id) { return new WaiterChoice(Task.SEAT, id); }
    public static WaiterChoice takeOrder(int id) { return new WaiterChoice(Task.TAKE_ORDER, id); }
    public static WaiterChoice serve(int id) { return new WaiterChoice(Task.SERVE, id); }
    public static WaiterChoice waitTask() { return new WaiterChoice(Task.WAIT, 0); }
    public Task getTask() { return task; }
    public int getCustomerId() { return customerId; }
}
