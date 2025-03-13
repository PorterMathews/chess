package service;

import dataaccess.*;
import model.*;

public class UserService {
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public UserService(AuthDAO authDAO, UserDAO userDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
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

        if (userDAO.isUserInDB(u.username())) {
            throw new DataAccessException("Username already taken");
        }

        userDAO.registerUser(u);
        String authToken = authDAO.generateAuthToken(u.username());
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

        if (!userDAO.checkPassword(u)) {
            throw new DataAccessException("Unauthorized");
        }

        String authToken = authDAO.generateAuthToken(u.username());
        return new AuthData(authToken, u.username());
    }


    public void logout(String authToken) throws DataAccessException {
        if (!authDAO.authTokenExists(authToken)) {
            throw new DataAccessException("Unauthorized Token");
        }
        authDAO.logout(authToken);
    }

    public void clearUserData() throws DataAccessException {
        userDAO.clearUserData();
    }
}
