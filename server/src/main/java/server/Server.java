package server;

import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.*;
import service.Service;
import service.UserService;
import spark.*;
import com.google.gson.Gson;

import java.util.Map;

public class Server {
    private final Service service;
    private final UserService userService;

    public Server() {
        this.service = new Service(new MemoryDataAccess());
        this.userService = new UserService(new MemoryDataAccess());
    }

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);
        Spark.delete("/db", this::clearDatabase);
        Spark.post("/session", this::login);
        Spark.delete("/session", this::logout);

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

}
