package client.websocket;

import chess.ChessGame;
import websocket.messages.Notification;

public interface NotificationHandler {
    void notify(Notification notification);
    void loadGame(ChessGame game);
}
