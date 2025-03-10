package dataaccess;

import com.google.gson.Gson;
import model.*;
import org.eclipse.jetty.server.Authentication;
import org.mindrot.jbcrypt.BCrypt;

import java.util.ArrayList;
import java.util.Collection;
import java.sql.*;
import java.util.UUID;

import static java.sql.Statement.RETURN_GENERATED_KEYS;
import static java.sql.Types.NULL;

public class MySqlDataAccess implements DataAccess {

    public MySqlDataAccess throws DataAccessException {
        configureDatabase();
    }

    public void clearUserData() throws DataAccessException{
        String statement = "DELETE FROM UserData";
        updateData(statement);
    }

    public void clearAuthData() throws DataAccessException{
        String statement = "DELETE FROM AuthData";
        updateData(statement);
    }

    public void clearGameData() throws DataAccessException{
        String statement = "DELETE FROM GameData";
        updateData(statement);
    }

    /**
     * @param userData the data for the user you are registering
     * @return The data for the user
     */
    public void registerUser(UserData userData) throws DataAccessException {
        String hashedPassword = BCrypt.hashpw(userData.password(), BCrypt.gensalt());
        String statement = "INSERT INTO UserData (username, password, email) VALUES (?, ?, ?)";
        updateData(statement, userData.username(), hashedPassword, userData.email());
    }

    public String generateAuthToken(String username) throws DataAccessException {
        String token = UUID.randomUUID().toString();
        String statement = "INSERT INTO AuthData (authToken, username) VALUES (?, ?)";
        updateData(statement, token, username);
        return token;
    }

    public Collection<UserData> getUsers() throws DataAccessException {
        return listUsers();
    }

    public boolean isUserInDB(String userName) throws DataAccessException {
        Collection<UserData> userData = listUsers();
        for (UserData user : userData) {
            if (user.username().equals(userName)) {
                return true;
            }
        }
        return false;
    }

    public boolean checkPassword(UserData u) throws DataAccessException {
        var hashedPassword = readHashedPasswordFromDatabase(u.username());
        return BCrypt.checkpw(u.password(), hashedPassword);
    }

    

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
            return null;
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    private UserData readUser(ResultSet rs) throws SQLException {
        var username = rs.getString("username");
        var password = rs.getString("password");
        var email = rs.getString("email");
        return new UserData(username, password, email);
    }

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
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }

    private final String[] createStatements = {
            """
            CREATE TABLE IF NOT EXISTS  UserData (
              `username` varchar(256) NOT NULL,
              `password` varchar(256) NOT NULL,
              `email` varchar(256) NOT NULL,
              PRIMARY KEY (`username`),
              INDEX(`email`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            
            CREATE TABLE IF NOT EXISTS  AuthData (
              `username` varchar(256) NOT NULL,
              `authToken` varchar(256) NOT NULL,
              PRIMARY KEY (`authToken`),
              INDEX(`username`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            
            CREATE TABLE IF NOT EXISTS  GameData (
              `gameID` int NOT NULL,
              `whiteUsername` varchar(256) DEFAULT NULL,
              `blackUsername` varchar(256) DEFAULT NULL,
              `gameName` varchar(256) NOT NULL,
              `chessGame` TEXT DEFAULT NULL,
              PRIMARY KEY (`gameID`),
              INDEX(`whiteUsername`),
              INDEX(`blackUsername`),
              INDEX(`gameName`)
            ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;
            """
    };

    private void configureDatabase() throws DataAccessException {
        DatabaseManager.createDatabase();
        try (var conn = DatabaseManager.getConnection()) {
            for (var statement : createStatements) {
                try (var preparedStatement = conn.prepareStatement(statement)) {
                    preparedStatement.executeUpdate();
                }
            }
        } catch (SQLException ex) {

            throw new DataAccessException(String.format("Unable to configure database: %s", ex.getMessage()));
        }
    }
}
