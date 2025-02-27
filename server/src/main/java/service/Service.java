package service;

import dataaccess.*;
import model.AuthData;
import model.UserData;

import java.util.Collection;

public class Service {

    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public AuthData register(UserData u) throws DataAccessException {
        if (u.username() == null || u.password() == null || u.email() == null) {
            throw new DataAccessException("Error: Bad Request");
        }
        Collection<UserData> userData = dataAccess.getUsers();
        for (UserData data : userData) {
            if (data.username().equals(u.username())) {
                throw new DataAccessException("Error: Username already taken");
            }
        }
        UserData user = dataAccess.register(u);
        String authToken = ((MemoryDataAccess) dataAccess).generateAuthToken(user.username());
        return new AuthData(authToken, user.username());
    }

    public void clearDatabase() throws DataAccessException {
        dataAccess.clearDatabase();
    }
}
