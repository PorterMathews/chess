package dataaccess;

import model.UserData;

import java.util.Collection;

public interface UserDAO {
    void registerUser(UserData u) throws DataAccessException;
    boolean isUserInDB(String userName) throws DataAccessException;
    boolean checkPassword(UserData u) throws DataAccessException;
    Collection<UserData> getUsers() throws DataAccessException;
    void clearUserData() throws DataAccessException;
}
