package dataaccess;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import org.junit.jupiter.api.*;
import model.*;
import java.util.Collection;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import service.AuthService;
import service.GameService;
import service.UserService;

public class DataAccessTest {
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
    public void testClearUserDataGood() throws DataAccessException {
        assertNotNull(existingAuth);
        assertEquals("ExistingUser", existingUser.username());
        userService.clearUserData();
        assertTrue(userDAO.getUsers().isEmpty());
    }

    @Test
    public void testClearAuthDataGood() throws DataAccessException {
        assertNotNull(authDAO.getAuthDataByAuthToken(existingAuth));
        authService.clearAuthData();
        assertNull(authDAO.getAuthDataByAuthToken(existingAuth));
    }

    @Test
    public void testClearGameDataVeryGood() throws DataAccessException {
        int gameID = gameService.createGame(existingAuth, "NewGame");
        assertNotEquals(0, gameID);
        gameService.clearGameData();
        assertTrue(gameDAO.getGames().isEmpty());
    }

    @Test
    public void testGetUser() throws DataAccessException {
        assertFalse(userDAO.getUsers().isEmpty());
    }

    @Test
    public void testGetUserClear() throws DataAccessException {
        userService.clearUserData();
        assertTrue(userDAO.getUsers().isEmpty());
    }

    @Test
    public void testIsUserInDBGood() throws DataAccessException {
        assertTrue(userDAO.isUserInDB(existingUser.username()));
    }

    @Test
    public void testIsUserInDBBad() throws DataAccessException {
        assertFalse(userDAO.isUserInDB("newUser"));
    }

    @Test
    public void testRegisterGood() {
        assertNotNull(existingAuth);
        assertEquals("ExistingUser", existingUser.username());
    }

    @Test
    public void testRegisterNullPasswordBad() throws DataAccessException {
        UserData user = new UserData("NewUser", null, "e@email.com");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            userService.register(user);
        });

        assertEquals("Bad request", exception.getMessage());
    }

    @Test
    public void authTokenGood() throws DataAccessException {
        assertTrue(authDAO.authTokenExists(existingAuth));
    }

    @Test
    public void authTokenBad() throws DataAccessException {
        assertFalse(authDAO.authTokenExists("null"));
    }

    @Test
    public void generateAuthTokenGood() throws DataAccessException {
        String string = authDAO.generateAuthToken("newUser");
        assertNotNull(string);
    }

    @Test
    public void testLoginGood() throws DataAccessException {
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
    public void testCreateGameNoNameBad() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.createGame(existingAuth, null);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGameGood() throws DataAccessException{
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        gameService.joinGame(existingAuth, "Black", gameID, false);
        GameData gameData = gameDAO.getGameByID(gameID);
        assertNotNull(gameData.blackUsername());
        assertEquals("ExistingUser", gameData.blackUsername());
    }

    @Test
    public void testJoinGameNoAuth() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame("null", "black", gameID, false);
        });
        assertEquals("Unauthorized", exception.getMessage());
    }

    @Test
    public void testJoinGameBadGameIDGood() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "BlAcK", 1234, false);
        });
        assertEquals("bad request", exception.getMessage());
    }

    @Test
    public void testJoinGameGREEN() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
        int gameID = gameService.createGame(existingAuth, "testGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.joinGame(existingAuth, "GREEN", gameID, false);
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
    }

    @Test
    public void testGameGetNoAuth() throws DataAccessException {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.getGames("null");
        });
        assertEquals("Unauthorized to Get Game", exception.getMessage());

    }


    @Test
    public void getGameByIDFail() throws DataAccessException {
        assertNull(gameDAO.getGameByID(0));
    }

    @Test
    public void testAddUserToGameGood() throws DataAccessException {
        int num = gameDAO.createGame("newGame");
        gameDAO.addUserToGame(existingUser.username(), num, "BLACK");
        assertFalse(gameDAO.getGames().isEmpty());
    }

    @Test
    public void testAddUserToGameBadGameIDGood() throws DataAccessException {
        int num = gameDAO.createGame("newGame");
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameDAO.addUserToGame(existingUser.username(), 1, "BLACK");
        });
        assertEquals("Game not found", exception.getMessage());
    }

    @Test
    public void testGetGames() throws DataAccessException {
        assertTrue(gameDAO.getGames().isEmpty());
    }

    @Test
    public void testUpdateGame() throws DataAccessException, InvalidMoveException {
        gameService.createGame(existingAuth, "testGame");
        ChessGame chessGame = new ChessGame();
        ChessPosition start = new ChessPosition(2, 5);
        ChessPosition end = new ChessPosition(3, 5);
        chessGame.makeMove(new ChessMove(start, end, null));
        assertNull(chessGame.getBoard().getPiece(new ChessPosition(2, 5)));
        Collection<GameData> gameDataCollection = gameService.getGames(existingAuth);
        GameData gameDataBefore = gameDataCollection.iterator().next();
        //System.out.println(chessGame.getBoard().getPiece(new ChessPosition(2,5)).getPieceType());
        //System.out.println("Game before ID: " + gameDataBefore.gameID());
        gameService.updateGame(existingAuth, gameDataBefore.gameID(), new GameData(gameDataBefore.gameID(), null, null, null, chessGame));
        Collection<GameData> gameDataC = gameService.getGames(existingAuth);
        GameData gameDataAfter = gameDataC.iterator().next();
        assertNotEquals(gameDataBefore.game().getBoard().getPiece(new ChessPosition(2,5)), gameDataAfter.game().getBoard().getPiece(new ChessPosition(2,5)));
    }

    @Test
    public void testUpdateGameBadAuth() {
        Exception exception = assertThrows(DataAccessException.class, () -> {
            gameService.updateGame("null", 1,  new GameData(1,null, null,null,null));
        });
        assertEquals("Unauthorized to update Game", exception.getMessage());
    }
}
