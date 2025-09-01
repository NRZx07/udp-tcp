package UDP;
import java.net.*;
import java.util.*;
import java.io.*;

public class PeerClient {
    private static String myName;
    private static int myPort;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        myPort = 8000 + new Random().nextInt(1000);

        System.out.print("Enter your peer name: ");
        myName = scanner.nextLine();

        try (DatagramSocket socket = new DatagramSocket(myPort)) {
            // Register with registry
            String regMsg = "REGISTER:" + myName;
            sendToRegistry(socket, regMsg);

            // Start listener for P2P messages
            new MessageListener(socket).start();

            while (true) {
                System.out.print("Command (LIST, LOOKUP:name, MSG:name:message): ");
                String input = scanner.nextLine();

                if (input.equalsIgnoreCase("LIST") || input.startsWith("LOOKUP:")) {
                    sendToRegistry(socket, input);
                } else if (input.startsWith("MSG:")) {
                    String[] parts = input.split(":", 3);
                    if (parts.length != 3) {
                        System.out.println("Invalid MSG format.");
                        continue;
                    }

                    String peerName = parts[1];
                    String message = parts[2];

                    String peerInfo = lookupPeer(socket, peerName);
                    if (peerInfo == null) {
                        System.out.println("Peer not found.");
                        continue;
                    }

                    String[] addrParts = peerInfo.split(":");
                    InetAddress peerAddr = InetAddress.getByName(addrParts[1]);
                    int peerPort = Integer.parseInt(addrParts[2]);

                    byte[] msgBytes = (myName + ": " + message).getBytes();
                    DatagramPacket msgPacket = new DatagramPacket(msgBytes, msgBytes.length, peerAddr, peerPort);
                    socket.send(msgPacket);
                }
            }

        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }

    private static void sendToRegistry(DatagramSocket socket, String msg) throws Exception {
        InetAddress registryAddr = InetAddress.getByName("localhost");
        byte[] sendBuf = msg.getBytes();
        DatagramPacket packet = new DatagramPacket(sendBuf, sendBuf.length, registryAddr, 7777);
        socket.send(packet);

        byte[] recvBuf = new byte[1024];
        DatagramPacket response = new DatagramPacket(recvBuf, recvBuf.length);
        socket.receive(response);
        String reply = new String(response.getData(), 0, response.getLength());
        System.out.println("Registry: " + reply);
    }

    private static String lookupPeer(DatagramSocket socket, String peerName) throws Exception {
        sendToRegistry(socket, "LOOKUP:" + peerName);

        byte[] buf = new byte[1024];
        DatagramPacket reply = new DatagramPacket(buf, buf.length);
        socket.receive(reply);
        String result = new String(reply.getData(), 0, reply.getLength());

        if (result.startsWith("PEER:")) {
            return result;
        } else {
            return null;
        }
    }

    static class MessageListener extends Thread {
        private DatagramSocket socket;

        public MessageListener(DatagramSocket socket) {
            this.socket = socket;
        }

        public void run() {
            byte[] buffer = new byte[1024];
            while (true) {
                try {
                    DatagramPacket packet = new DatagramPacket(buffer, buffer.length);
                    socket.receive(packet);
                    String msg = new String(packet.getData(), 0, packet.getLength());
                    System.out.println("P2P Message: " + msg);
                } catch (IOException e) {
                    System.out.println("Listener stopped.");
                    break;
                }
            }
        }
    }
}