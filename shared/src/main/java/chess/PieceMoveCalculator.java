package chess;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public interface PieceMoveCalculator {
    void pieceMoves(ChessBoard board, ChessPosition myPosition);
}

class BishopMoveCalculator implements PieceMoveCalculator {
    public static Object pieceMove;

    @Override
    public List<int[]> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        List<int[]> pieceMoves = new ArrayList<>();

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
            pieceMoves.add(new int[]{possible[0], possible[1]});
        }
        return pieceMoves;
    }

}