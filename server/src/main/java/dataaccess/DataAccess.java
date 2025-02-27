package dataaccess;

import model.*;

import java.util.Collection;

public interface DataAccess {
    UserData registerUser(UserData u) throws DataAccessException;
    void clearDatabase() throws DataAccessException;
    void clearUserData() throws DataAccessException;
    void clearAuthData() throws DataAccessException;
    void clearGameData() throws DataAccessException;
    Collection<UserData> getUsers() throws  DataAccessException;
}
