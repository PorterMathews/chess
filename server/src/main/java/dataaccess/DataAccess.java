package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {
    UserData register(UserData u) throws DataAccessException;
    void clearDatabase() throws DataAccessException;
    Collection<UserData> getUsers() throws  DataAccessException;
}
