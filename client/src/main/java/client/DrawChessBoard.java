package client;

import chess.*;

import static ui.EscapeSequences.*;

public class DrawChessBoard {
    static ChessGame chessGame;
    static ChessBoard chessBoard;
    private static final String darkColor = SET_BG_COLOR_BLACK;
    private static final String lightColor = SET_BG_COLOR_WHITE;
    private static final String edgeColor = SET_BG_COLOR_LIGHT_GREY;

    public DrawChessBoard(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    public static String drawChessboard() {
        StringBuilder result = new StringBuilder();
        for (int row = 0; row < 10; row++) {
            for (int col = 0; col < 10; col++) {
                String piece = EMPTY;
                if (row == 0 || col == 0 || row == 9 || col == 9) {
                    result.append(edgeSquare(EMPTY));
                }
                else if ((row + col) % 2 == 1){
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
        return (darkColor + piece + RESET_BG_COLOR);
    }

    private static String lightSquare(String piece) {
        piece = EMPTY;
        return (lightColor + piece + RESET_BG_COLOR);
    }

    private static String edgeSquare(String piece) {
        piece = EMPTY;
        return (edgeColor + piece + RESET_BG_COLOR);
    }

    private static String determinePiece(int row, int col) {
        ChessPiece.PieceType type = chessBoard.getPiece(new ChessPosition(row,col)).getPieceType();
        if (type == null) {
            return EMPTY;
        }
        ChessGame.TeamColor color = chessBoard.getPiece(new ChessPosition(row,col)).getTeamColor();
        if (color.equals(ChessGame.TeamColor.WHITE)) {
            if (type.equals(ChessPiece.PieceType.KING)) {
                return WHITE_KING;
            } else if (type.equals(ChessPiece.PieceType.QUEEN)) {
                return WHITE_QUEEN;
            } else if (type.equals(ChessPiece.PieceType.BISHOP)) {
                return WHITE_BISHOP;
            } else if (type.equals(ChessPiece.PieceType.KNIGHT)) {
                return WHITE_KNIGHT;
            } else if (type.equals(ChessPiece.PieceType.ROOK)) {
                return WHITE_ROOK;
            } else if (type.equals(ChessPiece.PieceType.PAWN)) {
                return WHITE_PAWN;
            }
        } else if (color.equals(ChessGame.TeamColor.BLACK)) {
            if (type.equals(ChessPiece.PieceType.KING)) {
                return BLACK_KING;
            } else if (type.equals(ChessPiece.PieceType.QUEEN)) {
                return BLACK_QUEEN;
            } else if (type.equals(ChessPiece.PieceType.BISHOP)) {
                return BLACK_BISHOP;
            } else if (type.equals(ChessPiece.PieceType.KNIGHT)) {
                return BLACK_KNIGHT;
            } else if (type.equals(ChessPiece.PieceType.ROOK)) {
                return BLACK_ROOK;
            } else if (type.equals(ChessPiece.PieceType.PAWN)) {
                return BLACK_PAWN;
            }
        }
        throw new RuntimeException("Not a correct piece/color");
    }

    private static String evenRow() {
        StringBuilder result = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            result.append(lightSquare(EMPTY)).append(darkSquare(EMPTY));
        }
        result.append(chessBoard.getPiece(new ChessPosition(1,1)).getPieceType());
        return result.toString();
    }
}
