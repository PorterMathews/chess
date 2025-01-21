package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface PieceMoveCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}

class BishopMoveCalculator extends MovementHelper implements PieceMoveCalculator {

    public BishopMoveCalculator(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        pieceMoves.addAll(MovementHelper.diagonalPieceMoves(board, position));
        return pieceMoves;
    }
}

class RookMoveCalculator extends MovementHelper implements PieceMoveCalculator {

    public RookMoveCalculator(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        pieceMoves.addAll(MovementHelper.straightPieceMoves(board, position));
        return pieceMoves;
    }
}

class QueenMoveCalculator extends MovementHelper implements PieceMoveCalculator {

    public QueenMoveCalculator(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        pieceMoves.addAll(MovementHelper.straightPieceMoves(board, position));
        pieceMoves.addAll(MovementHelper.diagonalPieceMoves(board, position));
        return pieceMoves;
    }
}

class KnightMoveCalculator implements PieceMoveCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        int[][] possibilities = {
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
        };
        ChessPiece myPiece = board.getPiece(myPosition);
        for (int[] possible: possibilities) {
            ChessPosition destination = new ChessPosition(
                    myPosition.getRow() + possible[0],
                    myPosition.getColumn() + possible[1]
            );
            ChessPiece targetPiece = null;
            if (board.isInBounds(destination)) {
                targetPiece = board.getPiece(destination);
            }

            if (board.isValidMove(destination, myPiece, targetPiece)) {
                pieceMoves.add(new ChessMove(myPosition, destination, null));
            }
        };
        return pieceMoves;
    }
}

class KingMoveCalculator implements PieceMoveCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        int[][] possibilities = {
                {-1, -1}, {-1, 0}, {-1, 1}, {0, -1}, {0, 1}, {1, -1}, {1, 0}, {1, 1}
        };
        ChessPiece myPiece = board.getPiece(myPosition);
        for (int[] possible: possibilities) {
            ChessPosition destination = new ChessPosition(
                    myPosition.getRow() + possible[0],
                    myPosition.getColumn() + possible[1]
            );
            ChessPiece targetPiece = null;
            if (board.isInBounds(destination)) {
                targetPiece = board.getPiece(destination);
            }

            if (board.isValidMove(destination, myPiece, targetPiece)) {
                pieceMoves.add(new ChessMove(myPosition, destination, null));
            }
        };
        return pieceMoves;
    }
}

class PawnMoveCalculator implements PieceMoveCalculator {
    public static Object pieceMove;

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        int mult = 1;
        if (myPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            mult = -1;
        }

        ChessPosition march = new ChessPosition(myPosition.getRow() +mult, myPosition.getColumn());
        if (board.getPiece(march) == null) {
            if (march.getRow() == 8 || march.getRow() == 1) {
                for (ChessPiece.PieceType promotionPiece : new ChessPiece.PieceType[]{
                        ChessPiece.PieceType.QUEEN,
                        ChessPiece.PieceType.ROOK,
                        ChessPiece.PieceType.BISHOP,
                        ChessPiece.PieceType.KNIGHT}) {
                    pieceMoves.add(new ChessMove(myPosition, march, promotionPiece));
                }
            } else {
                pieceMoves.add(new ChessMove(myPosition, march, null));
            }
            ChessPosition fastMarch = new ChessPosition(myPosition.getRow() +2 * mult, myPosition.getColumn());
            if (((myPosition.getRow() == 2 && myPiece.getTeamColor() == ChessGame.TeamColor.WHITE) ||
                    (myPosition.getRow() == 7 && myPiece.getTeamColor() == ChessGame.TeamColor.BLACK)) &&
                    board.getPiece(fastMarch) == null) {
                pieceMoves.add(new ChessMove(myPosition, fastMarch, null));
            }
        }
        pieceMoves.addAll(MovementHelper.pawnAttackMoves(board, myPosition));



        return pieceMoves;
    }
}