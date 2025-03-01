package server;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import dataaccess.DataAccessException;
import model.GameData;
import model.UserData;
import spark.Request;
import spark.Response;
import service.Service;
import service.UserService;
import service.GameService;
import java.util.Collection;
import java.util.Map;

public class Handler {

    private final UserService userService;
    private final GameService gameService;
    private final Service service;

    public Handler(Service service, UserService userService, GameService gameService) {
        this.service = service;
        this.userService = userService;
        this.gameService = gameService;
    }

    /**
     * @param req the incoming request
     * @param res the response, including status code
     * @return empty sting
     */
    public Object clearDatabase(Request req, Response res) {
        try {
            service.clearDatabase();
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
     * @return the Game ID
     */
    public Object createGame(Request req, Response res) {
        try{
            String authToken = req.headers("Authorization");
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
            String gameName = jsonObject.get("gameName").getAsString();
            int GameID = gameService.createGame(authToken, gameName);
            res.status(200);
            return new Gson().toJson(Map.of("gameID", GameID));
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
    public Object joinGame(Request req, Response res) {
        try {
            String authToken = req.headers("Authorization");
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
            String playerColor;
            int gameID;
            try {
                playerColor = jsonObject.get("playerColor").getAsString();
                gameID = jsonObject.get("gameID").getAsInt();
            }
            catch(Exception error) {
                res.status(400);
                return new Gson().toJson(Map.of("message", "Error: bad request"));
            }

            gameService.joinGame(authToken, playerColor, gameID);
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
}
