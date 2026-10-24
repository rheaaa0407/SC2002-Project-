package restaurantrush;

import java.io.InputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.OptionalDouble;
import java.util.Scanner;

/**
 * Boundary class: shows the game state and reads the manager's choices.
 * It holds no game rules. It only turns text into a WaiterChoice or an Order
 * and hands it to the controller, which asks the staff objects to act.
 * If a choice is invalid the staff object returns the reason, this class
 * prints it, and the controller asks again (brief 3.4).
 *
 * Input commands (case-insensitive, customer IDs are the numbers shown as C1, C2, ...):
 *   Waiter:  S <id> seat   |  O <id> take order  |  V <id> serve  |  W wait
 *   Chef:    C <id> cook one unit of that customer's order  |  W wait
 * The ID may be left out when exactly one customer fits the task.
 */
public class ConsoleUI {
    private static final String LINE = "------------------------------------------------------------";

    private final Scanner in;
    private final PrintStream out;
    private final int maxTurns;
    private final ResultEvaluator resultEvaluator;
    private final boolean echoInput;

    /**
     * @param echoInput print each command after reading it. Turn this on when
     *                  input is piped from a file so the log shows every choice.
     */
    public ConsoleUI(InputStream input, PrintStream output, GameConfig config,
                     ResultEvaluator resultEvaluator, boolean echoInput) {
        this.in = new Scanner(input);
        this.out = output;
        this.maxTurns = config.getMaxTurns();
        this.resultEvaluator = resultEvaluator;
        this.echoInput = echoInput;
    }

    // =====================================================================
    // Game start
    // =====================================================================

    public void showWelcome(GameConfig config) {
        out.println("============================================================");
        out.println("                   RESTAURANT RUSH  (Stage 2)");
        out.println("============================================================");
        out.println("Setting : " + config);
        out.println("Menu    : Salad $9.00 (1 unit), Burger $15.00 (1 unit), Pasta $18.00 (2 units)");
        out.println("Goal    : after turn " + config.getMaxTurns() + ", at least "
                + config.getTargetPaidCustomers() + " paid customers, revenue at least "
                + Money.format(config.getTargetRevenueCents()) + ", average satisfaction at least "
                + config.getTargetAverageSatisfaction() + ".");
        out.println("Guests  : Regular loses 8 satisfaction per waiting turn, pays full price.");
        out.println("          VIP loses 5 per waiting turn, gets 10% off the bill.");
        out.println("          Critic loses 12 per waiting turn, pays full price.");
        out.println("Food    : customers choose their own dish, never the manager.");
        out.println("          Regular and VIP choose by arrival order:");
        out.println("          C1 Salad, C2 Burger, C3 Pasta, C4 Salad, C5 Burger, C6 Pasta.");
        out.println("          A Critic always orders Pasta.");
        out.println();
        out.println("Commands  Waiter: S <id> seat | O <id> take order | V <id> serve | W wait");
        out.println("          Chef  : C <id> cook that customer's order | W wait");
        out.println("          (the ID can be left out when only one customer fits)");
    }

    // =====================================================================
    // Start-of-turn display (brief 3.1)
    // =====================================================================

    public void showArrival(Customer customer) {
        out.println();
        out.println(">> " + label(customer) + " arrives and joins the waiting queue (satisfaction "
                + customer.getSatisfaction() + ").");
    }

    public void showTurnStart(int turn, int maxTurns, Restaurant restaurant) {
        out.println();
        out.println("======================= TURN " + turn + " / " + maxTurns + " =======================");
        out.println("Revenue: " + Money.format(restaurant.getRevenueCents())
                + "   Avg satisfaction (paid): " + formatAverage(restaurant.getAverageSatisfaction())
                + "   Paid: " + restaurant.getPaidCount());

        List<Customer> queue = restaurant.getWaitingQueue().getCustomers();
        StringBuilder q = new StringBuilder("Waiting queue: ");
        if (queue.isEmpty()) {
            q.append("(empty)");
        } else {
            for (int i = 0; i < queue.size(); i++) {
                Customer c = queue.get(i);
                if (i > 0) q.append(", ");
                q.append(label(c)).append(" (sat ").append(c.getSatisfaction()).append(")");
            }
        }
        out.println(q);

        out.println("Tables:");
        for (Table table : restaurant.getTables()) {
            out.println("  " + describeTable(table));
        }

        out.println("Staff tasks available:");
        out.println("  Waiter : " + describeWaiterOptions(restaurant));
        out.println("  Chef   : " + describeChefOptions(restaurant));
        out.println("  Cashier: " + describeCashier(restaurant));
        out.println(LINE);
    }

    private String describeTable(Table table) {
        String head = "Table " + table.getNumber() + ": ";
        if (table.isFree()) return head + "FREE";
        Customer c = table.getCustomer();
        StringBuilder sb = new StringBuilder(head);
        sb.append(label(c))
          .append("  sat ").append(c.getSatisfaction())
          .append("  ").append(c.getStatus());
        Order order = c.getOrder();
        if (order == null) {
            sb.append("  order: not taken yet");
        } else {
            sb.append("  order: ").append(order.getItem().getName())
              .append(" ").append(Money.format(order.getBillCents()))
              .append(" ").append(order.getStatus());
            if (order.canAdvance()) {
                sb.append(" (").append(order.getRemainingUnits()).append(" unit")
                  .append(order.getRemainingUnits() == 1 ? "" : "s").append(" left)");
            }
        }
        return sb.toString();
    }

    private String describeWaiterOptions(Restaurant restaurant) {
        List<String> options = new ArrayList<>();
        List<Customer> queue = restaurant.getWaitingQueue().getCustomers();
        if (!queue.isEmpty()) {
            if (restaurant.findFreeTable() != null) {
                for (Customer c : queue) options.add("S " + c.getId());
            } else {
                options.add("(no free table to seat anyone)");
            }
        }
        for (Customer c : restaurant.getCustomersAwaitingOrder()) options.add("O " + c.getId());
        for (Order o : restaurant.getReadyOrders()) options.add("V " + o.getCustomer().getId());
        options.add("W");
        return String.join(" | ", options);
    }

    private String describeChefOptions(Restaurant restaurant) {
        List<String> options = new ArrayList<>();
        for (Order o : restaurant.getOrdersInKitchen()) {
            options.add("C " + o.getCustomer().getId() + " (" + o.getItem().getName() + ", "
                    + o.getRemainingUnits() + " left)");
        }
        options.add("W");
        return String.join(" | ", options);
    }

    private String describeCashier(Restaurant restaurant) {
        List<String> payers = new ArrayList<>();
        List<String> eaters = new ArrayList<>();
        for (Customer c : restaurant.getCustomersInRestaurant()) {
            if (c.getStatus() == CustomerStatus.READY_TO_PAY) payers.add("C" + c.getId());
            if (c.getStatus() == CustomerStatus.EATING) eaters.add("C" + c.getId());
        }
        String text = payers.isEmpty() ? "automatic, nobody ready to pay"
                : "automatic, will charge the oldest of " + String.join(", ", payers);
        if (!eaters.isEmpty()) text += "  (eating: " + String.join(", ", eaters) + ")";
        return text;
    }

    // =====================================================================
    // Reading the manager's choices
    // =====================================================================

    /** Reads one Waiter command. Rule checks happen in Waiter, not here. */
    public WaiterChoice askWaiterChoice(Restaurant restaurant) {
        while (true) {
            String line = readCommand("Waiter> ");
            if (line == null) return WaiterChoice.waitTask();
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toUpperCase(Locale.ROOT);
            if (cmd.equals("W") || cmd.equals("WAIT")) return WaiterChoice.waitTask();

            Integer id;
            switch (cmd) {
                case "S": case "SEAT":
                    id = readId(parts, idsOf(restaurant.getWaitingQueue().getCustomers()), "seat");
                    if (id != null) return WaiterChoice.seat(id);
                    break;
                case "O": case "ORDER":
                    id = readId(parts, idsOf(restaurant.getCustomersAwaitingOrder()), "take an order from");
                    if (id != null) return WaiterChoice.takeOrder(id);
                    break;
                case "V": case "SERVE":
                    id = readId(parts, customerIdsOf(restaurant.getReadyOrders()), "serve");
                    if (id != null) return WaiterChoice.serve(id);
                    break;
                default:
                    out.println("  Invalid: unknown Waiter command '" + parts[0]
                            + "'. Use S <id>, O <id>, V <id> or W.");
            }
        }
    }

    /** Reads one Chef command. Returns null for Wait. Rule checks happen in Chef. */
    public Order askChefChoice(Restaurant restaurant) {
        while (true) {
            String line = readCommand("Chef> ");
            if (line == null) return null;
            String[] parts = line.split("\\s+");
            String cmd = parts[0].toUpperCase(Locale.ROOT);
            if (cmd.equals("W") || cmd.equals("WAIT")) return null;

            String[] idParts = parts;
            if (cmd.matches("\\d+")) {                  // allow just "3" as well as "C 3"
                idParts = new String[] {"C", parts[0]};
            } else if (!cmd.equals("C") && !cmd.equals("COOK")) {
                out.println("  Invalid: unknown Chef command '" + parts[0] + "'. Use C <id> or W.");
                continue;
            }
            Integer id = readId(idParts, customerIdsOf(restaurant.getOrdersInKitchen()), "cook for");
            if (id == null) continue;

            Customer customer = restaurant.findCustomerById(id);
            Order order = customer == null ? null : restaurant.findOrderFor(customer);
            if (order == null) {
                out.println("  Invalid: C" + id + " has no order in the kitchen. Choose again.");
                continue;
            }
            return order;   // Chef.prepare decides whether this order can be advanced
        }
    }

    /** Prints a staff result. A failure means the manager will be asked again. */
    public void showActionResult(ActionResult result) {
        if (result.isSuccess()) {
            out.println("  " + result.getMessage());
        } else {
            out.println("  Invalid: " + result.getMessage() + " Choose again (task not used).");
        }
    }

    public void showDeparture(Customer customer) {
        out.println("  !! C" + customer.getId() + " reached 0 satisfaction and left without paying.");
    }

    // =====================================================================
    // End of turn and end of game (brief 3.5 and 3.2 step 6)
    // =====================================================================

    public void showTurnEnd(int turn, Restaurant restaurant) {
        out.println(LINE);
        out.println("End of turn " + turn + ":  served " + restaurant.getServedCount()
                + " | unhappy departures " + restaurant.getUnhappyDepartures()
                + " | revenue " + Money.format(restaurant.getRevenueCents())
                + " | avg satisfaction " + formatAverage(restaurant.getAverageSatisfaction())
                + " | turns used " + turn + "/" + maxTurns);
    }

    public void showFinalResult(boolean victory, Restaurant restaurant) {
        out.println();
        out.println("============================================================");
        out.println("                        GAME OVER");
        out.println("============================================================");
        out.println(check(resultEvaluator.meetsPaidTarget(restaurant))
                + " Paid customers     : " + restaurant.getPaidCount()
                + "  (need " + resultEvaluator.getTargetPaid() + ")");
        out.println(check(resultEvaluator.meetsRevenueTarget(restaurant))
                + " Revenue            : " + Money.format(restaurant.getRevenueCents())
                + "  (need " + Money.format(resultEvaluator.getTargetRevenueCents()) + ")");
        out.println(check(resultEvaluator.meetsSatisfactionTarget(restaurant))
                + " Average satisfaction: " + formatAverage(restaurant.getAverageSatisfaction())
                + "  (need " + resultEvaluator.getTargetAverageSatisfaction() + ")");
        out.println("     Served " + restaurant.getServedCount()
                + ", unhappy departures " + restaurant.getUnhappyDepartures()
                + ", turns used " + maxTurns + "/" + maxTurns);
        out.println();
        out.println(victory ? "*** VICTORY! The restaurant hit every target. ***"
                            : "*** DEFEAT. At least one target was missed. ***");
    }

    // =====================================================================
    // Helpers (formatting and parsing only)
    // =====================================================================

    /** @return the trimmed next non-empty line, or null if input has ended. */
    private String readCommand(String prompt) {
        while (true) {
            out.print(prompt);
            out.flush();
            if (!in.hasNextLine()) {
                out.println("(no more input, choosing Wait)");
                return null;
            }
            String line = in.nextLine().trim();
            int hash = line.indexOf('#');                 // allow comments in input files
            if (hash >= 0) line = line.substring(0, hash).trim();
            if (line.isEmpty()) {
                if (!echoInput) out.println("  Invalid: please type a command.");
                continue;
            }
            if (echoInput) out.println(line);
            return line;
        }
    }

    /**
     * Gets the customer ID from "X <id>". If the ID is missing and exactly one
     * customer fits, that customer is used. Returns null (after printing why)
     * when no usable ID was given.
     */
    private Integer readId(String[] parts, List<Integer> candidates, String verb) {
        if (parts.length >= 2) {
            String token = parts[1].toUpperCase(Locale.ROOT);
            if (token.startsWith("C")) token = token.substring(1);
            try {
                return Integer.parseInt(token);
            } catch (NumberFormatException e) {
                out.println("  Invalid: '" + parts[1] + "' is not a customer ID. Choose again.");
                return null;
            }
        }
        if (candidates.size() == 1) return candidates.get(0);
        if (candidates.isEmpty()) {
            out.println("  Invalid: there is no customer to " + verb + " right now. Choose again.");
        } else {
            out.println("  Invalid: say which customer to " + verb + ", e.g. " + parts[0]
                    + " " + candidates.get(0) + ". Choose again.");
        }
        return null;
    }

    /** e.g. "VIP C2". The type name comes from the customer object, not a type check. */
    private static String label(Customer customer) {
        return customer.getTypeName() + " C" + customer.getId();
    }

    private static List<Integer> idsOf(List<Customer> customers) {
        List<Integer> ids = new ArrayList<>();
        for (Customer c : customers) ids.add(c.getId());
        return ids;
    }

    private static List<Integer> customerIdsOf(List<Order> orders) {
        List<Integer> ids = new ArrayList<>();
        for (Order o : orders) ids.add(o.getCustomer().getId());
        return ids;
    }

    private static String formatAverage(OptionalDouble average) {
        return average.isPresent() ? String.format(Locale.ROOT, "%.1f", average.getAsDouble()) : "N/A";
    }

    private static String check(boolean ok) {
        return ok ? "[OK]" : "[--]";
    }
}
