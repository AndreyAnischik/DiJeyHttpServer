package server;

import java.io.IOException;
import java.net.Socket;

public class FTPConnection implements Runnable{
    private FTPServer ftpServer = null;
    private Socket socket = null;

    public FTPConnection(FTPServer server, Socket socket){
        this.ftpServer = server;
        this.socket = socket;
    }

    @Override
    public void run() {

    }

    public void stop(){
        if(!Thread.currentThread().isInterrupted()){
            Thread.currentThread().interrupt();
        }
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
