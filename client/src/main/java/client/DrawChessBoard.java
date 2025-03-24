package client;

import chess.*;
import java.util.*;

import static ui.EscapeSequences.*;

public class DrawChessBoard {
    static ChessGame chessGame;
    private static final String darkTileColor = SET_BG_COLOR_LIGHT_GREY;
    private static final String lightTileColor = SET_BG_COLOR_WHITE;
    private static final String edgeTileColor = SET_BG_COLOR_DARK_GREY;
    private static final String lightPieceColor = SET_TEXT_COLOR_BLUE;
    private static final String darkPieceColor = SET_TEXT_COLOR_RED;
    private static final String edgeCharacterColor = SET_TEXT_COLOR_WHITE;
    private static final String EMPTY = "   ";

    public DrawChessBoard(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    public static String drawBoard() {
        StringBuilder result = new StringBuilder();
        result.append(ERASE_SCREEN);
        for (int row = 0; row < 10; row++) {
            for (int col = 9; col >= 0; col--) {
                String piece;
                if (row == 0 || col == 0 || row == 9 || col == 9) {
                    piece = edgeCharacterDeterminer(row, col);
                    result.append(edgeSquare(piece));
                }
                else if ((row + col) % 2 == 0){
                    piece = determinePiece(row, col);
                    result.append(darkSquare(piece));
                }
                else {
                    piece = determinePiece(row, col);
                    result.append(lightSquare(piece));
                }
            }
            result.append("\n");
        }
        return result.toString();
    }

    private static String darkSquare(String piece) {
        return (darkTileColor + piece + RESET_BG_COLOR);
    }

    private static String lightSquare(String piece) {
        return (lightTileColor + piece + RESET_BG_COLOR);
    }

    private static String edgeSquare(String piece) {
        return (edgeTileColor + piece + RESET_BG_COLOR);
    }

    private static String determinePiece(int row, int col) {
        ChessPiece chessPiece = ChessGame.getBoard().getPiece(new ChessPosition(row, col));
        StringBuilder result = new StringBuilder();
        if (chessPiece == null) {
            return EMPTY;
        }
        ChessPiece.PieceType type = chessPiece.getPieceType();
        ChessGame.TeamColor color = ChessGame.getBoard().getPiece(new ChessPosition(row,col)).getTeamColor();

        String piece = "";
        String textColor = "";

        if (color.equals(ChessGame.TeamColor.WHITE)) {
            textColor = lightPieceColor;
        } else if (color.equals(ChessGame.TeamColor.BLACK)) {
            textColor = darkPieceColor;
        }
        if (type.equals(ChessPiece.PieceType.KING)) {
            piece = "K";
        } else if (type.equals(ChessPiece.PieceType.QUEEN)) {
            piece = "Q";
        } else if (type.equals(ChessPiece.PieceType.BISHOP)) {
            piece = "B";
        } else if (type.equals(ChessPiece.PieceType.KNIGHT)) {
            piece = "B";
        } else if (type.equals(ChessPiece.PieceType.ROOK)) {
            piece = "R";
        } else if (type.equals(ChessPiece.PieceType.PAWN)) {
            piece = "P";
        }
        result.append(String.format(textColor + " " + piece + " " + RESET_TEXT_COLOR));
        return result.toString();
    }


    private static String edgeCharacterDeterminer(int row, int col) {
        int dif = row - col;
        if (dif == 0 || dif == 9 || dif == -9) {
            return "   ";
        }
        if (col == 0 || col == 9) {
            return String.format(edgeCharacterColor + " " + row + " ");
        }
        if (row == 0 || row == 9) {
            return String.format(edgeCharacterColor + " " + ((char)('a' + col -1)) + " ");
        }
        throw new RuntimeException("determining edge character for not edge");
    }
}
