package chess;

import java.util.ArrayList;
import java.util.Collection;

public interface ChessMovesCalc {
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition);
}

class KnightMovesCalc implements ChessMovesCalc {

    KnightMovesCalc(ChessBoard board, ChessPosition myPosition) {

    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece myPiece = board.getPiece(myPosition);
        Collection<ChessMove> pieceMoves = new ArrayList<>();

        int[][] possibilities = {
                {2, 1}, {1, 2}, {-1, 2}, {-2, 1}, {-2, -1}, {-1, -2}, {1, -2}, {2, -1}
        };

        for (int[] possible : possibilities) {
            int row = possible[0] + myPosition.getRow();
            int col = possible[1] + myPosition.getColumn();
            ChessPosition possiblePosition = new ChessPosition(row, col);
            if(board.isValidMove(possiblePosition, myPiece)) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            }
        }
        return pieceMoves;
    }
}

class KingMovesCalc implements ChessMovesCalc {

    public KingMovesCalc(ChessBoard board, ChessPosition myPosition) {

    }

    @Override
    public Collection<ChessMove> pieceMoves(ChessBoard board, ChessPosition myPosition) {
        ChessPiece myPiece = board.getPiece(myPosition);
        Collection<ChessMove> pieceMoves = new ArrayList<>();

        int[][] possibilities = {
                {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}, {-1, -1}, {0, -1}, {1, -1}
        };

        for (int[] possible : possibilities) {
            int row = possible[0] + myPosition.getRow();
            int col = possible[1] + myPosition.getColumn();
            ChessPosition possiblePosition = new ChessPosition(row, col);
            if(board.isValidMove(possiblePosition, myPiece)) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            }
        }
        return pieceMoves;
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
        pieceMoves.addAll(StraightMoveCalc(board, myPosition));
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
        pieceMoves.addAll(StraightMoveCalc(board, myPosition));
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
        pieceMoves.addAll(PawnMoveCalc(board, myPosition));
        return pieceMoves;
    }
}