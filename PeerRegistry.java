package UDP;
import java.net.*;
import java.util.*;

public class PeerRegistry {
    private static Map<String, String> peers = new HashMap<>(); // name -> address:port

    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket(7777)) {
            byte[] buffer = new byte[1024];
            System.out.println("Peer Registry started on port 7777");

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String msg = new String(request.getData(), 0, request.getLength()).trim();
                String response = "";

                if (msg.startsWith("REGISTER:")) {
                    String name = msg.substring(9).trim();
                    String addr = request.getAddress().getHostAddress() + ":" + request.getPort();
                    peers.put(name, addr);
                    response = "REGISTERED:" + name;
                } else if (msg.equals("LIST")) {
                    StringBuilder sb = new StringBuilder();
                    for (Map.Entry<String, String> entry : peers.entrySet()) {
                        sb.append("PEER:").append(entry.getKey()).append(":")
                                .append(entry.getValue()).append("\n");
                    }
                    response = sb.toString().trim();
                } else if (msg.startsWith("LOOKUP:")) {
                    String name = msg.substring(7).trim();
                    if (peers.containsKey(name)) {
                        response = "PEER:" + name + ":" + peers.get(name);
                    } else {
                        response = "NOT_FOUND";
                    }
                } else {
                    response = "INVALID_COMMAND";
                }

                byte[] sendBuf = response.getBytes();
                DatagramPacket reply = new DatagramPacket(sendBuf, sendBuf.length,
                        request.getAddress(), request.getPort());
                socket.send(reply);
            }

        } catch (Exception e) {
            System.err.println("Registry Error: " + e.getMessage());
        }
    }
}