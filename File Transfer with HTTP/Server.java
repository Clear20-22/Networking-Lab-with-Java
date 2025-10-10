import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;
import java.io.*;
import java.net.InetSocketAddress;
import java.util.concurrent.Executors;

public class Server {
    private static final int PORT = 8080;

    public static void main(String[] args) throws IOException {

        HttpServer server = HttpServer.create(new InetSocketAddress(PORT), 0);
        server.createContext("/download", new DownloadHandler());
        server.createContext("/upload", new UploadHandler());
        server.setExecutor(Executors.newCachedThreadPool()); 
        server.start();
        System.out.println("HTTP File Server started on port " + PORT);
    }


    static class DownloadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            System.out.println("Download request received.");
            if (!"GET".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1); 
                System.out.println("HTTP/1.1 405 Method Not Allowed");
                return;
            }

            String query = exchange.getRequestURI().getQuery();
            String filename = null;
            if (query != null && query.startsWith("filename=")) {
                filename = query.substring(9);
            }

            if (filename == null || filename.isEmpty()) {
                exchange.sendResponseHeaders(400, -1); 
                System.out.println("HTTP/1.1 400 Bad Request");
                return;
            }

            File file = new File(filename);
            if (!file.exists() || !file.isFile()) {
                exchange.sendResponseHeaders(404, -1); 
                System.out.println("HTTP/1.1 404 Not Found");
                return;
            }

            exchange.getResponseHeaders().set("Content-Type", "application/octet-stream");
            exchange.getResponseHeaders().set("Content-Disposition", "attachment; filename=\"" + filename + "\"");
            exchange.sendResponseHeaders(200, file.length());
            System.out.println("HTTP/1.1 200 OK - File downloaded: " + filename);

        
            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, read);
                }
            }
        }
    }

 
    static class UploadHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if (!"POST".equals(exchange.getRequestMethod())) {
                exchange.sendResponseHeaders(405, -1);
                System.out.println("HTTP/1.1 405 Method Not Allowed");
                return;
            }


            String timestamp = String.valueOf(System.currentTimeMillis());
            String filename = "upload_" + timestamp + ".dat";
            File outFile = new File(filename);

       
            try (InputStream is = exchange.getRequestBody();
                 FileOutputStream fos = new FileOutputStream(outFile)) {
                byte[] buffer = new byte[8192];
                int read;
                while ((read = is.read(buffer)) != -1) {
                    fos.write(buffer, 0, read);
                }
            }

  
            exchange.sendResponseHeaders(200, 0);
            System.out.println("HTTP/1.1 200 OK");
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(("File uploaded successfully as: " + filename).getBytes());
                System.out.println("File uploaded and saved as: " + filename);
            }
        }
    }
}
