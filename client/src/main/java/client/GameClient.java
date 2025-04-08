package client;

import java.io.IOException;
import java.util.*;
import chess.*;
import model.*;
import exception.ResponseException;
import server.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;

import static ui.EscapeSequences.*;

public class GameClient {
    private final ServerFacade server;
    private final String serverUrl;
    private static final boolean DETAILED_ERROR_MSG = false;
    private static String errorMsg;
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;
    private LoggedInClient postClient;
    private static int gameID;
    private ChessBoard board;
    private ChessGame chessGame;
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

    /**
     *
     * @param input from the user
     * @return something to be printed out
     */
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
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     *
     * @param params input
     * @return something to be printed
     * @throws ResponseException
     * @throws InvalidMoveException
     * @throws IOException
     */
    public String move(String... params) throws ResponseException, InvalidMoveException, IOException {
        checkIfGameIsOver();
        checkObserver();
        checkTurn();
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
                ChessMove move;
                debug("you are moving " + pieceCurrent.getPieceType());
                if (pieceCurrent.getPieceType().equals(ChessPiece.PieceType.PAWN) && (targetPosition.getRow() == 1 || targetPosition.getRow() == 8)) {
                    ChessPiece.PieceType piece = askPiecePromotion();
                    move = new ChessMove(currentPosition, targetPosition, piece);
                } else {
                    move = new ChessMove(currentPosition, targetPosition, null);
                }
                if (ws != null) {
                    ws.close();
                }
                ws = new WebSocketFacade(serverUrl, notificationHandler);
                ws.makeMove(LoggedInClient.getAuthToken(), gameID, false, move);

                try {
                    chessGame.makeMove(move);
                } catch (InvalidMoveException e) {
                    throw new ResponseException(400, "Not a valid move: " + e.getMessage());
                }
                server.updateGame(LoggedInClient.getAuthToken(), gameID, chessGame);
                refreshGameState();
                String result = "";
                if (chessGame.isInCheck(chessGame.getTeamTurn())) {
                    result = (getTeamTurn() + " is in check");
                }
                if (chessGame.isInStalemate(chessGame.getTeamTurn())) {
                    result = ("It is a stalemate!");
                    server.setGameOver(LoggedInClient.getAuthToken(), gameID, null, "stalemate");
                }
                if (chessGame.isInCheckmate(chessGame.getTeamTurn())) {
                    result = ("CHECKMATE!\n" + getOppositeTeam() + " won");
                    server.setGameOver(LoggedInClient.getAuthToken(), gameID, getOppositeTeam(), "checkmate");
                }
//                return String.format(DrawChessBoard.drawBoard(LoggedInClient.getPlayerColor(), board, null)+
//                        IN_GAME_COLOR + "Move made!\n" +result + RESET_TEXT_COLOR);
                return String.format(IN_GAME_COLOR + "Move made!\n" +result + RESET_TEXT_COLOR);
            }
        }
        throw new ResponseException(400, "Expected: <current space> <target space> i.e. <b1> <c3>");
    }

    /**
     *
     * @return the board
     * @throws ResponseException
     */
    public String redraw() throws ResponseException {
        refreshGameState();
        return DrawChessBoard.drawBoard(postClient.getPlayerColor(), board, null);
    }

    /**
     * called to highlight the moves for a certain piece
     * @param params piece location
     * @return
     * @throws ResponseException
     */
    public String highlight(String... params) throws ResponseException {
        checkIfGameIsOver();
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
                return "Highlighted move from "+params[0]+"!";
            }
        }
        throw new ResponseException(400, "Expected: <space> i.e. <b1> <c3>");
    }

    /**
     *
     * @return response from taking you back
     * @throws ResponseException
     */
    public String back() throws ResponseException {
        if (Repl.getState().equals(State.INGAME)) {
            Repl.setState(State.LOGGEDIN);
            return "Taking you back";
        } else if (Repl.getState().equals(State.LOGGEDIN)) {
            throw new ResponseException(400, "please use logout instead");
        }
        throw new ResponseException(400, "Back, back to where? Try quit instead");
    }

    /**
     * able to be called
     * @return
     * @throws ResponseException
     * @throws IOException
     */
    public String leave() throws ResponseException, IOException {
        checkIfGameIsOver();
        if (ws != null) {
            ws.close();
        }
        ws = new WebSocketFacade(serverUrl, notificationHandler);
        ws.leaveGame(LoggedInClient.getAuthToken(), gameID, false);
        try {
            server.joinGame(LoggedInClient.getAuthToken(), LoggedInClient.getPlayerColor(), gameID, true);
        } catch (ResponseException e) {
            if (DETAILED_ERROR_MSG) {
                errorMsg = "";
                errorMsg = e.getMessage();
            }
            throw new ResponseException(400, "Unable to process leave game " + errorMsg);
        }
        Repl.setState(State.LOGGEDIN);
        Repl.setPrompt();
        return "you have left the game";
    }

    /**
     *
     * @return response
     * @throws ResponseException
     * @throws IOException
     */
    public String resign() throws ResponseException, IOException {
        checkIfGameIsOver();
        checkObserver();
        String response = "";
        while (!response.equals("yes") && !response.equals("no") &&
                !response.equals("y") && !response.equals("n")) {
            System.out.println(IN_GAME_COLOR + "Are you sure you want to resign?");
            System.out.print(GAME_COLOR + "[Resign?] yes/no >>> ");
            String line = scanner.nextLine();
            var tokens = line.split(" ");
            if (tokens.length > 0) {
                response = tokens[0].toLowerCase();
            }
        }

        if (response.equals("yes") || response.equals("y")) {
            if (LoggedInClient.getPlayerColor().equals("black")) {
                server.setGameOver(LoggedInClient.getAuthToken(), gameID, "white", "resign");
            } else {
                server.setGameOver(LoggedInClient.getAuthToken(), gameID, "black", "resign");
            }
            if (ws != null) {
                ws.close();
            }
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.resign(LoggedInClient.getAuthToken(), gameID, false);
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

    /**
     * throws error if it isn't your turn
     * @throws ResponseException
     */
    private void checkTurn() throws ResponseException {
        refreshGameState();
        if (!getTeamTurn().equals(LoggedInClient.getPlayerColor())) {
            throw new ResponseException(400, "Not your turn");
        }
        if (LoggedInClient.getPlayerColor().equals("observer")) {

        }
    }

    /**
     * throws error if you are an observer, use to restrict actions
     * @throws ResponseException
     */
    private void checkObserver() throws ResponseException {
        debug("you are playing as " + LoggedInClient.getPlayerColor());
        if (LoggedInClient.getPlayerColor().equals("observer")) {
            throw new ResponseException(400, "Observers can't perform this action");
        }
    }

    /**
     *
     * @return what team you're on
     */
    private String getTeamTurn() {
        if (chessGame.getTeamTurn().equals(ChessGame.TeamColor.WHITE)) {
            return "white";
        } else {
            return "black";
        }
    }

    /**
     *
     * @return the opposite team color
     */
    private String getOppositeTeam() {
        if (getTeamTurn().equals("white")) {
            return "black";
        } else {
            return "white";
        }
    }

    /**
     * If game is over, tells you how it is. Used to restrict actions
     * @throws ResponseException
     */
    private void checkIfGameIsOver() throws ResponseException {
        WinnerData winnerData = server.getGameOver(LoggedInClient.getAuthToken(), gameID);
        if (winnerData.gameIsOver()) {
            if (winnerData.winningColor() == null) {
                throw new ResponseException(400, "game is over\nThe game ended in a stalemate");
            } else {
                throw new ResponseException(400, "game is over\n" + winnerData.winningColor() + " won by " + winnerData.winningMethod());
            }
        }
    }

    /**
     * takes user input and turns into a chess position
     * @param input from user
     * @return
     */
    private ChessPosition inputToPosition  (String input) {
        int col = input.charAt(0) - 'a' + 1;
        int row = Character.getNumericValue(input.charAt(1));
        return new ChessPosition(row, col);
    }

    /**
     * makes sure game board is up to date
     */
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

    /**
     * Helps with pawn promotion
     * @return
     */
    private ChessPiece.PieceType askPiecePromotion() {
        String response = "";
        while (true) {
            System.out.println(IN_GAME_COLOR + "What piece would you like to promote to? Queen, Bishop, Knight or Rook)");
            String line = scanner.nextLine();
            var tokens = line.split(" ");
            if (tokens.length > 0) {
                response = tokens[0].toLowerCase();
            }
            if (response.equals("queen") || response.equals("q")){
                return ChessPiece.PieceType.QUEEN;
            }
            if (response.equals("bishop") || response.equals("b")){
                return ChessPiece.PieceType.BISHOP;
            }
            if (response.equals("knight") || response.equals("k") || response.equals("n")) {
                return ChessPiece.PieceType.KNIGHT;
            }
            if (response.equals("rook") || response.equals("r")){
                return ChessPiece.PieceType.ROOK;
            }
        }
    }

    /**
     * sets id
     * @param id
     */
    public static void setGameID(int id) {
        gameID = id;
    }

    /**
     * Prints only if detailed messaging is on
     * @param input string to be printed
     */
    private void debug(String input) {
        if (DETAILED_ERROR_MSG) {
            System.out.println(input);
        }
    }
}
