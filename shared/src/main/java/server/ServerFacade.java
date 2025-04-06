package server;

import chess.ChessGame;
import com.google.gson.Gson;
import exception.ResponseException;
import model.*;

import java.io.*;
import java.net.*;
import java.util.List;

public class ServerFacade {

    private final String serverUrl;

    public ServerFacade(String url) {
        serverUrl = url;
    }

    /**
     *
     * @throws ResponseException if call comes back bad
     */
    public void clearDatabase() throws ResponseException {
        var path = "/db";
        this.makeRequest("DELETE", path,null, null, null);
    }

    /**
     *
     * @param userData data to be registered
     * @return AuthData for user
     * @throws ResponseException if call comes back bad
     */
    public AuthData register(UserData userData) throws ResponseException {
        var path = "/user";
        return this.makeRequest("POST", path,null, userData, AuthData.class);
    }

    /**
     *
     * @param userData data from user attempting to login
     * @return AuthData for the user
     * @throws ResponseException if call comes back bad
     */
    public AuthData login(UserData userData) throws ResponseException {
        var path = "/session";
        return this.makeRequest("POST", path,null, userData, AuthData.class);
    }

    /**
     *
     * @param authToken use to make sure they are a user
     * @throws ResponseException if call comes back bad
     */
    public void logout(String authToken) throws ResponseException {
        var path = "/session";
        this.makeRequest("DELETE", path,authToken, null, null);
    }

    /**
     *
     * @param authToken used for verification
     * @param gameName name of the new game
     * @return gameID for new game
     * @throws ResponseException if call comes back bad
     */
    public int crateGame(String authToken, String gameName) throws ResponseException {
        var path = "/game";
        CreateGameResponse result = makeRequest("POST", path, authToken, new CreateGameRequest(gameName), CreateGameResponse.class);
        return result.gameID;
    }

    /**
     *
     * @param authToken used for verification
     * @param playerColor color player is trying to play as
     * @param gameID ID of game in DB
     * @throws ResponseException if call comes back bad
     */
    public void joinGame(String authToken, String playerColor, int gameID) throws ResponseException {
        var path = "/game";
        this.makeRequest("PUT", path, authToken, new JoinGameRequest(playerColor, gameID), null);
    }

    public void updateGame(String authToken, int gameID, ChessGame updatedGame) throws ResponseException {
        String path = "/game/" + gameID;
        GameData updatedGameData = new GameData(gameID, null, null, null, updatedGame);
        makeRequest("PUT", path, authToken, updatedGameData, null);
    }

    /**
     *
     * @param authToken used for verification
     * @return a list of games
     * @throws ResponseException if call comes back bad
     */
    public List<GameData> listGames(String authToken) throws ResponseException {
        var path = "/game";
        GameListResponse result = makeRequest("GET", path, authToken, null, GameListResponse.class);
        return result.games;
    }

    /**
     * heart of making requests, does all the formating
     * @param method API being called
     * @param path Path in API
     * @param authToken authToken to go in Header
     * @param requestBody request data
     * @param responseClass What we expect to get back as a response
     * @return the response
     * @param <T> ??
     * @throws ResponseException if call comes back bad
     */
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

    /**
     *
     * @param request the request body
     * @param http the http
     * @throws IOException if call comes back bad
     */
    private static void writeBody(Object request, HttpURLConnection http) throws IOException {
        if (request != null) {
            http.addRequestProperty("Content-Type", "application/json");
            String reqData = new Gson().toJson(request);
            try (OutputStream reqBody = http.getOutputStream()) {
                reqBody.write(reqData.getBytes());
            }
        }
    }

    /**
     *
     * @param http the http
     * @throws IOException if call comes back bad
     * @throws ResponseException if call comes back bad
     */
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

    /**
     *
     * @param http the http
     * @param responseClass the thing we got back
     * @return the stuff of the response body
     * @param <T> ??
     * @throws IOException if call comes back bad
     */
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

/**
 * Creates a game request
 */
class CreateGameRequest {
    String gameName;
    public CreateGameRequest(String gameName) {
        this.gameName = gameName; }
}

/**
 * Creates a join Request
 */
class JoinGameRequest {
    String playerColor;
    int gameID;
    public JoinGameRequest(String playerColor, int gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
    }
}

/**
 * Creates a list response
 */
class GameListResponse {
    public List<GameData> games;
}

/**
 * Creates a game response
 */
class CreateGameResponse {
    public int gameID;
}
