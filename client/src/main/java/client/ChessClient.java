package client;

import java.util.Arrays;
import model.*;
import exception.ResponseException;
import server.ServerFacade;

public class ChessClient {
    private final ServerFacade server;
    private final String serverUrl;
    private final boolean detailedErrorMsg = false;
    private String errorMsg;
    private LoggedInClient postClient;


    public ChessClient(String serverUrl, LoggedInClient postClient) {
        this.serverUrl = serverUrl;
        server = new ServerFacade(serverUrl);
        this.postClient = postClient;
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
        if (params.length == 3 && Repl.getState().equals(State.LOGGEDOUT)) {
            UserData userData = new UserData(params[0], params[1], params[2]);
            try {
                AuthData authData = server.register(userData);
                LoggedInClient.setAuthToken(authData.authToken());
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Username already taken " + errorMsg);
            }
            Repl.setState(State.LOGGEDIN);
            Repl.setPrompt();
            LoggedInClient.setUserName(params[0]);
            postClient.reloadGameIDs();
            return String.format("You registered as %s.", LoggedInClient.getUsername());
        }
        if (!Repl.getState().equals(State.LOGGEDOUT)) {
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
        if (params.length == 2 && Repl.getState().equals(State.LOGGEDOUT)) {
            UserData userData = new UserData(params[0], params[1], null);
            try {
                AuthData authData = server.login(userData);
                LoggedInClient.setAuthToken(authData.authToken());;
            } catch (ResponseException e) {
                if (detailedErrorMsg) {
                    errorMsg = "";
                    errorMsg = e.getMessage();
                }
                throw new ResponseException(400, "Invalid username or password " + errorMsg);
            }
            Repl.setState(State.LOGGEDIN);
            Repl.setPrompt();
            LoggedInClient.setUserName(params[0]);
            postClient.reloadGameIDs();
            return String.format("Success! Logged in as " + LoggedInClient.getUsername());
        }
        if (!Repl.getState().equals(State.LOGGEDOUT)) {
            throw new ResponseException(400, "Already logged in");
        }
        throw new ResponseException(400, "Expected: <username> <password>");
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
            LoggedInClient.setAuthToken(null);
            return "db cleared";
        }
        throw new ResponseException(400, "");
    }

    /**
     *
     * @return options for different states
     */
    public String help() {
        return """
                Options:
                - "help"
                - "login" <username> <password> - Logs in an existing user
                - "register" <username> <password> <email> - Creates a user account
                - "quit" - exits program""";
    }
}