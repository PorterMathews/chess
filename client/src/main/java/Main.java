import chess.*;
import client.Repl;


public class Main {
    public static void main(String[] args) {
        var piece = new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN);
        System.out.println("\n♕ 240 Chess Client: " + piece + "\n");
        var serverUrl = "http://localhost:8080";
        if (args.length == 1) {
            serverUrl = args[0];
        }
        System.out.print("   Welcome!\n");

        new Repl(serverUrl).replMain();
    }
}