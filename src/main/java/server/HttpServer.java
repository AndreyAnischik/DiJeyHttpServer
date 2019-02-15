package server;

import io.github.cdimascio.dotenv.Dotenv;
import javafx.scene.control.TextArea;
import logger.Logger;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Runnable {
    private Dotenv dotenv = Dotenv.configure().directory("./").load();

    private boolean running;
    private ServerSocket serverSocket;
    private ConnectionManager connectionsManager;
    private Logger logger;

    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port, Integer.valueOf(dotenv.get("BACKLOG")));
        this.logger = Logger.getInstance(new TextArea());
        this.connectionsManager = new ConnectionManager();
        this.running = true;
    }

    @Override
    public void run() {
        runServer();
    }

    public boolean isRunning() {
        return running;
    }

    public void stop() throws IOException {
        if (!Thread.currentThread().isInterrupted()) {
            Thread.currentThread().interrupt();
            serverSocket.close();
            writeToLog("Server was stopped");
        }

        for (HttpConnection httpConnection : connectionsManager.getConnections()) {
            httpConnection.stop();
        }

        running = false;
    }

    private void runServer() {
        writeToLog("Server is running on " + serverSocket.getLocalPort() + " port");
        try {
            Socket client = serverSocket.accept();
            HttpConnection session = new HttpConnection(this, client);
            connectionsManager.add(session);
            new Thread(session).start();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void writeToLog(String message) {
        logger.writeToLog(message);
    }
}
