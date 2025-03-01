package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {
    UserData registerUser(UserData u) throws DataAccessException;
    void clearUserData() throws DataAccessException;
    void clearAuthData() throws DataAccessException;
    void clearGameData() throws DataAccessException;
    Collection<UserData> getUsers() throws  DataAccessException;
    void logout(String authToken) throws  DataAccessException;;
    AuthData getAuthDataByAuthToken(String authToken) throws DataAccessException;
    boolean authTokenExists(String AuthToken) throws DataAccessException;
    int createGame(String gameName) throws DataAccessException;
    GameData getGameByID(int gameID) throws DataAccessException;
    void addUserToGame(AuthData authData, int gameID, String playerColor) throws DataAccessException;
    Collection<GameData> getGames() throws DataAccessException;
}
