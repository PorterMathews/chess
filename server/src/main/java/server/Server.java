package server;

import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import service.Service;
import service.UserService;
import service.GameService;
import spark.*;

public class Server {
    private final Service service;
    private final UserService userService;
    private final GameService gameService;
    private final Handler handler;

    public Server() {
        this(new MemoryDataAccess());
    }

    public Server(DataAccess dataAccess) {
        this.service = new Service(dataAccess);
        this.userService = new UserService(dataAccess);
        this.gameService = new GameService(dataAccess);
        this.handler = new Handler(service, userService, gameService);
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

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
