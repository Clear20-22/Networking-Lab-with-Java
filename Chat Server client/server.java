// Simple multithreaded chat server (improved)

import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class server {
    // list of client handlers
    static Vector<ClientHandler> clients = new Vector<>();
    static int clientCount = 0;
    // admin inbox for private messages to server
    static BlockingQueue<String> adminInbox = new LinkedBlockingQueue<>();
    final static int PORT = 1234;

    public static void main(String[] args) throws IOException {
        try (ServerSocket ss = new ServerSocket(PORT)) {
            System.out.println("Server started on port " + PORT);
            // start admin console
            Thread console = new Thread(new ServerConsole());
            console.setDaemon(true);
            console.start();
            while (true) {
                Socket s = ss.accept();
                System.out.println("New client request received: " + s.getRemoteSocketAddress());
                DataInputStream dis = new DataInputStream(s.getInputStream());
                DataOutputStream dos = new DataOutputStream(s.getOutputStream());

                // First message should be username
                String username = dis.readUTF();
                if (username == null || username.trim().isEmpty()) {
                    username = "guest" + (clientCount++);
                }

                System.out.println("Assigned username: " + username);

                ClientHandler handler = new ClientHandler(s, username, dis, dos);
                clients.add(handler);

                Thread t = new Thread(handler);
                t.start();
                System.out.println("Started handler for: " + username + " (" + s.getRemoteSocketAddress() + ")");
                clientCount++;
            }
        }
    }
}

class ClientHandler implements Runnable {
    final DataInputStream dis;
    final DataOutputStream dos;
    Socket s;
    String name;
    volatile boolean isLoggedIn;

    public ClientHandler(Socket s, String name, DataInputStream dis, DataOutputStream dos) {
        this.s = s;
        this.name = name;
        this.dis = dis;
        this.dos = dos;
        this.isLoggedIn = true;
    }

    @Override
    public void run() {
        try {
            // notify others
            broadcast(name + " has joined the chat.");
            System.out.println("User connected: " + name + " (" + s.getRemoteSocketAddress() + ")");

            String received;
            while (true) {
                try {
                    received = dis.readUTF();
                } catch (IOException e) {
                    received = null;
                }
                if (received == null) break;
                System.out.println("Received from " + name + ": " + received);

                if (received.equalsIgnoreCase("logout")) {
                    this.isLoggedIn = false;
                    this.s.close();
                    break;
                }

                // private message syntax: @username message
                if (received.startsWith("@")) {
                    int space = received.indexOf(' ');
                    if (space != -1) {
                        String target = received.substring(1, space);
                        String msg = received.substring(space + 1);
                        if (target.equalsIgnoreCase("server")) {
                            // push to admin inbox and acknowledge
                            server.adminInbox.offer("[FROM] " + name + ": " + msg);
                            this.dos.writeUTF("[PM_ACK] to server: " + msg);
                        } else {
                            boolean found = false;
                            for (ClientHandler ch : server.clients) {
                                if (ch.name.equals(target) && ch.isLoggedIn) {
                                    ch.dos.writeUTF("[PM] " + name + " : " + msg);
                                    found = true;
                                    break;
                                }
                            }
                            if (!found) {
                                this.dos.writeUTF("User not found: " + target);
                            }
                        }
                    } else {
                        this.dos.writeUTF("Invalid private message format. Use: @username message");
                    }
                } else {
                    // broadcast to all
                    broadcast(name + " : " + received);
                }
            }
        } catch (IOException e) {
            System.out.println("Connection with " + name + " closed.");
        } finally {
            // cleanup
            try {
                this.dis.close();
                this.dos.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            // remove from list
            server.clients.remove(this);
            System.out.println("User disconnected: " + name + " (" + s.getRemoteSocketAddress() + ")");
            broadcast(name + " has left the chat.");
        }
    }

    private void broadcast(String message) {
        for (ClientHandler ch : server.clients) {
            try {
                if (ch.isLoggedIn) ch.dos.writeUTF(message);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

// Admin console to allow server-side messaging and commands
class ServerConsole implements Runnable {
    private final Scanner scanner = new Scanner(System.in);
    private final Thread inboxThread;

    public ServerConsole() {
        inboxThread = new Thread(() -> {
            try {
                while (true) {
                    String msg = server.adminInbox.take();
                    System.out.println("\n[ADMIN INBOX] " + msg);
                    System.out.print(": ");
                }
            } catch (InterruptedException ignored) {}
        });
        inboxThread.setDaemon(true);
        inboxThread.start();
    }

    @Override
    public void run() {
        System.out.println("Server console ready. Type 'help' for commands.");
        while (true) {
            String line = scanner.nextLine();
            if (line == null) break;
            line = line.trim();
            if (line.isEmpty()) continue;
            try {
                if (line.equalsIgnoreCase("help")) {
                    System.out.println("Commands: broadcast <msg> | @username <msg> | users | quit");
                } else if (line.equalsIgnoreCase("users")) {
                    System.out.println("Online users: ");
                    for (ClientHandler ch : server.clients) System.out.println(" - " + ch.name);
                } else if (line.startsWith("broadcast ")) {
                    String msg = line.substring("broadcast ".length());
                    for (ClientHandler ch : server.clients) {
                        if (ch.isLoggedIn) ch.dos.writeUTF("[SERVER] " + msg);
                    }
                } else if (line.startsWith("@")) {
                    int space = line.indexOf(' ');
                    if (space != -1) {
                        String target = line.substring(1, space);
                        String msg = line.substring(space + 1);
                        boolean found = false;
                        for (ClientHandler ch : server.clients) {
                            if (ch.name.equals(target) && ch.isLoggedIn) {
                                ch.dos.writeUTF("[SERVER->YOU] " + msg);
                                found = true;
                                break;
                            }
                        }
                        if (!found) System.out.println("User not found: " + target);
                    } else {
                        System.out.println("Usage: @username message");
                    }
                } else if (line.equalsIgnoreCase("quit") || line.equalsIgnoreCase("shutdown")) {
                    System.out.println("Shutting down server by admin command.");
                    // close all client sockets
                    for (ClientHandler ch : server.clients) {
                        ch.isLoggedIn = false;
                        try { ch.s.close(); } catch (IOException ignored) {}
                    }
                    System.exit(0);
                } else {
                    // default broadcast
                    for (ClientHandler ch : server.clients) if (ch.isLoggedIn) ch.dos.writeUTF("[SERVER] " + line);
                }
            } catch (IOException e) {
                System.out.println("Error sending server message: " + e.getMessage());
            }
        }
    }
}