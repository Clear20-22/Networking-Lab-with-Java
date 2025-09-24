import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

public class Client {
    private static final String SERVER_URL = "http://localhost:8080";

    public static void main(String[] args) throws Exception {
        try (Scanner scanner = new Scanner(System.in)) {
            while (true) {
                System.out.println("\n=== HTTP File Client ===");
                System.out.println("1. Upload File (POST)");
                System.out.println("2. Download File (GET)");
                System.out.println("3. Exit");
                System.out.print("Choose option: ");
                int choice = scanner.nextInt();
                scanner.nextLine();

                if (choice == 1) {
                    System.out.print("Enter path of file to upload: ");
                    String path = scanner.nextLine();
                    uploadFile(path);
                } else if (choice == 2) {
                    System.out.print("Enter filename to download: ");
                    String fileName = scanner.nextLine();
                    downloadFile(fileName);
                } else {
                    break;
                }
            }
        }
    }

    // ---------- Upload ----------
    @SuppressWarnings("deprecation")
    private static void uploadFile(String filePath) throws Exception {
        File file = new File(filePath);
        if (!file.exists()) {
            System.out.println("❌ File not found locally: " + filePath);
            return;
        }

        URL url = new URL(SERVER_URL + "/upload");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setDoOutput(true);
        conn.setRequestMethod("POST");

        try (OutputStream os = conn.getOutputStream();
             FileInputStream fis = new FileInputStream(file)) {
            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
        }

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String response = br.readLine();
                System.out.println("✅ Server Response: " + response);
            }
        } else {
            System.out.println("❌ Upload failed with code: " + responseCode);
        }
    }

    // ---------- Download ----------
    @SuppressWarnings("deprecation")
    private static void downloadFile(String fileName) throws Exception {
        URL url = new URL(SERVER_URL + "/download?filename=" + fileName);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");

        int responseCode = conn.getResponseCode();
        if (responseCode == 200) {
            System.out.println("✅ File found on server: " + fileName);
            System.out.println("📥 File content (showing in terminal):");

            try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    System.out.println(line); // Print file content
                }
            }
        } else if (responseCode == 404) {
            System.out.println("❌ File not found on server: " + fileName);
        } else {
            System.out.println("⚠️ Request failed, code: " + responseCode);
        }
    }
}
