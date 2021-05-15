import physics.mapcollision.MapCollisionDetector;
import server.GameServer;
import server.GrpcServer;
import service.GameService;
import service.MapService;
import types.WallType;
import util.GameStateHandler;

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

        GameService gameService = new GameService();

        MapService mapService = new MapService(gameService);

        GameServer server = new GrpcServer(50051, gameService, mapService);
        server.start();

        GameStateHandler stateHandler = new GameStateHandler(gameService);
        stateHandler.gameThread().start();


        server.blockUntilShutdown();
    }
}
