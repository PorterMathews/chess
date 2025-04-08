package websocket.commands;

import chess.ChessMove;

public class MakeMoveCommand extends UserGameCommand{
    private final ChessMove move;

    public MakeMoveCommand(CommandType commandType, ChessMove move, String authToken, Integer gameID, boolean asObserver) {
        super(commandType, authToken, gameID, asObserver);
        this.move = move;
    }

    public ChessMove getMove() {
        return move;
    }
}
