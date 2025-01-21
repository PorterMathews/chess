package chess;

import java.util.Arrays;
import java.util.Objects;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] board = new ChessPiece[9][9];

    public ChessBoard() {

    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(board, that.board);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(board);
    }



    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        if (isInBounds(position)) {
            if (getPiece(position) == null) {
                board[position.getRow()][position.getColumn()] = piece;
            } else {
                throw new RuntimeException("Already occupied position");
            }
        }
        else throw new RuntimeException("Not A valid position");
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        if (isInBounds(position)) {
            return board[position.getRow()][position.getColumn()];
        }
        return null;
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                board[row][col] = null;
            }
        };

        //White Rook
        addPiece(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        //Black Rook
        addPiece(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));

        //White Knight
        addPiece(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));

        //Black Knight
        addPiece(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));

        //White Bishop;
        addPiece(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));

        //Black Bishop
        addPiece(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));

        //White Queen
        addPiece(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));

        // Black Queen
        addPiece(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));

        //White King
        addPiece(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));

        //Black Queen
        addPiece(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));

        //Pawns
        for (int col = 0; col < 8; col++) {
            addPiece(new ChessPosition(2, col), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, col), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
        }
    }


    /**
     * Checks to see if the destination is in bounds
     *
    *  @param position The destination position you are checking
    *  @return false if it is out of bounds otherwise true
     *  */
    public boolean isInBounds(ChessPosition position) {
        if (position == null) {
            return false;
        }
        else if (position.getRow() > 8 || position.getColumn() > 8) {
            return false;
        }
        else if (position.getRow() < 1 || position.getColumn() < 1) {
            return false;
        }
        else {
            return true;
        }
    }

    /**
     * Meant to check if a piece is able to be captured
     *
     * @param pieceOne The piece you are attempting to move
     * @param pieceTwo The piece that is in a potential destination
     * @return true if different colors, else false
     */
    public boolean canCapture(ChessPiece pieceOne, ChessPiece pieceTwo) {
        if (pieceOne == null) {
            throw new RuntimeException("pieceOne is null");
        }
        if (pieceTwo == null) {
            return false;
        }
        return pieceOne.getTeamColor() == pieceTwo.getTeamColor();
    }

    /**
     *  Checks to see if move is valid
     *
     * @param position The destination position you are trying to move to
     * @param pieceOne The piece you are moving
     * @param pieceTwo The piece that is at the destination
     * @return returns true if the move is on the board or the piece at the destination can be captured
     */

    public boolean isValidMove(ChessPosition position, ChessPiece pieceOne, ChessPiece pieceTwo) {
        if (pieceOne == null) {
            throw new RuntimeException("Moving piece is null");
        }

        else if (!isInBounds(position)) {
            return false;
        }
        else if (canCapture(pieceOne, pieceTwo)) {
            return false;
        }
//        else if(putsKingInCheck()){
//            return false;
//        }
        else return true;
    }
}
