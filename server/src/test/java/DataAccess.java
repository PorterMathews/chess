import dataaccess.*;
import org.junit.jupiter.api.*;
import model.*;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.GameService;
import service.UserService;

public class DataAccess {
    private final UserService userService = new UserService(new SQLAuthDAO(), new SQLUserDAO());
    private final GameService gameService = new GameService(new SQLAuthDAO(), new SQLGameDAO());
    private final AuthService authService = new AuthService(new SQLAuthDAO(), new SQLUserDAO(), new SQLGameDAO());
    private final GameDAO gameDAO = new SQLGameDAO();
    private final AuthDAO authDAO = new SQLAuthDAO();
    private final UserDAO userDAO = new SQLUserDAO();

    private static UserData existingUser;

    private static UserData newUser;

    private String existingAuth;

    @BeforeAll
    public static void init() {
        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("newUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() throws DataAccessException {
        authService.clearDatabase();

        //one user already logged in
        AuthData regResult = userService.register(existingUser);
        existingAuth = regResult.authToken();
    }

    @Test
    public void testClear() throws DataAccessException {
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
    public void testRegSuccess() {
        assertNotNull(existingAuth);
        assertEquals("ExistingUser", existingUser.username());
    }

    @Test
    public void testRegNullPassword() throws DataAccessException {
        UserData user = new UserData("User", null, "e@email.com");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.register(user);
        });

        assertEquals("Bad request", exception.getMessage());
    }

    @Test
    public void testLoginGood() throws DataAccessException {
        AuthData authData = userService.login(existingUser);
        assertEquals(authData.username(), existingUser.username());
        assertNotNull(authData.authToken());
    }

    @Test
    public void testLoginNotReg() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.login(newUser);
        });
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    public void testLogoutGood() throws DataAccessException {
        userService.logout(existingAuth);
        assertNull(authDAO.getAuthDataByAuthToken(existingAuth));
    }

    @Test
    public void testLogoutBad() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.logout("null");
        });
        assertEquals("Unauthorized Token", exception.getMessage());
    }

    @Test
    public void testCreateGameGood() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        gameService.createGame(existingAuth, "testGame");
        assertFalse(gameDAO.getGames().isEmpty());
    }

    @Test
    public void testCreateGameBadAuth() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame("null", "awesomeGame");
        });
        assertEquals("Unauthorized to Create Game", exception.getMessage());
    }

    @Test
    public void testCreateGameBadName() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(existingAuth, null);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGameGood() throws DataAccessException{
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        gameService.joinGame(existingAuth, "BLACK", gameID);
        GameData gameData = gameDAO.getGameByID(gameID);
        assertNotNull(gameData.blackUsername());
        assertEquals("ExistingUser", gameData.blackUsername());
    }

    @Test
    public void testJoinGameWrongGameID() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "BLACK", 1234);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGameYellowColor() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "YELLOW", gameID);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testGetGamesGood() throws DataAccessException {
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
    public void testGameGetBadAuth() throws DataAccessException {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.getGames("null");
        });
        assertEquals("Unauthorized to Get Game", exception.getMessage());

    }



}
