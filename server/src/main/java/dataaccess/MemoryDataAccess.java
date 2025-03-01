package dataaccess;

import chess.ChessGame;
import model.AuthData;
import model.GameData;
import model.UserData;
import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private static final HashMap<String, UserData> USERS = new HashMap<>();
    private static final HashMap<String, String> AUTH_TOKENS = new HashMap<>();
    private static final HashMap<Integer, GameData> GAME_INFO = new HashMap<>();
    private final Random random = new Random();

    /**
     * @return All users and their data
     */
    public Collection<UserData> getUsers() {
        return USERS.values();
    }

    /**
     * @param userName the name of the user you are looking up
     * @return true if the userName has been stored, else false
     */
    public boolean isUserInDB(String userName) {
        return USERS.containsKey(userName);
    }

    /**
     * @param u Data for user
     * @return the password for the user
     * @throws DataAccessException if the user doesn't exist
     */
    public String getPassword(UserData u) throws DataAccessException {
        UserData user = USERS.get(u.username());
        if (user == null) {
            throw new DataAccessException("Unauthorized");
        }
        return user.password();
    }

    /**
     * @param authToken AuthToken
     * @return AuthData for user with authToken
     */
    public AuthData getAuthDataByAuthToken(String authToken) {
        String username = AUTH_TOKENS.get(authToken);
        return (username != null) ? new AuthData(authToken, username) : null;
    }

    /**
     * @param authToken AuthToke you are checking for
     * @return true if the authToken exists
     */
    public boolean authTokenExists(String authToken) {
        //System.out.println("Checking auth token: " + authToken);
        //System.out.println("Current auth tokens: " + authTokens);
        return AUTH_TOKENS.containsKey(authToken);
    }

    /**
     * @param userData the data for the user you are registering
     * @return The data for the user
     */
    public void registerUser(UserData userData) {
        userData = new UserData(userData.username(), userData.password(), userData.email());
        USERS.put(userData.username(), userData);
    }

    /**
     * Generates authToken and stores it for later look up
     * @param username the username to be associate with the authToken
     * @return The taken that was generated
     */
    public String generateAuthToken(String username) {
        String token = UUID.randomUUID().toString();
        AUTH_TOKENS.put(token, username);
        return token;
    }

    /**
     * Removes authToken for hashMap storage
     * @param authToken The auth token to be removed
     */
    public void logout(String authToken) {
        AUTH_TOKENS.remove(authToken);
    }

    /**
     * Creates a game with a random, unique game ID. Puts the game in the HasMap, read to be used
     * @param gameName The name to be stored with the game
     * @return the gameID
     */
    public int createGame(String gameName){
        int gameID;
        do {
            gameID = 1000 + random.nextInt(9000);
        } while (GAME_INFO.containsKey(gameID));
        var gameData = new GameData(gameID, null, null, gameName, new ChessGame());
        GAME_INFO.put(gameID, gameData);
        return gameID;
    }

    /**
     * @param gameID the ID of the game to be looked up
     * @return the game with the ID
     */
    public GameData getGameByID(int gameID) {
        return GAME_INFO.get(gameID);
    }

    /**
     *
     * @param userName the User to be added
     * @param gameID the ID of the game they are to be added to
     * @param playerColor the color they are being added as
     * @throws DataAccessException if a game does not exist with the ID
     */
    public void addUserToGame(String userName, int gameID, String playerColor) throws DataAccessException {
        GameData game = GAME_INFO.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        GameData gameData;
        if (playerColor.equals("black")) {
            gameData = new GameData(game.gameID(), game.whiteUsername(), userName, game.gameName(), game.game());
        } else {
            gameData = new GameData(game.gameID(), userName, game.blackUsername(), game.gameName(), game.game());
        }
        GAME_INFO.put(gameData.gameID(), gameData);
    }

    /**
     * @return A collection of all games
     */
    public Collection<GameData> getGames() {
        return GAME_INFO.values();
    }

    /**
     * removes all data from the Users hashMap
     */
    public void clearUserData() {
        USERS.clear();
    }

    /**
     * removes all data from the gameInfo hashMap
     */
    public void clearAuthData() {
        GAME_INFO.clear();
    }

    /**
     * removes all data from the authTokens hashMap
     */
    public void clearGameData() {
        AUTH_TOKENS.clear();
    }
}