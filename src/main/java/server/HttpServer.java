package server;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Runnable {
    private Dotenv dotenv = Dotenv.configure().directory("./").load();

    private ServerSocket serverSocket;
    private ConnectionManager connectionsManager;
    private Logger logger = Logger.getLogger(HttpServer.class);

    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port, Integer.valueOf(dotenv.get("BACKLOG")));
        this.connectionsManager = new ConnectionManager();
    }

    @Override
    public void run() {
        runServer();
    }

    public void stop() throws IOException {
        for (HttpConnection httpConnection : connectionsManager.getConnections()) {
            httpConnection.stop();
        }

        if (!Thread.currentThread().isInterrupted()) {
            serverSocket.close();
            logger.info("Server was stopped.");
            Thread.currentThread().interrupt();
        }
    }

    private void runServer() {
        logger.info("Server is running on " + serverSocket.getLocalPort() + " port.");

        try {
            Socket client = serverSocket.accept();
            HttpConnection session = new HttpConnection(client);
            connectionsManager.add(session);
            new Thread(session).start();
        } catch (IOException e) {
            logger.error("I/O error occurs while waiting for a connection.");
        }
    }
}
