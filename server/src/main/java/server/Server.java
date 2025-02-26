package server;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.UserData;
import service.Service;
import spark.*;
import com.google.gson.Gson;

public class Server {
    private final Service service;

    public Server() {
        this.service = new Service(new MemoryDataAccess()); // Default MemoryDataAccess
    }

    public Server(Service service) {
        this.service = service;
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.delete("/db", this::clearDatabase);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

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

    private Object register(Request req, Response res) throws DataAccessException {
        var userData = new Gson().fromJson(req.body(), UserData.class);
        var authData = service.register(userData);

        res.status(200);

        return new Gson().toJson(authData);
    }

    private Object clearDatabase(Request req, Response res) throws DataAccessException {
        service.clearDatabase();
        res.status(200);
        return "";
    }
}
