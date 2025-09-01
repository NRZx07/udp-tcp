package UDP;
import java.net.*;
import java.util.*;

public class GameServer {
    private static int secretNumber;
    private static Map<String, Integer> clientAttempts = new HashMap<>();

    public static void main(String[] args) {
        secretNumber = new Random().nextInt(100) + 1;
        System.out.println("Secret number is: " + secretNumber); // For testing/debugging

        try (DatagramSocket socket = new DatagramSocket(6666)) {
            byte[] buffer = new byte[1024];

            while (true) {
                DatagramPacket request = new DatagramPacket(buffer, buffer.length);
                socket.receive(request);

                String received = new String(request.getData(), 0, request.getLength()).trim();
                String clientId = request.getAddress() + ":" + request.getPort();
                clientAttempts.putIfAbsent(clientId, 0);

                String response;

                try {
                    int guess = Integer.parseInt(received);
                    int attempts = clientAttempts.get(clientId) + 1;
                    clientAttempts.put(clientId, attempts);

                    if (guess < secretNumber) {
                        response = "HIGHER";
                    } else if (guess > secretNumber) {
                        response = "LOWER";
                    } else {
                        response = "CORRECT! Attempts: " + attempts;
                    }
                } catch (NumberFormatException e) {
                    response = "INVALID";
                }

                byte[] sendBuffer = response.getBytes();
                DatagramPacket reply = new DatagramPacket(sendBuffer, sendBuffer.length,
                        request.getAddress(), request.getPort());
                socket.send(reply);
            }

        } catch (Exception e) {
            System.err.println("Server Error: " + e.getMessage());
        }
    }
}