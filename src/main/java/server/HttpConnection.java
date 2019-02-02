package server;

import java.io.IOException;
import java.net.Socket;

public class HttpConnection implements Runnable {
    private HttpServer httpServer;
    private Socket socket;

    public HttpConnection(HttpServer server, Socket socket) {
        this.httpServer = server;
        this.socket = socket;
    }

    @Override
    public void run() {
    }

    public void stop() {
        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
