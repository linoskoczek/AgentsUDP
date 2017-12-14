import java.io.IOException;
import java.net.*;

public class Agent {
    static DatagramSocket serverSocket = null;

    public static void main(String[] args) {
        readParameters(args);
        Clock.startClock();
        startServer(Settings.port);
    }

    private static void readParameters(String[] args) {
        if(args.length != 3) {
            System.err.println("You have to provide 3 arguments:\n" +
                    "(1) initial counter value\n" +
                    "(2) time period (in seconds)\n" +
                    "(3) broadcast IP address of the network");
            System.exit(1);
        }

        try {
            Clock.setInitialClock(Long.parseUnsignedLong(args[0]));
        } catch (NumberFormatException e) {
            System.err.println("Provided initial counter value is too big! Maximum possible value is " + Long.MAX_VALUE);
            System.exit(1);
        }

        if(Integer.parseInt(args[1]) < Settings.timeToWaitForAnswers) {
            System.err.println("Time period between clock sync must be lower than time to wait for answers ("+Settings.timeToWaitForAnswers+")!");
            System.exit(1);
        }

        try {
            Settings.broadcastAddress = InetAddress.getByName(args[2]);
        } catch (UnknownHostException e) {
            System.err.println("Broadcast is wrong: Unknown host exception.");
            e.printStackTrace();
            System.exit(1);
        }

        Settings.setTimePeriod(Integer.parseInt(args[1]));
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private static void startServer(int port) {
        try {
            serverSocket = new DatagramSocket(port);
            serverSocket.setBroadcast(true);
        } catch (BindException e) {
            System.err.println("Could not open server socket on port " + port + " because port is already used.");
            System.exit(1);
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
        else if(extended.length == 3 && extended[0].equals("ANS"))
            gotAnswerAction(extended[1], extended[2]);
    }

    private static void gotRequestAction(String received, InetAddress address) {
        switch(received) {
            case "CLK":
                UDPTweaks.sendMessage(Clock.getClockValue(), address, "CLK");
                break;
        }
    }

    private static void gotAnswerAction(String command, String value) {
        switch(command) {
            case "CLK":
                Synchronizer.addToClockSum(value);
                break;
            case "SET":
                Clock.setValue(Clock.getClockValue() - Long.parseLong(value));
        }

        System.out.println("Received CLK with value " + value);
    }
}
