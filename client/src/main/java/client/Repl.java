package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;
import chess.*;
import client.websocket.NotificationHandler;
import websocket.messages.NotificationMessage;

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
    private static final String WS_COLOR = SET_TEXT_COLOR_YELLOW;
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
            gameClient.refreshGameState();
            System.out.println(IN_GAME_COLOR + gameClient.help());
            //System.out.println(DrawChessBoard.drawBoard(LoggedInClient.getPlayerColor(), gameClient.getChessBoard(), null));
            prompt = false;
        }
        //printPromptInGame();
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

    /**
     * prints prompt
     */
    private void printPromptLogout() {
        System.out.print("\n" + GAME_COLOR + "[Logged out] >>> ");
    }

    /**
     * prints prompt
     */
    private void printPromptLogin() {
        System.out.print("\n" + GAME_COLOR + "[Logged in as "+LoggedInClient.getUsername()+"] >>> ");
    }

    /**
     * prints prompt
     */
    private void printPromptInGame() {
        System.out.print("\n" + GAME_COLOR + "[In game] >>> ");
    }

    /**
     * draws the board and prompts
     * @param game The game of chess
     */
    public void loadGame(ChessGame game) {
        System.out.print("\033[2K\r");
        System.out.println(DrawChessBoard.drawBoard(LoggedInClient.getPlayerColor(), game.getBoard(), null));
        switch (state) {
            case LOGGEDOUT -> printPromptLogout();
            case LOGGEDIN -> printPromptLogin();
            default -> printPromptInGame();
        }
    }

    /**
     * sets state
     * @param s
     */
    public static void setState(State s) {
        state = s;
    }

    /**
     * gets the state
     */
    public static State getState() {
        return state;
    }

    /**
     * sets the prompt so you print help on first go around
     */
    public static void setPrompt() {
        prompt = true;
    }
}
