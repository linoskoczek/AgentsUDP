package Utilities;

import Agent.Agent;

import java.io.IOException;
import java.net.*;
import java.util.*;

public class UDPTweaks {

    public static DatagramSocket serverSocket = null;

    public static void sendMessage(String data, InetAddress address, int port) {
        byte[] buffer = data.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, port);
        try {
            serverSocket.send(packet);
        } catch (IOException e) {
            System.err.println("[Single message] Could not send packet to " + address);
            e.printStackTrace();
        }
    }

    public static void sendMessage(long clock, InetAddress address, String command) {
        String data = "ANS:" + command + ":" + clock;
        sendMessage(data, address, Settings.agentPort);
    }

    public static void sendMessage(long clock, InetAddress address, String command, int port) {
        String data = "ANS:" + command + ":" + clock;
        sendMessage(data, address, port);
    }

    public static void sendBroadcastMessage(String data) {
        byte[] buffer = data.getBytes();

        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, null, Settings.agentPort);

        packet.setAddress(Settings.broadcastAddress);
        try {
            Agent.serverSocket.send(packet);
            System.out.println("Sent a broadcast message to " + Settings.broadcastAddress + " with clk val");
        } catch (IOException e) {
            System.err.println("[Broadcast message] Could not send packet to " + packet.getAddress());
            e.printStackTrace();
        }

    }

    /* Additional feauture to create a list of all available broadcast addresses on a machine */
    private static List<InetAddress> getBroadcastAddresses() {
        List<InetAddress> broadcastAddresses = new ArrayList<>();

        try {
            Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
            Collections.list(interfaces).forEach(networkInterface -> {
                List<InterfaceAddress> interfaceAddresses = networkInterface.getInterfaceAddresses();
                interfaceAddresses.parallelStream()
                        .filter(Objects::nonNull)
                        .forEach(interfaceAddress ->
                                broadcastAddresses.add(interfaceAddress.getBroadcast()));
            });

        } catch (SocketException e) {
            e.printStackTrace();
        }
        return broadcastAddresses;
    }
}
