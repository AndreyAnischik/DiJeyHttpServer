package server;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.log4j.Logger;
import server.mappers.PermanentlyPageMapper;
import server.mappers.TemporarilyPageMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Runnable {
    private Dotenv dotenv = Dotenv.configure().directory("./").load();

    private ServerSocket serverSocket;
    private ConnectionManager connectionsManager;
    private TemporarilyPageMapper temporarilyPageMapper;
    private PermanentlyPageMapper permanentlyPageMapper;
    private Logger logger = Logger.getLogger(HttpServer.class);

    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port, Integer.valueOf(dotenv.get("BACKLOG")));
        this.connectionsManager = new ConnectionManager();
        this.permanentlyPageMapper = new PermanentlyPageMapper();
        this.temporarilyPageMapper = new TemporarilyPageMapper();
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

    public String getMovedUrl(String oldUrl) {
        return permanentlyPageMapper.getMovedUrl(oldUrl);
    }

    public String getFoundUrl(String oldUrl) {
        return temporarilyPageMapper.getFoundUrl(oldUrl);
    }

    private void runServer() {
        logger.info("Server is running on " + serverSocket.getLocalPort() + " port.");

        try {
            Socket client = serverSocket.accept();
            HttpConnection session = new HttpConnection(this, client);
            connectionsManager.add(session);
            new Thread(session).start();
        } catch (IOException e) {
            logger.error("I/O error occurs while waiting for a connection.");
        }
    }

    public int getServerPort(){
        return serverSocket.getLocalPort();
    }
}
