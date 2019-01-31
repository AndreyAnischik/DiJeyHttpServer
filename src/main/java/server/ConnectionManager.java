package server;

import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {
    private List<FTPConnection> connection = new ArrayList<>();

    public void add(FTPConnection ftpConnection){
        connection.add(ftpConnection);
    }

    public void remove(FTPConnection ftpConnection){
        connection.remove(ftpConnection);
    }

    public List<FTPConnection> getConnection(){
        return connection;
    }

    public void removeAll(){
        connection.clear();
    }
}
