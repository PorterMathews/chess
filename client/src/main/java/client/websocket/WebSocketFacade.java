package client.websocket;

import static ui.EscapeSequences.*;
import chess.ChessMove;
import com.google.gson.Gson;
import exception.ResponseException;
import websocket.commands.MakeMoveCommand;
import websocket.commands.UserGameCommand;
import websocket.messages.*;

import javax.websocket.*;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;

public class WebSocketFacade extends Endpoint {
    private final boolean detailedErrorMsg = true;
    Session session;
    NotificationHandler notificationHandler;


    /**
     *
     * @param url wrl
     * @param notificationHandler handler
     * @throws ResponseException when things go bad
     */
    public WebSocketFacade(String url, NotificationHandler notificationHandler) throws ResponseException {
        try {
            url = url.replace("http", "ws");
            URI socketURI = new URI(url + "/ws");
            this.notificationHandler = notificationHandler;

            WebSocketContainer container = ContainerProvider.getWebSocketContainer();
            this.session = container.connectToServer(this, socketURI);

            session.addMessageHandler(new MessageHandler.Whole<String>() {
                @Override
                public void onMessage(String message) {
                    ServerMessage msg = new Gson().fromJson(message, ServerMessage.class);
                    switch (msg.getServerMessageType()) {
                        case LOAD_GAME -> {
                            LoadGameMessage gameMsg = new Gson().fromJson(message, LoadGameMessage.class);
                            notificationHandler.loadGame(gameMsg.getGame());
                        }
                        case NOTIFICATION -> {
                            NotificationMessage notify = new Gson().fromJson(message, NotificationMessage.class);
                            System.out.println(SET_TEXT_COLOR_LIGHT_GREY + notify.getMessage() + RESET_TEXT_COLOR);
                        }
                        case ERROR -> {
                            ErrorMessage error = new Gson().fromJson(message, ErrorMessage.class);
                            System.out.println(SET_TEXT_COLOR_RED + error.getErrorMessage() + RESET_TEXT_COLOR);
                        }
                    }
                }
            });

        } catch (DeploymentException | IOException | URISyntaxException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    /**
     * needed for some reason
     * @param session session
     * @param endpointConfig config
     */
    @Override
    public void onOpen(Session session, EndpointConfig endpointConfig) {
    }

    /**
     * closes a connection
     * @throws IOException
     */
    public void close() throws IOException {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    /**
     * helps peeps connect
     * @param authToken
     * @param gameID
     * @param asObserver whither or not you are an observer
     * @throws ResponseException
     */
    public void connectToGame(String authToken, int gameID, boolean asObserver) throws ResponseException {
        try {
            UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.CONNECT, authToken, gameID, asObserver);
            this.session.getBasicRemote().sendText(new Gson().toJson(connectCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    /**
     * help you make a move
     * @param authToken
     * @param gameID
     * @param asObserver
     * @param move the move you are trying to make
     * @throws ResponseException
     */
    public void makeMove(String authToken, int gameID, boolean asObserver, ChessMove move) throws ResponseException {
        try {
            MakeMoveCommand moveCommand = new MakeMoveCommand(UserGameCommand.CommandType.MAKE_MOVE, move, authToken, gameID, asObserver);
            this.session.getBasicRemote().sendText(new Gson().toJson(moveCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    /**
     * help you leave the game
     * @param authToken
     * @param gameID
     * @param asObserver
     * @throws ResponseException
     */
    public void leaveGame(String authToken, int gameID, boolean asObserver) throws ResponseException {
        try {
            UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.LEAVE, authToken, gameID, asObserver);
            this.session.getBasicRemote().sendText(new Gson().toJson(connectCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }


    /**
     * helps peeps resign
     * @param authToken
     * @param gameID
     * @param asObserver
     * @throws ResponseException
     */
    public void resign(String authToken, int gameID, boolean asObserver) throws ResponseException {
        try {
            UserGameCommand connectCommand = new UserGameCommand(UserGameCommand.CommandType.RESIGN, authToken, gameID, asObserver);
            this.session.getBasicRemote().sendText(new Gson().toJson(connectCommand));
        } catch (IOException ex) {
            throw new ResponseException(500, ex.getMessage());
        }
    }

    /**
     * Prints only if detailed messaging is on
     * @param input string to be printed
     */
    private void debug(String input) {
        if (detailedErrorMsg) {
            System.out.println(input);
        }
    }
}
