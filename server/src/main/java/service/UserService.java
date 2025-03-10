package service;

import dataaccess.*;
import model.*;

public class UserService {
    private final DataAccess dataAccess;

    public UserService(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    /**
     * @param u The user's data
     * @return The AuthData for the user after registration
     * @throws DataAccessException If they request is bad or the username is taken
     */
    public AuthData register(UserData u) throws DataAccessException {
        if (u.username() == null || u.password() == null || u.email() == null) {
            throw new DataAccessException("Bad request");
        }

        if (dataAccess.isUserInDB(u.username())) {
            throw new DataAccessException("Username already taken");
        }

        dataAccess.registerUser(u);
        String authToken = ((MemoryDataAccess) dataAccess).generateAuthToken(u.username());
        return new AuthData(authToken, u.username());
    }

    /**
     * Logs a user in
     * @param u username and password
     * @return AuthData from login
     * @throws DataAccessException if the password and username are wrong
     */
    public AuthData login(UserData u) throws DataAccessException {
        if (u.password() == null || u.username() == null){
            throw new DataAccessException("Unauthorized");
        }

        if (!dataAccess.checkPassword(u)) {
            throw new DataAccessException("Unauthorized");
        }

        String authToken = ((MemoryDataAccess) dataAccess).generateAuthToken(u.username());
        return new AuthData(authToken, u.username());
    }


    /**
     *
     * @param authToken the authToken to be removed
     * @throws DataAccessException If the authToken doesn't exist or is wrong
     */
    public void logout(String authToken) throws DataAccessException {
        if (!dataAccess.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized Token");
        }

        dataAccess.logout(authToken);
    }
}
