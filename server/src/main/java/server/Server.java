package server;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
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
            var gameDAI = new SQLGameDAO();

            this.service = new Service(UserDAO, AuthDAO, GameDAO);
            this.userService = new UserService(dataAUserDAO, AuthDAO, GameDAOccess);
            this.gameService = new GameService(dataAccUserDAO, AuthDAO, GameDAOess);

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
        Spark.delete("/session", UserService::logout);
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
