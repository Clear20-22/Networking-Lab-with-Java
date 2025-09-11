import java.io.IOException;

public class MultiClientLauncher {
    public static void main(String[] args) {
        int numClients = 2; // Default number of clients
        if (args.length > 0) {
            try {
                numClients = Integer.parseInt(args[0]);
            } catch (NumberFormatException e) {
                System.out.println("Invalid number, using default: 2");
            }
        }

        System.out.println("Launching " + numClients + " clients...");

        for (int i = 1; i <= numClients; i++) {
            try {
                ProcessBuilder pb = new ProcessBuilder("java", "Client_17_23");
                pb.inheritIO(); // Inherit terminal I/O for interaction
                Process p = pb.start();
                System.out.println("Client " + i + " started (PID: " + p.pid() + ")");
            } catch (IOException e) {
                System.out.println("Failed to start client " + i + ": " + e.getMessage());
            }
        }

        System.out.println("All clients launched. Interact with them in the terminal.");
    }
}