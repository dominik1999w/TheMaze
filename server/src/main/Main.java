import entity.bullet.BulletConfig;
import entity.bullet.BulletController;
import entity.player.Player;
import entity.player.PlayerHitbox;
import entity.player.controller.AuthoritativePlayerController;
import lib.connection.BulletState;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.PlayerState;
import map.Map;
import map.generator.MapGenerator;
import physics.CollisionWorld;
import server.GameServer;
import server.GrpcServer;
import service.GameService;
import service.MapService;
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

        MapGenerator mapGenerator = new MapGenerator(50);
        Map map = mapGenerator.generateMap(GameService.SEED);
        CollisionWorld collisionWorld = new CollisionWorld(map);

        World<AuthoritativePlayerController> world = new World<>(
                AuthoritativePlayerController::new,
                BulletController::new);
        world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addHitbox(new PlayerHitbox(newPlayer)));

        GameService gameService = new GameService(world);
        MapService mapService = new MapService();
        GameServer server = new GrpcServer(50051, gameService, mapService);
        server.start();

        new Thread(() -> Timer.executeAtFixedRate(delta ->
        {
            world.update(delta);
            collisionWorld.update();
        }, 0.025f)).start(); // 40 fps

        // start new Thread with
        // clientResponseObservers.forEach(::onNext(GameStateResponse))
        // every <x> seconds

        server.blockUntilShutdown();
    }

}
