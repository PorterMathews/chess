package service;

import dataaccess.*;
import model.*;

import java.util.Collection;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
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
        UserData user = dataAccess.registerUser(u);
        String authToken = ((MemoryDataAccess) dataAccess).generateAuthToken(user.username());
        return new AuthData(authToken, user.username());
    }

    public AuthData login(UserData u) throws DataAccessException {
        Collection<UserData> userData = dataAccess.getUsers();
        boolean loginMatch = false;
        for (UserData data : userData) {
            if (data.username().equals(u.username()) && data.password().equals(u.password())) {
                loginMatch = true;
                break;
            }
        }
        if (!loginMatch) {
            throw new DataAccessException("Error: Unauthorized");
        }
        String authToken = ((MemoryDataAccess) dataAccess).generateAuthToken(u.username());
        return new AuthData(authToken, u.username());
    }
}
