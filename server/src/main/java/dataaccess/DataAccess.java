package dataaccess;

import model.*;

public interface DataAccess {
    UserData register(UserData u) throws RuntimeException;
    void clearDatabase() throws RuntimeException;
}
