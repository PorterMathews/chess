package server.websocket;

import chess.ChessGame;
import com.google.gson.Gson;
import dataaccess.*;
import exception.ResponseException;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.messages.*;
import websocket.commands.UserGameCommand;
import websocket.messages.Notification;

import java.io.IOException;


@WebSocket
public class WebSocketHandler {
    private boolean detailedErrorMsg;
    private final ConnectionManager connections = new ConnectionManager();
    private final AuthDAO authDAO;
    private final GameDAO gameDAO;

    public WebSocketHandler(AuthDAO authDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.gameDAO = gameDAO;
        detailedErrorMsg = true;
    }

    @OnWebSocketMessage
    public void onMessage(Session session, String message) throws IOException {
        UserGameCommand command = new Gson().fromJson(message, UserGameCommand.class);
        switch (command.getCommandType()) {
            case CONNECT -> connectUser(command, session);
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

    private void connectUser(UserGameCommand command, Session session) throws IOException {
        try {
            AuthData auth = authDAO.getAuthDataByAuthToken(command.getAuthToken());
            String username = auth.username();
            String role = command.isObserver() ? "observer" : getRole(username, command.getGameID());

            Connection connection = new Connection(username, command.getGameID(), role, session);
            connections.add(connection);

            debug("WSH: getting game with ID: " + gameDAO.getGameByID(command.getGameID()));
            ChessGame game = gameDAO.getGameByID(command.getGameID()).game();
            debug("got game " + game);
            LoadGameMessage loadGame = new LoadGameMessage(game);
            debug("loaded game: " + new Gson().toJson(loadGame));
            session.getRemote().sendString(new Gson().toJson(loadGame));

            Notification notification;
            if (role.equals("observer")) {
                notification = new Notification(Notification.Type.NOTIFICATION,
                        username + " has joined the game as an observer");
            } else {
                notification = new Notification(Notification.Type.NOTIFICATION,
                        username + " has joined the game as the " + role + " player");
            }
            connections.broadcast(connection, notification);
        } catch (DataAccessException e) {
            ErrorMessage error = new ErrorMessage("Error: " + e.getMessage());
            session.getRemote().sendString(new Gson().toJson(error));
        }
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

    private String getRole(String username, int gameID) throws DataAccessException {
        GameData game = gameDAO.getGameByID(gameID);
        if (game == null) throw new DataAccessException("Invalid game ID");
        if (username == null) {
            return "observer";
        }
        if (username.equals(game.whiteUsername())) {return "white";}
        if (username.equals(game.blackUsername())) {return "black";}
        throw new DataAccessException("unable to get role");
    }

    /**
     * Prints only if detailed messaging is on
     * @param input string to be printed
     */
    private void debug(String input) {
        if (detailedErrorMsg) {
            System.out.println(input);
        }
    }
}
