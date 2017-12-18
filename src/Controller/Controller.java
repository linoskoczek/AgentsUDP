package Controller;

import Utilities.Settings;
import Utilities.UDPTweaks;

import java.io.IOException;
import java.net.*;

public class Controller {
    private static DatagramSocket serverSocket = null;
    private static volatile String lastAnswer;

    public static void main(String[] args) {
        readParameters(args);
    }

    private static void readParameters(String[] args) {
        if (args.length != 3 && args.length != 4) {
            System.err.println("You have to provide at least 3 arguments!\n\n" +
                    "For reading CLK value:\n" +
                    "(1) ip address of an agent \n" +
                    "(2) keyword 'get'\n" +
                    "(3) value to be read (counter/period)\n\n" +
                    "For writing CLK value:\n" +
                    "(1) ip address of an agent" +
                    "(2) keyword 'set'\n" +
                    "(3) value to be changed (counter/period)" +
                    "(4) new value");
            System.exit(1);
        }

        try {
            UDPTweaks.serverSocket = serverSocket = new DatagramSocket(Settings.controllerPort);
            serverSocket.setBroadcast(true);
        } catch (SocketException e) {
            System.err.println("Could not open server socket");
            System.exit(1);
        }

        System.out.println("Welcome to the controller. You will " + args[1] + " a " + args[2] + " value!");

        try {
            InetAddress address = InetAddress.getByName(args[0]);
            if (args.length == 3 && args[1].equals("get")) {
                read(args[2], address);
            } else if (args.length == 4 && args[1].equals("set")) {
                write(args[2], args[3], address);
            } else {
                System.err.println("Given parameters are not correct");
                System.exit(1);
            }
        } catch (UnknownHostException e) {
            System.err.println("Given IP address is wrong.");
            System.exit(1);
        }
    }

    private static void write(String valueToBeChanged, String newValue, InetAddress address) {
        String data = prepareData("W", valueToBeChanged) + newValue;
        Thread receiver = new Thread(Controller::receive);
        receiver.start();
        UDPTweaks.sendMessage(data, address, Settings.agentPort);
        while (receiver.isAlive()) {
            try {
                Thread.sleep(5);
            } catch (InterruptedException ignored) {
            }
        }
        showAnswer();
    }

    private static void read(String valueToBeRead, InetAddress address) {
        String data = prepareData("G", valueToBeRead);
        Thread receiver = new Thread(Controller::receive);
        receiver.start();
        UDPTweaks.sendMessage(data, address, Settings.agentPort);
        while (receiver.isAlive()) {
            try {
                Thread.sleep(5000);
            } catch (InterruptedException ignored) {
            }
        }
        showAnswer();
    }

    private static void showAnswer() {
        if (lastAnswer != null) {
            try {
                System.out.println("Received answer: " + lastAnswer.split(":")[2]);
            } catch (ArrayIndexOutOfBoundsException e) {
                System.err.println("Answer is incorrect.");
            }
        } else {
            System.err.println("Didn't receive answer...");
        }
    }

    private static String prepareData(String command, String value) {
        String data = command;
        switch (value) {
            case "counter":
                data += "CL"; //clock
                break;
            case "period":
                data += "TP"; //time period
                break;
            default:
                System.err.println("You cannot read anything else apart from counter or period (3rd argument)");
                System.exit(1);
        }
        return data;
    }

    private static void receive() {
        byte[] buffer = new byte[64];
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
        try {
            Controller.serverSocket.setSoTimeout(Settings.timeToWaitForAnswers);
            Controller.serverSocket.receive(packet);
            lastAnswer = new String(packet.getData(), 0, packet.getLength());
        } catch (IOException e) {
            System.err.println("Error while receiving packet.");
        }
    }
}