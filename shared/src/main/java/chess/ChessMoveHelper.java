package chess;

import java.util.ArrayList;
import java.util.Collection;

public class ChessMoveHelper {

    public  ChessMoveHelper(ChessBoard board, ChessPosition myPosition) {



    }

    public Collection<ChessMove> hopMoveCalc (ChessBoard board, ChessPosition myPosition,int[][] possibilities) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

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

    public Collection<ChessMove> moveCalc (ChessBoard board, ChessPosition myPosition,int[][] directions) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);

        int steps = 1;
        for (int[] direction: directions) {
            steps = 1;
            while (true) {
                int possibleRow = myPosition.getRow() + steps * direction[0];
                int possibleCol = myPosition.getColumn() + steps * direction[1];
                ChessPosition possiblePosition = new ChessPosition(possibleRow, possibleCol);

                if (!board.isInBounds(possiblePosition)){
                    break;
                }

                if (board.isValidMove(possiblePosition, myPiece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }

                if (board.isOccupied(possiblePosition)) {
                    break;
                }
                steps++;
            }
        }
        return pieceMoves;
    }


    public Collection<ChessMove> diagonalMoveCalc(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = { {1,1},{1,-1},{-1,-1},{-1,1} };
        return moveCalc(board, myPosition, directions);
    }

    public Collection<ChessMove> straightMoveCalc(ChessBoard board, ChessPosition myPosition) {
        int[][] directions = { {1,0},{0,-1},{-1,0},{0,1} };
        return moveCalc(board, myPosition, directions);
    }

    public Collection<ChessMove> pawnMoveCalc(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        int colorMult = 1;
        if (myPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            colorMult = -1;
        }

        ChessPiece.PieceType[] types = {
                ChessPiece.pieceType.QUEEN,
                ChessPiece.pieceType.ROOK,
                ChessPiece.pieceType.BISHOP,
                ChessPiece.pieceType.KNIGHT
        };

        ChessPosition march = new ChessPosition((myPosition.getRow() + colorMult),
                (myPosition.getColumn()));
        ChessPosition fastMarch = new ChessPosition((myPosition.getRow() + 2*colorMult),
                (myPosition.getColumn()));

        if (board.isValidPawnMove(march)) {
            if (march.getRow() == 1 || march.getRow() == 8) {
                for (ChessPiece.PieceType type : types) {
                    pieceMoves.add(new ChessMove(myPosition, march, type));
                }
            } else {
                pieceMoves.add(new ChessMove(myPosition, march, null));
            }
            if (board.isValidPawnMove(fastMarch) &&
                            ((myPosition.getRow() == 2 && myPiece.getTeamColor() == ChessGame.TeamColor.WHITE) ||
                            (myPosition.getRow() == 7 && myPiece.getTeamColor() == ChessGame.TeamColor.BLACK))) {
                pieceMoves.add(new ChessMove(myPosition, fastMarch, null));
            }
        }

        int[][] attacks = { {colorMult, 1},{colorMult, -1} };

        for (int[] attack : attacks) {
            int row = myPosition.getRow() + attack[0];
            int col = myPosition.getColumn() + attack[1];
            ChessPosition attackPosition = new ChessPosition(row, col);

            if (board.isValidPawnAttack(attackPosition, myPiece)) {
                if (attackPosition.getRow() == 1 || attackPosition.getRow() == 8) {
                    for (ChessPiece.PieceType type : types) {
                        pieceMoves.add(new ChessMove(myPosition, attackPosition, type));
                    }
                } else {
                    pieceMoves.add(new ChessMove(myPosition, attackPosition, null));
                }
            }
        }

        return pieceMoves;
    }

}
