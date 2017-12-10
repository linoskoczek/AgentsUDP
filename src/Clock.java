public class Clock {
    private static long startTime;
    private static long initialClock;

    public static void setInitialClock(long _initialClock) {
        if(_initialClock < 0) throw new IllegalArgumentException("Initial clock value must be bigger or equal zero!");
        initialClock = _initialClock;
    }

    public static long getClockValue() {
        return System.currentTimeMillis() - startTime + initialClock;
    }

    public static void startClock() {
        startTime = System.currentTimeMillis();
    }


    public static void setValue(long value) {
        initialClock -= value;
        System.out.println("[Clock]" + getClockValue());
    }
}
