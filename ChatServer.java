package TCP;
import java.io.*;
import java.net.*;
import java.util.*;

public class ChatServer {
    //stores all client outputstreams
    private static Set<PrintWriter> clientWriters = Collections.synchronizedSet(new HashSet<>());

    public static void main(String[] args) {
        System.out.println("Chat server started on port 5555...");  // starting on port 5555

        try (ServerSocket serverSocket = new ServerSocket(5555)) {  //keep accept client
            while (true) {
                Socket clientSocket = serverSocket.accept();  // accept new client
                PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                clientWriters.add(out);
                out.println("Welcome to chat!");
                new ClientHandler(clientSocket, out).start();
            }
        } catch (IOException e) {   // for exception handling
            e.printStackTrace();
        }
    }

    static class ClientHandler extends Thread {
        private Socket socket;
        private PrintWriter out;
        private BufferedReader in;

        public ClientHandler(Socket socket, PrintWriter out) {
            this.socket = socket;
            this.out = out;
            try {
                this.in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        public void run() {
            String message;
            try {
                while ((message = in.readLine()) != null) {
                    synchronized (clientWriters) {
                        for (PrintWriter writer : clientWriters) {
                            if (writer != out) {
                                writer.println(message);
                            }
                        }
                    }
                }
            } catch (IOException e) {
                System.out.println("Client disconnected.");
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                clientWriters.remove(out);
            }
        }
    }
}