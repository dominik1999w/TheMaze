import server.GameServer;
import server.GrpcServer;

public class Main {

    public static void main(String[] args) throws Exception {
        GameServer server = new GrpcServer();
        server.start(50051);
        server.blockUntilShutdown();
    }

}
