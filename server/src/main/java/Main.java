import server.Server;

public class Main {

    //start on port 8080
    public static void main(String[] args) {
        try {
            var server = new Server();
            int port = server.run(8080);
            System.out.printf("Server started on port %d%n", port);
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