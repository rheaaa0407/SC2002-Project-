package restaurantrush;

public final class Money {
    private Money() {}

    // stores cents, prints dollars with two decimals
    public static String format(int cents) {
        return String.format("$%d.%02d", cents / 100, cents % 100);
    }

    /**
     * Price after taking {@code percent} percent off, rounded to the nearest cent
     * (halves round up). Works in whole cents, so there are no floating-point errors.
     * Examples: percentOff(1500, 10) = 1350 ($13.50), percentOff(1350, 20) = 1080 ($10.80).
     */
    public static int percentOff(int cents, int percent) {
        if (cents < 0) throw new IllegalArgumentException("Price cannot be negative");
        if (percent < 0 || percent > 100) throw new IllegalArgumentException("Percent must be 0 to 100");
        return (cents * (100 - percent) + 50) / 100;
    }
}
