package server;

/**
 * Creates a join Request
 */
public class JoinGameRequest {
    private String playerColor;
    private Integer gameID;
    private Boolean removePlayer;

    public JoinGameRequest(String playerColor, Integer gameID) {
        this.playerColor = playerColor;
        this.gameID = gameID;
        this.removePlayer = false;
    }

    public JoinGameRequest(String playerColor, Integer gameID, Boolean removePlayer) {
        this.playerColor = playerColor;
        this.gameID = gameID;
        this.removePlayer = removePlayer;
    }

    public String getPlayerColor() { return playerColor; }
    public Integer getGameID() { return gameID; }
    public Boolean getRemovePlayer() { return removePlayer != null && removePlayer; }
}
