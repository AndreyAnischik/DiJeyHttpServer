package server;

import io.github.cdimascio.dotenv.Dotenv;
import org.apache.log4j.Logger;
import server.mappers.PageMapper;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class HttpServer implements Runnable {
    private Dotenv dotenv = Dotenv.configure().directory("./").load();

    private ServerSocket serverSocket;
    private ConnectionManager connectionsManager;
    private PageMapper pageMapper;
    private Logger logger = Logger.getLogger(HttpServer.class);

    public HttpServer(int port) throws IOException {
        this.serverSocket = new ServerSocket(port, Integer.valueOf(dotenv.get("BACKLOG")));
        this.connectionsManager = new ConnectionManager();
        this.pageMapper = new PageMapper();
    }

    @Override
    public void run() {
        runServer();
    }

    public void stop() throws IOException {
        for (HttpConnection httpConnection : connectionsManager.getConnections()) {
            httpConnection.stop();
        }
        
        serverSocket.close();

        if (!Thread.currentThread().isInterrupted()) {
            logger.info("Server was stopped.");
            Thread.currentThread().interrupt();
        }
    }

    public String getMovedUrl(String oldUrl) {
        return pageMapper.getMovedUrl(oldUrl);
    }

    public String getFoundUrl(String oldUrl) {
        return pageMapper.getFoundUrl(oldUrl);
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

    public int getServerPort() {
        return serverSocket.getLocalPort();
    }
}
