package MultiClientServer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class MasterControlProgram {
    private static final String SERVER_ADDRESS = "localhost";
    private static final int PORT = 12345;

    public static void main(String[] args) {
        try {
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
                String command = userInputScanner.nextLine();
                if (command.strip().equalsIgnoreCase("pi")) {
                    writer.println(command);
                    // Wait for response from server
                    if (serverScanner.hasNextLine()) {
                        String response = serverScanner.nextLine();
                        double result=Double.parseDouble(response);
                        System.out.println("Result from server: " + response);
                        System.out.println("Deviation from Math.PI: "+Math.abs(Math.PI-result));
                    }
                } else if (command.strip().equalsIgnoreCase("quit")) {
                    writer.println(command);
                    break;
                } else {
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
