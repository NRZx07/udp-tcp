package TCP;
import java.io.*;
import java.net.*;

public class ChatClient {
    public static void main(String[] args) {
        try (Socket socket = new Socket("localhost", 5555);
             BufferedReader userInput = new BufferedReader(new InputStreamReader(System.in));
             BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
             PrintWriter out = new PrintWriter(socket.getOutputStream(), true)) {

            // Thread to read messages from server
            Thread readerThread = new Thread(() -> {
                String serverMsg;
                try {
                    while ((serverMsg = in.readLine()) != null) {
                        System.out.println("Server: " + serverMsg);
                    }
                } catch (IOException e) {
                    System.out.println("Disconnected from server.");
                }
            });

            // Thread to send user input to server
            Thread writerThread = new Thread(() -> {
                String userMsg;
                try {
                    while ((userMsg = userInput.readLine()) != null) {
                        if (userMsg.equalsIgnoreCase("EXIT")) {
                            socket.close();
                            break;
                        }
                        out.println(userMsg);
                    }
                } catch (IOException e) {
                    // Ignore
                }
            });

            readerThread.start();
            writerThread.start();

            // Wait for both threads to finish
            readerThread.join();
            writerThread.join();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}