package Agent;

import Utilities.Settings;
import Utilities.UDPTweaks;

import java.math.BigInteger;

public class Synchronizer implements Runnable {
    /* Agent.Synchronizer is a thread which works in background and synchronizes the clock periodically. */

    private static volatile BigInteger clockSum;
    private static volatile int numberOfAnswers;
    private static volatile long syncStart;

    static void addToClockSum(String clock) {
        numberOfAnswers++;
        long restOfWaiting = (Settings.timeToWaitForAnswers * 1000 - (System.currentTimeMillis() - syncStart));
        clockSum = clockSum
                .add(new BigInteger(clock))
                .add(new BigInteger(String.valueOf(restOfWaiting)));
    }

    private void sleep(int time) {
        try {
            Thread.sleep(time * 1000);
        } catch (InterruptedException ignored) {
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    @Override
    public void run() {
        while (true) {
            Thread requestor = new Thread(this::sendClockRequests, "Requestor");
            requestor.start();
            sleep(Settings.timePeriodBetweenSync);
        }
    }

    private void sendClockRequests() {
        syncStart = System.currentTimeMillis();
        clockSum = new BigInteger("0");
        numberOfAnswers = 0;
        UDPTweaks.sendBroadcastMessage("CLK");
        sleep(Settings.timeToWaitForAnswers);

        addToClockSum(String.valueOf(Clock.getClockValue()));

        if (numberOfAnswers < 2) {
            System.err.println("No other agent connected (or connection error!)");
        }
        long average = clockSum.divide(BigInteger.valueOf(numberOfAnswers)).longValue();
        Clock.setValue(Clock.getClockValue() - average);
    }
}
