package client;

import java.util.Scanner;
import static ui.EscapeSequences.*;

public class Repl {
    private final ChessClient client;
    Scanner scanner = new Scanner(System.in);
    private String result;


    public Repl(String serverUrl) {
        client = new ChessClient(serverUrl);
        result = "";
    }

    public void replMain() {
        while (!result.equals("quit")) {
            switch (ChessClient.getState()) {
                case LOGGEDIN -> postLogin();
                case INGAME -> gameplay();
                default -> preLogin();
            }
        }
    }

    public void preLogin() {
        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_COLOR_BLUE);
        System.out.print("Please, sign in or register");

        printPromptLogout();
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

    public void postLogin() {
        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_COLOR_BLUE);
        System.out.print("Please, join a game");

        printPromptLogin();
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

    public void gameplay() {
        System.out.print(SET_TEXT_COLOR_WHITE + SET_TEXT_COLOR_BLUE);

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
        System.out.print("\n" + SET_TEXT_COLOR_WHITE + "[Logged out] >>> " + SET_TEXT_COLOR_GREEN);
    }

    private void printPromptLogin() {
        System.out.print("\n" + SET_TEXT_COLOR_WHITE + "[Logged in] >>> " + SET_TEXT_COLOR_MAGENTA);
    }

    private void printPromptInGame() {
        System.out.print("\n" + SET_TEXT_COLOR_WHITE + "[In game]>>> " + SET_TEXT_COLOR_YELLOW);
    }
}
