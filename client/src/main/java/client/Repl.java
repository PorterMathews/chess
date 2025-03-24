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


    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
        result = "";
        drawChessBoard = new DrawChessBoard(chessGame);
    }

    public void replMain() {
        System.out.print(SET_TEXT_ITALIC + SET_TEXT_COLOR_RED + "      Welcome!\n\n" + RESET_TEXT_ITALIC);

        System.out.print(ERASE_SCREEN);
        System.out.println(DrawChessBoard.drawBoard());

        while (!result.equals("quit")) {
            switch (ChessClient.getState()) {
                case LOGGEDIN -> postLogin();
                case INGAME -> gameplay();
                default -> preLogin();
            }
        }
    }

    public void preLogin() {
        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_COLOR_GREEN);
        System.out.print("Please, sign in or register");

        printPromptLogout();
        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.print(SET_TEXT_COLOR_GREEN + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    public void postLogin() {
        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_COLOR_MAGENTA);
        System.out.print("Please, join a game");

        printPromptLogin();
        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.print(SET_TEXT_COLOR_MAGENTA + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    public void gameplay() {
        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_COLOR_BLUE);
        System.out.print("Printing board");
        //System.out.print(DrawChessBoard.drawChessboard());

        printPromptInGame();
        String line = scanner.nextLine();

        try {
            result = client.eval(line);
            System.out.print(SET_TEXT_COLOR_BLUE + result);
        } catch (Throwable e) {
            var msg = e.toString();
            System.out.print(msg);
        }
        System.out.println();
    }

    private void printPromptLogout() {
        System.out.print("\n" + SET_TEXT_COLOR_LIGHT_GREY + "[Logged out] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPromptLogin() {
        System.out.print("\n" + SET_TEXT_COLOR_LIGHT_GREY + "[Logged in] >>> " + SET_TEXT_COLOR_MAGENTA);
    }

    private void printPromptInGame() {
        System.out.print("\n" + SET_TEXT_COLOR_LIGHT_GREY + "[In game] >>> " + SET_TEXT_COLOR_YELLOW);
    }
}
