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
            case OBSERVERJOIN -> observerJoin();
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

    private void departure() throws IOException {

    }

    private void playerJoin(String userName, String playerColor, Session session) throws IOException {
        connections.add(userName, session);
        var message = String.format(userName + " has joined the game as the " + playerColor + " player");
        var notification = new Notification(Notification.Type.PLAYERJOIN, message);
        connections.broadcast(userName, notification);
    }

    private void observerJoin(String userName, Session session) throws IOException {
        connections.add(userName, session);
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

    private void enter(String visitorName, Session session) throws IOException {
        connections.add(visitorName, session);
        var message = String.format("%s is in the shop", visitorName);
        var notification = new Notification(Notification.Type.ARRIVAL, message);
        connections.broadcast(visitorName, notification);
    }

    private void exit(String visitorName) throws IOException {
        connections.remove(visitorName);
        var message = String.format("%s left the shop", visitorName);
        var notification = new Notification(Notification.Type.DEPARTURE, message);
        connections.broadcast(visitorName, notification);
    }

    public void makeNoise(String petName, String sound) throws ResponseException {
        try {
            var message = String.format("%s says %s", petName, sound);
            var notification = new Notification(Notification.Type.NOISE, message);
            connections.broadcast("", notification);
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }
}
