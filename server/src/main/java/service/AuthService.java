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
        //System.out.println("Service clear 1");
        authDAO.clearAuthData();
        userDAO.clearUserData();
        //System.out.println("Service clear GameData");
        gameDAO.clearGameData();
    }

    public void clearAuthData() throws DataAccessException {
        //System.out.println("Service clear 2");
        authDAO.clearAuthData();
    }
}
