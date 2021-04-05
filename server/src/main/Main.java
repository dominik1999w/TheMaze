import server.GameServer;
import server.GrpcServer;

public class Main {

    public static void main(String[] args) throws Exception {
        GameServer server = new GrpcServer(50051);
        server.start();
        server.blockUntilShutdown();
    }

}
