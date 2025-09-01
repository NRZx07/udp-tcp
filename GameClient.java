package UDP;
import java.net.*;
import java.util.*;

public class GameClient {
    public static void main(String[] args) {
        try (DatagramSocket socket = new DatagramSocket()) {
            socket.setSoTimeout(3000); // 3-second timeout
            InetAddress serverAddress = InetAddress.getByName("localhost");

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.print("Enter your guess (1-100): ");
                String guess = scanner.nextLine();

                byte[] sendBuffer = guess.getBytes();
                DatagramPacket request = new DatagramPacket(sendBuffer, sendBuffer.length, serverAddress, 6666);
                socket.send(request);

                byte[] receiveBuffer = new byte[1024];
                DatagramPacket response = new DatagramPacket(receiveBuffer, receiveBuffer.length);

                try {
                    socket.receive(response);
                    String reply = new String(response.getData(), 0, response.getLength());
                    System.out.println("Server: " + reply);

                    if (reply.startsWith("CORRECT")) {
                        break;
                    }

                } catch (SocketTimeoutException e) {
                    System.out.println("Timeout - no response from server");
                }
            }

        } catch (Exception e) {
            System.err.println("Client Error: " + e.getMessage());
        }
    }
}