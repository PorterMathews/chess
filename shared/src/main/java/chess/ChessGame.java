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
    private static ChessBoard chessBoard;
    private boolean canEnPassant;
    private int enPassantCol;

    public ChessGame() {
        teamTurn = TeamColor.WHITE;
        chessBoard = new ChessBoard();
        chessBoard.resetBoard();
        canEnPassant = false;
        enPassantCol = 0;
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
     * @param canEnPassant
     */
    public void setCanEnPassant(boolean canEnPassant) {
        this.canEnPassant = canEnPassant;
    }

    /**
     * @return
     */
    public boolean getCanEnPassant() {
        return this.canEnPassant;
    }

    /**
     * @param enPassantCol
     */
    public void setEnPassantCol(int enPassantCol) {
        this.enPassantCol = enPassantCol;
    }

    /**
     * @return
     */
    public int getEnPassantCol() {
        return enPassantCol;
    }

    /** @return Which team's turn it is */
    public TeamColor getTeamTurn() {
        return teamTurn;
    }

    /** Set's which teams turn it is
     * @param team the team whose turn it is */
    public void setTeamTurn(TeamColor team) {
        teamTurn = team;
    }

    /** Enum identifying the 2 possible teams in a chess game */
    public enum TeamColor {
        WHITE,
        BLACK
    }

    /**Gets a valid moves for a piece at the given location
     * @param startPosition the piece to get valid moves for
     * @return Set of valid moves for requested piece, or null if no piece at
     * startPosition */
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
        if (myPiece.getPieceType() == PieceType.KING) {
            validMoves.addAll(checkCastling(myTeamColor, startPosition));
        }
        if (getCanEnPassant() && myPiece.getPieceType() == PieceType.PAWN) {
            validMoves.addAll(enPassantAttacks(myTeamColor, startPosition));
        }
        return validMoves;
    }

    /** Calculates the enPassant attacks
     * @param myTeamColor The color of the pawn that is being checked
     * @param myPosition the position of the pawn
     * @return the attack move only if the pawn can perform an enPassant */
    public Collection<ChessMove> enPassantAttacks(TeamColor myTeamColor, ChessPosition myPosition) {
        Collection<ChessMove> enPassantAttacks = new ArrayList<>();
        int row = 5;
        int direction = 1;
        if (myTeamColor == TeamColor.BLACK) {
            row = 4;
            direction = -1;
        }
        ChessPosition leftPosition = new ChessPosition(row, enPassantCol -1);
        ChessPosition rightPosition = new ChessPosition(row, enPassantCol +1);

        if (!(myPosition.equals(leftPosition) || myPosition.equals(rightPosition))) {
            return enPassantAttacks;
        }
        if (getTeamTurn() != chessBoard.getPiece(myPosition).getTeamColor()) {
            return enPassantAttacks;
        }
        enPassantAttacks.add(new ChessMove(myPosition, new ChessPosition(row + direction, enPassantCol), null));
        return enPassantAttacks;
    }

    /**Checks if a castle is possible for the king of either color
     * @param myTeamColor the turn color
     * @param myPosition the position of the king in question
     * @return any ChessMoves the king could castle to */
    public Collection<ChessMove> checkCastling(TeamColor myTeamColor, ChessPosition myPosition) {
        Collection<ChessMove> castlingMoves = new ArrayList<>();
        int row = 1;
        if (myTeamColor == TeamColor.BLACK) {
            row = 8;
        }
        if (!chessBoard.getPiece(myPosition).getHasNotMoved()) {
            return castlingMoves;
        }
        int[] directions = {1, -1};
        for (int direction : directions) {
            if (isBackRowClear(row, direction) && noCheckAlongPath(row, direction, myTeamColor)) {
                ChessPosition position = new ChessPosition(row, 5 + 2*direction);
                castlingMoves.add(new ChessMove(myPosition, position, null));
            }
        }
        return castlingMoves;
    }

    /**Checks to see if the king would be in check while castling
     * @param row The row we are checking, only used for row 1 or 8
     * @param direction if we are moving right (1) or left (-1)
     * @return true if the row is clear and the rook at the end hasn't moved */
    public boolean isBackRowClear(int row, int direction) {
        int col = 5 + direction;
        while (col > 1 && col < 8) {
            if (chessBoard.getPiece(new ChessPosition(row, col)) != null){
                return false;
            }
            col += direction;
        }
        if (direction == -1) {
            ChessPiece rook = chessBoard.getPiece(new ChessPosition(row, 1));
            if (rook != null && rook.getPieceType() == PieceType.ROOK && rook.getHasNotMoved()) {
                return true;
            }
        }
        if (direction == 1) {
            ChessPiece rook = chessBoard.getPiece(new ChessPosition(row, 8));
            if (rook != null && rook.getPieceType() == PieceType.ROOK && rook.getHasNotMoved()) {
                return true;
            }
        }
        return false;
        //throw new RuntimeException("Unexpected direction while checkingCastling");
    }

    /**Used to check if the back row is clear and there is a rook on the end that hasn't moved
     * @param row The row we are checking, only used for row 1 or 8
     * @param direction if we are moving right (1) or left (-1)
     * @param myTeamColor The team who we are checking if they can castle
     * @return true if the king and the next two spots don't result in a check */
    public boolean noCheckAlongPath(int row, int direction, TeamColor myTeamColor) {
        int col = 5 + direction;
        if (isInCheck(myTeamColor)) {
            return false;
        }
        chessBoard.addPiece(new ChessPosition(row, 5), null);
        while (col > 2 && col < 8) {
            chessBoard.addPiece(new ChessPosition(row, col), new ChessPiece(myTeamColor, PieceType.KING));
            if (isInCheck(myTeamColor)){
                chessBoard.addPiece(new ChessPosition(row, col), null);
                chessBoard.addPiece(new ChessPosition(row, 5), new ChessPiece(myTeamColor, PieceType.KING));
                return false;
            }
            chessBoard.addPiece(new ChessPosition(row, col), null);
            col += direction;
        }
        chessBoard.addPiece(new ChessPosition(row, 5), new ChessPiece(myTeamColor, PieceType.KING));
        return true;
    }

    /**Makes a move in a chess game
     * @param move chess move to preform
     * @throws InvalidMoveException if move is invalid */
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
        if (movingPiece.getPieceType() == PieceType.KING) {
            int colDiff = move.getStartPosition().getColumn() - move.getEndPosition().getColumn();
            if (colDiff == 2 || colDiff == -2) {
                makeMoveCastling(move, movingPiece);
                setCanEnPassant(false);
                setEnPassantCol(0);
                switchTurns();
                return;
            }
        }

        if (getCanEnPassant() &&
                movingPiece.getPieceType() == PieceType.PAWN &&
                move.getEndPosition().getColumn() == getEnPassantCol() &&
                chessBoard.getPiece(move.getEndPosition()) == null) {
            makeMoveEnPassant(move, movingPiece);
            setCanEnPassant(false);
            setEnPassantCol(0);
            switchTurns();
            return;
        }

        setCanEnPassant(false);
        setEnPassantCol(0);

        if (movingPiece.getPieceType() == PieceType.PAWN) {
            int endRow = move.getEndPosition().getRow();
            int rowDiff = move.getStartPosition().getRow() - endRow;
            if (rowDiff == 2 || rowDiff == -2) {
                int endCol = move.getEndPosition().getColumn();
                ChessPosition rightPawnPosition = new ChessPosition(endRow,endCol +1);
                ChessPosition leftPawnPosition = new ChessPosition(endRow,endCol -1);
                ChessPiece rightPawn = chessBoard.getPiece(rightPawnPosition);
                ChessPiece leftPawn = chessBoard.getPiece(leftPawnPosition);
                if (rightPawn != null &&
                        rightPawn.getPieceType() == PieceType.PAWN &&
                        rightPawn.getTeamColor() != movingPiece.getTeamColor()) {
                    setCanEnPassant(true);
                    setEnPassantCol(endCol);
                } else if (leftPawn != null &&
                        leftPawn.getPieceType() == PieceType.PAWN &&
                        leftPawn.getTeamColor() != movingPiece.getTeamColor()) {
                    setCanEnPassant(true);
                    setEnPassantCol(endCol);
                }

            }
        }

        chessBoard.addPiece(move.getStartPosition(), null);
        if (move.getPromotionPiece() != null) {
            chessBoard.addPiece(move.getEndPosition(), new ChessPiece(getTeamTurn(), move.getPromotionPiece()));
        } else {
            chessBoard.addPiece(move.getEndPosition(), movingPiece);
        }
        movingPiece.setNotHasMoved();
        switchTurns();
    }

    /**  */
    public void switchTurns() {
        if (getTeamTurn() == TeamColor.WHITE) {
            setTeamTurn(TeamColor.BLACK);
        } else {
            setTeamTurn(TeamColor.WHITE);
        }
    }

    /**
     * @param move
     * @param pawn */
    public void makeMoveEnPassant(ChessMove move, ChessPiece pawn){
        ChessPosition enemyPosition = new ChessPosition(move.getStartPosition().getRow(), move.getEndPosition().getColumn());
        chessBoard.addPiece(move.getStartPosition(), null);
        chessBoard.addPiece(enemyPosition, null);
        chessBoard.addPiece(move.getEndPosition(), pawn);
        pawn.setNotHasMoved();
        switchTurns();
    }

    /**If the king is castling, this function handles both his, and the rooks' movement
     * @param move the move that is being made
     * @param king the king ChessPiece that is castling */
    public void makeMoveCastling(ChessMove move, ChessPiece king) {
        int row = move.getEndPosition().getRow();
        int kingEndCol = move.getEndPosition().getColumn();
        chessBoard.addPiece(move.getStartPosition(), null);
        chessBoard.addPiece(move.getEndPosition(), king);
        king.setNotHasMoved();
        if (kingEndCol == 3) {
            ChessPosition rookPosition = new ChessPosition(row, 1);
            ChessPiece rookPiece = chessBoard.getPiece(rookPosition);
            chessBoard.addPiece(rookPosition, null);
            chessBoard.addPiece(new ChessPosition(row, 4), rookPiece);
            rookPiece.setNotHasMoved();
            return;
        }
        else if (kingEndCol == 7) {
            ChessPosition rookPosition = new ChessPosition(row, 8);
            ChessPiece rookPiece = chessBoard.getPiece(rookPosition);
            chessBoard.addPiece(rookPosition, null);
            chessBoard.addPiece(new ChessPosition(row, 6), rookPiece);
            rookPiece.setNotHasMoved();
            return;
        }
        throw new RuntimeException("Trouble placing rook while castling");

    }

    /**
     * @param threateningPiece The piece in the position that is being tested
     * @param position The position of the threatening position
     * @param kingPosition The position of the opposing King
     * @param teamColor The Color of the opposing King, threateningPiece must be this color
     * @return true if the threatening piece can attack the king */
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


    /**Determines if the given team is in check
     * @param teamColor which team to check for check
     * @return True if the specified team is in check */
    public boolean isInCheck(TeamColor teamColor) {
        ChessPosition kingPosition = null;
        for (int col = 1; col <= 8; col++) {
            for (int row = 1; row <= 8; row++) {
                ChessPosition position = new ChessPosition(row, col);
                ChessPiece piece = chessBoard.getPiece(position);
                if (piece == null){
                    continue;
                } else if (piece.getPieceType() == ChessPiece.pieceType.KING &&
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
     * @param teamColor The team color the piece must be at that location, else false
     * @param row The row position of the piece you are checking
     * @param col The col position of the piece you are checking
     * @return ture if there is a valid move for the piece at that position */
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
     * @param teamColor the team for which you are checking the valid move
     * @return True if there is any move a piece could make, else false */
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

    /**Determines if the given team is in checkmate
     * @param teamColor which team to check for checkmate
     * @return True if the specified team is in checkmate */
    public boolean isInCheckmate(TeamColor teamColor) {
        if (isInCheck(teamColor)) {
            return !anyValidMoves(teamColor);
        }
        return false;
    }

    /**Determines if the given team is in stalemate, which here is defined as having no valid moves
     * @param teamColor which team to check for stalemate
     * @return True if the specified team is in stalemate, otherwise false */
    public boolean isInStalemate(TeamColor teamColor) {
        if (!isInCheck(teamColor)) {
            return !anyValidMoves(teamColor);
        }
        return false;
    }

    /**Sets this game's chessboard with a given board
     * @param board the new board to use */
    public void setBoard(ChessBoard board) {
        chessBoard = board;
    }

    /**Gets the current chessboard
     * @return the chessboard */
    public static ChessBoard getBoard() {
        return chessBoard;
    }
}