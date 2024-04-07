package MultiClientServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class MultiClient {
    private static final String SERVER_ADDRESS = "localhost";
    // Base port number for clients
    private static final int BASE_PORT = 60000;
    // Number of clients for the connection
    private static final int NUM_CLIENTS = 10;

    public static void main(String[] args) {
        // Connect multiple clients to the server
        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int port = BASE_PORT + i;
            new Thread(() -> {
                try {
                    // Connect to the server
                    Socket socket = new Socket(SERVER_ADDRESS, port);
                    System.out.println("Connected to server on port " + port);

                    // Send to server
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    Thread readerThread = new Thread(new ServerReader(socket, writer));
                    readerThread.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }).start();
        }
    }

    static class ServerReader implements Runnable {
        private Socket socket;
        private PrintWriter writer;

        public ServerReader(Socket socket, PrintWriter writer) {
            this.socket = socket;
            this.writer = writer;
        }

        public void run() {
            try {
                // Read task from server
                Scanner reader = new Scanner(socket.getInputStream());
                while (reader.hasNextLine()) {
                    String command = reader.nextLine();
                    if (command.strip().equalsIgnoreCase("pi")) {
                        // Calculate approximation of Pi
                        double approximationOfPi = calculatePi();
                        System.out.println("Approximation of port " + socket.getPort() + ": " + approximationOfPi);
                        // Send result back to server
                        writer.println(approximationOfPi);
                    } else if (command.strip().equalsIgnoreCase("quit")) {
                        // Close the socket and exit thread
                        socket.close();
                        break;
                    } else {
                        // Print received message from server
                        System.out.println("Port " + socket.getPort() + " received from server: " + command);
                    }
                }
                // Exit client application after handling server task
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    // Method to calculate Pi approximation using Monte Carlo method
    private static double calculatePi() {
        Random random = new Random();
        int totalPoints = 10000000;
        int insideCircle = 0;

        for (int i = 0; i < totalPoints; i++) {
            double x = random.nextDouble();
            double y = random.nextDouble();
            double distance = Math.sqrt(x * x + y * y);
            if (distance <= 1) {
                insideCircle++;
            }
        }
        return 4.0 * insideCircle / totalPoints;
    }
}
