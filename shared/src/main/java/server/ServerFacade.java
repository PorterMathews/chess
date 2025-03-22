package server;

import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.io.*;
import java.net.*;
import java.util.Collection;
import java.util.List;
import java.util.Map;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    public void clearDatabase() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path,null, null, null);
    }

    public AuthData register(UserData userData) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path,null, userData, AuthData.class);
    }

    public AuthData login(UserData userData) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path,null, userData, AuthData.class);
    }

    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path,authToken, null, null);
    }

    public int crateGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        CreateGameResponse result = makeRequest("POST", path, authToken, new CreateGameRequest(gameName), CreateGameResponse.class);
        return result.gameID;
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        var path = "/game";
        this.makeRequest("PUT", path, authToken, new JoinGameRequest(playerColor, gameID), null);
    }

    public List<GameData> getGames(String authToken) throws ResponseException {
        var path = "/game";
        GameListResponse result = makeRequest("GET", path, authToken, null, GameListResponse.class);
        return result.games;
    }

    private <T> T makeRequest(String method, String path,String authToken, Object requestBody, Class<T> responseClass) throws ResponseException  {
        try {
            URL url = (new URI(serverUrl + path)).toURL();
            HttpURLConnection http = (HttpURLConnection) url.openConnection();
            http.setRequestMethod(method);
            http.setDoOutput(true);
            http.setRequestProperty("Authorization", authToken);
            writeBody(requestBody, http);

            http.connect();
            throwIfNotSuccessful(http);
            return readBody(http, responseClass);
        } catch (ResponseException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    private void throwIfNotSuccessful(HttpURLConnection http) throws IOException, ResponseException {
        var status = http.getResponseCode();
        if (!isSuccessful(status)) {
            try (InputStream respErr = http.getErrorStream()) {
                if (respErr != null) {
                    throw ResponseException.fromJson(respErr);
                }
            }

            throw new ResponseException(status, "other failure: " + status);
        }
    }

    private static <T> T readBody(HttpURLConnection http, Class<T> responseClass) throws IOException {
        T response = null;
        if (http.getContentLength() < 0) {
            try (InputStream respBody = http.getInputStream()) {
                InputStreamReader reader = new InputStreamReader(respBody);
                if (responseClass != null) {
                    response = new Gson().fromJson(reader, responseClass);
                }
            }
        }
        return response;
    }

    private boolean isSuccessful(int status) {
        return status / 100 == 2;
    }
}

class CreateGameRequest {
    String gameName;
    public CreateGameRequest(String gameName) {
        this.gameName = gameName; }
}

class JoinGameRequest {
    String playerColor;
    int gameID;
    public JoinGameRequest(String playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }
}

class GameListResponse {
    public List<GameData> games;
}

class CreateGameResponse {
    public int gameID;
}
