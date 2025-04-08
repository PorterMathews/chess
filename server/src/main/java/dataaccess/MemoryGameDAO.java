package dataaccess;

import chess.ChessGame;
import model.GameData;
import model.WinnerData;

import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

public class MemoryGameDAO implements GameDAO {
    private static final HashMap<Integer, GameData> GAME_INFO = new HashMap<>();
    private final Random random = new Random();

    /**
     * Creates a game with a random, unique game ID. Puts the game in the HasMap, read to be used
     * @param gameName The name to be stored with the game
     * @return the gameID
     */
    public int createGame(String gameName){
        int gameID;
        do {
            gameID = 1000 + random.nextInt(9000);
        } while (GAME_INFO.containsKey(gameID));
        var gameData = new GameData(gameID, null, null, gameName, new ChessGame());
        GAME_INFO.put(gameID, gameData);
        return gameID;
    }

    /**
     * @param gameID the ID of the game to be looked up
     * @return the game with the ID
     */
    public GameData getGameByID(int gameID) {
        return GAME_INFO.get(gameID);
    }

    /**
     *
     * @param userName the User to be added
     * @param gameID the ID of the game they are to be added to
     * @param playerColor the color they are being added as
     * @throws DataAccessException if a game does not exist with the ID
     */
    public void addUserToGame(String userName, int gameID, String playerColor) throws DataAccessException {
        GameData game = GAME_INFO.get(gameID);
        if (game == null) {
            throw new DataAccessException("Game not found");
        }
        GameData gameData;
        if (playerColor.equals("black")) {
            gameData = new GameData(game.gameID(), game.whiteUsername(), userName, game.gameName(), game.game());
        } else {
            gameData = new GameData(game.gameID(), userName, game.blackUsername(), game.gameName(), game.game());
        }
        GAME_INFO.put(gameData.gameID(), gameData);
    }

    /**
     * not implemented for memory
     * @param gameID
     * @param gameData
     */
    public void updateGame(int gameID, GameData gameData) {

    }

    /**
     * not implemented for memory
     * @param gameID
     * @param winnerData
     */
    public void updateWinner(int gameID, WinnerData winnerData) {

    }

    /**
     * not implemented for memory
     * @param gameID
     * @return
     */
    public WinnerData getWinner(int gameID) {
        return new WinnerData(false, null, null);
    }

    /**
     * @return A collection of all games
     */
    public Collection<GameData> getGames() {
        return GAME_INFO.values();
    }

    /**
     * removes all data from the authTokens hashMap
     */
    public void clearGameData() {
        GAME_INFO.clear();
    }
}
