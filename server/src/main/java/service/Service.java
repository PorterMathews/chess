package service;

import dataaccess.DataAccessException;

public class Service {

    private final DataAccess dataAccess;

    public Service(DataAccess dataAccess) {
        this.dataAccess = dataAccess;
    }

    public Register register(register reg) throws DataAccessException {
        return dataAccess.register(reg);
    }
}
