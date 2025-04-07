package client;

import java.util.*;

import chess.*;
import model.*;
import exception.ResponseException;
import server.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;

import static ui.EscapeSequences.SET_TEXT_COLOR_BLUE;
import static ui.EscapeSequences.SET_TEXT_COLOR_LIGHT_GREY;

public class GameClient {
    private final ServerFacade server;
    private final String serverUrl;
    private static final boolean detailedErrorMsg = true;
    private static String errorMsg;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private LoggedInClient postClient;
    private static int gameID;
    private DrawChessBoard drawChessBoard;
    private ChessBoard board;
    private ChessGame chessGame;
    private static final Map<Integer, Boolean> gameOverMap = new HashMap<>();
    Scanner scanner = new Scanner(System.in);
    private static final String IN_GAME_COLOR = SET_TEXT_COLOR_BLUE;
    private static final String GAME_COLOR = SET_TEXT_COLOR_LIGHT_GREY;


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
        } catch (InvalidMoveException e) {
            throw new RuntimeException(e);
        }
    }

    public String move(String... params) throws ResponseException, InvalidMoveException {
        checkIfGameIsOver();
        if (params.length == 2 && params[0].length() == 2 && params[1].length() == 2) {
            String pattern = "^[a-h][1-8]$";
            if (params[0].matches(pattern) && params[1].matches(pattern)) {
                refreshGameState();
                ChessPosition currentPosition = inputToPosition(params[0]);
                ChessPosition targetPosition = inputToPosition(params[1]);
                ChessPiece pieceCurrent = board.getPiece(currentPosition);
                if (pieceCurrent == null) {
                    throw new ResponseException(400, "No piece at target location");
                }
                if (board.isValidMove(currentPosition, pieceCurrent)) {
                    throw new ResponseException(400, "Not a valid move");
                }
                ChessMove move = new ChessMove(currentPosition, targetPosition, null);
                try {
                    chessGame.makeMove(move);
                } catch (InvalidMoveException e) {
                    throw new ResponseException(400, "Not a valid move: " + e.getMessage());
                }
                server.updateGame(LoggedInClient.getAuthToken(), gameID, chessGame);
                refreshGameState();
                return String.format(DrawChessBoard.drawBoard(LoggedInClient.getPlayerColor(), board, null)+"\nMove made!");
            }
        }
        throw new ResponseException(400, "Expected: <current space> <target space> i.e. <b1> <c3>");
    }

    public String redraw() throws ResponseException {
        refreshGameState();
        return DrawChessBoard.drawBoard(postClient.getPlayerColor(), board, null);
    }

    public String highlight(String... params) throws ResponseException {
        if (params.length == 1 && params[0].length() == 2) {
            String pattern = "^[a-h][1-8]$";
            if (params[0].matches(pattern)) {
                ChessPosition position = inputToPosition(params[0]);
                if (chessGame.getBoard().getPiece(position) == null) {
                    throw new ResponseException(400, "No piece at location");
                }
                Collection<ChessMove> moves = chessGame.validMoves(position);
                if (moves.isEmpty()) {
                    throw new ResponseException(400, "No valid moves for that piece");
                }
                System.out.println(DrawChessBoard.drawBoard(LoggedInClient.getPlayerColor(), board, moves));
                return "Highlighted!";
            }
        }
        throw new ResponseException(400, "Expected: <space> i.e. <b1> <c3>");
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
        checkIfGameIsOver();
        try {
            server.joinGame(LoggedInClient.getAuthToken(), LoggedInClient.getPlayerColor(), gameID, true);
        } catch (ResponseException e) {
            if (detailedErrorMsg) {
                errorMsg = "";
                errorMsg = e.getMessage();
            }
            throw new ResponseException(400, "Unable to process leave game " + errorMsg);
        }
        Repl.setState(State.LOGGEDIN);
        Repl.setPrompt();
        return "you have left the game";
    }

    public String resign() throws ResponseException {
        checkIfGameIsOver();
        String response = "";
        while (!response.equals("yes") && !response.equals("no") &&
                !response.equals("y") && !response.equals("n")) {
            System.out.println(IN_GAME_COLOR + "Are you sure you want to resign?");
            System.out.print(GAME_COLOR + "[Resign?] yes/no >>> ");
            String line = scanner.nextLine();
            var tokens = line.split(" ");
            if (tokens.length > 0)
                response = tokens[0].toLowerCase();
        }

        if (response.equals("yes") || response.equals("y")) {
            gameOverMap.put(gameID, true);
            return "You have resigned. GG!";
        }

        return "You did not resign, phew";
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

    private void checkIfGameIsOver() throws ResponseException {
        if (gameOverMap.getOrDefault(gameID, false)) {
            throw new ResponseException(400, "The game is over");
        }
    }

    private ChessPosition inputToPosition  (String input) {
        int col = input.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(input.charAt(1));
        return new ChessPosition(row, col);
    }

    public void refreshGameState() {
        try {
            List<GameData> gameList = server.listGames(LoggedInClient.getAuthToken());
            for (GameData game : gameList) {
                if (game.gameID() == gameID) {
                    this.chessGame = game.game();
                    this.board = chessGame.getBoard();
                }
            }
        } catch (ResponseException e) {
            throw new RuntimeException("Could not refresh game state: " + e.getMessage());
        }
    }

    public ChessBoard getChessBoard() {
        if (board == null) {
            refreshGameState();
        }
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
