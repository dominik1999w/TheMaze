import entity.bullet.BulletConfig;
import entity.bullet.BulletController;
import entity.player.Player;
import entity.player.controller.AuthoritativePlayerController;
import lib.connection.BulletState;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import server.GameServer;
import server.GrpcServer;
import service.GameService;
import time.Timer;
import util.Point2D;
import world.World;

public class Main {

    public static void main(String[] args) throws Exception {
        // openjdk hack
        if (System.getProperty("java.runtime.name").startsWith("OpenJDK")) {
            ConnectReply.newBuilder();
            ConnectRequest.newBuilder();
            GameStateRequest.newBuilder();
            GameStateResponse.newBuilder();
            PlayerState.newBuilder();
            BulletState.newBuilder();
            new Player();
            new Point2D();
            new BulletConfig();
        }


        World<AuthoritativePlayerController> world = new World<>(
                AuthoritativePlayerController::new,
                BulletController::new);
        GameService gameService = new GameService(world);
        GameServer server = new GrpcServer(50051, gameService);
        server.start();

        new Thread(() -> Timer.executeAtFixedRate(world::update, 0.025f)).start(); // 40 fps

        // start new Thread with
        // clientResponseObservers.forEach(::onNext(GameStateResponse))
        // every <x> seconds

        server.blockUntilShutdown();
    }

}
