package client;

import exception.ResponseException;
import model.AuthData;
import model.UserData;
import org.junit.jupiter.api.*;
import server.Server;
import server.ServerFacade;


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
    public void getGamesSuccess() {
        Assertions.assertDoesNotThrow(() -> {
            serverFacade.getGames(existingAuth);
        });
    }

    @Test
    public void getGamesFail() {
        Assertions.assertThrows(ResponseException.class, () -> {
            brokenFacade.getGames(null);
        });
    }

}
