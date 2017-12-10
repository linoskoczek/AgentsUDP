public class Settings {

    static final int port = 10000;
    static int timePeriod = 5; //in seconds
    static int timeToWaitForAnswers = 1;//in seconds
    //exception here, timePeriod must be greater than timeToWaitForAnswers

    public static void setTimePeriod(int timePeriod) {
        if(timePeriod <= 0) throw new IllegalArgumentException("Time period must be greater than zero!");
        Settings.timePeriod = timePeriod;
    }

}