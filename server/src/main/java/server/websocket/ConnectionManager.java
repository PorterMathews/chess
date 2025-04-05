package server.websocket;

import org.eclipse.jetty.websocket.api.Session;
import websocket.messages.Notification;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, List<Connection>> connections = new ConcurrentHashMap<>();

    public void add(Connection c) {
        var connection = new Connection(c.userName, c.gameID, c.role, c.session);
        if (connections.containsKey(c.userName)) {
            List<Connection> connectionList = connections.get(c.userName);
            connectionList.add(connection);
            connections.put(c.userName, connectionList);
        } else {
            List<Connection> connectionList = new ArrayList<>();
            connectionList.add(connection);
            connections.put(c.userName, connectionList);
        }
    }

    public void remove(String userName, int gameID, String role) {
        if (!connections.containsKey(userName)) {
            throw new RuntimeException("Trying to remove a connection that doesn't exist");
        }
        if (connections.get(userName).size() == 1) {
            connections.remove(userName);
        } else {
            List<Connection> connectionList = connections.get(userName);
            int i = 0;
            for (Connection connection : connectionList) {
                if (connection.gameID == gameID && connection.role.equals(role)) {
                    connectionList.remove(i);
                    break;
                }
                i++;
            }
        }
    }

    public void broadcast(Connection excludeConnection, Notification notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (List<Connection> connectionList : connections.values()) {
            for (Connection c : connectionList) {
                if (!c.session.isOpen()) {
                    removeList.add(c);
                    continue;
                }
                if (c.gameID == excludeConnection.gameID && !c.equals(excludeConnection)) {
                    c.send(notification.toString());
                }
            }
        }
        for (Connection c : removeList) {
            List<Connection> list = connections.get(c.userName);
            if (list != null) {
                list.remove(c);
                if (list.isEmpty()) {
                    connections.remove(c.userName);
                }
            }
        }
    }
}