package chess;

import java.util.ArrayList;
import java.util.Collection;

public class MovementHelper {

    public MovementHelper(ChessBoard board, ChessPosition myPosition) {

    }

    public static Collection<ChessMove> diagonalPieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();

        int thisRow = myPosition.getRow();
        int thisCol = myPosition.getColumn();
        ChessPiece myPiece = board.getPiece(myPosition);

        //Up-Right
        int moves = 1;
        while (true) {
            int possibleRow = thisRow + moves;
            int possibleCol = thisCol + moves;

            ChessPosition possiblePosition = new ChessPosition(possibleRow, possibleCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }

        //Down-Right
        moves = 1;
        while (true) {
            int possibleRow = thisRow - moves;
            int possibleCol = thisCol + moves;

            ChessPosition possiblePosition = new ChessPosition(possibleRow, possibleCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }

        //Down-Left
        moves = 1;
        while (true) {
            int possibleRow = thisRow - moves;
            int possibleCol = thisCol - moves;

            ChessPosition possiblePosition = new ChessPosition(possibleRow, possibleCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }

        //Up-Left
        moves = 1;
        while (true) {
            int possibleRow = thisRow + moves;
            int possibleCol = thisCol - moves;

            ChessPosition possiblePosition = new ChessPosition(possibleRow, possibleCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }

        return pieceMoves;
    }

    public static Collection<ChessMove> straightPieceMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();

        int thisRow = myPosition.getRow();
        int thisCol = myPosition.getColumn();
        ChessPiece myPiece = board.getPiece(myPosition);

        //Up
        int moves = 1;
        while (true) {
            int possibleRow = thisRow + moves;

            ChessPosition possiblePosition = new ChessPosition(possibleRow, thisCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }

        //Down
        moves = 1;
        while (true) {
            int possibleRow = thisRow - moves;

            ChessPosition possiblePosition = new ChessPosition(possibleRow, thisCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }

        //Left
        moves = 1;
        while (true) {
            int possibleCol = thisCol - moves;

            ChessPosition possiblePosition = new ChessPosition(thisRow, possibleCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }

        //Right
        moves = 1;
        while (true) {
            int possibleCol = thisCol + moves;

            ChessPosition possiblePosition = new ChessPosition(thisRow, possibleCol);

            if (!board.isInBounds(possiblePosition)) {
                break;
            }

            ChessPiece piece = board.getPiece(possiblePosition);
            if (piece == null) {
                pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
            } else {
                if (!board.canCapture(myPiece, piece)) {
                    pieceMoves.add(new ChessMove(myPosition, possiblePosition, null));
                }
                break;
            }
            moves++;
        }


        return pieceMoves;
    }

    public static Collection<ChessMove> pawnAttackMoves(ChessBoard board, ChessPosition myPosition) {
        Collection<ChessMove> pieceMoves = new ArrayList<>();
        ChessPiece myPiece = board.getPiece(myPosition);
        int mult = 1;
        if (myPiece.getTeamColor() == ChessGame.TeamColor.BLACK) {
            mult = -1;
        }
        ChessPosition leftAttack = new ChessPosition(myPosition.getRow() + mult, myPosition.getColumn() - 1);
        ChessPosition rightAttack = new ChessPosition(myPosition.getRow() + mult, myPosition.getColumn() + 1);


        if (board.isInBounds(leftAttack) && board.getPiece(leftAttack) != null) {
            if (!board.canCapture(myPiece, board.getPiece(leftAttack))) {
                if (leftAttack.getRow() == 8 || leftAttack.getRow() == 1) {
                    for (ChessPiece.PieceType promotionPiece : new ChessPiece.PieceType[]{
                            ChessPiece.PieceType.QUEEN,
                            ChessPiece.PieceType.ROOK,
                            ChessPiece.PieceType.BISHOP,
                            ChessPiece.PieceType.KNIGHT}) {
                        pieceMoves.add(new ChessMove(myPosition, leftAttack, promotionPiece));
                    }
                } else {
                    pieceMoves.add(new ChessMove(myPosition, leftAttack, null));
                }
            }
        }
        if (board.isInBounds(rightAttack) && board.getPiece(rightAttack) != null) {
            if (!board.canCapture(myPiece, board.getPiece(rightAttack))) {
                if (rightAttack.getRow() == 8 || rightAttack.getRow() == 1) {
                    for (ChessPiece.PieceType promotionPiece : new ChessPiece.PieceType[]{
                            ChessPiece.PieceType.QUEEN,
                            ChessPiece.PieceType.ROOK,
                            ChessPiece.PieceType.BISHOP,
                            ChessPiece.PieceType.KNIGHT}) {
                        pieceMoves.add(new ChessMove(myPosition, rightAttack, promotionPiece));
                    }
                } else {
                    pieceMoves.add(new ChessMove(myPosition, rightAttack, null));
                }
            }

        }
        return pieceMoves;
    }
}