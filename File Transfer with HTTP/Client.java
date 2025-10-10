import java.io.*;
import java.net.*;
import java.util.Scanner;

public class Client {
    private static final String SERVER_IP = "localhost";
    private static final int SERVER_PORT = 8080;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);
        while (true) {
            System.out.println("\nChoose an option:");
            System.out.println("1) Upload file");
            System.out.println("2) Download file");
            System.out.println("3) Quit");
            System.out.print("Enter choice: ");
            String choice = sc.nextLine().trim();

            if (choice.equals("1")) {
                uploadFile(sc);
            } else if (choice.equals("2")) {
                downloadFile(sc);
            } else if (choice.equals("3")) {
                break;
            } else {
                System.out.println("Invalid choice.");
            }
        }
    }

    @SuppressWarnings("deprecation")
    private static void uploadFile(Scanner sc) {
        System.out.print("Enter the path of the file to upload: ");
        String filePath = sc.nextLine().trim();
        File file = new File(filePath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("File not found.");
            return;
        }

        try {
            URL url = new URL("http://" + SERVER_IP + ":" + SERVER_PORT + "/upload");
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = conn.getOutputStream()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }


            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
                try (BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()))) {
                    String response = br.readLine();
                    System.out.println("Upload confirmation: " + response);
                }
            } else {
                System.out.println("Upload failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Upload error: " + e.getMessage());
        }
    }

    @SuppressWarnings("deprecation")
    private static void downloadFile(Scanner sc) {
        System.out.print("Enter the filename to download: ");
        String filename = sc.nextLine().trim();

        try {
            URL url = new URL("http://" + SERVER_IP + ":" + SERVER_PORT + "/download?filename=" + URLEncoder.encode(filename, "UTF-8"));
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setRequestMethod("GET");

            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
              
                File outFile = new File("downloaded_" + filename);
                try (InputStream is = conn.getInputStream();
                     FileOutputStream fos = new FileOutputStream(outFile)) {
                    byte[] buffer = new byte[8192];
                    int read;
                    while ((read = is.read(buffer)) != -1) {
                        fos.write(buffer, 0, read);
                    }
                    
                }
                System.out.println("Download successful. File saved as: " + outFile.getName());
            } else if (responseCode == 404) {
                System.out.println("File not found on server.");
            } else {
                System.out.println("Download failed with response code: " + responseCode);
            }
        } catch (IOException e) {
            System.err.println("Download error: " + e.getMessage());
        }
    }
}
