package server;

import spark.*;
import server.websocket.WebSocketHandler;

public class Server {
    private static final boolean USE_SQL = true;
    private final Handler handler = new Handler(USE_SQL);
    private final WebSocketHandler webSocketHandler;

    public Server() {
        webSocketHandler = new WebSocketHandler();
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("/web");

        Spark.webSocket("/ws", webSocketHandler);
        Spark.delete("/db", handler::clearDatabase);
        Spark.post("/user", handler::register);
        Spark.post("/session", handler::login);
        Spark.delete("/session", handler::logout);
        Spark.post("/game", handler::createGame);
        Spark.put("/game", handler::joinGame);
        Spark.get("/game", handler::getGames);
        Spark.put("/game/:id/end", handler::updateWinner);
        Spark.get("/game/:id/status", handler::getWinner);
        Spark.put("/game/:id", handler::updateGame);


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