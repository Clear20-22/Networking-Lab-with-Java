import java.io.*;
import java.net.*;
import java.util.Scanner;
import java.util.UUID;

public class Client_17_23 {
    public static void main(String[] args) throws IOException {
        Scanner sc = new Scanner(System.in);
        Socket s = new Socket("localhost", 6002);
        DataInputStream input = new DataInputStream(s.getInputStream());
        DataOutputStream output = new DataOutputStream(s.getOutputStream());

        try {
            System.out.print("Enter card number: ");
            String card = sc.nextLine();

            System.out.print("Enter PIN: ");
            String pin = sc.nextLine();
            output.writeUTF("AUTH:" + card + ":" + pin);
            String response = input.readUTF();
            System.out.println("Auth response: " + response);
            if (!"AUTH_OK".equals(response)) {
                System.out.println("Authentication failed");
                return;
            }
    

            while (true) {
                System.out.println("\nMenu:");
                System.out.println("1. Check Balance");
                System.out.println("2. Withdraw");
                System.out.println("3. Exit");
                System.out.print("Choose option: ");
                int choice = Integer.parseInt(sc.nextLine());

                if (choice == 1) {
                    output.writeUTF("BALANCE_REQ");
                    response = input.readUTF();
                    System.out.println("Balance response: " + response);
                    if (response.startsWith("BALANCE_RES:")) {
                        String[] parts = response.split(":");
                        int balance = Integer.parseInt(parts[1]);
                        System.out.println("Current balance: " + balance);
                    }
                } else if (choice == 2) {
                    System.out.print("Enter amount to withdraw: ");
                    int amount = Integer.parseInt(sc.nextLine());
                    String txid = "tx" + UUID.randomUUID().toString();
                    System.out.println("Transaction ID: " + txid);
                    output.writeUTF("WITHDRAW:" + txid + ":" + amount);
                    response = input.readUTF();
                    System.out.println("Withdraw response: " + response);
                    if ("WITHDRAW_OK".equals(response)) {
                        System.out.println("Withdrawal successful");
                    } else if ("INSUFFICIENT_FUNDS".equals(response)) {
                        System.out.println("Insufficient funds");
                    }
                    output.writeUTF("ACK");
                    
                } else if (choice == 3) {
                    break;
                } else {
                    System.out.println("Invalid option");
                }
            }
            output.writeUTF("Exit");
        } finally {
            input.close();
            output.close();
            s.close();
            sc.close();
        }
    }
}
