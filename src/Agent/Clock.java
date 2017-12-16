package Agent;

class Clock {
    private static long startTime;
    private static long initialClock;

    static void setInitialClock(long _initialClock) {
        if (_initialClock < 0) throw new IllegalArgumentException("Initial clock value must be bigger or equal zero!");
        initialClock = _initialClock;
    }

    static long getClockValue() {
        return System.currentTimeMillis() - startTime + initialClock;
    }

    static void startClock() {
        startTime = System.currentTimeMillis();
    }

    static void setValue(long value) {
        initialClock -= value;
        System.out.println("[CLK] " + getClockValue());
    }

}
