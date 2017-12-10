import java.io.IOException;
import java.net.*;
import java.util.*;

public class UDPTweaks {

    public static void sendMessage(String data, InetAddress address) {
        byte[] buffer = data.getBytes();
        DatagramPacket packet = new DatagramPacket(buffer, buffer.length, address, Settings.port);
        try {
            Agent.serverSocket.send(packet);
        } catch (IOException e) {
            System.err.println("[Single message] Could not send packet to " + address);
            e.printStackTrace();
        }
    }

    public static void sendMessage(long clock, InetAddress address) {
        String data = "ANS:" + clock;
        sendMessage(data, address);
    }

    public static void sendBroadcastMessage(String data) {
        byte[] buffer = data.getBytes();

        List<InetAddress> broadcastAddresses = getBroadcastAddresses();
        DatagramPacket packet = new DatagramPacket(
                buffer, buffer.length, null, Settings.port);

        broadcastAddresses.stream()
                .filter(Objects::nonNull)
                .filter(address -> address.getAddress() != null)
                .forEach(address -> {
            packet.setAddress(address);
            try {
                Agent.serverSocket.send(packet);
            } catch (IOException e) {
                System.err.println("[Broadcast message] Could not send packet to " + address);
                e.printStackTrace();
            }
        });

    }

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
