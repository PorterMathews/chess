package service;

import chess.ChessGame;
import dataaccess.DataAccess;
import dataaccess.DataAccessException;
import dataaccess.MemoryDataAccess;
import org.junit.jupiter.api.*;
import model.*;
import passoff.model.*;
import dataaccess.MemoryDataAccess;
import server.Server;
import java.net.HttpURLConnection;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.Locale;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ServiceTests {
    private final UserService userService = new UserService(new MemoryDataAccess());
    private final GameService gameService = new GameService(new MemoryDataAccess());
    private final Service service = new Service(new MemoryDataAccess());
    private final DataAccess dataAccess = new MemoryDataAccess();

    private static UserData existingUser;

    private static UserData newUser;

    private String existingAuth;

    @BeforeAll
    public static void init() {
        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        service.clearDatabase();

        //one user already logged in
        AuthData regResult = userService.register(existingUser);
        existingAuth = regResult.authToken();
    }

    @Test
    public void testClearDatabase() throws DataAccessException {
        assertNotNull(existingAuth);
        assertEquals("ExistingUser", existingUser.username());

        int gameID = gameService.createGame(existingAuth, "NewGame");
        assertNotEquals(0, gameID);

        assertFalse(dataAccess.getUsers().isEmpty());
        assertFalse(dataAccess.getGames().isEmpty());
        assertNotNull(dataAccess.getAuthDataByAuthToken(existingAuth));

        service.clearDatabase();

        assertTrue(dataAccess.getUsers().isEmpty());
        assertTrue(dataAccess.getGames().isEmpty());
        assertNull(dataAccess.getAuthDataByAuthToken(existingAuth));

    }

    @Test
    public void testRegister_Success() {
        assertNotNull(existingAuth);
        assertEquals("ExistingUser", existingUser.username());
    }

    @Test
    public void testRegister_NullPassword() throws DataAccessException {
        UserData user = new UserData("User", null, "e@email.com");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.register(user);
        });

        assertEquals("Bad request", exception.getMessage());
    }

    @Test
    public void testLogin_Success() throws DataAccessException {
        AuthData authData = userService.login(existingUser);
        assertEquals(authData.username(), existingUser.username());
        assertNotNull(authData.authToken());
    }

    @Test
    public void testLogin_NotRegistered() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.login(newUser);
        });
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    public void testLogout_Success() throws DataAccessException {
        userService.logout(existingAuth);
        assertNull(dataAccess.getAuthDataByAuthToken(existingAuth));
    }

    @Test
    public void testLogout_Null() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.logout(null);
        });
        assertEquals("Unauthorized Token", exception.getMessage());
    }

    @Test
    public void testCreateGame_Success() throws DataAccessException {
        assertTrue(dataAccess.getGames().isEmpty());
        gameService.createGame(existingAuth, "testGame");
        assertFalse(dataAccess.getGames().isEmpty());
    }

    @Test
    public void testCreateGame_NoAuth() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(null, "awesomeGame");
        });
        assertEquals("Unauthorized to Create Game", exception.getMessage());
    }

    @Test
    public void testCreateGame_NoName() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(existingAuth, null);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGame_Success() throws DataAccessException{
        assertTrue(dataAccess.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        gameService.joinGame(existingAuth, "BLACK", gameID);
        GameData gameData = dataAccess.getGameByID(gameID);
        assertNotNull(gameData.blackUsername());
        assertEquals("ExistingUser", gameData.blackUsername());
    }

    @Test
    public void testJoinGame_NoAuth() throws DataAccessException {
        assertTrue(dataAccess.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(null, "BLACK", gameID);
        });
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    public void testJoinGame_BadGameID() throws DataAccessException {
        assertTrue(dataAccess.getGames().isEmpty());
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "BLACK", 1234);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGame_BadColor() throws DataAccessException {
        assertTrue(dataAccess.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "YELLOW", gameID);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testGetGames_Success() throws DataAccessException {
        assertTrue(dataAccess.getGames().isEmpty());
        gameService.createGame(existingAuth, "testGame");
        Collection<GameData> game1Data = gameService.getGames(existingAuth);
        assertEquals(1, game1Data.size());
        gameService.createGame(existingAuth, "testGame");
        Collection<GameData> game2Data = gameService.getGames(existingAuth);
        assertEquals(2, game2Data.size());
        //System.out.println("Games: " + game1Data);
    }

    @Test
    public void testGameGets_NoAuth() throws DataAccessException {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.getGames(null);
        });
        assertEquals("Unauthorized to Get Game", exception.getMessage());

    }



}
