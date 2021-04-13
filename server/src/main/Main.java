import server.GameServer;
import server.GrpcServer;

public class Main {

    public static void main(String[] args) throws Exception {
        // openjdk hack
//        ConnectReply.newBuilder();
//        ConnectRequest.newBuilder();
//        GameStateRequest.newBuilder();
//        GameStateResponse.newBuilder();
//        PlayerState.newBuilder();
//        new Player();
        GameServer server = new GrpcServer(50051);
        server.start();
        server.blockUntilShutdown();
    }

}
