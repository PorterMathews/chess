package client;

import java.util.Arrays;
import java.util.List;
import chess.ChessBoard;
import chess.ChessGame;
import com.google.gson.Gson;
import model.*;
import exception.ResponseException;
import server.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;

public class GameClient {
    private final ServerFacade server;
    private final String serverUrl;
    private static final boolean detailedErrorMsg = false;
    private static String errorMsg;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private LoggedInClient postClient;
    private static int gameID;
    private DrawChessBoard drawChessBoard;
    private static ChessBoard board;

    public GameClient(String serverUrl, LoggedInClient postClient, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        server = new ServerFacade(serverUrl);
        this.postClient = postClient;
        errorMsg = "";
    }


    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";;
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "move", "m" -> move(params);
                case "redraw", "r" -> redraw();
                case "highlight", "h" -> highlight(params);
                case "back", "b" -> back();
                case "leave", "l" -> leave();
                case "resign", "re" -> resign();
                case "db", "erase" -> clear(params);
                case "quit", "q", ":q" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String move(String... params) throws ResponseException {
        if (params.length == 2 && params[0].length() == 2 && params[1].length() == 2) {
            String pattern = "^[a-h][1-8]$";
            if (params[0].matches(pattern) && params[1].matches(pattern)) {

            }
        }
        throw new ResponseException(400, "Expected: <current space> <target space> i.e. <b1> <c3>");
    }

    public String redraw() throws ResponseException {
        setChessBoard();
        return DrawChessBoard.drawBoard(postClient.getPlayerColor());
    }

    public String highlight(String... params) throws ResponseException {
        return "";
    }

    public String back() throws ResponseException {
        if (Repl.getState().equals(State.INGAME)) {
            Repl.setState(State.LOGGEDIN);
            return "Taking you back";
        } else if (Repl.getState().equals(State.LOGGEDIN)) {
            throw new ResponseException(400, "please use logout instead");
        }
        throw new ResponseException(400, "Back, back to where? Try quit instead");
    }

    public String leave() throws ResponseException {
        return "";
    }

    public String resign() throws ResponseException {
        return "";
    }

    /**
     *
     * @param params the password
     * @return the status of the clear
     * @throws ResponseException Used for bad inputs
     */
    public String clear(String... params) throws ResponseException {
        if (params.length == 1 && params[0].equals("1")) {
            try {
                server.clearDatabase();
            } catch (ResponseException e) {
                errorMsg = "";
                errorMsg = e.getMessage();
                throw new ResponseException(400, "Unable to clearDB: " + errorMsg);
            }
            Repl.setState(State.LOGGEDOUT);
            LoggedInClient.setAuthToken(null);
            return "db cleared";
        }
        throw new ResponseException(400, "No");
    }

    /**
     *
     * @return options for different states
     */
    public String help() {
        return """
                Options:
                - "help"
                - "move" <current space> <desired space> - moves the piece from the current space to the indicated space
                - "redraw" - redraws the chess board
                - "highlight" <space> - highlights the moves available to the piece at the space
                - "back" - exits the game but keeps you as a player
                - "leave" - exits the game and removes you as a player
                - "resign" - forfeit the game (only available to player)
                - "quit" - exits program""";
    }

    public void setChessBoard() {
        List<GameData> gameList;
        try {
            gameList = server.listGames(LoggedInClient.getAuthToken());
        } catch (ResponseException e) {
            if (detailedErrorMsg) {
                errorMsg = "";
                errorMsg = e.getMessage();
            }
            throw new RuntimeException("Broke Chess Game Getter 1");
        }
         for (GameData game : gameList) {
             if (game.gameID() == gameID) {
                 board = new Gson().fromJson((new Gson().toJson(ChessGame.getBoard())), ChessBoard.class);
             }
         }
    }

    public static ChessBoard getChessBoard() {
        return board;
    }

    public static void setGameID(int ID) {
        gameID = ID;
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
