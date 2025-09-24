// Simple multithreaded chat client (improved)
// Connects to server, sends a username, supports broadcast and private messages.

import java.io.*;
import java.net.*;
import java.util.Scanner;

public class client1 {
	final static int ServerPort = 1234;

	public static void main(String args[]) {
		try (Scanner scn = new Scanner(System.in)) {
			InetAddress ip = InetAddress.getByName("localhost");
			try (Socket s = new Socket(ip, ServerPort)) {
				DataInputStream dis = new DataInputStream(s.getInputStream());
				DataOutputStream dos = new DataOutputStream(s.getOutputStream());

				System.out.print("Enter username: ");
				String username = scn.nextLine().trim();
				if (username.isEmpty()) username = "guest" + System.currentTimeMillis()%1000;
				// send username as first message to the server
				dos.writeUTF(username);

				// Thread to read messages from server
				Thread readMessage = new Thread(() -> {
					try {
						while (true) {
							String msg = dis.readUTF();
							System.out.println(msg);
						}
					} catch (IOException e) {
						System.out.println("Connection closed by server.");
					}
				});

				// Thread to send messages to server
				Thread sendMessage = new Thread(() -> {
					try {
						while (true) {
							String msg = scn.nextLine();
							if (msg == null) break;
							msg = msg.trim();
							if (msg.equalsIgnoreCase("logout")) {
								dos.writeUTF("logout");
								break;
							}
							// support private message syntax: @username message
							if (msg.startsWith("@")) {
								// send as-is; server will parse
								dos.writeUTF(msg);
							} else {
								// broadcast
								// use format: message (server treats non-@ as broadcast)
								dos.writeUTF(msg);
							}
						}
					} catch (IOException e) {
						System.out.println("Error sending message: " + e.getMessage());
					} finally {
						try { s.close(); } catch (IOException ignored) {}
					}
				});

				readMessage.start();
				sendMessage.start();

				// wait for sender to finish
				sendMessage.join();
			} catch (InterruptedException ie) {
				Thread.currentThread().interrupt();
			}
		} catch (UnknownHostException e) {
			System.out.println("Server not found: " + e.getMessage());
		} catch (IOException e) {
			System.out.println("I/O Error: " + e.getMessage());
		}
		System.out.println("Client exited.");
	}
}