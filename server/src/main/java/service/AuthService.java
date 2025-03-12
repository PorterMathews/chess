package service;

import dataaccess.*;

public class AuthService {
    private GameDAO gameDAO = new MemoryGameDAO();
    private AuthDAO authDAO = new MemoryAuthDAO();
    private UserDAO userDAO = new MemoryUserDAO();

    public void clearDatabase() throws DataAccessException {
        authDAO.clearAuthData();
        userDAO.clearUserData();
        gameDAO.clearGameData();
    }
}
