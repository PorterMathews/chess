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
            throw new DataAccessException("Error: Unauthorized");
        }
        return dataAccess.createGame(gameName);
    }
}
