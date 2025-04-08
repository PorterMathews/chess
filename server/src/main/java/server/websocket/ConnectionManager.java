package server.websocket;

import com.google.gson.Gson;
import websocket.messages.NotificationMessage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

public class ConnectionManager {
    public final ConcurrentHashMap<String, List<Connection>> connections = new ConcurrentHashMap<>();
    private boolean detailedErrorMsg = false;

    /**
     * adds a connection
     * @param c
     */
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

    /**
     * removes a connection
     * @param userName user to remove
     * @param gameID gameID of user
     * @param color color of user
     */
    public void remove(String userName, int gameID, String color) {
        if (!connections.containsKey(userName)) {
            throw new RuntimeException("Trying to remove a connection that doesn't exist");
        }
        if (connections.get(userName).size() == 1) {
            connections.remove(userName);
        } else {
            List<Connection> connectionList = connections.get(userName);
            int i = 0;
            for (Connection connection : connectionList) {
                if (connection.gameID == gameID && connection.role.equals(color)) {
                    connectionList.remove(i);
                    break;
                }
                i++;
            }
        }
    }

    /**
     * sends notifications to others
     * @param excludeConnection not to your own connection
     * @param notification what you wanna send
     * @throws IOException
     */
    public void broadcast(Connection excludeConnection, NotificationMessage notification) throws IOException {
        var removeList = new ArrayList<Connection>();
        for (List<Connection> connectionList : connections.values()) {
            for (Connection c : connectionList) {
                debug(String.format("Broadcast1: Sending to" + c.userName + ": "  + notification.getMessage()));
                if (!c.session.isOpen()) {
                    removeList.add(c);
                    continue;
                }
                if (c.gameID == excludeConnection.gameID && !c.equals(excludeConnection)) {
                    c.send(new Gson().toJson(notification));
                }
            }
        }
        for (Connection c : removeList) {
            debug(String.format("Broadcast2: Sending to" + c.userName + ": "  + notification.getMessage()));
            List<Connection> list = connections.get(c.userName);
            if (list != null) {
                list.remove(c);
                if (list.isEmpty()) {
                    connections.remove(c.userName);
                }
            }
        }
    }

    /**
     * gets them connections
     * @param gameID game to get connections from
     * @return
     */
    public List<Connection> getConnectionsInGame(int gameID) {
        List<Connection> result = new ArrayList<>();
        for (List<Connection> list : connections.values()) {
            for (Connection c : list) {
                if (c.gameID == gameID) {
                    result.add(c);
                }
            }
        }
        return result;
    }

    /**
     * broadcasts to others
     * @param gameID game to broadcast to
     * @param excludeUser yourself
     * @param notification what you wanna send
     * @throws IOException
     */
    public void broadcastToOthers(int gameID, String excludeUser, NotificationMessage notification) throws IOException {
        for (List<Connection> list : connections.values()) {
            for (Connection c : list) {
                debug(String.format("To Others: Sending to" + c.userName + ": "  + notification.getMessage()));
                if (c.gameID == gameID && !c.userName.equals(excludeUser)) {
                    c.send(new Gson().toJson(notification));
                }
            }
        }
    }

    /**
     * send to everyone connected to the game
     * @param gameID game to send things to
     * @param notification what you wanna send
     * @throws IOException
     */
    public void broadcastAll(int gameID, NotificationMessage notification) throws IOException {
        for (List<Connection> list : connections.values()) {
            for (Connection c : list) {
                if (c.gameID == gameID) {
                    debug(String.format("To All: Sending to" + c.userName + ": "  + notification.getMessage()));
                    c.send(new Gson().toJson(notification));
                }
            }
        }
    }

    /**
     * little debugging
     * @param input
     */
    private void debug(String input) {
        if (detailedErrorMsg) {
            System.out.println(input);
        }
    }
}