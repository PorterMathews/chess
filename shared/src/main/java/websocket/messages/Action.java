package websocket.messages;

import com.google.gson.Gson;

public record Action(Type type, String userName, String playerColor) {
    public enum Type {
        ENTER,
        EXIT,
        DEPARTURE,
        PLAYERJOIN,
        OBSERVERJOIN,
        MOVEMADE,
        PLAYERLEFT,
        OBSERVERLEFT,
        PLAYERRESIGNED,
        CHECK,
        CHECKMATE
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
