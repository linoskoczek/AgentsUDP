package Utilities;

import Agent.Agent;

import java.io.IOException;
import java.net.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Objects;

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
            System.out.println("Sent a broadcast message to " + Settings.broadcastAddress);
        } catch (IOException e) {
            System.err.println("[Broadcast message] Could not send packet to " + packet.getAddress());
            e.printStackTrace();
        }

    }

    public static List<Integer> getLocalAddresses() throws SocketException {
        Enumeration<NetworkInterface> interfaces = NetworkInterface.getNetworkInterfaces();
        List<Integer> list = new ArrayList<>();
        NetworkInterface i;
        while (interfaces.hasMoreElements() && (i = interfaces.nextElement()) != null) {
            i.getInterfaceAddresses()
                    .stream()
                    .map(InterfaceAddress::getAddress)
                    .filter(Objects::nonNull)
                    .map(UDPTweaks::addressToInt)
                    .forEach(list::add);
        }
        return list;
    }

    public static int addressToInt(InetAddress address) {
        int result = 0;
        byte[] bytes = address.getAddress();
        for (int i = 0; i < bytes.length; i++)
            result |= ((bytes[i] & 0xFF) << (8 * i));
        return result;
    }
}
