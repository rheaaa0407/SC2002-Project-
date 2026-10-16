package restaurantrush;

public final class Money {
    private Money() {}

    // stores cents, prints dollars with two decimals
    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }
}
