import java.math.BigInteger;

public class Synchronizer implements Runnable {
    /* Synchronizer is a thread which works in background and synchronizes the clock periodically. */

    private static volatile BigInteger clockSum;
    private static volatile int numberOfAnswers;

    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException ignored) {}
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while(true) {
            Thread requestor = new Thread(this::sendClockRequests, "Requestor");
            requestor.start();
            System.out.println("Synchronization started.");
            sleep(Settings.timePeriod);
        }
    }

    private void sendClockRequests() {
        clockSum = new BigInteger("0");
        numberOfAnswers = 0;
        UDPTweaks.sendBroadcastMessage("CLK");
        //clockSum = clockSum.add(new BigInteger(String.valueOf(Clock.getClockValue())));
        sleep(Settings.timeToWaitForAnswers);

        long average = clockSum.divide(BigInteger.valueOf(numberOfAnswers)).longValue();
        Clock.setValue(Clock.getClockValue() - average);
        System.out.println("Synchronization finished.");
    }

    public static void addToClockSum(String clock) {
        numberOfAnswers++;
        clockSum = clockSum.add(new BigInteger(clock));
    }
}
