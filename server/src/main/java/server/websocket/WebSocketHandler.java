package server.websocket;

import com.google.gson.Gson;
import exception.ResponseException;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.messages.Action;
import websocket.messages.Notification;

import java.io.IOException;


@WebSocket
public class WebSocketHandler {

    private final ConnectionManager connections = new ConnectionManager();

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        Action action = new Gson().fromJson(message, Action.class);
        switch (action.type()) {
            case PLAYERJOIN -> playerJoin(action.userName(), action.playerColor(), session);
            case OBSERVERJOIN -> observerJoin(action.userName(), session);
            case MOVEMADE ->  moveMade();
            case PLAYERLEFT -> playerLeft();
            case OBSERVERLEFT -> observerLeft();
            case PLAYERRESIGNED -> playerResigned();
            case CHECK -> check();
            case CHECKMATE -> checkMate();
        }
    }

    @OnWebSocketConnect
    public void onConnect(Session session) {
        System.out.println("✅ WebSocket connection established with session: " + session);
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
        System.out.println("❌ Connection closed: " + reason);
    }

    @OnWebSocketError
    public void onError(Session session, Throwable error) {
        error.printStackTrace();
    }

    private void playerJoin(String userName, String playerColor, Session session) throws IOException {
        Connection connection = new Connection(userName, 1, playerColor, session);
        connections.add(connection);
        var message = String.format(userName + " has joined the game as the " + playerColor + " player");
        var notification = new Notification(Notification.Type.PLAYERJOIN, message);
        connections.broadcast(connection, notification);
    }

    private void observerJoin(String userName, Session session) throws IOException {
        Connection connection = new Connection(userName, 1, "observer", session);
        connections.add(connection);
        var message = String.format(userName + " has joined the game as an observer");
        var notification = new Notification(Notification.Type.OBSERVERJOIN, message);
        connections.broadcast(connection, notification);
    }

    private void moveMade() throws IOException {

    }

    private void playerLeft() throws IOException {

    }

    private void observerLeft() throws IOException {

    }

    private void playerResigned() throws IOException {

    }

    private void check() throws IOException {

    }

    private void checkMate() throws IOException {

    }
}
