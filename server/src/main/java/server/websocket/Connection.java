package server.websocket;

import org.eclipse.jetty.websocket.api.Session;

import java.io.IOException;
import java.util.Objects;

public class Connection {
    public String userName;
    public int gameID;
    public String role;
    public Session session;


    public Connection(String userName, int gameID, String role, Session session) {
        this.userName = userName;
        this.session = session;
        this.gameID = gameID;
        this.role = role;
    }

    public void send(String msg) throws IOException {
        session.getRemote().sendString(msg);
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        Connection that = (Connection) o;
        return gameID == that.gameID && Objects.equals(userName, that.userName) && Objects.equals(role, that.role) && Objects.equals(session, that.session);
    }

    @Override
    public int hashCode() {
        return Objects.hash(userName, gameID, role, session);
    }
}
