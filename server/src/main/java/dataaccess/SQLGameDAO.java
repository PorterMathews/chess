package dataaccess;

import chess.ChessGame;
import com.google.gson.Gson;
import model.AuthData;
import model.GameData;
import model.WinnerData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Random;

import static java.sql.Types.NULL;

public class SQLGameDAO implements GameDAO {
    private final Random random = new Random();

    /**
     * @throws DataAccessException
     */
    public void clearGameData() throws DataAccessException{
        String statement = "DELETE FROM GameData";
        updateData(statement);
    }

    /**
     *
     * @return all the games in the DB
     * @throws DataAccessException
     */
    public Collection<GameData> getGames() throws DataAccessException {
        return getAllGamesFromDatabase();
    }

    public void updateGame(int gameID, GameData gameData) throws DataAccessException {
        String statement = "UPDATE GameData SET chessGame = ? WHERE gameID = ?";
        updateData(statement, new Gson().toJson(gameData.game()), gameID);
    }

    public void updateWinner(int gameID, WinnerData winnerData) throws DataAccessException {
        String statement = "UPDATE GameData SET winnerData = ? WHERE gameID = ?";
        updateData(statement, new Gson().toJson(winnerData), gameID);
    }

    public WinnerData getWinner(int gameID) throws DataAccessException {
        return getWinnerDataFromDatabase(gameID);
    }

    /**
     *
     * @param userName username to be added
     * @param gameID the game you are updating the player of
     * @param playerColor the color you are updating the user of
     * @throws DataAccessException
     */
    public void addUserToGame(String userName, int gameID, String playerColor) throws DataAccessException {
        GameData gameData = getGameFromDatabaseByID(gameID);
        if (gameData == null) {
            throw new DataAccessException("Game not found");
        }
        String statement;
        if (playerColor.equals("black")) {
            statement = "UPDATE GameData SET blackUsername = ? WHERE gameID = ?";
        } else {
            statement = "UPDATE GameData SET whiteUsername = ? WHERE gameID = ?";
        }
        updateData(statement, userName, gameID);
    }

    /**
     *
     * @param gameID The game ID you are looking up
     * @return the gameData with ID
     * @throws DataAccessException
     */
    public GameData getGameByID(int gameID) throws DataAccessException {
        return getGameFromDatabaseByID(gameID);
    }

    /**
     *
     * @param gameName The name to be stored of the game
     * @return the game ID
     * @throws DataAccessException
     */
    public int createGame(String gameName) throws DataAccessException {
        int gameID;
        do {
            gameID = 1000 + random.nextInt(9000);
        } while (isGameIDInDatabase(gameID));
        String statement = "INSERT INTO GameData (gameID, whiteUsername, blackUsername, gameName, chessGame) VALUES (?, ?, ?, ?, ?)";
        var chessGame = new Gson().toJson(new ChessGame());
        updateData(statement, gameID, null, null, gameName, chessGame);
        return gameID;
    }

    /**
     *
     * @param rs the resultSet
     * @return a GameData object made from the rs
     * @throws SQLException
     */
    private GameData readGame(ResultSet rs) throws SQLException {
        var gameID = rs.getInt("gameID");
        var whiteUsername = rs.getString("whiteUsername");
        var blackUsername = rs.getString("blackUsername");
        var gameName = rs.getString("gameName");
        var chessGameJson = rs.getString("chessGame");
        var chessGame = new Gson().fromJson(chessGameJson, ChessGame.class);
        return new GameData(gameID, whiteUsername, blackUsername, gameName, chessGame);
    }

    /**
     *
     * @param rs DB response
     * @return The winnerData if it is there, else makes one
     * @throws SQLException
     */
    private WinnerData readWinner(ResultSet rs) throws SQLException {
        if (rs.next()) {
            var winnerDataJson = rs.getString("winnerData");
            if (winnerDataJson != null) {
                return new Gson().fromJson(winnerDataJson, WinnerData.class);
            }
        }
        return new WinnerData(false, null, null);
    }

    /**
     *
     * @return all the games from the DB
     * @throws DataAccessException
     */
    private Collection<GameData> getAllGamesFromDatabase() throws DataAccessException {
        var result = new ArrayList<GameData>();
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT gameID, whiteUsername, blackUsername, gameName, chessGame FROM GameData";
            try (var ps = conn.prepareStatement(statement)) {
                try (var rs = ps.executeQuery()) {
                    while (rs.next()) {
                        result.add(readGame(rs));
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
     * @param gameID the game ID you are looking up
     * @return the GameData associated with the ID
     * @throws DataAccessException
     */
    private GameData getGameFromDatabaseByID(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM GameData WHERE gameID = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return readGame(rs);
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return null;
    }

    /**
     * gets winner data from DB
     * @param gameID game with winner data
     * @return WinnerData
     * @throws DataAccessException
     */
    private WinnerData getWinnerDataFromDatabase(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT winnerData FROM GameData WHERE gameID = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    return readWinner(rs);
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
    }

    /**
     *
     * @param gameID The game ID you are checking with
     * @return true if the game is in the DB, else false
     * @throws DataAccessException
     */
    private boolean isGameIDInDatabase(int gameID) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            var statement = "SELECT * FROM GameData WHERE gameID = ?";
            try (var ps = conn.prepareStatement(statement)) {
                ps.setInt(1, gameID);
                try (var rs = ps.executeQuery()) {
                    if (rs.next()) {
                        return true;
                    }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to read data: %s", e.getMessage()));
        }
        return false;
    }

    /**
     *
     * @param statement the SQL statement
     * @param params the fields being changed
     * @throws DataAccessException
     */
    private void updateData(String statement, Object... params) throws DataAccessException {
        try (var conn = DatabaseManager.getConnection()) {
            try (var ps = conn.prepareStatement(statement)) {
                for (var i = 0; i < params.length; i++) {
                    var param = params[i];
                    if (param instanceof String p) {ps.setString(i + 1, p);}
                    else if (param instanceof Integer p) {ps.setInt(i + 1, p);}
                    else if (param instanceof GameData g) {ps.setString(i + 1, g.toString());}
                    else if (param instanceof AuthData a) {ps.setString(i + 1, a.toString());}
                    else if (param == null) {ps.setNull(i + 1, NULL);}
                }
                ps.executeUpdate();
            }
        } catch (SQLException e) {
            throw new DataAccessException(String.format("unable to update database: %s, %s", statement, e.getMessage()));
        }
    }
}
