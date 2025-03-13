package service;

import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;

public class ServiceTests {
    private final UserService userService = new UserService(new MemoryAuthDAO(), new MemoryUserDAO());
    private final GameService gameService = new GameService(new MemoryAuthDAO(), new MemoryGameDAO());
    private final AuthService authService = new AuthService(new MemoryAuthDAO(), new MemoryUserDAO(), new MemoryGameDAO());
    private final GameDAO gameDAO = new MemoryGameDAO();
    private final AuthDAO authDAO = new MemoryAuthDAO();
    private final UserDAO userDAO = new MemoryUserDAO();

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
        authService.clearDatabase();

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

        assertFalse(userDAO.getUsers().isEmpty());
        assertFalse(gameDAO.getGames().isEmpty());
        assertNotNull(authDAO.getAuthDataByAuthToken(existingAuth));

        authService.clearDatabase();

        assertTrue(userDAO.getUsers().isEmpty());
        assertTrue(gameDAO.getGames().isEmpty());
        assertNull(authDAO.getAuthDataByAuthToken(existingAuth));

    }

    @Test
    public void testRegisterSuccess() {
        assertNotNull(existingAuth);
        assertEquals("ExistingUser", existingUser.username());
    }

    @Test
    public void testRegisterNullPassword() throws DataAccessException {
        UserData user = new UserData("User", null, "e@email.com");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.register(user);
        });

        assertEquals("Bad request", exception.getMessage());
    }

    @Test
    public void testLoginSuccess() throws DataAccessException {
        AuthData authData = userService.login(existingUser);
        assertEquals(authData.username(), existingUser.username());
        assertNotNull(authData.authToken());
    }

    @Test
    public void testLoginNotRegistered() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.login(newUser);
        });
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    public void testLogoutSuccess() throws DataAccessException {
        userService.logout(existingAuth);
        assertNull(authDAO.getAuthDataByAuthToken(existingAuth));
    }

    @Test
    public void testLogoutNull() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.logout(null);
        });
        assertEquals("Unauthorized Token", exception.getMessage());
    }

    @Test
    public void testCreateGameSuccess() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        gameService.createGame(existingAuth, "testGame");
        assertFalse(gameDAO.getGames().isEmpty());
    }

    @Test
    public void testCreateGameNoAuth() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(null, "awesomeGame");
        });
        assertEquals("Unauthorized to Create Game", exception.getMessage());
    }

    @Test
    public void testCreateGameNoName() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(existingAuth, null);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGameSuccess() throws DataAccessException{
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        gameService.joinGame(existingAuth, "BLACK", gameID);
        GameData gameData = gameDAO.getGameByID(gameID);
        assertNotNull(gameData.blackUsername());
        assertEquals("ExistingUser", gameData.blackUsername());
    }

    @Test
    public void testJoinGameNoAuth() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(null, "BLACK", gameID);
        });
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    public void testJoinGameBadGameID() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "BLACK", 1234);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGameBadColor() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "YELLOW", gameID);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testGetGamesSuccess() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        gameService.createGame(existingAuth, "testGame");
        Collection<GameData> game1Data = gameService.getGames(existingAuth);
        assertEquals(1, game1Data.size());
        gameService.createGame(existingAuth, "testGame");
        Collection<GameData> game2Data = gameService.getGames(existingAuth);
        assertEquals(2, game2Data.size());
        //System.out.println("Games: " + game1Data);
    }

    @Test
    public void testGameGetNoAuth() throws DataAccessException {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.getGames(null);
        });
        assertEquals("Unauthorized to Get Game", exception.getMessage());

    }



}
