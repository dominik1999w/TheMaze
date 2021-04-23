
import entity.bullet.BulletController;
import entity.player.controller.AuthoritativePlayerController;
import physics.mapcollision.IterativeMapCollisionDetector;
import physics.mapcollision.MapCollisionDetector;
import server.GameServer;
import server.GrpcServer;
import service.GameService;
import time.Timer;
import world.World;

public class Main {

    public static void main(String[] args) throws Exception {
        // openjdk hack
//        ConnectReply.newBuilder();
//        ConnectRequest.newBuilder();
//        GameStateRequest.newBuilder();
//        GameStateResponse.newBuilder();
//        PlayerState.newBuilder();
//        new Player();


        MapCollisionDetector mapCollisionDetector = new IterativeMapCollisionDetector(null);
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
