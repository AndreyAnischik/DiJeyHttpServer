package server;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    Dotenv dotenv = Dotenv.configure().directory("./").load();

    private static boolean running = false;
    private ServerSocket serverSocket = null;
    private Database database;

    private ConnectionManager connectionsManager = null;

    public HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port, Integer.valueOf(dotenv.get("BACKLOG")), InetAddress.getLocalHost());
        database = new Database();
    }

    public static boolean isRunning() {
        return running;
    }

    public void stop() {
        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }

    }

    private void runServer() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                HttpConnection session = new HttpConnection(this, client);
                connectionsManager.add(session);
                new Thread(session).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
