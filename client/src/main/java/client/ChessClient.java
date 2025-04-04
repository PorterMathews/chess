package client;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import model.*;
import exception.ResponseException;
import server.ServerFacade;
import client.websocket.NotificationHandler;
import client.websocket.WebSocketFacade;

public class ChessClient {
    private static String userName = null;
    private String authToken = null;
    private final ServerFacade server;
    private final String serverUrl;
    private static State state = State.LOGGEDOUT;
    private static String playerColor;
    private final boolean detailedErrorMsg = false;
    private String errorMsg;
    private static final HashMap<Integer, Integer> ID_LOOKUP = new HashMap<>();
    private final NotificationHandler notificationHandler;
    private WebSocketFacade ws;

    public ChessClient(String serverUrl, NotificationHandler notificationHandler) {
        this.serverUrl = serverUrl;
        this.notificationHandler = notificationHandler;
        server = new ServerFacade(serverUrl);
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
                case "register", "reg", "r" -> register(params);
                case "login", "in", "i" -> login(params);
                case "logout", "out" -> logout();
                case "create", "c" -> create(params);
                case "list", "ll", "ls", "l" -> list();
                case "join", "j" -> join(params);
                case "observe", "view", "ob", "o" -> observe(params);
                case "back", "b" -> back();
                case "db", "erase" -> clear(params);
                case "quit", "q", ":q" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    /**
     *
     * @param params username, password, and email
     * @return The status of the registration
     * @throws ResponseException Used for bad inputs
     */
    public String register(String... params) throws ResponseException {
        if (params.length == 3 && state == State.LOGGEDOUT) {
            UserData userData = new UserData(params[0], params[1], params[2]);
            try {
                AuthData authData = server.register(userData);
                authToken = authData.authToken();
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Username already taken " + errorMsg);
            }
            state = State.LOGGEDIN;
            userName = params[0];
            reloadGameIDs();
            return String.format("You registered as %s.", userName);
        }
        if (state != State.LOGGEDOUT) {
            throw new ResponseException(400, "already registered");
        }
        throw new ResponseException(400,"Expected: <username> <password> <email>");
    }

    /**
     *
     * @param params username, password
     * @return The status of the login
     * @throws ResponseException Used for bad inputs
     */
    public String login(String... params) throws ResponseException {
        if (params.length == 2 && state == State.LOGGEDOUT) {
            UserData userData = new UserData(params[0], params[1], null);
            try {
                AuthData authData = server.login(userData);
                authToken = authData.authToken();
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Invalid username or password " + errorMsg);
            }
            state = State.LOGGEDIN;
            userName = params[0];
            reloadGameIDs();
            return String.format("Success! Logged in as " + userName);
        }
        if (state != State.LOGGEDOUT) {
            throw new ResponseException(400, "Already logged in");
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    /**
     *
     * @return The status of the logout
     * @throws ResponseException Used for bad inputs
     */
    public String logout() throws ResponseException {
        if (state != State.LOGGEDOUT) {
            try {
                server.logout(authToken);
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Unable to Logout " + errorMsg);
            }
            state = State.LOGGEDOUT;
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
        if (params.length == 1 && state == State.LOGGEDIN) {
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
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "please log in first");
        }
        if (state == State.INGAME) {
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
        if (state == State.LOGGEDIN) {
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
        if (state == State.INGAME) {
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
    public String join(String... params) throws ResponseException {
        debug("joining game");
        if (params.length == 2 && state == State.LOGGEDIN && isInteger(params[0])) {
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
            debug("looking up gameID");
            int gameID = ID_LOOKUP.get(game);
            //debug("checking if part of game: " + gameID);
            if (alreadyPartOfGame(gameList, gameID, passedPlayerColor)) {
                ws = new WebSocketFacade(serverUrl, notificationHandler);
                ws.playerJoinsGame(params[0], params[1]);
                state = State.INGAME;
                playerColor = params[1];
                //DrawChessBoard.drawBoard(playerColor);
                return String.format("Rejoining game " +params[0]+ " as " + params[1] + " player");
            }
            else {
                try {
                    server.joinGame(authToken, params[1], gameID);
                } catch (ResponseException e) {
                    if (detailedErrorMsg) {
                        errorMsg = "";
                        errorMsg = e.getMessage();
                    }
                    throw new ResponseException(400, "Unable to join game " + errorMsg);
                }
            }
            ws = new WebSocketFacade(serverUrl, notificationHandler);
            ws.playerJoinsGame(params[0], params[1]);
            state = State.INGAME;
            playerColor = params[1];
            return String.format("Joined game " +params[0]+ " as " + params[1] + " player");
        }  else if (state == State.INGAME) {
            throw new ResponseException(400, "please exit game first");
        } else if (state == State.LOGGEDOUT) {
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
    public String observe(String... params) throws ResponseException {
        if (params.length == 1 && state == State.LOGGEDIN && isInteger(params[0])) {
            reloadGameIDs();
            int game = Integer.parseInt(params[0]);
            if (game > ID_LOOKUP.size() || game < 1) {
                throw new ResponseException(400, "Invalid game");
            }
            state = State.INGAME;
            playerColor = "white";
            //DrawChessBoard.drawBoard(playerColor);
            return String.format("observing game " + params[0]);
        }  else if (state == State.INGAME) {
            throw new ResponseException(400, "please exit game first");
        } else if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "please log in first");
        }
        throw new ResponseException(400, "Expected: <gameID>");
    }

    public String back() throws ResponseException {
        if (state == State.INGAME) {
            state = State.LOGGEDIN;
            return "Taking you back";
        } else if (state == State.LOGGEDIN) {
            throw new ResponseException(400, "please use logout instead");
        }
        throw new ResponseException(400, "Back, back to where? Try quit instead");
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
            state = State.LOGGEDOUT;
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
        if (state == State.LOGGEDOUT) {
            return """
                    Options:
                    - "help"
                    - "login" <username> <password> - Logs in an existing user
                    - "register" <username> <password> <email> - Creates a user account
                    - "quit" - exits program""";
        } else if (state == State.LOGGEDIN) {
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
        return """
                Options:
                - "back" - returns to previous menu
                - "quit" - exits program""";
    }

    /**
     * Keeps games up to date in ID_LOOKUP
     * @throws ResponseException
     */
    private void reloadGameIDs() throws ResponseException {
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
    private String listFormater(List<GameData> list) {
        if (list.isEmpty()) {
            return "No Games";
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        ID_LOOKUP.clear();
        while (i < list.size()) {
            GameData gameData;
            gameData = list.get(i);
            result.append(String.format(i+1 + ". Name: " + gameData.gameName() +
                    ", Black username: " + nullToString(gameData.blackUsername()) +
                    ", White username: " + nullToString(gameData.whiteUsername()) + "\n"));
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
     * @return the State for the Repl
     */
    public static State getState() {
        return state;
    }

    /**
     *
     * @return player color for drawing board
     */
    public static String getPlayerColor() {
        return playerColor;
    }

    /**
     *
     * @return the active Username
     */
    public static String getUsername() {
        return userName;
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
