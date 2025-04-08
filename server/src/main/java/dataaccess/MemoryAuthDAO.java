package dataaccess;

import model.AuthData;
import java.util.HashMap;
import java.util.UUID;

public class MemoryAuthDAO implements AuthDAO {
    private static final HashMap<String, String> AUTH_TOKENS = new HashMap<>();

    /**
     * @param authToken AuthToken
     * @return AuthData for user with authToken
     */
    public AuthData getAuthDataByAuthToken(String authToken) {
        String username = AUTH_TOKENS.get(authToken);
        return (username != null) ? new AuthData(authToken, username) : null;
    }

    /**
     * @param authToken AuthToke you are checking for
     * @return true if the authToken exists
     */
    public boolean authTokenExists(String authToken) {
        return AUTH_TOKENS.containsKey(authToken);
    }

    /**
     * Generates authToken and stores it for later look up
     * @param username the username to be associate with the authToken
     * @return The taken that was generated
     */
    public String generateAuthToken(String username) {
        String token = UUID.randomUUID().toString();
        AUTH_TOKENS.put(token, username);
        return token;
    }

    /**
     * Removes authToken for hashMap storage
     * @param authToken The auth token to be removed
     */
    public void logout(String authToken) {
        AUTH_TOKENS.remove(authToken);
    }

    /**
     * removes all data from the authTokens hashMap
     */
    public void clearAuthData() {
        AUTH_TOKENS.clear();
    }
}
