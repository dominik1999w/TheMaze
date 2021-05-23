import lib.connection.BulletState;
import lib.connection.ConnectReply;
import lib.connection.ConnectRequest;
import lib.connection.GameStateRequest;
import lib.connection.GameStateResponse;
import lib.connection.LocalPlayerInput;
import lib.connection.PlayerState;
import lib.map.Position;
import lib.map.StateRequest;
import lib.map.StateResponse;
import map.Map;
import physics.mapcollision.MapCollisionDetector;
import server.GameServer;
import server.GrpcServer;
import service.GameService;
import service.MapService;
import types.WallType;
import util.StateHandler;

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
            BulletState.newBuilder();
            ConnectReply.newBuilder();
            ConnectRequest.newBuilder();
            GameStateRequest.newBuilder();
            GameStateResponse.newBuilder();
            LocalPlayerInput.newBuilder();
            PlayerState.newBuilder();
            Position.newBuilder();
            StateRequest.newBuilder();
            StateResponse.newBuilder();

            loadClasses(
                    connection.CallKey.class,
                    connection.PlayerIDInterceptor.class,
                    entity.bullet.Bullet.class,
                    entity.bullet.BulletConfig.class,
                    entity.bullet.BulletController.class,
                    entity.bullet.BulletHitbox.class,
                    entity.player.controller.GameAuthoritativeListener.class,
                    entity.player.controller.InputPlayerController.class,
                    entity.player.controller.PlayerController.class,
                    entity.player.Player.class,
                    entity.player.PlayerConfig.class,
                    entity.player.PlayerHitbox.class,
                    entity.player.PlayerInput.class,
                    entity.WorldEntity.class,
                    lib.connection.BulletState.class,
                    lib.connection.ConnectReply.class,
                    lib.connection.ConnectRequest.class,
                    lib.connection.GameStateRequest.class,
                    lib.connection.GameStateResponse.class,
                    lib.connection.LocalPlayerInput.class,
                    lib.connection.PlayerState.class,
                    lib.connection.TheMazeGrpcGdx.class,
                    lib.map.MapGrpcGdx.class,
                    lib.map.Position.class,
                    lib.map.StateRequest.class,
                    lib.map.StateResponse.class,
                    map.generator.MapGenerator.class,
                    map.Map.class,
                    Map.Node.class,
                    map.MapConfig.class,
                    physics.mapcollision.ClampMapCollisionDetector.class,
                    physics.mapcollision.IterativeMapCollisionDetector.class,
                    physics.mapcollision.LineMapCollisionDetector.class,
                    physics.mapcollision.MapCollisionDetector.class,
                    MapCollisionDetector.MapCollisionInfo.class,
                    physics.CollisionWorld.class,
                    physics.Hitbox.class,
                    physics.HitboxHistory.class,
                    physics.HitboxType.class,
                    types.WallType.class,
                    WallType.WallShape.class,
                    WallType.UP_WALL.getClass(),
                    WallType.DOWN_WALL.getClass(),
                    WallType.LEFT_WALL.getClass(),
                    WallType.RIGHT_WALL.getClass(),
                    util.GRpcMapper.class,
                    util.MathUtils.class,
                    util.Point2D.class,
                    util.Point2Di.class,
                    world.World.class
            );
        }

        GameService gameService = new GameService();

        MapService mapService = new MapService();

        GameServer server = new GrpcServer(50051, gameService, mapService);
        server.start();

        StateHandler stateHandler = new StateHandler(mapService, gameService);
        stateHandler.mainThread().start();

        server.blockUntilShutdown();
    }
}
