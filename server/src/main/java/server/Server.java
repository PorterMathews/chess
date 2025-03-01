package server;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.*;
import service.Service;
import service.UserService;
import service.GameService;
import spark.*;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.Collection;
import java.util.Map;

public class Server {
    private final Service service;
    private final UserService userService;
    private final GameService gameService;

    public Server() {
        this.service = new Service(new MemoryDataAccess());
        this.userService = new UserService(new MemoryDataAccess());
        this.gameService = new GameService(new MemoryDataAccess());
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.delete("/db", this::clearDatabase);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);
        Spark.post("/game", this::createGame);
        Spark.put("/game", this::joinGame);
        Spark.get("/game", this::getGames);

        Spark.awaitInitialization();
        return Spark.port();
    }

    public int port() {
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    private Object clearDatabase(Request req, Response res) throws DataAccessException {
        try {
            service.clearDatabase();
            res.status(200);
            return "";
        } catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    private Object register(Request req, Response res) throws DataAccessException {
        try {
            var userData = new Gson().fromJson(req.body(), UserData.class);
            var authData = userService.register(userData);
            res.status(200);
            return new Gson().toJson(authData);
        } catch (DataAccessException error) {
            if (error.getMessage().equals("Error: Bad Request")) {
                res.status(400);
            } else if (error.getMessage().equals("Error: Username already taken")) {
                res.status(403);
            }
            return new Gson().toJson(Map.of("message", error.getMessage()));
        } catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    private Object login(Request req, Response res) throws DataAccessException {
        try {
            var userData = new Gson().fromJson(req.body(), UserData.class);
            var authData = userService.login(userData);
            res.status(200);
            return new Gson().toJson(authData);
        } catch(DataAccessException error) {
            if (error.getMessage().equals("Error: Unauthorized")) {
                res.status(401);
            }
            return new Gson().toJson(Map.of("message", error.getMessage()));
        } catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    private Object logout(Request req, Response res) throws DataAccessException {
        try{
            String authToken = req.headers("Authorization");
            userService.logout(authToken);
            res.status(200);
            return "";
        } catch(DataAccessException error) {
            if (error.getMessage().equals("Error: Unauthorized")) {
                res.status(401);
            }
            return new Gson().toJson(Map.of("message", error.getMessage()));
        } catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    private Object createGame(Request req, Response res) throws DataAccessException {
        try{
            String authToken = req.headers("Authorization");
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
            String gameName = jsonObject.get("gameName").getAsString();
            int GameID = gameService.createGame(authToken, gameName);
            res.status(200);
            return new Gson().toJson(Map.of("gameID", GameID));
        } catch(DataAccessException error) {
            if (error.getMessage().equals("Error: Unauthorized")) {
                res.status(401);
            }
            return new Gson().toJson(Map.of("message", error.getMessage()));
        } catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    private Object joinGame(Request req, Response res) throws DataAccessException {
        try {
            String authToken = req.headers("Authorization");
            JsonObject jsonObject = JsonParser.parseString(req.body()).getAsJsonObject();
            String playerColor;
            int gameID;

            try {
                playerColor = jsonObject.get("playerColor").getAsString();
                gameID = jsonObject.get("gameID").getAsInt();
            } catch(Exception error) {
                res.status(400);
                return new Gson().toJson(Map.of("message", "Error: bad request"));
            }

            gameService.joinGame(authToken, playerColor, gameID);
            res.status(200);
            return "";
        } catch(DataAccessException error) {
            if (error.getMessage().equals("Error: Unauthorized")) {
                res.status(401);
            } else if (error.getMessage().equals("Error: Username already taken")) {
                res.status(403);
            } else if (error.getMessage().equals("Error: bad request")) {
                res.status(400);
            }
            return new Gson().toJson(Map.of("message", error.getMessage()));
        }  catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }

    private Object getGames(Request req, Response res) throws DataAccessException {
        try {
            String authToken = req.headers("Authorization");
            Collection<GameData> gameData = gameService.getGames(authToken);
            res.status(200);
            return new Gson().toJson(Map.of("games", gameData));
        } catch(DataAccessException error) {
            if (error.getMessage().equals("Error: Unauthorized")) {
                res.status(401);
            }
            return new Gson().toJson(Map.of("message", error.getMessage()));
        } catch (Exception error) {
            res.status(500);
            return new Gson().toJson(Map.of("message", "Error: Internal Server Error"));
        }
    }
}
