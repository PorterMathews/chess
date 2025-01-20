package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface PieceMoveCalculator {
    Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}

class BishopMoveCalculator implements PieceMoveCalculator {
    public static Object pieceMove;

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        int[][] possibilities = {
                {-7, -7}, {-6, -6}, {-5, -5}, {-4, -4}, {-3, -3}, {-2, -2}, {-1, -1},
                {1, 1}, {2, 2}, {3, 3}, {4, 4}, {5, 5}, {6, 6}, {7, 7},
                {-7, 7}, {-6, 6}, {-5, 5}, {-4, 4}, {-3, 3}, {-2, 2}, {-1, 1},
                {1, -1}, {2, -2}, {3, -3}, {4, -4}, {5, -5}, {6, -6}, {7, -7}
        };

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
            ChessPiece targetPiece = board.getPiece(destination);
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

        return pieceMoves;
    }
}