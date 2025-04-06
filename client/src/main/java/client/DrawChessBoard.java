package client;

import chess.*;
import exception.ResponseException;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static ui.EscapeSequences.*;

public class DrawChessBoard {
    private static final String DARK_TILE_COLOR = SET_BG_COLOR_LIGHT_GREY;
    private static final String LIGHT_TILE_COLOR = SET_BG_COLOR_WHITE;
    private static final String EDGE_TILE_COLOR = SET_BG_COLOR_DARK_GREY;
    private static final String DARK_HIGHLIGHTED_TILE_COLOR = SET_BG_COLOR_DARK_GREEN;
    private static final String LIGHT_HIGHLIGHTED_TILE_COLOR = SET_BG_COLOR_GREEN;
    private static final String LIGHT_PIECE_COLOR = SET_TEXT_COLOR_BLUE;
    private static final String DARK_PIECE_COLOR = SET_TEXT_COLOR_RED;
    private static final String EDGE_CHARACTER_COLOR = SET_TEXT_COLOR_WHITE;
    private static final String SELF_TILE_COLOR = SET_BG_COLOR_YELLOW;
    private static final String EMPTY = "   ";

    public DrawChessBoard() {

    }

    /**
     * main method
     * @param playerColor player orientation
     * @return the gameBoard in string to be printed
     */
    public static String drawBoard(String playerColor, ChessBoard board, Collection<ChessMove> highlightedMoves) {
        StringBuilder result = new StringBuilder();
        Set<ChessPosition> moves = new HashSet<>();
        ChessPosition myPosition = null;
        if (highlightedMoves != null && !highlightedMoves.isEmpty()) {
            moves = movesToPositions(highlightedMoves);
            myPosition = getMyPosition(highlightedMoves);
        }
        if (playerColor.equals("white")) {
            for (int row = 9; row >= 0; row--) {
                for (int col = 0; col < 10; col++) {
                    result.append(buildingLoop(row, col, board, moves, myPosition));
                }
                result.append("\n");
            }
        } else {
            for (int row = 0; row < 10; row++) {
                for (int col = 9; col >= 0; col--) {
                    result.append(buildingLoop(row, col, board, moves, myPosition));
                }
                result.append("\n");
            }
        }
        return result.toString();
    }

    private static Set<ChessPosition> movesToPositions(Collection<ChessMove> highlightedMoves) {
        Set<ChessPosition> positions = new HashSet<>();
        for (ChessMove move : highlightedMoves) {
            positions.add(move.getEndPosition());
        }
        return positions;
    }

    private static ChessPosition getMyPosition(Collection<ChessMove> highlightedMoves) {
        for (ChessMove move : highlightedMoves) {
            return move.getStartPosition();
        }
        throw new RuntimeException("HOW DID I GET HERE? MyPosition");
    }

    /**
     *
     * @param piece object at center
     * @return that square
     */
    private static String darkSquare(String piece) {
        return (DARK_TILE_COLOR + piece + RESET_BG_COLOR);
    }

    /**
     *
     * @param piece object at center
     * @return that square
     */
    private static String lightSquare(String piece) {
        return (LIGHT_TILE_COLOR + piece + RESET_BG_COLOR);
    }

    /**
     *
     * @param piece object at center
     * @return that square
     */
    private static String edgeSquare(String piece) {
        return (EDGE_TILE_COLOR + piece + RESET_BG_COLOR);
    }

    private static String lightHighlightedSquare(String piece) {
        return (LIGHT_HIGHLIGHTED_TILE_COLOR + piece + RESET_BG_COLOR);
    }

    private static String darkHighlightedSquare(String piece) {
        return (DARK_HIGHLIGHTED_TILE_COLOR + piece + RESET_BG_COLOR);
    }

    private static String mySquare(String piece) {
        return (SELF_TILE_COLOR + piece + RESET_BG_COLOR);
    }

    /**
     *
     * @param row target row
     * @param col target col
     * @return the appreciate filled square for row, col
     */
    private static String buildingLoop(int row, int col, ChessBoard board, Set<ChessPosition> moves, ChessPosition myPosition) {
        ChessPosition currentPosition = new ChessPosition(row, col);
        boolean isHighlighted = moves.contains(currentPosition);

        if (currentPosition.equals(myPosition)){
            return mySquare(determinePiece(row, col, board));
        }
        if (row == 0 || col == 0 || row == 9 || col == 9) {
            return edgeSquare(edgeCharacterDeterminer(row, col));
        } else if ((row + col) % 2 == 0) {
            return isHighlighted ? darkHighlightedSquare(determinePiece(row, col, board)) :
                    darkSquare(determinePiece(row, col, board));
        } else {
            return isHighlighted ? lightHighlightedSquare(determinePiece(row, col, board)) :
                    lightSquare(determinePiece(row, col, board));
        }
    }

    /**
     *
     * @param row target row
     * @param col target col
     * @return what piece goes in the space, if any
     */
    private static String determinePiece(int row, int col, ChessBoard board) {
        ChessPiece chessPiece =  board.getPiece(new ChessPosition(row, col));
        if (chessPiece == null) {
            return EMPTY;
        }

        StringBuilder result = new StringBuilder();

        ChessPiece.PieceType type = chessPiece.getPieceType();
        ChessGame.TeamColor color = board.getPiece(new ChessPosition(row,col)).getTeamColor();

        String piece = "";
        String textColor = "";

        if (color.equals(ChessGame.TeamColor.WHITE)) {
            textColor = LIGHT_PIECE_COLOR;
        } else if (color.equals(ChessGame.TeamColor.BLACK)) {
            textColor = DARK_PIECE_COLOR;
        }
        if (type.equals(ChessPiece.pieceType.KING)) {
            piece = "K";
        } else if (type.equals(ChessPiece.pieceType.QUEEN)) {
            piece = "Q";
        } else if (type.equals(ChessPiece.pieceType.BISHOP)) {
            piece = "B";
        } else if (type.equals(ChessPiece.pieceType.KNIGHT)) {
            piece = "N";
        } else if (type.equals(ChessPiece.pieceType.ROOK)) {
            piece = "R";
        } else if (type.equals(ChessPiece.pieceType.PAWN)) {
            piece = "P";
        }
        result.append(String.format(textColor + " " + piece + " " + RESET_TEXT_COLOR));
        return result.toString();
    }

    /**
     *
     * @param row target row
     * @param col target col
     * @return the numbers/letters for the outside
     */
    private static String edgeCharacterDeterminer(int row, int col) {
        int dif = row - col;
        if (dif == 0 || dif == 9 || dif == -9) {
            return "   ";
        }
        if (col == 0 || col == 9) {
            return String.format(EDGE_CHARACTER_COLOR + " " + row + " ");
        }
        if (row == 0 || row == 9) {
            return String.format(EDGE_CHARACTER_COLOR + " " + ((char)('a' + col -1)) + " ");
        }
        throw new RuntimeException("determining edge character for not edge");
    }

}
