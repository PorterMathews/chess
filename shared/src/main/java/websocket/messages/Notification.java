package websocket.messages;

import com.google.gson.Gson;

//perhaps don't need the extras
public record Notification(Type type, String message) {
    public enum Type {
        ARRIVAL,
        NOISE,
        DEPARTURE,
        PLAYERJOIN,
        OBSERVERJOIN,
        MOVEMADE,
        PLAYERLEFT,
        OBSERVERLEFT,
        PLAYERRESGNED,
        CHECK,
        CHECKMATE
    }

    public String toString() {
        return new Gson().toJson(this);
    }
}
