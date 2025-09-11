import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class Server_17_23 {
    static ConcurrentHashMap<String, Integer> balances = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, String> pins = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Boolean> completedTx = new ConcurrentHashMap<>();
    static ConcurrentHashMap<String, Boolean> hasWithdrawn = new ConcurrentHashMap<>();

    static void loadClients() {
        try (BufferedReader br = new BufferedReader(new FileReader("clients.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                String[] parts = line.split(",");
                if (parts.length == 3) {
                    String card = parts[0];
                    String pin = parts[1];
                    int bal = Integer.parseInt(parts[2]);
                    pins.put(card, pin);
                    balances.put(card, bal);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    static void saveClients() {
        try (BufferedWriter bw = new BufferedWriter(new FileWriter("clients.txt"))) {
            for (Map.Entry<String, String> entry : pins.entrySet()) {
                String card = entry.getKey();
                String pin = entry.getValue();
                int bal = balances.getOrDefault(card, 0);
                bw.write(card + "," + pin + "," + bal);
                bw.newLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) throws IOException {
        try (
        ServerSocket serverSocket = new ServerSocket(6002)) {
            System.out.println("Server listening on port 6002...");
            loadClients();
            int cnt = 0;

            while (true) {
                Socket socket = serverSocket.accept();
                
                System.out.println("New client" + ++cnt + " connected");

                DataInputStream input = new DataInputStream(socket.getInputStream());
                DataOutputStream output = new DataOutputStream(socket.getOutputStream());

                ClientHandler clientHandler = new ClientHandler(socket, input, output);

                clientHandler.start();
            }
        }
    }
}

class ClientHandler extends Thread {
    Socket socket;
    DataInputStream input;
    DataOutputStream output;
    String authenticatedCard = null;

    public ClientHandler(Socket socket, DataInputStream input, DataOutputStream output) {
        this.socket = socket;
        this.input = input;
        this.output = output;
    }

    public void run() {
        try {
            String message = "";
            while (true) {
                message = input.readUTF();
                if (message.equalsIgnoreCase("Exit")) {
                    System.out.println("User logged out: " + (authenticatedCard != null ? authenticatedCard : "unauthenticated"));
                    Server_17_23.hasWithdrawn.put(authenticatedCard, false);
                    break;
                }
                String[] parts = message.split(":");
                String type = parts[0];
                if (type.equals("AUTH")) {
                    String card = parts[1];
                    String pin = parts[2];
                    if (Server_17_23.pins.containsKey(card) && Server_17_23.pins.get(card).equals(pin)) {
                        authenticatedCard = card;
                        output.writeUTF("AUTH_OK");
                        System.out.println("Authentication successful for card: " + card);
                    } else {
                        output.writeUTF("AUTH_FAIL");
                        System.out.println("Authentication failed for card: " + card);
                    }
                } else if (type.equals("BALANCE_REQ")) {
                    if (authenticatedCard != null) {
                        int bal = Server_17_23.balances.getOrDefault(authenticatedCard, 0);
                        output.writeUTF("BALANCE_RES:" + bal);
                        System.out.println("Balance requested for card " + authenticatedCard + ": " + bal);
                    } else {
                        output.writeUTF("AUTH_FAIL");
                        System.out.println("Balance request denied: not authenticated");
                    }
                } else if (type.equals("WITHDRAW")) {
                    if (authenticatedCard != null) {
                        String txid = parts[1];
                        int amount = Integer.parseInt(parts[2]);
                        System.out.println("Withdrawal requested for card " + authenticatedCard + ": amount " + amount + ", txid " + txid);
                        if (Server_17_23.hasWithdrawn.getOrDefault(authenticatedCard, false)) {
                            output.writeUTF("ALREADY_WITHDRAWN");
                            System.out.println("Withdrawal denied for card " + authenticatedCard + ": already withdrawn once");
                        } else if (Server_17_23.completedTx.containsKey(txid)) {
                            output.writeUTF("WITHDRAW_OK");
                            System.out.println("Duplicate withdrawal detected, sent WITHDRAW_OK for txid " + txid);
                        } else {
                            int bal = Server_17_23.balances.get(authenticatedCard);
                            if (bal >= amount) {
                                Server_17_23.balances.put(authenticatedCard, bal - amount);
                                Server_17_23.completedTx.put(txid, true);
                                Server_17_23.hasWithdrawn.put(authenticatedCard, true);
                                Server_17_23.saveClients();
                                output.writeUTF("WITHDRAW_OK");
                                System.out.println("Withdrawal successful for card " + authenticatedCard + ": new balance " + (bal - amount));
                            } else {
                                output.writeUTF("INSUFFICIENT_FUNDS");
                                System.out.println("Withdrawal failed for card " + authenticatedCard + ": insufficient funds");
                            }
                        }

                        String ack = input.readUTF();
                        if (!"ACK".equals(ack)) {
                            System.out.println("Expected ACK, got: " + ack);
                        } else {
                            System.out.println("ACK received for transaction");
                        }
                    } else {
                        output.writeUTF("AUTH_FAIL");
                        System.out.println("Withdrawal request denied: not authenticated");
                    }
                } else {
                    output.writeUTF("UNKNOWN_COMMAND");
                    System.out.println("Unknown command received: " + type);
                }
            }
        } catch (IOException e) {
            if (e instanceof EOFException) {
                System.out.println("Client disconnected.");
            } else {
                e.printStackTrace();
            }
        } finally {
            try {
                input.close();
                output.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
