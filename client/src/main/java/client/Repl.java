package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;
import chess.*;
import client.websocket.NotificationHandler;
import websocket.messages.Notification;

public class Repl implements NotificationHandler  {
    private final ChessClient preClient;
    private final LoggedInClient postClient;
    private final GameClient gameClient;
    Scanner scanner = new Scanner(System.in);
    private String result;
    private static final String PRE_LOGIN_COLOR = SET_TEXT_COLOR_GREEN;
    private static final String POST_LOGIN_COLOR = SET_TEXT_COLOR_MAGENTA;
    private static final String IN_GAME_COLOR = SET_TEXT_COLOR_BLUE;
    private static final String GAME_COLOR = SET_TEXT_COLOR_LIGHT_GREY;
    private static State state = State.LOGGEDOUT;
    private static boolean prompt = true;


    public Repl(String serverUrl){
        postClient = new LoggedInClient(serverUrl, this);
        gameClient = new GameClient(serverUrl, postClient, this);
        preClient = new ChessClient(serverUrl, postClient);
        result = "";
    }

    /**
     * The main repl function, SM to switch between layers
     */
    public void replMain() {
        System.out.println(SET_TEXT_ITALIC + SET_TEXT_COLOR_RED + "      Welcome!\n" + RESET_TEXT_ITALIC);
        //System.out.println(DrawChessBoard.drawBoard("black"));

        while (!result.equals("quit")) {
            switch (state) {
                case LOGGEDOUT -> preLogin();
                case LOGGEDIN -> postLogin();
                default -> gameplay();
            }
        }
    }

    /**
     * Pre login script
     */
    public void preLogin() {
        if (prompt) {
            System.out.println(PRE_LOGIN_COLOR + preClient.help());
            prompt = false;
        }
        System.out.println(PRE_LOGIN_COLOR + "Please, sign in or register");
        printPromptLogout();

        String line = scanner.nextLine();

        try {
            result = preClient.eval(line);
            System.out.println(PRE_LOGIN_COLOR + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    /**
     * post login script
     */
    public void postLogin() {
        if (prompt) {
            System.out.println(POST_LOGIN_COLOR + postClient.help());
            prompt = false;
        }
        System.out.println(POST_LOGIN_COLOR + "Please, join or create a game");
        printPromptLogin();

        String line = scanner.nextLine();

        try {
            result = postClient.eval(line);
            System.out.println(POST_LOGIN_COLOR + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    /**
     * gameplay script
     */
    public void gameplay() {
        if (prompt) {
            gameClient.setChessBoard();
            System.out.println(IN_GAME_COLOR + gameClient.help());
            System.out.println(DrawChessBoard.drawBoard(LoggedInClient.getPlayerColor(), gameClient.getChessBoard()));
            prompt = false;
        }
        printPromptInGame();
        String line = scanner.nextLine();

        try {
            result = gameClient.eval(line);
            System.out.print(IN_GAME_COLOR + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    private void printPromptLogout() {
        System.out.print("\n" + GAME_COLOR + "[Logged out] >>> ");
    }

    private void printPromptLogin() {
        System.out.print("\n" + GAME_COLOR + "[Logged in as "+LoggedInClient.getUsername()+"] >>> ");
    }

    private void printPromptInGame() {
        System.out.print("\n" + GAME_COLOR + "[In game] >>> ");
    }

    public void notify(Notification notification) {
        System.out.println(GAME_COLOR + notification.message());
        printPromptLogout();
    }

    public static void setState(State s) {
        state = s;
    }

    public static State getState() {
        return state;
    }

    public static void setPrompt() {
        prompt = true;
    }
}
