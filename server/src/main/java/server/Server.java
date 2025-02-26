package server;

import dataaccess.DataAccessException;
import spark.*;
import com.google.gson.Gson;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("/user", this::register);

        //This line initializes the server and can be removed once you have a functioning endpoint 
        Spark.init();

        Spark.awaitInitialization();
        return Spark.port();
    }

    public void stop() {
        Spark.stop();
        Spark.awaitStop();
    }

    public int port() {
        return Spark.port();
    }

    private static void setupRoutes() {
        post("/user", (req, res) -> {
            // Parse JSON request to User object
            User user = new Gson().fromJson(req.body(), User.class);

            // Validate user data
            if (user.getUsername() == null || user.getPassword() == null || user.getEmail() == null) {
                res.status(400); // Bad Request
                return "Missing required fields";
            }

            // Check if username already exists
            if (users.containsKey(user.getUsername())) {
                res.status(403); // Forbidden
                return "Username already taken";
            }

            // Store user in the map
            users.put(user.getUsername(), user);

            // Set response status and return success message
            res.status(201); // Created
            return "User registered successfully";
        });
}
