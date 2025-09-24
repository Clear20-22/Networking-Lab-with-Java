import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client {
    public static final String SERVER_HOST = "192.168.137.80";
    public static final int SERVER_PORT = 9000;

    public static void main(String[] args) {
        System.out.println("Connecting to server " + SERVER_HOST + ":" + SERVER_PORT);
        try (Scanner sc = new Scanner(System.in);
             Socket socket = new Socket(SERVER_HOST, SERVER_PORT)) {
            DataInputStream dis = new DataInputStream(socket.getInputStream());
            DataOutputStream dos = new DataOutputStream(socket.getOutputStream());

            // Read server welcome / commands info (UTF)
            String welcome = dis.readUTF();
            System.out.println("Server: " + welcome);

            boolean running = true;
            while (running) {
                System.out.println("\nChoose an option:\n1) List server files\n2) Download file\n3) Quit");
                System.out.print("Enter choice: ");
                String choice = sc.nextLine().trim();
                if (choice.equals("1")) {
                    dos.writeUTF("LIST");
                    dos.flush();
                    // Read until END_OF_LIST (UTF)
                    System.out.println("Server files:");
                    while (true) {
                        String entry = dis.readUTF();
                        if ("END_OF_LIST".equals(entry)) break;
                        System.out.println("  " + entry);
                    }
                } else if (choice.equals("2")) {
                    System.out.print("Enter path to download (e.g. Server file list/testfile.txt): ");
                    String fileName = sc.nextLine().trim();
                    dos.writeUTF("GET " + fileName);
                    dos.flush();

                    String response = dis.readUTF();
                    if (response != null && response.equals("FOUND")) {
                        long fileSize = dis.readLong();
                        System.out.println("Server found file. Size: " + fileSize + " bytes. Downloading...");

                        // Ensure client download folder exists and save using basename only
                        File clientDownloadDir = new File("Client downloaded list");
                        if (!clientDownloadDir.exists()) {
                            clientDownloadDir.mkdirs();
                        }
                        String baseName = new File(fileName).getName();
                        File outFile = new File(clientDownloadDir, "downloaded_" + baseName);
                        try (FileOutputStream fos = new FileOutputStream(outFile)) {
                            byte[] buffer = new byte[8192];
                            long remaining = fileSize;
                            while (remaining > 0) {
                                int read = dis.read(buffer, 0, (int)Math.min(buffer.length, remaining));
                                if (read == -1) break;
                                fos.write(buffer, 0, read);
                                remaining -= read;
                            }
                            fos.flush();
                        }
                        System.out.println("Download complete. Saved as: " + outFile.getName());
                    } else {
                        System.out.println("Server response: " + response + " - File not found on server.");
                        // Do not exit; return to menu
                    }
                } else if (choice.equals("3")) {
                    dos.writeUTF("QUIT");
                    dos.flush();
                    String bye = dis.readUTF();
                    System.out.println("Server: " + bye);
                    running = false;
                } else {
                    System.out.println("Invalid choice, try again.");
                }
            }

        } catch (IOException e) {
            System.err.println("Client exception: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
