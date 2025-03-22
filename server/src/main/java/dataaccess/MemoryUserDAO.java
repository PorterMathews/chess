package dataaccess;

import model.UserData;

import java.util.Collection;
import java.util.HashMap;

public class MemoryUserDAO implements UserDAO {
    private static final HashMap<String, UserData> USERS = new HashMap<>();

    /**
     * @return All users and their data
     */
    public Collection<UserData> getUsers() {
        return USERS.values();
    }

    /**
     * @param userName the name of the user you are looking up
     * @return true if the userName has been stored, else false
     */
    public boolean isUserInDB(String userName) {
        return USERS.containsKey(userName);
    }

    /**
     * @param u Data for user
     * @return the password for the user
     * @throws DataAccessException if the user doesn't exist
     */
    public boolean checkPassword(UserData u) throws DataAccessException {
        //System.out.println("Finding user...");
        UserData user = USERS.get(u.username());
        //System.out.println("user is: " + user);
        if (user == null) {
            throw new DataAccessException("Unauthorized");
        }
        return user.password().equals(u.password());
    }

    /**
     * @param userData the data for the user you are registering
     * @return The data for the user
     */
    public void registerUser(UserData userData) {
        //System.out.println("Register Mem");
        userData = new UserData(userData.username(), userData.password(), userData.email());
        USERS.put(userData.username(), userData);
    }

    /**
     * removes all data from the Users hashMap
     */
    public void clearUserData() {
        USERS.clear();
    }
}
