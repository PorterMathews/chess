package client;

import chess.*;

import static ui.EscapeSequences.*;

public class DrawChessBoard {
    static ChessGame chessGame;
    private static final String darkTileColor = SET_BG_COLOR_DARK_GREEN;
    private static final String lightTileColor = SET_BG_COLOR_BLUE;
    private static final String edgeTileColor = SET_BG_COLOR_RED;
    private static final String lightPieceColor = SET_TEXT_COLOR_YELLOW;
    private static final String darkPieceColor = SET_TEXT_COLOR_RED;
    private static final String edgeCharacterColor = SET_TEXT_COLOR_GREEN;

    public DrawChessBoard(ChessGame chessGame) {
        this.chessGame = chessGame;
    }

    public static String drawBoard() {
        StringBuilder result = new StringBuilder();
        result.append(ERASE_SCREEN);
        for (int row = 0; row < 10; row++) {
            for (int col = 9; col >= 0; col--) {
                String piece = EMPTY;
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
        if (chessPiece == null) {
            return EMPTY;
        }
        ChessPiece.PieceType type = chessPiece.getPieceType();
        ChessGame.TeamColor color = ChessGame.getBoard().getPiece(new ChessPosition(row,col)).getTeamColor();
        if (color.equals(ChessGame.TeamColor.WHITE)) {

            if (type.equals(ChessPiece.PieceType.KING)) {
                return lightPieceColor + WHITE_KING;
            } else if (type.equals(ChessPiece.PieceType.QUEEN)) {
                return lightPieceColor + WHITE_QUEEN;
            } else if (type.equals(ChessPiece.PieceType.BISHOP)) {
                return lightPieceColor + WHITE_BISHOP;
            } else if (type.equals(ChessPiece.PieceType.KNIGHT)) {
                return lightPieceColor + WHITE_KNIGHT;
            } else if (type.equals(ChessPiece.PieceType.ROOK)) {
                return lightPieceColor + WHITE_ROOK;
            } else if (type.equals(ChessPiece.PieceType.PAWN)) {
                return lightPieceColor + WHITE_PAWN;
            }
        } else if (color.equals(ChessGame.TeamColor.BLACK)) {
            if (type.equals(ChessPiece.PieceType.KING)) {
                return darkPieceColor + BLACK_KING;
            } else if (type.equals(ChessPiece.PieceType.QUEEN)) {
                return darkPieceColor + BLACK_QUEEN;
            } else if (type.equals(ChessPiece.PieceType.BISHOP)) {
                return darkPieceColor + BLACK_BISHOP;
            } else if (type.equals(ChessPiece.PieceType.KNIGHT)) {
                return darkPieceColor + BLACK_KNIGHT;
            } else if (type.equals(ChessPiece.PieceType.ROOK)) {
                return darkPieceColor + BLACK_ROOK;
            } else if (type.equals(ChessPiece.PieceType.PAWN)) {
                return darkPieceColor + BLACK_PAWN;
            }
        }
        throw new RuntimeException("Not a correct piece/color");
    }

    private static String edgeCharacterDeterminer(int row, int col) {
        int dif = row - col;
        if (dif == 0 || dif == 9 || dif == -9) {
            return SET_TEXT_COLOR_GREEN + EMPTY;
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
