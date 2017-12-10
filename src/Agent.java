import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;

public class Agent {
    static DatagramSocket serverSocket = null;

    public static void main(String[] args) {
        readParameters(args);
        Clock.startClock();
        startServer(Settings.port);
    }

    private static void readParameters(String[] args) {
        if(args.length != 2) {
            System.err.println("You have to provide 2 arguments: (1) initial counter value (2) time period (in seconds)");
            System.exit(1);
        }

        Clock.setInitialClock(Integer.parseInt(args[0]));

        if(Integer.parseInt(args[1]) < Settings.timeToWaitForAnswers) {
            System.err.println("Time period between clock sync must be lower than time to wait for answers ("+Settings.timeToWaitForAnswers+")!");
            System.exit(1);
        }

        Settings.setTimePeriod(Integer.parseInt(args[1]));
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private static void startServer(int port) {
        try {
            serverSocket = new DatagramSocket(port);
            serverSocket.setBroadcast(true);
        } catch (SocketException e) {
            System.err.println("Could not open server socket on port " + port);
            e.printStackTrace();
        }
        System.out.println("Server started on port " + Settings.port);

        Thread synchronizer = new Thread(new Synchronizer(), "Synchronizer");
        synchronizer.start();

        while(true) {
            byte[] buffer = new byte[64];
            DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
            try {
                serverSocket.receive(packet);
                receivedAction(packet);
            } catch (IOException e) {
                System.err.println("Error while receiving packet - ignoring then.");
                e.printStackTrace();
            }
        }
    }

    private static void receivedAction(DatagramPacket packet) {
        String received = new String(packet.getData(), 0, packet.getLength());

        String[] extended = received.split(":");

        if(extended.length == 1)
            gotRequestAction(received, packet.getAddress());
        else if(extended.length == 2 && extended[0].equals("ANS"))
            gotAnswerAction(extended[1]);
    }

    private static void gotRequestAction(String received, InetAddress address) {
        switch(received) {
            case "CLK":
                UDPTweaks.sendMessage(Clock.getClockValue(), address);
                break;
        }
    }

    private static void gotAnswerAction(String answer) {
        Synchronizer.addToClockSum(answer);
    }
}
