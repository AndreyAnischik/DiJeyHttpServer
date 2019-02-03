package server;

import io.github.cdimascio.dotenv.Dotenv;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer {
    private Dotenv dotenv = Dotenv.configure().directory("./").load();

    private boolean running = false;
    private ServerSocket serverSocket;
    private ConnectionManager connectionsManager;

    public HttpServer(int port) throws IOException {
        serverSocket = new ServerSocket(port, Integer.valueOf(dotenv.get("BACKLOG")));
        connectionsManager = new ConnectionManager();
        running = true;
        runServer();
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() {
        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }

        for (HttpConnection httpConnection : connectionsManager.getConnections()) {
            httpConnection.stop();
        }

        running = false;
    }

    private void runServer() {
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
