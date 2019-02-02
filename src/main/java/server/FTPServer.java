package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class FTPServer
    implements Runnable{
    private static boolean running = false;
    private ServerSocket serverSocket = null;
    private Database database;

    private ConnectionManager connectionsManager = null;

    public FTPServer() throws IOException {
        serverSocket = new ServerSocket();
        connectionsManager = new ConnectionManager();
        database = new Database();
    }

    public static boolean isRunning(){
        return running;
    }

    public void stop(){
        if(!Thread.currentThread().isInterrupted()){
            Thread.currentThread().interrupt();
        }

        for(FTPConnection session: connectionsManager.getConnection()) {
            session.stop();
        }

        connectionsManager.removeAll();
    }


    @Override
    public void run() {
        runServer();
    }

    private void runServer() {
        while (running) {
            try {
                Socket client = serverSocket.accept();
                FTPConnection session = new FTPConnection(this,client);
                connectionsManager.add(session);
                new Thread(session).start();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
