package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private static final HashMap<String, UserData> users = new HashMap<>();
    private static final HashMap<String, String> authTokens = new HashMap<>();
    private static final HashMap<Integer, GameData> gameInfo = new HashMap<>();
    private final Random random = new Random();

    public Collection<UserData> getUsers() {
        return users.values();
    }

    public AuthData getAuthDataByAuthToken(String authToken) {
        String username = authTokens.get(authToken);
        return (username != null) ? new AuthData(authToken, username) : null;
    }

    public boolean authTokenExists(String authToken) {
        System.out.println("Checking auth token: " + authToken);
        System.out.println("Current auth tokens: " + authTokens);
        return authTokens.containsKey(authToken);
    }

    public UserData registerUser(UserData userData) {
        userData = new UserData(userData.username(), userData.password(), userData.email());
        users.put(userData.username(), userData);
        return userData;
    }

    public String generateAuthToken(String username) {
        String token = UUID.randomUUID().toString();
        authTokens.put(token, username);
        return token;
    }

    public void logout(String authToken) {
        authTokens.remove(authToken);
    }

    public int createGame(String gameName){
        int gameID;
        do {
            gameID = 1000 + random.nextInt(9000);
        } while (gameInfo.containsKey(gameID));
        var gameData = new GameData(gameID, null, null, gameName, new ChessGame());
        gameInfo.put(gameID, gameData);
        return gameID;
    }

    public GameData getGameByID(int gameID) {
        return gameInfo.get(gameID);
    }

    public void addUserToGame(AuthData authData, int gameID, String playerColor) throws DataAccessException {
        GameData game = gameInfo.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        GameData gameData;
        String lowerCasePlayerColor = playerColor.toLowerCase();
        if (lowerCasePlayerColor.equals("black")) {
            gameData = new GameData(game.gameID(), game.whiteUsername(), authData.username(), game.gameName(), game.game());
        } else if (lowerCasePlayerColor.equals("white")) {
            gameData = new GameData(game.gameID(), authData.username(), game.blackUsername(), game.gameName(), game.game());
        } else {
            throw new DataAccessException("bad request");
        }
        gameInfo.put(gameData.gameID(), gameData);
    }

    public Collection<GameData> getGames() {
        return gameInfo.values();
    }

    public void clearUserData() {
        users.clear();
    }

    public void clearAuthData() {
        gameInfo.clear();
    }

    public void clearGameData() {
        authTokens.clear();
    }
}