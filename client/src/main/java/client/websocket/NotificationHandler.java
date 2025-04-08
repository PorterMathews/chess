package client.websocket;

import chess.ChessGame;
import websocket.messages.NotificationMessage;

public interface NotificationHandler {
    void notify(NotificationMessage notification);
    void loadGame(ChessGame game);
}
