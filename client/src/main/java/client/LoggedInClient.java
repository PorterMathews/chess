package client;

import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import chess.ChessGame;
import model.*;
import exception.ResponseException;
import server.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;

public class LoggedInClient {

    private static String userName = null;
    private static String authToken = null;
    private final ServerFacade server;
    private final String serverUrl;
    private static ChessGame.TeamColor playerColor;
    private static final boolean detailedErrorMsg = false;
    private static String errorMsg;
    private static final HashMap<Integer, Integer> ID_LOOKUP = new HashMap<>();
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;

    public LoggedInClient(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        server = new ServerFacade(serverUrl);
        playerColor = null;
        errorMsg = "";
    }

    /**
     *
     * @param input The incoming command
     * @return The outcome of the command
     */
    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";;
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "logout", "out" -> logout();
                case "create", "c" -> create(params);
                case "list", "ll", "ls", "l" -> list();
                case "join", "j" -> join(params);
                case "observe", "view", "ob", "o" -> observe(params);
                case "db", "erase" -> clear(params);
                case "quit", "q", ":q" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    /**
     *
     * @return The status of the logout
     * @throws ResponseException Used for bad inputs
     */
    public String logout() throws ResponseException {
        if (!Repl.getState().equals(State.LOGGEDOUT)) {
            try {
                server.logout(authToken);
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Unable to Logout " + errorMsg);
            }
            Repl.setState(State.LOGGEDOUT);
            Repl.setPrompt();
            authToken = null;
            return "Logged out";
        }
        throw new ResponseException(400, "Not logged in");
    }

    /**
     *
     * @param params the name of the game
     * @return the status of the creation
     * @throws ResponseException Used for bad inputs
     */
    public String create(String... params) throws ResponseException {
        if (params.length == 1 && Repl.getState().equals(State.LOGGEDIN)) {
            int gameID;
            try {
                gameID = server.crateGame(authToken, params[0]);
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Unable to creat game: " + errorMsg);
            }
            ID_LOOKUP.put(ID_LOOKUP.size() + 1, gameID);
            return String.format("You created a game named " + params[0]);
        }
        if (Repl.getState().equals(State.LOGGEDOUT)) {
            throw new ResponseException(400, "please log in first");
        }
        if (Repl.getState().equals(State.INGAME)) {
            throw new ResponseException(400, "already in game");
        }
        throw new ResponseException(400, "Expected: <game name>");
    }

    /**
     *
     * @return the status of getting the list
     * @throws ResponseException Used for bad inputs
     */
    public String list() throws ResponseException {
        if (Repl.getState().equals(State.LOGGEDIN)) {
            List<GameData> gameList;
            try {
                gameList = server.listGames(authToken);
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Unable to generate list: " + errorMsg);
            }
            return String.format(listFormater(gameList));
        }
        if (Repl.getState().equals(State.INGAME)) {
            throw new ResponseException(400, "please exit game first");
        }
        throw new ResponseException(400, "please log in first");
    }

    /**
     *
     * @param params game number from list, desired color
     * @return status of the join
     * @throws ResponseException Used for bad inputs
     */
    public String join(String... params) throws ResponseException, IOException {
        debug("joining game");
        if (params.length == 2 && Repl.getState().equals(State.LOGGEDIN) && isInteger(params[0])) {
            int game = Integer.parseInt(params[0]);
            String passedPlayerColor = params[1].toLowerCase();
            if (game > ID_LOOKUP.size() || game < 1) {
                throw new ResponseException(400, "Invalid game");
            }
            List<GameData> gameList;
            try {
                gameList = server.listGames(authToken);
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Unable to generate list " + errorMsg);
            }
            reloadGameIDs();
            setPlayerColor(passedPlayerColor);
            debug("looking up gameID");
            int gameID = ID_LOOKUP.get(game);
            debug("checking if part of game: " + gameID);
            if (alreadyPartOfGame(gameList, gameID, passedPlayerColor)) {
                if (ws != null) {
                    ws.close();
                }
                ws = new WebSocketFacade(serverUrl, notificationHandler);
                ws.connectToGame(authToken, gameID, false);
                Repl.setState(State.INGAME);
                Repl.setPrompt();
                GameClient.setGameID(gameID);
                setPlayerColor(params[1]);
                return String.format("Rejoining game " +params[0]+ " as " + params[1] + " player");
            }
            else {
                try {
                    server.joinGame(authToken, params[1], gameID, false);
                } catch (ResponseException e) {
                    if (detailedErrorMsg) {
                        errorMsg = "";
                        errorMsg = e.getMessage();
                    }
                    throw new ResponseException(400, "Unable to join game " + errorMsg);
                }
            }
            if (ws != null) {
                ws.close();
            }
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.connectToGame(authToken, gameID, false);
            Repl.setState(State.INGAME);
            Repl.setPrompt();
            GameClient.setGameID(gameID);
            setPlayerColor(params[1]);
            return String.format("Joined game " +params[0]+ " as " + params[1] + " player");
        }  else if (Repl.getState().equals(State.INGAME)) {
            throw new ResponseException(400, "please exit game first");
        } else if (Repl.getState().equals(State.LOGGEDOUT)) {
            throw new ResponseException(400, "please log in first");
        }
        throw new ResponseException(400, "Expected: <gameID> <white|black>");
    }

    /**
     *
     * @param params game number from list
     * @return the status of the observation request
     * @throws ResponseException Used for bad inputs
     */
    public String observe(String... params) throws ResponseException, IOException {
        if (params.length == 1 && Repl.getState().equals(State.LOGGEDIN) && isInteger(params[0])) {
            reloadGameIDs();
            int game = Integer.parseInt(params[0]);
            if (game > ID_LOOKUP.size() || game < 1) {
                throw new ResponseException(400, "Invalid game");
            }
            int gameID = ID_LOOKUP.get(game);
            debug("Observe game with ID: " + gameID);
            if (ws != null) {
                ws.close();
            }
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.connectToGame(authToken, gameID, true);
            debug("Made it past the web socket stuff");
            Repl.setState(State.INGAME);
            Repl.setPrompt();
            GameClient.setGameID(gameID);
            debug("Setting player to null");
            setPlayerColor(null);
            //DrawChessBoard.drawBoard(playerColor);
            return String.format("observing game " + params[0]);
        }  else if (Repl.getState().equals(State.INGAME)) {
            throw new ResponseException(400, "please exit game first");
        } else if (Repl.getState().equals(State.LOGGEDOUT)) {
            throw new ResponseException(400, "please log in first");
        }
        throw new ResponseException(400, "Expected: <gameID>");
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
            Repl.setPrompt();
            authToken = null;
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
                    - "create" <game name> - Makes a new game
                    - "list" - Shows a list of all games
                    - "join" <ID> <Colour> - Joins a game from the list as specified colour
                    - "observe" <ID> - Lets you observe a game in progress
                    - "logout" - logs you out, returning to login prompt
                    - "quit" - exits program""";
        }

    /**
     * Keeps games up to date in ID_LOOKUP
     * @throws ResponseException
     */
    public void reloadGameIDs() throws ResponseException {
        List<GameData> list;
        try {
            list = server.listGames(authToken);
        } catch (ResponseException e) {
            if (detailedErrorMsg) {
                errorMsg = "";
                errorMsg = e.getMessage();
            }
            throw new ResponseException(400, "Unable to generate list: " + errorMsg);
        }
        if (list.isEmpty()) {
            return;
        }
        int i = 0;
        ID_LOOKUP.clear();
        while (i < list.size()) {
            GameData gameData;
            gameData = list.get(i);
            i++;
            debug("adding to ID_LOOKUP: " + i + " and " + gameData.gameID());
            ID_LOOKUP.put(i , gameData.gameID());
        }
    }

    /**
     *
     * @param input string to be tested if integer
     * @return true if integer, else false
     */
    public static boolean isInteger(String input) {
        try {
            Integer.parseInt(input);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    /**
     *
     * @param list list of games in DB
     * @param gameID game ID you are searching for
     * @param playerColour The color you are checking against
     * @return true if the current player is  playerColor in that game, else false
     */
    private boolean alreadyPartOfGame(List<GameData> list, int gameID, String playerColour) {
        if (playerColour == null) {
            return false;
        }
        int i = 0;
        while (i < list.size()) {
            GameData gameData = list.get(i);
            if (gameData.gameID() == gameID &&
                    playerColour.equals("black") &&
                    userName.equals(gameData.blackUsername())) {
                return true;
            } else if (gameData.gameID() == gameID &&
                    playerColour.equals("white") &&
                    userName.equals(gameData.whiteUsername())){
                return true;
            }
            i++;
        }
        return false;
    }

    /**
     *
     * @param list list of game from the DB
     * @return a formated list of game for the list method
     */
    private String listFormater(List<GameData> list) throws ResponseException {
        if (list.isEmpty()) {
            return "No Games";
        }
        StringBuilder result = new StringBuilder();

        int i = 0;
        ID_LOOKUP.clear();
        while (i < list.size()) {
            GameData gameData;
            gameData = list.get(i);
            String finished = "";
            WinnerData winnerData = server.getGameOver(authToken, gameData.gameID());
            if (winnerData.gameIsOver()) {
                finished = ", FINISHED";
            }
            result.append(String.format(i+1 + ". Name: " + gameData.gameName() +
                    ", Black username: " + nullToString(gameData.blackUsername()) +
                    ", White username: " + nullToString(gameData.whiteUsername()) +
                    finished + "\n"));
            i++;
            debug("adding to ID_LOOKUP: " + i + " and " + gameData.gameID());
            ID_LOOKUP.put(i , gameData.gameID());
        }
        return result.toString();
    }

    /**
     *
     * @param playerUsername the username to be checked if null
     * @return "No user joined" if playerUsername in null, else playerUsername
     */
    private String nullToString(String playerUsername) {
        if (playerUsername == null) {
            return "No user joined";
        }
        return playerUsername;
    }

    /**
     *
     * @return player color for drawing board
     */
    public static String getPlayerColor() {
        debug("getting player by color");
        if (playerColor == null) {
            debug("got observer");
            return "observer";
        }
        if (playerColor.equals(ChessGame.TeamColor.WHITE)) {
            debug("got white");
            return "white";
        } else if (playerColor.equals(ChessGame.TeamColor.BLACK)) {
            debug("got black");
            return "black";
        }
        return "observer";
    }

    /**
     * used to set player color
     * @param string desired color, can be null
     */
    private void setPlayerColor(String string) {
        debug("setting player color");
        if (string == null) {
            debug("set to null");
            playerColor = null;
        }
        else if (string.equals("white")) {
            debug("set to white");
            playerColor = ChessGame.TeamColor.WHITE;
        } else if (string.equals("black")) {
            debug("set to black");
            playerColor = ChessGame.TeamColor.BLACK;
        }
    }

    /**
     *
     * @return the active Username
     */
    public static String getUsername() {
        return userName;
    }

    /**
     * sets username
     * @param name
     */
    public static void setUserName(String name) {
        userName = name;
    }

    /**
     * sets auth
     * @param auth
     */
    public static void setAuthToken(String auth) {
        authToken = auth;
    }

    /**
     * gets auth
     * @return
     */
    public static String getAuthToken() {
        return authToken;
    }

    /**
     * Prints only if detailed messaging is on
     * @param input string to be printed
     */
    private static void debug(String input) {
        if (detailedErrorMsg) {
            System.out.println(input);
        }
    }
}
