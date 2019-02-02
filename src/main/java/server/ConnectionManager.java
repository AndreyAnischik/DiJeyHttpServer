package server;

import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {
    private List<HttpConnection> connection = new ArrayList<>();

    public void add(HttpConnection httpConnection){
        connection.add(httpConnection);
    }

    public void remove(HttpConnection httpConnection){
        connection.remove(httpConnection);
    }

    public List<HttpConnection> getConnection(){
        return connection;
    }

    public void removeAll(){
        connection.clear();
    }
}
