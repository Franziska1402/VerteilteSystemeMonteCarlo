package MultiClientServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MasterControlProgram {
    // Server address and port
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try {
            // Connect to the server
            Socket socket = new Socket(SERVER_ADDRESS, PORT);
            System.out.println("Connected to server");

            // Send command to server
            PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
            // Result from server
            Scanner serverScanner = new Scanner(socket.getInputStream());
            // Read user input
            Scanner userInputScanner = new Scanner(System.in);

            while (true) {
                System.out.println("==============================================");
                System.out.println("Enter a command for the server ('pi', 'quit'):");
                // Get user input
                String command = userInputScanner.nextLine();
                if (command.strip().equalsIgnoreCase("pi")) {
                    // Send 'pi' command to server
                    writer.println(command);
                    // Wait for response from server
                    if (serverScanner.hasNextLine()) {
                        String response = serverScanner.nextLine();
                        double result = Double.parseDouble(response);
                        // Print result and deviation from Math.PI
                        System.out.println("Result from server: " + response);
                        System.out.println("Deviation from Math.PI: " + Math.abs(Math.PI - result));
                    }
                } else if (command.strip().equalsIgnoreCase("quit")) {
                    // Send 'quit' command to server
                    writer.println(command);
                    break;
                } else {
                    // Unknown command
                    System.out.println("Unknown command");
                }
            }

            // Close resources
            userInputScanner.close();
            serverScanner.close();
            writer.close();
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
