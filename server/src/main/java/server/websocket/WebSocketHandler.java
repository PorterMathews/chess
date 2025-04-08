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
            case LEAVE -> leave(command, session);
            case RESIGN -> resign(command, session);
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
            if (auth == null) {
                throw new DataAccessException("Error: bad auth");
            }
            String username = auth.username();
            String role;
            GameData gameData = gameDAO.getGameByID(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Error: bad gameID");
            }

            if (username.equals(gameData.whiteUsername())) {
                role = "white";
            } else if (username.equals(gameData.blackUsername())) {
                role = "black";
            } else {
                role = "observer";
            }


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
            AuthData auth = authDAO.getAuthDataByAuthToken(command.getAuthToken());
            if (auth == null) {
                throw new DataAccessException("Error: bad auth");
            }

            String username = auth.username();
            int gameID = command.getGameID();

            WinnerData winnerData = gameDAO.getWinner(gameID);
            debug("winnerData: " + winnerData);
            if (winnerData.gameIsOver()) {
                ErrorMessage error = new ErrorMessage("Error: game is over");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }


            ChessMove move = command.getMove();
            String role = getRole(username, command.getGameID());
            GameData gameData = gameDAO.getGameByID(gameID);
            ChessGame chessGame = gameData.game();

            ChessGame.TeamColor playerColor = role.equals("white") ? ChessGame.TeamColor.WHITE : ChessGame.TeamColor.BLACK;

            if (chessGame.getTeamTurn() != playerColor) {
                ErrorMessage error = new ErrorMessage("Error: Not your turn");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }

            chessGame.makeMove(move);

            gameDAO.updateGame(gameID, gameData);

            LoadGameMessage loadGame = new LoadGameMessage(chessGame);
            for (Connection conn : connections.getConnectionsInGame(gameID)) {
                conn.send(new Gson().toJson(loadGame));
            }

            String moveDesc = String.format("%s moved from %s to %s", username,
                    posToString(move.getStartPosition()), posToString(move.getEndPosition()));
            NotificationMessage notifi  = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, moveDesc);
            connections.broadcastToOthers(gameID, username, notifi);

            ChessGame.TeamColor turn = chessGame.getTeamTurn();

            if (chessGame.isInStalemate(turn)) {
                connections.broadcastAll(gameID, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Stalemate!"));
            }
            if (chessGame.isInCheckmate(turn)) {
                connections.broadcastAll(gameID, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, "Checkmate!"));
            }else if (chessGame.isInCheck(turn)) {
                connections.broadcastAll(gameID, new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, turn + " is in check"));
            }

        } catch (InvalidMoveException | DataAccessException e) {
            session.getRemote().sendString(new Gson().toJson(new ErrorMessage("Error: " + e.getMessage())));
        }
    }

    public void leave(UserGameCommand command, Session session) throws IOException {
        try {
            AuthData auth = authDAO.getAuthDataByAuthToken(command.getAuthToken());
            String username = auth.username();
            String role;
            GameData gameData = gameDAO.getGameByID(command.getGameID());
            if (gameData == null) {
                throw new DataAccessException("Error: bad gameID");
            }

            if (username.equals(gameData.whiteUsername())) {
                role = "white";
            } else if (username.equals(gameData.blackUsername())) {
                role = "black";
            } else {
                role = "observer";
            }
            if (!role.equals("observer")){
                gameDAO.addUserToGame(null,command.getGameID(),role);
            }

            connections.remove(username, command.getGameID(), role);

            NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, username + " has left the game");
            connections.broadcastToOthers(command.getGameID(), username, notification);
        } catch (DataAccessException e) {
            ErrorMessage error = new ErrorMessage("Error: " + e.getMessage());
            session.getRemote().sendString(new Gson().toJson(error));
        }
    }

    public void resign(UserGameCommand command, Session session) throws IOException {
        try {

            WinnerData winner = gameDAO.getWinner(command.getGameID());
            if (winner.gameIsOver()) {
                ErrorMessage error = new ErrorMessage("Error: game is over");
                session.getRemote().sendString(new Gson().toJson(error));
                return;
            }
            AuthData auth = authDAO.getAuthDataByAuthToken(command.getAuthToken());
            String username = auth.username();
            String role = getRole(username, command.getGameID());

            WinnerData winnerData = new WinnerData(true, role , "resign");
            gameDAO.updateWinner(command.getGameID(), winnerData);

            NotificationMessage notification = new NotificationMessage(ServerMessage.ServerMessageType.NOTIFICATION, username + " has resigned");
            connections.broadcastAll(command.getGameID(), notification);
            connections.remove(username, command.getGameID(), role);
        } catch (DataAccessException e) {
            ErrorMessage error = new ErrorMessage("Error: " + e.getMessage());
            session.getRemote().sendString(new Gson().toJson(error));
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
