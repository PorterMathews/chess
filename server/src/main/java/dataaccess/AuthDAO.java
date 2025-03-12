package dataaccess;

import model.AuthData;

public interface AuthDAO {
    boolean authTokenExists(String authToken) throws DataAccessException;
    AuthData getAuthDataByAuthToken(String authToken) throws DataAccessException;
    String generateAuthToken(String username) throws DataAccessException;
    void logout(String authToken) throws  DataAccessException;
    void clearAuthData() throws DataAccessException;
}
