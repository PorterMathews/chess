package service;

import dataaccess.*;

public class Service {
    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public void clearDatabase() throws DataAccessException {
        dataAccess.clearAuthData();
        dataAccess.clearUserData();
        dataAccess.clearGameData();
    }
}
