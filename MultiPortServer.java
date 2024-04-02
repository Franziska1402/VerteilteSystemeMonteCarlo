package MultiClientServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class MultiPortServer {
    // Master control program port
    private static final int MASTER_PORT = 12345;

    // Range for possible ports
    private static final int START_PORT = 60000;
    private static final int END_PORT = 60500;
    // Send to connected clients
    private static final List<PrintWriter> clientOutputStreams = new ArrayList<>();
    // Results from clients for average
    private static final List<Double> clientResults = new ArrayList<>();
    // Send result to master control program
    private static PrintWriter masterControlWriter = null;

    public static void main(String[] args) {
        new Thread(new PortListener(MASTER_PORT)).start();
        for (int port = START_PORT; port <= END_PORT; port++) {
            new Thread(new PortListener(port)).start();
        }
    }

    static class PortListener implements Runnable {
        private final int port;

        public PortListener(int port) {
            this.port = port;
        }

        public void run() {
            // Open server port for connection
            try (ServerSocket serverSocket = new ServerSocket(port)) {
                System.out.println("Server is running on port " + port);

                while (true) {
                    // Client connected
                    Socket clientSocket = serverSocket.accept();
                    System.out.println("New client connected on port " + port + ": " + clientSocket);

                    // Writer to send task to the clients
                    PrintWriter writer = new PrintWriter(clientSocket.getOutputStream());
                    if (port != MASTER_PORT) {
                        synchronized (clientOutputStreams) {
                            clientOutputStreams.add(writer);
                        }
                    } else {
                        masterControlWriter = writer;
                    }

                    Thread t = new Thread(new ClientHandler(clientSocket, port));
                    t.start();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    static class ClientHandler implements Runnable {
        private final Socket socket;
        private final Scanner input;
        private final int port;

        public ClientHandler(Socket clientSocket, int port) throws IOException {
            this.socket = clientSocket;
            // Input from client
            this.input = new Scanner(socket.getInputStream());
            this.port = port;
        }

        public void run() {
            try {
                while (input.hasNextLine()) {
                    String message = input.nextLine();
                    if (port == MASTER_PORT) {
                        // Task from master control -> here only calculation of pi
                        System.out.println("==============================================");
                        System.out.println("Master Control Program sent task: " + message);
                        if (message.strip().equalsIgnoreCase("quit")) {
                            disconnectClients();
                            if (clientOutputStreams.size() == 0) {
                                System.exit(0);
                            }
                        } else if (message.strip().equalsIgnoreCase("pi")) {
                            // Send task to all connected clients
                            broadcastFromMasterControl(message);
                        }
                    } else {
                        // Processing result from client
                        System.out.println("Received double from client on port " + port + ": " + message);
                        if (!message.equals("quit")) {
                            double result;
                            try {
                                result = Double.parseDouble(message);
                                synchronized (clientResults) {
                                    clientResults.add(result);
                                }
                            } catch (NumberFormatException e) {
                                // Ignore if no double was sent
                                continue;
                            }
                        } else {
                            break; // Client sent "quit", terminate this handler
                        }
                    }

                    if (port != MASTER_PORT && clientResults.size() == clientOutputStreams.size()) {
                        double result = calculateAverageResult();
                        sendResultToMasterControl(result);
                        synchronized (clientResults) {
                            clientResults.clear();
                        }
                    }
                }
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void broadcastFromMasterControl(String task) {
        // Send to all connected clients through their writer
        synchronized (clientOutputStreams) {
            for (PrintWriter writer : clientOutputStreams) {
                try {
                    writer.println(task);
                    writer.flush();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private static void sendResultToMasterControl(double result) {
        System.out.println("==============================================");
        System.out.println("Result send to master: " + result + "\n");
        try {
            masterControlWriter.println(result);
            masterControlWriter.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static double calculateAverageResult() {
        double average = 0.0;
        for (double result : clientResults) {
            average += result;
        }
        return average / clientResults.size();
    }

    private static void disconnectClients() {
        synchronized (clientOutputStreams) {
            for (PrintWriter writer : clientOutputStreams) {
                writer.println("quit");
                writer.flush();
            }
        }
        clientOutputStreams.clear();
        clientResults.clear();
        System.out.println("All clients disconnected");
    }
}
