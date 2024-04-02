package MultiClientServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

public class MultiClient {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int BASE_PORT = 60000;
    // Number of clients for the connection
    private static final int NUM_CLIENTS = 10;

    public static void main(String[] args) {
        for (int i = 0; i < NUM_CLIENTS; i++) {
            final int port = BASE_PORT + i;
            new Thread(() -> {
                try {
                    Socket socket = new Socket(SERVER_ADDRESS, port);
                    System.out.println("Connected to server on port " + port);

                    // Send to server
                    PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
                    Thread readerThread = new Thread(new ServerReader(socket, writer));
                    readerThread.start();

                    // Read input from console
                    Scanner userInputScanner = new Scanner(System.in);
                    while (true) {
                        String command = userInputScanner.nextLine();
                        if (command.equals("quit")) {
                            // Send quit command to server and terminate
                            writer.println(command);
                            userInputScanner.close();
                            socket.close();
                            break;
                        } else if (command.equals("pi")) {
                            // Send pi command to server
                            writer.println(command);
                        }
                    }
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
                        double approximationOfPi = calculatePi();
                        System.out.println("Approximation of port " + socket.getPort() + ": " + approximationOfPi);
                        // Send result back to server
                        writer.println(approximationOfPi);
                    } else if (command.strip().equalsIgnoreCase("quit")) {
                        socket.close();
                        System.out.println("Port " + socket.getPort() + " disconnected");
                        break;
                    } else {
                        System.out.println("Port " + socket.getPort() + " received from server: " + command);
                    }
                }
                System.exit(0);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private static double calculatePi() {
        // Calculate an approximation of PI with Monte Carlo
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
