package Utilities;

import java.net.InetAddress;

public class Settings {
    /* You can change those */
    public static final int agentPort = 10000;
    public static final int controllerPort = 15000;
    public static int timeToWaitForAnswers = 5;//in seconds

    /* Don't touch these */
    public static int timePeriodBetweenSync = -1; //in seconds
    public static InetAddress broadcastAddress = null;

    public static void setTimePeriodBetweenSync(int timePeriodBetweenSync) {
        if (timePeriodBetweenSync <= 0) throw new IllegalArgumentException("Time period must be greater than zero!");
        Settings.timePeriodBetweenSync = timePeriodBetweenSync;
    }

}