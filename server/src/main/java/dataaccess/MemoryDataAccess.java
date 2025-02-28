package dataaccess;

import model.AuthData;
import model.GameData;
import model.UserData;

import java.util.*;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, String> authTokens = new HashMap<>();
    private final HashMap<Integer, GameData> gameInfo = new HashMap<>();

    public Collection<UserData> getUsers() {
        return users.values();
    }

    public Collection<AuthData> getAuth() {
        return authTokens.entrySet().stream()
                .map(entry -> new AuthData(entry.getKey(), entry.getValue()))
                .toList();
    }

    public String getAuthTokenByUsername(String username) {
        for (var authToken : authTokens.entrySet()) {
            if (authToken.getValue().equals(username)) {
                return authToken.getKey();
            }
        }
        return null;
    }

    public boolean authTokenExists(String AuthToken) {
        return authTokens.containsKey(AuthToken);
    }



    public UserData registerUser(UserData userData) {
        userData = new UserData(userData.username(), userData.password(), userData.email());
        users.put(userData.username(), userData);
        return userData;
    }

    public String generateAuthToken(String username) {
        String token = UUID.randomUUID().toString();
        authTokens.put(token, username);
        return token;
    }

    public void logout(String authToken) {
        authTokens.remove(authToken);
    }

    public void clearDatabase() {
        users.clear();
        authTokens.clear();
        gameInfo.clear();
    }

    public void clearUserData() {
        users.clear();
    }

    public void clearAuthData() {
        gameInfo.clear();
    }

    public void clearGameData() {
        authTokens.clear();
    }
}
