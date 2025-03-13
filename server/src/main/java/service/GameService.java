package service;

import dataaccess.*;
import model.*;

import java.util.Collection;
public class GameService {
        private final GameDAO gameDAO;
        private final AuthDAO authDAO;

        public GameService(AuthDAO authDAO, GameDAO gameDAO) {
            this.authDAO = authDAO;
            this.gameDAO = gameDAO;
        }

    public int createGame(String authToken, String gameName) throws DataAccessException {
        if (!authDAO.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized to Create Game");
        }
        if (gameName == null) {
            throw new DataAccessException("bad request");
        }
        return gameDAO.createGame(gameName);
    }

    public void joinGame(String authToken, String playerColor, int gameID) throws DataAccessException {
        if (!authDAO.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized");
        }
        String gameIDString = String.valueOf(gameID);
        if (playerColor == null || gameIDString.length() != 4) {
            throw new DataAccessException("bad request");
        }
        GameData gameData = gameDAO.getGameByID(gameID);
        if (gameData == null) {
            throw new DataAccessException("bad request");
        }

        String lowerCasePlayerColor = playerColor.toLowerCase();
        if (!(lowerCasePlayerColor.equals("white") || lowerCasePlayerColor.equals("black"))) {
            throw new DataAccessException("bad request");
        }
        if (gameData.whiteUsername() != null && gameData.blackUsername() != null) {
            throw new DataAccessException("Players full for game");
        } else if (lowerCasePlayerColor.equals("white") && gameData.whiteUsername() != null) {
            throw new DataAccessException("Username already taken");
        } else if (lowerCasePlayerColor.equals("black") && gameData.blackUsername() != null) {
            throw new DataAccessException("Username already taken");
        }
        AuthData authData = authDAO.getAuthDataByAuthToken(authToken);
        gameDAO.addUserToGame(authData.username(), gameID, lowerCasePlayerColor);
    }

    public Collection<GameData> getGames(String authToken) throws DataAccessException{
        if (!authDAO.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized to Get Game");
        }
        return gameDAO.getGames();
    }

    public void clearGameData() throws DataAccessException {
        gameDAO.clearGameData();
    }


}
