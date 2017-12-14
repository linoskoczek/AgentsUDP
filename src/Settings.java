import java.net.InetAddress;

public class Settings {

    static final int port = 10000;
    static int timePeriod = 3; //in seconds
    static int timeToWaitForAnswers = 1;//in seconds
    static InetAddress broadcastAddress = null;

    public static void setTimePeriod(int timePeriod) {
        if(timePeriod <= 0) throw new IllegalArgumentException("Time period must be greater than zero!");
        Settings.timePeriod = timePeriod;
    }

}