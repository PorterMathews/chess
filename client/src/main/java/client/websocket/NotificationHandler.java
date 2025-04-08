package client.websocket;

import chess.ChessGame;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void loadGame(ChessGame game);
}
