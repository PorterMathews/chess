package server;

import dataaccess.*;
import service.Service;
import service.UserService;
import service.GameService;
import spark.*;

public class Server {
    private Service service;
    private UserService userService;
    private GameService gameService;
    private Handler handler;

    private void init() {
        try {
            var userDAO = new SQLUserDAO();
            var authDAO = new SQLAuthDAO();
            var gameDAO = new SQLGameDAO();

            this.service = new Service(userDAO, authDAO, gameDAO);
            this.userService = new UserService(userDAO);
            this.gameService = new GameService(userDAO, authDAO, gameDAO);

        } catch (RuntimeException e) {
            throw new RuntimeException("Server failed to initialize due to error", e);
        }
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

        Spark.delete("/db", Service::clearDatabase);
        Spark.post("/user", UserService::register);
        Spark.post("/session", UserService::login);
        Spark.delete("/session", Service::logout);
        Spark.post("/game", GameService::createGame);
        Spark.put("/game", GameService::joinGame);
        Spark.get("/game", GameService::getGames);

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
