package server;

import java.util.ArrayList;
import java.util.List;

public class ConnectionManager {
    private List<HttpConnection> connections = new ArrayList<>();

    public void add(HttpConnection httpConnection) {
        connections.add(httpConnection);
    }

    public void remove(HttpConnection httpConnection) {
        connections.remove(httpConnection);
    }

    public List<HttpConnection> getConnections() {
        return connections;
    }

    public void removeAll() {
        connections.clear();
    }
}
