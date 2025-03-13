package dataaccess;

import model.AuthData;
import model.UserData;

import java.sql.SQLException;

import org.mindrot.jbcrypt.BCrypt;

import java.util.*;
import java.sql.*;

import static java.sql.Types.NULL;

public class SQLUserDAO implements UserDAO {

    /**
     * Clears users from UserData
     * @throws DataAccessException
     */
    public void clearUserData() throws DataAccessException{
        String statement = "DELETE FROM UserData";
        updateData(statement);
    }

    /**
     * @param userData the data for the user you are registering
     * @return The data for the user
     */
    public void registerUser(UserData userData) throws DataAccessException {
        System.out.println("Register SQL");
        String hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
        String statement = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";
        updateData(statement, userData.username(), hashedPassword, userData.email());
    }

    /**
     *
     * @return a list of users
     * @throws DataAccessException
     */
    public Collection<UserData> getUsers() throws DataAccessException {
        return listUsers();
    }

    /**
     *
     * @param userName The userName you are checking for
     * @return true if it is in the DB, else false
     * @throws DataAccessException
     */
    public boolean isUserInDB(String userName) throws DataAccessException {
        Collection<UserData> userData = listUsers();
        for (UserData user : userData) {
            if (user.username().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    /**
     *
     * @param u the userdata you are checking the password for
     * @return true if the passwords match
     * @throws DataAccessException
     */
    public boolean checkPassword(UserData u) throws DataAccessException {
        //System.out.println("Checking password SQL");
        var hashedPassword = readHashedPasswordFromDatabase(u.username());
        //System.out.println("Checking passwords: \nUser: " + u.password() + "\nHashed: " + hashedPassword);
        if (hashedPassword == null) {
            return false;
        }
        return BCrypt.checkpw(u.password(), hashedPassword);
    }

    /**
     *
     * @param username would get the password from
     * @return the hashed password
     * @throws DataAccessException
     */
    private String readHashedPasswordFromDatabase(String username) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT password FROM UserData WHERE username = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setString(1, username);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return rs.getString("password");
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
     * @param rs the resultSet
     * @return a userData object constructed from the rs
     * @throws SQLException
     */
    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var email = rs.getString("email");
        return new UserData(username, null, email);
    }

    /**
     *
     * @return a list of users
     * @throws DataAccessException
     */
    private Collection<UserData> listUsers() throws DataAccessException {
        var result = new ArrayList<UserData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT username, password, email FROM UserData";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readUser(rs));
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return result;
    }

    /**
     *
     * @param statement the SQL statement
     * @param params the fields you are updating
     * @throws DataAccessException
     */
    private void updateData(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {ps.setString(i + 1, p);}
                    else if (param instanceof Integer p) {ps.setInt(i + 1, p);}
                    else if (param instanceof UserData u) {ps.setString(i + 1, u.toString());}
                    else if (param instanceof AuthData a) {ps.setString(i + 1, a.toString());}
                    else if (param == null) {ps.setNull(i + 1, NULL);}
                }
                ps.executeUpdate();
                //conn.commit();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}
