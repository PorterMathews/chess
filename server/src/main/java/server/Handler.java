package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.*;
import model.GameData;
import model.UserData;
import model.WinnerData;
import spark.Request;
import spark.Response;
import service.AuthService;
import service.UserService;
import service.GameService;
import java.util.Collection;
import java.util.Map;

public class Handler {

    private final UserService userService;
    private final GameService gameService;
    private final AuthService authService;

    public Handler(boolean useSQL) {
        //System.out.println("start");
        AuthDAO authDAO;
        UserDAO userDAO;
        GameDAO gameDAO;

        if (useSQL) {
            authDAO = new SQLAuthDAO();
            userDAO = new SQLUserDAO();
            gameDAO = new SQLGameDAO();
        } else {
            authDAO = new MemoryAuthDAO();
            userDAO = new MemoryUserDAO();
            gameDAO = new MemoryGameDAO();
        }

        //System.out.println("Using DAO: " + userDAO.getClass().getSimpleName());

        this.userService = new UserService(authDAO, userDAO);
        this.gameService = new GameService(authDAO, gameDAO);
        this.authService = new AuthService(authDAO, userDAO, gameDAO);

        if (useSQL) {
            new MySqlDataAccess();
        }
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return empty sting
     */
    public Object clearDatabase(Request req, Response res) {
        try {
            authService.clearAuthData();
            gameService.clearGameData();
            userService.clearUserData();
            res.status(200);
            return "";
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return authData
     */
    public Object register(Request req, Response res) {
        try {
            var userData = new Gson().fromJson(req.body(), UserData.class);
            var authData = userService.register(userData);
            res.status(200);
            return new Gson().toJson(authData);
        }
        catch (DataAccessException error) {
            if (error.getMessage().equals("Bad request")) {
                res.status(400);
            } else if (error.getMessage().equals("Username already taken")) {
                res.status(403);
            }
            return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return authData
     */
    public Object login(Request req, Response res) {
        try {
            var userData = new Gson().fromJson(req.body(), UserData.class);
            var authData = userService.login(userData);
            res.status(200);
            return new Gson().toJson(authData);
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized")) {
                res.status(401);
            }
            return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return empty sting
     */
    public Object logout(Request req, Response res) {
        try{
            String authToken = req.headers("Authorization");
            userService.logout(authToken);
            res.status(200);
            return "";
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized Token")) {
                res.status(401);
            } return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return the Game ID
     */
    public Object createGame(Request req, Response res) {
        try{
            String authToken = req.headers("Authorization");
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
            String gameName = jsonObject.get("gameName").getAsString();
            int gameID = gameService.createGame(authToken, gameName);
            res.status(200);
            return new Gson().toJson(Map.of("gameID", gameID));
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized to Create Game")) {
                res.status(401);
            } return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return empty sting
     */
    public Object joinGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();

            String playerColor = jsonObject.has("playerColor") ? jsonObject.get("playerColor").getAsString() : null;
            boolean removePlayer = jsonObject.has("removePlayer") && jsonObject.get("removePlayer").getAsBoolean();
            Integer gameID = jsonObject.has("gameID") && !jsonObject.get("gameID").isJsonNull()
                    ? jsonObject.get("gameID").getAsInt()
                    : null;

            if (playerColor == null || gameID == null) {
                res.status(400);
                return new Gson().toJson(Map.of("message", "Error: bad request - missing fields"));
            }

            gameService.joinGame(authToken, playerColor, gameID, removePlayer);
            res.status(200);
            return "";
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized")) {
                res.status(401);
            } else if (error.getMessage().equals("Username already taken") || error.getMessage().equals("Players full for game")) {
                res.status(403);
            } else if (error.getMessage().equals("bad request")) {
                res.status(400);
            }
            return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return a list of all games
     */
    public Object getGames(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            Collection<GameData> gameData = gameService.getGames(authToken);
            res.status(200);
            return new Gson().toJson(Map.of("games", gameData));
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized to Get Game")) {
                res.status(401);
            } return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    public Object updateGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            var gameData = new Gson().fromJson(req.body(), GameData.class);
            int gameID = Integer.parseInt(req.params("id"));
            gameService.updateGame(authToken, gameID, gameData);
            res.status(200);
            return "";
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized to update game")) {
                res.status(401);
            } return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    public Object updateWinner(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            var winnerData = new Gson().fromJson(req.body(), WinnerData.class);
            int gameID = Integer.parseInt(req.params("id"));
            gameService.updateWinner(authToken, gameID, winnerData);
            res.status(200);
            return "";
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized to update Winner")) {
                res.status(401);
            } return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    public Object getWinner(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            int gameID = Integer.parseInt(req.params("id"));
            WinnerData winnerData = gameService.getWinner(authToken, gameID);
            res.status(200);
            return new Gson().toJson(winnerData);
        }
        catch(DataAccessException error) {
            if (error.getMessage().equals("Unauthorized to get winner")) {
                res.status(401);
            } return new Gson().toJson(Map.of("message","Error: "+ error.getMessage()));
        }
        catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }
}
