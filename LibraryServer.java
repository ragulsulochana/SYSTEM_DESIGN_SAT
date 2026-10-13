import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URLConnection;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.Executors;

/**
 * Lightweight Single-File Java HTTP Server for Library Management System
 * Requires Java 11+ (Tested on Java 25)
 * Run directly with: java LibraryServer.java
 */
public class LibraryServer {

    private static final int PORT = 8080;
    private static final String ROOT_DIR = ".";

    public static void main(String[] args) {
        int port = PORT;
        if (args.length > 0) {
            try {
                port = Integer.parseInt(args[0]);
            } catch (NumberFormatException ignored) {}
        }

        try {
            HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);
            server.createContext("/", new StaticFileHandler(ROOT_DIR));
            server.setExecutor(Executors.newVirtualThreadPerTaskExecutor()); // Java 21+ Virtual Threads
            server.start();

            System.out.println("================================================================");
            System.out.println("  ATHENAEUM LIBRARY MANAGEMENT SYSTEM - JAVA HTTP SERVER");
            System.out.println("================================================================");
            System.out.println("  [Status] Server successfully running at:");
            System.out.println("  >>> http://localhost:" + port + "/index.html");
            System.out.println("  >>> http://127.0.0.1:" + port + "/");
            System.out.println("  [Base Directory] " + new File(ROOT_DIR).getCanonicalPath());
            System.out.println("  [Runtime] " + System.getProperty("java.runtime.name") + " (v" + System.getProperty("java.version") + ")");
            System.out.println("  Press Ctrl+C to terminate the server.");
            System.out.println("================================================================");

        } catch (IOException e) {
            System.err.println("Failed to start Java HTTP Server: " + e.getMessage());
            e.printStackTrace();
        }
    }

    static class StaticFileHandler implements HttpHandler {
        private final String baseDir;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        public StaticFileHandler(String baseDir) {
            this.baseDir = baseDir;
        }

        @Override
        public void handle(HttpExchange exchange) throws IOException {
            String path = exchange.getRequestURI().getPath();
            
            // Map root path to index.html
            if (path == null || path.equals("/") || path.isEmpty()) {
                path = "/index.html";
            }

            // Prevent path traversal attacks
            if (path.contains("..")) {
                sendError(exchange, 403, "403 Forbidden: Invalid Path");
                return;
            }

            File file = new File(baseDir, path.substring(1));

            // Default to index.html if file doesn't exist
            if (!file.exists() || file.isDirectory()) {
                file = new File(baseDir, "index.html");
            }

            if (!file.exists()) {
                sendError(exchange, 404, "404 Not Found: " + path);
                return;
            }

            String mimeType = getMimeType(file.getName());
            long length = file.length();

            exchange.getResponseHeaders().set("Content-Type", mimeType);
            exchange.getResponseHeaders().set("Access-Control-Allow-Origin", "*");
            exchange.getResponseHeaders().set("Cache-Control", "no-cache, no-store, must-revalidate");
            exchange.sendResponseHeaders(200, length);

            try (FileInputStream fis = new FileInputStream(file);
                 OutputStream os = exchange.getResponseBody()) {
                byte[] buffer = new byte[8192];
                int bytesRead;
                while ((bytesRead = fis.read(buffer)) != -1) {
                    os.write(buffer, 0, bytesRead);
                }
                os.flush();
            }

            logRequest(exchange.getRequestMethod(), exchange.getRequestURI().toString(), 200);
        }

        private String getMimeType(String filename) {
            String lower = filename.toLowerCase();
            if (lower.endsWith(".html") || lower.endsWith(".htm")) return "text/html; charset=UTF-8";
            if (lower.endsWith(".css")) return "text/css; charset=UTF-8";
            if (lower.endsWith(".js")) return "application/javascript; charset=UTF-8";
            if (lower.endsWith(".json")) return "application/json; charset=UTF-8";
            if (lower.endsWith(".png")) return "image/png";
            if (lower.endsWith(".jpg") || lower.endsWith(".jpeg")) return "image/jpeg";
            if (lower.endsWith(".svg")) return "image/svg+xml";
            if (lower.endsWith(".ico")) return "image/x-icon";
            if (lower.endsWith(".woff2")) return "font/woff2";
            return URLConnection.guessContentTypeFromName(filename);
        }

        private void sendError(HttpExchange exchange, int statusCode, String message) throws IOException {
            byte[] bytes = message.getBytes(StandardCharsets.UTF_8);
            exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=UTF-8");
            exchange.sendResponseHeaders(statusCode, bytes.length);
            try (OutputStream os = exchange.getResponseBody()) {
                os.write(bytes);
            }
            logRequest(exchange.getRequestMethod(), exchange.getRequestURI().toString(), statusCode);
        }

        private void logRequest(String method, String uri, int status) {
            String timestamp = dateFormat.format(new Date());
            System.out.printf("[%s] %s %s -> HTTP %d%n", timestamp, method, uri, status);
        }
    }
}
