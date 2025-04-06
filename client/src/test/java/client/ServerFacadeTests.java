package client;

import chess.ChessGame;
import chess.ChessMove;
import chess.ChessPosition;
import chess.InvalidMoveException;
import com.google.gson.Gson;
import dataaccess.DataAccessException;
import exception.ResponseException;
import model.AuthData;
import model.GameData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertEquals;


public class ServerFacadeTests {

    private static Server server;
    private static ServerFacade serverFacade;
    private static ServerFacade brokenFacade;

    private static UserData existingUser;

    private static UserData newUser;

    private String existingAuth;


    @BeforeAll
    public static void init() {
        server = new Server();
        var port = server.run(0);
        System.out.println("Started test HTTP server on " + port);
        var serverUrl = "http://localhost:" + server.port();
        serverFacade = new ServerFacade(serverUrl);
        var badServerUrl = "http://localhost:" + server.port() + "/wrong";
        brokenFacade = new ServerFacade(badServerUrl);

        existingUser = new UserData("ExistingUser", "existingUserPassword", "eu@mail.com");
        newUser = new UserData("NewUser", "newUserPassword", "nu@mail.com");
    }

    @BeforeEach
    public void setup() throws ResponseException {
        serverFacade.clearDatabase();

        AuthData regResult = serverFacade.register(existingUser);
        existingAuth = regResult.authToken();
    }

    @AfterAll
    static void stopServer() {
        server.stop();
    }

    @Test
    public void clearDatabaseSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.clearDatabase();
        });
    }

    @Test
    public void clearDatabaseFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.clearDatabase();
        });
    }

    @Test
    public void registerSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.register(newUser);
        });
    }

    @Test
    public void registerFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.register(newUser);
        });
    }

    @Test
    public void loginSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.login(existingUser);
        });
    }

    @Test
    public void loginFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.login(newUser);
        });
    }

    @Test
    public void logoutSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.logout(existingAuth);
        });
    }

    @Test
    public void logoutFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.logout(null);
        });
    }

    @Test
    public void createGameSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.crateGame(existingAuth,"existingAuth");
        });
    }

    @Test
    public void createGameFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.crateGame(null, "game");
        });
    }

    @Test
    public void joinGameSuccess() throws ResponseException {
        int gameID = serverFacade.crateGame(existingAuth,"existingAuth");
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.joinGame(existingAuth,"black", gameID);
        });
    }

    @Test
    public void joinGameFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.joinGame(null, "white", 1324);
        });
    }

    @Test
    public void listGamesSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.listGames(existingAuth);
        });
    }

    @Test
    public void listGamesFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.listGames(null);
        });
    }

    @Test
    public void testUpdateGame() throws ResponseException, InvalidMoveException {
        int gameID = serverFacade.crateGame(existingAuth, "testGame");
        ChessGame chessGame = new ChessGame();
        ChessPosition start = new ChessPosition(2, 5);
        ChessPosition end = new ChessPosition(3, 5);
        chessGame.makeMove(new ChessMove(start, end, null));
        assertNull(chessGame.getBoard().getPiece(new ChessPosition(2, 5)));
        Collection<GameData> gameDataCollection = serverFacade.listGames(existingAuth);
        GameData gameDataBefore = gameDataCollection.iterator().next();
        assertEquals(gameDataBefore.gameID(), gameID);

        serverFacade.updateGame(existingAuth, gameID, chessGame);

        Collection<GameData> gameDataC = serverFacade.listGames(existingAuth);
        GameData gameDataAfter = gameDataC.iterator().next();

        assertNotEquals(gameDataBefore.game().getBoard().getPiece(new ChessPosition(2,5)),
                gameDataAfter.game().getBoard().getPiece(new ChessPosition(2,5)));
    }


    @Test
    public void testUpdateGameBadAuth() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.updateGame("null", 1234, new GameData(1234,null, null,null,new ChessGame()).game());
        });
    }
}
