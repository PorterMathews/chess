package client;

import java.util.Arrays;
import java.util.List;

import model.*;
import exception.ResponseException;
import server.ServerFacade;

public class ChessClient {
    private String userName = null;
    private String authToken = null;
    private final ServerFacade server;
    private final String serverUrl;
    private State state = State.LOGGEDOUT;

    public ChessClient(String serverUrl) {
        server = new ServerFacade(serverUrl);
        this.serverUrl = serverUrl;
    }

    public String eval(String input) {
        try {
            var tokens = input.split(" ");
            var cmd = (tokens.length > 0) ? tokens[0].toLowerCase() : "help";;
            var params = Arrays.copyOfRange(tokens, 1, tokens.length);
            return switch (cmd) {
                case "register" -> register(params);
                case "login" -> login(params);
                case "logout" -> logout();
                case "create" -> create(params);
                case "list" -> list();
                case "join" -> join(params);
                case "observe" -> observe(params);
                case "back" -> back();
                case "quit" -> "quit";
                default -> help();
            };
        } catch (ResponseException ex) {
            return ex.getMessage();
        }
    }

    public String help() {
        if (state == State.LOGGEDOUT) {
            return """
                    - help <command>
                    - login <username> <password>
                    - register <username> <password> <email>
                    - quit - exits program
                    """;
        } else if (state == State.LOGGEDIN) {
            return """
                    - help <command>
                    - create <game name>
                    - list - list of all games
                    - join - <ID> <Colour>
                    - observe <ID>
                    - logout - logs you out, returning to login prompt
                    - quit - exits program
                    """;
        }
        return """
                - back - returns to logged in state
                - quit - exits program
                """;
    }

    public String register(String... params) throws ResponseException {
        if (params.length == 3 && state == State.LOGGEDOUT) {
            UserData userData = new UserData(params[0], params[1], params[2]);
            try {
                AuthData authData = server.register(userData);
                authToken = authData.authToken();
            } catch (ResponseException e) {
                throw new ResponseException(400, "Unable to register: " + e.getMessage());
            }
            state = State.LOGGEDIN;
            userName = params[0];
            return String.format("You registered as %s.", userName);
        }
        if (state != State.LOGGEDOUT) {
            throw new ResponseException(400, "already registered");
        }
        throw new ResponseException(400, "Expected: <username> <password> <email>");
    }

    public String login(String... params) throws ResponseException {
        if (params.length == 2 && state == State.LOGGEDOUT) {
            UserData userData = new UserData(params[0], params[1], null);
            try {
                AuthData authData = server.login(userData);
                authToken = authData.authToken();
            } catch (ResponseException e) {
                throw new ResponseException(400, "Unable to Login: " + e.getMessage());
            }
            state = State.LOGGEDIN;
            userName = params[0];
            return String.format("You logged in as %s.", userName);
        }
        if (state != State.LOGGEDOUT) {
            throw new ResponseException(400, "already logged in");
        }
        throw new ResponseException(400, "Expected: <username> <password>");
    }

    public String logout() throws ResponseException {
        if (state != State.LOGGEDOUT) {
            try {
                server.logout(authToken);
            } catch (ResponseException e) {
                throw new ResponseException(400, "Unable to Logout: " + e.getMessage());
            }
            state = State.LOGGEDOUT;
            authToken = null;
            return "you have logged out";
        }
        throw new ResponseException(400, "not logged in");
    }

    public String create(String... params) throws ResponseException {
        if (params.length == 1 && state == State.LOGGEDIN) {
            int gameID;
            try {
                gameID = server.crateGame(authToken, params[0]);
            } catch (ResponseException e) {
                throw new ResponseException(400, "Unable to creat game: " + e.getMessage());
            }
            return String.format("You created a game named " + params[0] + " with game ID: " + gameID);
        }
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "please log in first");
        }
        if (state == State.INGAME) {
            throw new ResponseException(400, "already in game");
        }
        throw new ResponseException(400, "Expected: <game name>");
    }

    public String list() throws ResponseException {
        if (state == State.LOGGEDIN) {
            List<GameData> gameList;
            try {
                gameList = server.listGames(authToken);
            } catch (ResponseException e) {
                throw new ResponseException(400, "Unable to generate list: " + e.getMessage());
            }
            return String.format(listFormater(gameList));
        }
        if (state == State.INGAME) {
            throw new ResponseException(400, "please exit game first");
        }
        throw new ResponseException(400, "please log in first");
    }

    public String join(String... params) throws ResponseException {
        if (params.length == 2 && state == State.LOGGEDIN) {
            List<GameData> gameList;
            try {
                gameList = server.listGames(authToken);
            } catch (ResponseException e) {
                throw new ResponseException(400, "Unable to generate list: " + e.getMessage());
            }
            //checking if they are already part of the game
            if (alreadyPartOfGame(gameList, Integer.parseInt(params[0]), params[1])) {
                state = State.INGAME;
                return String.format("Rejoining game " +params[0]+ " as " + params[1] + " player");
            }
            else {
                try {
                    server.joinGame(authToken, params[1], Integer.parseInt(params[0]));
                } catch (ResponseException e) {
                    throw new ResponseException(400, "Unable to join game: " + e.getMessage());
                }
            }
            state = State.INGAME;
            return String.format("Joined game " +params[0]+ " as " + params[1] + " player");
        }  else if (state == State.INGAME) {
            throw new ResponseException(400, "please exit game first");
        } else if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "please log in first");
        }
        throw new ResponseException(400, "Expected: <gameID> <white|black>");
    }

    public String observe(String... params) throws ResponseException {
        if (params.length == 1 && state == State.LOGGEDIN) {
            try {
                server.joinGame(authToken, params[1], Integer.parseInt(params[0]));
            } catch (ResponseException e) {
                throw new ResponseException(400, "Unable to join game: " + e.getMessage());
            }
            state = State.INGAME;
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

    private boolean alreadyPartOfGame(List<GameData> list, int gameID, String playerColour) {
        if (playerColour == null) {
            return false;
        }
        int i = 0;
        while (i < list.size()) {
            GameData gameData = list.get(i);
            if (gameData.gameID() == gameID &&
                    playerColour.equals("black") &&
                    userName.equals(gameData.whiteUsername())) {
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

    private String listFormater(List<GameData> list) {
        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < list.size()) {
            GameData gameData = list.get(i);
            result.append(String.format(i+1 + ". Name: " + gameData.gameName() +
                    ", Black username: " + nullToString(gameData.blackUsername()) +
                    ", White username: " + nullToString(gameData.whiteUsername()) +
                    ", gameID: " + gameData.gameID() + "\n"));
            i++;
        }
        return result.toString();
    }

    private String nullToString(String playerUsername) {
        if (playerUsername == null) {
            return "No user joined";
        }
        return playerUsername;
    }

    private void assertSignedIn() throws ResponseException {
        if (state == State.LOGGEDOUT) {
            throw new ResponseException(400, "You must sign in");
        }
    }
}
