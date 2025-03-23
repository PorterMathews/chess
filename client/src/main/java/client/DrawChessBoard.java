package client;

import chess.ChessGame;
import chess.ChessPiece;

import static ui.EscapeSequences.*;

public class DrawChessBoard {
    static ChessGame chessGame;

    public DrawChessBoard(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    public static String drawChessboard() {
        return String.format(oddRow() +"\n" + evenRow());
    }

    private static String square() {
        return EMPTY;
    }

    private static String blackSquare() {
        return (SET_BG_COLOR_BLACK + square() + RESET_BG_COLOR);
    }

    private static String whiteSquare() {
        return (SET_BG_COLOR_WHITE + square() + RESET_BG_COLOR);
    }

    private static String blackSquarePiece(String piece) {
        return (SET_BG_COLOR_WHITE + piece + RESET_BG_COLOR);
    }

    private static String whiteSquarePiece(String piece) {
        piece = WHITE_KING;
        return (SET_BG_COLOR_WHITE + piece + RESET_BG_COLOR);
    }

    private static String oddRow() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            result.append(blackSquare()).append(whiteSquarePiece("w"));
        }
        return result.toString();
    }

    private static String evenRow() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            result.append(whiteSquare()).append(blackSquare());
        }
        return result.toString();
    }
}
