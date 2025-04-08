package server.websocket;

import chess.*;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.*;
import exception.ResponseException;
import model.*;
import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;
import websocket.commands.MakeMoveCommand;
import websocket.messages.*;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

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
            case MAKE_MOVE -> makeMove(new Gson().fromJson(message, MakeMoveCommand.class), session);
        }
    }


    @OnWebSocketConnect
    public void onConnect(Session session) {
    }

    @OnWebSocketClose
    public void onClose(Session session, int statusCode, String reason) {
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

            NotificationMessage notification;
            if (role.equals("observer")) {
                notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " has joined the game as an observer");
            } else {
                notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION,
                        username + " has joined the game as the " + role + " player");
            }
            connections.broadcast(connection, notification);
        } catch (DataAccessException e) {
            ErrorMessage error = new ErrorMessage("Error: " + e.getMessage());
            session.getRemote().sendString(new Gson().toJson(error));
        }
    }

    private void makeMove(MakeMoveCommand command, Session session) throws IOException {
        try {
            // 1. Auth + game fetch
            AuthData auth = authDAO.getAuthDataByAuthToken(command.getAuthToken());
            String username = auth.username();
            int gameID = command.getGameID();
            ChessMove move = command.getMove();

            GameData gameData = gameDAO.getGameByID(gameID);
            ChessGame chessGame = gameData.game();

            // 2. Validate and apply move
            chessGame.makeMove(move); // This can throw InvalidMoveException

            // 3. Update DB
            gameDAO.updateGame(gameID, gameData);

            // 4. Send LOAD_GAME to all clients
            LoadGameMessage loadGame = new LoadGameMessage(chessGame);
            for (Connection conn : connections.getConnectionsInGame(gameID)) {
                conn.send(new Gson().toJson(loadGame));
            }

            // 5. Notify others what move was made
            String moveDesc = String.format("%s moved from %s to %s", username,
                    posToString(move.getStartPosition()), posToString(move.getEndPosition()));
            NotificationMessage notifi  = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveDesc);
            connections.broadcastToOthers(gameID, username, notifi);

            // 6. Notify all if check/stalemate/checkmate
            ChessGame.TeamColor turn = chessGame.getTeamTurn();
            if (chessGame.isInCheck(turn)) {
                connections.broadcastAll(gameID, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, turn + " is in check"));
            }
            if (chessGame.isInStalemate(turn)) {
                connections.broadcastAll(gameID, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate!"));
            }
            if (chessGame.isInCheckmate(turn)) {
                connections.broadcastAll(gameID, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Checkmate!"));
            }

        } catch (InvalidMoveException | DataAccessException e) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    private String posToString(ChessPosition pos) {
        char col = (char) ('a' + pos.getColumn() - 1);
        return "" + col + pos.getRow();
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
