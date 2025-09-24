
import java.io.*;
import java.net.*;

public class Server {
    public static final int PORT = 1234;

    public static void main(String[] args) {
        System.out.println("Starting file-transfer server on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connection accepted from " + clientSocket.getRemoteSocketAddress());
                new Thread(new ClientHandler(clientSocket)).start();
            }
        } catch (IOException e) {
            System.err.println("Server exception: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class ClientHandler implements Runnable {
        private Socket socket;
        private static int clientCounter = 0;
        private int clientId;

        // Serve only from this fixed serverRoot directory
        private static final File serverRoot = new File("Server file list");

        public ClientHandler(Socket socket) {
            this.socket = socket;
            this.clientId = ++clientCounter;
        }

        public void run() {
            try (
                    DataInputStream dis = new DataInputStream(socket.getInputStream());
                    DataOutputStream dos = new DataOutputStream(socket.getOutputStream());
                ) {
                    // Interaction loop: accept simple commands from client via UTF strings
                // Commands:
                // LIST            -> server sends newline-separated list of files under serverRoot, then a single line with END_OF_LIST
                // GET <path>      -> server sends FOUND or NOT_FOUND. If FOUND: sendLong(fileSize) then raw bytes
                // QUIT            -> server closes connection
                dos.writeUTF("Welcome to File Server (Client #" + clientId + "). Available commands: LIST, GET <path>, QUIT");
                dos.flush();

                String cmd;
                while (true) {
                    try {
                        cmd = dis.readUTF();
                    } catch (EOFException eof) {
                        System.out.println("Client #" + clientId + " disconnected unexpectedly");
                        break; // client disconnected
                    }
                    if (cmd == null) break;
                    cmd = cmd.trim();
                    if (cmd.equalsIgnoreCase("QUIT")) {
                        dos.writeUTF("BYE");
                        dos.flush();
                        System.out.println("Client #" + clientId + " disconnected gracefully");
                        break;
                    } else if (cmd.equalsIgnoreCase("LIST")) {
                        File[] files = serverRoot.listFiles();
                        if (files != null) {
                            for (File f : files) {
                                dos.writeUTF(f.getName() + (f.isDirectory() ? "/" : ""));
                            }
                        }
                        dos.writeUTF("END_OF_LIST");
                        dos.flush();
                    } else if (cmd.toUpperCase().startsWith("GET ")) {
                        String fileName = cmd.substring(4).trim();
                        System.out.println("Client #" + clientId + " requested file: " + fileName);

                        // Resolve requested file inside serverRoot and prevent path traversal
                        File requested = new File(fileName);
                        File file;
                        try {
                            if (requested.isAbsolute()) {
                                file = requested;
                            } else {
                                String reqPath = fileName;
                                String rootName = serverRoot.getName();
                                if (reqPath.startsWith(rootName + File.separator)) {
                                    reqPath = reqPath.substring(rootName.length() + 1);
                                } else if (reqPath.equals(rootName)) {
                                    reqPath = "";
                                }
                                file = new File(serverRoot, reqPath);
                            }
                            String rootCanonical = serverRoot.getCanonicalPath();
                            String fileCanonical = file.getCanonicalPath();
                            if (!fileCanonical.startsWith(rootCanonical + File.separator) && !fileCanonical.equals(rootCanonical)) {
                                // Attempt to access outside of server root
                                dos.writeUTF("NOT_FOUND");
                                dos.flush();
                                System.out.println("Rejected outside-root access attempt: " + fileName + " -> " + fileCanonical);
                                continue;
                            }
                        } catch (IOException ioe) {
                            dos.writeUTF("NOT_FOUND");
                            dos.flush();
                            System.out.println("Error resolving path: " + fileName + " -> " + ioe.getMessage());
                            continue;
                        }

                        if (file.exists() && file.isFile()) {
                            dos.writeUTF("FOUND");
                            dos.flush();

                            long fileSize = file.length();
                            dos.writeLong(fileSize);
                            dos.flush();
                            
                            System.out.println("Starting file transfer for Client #" + clientId + ": " + fileName + " (" + fileSize + " bytes)");

                            try (FileInputStream fis = new FileInputStream(file)) {
                                byte[] buffer = new byte[8192];
                                int read;
                                long totalSent = 0;
                                while ((read = fis.read(buffer)) != -1) {
                                    dos.write(buffer, 0, read);
                                    totalSent += read;
                                }
                                dos.flush();
                                System.out.println("File transfer completed for Client #" + clientId + ": '" + fileName + "' (" + totalSent + " bytes)");
                            }
                        } else {
                            dos.writeUTF("NOT_FOUND");
                            dos.flush();
                            System.out.println("Client #" + clientId + " - File not found: " + fileName);
                        }
                    } else {
                        dos.writeUTF("ERROR Unknown command");
                        dos.flush();
                    }
                }
            } catch (IOException e) {
                System.err.println("Client handler exception: " + e.getMessage());
                e.printStackTrace();
            } finally {
                try {
                    socket.close();
                } catch (IOException e) {
                    // ignore
                }
            }
        }
    }
}
