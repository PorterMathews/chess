package dataaccess;

import model.GameData;
import model.UserData;

import java.util.UUID;
import java.util.Collection;
import java.util.HashMap;

public class MemoryDataAccess implements DataAccess {
    private final HashMap<String, UserData> users = new HashMap<>();
    private final HashMap<String, String> authTokens = new HashMap<>();
    private final HashMap<Integer, GameData> gameInfo = new HashMap<>();

    public UserData register(UserData userData) {
        userData = new UserData(userData.username(), userData.password(), userData.email());
        users.put(userData.username(), userData);
        return userData;
    }

    public String generateAuthToken(String username) {
        String token = UUID.randomUUID().toString();
        authTokens.put(token, username);
        return token;
    }

    public void clearDatabase() {
        users.clear();
        authTokens.clear();
        gameInfo.clear();
    }

}
