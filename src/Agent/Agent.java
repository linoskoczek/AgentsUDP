package Agent;

import Utilities.Settings;
import Utilities.UDPTweaks;

import java.io.IOException;
import java.net.*;
import java.util.List;

public class Agent {
    public static DatagramSocket serverSocket = null;
    private static List<Integer> localAddresses;

    static {
        try {
            localAddresses = UDPTweaks.getLocalAddresses();
        } catch (SocketException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        readParameters(args);
        Clock.startClock();
        startServer(Settings.agentPort);
    }

    private static void readParameters(String[] args) {
        if (args.length != 3) {
            System.err.println("You have to provide 3 arguments:\n" +
                    "(1) initial counter value\n" +
                    "(2) time period (in seconds)\n" +
                    "(3) broadcast IP address of the network");
            System.exit(1);
        }

        try {
            Clock.setInitialClock(Long.parseLong(args[0]));
        } catch (NumberFormatException e) {
            System.err.println("Provided initial counter value is too big! Maximum possible value is " + Long.MAX_VALUE);
            System.exit(1);
        }

        setTimePeriod(args[1]);

        try {
            Settings.broadcastAddress = InetAddress.getByName(args[2]);
        } catch (UnknownHostException e) {
            System.err.println("Broadcast is wrong: Unknown host exception.");
            e.printStackTrace();
            System.exit(1);
        }
    }

    @SuppressWarnings("InfiniteLoopStatement")
    private static void startServer(int port) {
        try {
            UDPTweaks.serverSocket = serverSocket = new DatagramSocket(port);
            serverSocket.setBroadcast(true);
        } catch (BindException e) {
            System.err.println("Could not open server socket on port " + port + " because port is already used.");
            System.exit(1);
        } catch (SocketException e) {
            System.err.println("Could not open server socket on port " + port);
            System.exit(1);
        }
        System.out.println("Server started on port " + Settings.agentPort);

        Thread synchronizer = new Thread(new Synchronizer(), "Agent.Synchronizer");
        synchronizer.start();

        byte[] buffer = new byte[64];
        while (true) {
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

        if (extended.length == 1)
            gotRequestAction(received, packet.getAddress());
        else if (extended.length == 3 && extended[0].equals("ANS"))
            gotAnswerAction(extended[1], extended[2]);
    }

    private static void gotRequestAction(String received, InetAddress address) {
        String command = received.substring(0, 3);
        String val;
        switch (command) {
            case "CLK":
                int intAddress = UDPTweaks.addressToInt(address);
                if (localAddresses.contains(intAddress)) {
                    break;
                }
                UDPTweaks.sendMessage(Clock.getClockValue(), address, "CLK");
                break;
            case "GCL":
                UDPTweaks.sendMessage(Clock.getClockValue(), address, "GCL", Settings.controllerPort);
                break;
            case "GTP":
                UDPTweaks.sendMessage(Settings.timePeriodBetweenSync, address, "GTP", Settings.controllerPort);
                break;
            case "WCL":
                val = received.substring(3, received.length());
                long longVal;
                try {
                    longVal = Long.parseLong(val);
                    if (val.length() < 1) throw new IllegalArgumentException();
                } catch (IllegalArgumentException e) {
                    System.err.println("Writing counter cannot be completed because wrong value has been sent");
                    UDPTweaks.sendMessage("ANS:ACK:ERR", address, Settings.controllerPort);
                    break;
                }
                Clock.setValue(Clock.getClockValue() - longVal);
                UDPTweaks.sendMessage("ANS:ACK:OK", address, Settings.controllerPort);
                break;
            case "WTP":
                val = received.substring(3, received.length());
                if (val.length() < 1) {
                    System.err.println("Writing time period cannot be completed because wrong value has been sent");
                    break;
                }
                boolean wasSet = setTimePeriod(val);
                UDPTweaks.sendMessage("ANS:ACK:" + (wasSet ? "OK" : "ERR"), address, Settings.controllerPort);
                break;
        }
    }

    private static void gotAnswerAction(String command, String value) {
        switch (command) {
            case "CLK":
                Synchronizer.addToClockSum(value);
                break;
        }

        System.out.println("Received CLK with value " + value);
    }

    private static boolean setTimePeriod(String timePeriod) {
        int time = 0;
        try {
            time = Integer.parseInt(timePeriod);
        } catch (NumberFormatException e) {
            System.err.println("Provided time period is too big!");
        }
        if (time <= Settings.timeToWaitForAnswers || time <= 0) {
            System.err.println("Time period between clock sync must be greater than time to wait for answers (" + Settings.timeToWaitForAnswers + ") and positive!");
            if (Settings.timePeriodBetweenSync == -1) System.exit(1);
            return false;
        }
        Settings.setTimePeriodBetweenSync(time);

        System.out.println("Time period set to " + timePeriod);
        return true;
    }
}
