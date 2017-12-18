package Utilities;

import Agent.Agent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

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
}
