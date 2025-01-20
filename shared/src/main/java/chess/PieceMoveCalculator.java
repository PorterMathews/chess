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
                {-7, -7},
                {-6, -6},
                {-5, -5},
                {-4, -4},
                {-3, -3},
                {-2, -2},
                {-1, -1},
                {1, 1},
                {2, 2},
                {3, 3},
                {4, 4},
                {5, 5},
                {6, 6},
                {7, 7},
                {-7, 7},
                {-6, 6},
                {-5, 5},
                {-4, 4},
                {-3, 3},
                {-2, 2},
                {-1, 1},
                {1, -1},
                {2, -2},
                {3, -3},
                {4, -4},
                {5, -5},
                {6, -6},
                {7, -7}
        };
        for (int[] possible: possibilities) {
            if (possible)
        }
        return pieceMoves;
    }

}

class KnightMoveCalculator implements PieceMoveCalculator {

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition position) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        int[][] possibilities = {
                {1, -2}, {2, -1}, {2, 1}, {1, 2},
                {-1, 2}, {-2, 1}, {-2, -1}, {-1 ,-2}
        };
        for (int[] possible: possibilities) {
            if (isValidMove(board, position, possible)){
                pieceMoves.add(new ChessMove);
            };
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