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


    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
        result = "";
        drawChessBoard = new DrawChessBoard(chessGame);
    }

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

    public void preLogin() {
        System.out.print(preLoginColor + "Please, sign in or register");
        printPromptLogout();

        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.print(preLoginColor + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    public void postLogin() {
        System.out.print(postLoginColor + "Please, join or create a game");
        printPromptLogin();

        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.print(postLoginColor + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    public void gameplay() {
        System.out.print(inGameColor + "Printing board");
        System.out.println(DrawChessBoard.drawBoard(ChessClient.getPlayerColor()));
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
        System.out.print("\n" + gameColor + "[Logged in] >>> ");
    }

    private void printPromptInGame() {
        System.out.print("\n" + gameColor + "[In game] >>> ");
    }
}
