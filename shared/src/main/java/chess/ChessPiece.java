package chess;

import java.util.Collection;
import java.util.Objects;

/**
 * Represents a single chess piece
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessPiece {

    private final PieceType pieceType;
    private final ChessGame.TeamColor pieceColor;

    public ChessPiece(ChessGame.TeamColor pieceColor, PieceType pieceType) {
        this.pieceColor = pieceColor;
        this.pieceType = pieceType;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessPiece that = (ChessPiece) o;
        return pieceType == that.pieceType && pieceColor == that.pieceColor;
    }

    @Override
    public int hashCode() {
        return Objects.hash(pieceType, pieceColor);
    }

    /**
     * The various different chess piece options
     */
    public enum PieceType {
        KING,
        QUEEN,
        BISHOP,
        KNIGHT,
        ROOK,
        PAWN
    }

    /**
     * @return Which team this chess piece belongs to
     */
    public ChessGame.TeamColor getTeamColor() {
        return this.pieceColor;
    }

    /**
     * @return which type of chess piece this piece is
     */
    public PieceType getPieceType() {
        return this.pieceType;
    }

    /**
     * Calculates all the positions a chess piece can move to
     * Does not take into account moves that are illegal due to leaving the king in
     * danger
     *
     * @return Collection of valid moves
     */
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        switch (this.pieceType) {
            case KING:
                KingMoveCalculator kingMoveCalculator = new KingMoveCalculator();
                return kingMoveCalculator.pieceMoves(board, myPosition);

            case QUEEN:
                QueenMoveCalculator queenMoveCalculator = new QueenMoveCalculator(board, myPosition);
                return queenMoveCalculator.pieceMoves(board, myPosition);

            case BISHOP:
                BishopMoveCalculator bishopMoveCalculator = new BishopMoveCalculator(board, myPosition);
                return bishopMoveCalculator.pieceMoves(board, myPosition);

            case KNIGHT:
                KnightMoveCalculator knightMoveCalculator = new KnightMoveCalculator();
                return knightMoveCalculator.pieceMoves(board, myPosition);

            case ROOK:
                RookMoveCalculator rookMoveCalculator = new RookMoveCalculator(board, myPosition);
                return rookMoveCalculator.pieceMoves(board, myPosition);

            case PAWN:

                break;

            default:
                System.out.println("Invalid piece type");
                break;
        }
        return null;
    }
}