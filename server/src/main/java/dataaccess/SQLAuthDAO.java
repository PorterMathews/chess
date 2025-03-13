package dataaccess;

import model.*;
import java.util.*;
import java.sql.*;

import static java.sql.Types.NULL;

public class SQLAuthDAO implements AuthDAO {

    /**
     * @throws DataAccessException unable to delete data
     */
    public void clearAuthData() throws DataAccessException{
        String statement = "DELETE FROM AuthData";
        updateData(statement);
    }

    /**
     * @param authToken authToken tied to username
     * @return AuthData for the authToken
     * @throws DataAccessException
     */
    public AuthData getAuthDataByAuthToken(String authToken) throws DataAccessException {
        return getAuthDataFromDatabase(authToken);
    }

    /**
     *
     * @param username Username to be tied to the authToken
     * @return The authToken
     * @throws DataAccessException
     */
    public String generateAuthToken(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        String statement = "INSERT INTO AuthData (authToken, username) VALUES (?, ?)";
        updateData(statement, token, username);
        return token;
    }

    /**
     *
     * @param authToken authToken to check
     * @return ture if authToken is in DB, else false
     * @throws DataAccessException
     */
    public boolean authTokenExists(String authToken) throws DataAccessException {
        return Objects.equals(authToken, getAuthToken(authToken));
    }

    /**
     *
     * @param authToken the authToken to be removed from DB
     * @throws DataAccessException
     */
    public void logout(String authToken) throws DataAccessException {
        String statement = "DELETE FROM AuthData WHERE authToken = ?";
        updateData(statement, authToken);
    }

    /**
     *
     * @param authToken auth token
     * @return the same auth token?
     * @throws DataAccessException
     */
    private String getAuthToken(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT authToken FROM AuthData WHERE authToken = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("authToken");
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    /**
     *
     * @param authToken the authToken to be looked up
     * @return The authData for that token
     * @throws DataAccessException
     */
    private AuthData getAuthDataFromDatabase(String authToken) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username FROM AuthData WHERE authToken = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, authToken);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return new AuthData(authToken, (rs.getString("username")));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    /**
     *
     * @param statement the SQL statement
     * @param params any fields to update
     * @throws DataAccessException
     */
    private void updateData(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) ps.setString(i + 1, p);
                    else if (param instanceof Integer p) ps.setInt(i + 1, p);
                    else if (param instanceof UserData u) ps.setString(i + 1, u.toString());
                    else if (param instanceof GameData g) ps.setString(i + 1, g.toString());
                    else if (param instanceof AuthData a) ps.setString(i + 1, a.toString());
                    else if (param == null) ps.setNull(i + 1, NULL);
                }
                ps.executeUpdate();
                conn.commit();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}
