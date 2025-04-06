package dataaccess;

import model.GameData;

import java.util.Collection;

public interface GameDAO {
    int createGame(String gameName) throws DataAccessException;
    GameData getGameByID(int gameID) throws DataAccessException;
    void addUserToGame(String userName, int gameID, String playerColor) throws DataAccessException;
    Collection<GameData> getGames() throws DataAccessException;
    void clearGameData() throws DataAccessException;
    void updateGame(int gameID, GameData gameData) throws DataAccessException;
}
