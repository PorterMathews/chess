package service;

import dataaccess.*;

public class AuthService {
    private final GameDAO gameDAO;
    private final AuthDAO authDAO;
    private final UserDAO userDAO;

    public AuthService(AuthDAO authDAO, UserDAO userDAO, GameDAO gameDAO) {
        this.authDAO = authDAO;
        this.userDAO = userDAO;
        this.gameDAO = gameDAO;
    }

    public void clearDatabase() throws DataAccessException {
        authDAO.clearAuthData();
        userDAO.clearUserData();
        gameDAO.clearGameData();
    }
}
