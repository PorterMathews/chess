package server;

import dataaccess.DataAccessException;
import spark.*;
import com.google.gson.Gson;

public class Server {

    public int run(int desiredPort) {
        Spark.port(desiredPort);

        Spark.staticFiles.location("web");

        // Register your endpoints and handle exceptions here.
        Spark.post("?", this::register);

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

    private Object register(Request req, Response res) throws DataAccessException {
        var register = new Gson().fromJson(req.body(), request.class);
        var result = service.register(reg);
        return new Gson().toJson(result);
    }
}
