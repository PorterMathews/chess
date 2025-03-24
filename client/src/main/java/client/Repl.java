package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;
import chess.*;

public class Repl {
    private final ChessClient client;
    Scanner scanner = new Scanner(System.in);
    private String result;
    ChessGame chessGame = new ChessGame();
    DrawChessBoard drawChessBoard;
    private static final String preLoginColor = SET_TEXT_COLOR_GREEN;
    private static final String postLoginColor = SET_TEXT_COLOR_MAGENTA;
    private static final String inGameColor = SET_TEXT_COLOR_BLUE;
    private static final String gameColor = SET_TEXT_COLOR_LIGHT_GREY;
    private static State state;


    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
        result = "";
        drawChessBoard = new DrawChessBoard(chessGame);
    }

    /**
     * The main repl function, SM to switch between layers
     */
    public void replMain() {
        System.out.println(SET_TEXT_ITALIC + SET_TEXT_COLOR_RED + "      Welcome!\n" + RESET_TEXT_ITALIC);
        //System.out.println(DrawChessBoard.drawBoard("black"));

        while (!result.equals("quit")) {
            switch (ChessClient.getState()) {
                case LOGGEDIN -> postLogin();
                case INGAME -> gameplay();
                default -> preLogin();
            }
        }
    }

    /**
     * Pre login script
     */
    public void preLogin() {
        if (state != State.LOGGEDOUT) {
            System.out.println(preLoginColor + client.help());
            state = State.LOGGEDOUT;
        }
        System.out.println(preLoginColor + "Please, sign in or register");
        printPromptLogout();

        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.println(preLoginColor + result);
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
        if (state != State.LOGGEDIN) {
            System.out.println(postLoginColor + client.help());
            state = State.LOGGEDIN;
        }
        System.out.println(postLoginColor + "Please, join or create a game");
        printPromptLogin();

        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.println(postLoginColor + result);
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
        System.out.println(DrawChessBoard.drawBoard(ChessClient.getPlayerColor()));
        if (state != State.INGAME) {
            System.out.println(inGameColor + client.help());
            state = State.INGAME;
        }
        printPromptInGame();
        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.print(inGameColor + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    private void printPromptLogout() {
        System.out.print("\n" + gameColor + "[Logged out] >>> ");
    }

    private void printPromptLogin() {
        System.out.print("\n" + gameColor + "[Logged in as "+ChessClient.getUsername()+"] >>> ");
    }

    private void printPromptInGame() {
        System.out.print("\n" + gameColor + "[In game] >>> ");
    }
}
