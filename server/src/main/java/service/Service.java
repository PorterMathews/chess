package service;

import dataaccess.*;
import model.*;

import java.util.Collection;

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
