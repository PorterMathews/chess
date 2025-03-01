package service;

import dataaccess.*;
import model.*;

import java.util.Collection;
public class GameService {
    private final DataAccess dataAccess;

    public GameService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (!dataAccess.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized");
        }
        return dataAccess.createGame(gameName);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        if (!dataAccess.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized");
        }
        String gameIDString = String.valueOf(gameID);
        if (playerColor == null || gameIDString.length() != 4) {
            throw new DataAccessException("bad request");
        }
        GameData gameData = dataAccess.getGameByID(gameID);
        if (gameData == null) {
            throw new DataAccessException("bad request");
        }

        String lowerCasePlayerColor = playerColor.toLowerCase();
        if (gameData.whiteUsername() != null && gameData.blackUsername() != null) {
            throw new DataAccessException("Username already taken");
        } else if (lowerCasePlayerColor.equals("white") && gameData.whiteUsername() != null) {
            throw new DataAccessException("Username already taken");
        } else if (lowerCasePlayerColor.equals("black") && gameData.blackUsername() != null) {
            throw new DataAccessException("Username already taken");
        }

        AuthData authData = dataAccess.getAuthDataByAuthToken(authToken);
        dataAccess.addUserToGame(authData, gameID, lowerCasePlayerColor);
    }

    public Collection<GameData> getGames(String authToken) throws DataAccessException{
        if (!dataAccess.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized");
        }
        return dataAccess.getGames();
    }
}
