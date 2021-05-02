import entity.bullet.BulletController;
import entity.bullet.BulletHitbox;
import entity.player.PlayerHitbox;
import entity.player.controller.InputPlayerController;
import map.Map;
import map.generator.MapGenerator;
import physics.CollisionWorld;
import physics.mapcollision.MapCollisionDetector;
import server.GameServer;
import server.GrpcServer;
import service.GameService;
import service.MapService;
import time.Timer;
import types.WallType;
import world.World;

import static util.ServerConfig.SERVER_UPDATE_RATE;

public class Main {
    private static void loadClasses(Class<?>... classes) {
        try {
            for (Class<?> cls : classes) {
                System.out.println("Loaded " + cls.getName());
                Class.forName(cls.getName(), true, cls.getClassLoader());
                Class.forName(cls.getName() + "$Builder", true, cls.getClassLoader());
            }
        } catch (ClassNotFoundException ignore) {

        }
    }

    public static void main(String[] args) throws Exception {
        if (Integer.parseInt(System.getProperty("java.version").split("\\.")[0]) >= 9) {
            loadClasses(
                    lib.connection.ConnectReply.class,
                    lib.connection.LocalPlayerInput.class,
                    lib.connection.PlayerState.class,
                    lib.connection.ConnectReply.class,
                    lib.connection.GameStateRequest.class,
                    lib.connection.GameStateResponse.class,
                    lib.connection.BulletState.class,
                    lib.map.ConnectReply.class,
                    lib.map.StateRequest.class,
                    lib.map.StateResponse.class,
                    entity.player.Player.class,
                    entity.player.PlayerInput.class,
                    entity.player.PlayerConfig.class,
                    entity.player.PlayerHitbox.class,
                    entity.bullet.BulletConfig.class,
                    entity.bullet.Bullet.class,
                    entity.bullet.BulletHitbox.class,
                    util.Point2D.class,
                    util.Point2Di.class,
                    util.MathUtils.class,
                    physics.HitboxHistory.class,
                    physics.HitboxType.class,
                    physics.mapcollision.ClampMapCollisionDetector.class,
                    physics.mapcollision.IterativeMapCollisionDetector.class,
                    physics.mapcollision.LineMapCollisionDetector.class,
                    physics.mapcollision.MapCollisionDetector.class,
                    MapCollisionDetector.MapCollisionInfo.class,
                    types.WallType.class,
                    WallType.WallShape.class,
                    util.GRpcMapper.class
            );
        }

        MapGenerator mapGenerator = new MapGenerator(5);
        Map map = mapGenerator.generateMap(0);
        CollisionWorld collisionWorld = new CollisionWorld(map);

        World<InputPlayerController> world = new World<>(
                InputPlayerController::new,
                BulletController::new);
        world.subscribeOnPlayerAdded(newPlayer -> collisionWorld.addHitbox(new PlayerHitbox(newPlayer)));
        world.subscribeOnBulletAdded((player, newBullet) -> collisionWorld.addHitbox(new BulletHitbox(newBullet, world)));
        world.subscribeOnBulletRemoved(collisionWorld::removeHitbox);

        GameService gameService = new GameService(world);
        MapService mapService = new MapService();
        GameServer server = new GrpcServer(50051, gameService, mapService);
        server.start();

        new Thread(() -> Timer.executeAtFixedRate(delta ->
        {
            gameService.dispatchMessages((sequenceNumber, id, playerInput) ->
            {
                InputPlayerController playerController = world.getPlayerController(id);
                playerController.updateInput(playerInput);
                playerController.update();
                collisionWorld.update();
            });
            // TODO: rewrite: in world.update only bullets will be actually updated
            world.update(delta);
            collisionWorld.update();
            gameService.broadcastGameState();
        }, 1.0f / SERVER_UPDATE_RATE)).start();

        server.blockUntilShutdown();
    }
}
