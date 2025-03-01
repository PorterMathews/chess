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
    private final Handler handler;

    public Server() {
        this.service = new Service(new MemoryDataAccess());
        this.userService = new UserService(new MemoryDataAccess());
        this.gameService = new GameService(new MemoryDataAccess());
        this.handler = new Handler(service, userService, gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

        // Register your endpoints and handle exceptions here.
        Spark.delete("/db", handler::clearDatabase);
        Spark.post("/user", handler::register);
        Spark.post("/session", handler::login);
        Spark.delete("/session", handler::logout);
        Spark.post("/game", handler::createGame);
        Spark.put("/game", handler::joinGame);
        Spark.get("/game", handler::getGames);

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
}
