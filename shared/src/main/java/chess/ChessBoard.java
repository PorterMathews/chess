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
    private final ChessPiece[][] chessBoard;

    public ChessBoard() {
        this.chessBoard = new ChessPiece[9][9];
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessBoard that = (ChessBoard) o;
        return Objects.deepEquals(chessBoard, that.chessBoard);
    }

    @Override
    public int hashCode() {
        return Arrays.deepHashCode(chessBoard);
    }

    /**
     * Adds a chess piece to the chessboard
     *
     * @param position where to add the piece to
     * @param piece    the piece to add
     */
    public void addPiece(ChessPosition position, ChessPiece piece) {
        chessBoard[position.getRow()][position.getColumn()] = piece;
    }

    /**
     * Gets a chess piece on the chessboard
     *
     * @param position The position to get the piece from
     * @return Either the piece at the position, or null if no piece is at that
     * position
     */
    public ChessPiece getPiece(ChessPosition position) {
        return chessBoard[position.getRow()][position.getColumn()];
    }

    /**
     * Sets the board to the default starting board
     * (How the game of chess normally starts)
     */
    public void resetBoard() {
        for (int col = 0; col <= 8; col++) {
            for (int row = 0; row <= 8; row++) {
                chessBoard[row][col] = null;
            }
        }

        int column = 1;
        while (column <= 8) {
            addPiece(new ChessPosition(2, column), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.PAWN));
            addPiece(new ChessPosition(7, column), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.PAWN));
            column++;
        }

        //WhitePieces
        addPiece(new ChessPosition(1, 1), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(1, 2), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 3), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 4), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(1, 5), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(1, 6), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(1, 7), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(1, 8), new ChessPiece(ChessGame.TeamColor.WHITE, ChessPiece.PieceType.ROOK));

        //BlackPieces
        addPiece(new ChessPosition(8, 1), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
        addPiece(new ChessPosition(8, 2), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 3), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 4), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.QUEEN));
        addPiece(new ChessPosition(8, 5), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KING));
        addPiece(new ChessPosition(8, 6), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.BISHOP));
        addPiece(new ChessPosition(8, 7), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.KNIGHT));
        addPiece(new ChessPosition(8, 8), new ChessPiece(ChessGame.TeamColor.BLACK, ChessPiece.PieceType.ROOK));
    }

    /**
     *
     * @param checkPosition The position you are checking
     * @return True if in bounds
     */
    public boolean isInBounds(ChessPosition checkPosition) {
        if (checkPosition.getColumn() > 8 || checkPosition.getRow() > 8 ||
                checkPosition.getColumn() < 1 || checkPosition.getRow() < 1) {
            return false;
        }
        return true;
    }

    /**
     *
     * @param checkPosition The position the potential other piece is at
     * @param myPiece The piece you are attempting to move to checkPosition
     * @return True if the piece is a different color or if the space is empty
     */
    public boolean isOccupiedByEnemy(ChessPosition checkPosition, ChessPiece myPiece) {
        if (getPiece(checkPosition) == null) {
            return true;
        }
        ChessPiece occupiedPiece = getPiece(checkPosition);
        return occupiedPiece.getTeamColor() != myPiece.getTeamColor();
    }

    /**
     *
     * @param checkPosition The position you want to know if it is occupied
     * @return returns true if occupied
     */
    public boolean isOccupied(ChessPosition checkPosition) {
        return getPiece(checkPosition) != null;
    }

    /**
     *
     * @param checkPosition The position you are checking
     * @param myPiece The piece you are attempting to move
     * @return ture if the square is on the board and the checkPosition is empty or has an enemy piece
     */
    public boolean isValidMove(ChessPosition checkPosition, ChessPiece myPiece) {
        return isInBounds(checkPosition) && isOccupiedByEnemy(checkPosition, myPiece);
    }

    /**
     *
     * @param checkPosition The position you are checking
     * @param myPiece The piece you are attempting to move
     * @return Ture only if the space is occupied by an enemy piece, else false
     */
    public boolean pawnIsOccupiedByEnemy(ChessPosition checkPosition, ChessPiece myPiece) {
        if (getPiece(checkPosition) == null) {
            return false;
        }
        ChessPiece occupiedPiece = getPiece(checkPosition);
        return occupiedPiece.getTeamColor() != myPiece.getTeamColor();
    }

    /**
     *
     * @param checkPosition The position you are trying to move to
     * @return true if the position is in bounds and is not occupied
     */
    public boolean isValidPawnMove(ChessPosition checkPosition) {
        return isInBounds(checkPosition) && !isOccupied(checkPosition);
    }

    /**
     *
     * @param checkPosition The position you are attempting to attack
     * @param myPiece The piece you are attacking with, pawn
     * @return true if the position is in bounds and is occupied by an enemy
     */
    public boolean isValidPawnAttack(ChessPosition checkPosition, ChessPiece myPiece) {
        return isInBounds(checkPosition) && pawnIsOccupiedByEnemy(checkPosition, myPiece);
    }

}