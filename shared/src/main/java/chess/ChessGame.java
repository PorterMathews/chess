package chess;

import chess.ChessPiece.PieceType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;

/**
 * For a class that can manage a chess game, making moves on a board
 * <p>
 * Note: You can add to this class, but you may not alter
 * signature of the existing methods.
 */
public class ChessGame {

    private TeamColor teamTurn;
    private ChessBoard chessBoard;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        ChessGame chessGame = (ChessGame) o;
        return teamTurn == chessGame.teamTurn && Objects.equals(chessBoard, chessGame.chessBoard);
    }

    @Override
    public int hashCode() {
        return Objects.hash(teamTurn, chessBoard);
    }

    /**
     * @return Which team's turn it is
     */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /**
     * Set's which teams turn it is
     *
     * @param team the team whose turn it is
     */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /**
     * Enum identifying the 2 possible teams in a chess game
     */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**
     * Gets a valid moves for a piece at the given location
     *
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition
     */
    public Collection<ChessMove> validMoves(ChessPosition startPosition) {
        Collection<ChessMove> validMoves = new ArrayList<>();
        ChessPiece myPiece = chessBoard.getPiece(new ChessPosition(startPosition.getRow(), startPosition.getColumn()));
        if (myPiece == null) {
            return null;
        }
        TeamColor myTeamColor = myPiece.getTeamColor();

        Collection<ChessMove> possibleMoves = myPiece.pieceMoves(chessBoard, startPosition);

        for (ChessMove move : possibleMoves){
            ChessPiece pieceAtMoveSpot = chessBoard.getPiece(move.getEndPosition());
            chessBoard.addPiece(move.getEndPosition(), null);
            chessBoard.addPiece(move.getEndPosition(), myPiece);
            chessBoard.addPiece(move.getStartPosition(), null);
            if (!isInCheck(myTeamColor)) {
                validMoves.add(move);
            }
            chessBoard.addPiece(move.getEndPosition(), null);
            chessBoard.addPiece(move.getEndPosition(), pieceAtMoveSpot);
            chessBoard.addPiece(move.getStartPosition(), myPiece);
        }

        return validMoves;
    }

    /**
     * Makes a move in a chess game
     *
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid
     */
    public void makeMove(ChessMove move) throws InvalidMoveException {
        ChessPiece movingPiece = chessBoard.getPiece(move.getStartPosition());
        if (movingPiece == null) {
            throw new InvalidMoveException("No piece to move");
        }

        if (movingPiece.getTeamColor() != getTeamTurn()) {
            throw new InvalidMoveException("Not that teams turn");
        }


        Collection<ChessMove> pieceMoves = validMoves(move.getStartPosition());

        if (pieceMoves == null || pieceMoves.isEmpty()) {
            throw new InvalidMoveException("No valid moves for that piece");
        }

        boolean isMoveInValidMoves = false;
        for (ChessMove pieceMove : pieceMoves) {
            if (move.getEndPosition().equals(pieceMove.getEndPosition())) {
                isMoveInValidMoves = true;
                break;
            }
        }

        if (!isMoveInValidMoves) {
            throw new InvalidMoveException("Not a valid location to move to");
        }

        chessBoard.addPiece(move.getStartPosition(), null);
        if (move.getPromotionPiece() != null) {
            chessBoard.addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
        } else {
            chessBoard.addPiece(move.getEndPosition(), movingPiece);
        }

        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     *
     * @param threateningPiece The piece in the position that is being tested
     * @param position The position of the threatening position
     * @param kingPosition The position of the opposing King
     * @param teamColor The Color of the opposing King, threateningPiece must be this color
     * @return true if the threatening piece can attack the king
     */
    public boolean isKingThreatened (ChessPiece threateningPiece, ChessPosition position, ChessPosition kingPosition, TeamColor teamColor) {
        if (threateningPiece == null) {
            return false;
        } else if (threateningPiece.getTeamColor() != teamColor) {
            Collection<ChessMove> moves = threateningPiece.pieceMoves(chessBoard, position);
            for (ChessMove move : moves) {
                if (move.getEndPosition().equals(kingPosition)) {
                    return true;
                }
            }
        }
        return false;
    }


    /**
     * Determines if the given team is in check
     *
     * @param teamColor which team to check for check
     * @return True if the specified team is in check
     */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;

        for (int col = 1; col <= 8; col++) {
            for (int row = 1; row <= 8; row++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(position);

                if (piece == null){
                    continue;
                } else if (piece.getPieceType() == ChessPiece.PieceType.KING &&
                            piece.getTeamColor() == teamColor) {
                    kingPosition = position;
                    break;
                }
            }
        }

        if (kingPosition == null) {
            throw new RuntimeException("Didn't find King");
        }

        for (int col = 1; col <= 8; col++) {
            for (int row = 1; row <= 8; row++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(position);
                if (isKingThreatened(piece, position, kingPosition, teamColor)) {
                    return true;
                }

            }
        }
        return false;
    }

    /**
     *
     * @param teamColor The team color the piece must be at that location, else false
     * @param row The row position of the piece you are checking
     * @param col The col position of the piece you are checking
     * @return ture if there is a valid move for the piece at that position
     */
    public boolean anyValidMoves2(TeamColor teamColor, int row, int col) {
        ChessPosition position = new ChessPosition(row, col);
        ChessPiece piece = chessBoard.getPiece(position);
        if (piece == null) {
            return false;
        } else if (piece.getTeamColor() == teamColor) {
            Collection<ChessMove> moves = validMoves(position);
            if (moves == null) {
            }  else if (!moves.isEmpty()){
                return true;
            }
            return false;
        }
        return false;
    }


    /**
     *
     * @param teamColor the team for which you are checking the valid move
     * @return True if there is any move a piece could make, else false
     */
    public boolean anyValidMoves(TeamColor teamColor) {
        for (int col = 1; col <= 8; col++) {
            for (int row = 1; row <= 8; row++) {
                if (anyValidMoves2(teamColor, row, col)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Determines if the given team is in checkmate
     *
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate
     */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return !anyValidMoves(teamColor);
        }
        return false;
    }

    /**
     * Determines if the given team is in stalemate, which here is defined as having
     * no valid moves
     *
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false
     */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return !anyValidMoves(teamColor);
        }
        return false;
    }

    /**
     * Sets this game's chessboard with a given board
     *
     * @param board the new board to use
     */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**
     * Gets the current chessboard
     *
     * @return the chessboard
     */
    public ChessBoard getBoard() {
        return chessBoard;
    }
}
