package service;

import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import model.AuthData;
import model.UserData;

public class Service {

    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData u) throws DataAccessException {
        UserData user = dataAccess.register(u);
        String authToken = ((MemoryDataAccess) dataAccess).generateAuthToken(user.username());
        return new AuthData(authToken, user.username());
    }

    public void clearDatabase() throws DataAccessException {
        dataAccess.clearDatabase();
    }
}
