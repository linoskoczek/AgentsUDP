package Agent;

import Utilities.Settings;
import Utilities.UDPTweaks;

import java.math.BigInteger;

public class Synchronizer implements Runnable {
    /* Agent.Synchronizer is a thread which works in background and synchronizes the clock periodically. */

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
            sleep(Settings.timePeriodBetweenSync);
        }
    }

    private void sendClockRequests() {
        clockSum = new BigInteger("0");
        numberOfAnswers = 0;
        UDPTweaks.sendBroadcastMessage("CLK");
        sleep(Settings.timeToWaitForAnswers);

        if(numberOfAnswers == 0) {
            System.err.println("No data received - check your connection.");
            return;
        }
        long average = clockSum.divide(BigInteger.valueOf(numberOfAnswers)).longValue();
        Clock.setValue(Clock.getClockValue() - average);
    }

    static void addToClockSum(String clock) {
        numberOfAnswers++;
        clockSum = clockSum.add(new BigInteger(clock));
    }
}
