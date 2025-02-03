package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface ChessMovesCalc {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}

class KnightMovesCalc extends ChessMoveHelper implements ChessMovesCalc {

    KnightMovesCalc(ChessBoard board, ChessPosition myPosition) {
        super(board,myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        int[][] possibilities = {
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
        };

        return hopMoveCalc(board, myPosition, possibilities);

    }
}

class KingMovesCalc extends ChessMoveHelper implements ChessMovesCalc {

    public KingMovesCalc(ChessBoard board, ChessPosition myPosition) {
        super(board,myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {

        int[][] possibilities = {
                {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
        };

        return hopMoveCalc(board, myPosition, possibilities);
    }
}

class BishopMoveCalc extends ChessMoveHelper implements ChessMovesCalc {

    public BishopMoveCalc(ChessBoard board, ChessPosition myPosition){
        super(board, myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        pieceMoves.addAll(diagonalMoveCalc(board, myPosition));
        return pieceMoves;
    }
}

class RookMoveCalc extends ChessMoveHelper implements ChessMovesCalc {

    public RookMoveCalc(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        pieceMoves.addAll(straightMoveCalc(board, myPosition));
        return pieceMoves;
    }
}

class QueenMoveCalc extends ChessMoveHelper implements ChessMovesCalc {

    public QueenMoveCalc(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        pieceMoves.addAll(straightMoveCalc(board, myPosition));
        pieceMoves.addAll(diagonalMoveCalc(board, myPosition));
        return pieceMoves;
    }
}

class PawnMoveCalc extends ChessMoveHelper implements ChessMovesCalc {

    public PawnMoveCalc(ChessBoard board, ChessPosition myPosition) {
        super(board, myPosition);
    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        pieceMoves.addAll(pawnMoveCalc(board, myPosition));
        return pieceMoves;
    }
}