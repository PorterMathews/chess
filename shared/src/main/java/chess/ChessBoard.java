package chess;

/**
 * A chessboard that can hold and rearrange chess pieces.
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessBoard {
    private final ChessPiece[][] board = new ChessPiece[8][8];

    public ChessBoard() {
        
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        board[position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return board[position.getRow()][position.getColumn()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        throw new RuntimeException("Not implemented");
    }


    /**
     * Checks to see if the destination is in bounds
     *
    *  @param position The destination position you are checking
    *  @return false if it is out of bounds otherwise true
     *  */
    public boolean isInBounds(ChessPosition position) {
        if (position.getRow() > 8 || position.getColumn() > 8) {
            return false;
        }
        else if (position.getRow() < 0 || position.getColumn() < 0) {
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
        if (pieceOne == null || pieceTwo == null) {
            return false;
        }
        return pieceOne.getTeamColor() != pieceTwo.getTeamColor();
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
