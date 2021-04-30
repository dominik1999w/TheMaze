package world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.BiConsumer;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

import entity.bullet.Bullet;
import entity.bullet.BulletController;
import entity.player.Player;
import entity.player.controller.PlayerController;
import map.MapConfig;
import util.Point2D;

public class World<TController extends PlayerController> {

    private final Map<UUID, TController> players = new ConcurrentHashMap<>();
    private final Map<Player, BulletController> bullets = new ConcurrentHashMap<>();

    private final List<Consumer<Player>> onPlayerAddedSubscribers = new ArrayList<>();
    private final List<Consumer<UUID>> onPlayerRemovedSubscribers = new ArrayList<>();
    private final List<BiConsumer<Player, Bullet>> onBulletAddedSubscribers = new ArrayList<>();
    private final List<Consumer<UUID>> onBulletRemovedSubscribers = new ArrayList<>();

    private final BiFunction<Player, World<?>, TController> controllerConstructor;
    private final Function<Bullet, BulletController> bulletControllerConstructor;

    public World(BiFunction<Player, World<?>, TController> playerControllerConstructor,
                 Function<Bullet, BulletController> bulletControllerConstructor) {
        this.controllerConstructor = playerControllerConstructor;
        this.bulletControllerConstructor = bulletControllerConstructor;
    }

    public void subscribeOnPlayerAdded(Consumer<Player> callback) {
        onPlayerAddedSubscribers.add(callback);
    }

    public void subscribeOnPlayerRemoved(Consumer<UUID> callback) {
        onPlayerRemovedSubscribers.add(callback);
    }

    public void subscribeOnBulletAdded(BiConsumer<Player, Bullet> callback) {
        onBulletAddedSubscribers.add(callback);
    }

    public void subscribeOnBulletRemoved(Consumer<UUID> callback) {
        onBulletRemovedSubscribers.add(callback);
    }

    public TController getPlayerController(UUID id) {
        return players.computeIfAbsent(id, k -> {
            Player player = new Player(id, new Point2D(3.5f * MapConfig.BOX_SIZE, 2.5f * MapConfig.BOX_SIZE));
            onPlayerAddedSubscribers.forEach(subscriber -> subscriber.accept(player));
            return controllerConstructor.apply(player, this);
        });
    }

    public void removePlayerController(UUID playerID) {
        onPlayerRemovedSubscribers.forEach(subscriber -> subscriber.accept(playerID));
        players.remove(playerID);
    }

    public BulletController getBulletController(Player player) {
        return bullets.get(player);
    }

    public void onBulletFired(Player player) {
        bullets.computeIfAbsent(player, p ->
        {
            Bullet bullet = new Bullet(p.getPosition(), p.getRotation());
            onBulletAddedSubscribers.forEach(subscriber -> subscriber.accept(player, bullet));
            return bulletControllerConstructor.apply(bullet);
        });
    }

    public void update(float delta) {
        players.values().forEach(playerController -> playerController.update(delta));
        bullets.values().forEach(bulletController -> bulletController.update(delta));
    }

    public Iterable<Map.Entry<UUID, TController>> getConnectedPlayers() {
        return players.entrySet();
    }

    public void onBulletDied(UUID bulletID) {
        onBulletRemovedSubscribers.forEach(subscriber -> subscriber.accept(bulletID));
        bullets.values().removeIf(bullet -> bullet.getBulletId().equals(bulletID));
    }
}
