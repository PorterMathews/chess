import dataaccess.DataAccess;
import dataaccess.MemoryDataAccess;
import dataaccess.MySqlDataAccess;
import server.Server;

public class Main {

    //start on port 8080
    public static void main(String[] args) {
        try {

            DataAccess dataAccess = new MemoryDataAccess();
            if (args.length >= 2 && args[1].equals("sql")) {
                dataAccess = new MySqlDataAccess();
            }

            var server = new Server();
            int port = server.run(8080);
            System.out.printf("Server started on port %d%n", port);

            System.out.printf("Server started on port %d with %s%n", port, dataAccess.getClass());
            return;
        } catch (Throwable ex) {
            System.out.printf("Unable to start server: %s%n", ex.getMessage());
        }
        System.out.println("""
                Server:
                java ServerMain <port> [sql]
                """);
    }
}