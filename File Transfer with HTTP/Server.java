import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.*;
import java.net.InetSocketAddress;
import java.net.URI;
import java.nio.file.Files;
import java.util.concurrent.Executors;

public class Server {
    public static void main(String[] args) throws Exception {
        int port = 8080;
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        server.createContext("/upload", new UploadHandler());
        server.createContext("/download", new DownloadHandler());

        server.setExecutor(Executors.newFixedThreadPool(10)); // multiple clients
        System.out.println("🚀 Server started at http://localhost:" + port);
        server.start();
    }

    // ====== POST Handler: Upload File ======
    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("POST")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            String fileName = "upload_" + System.currentTimeMillis() + ".bin";
            File file = new File(fileName);

            try (InputStream is = exchange.getRequestBody();
                 FileOutputStream fos = new FileOutputStream(file)) {
                byte[] buffer = new byte[4096];
                int bytesRead;
                while ((bytesRead = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, bytesRead);
                }
            }

            String response = "✅ File uploaded as: " + file.getName();
            byte[] bytes = response.getBytes();

            exchange.sendResponseHeaders(200, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
                os.flush();
            }

            System.out.println("[UPLOAD] Saved file: " + file.getName());
        }
    }

    // ====== GET Handler: Download File ======
    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!exchange.getRequestMethod().equalsIgnoreCase("GET")) {
                exchange.sendResponseHeaders(405, -1); // Method Not Allowed
                return;
            }

            URI requestURI = exchange.getRequestURI();
            String query = requestURI.getQuery(); // e.g., filename=test.txt
            if (query == null || !query.startsWith("filename=")) {
                String response = "❌ Missing filename parameter";
                exchange.sendResponseHeaders(400, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                return;
            }

            String fileName = query.substring("filename=".length());
            File file = new File(fileName);

            if (!file.exists()) {
                String response = "❌ File Not Found";
                exchange.sendResponseHeaders(404, response.length());
                try (OutputStream os = exchange.getResponseBody()) {
                    os.write(response.getBytes());
                }
                System.out.println("[DOWNLOAD] File not found: " + fileName);
                return;
            }

            byte[] fileBytes = Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().add("Content-Type", "application/octet-stream");
            exchange.getResponseHeaders().add("Content-Disposition", "attachment; filename=\"" + fileName + "\"");

            exchange.sendResponseHeaders(200, fileBytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(fileBytes);
                os.flush();
            }

            System.out.println("[DOWNLOAD] Sent file: " + fileName);
        }
    }
}
