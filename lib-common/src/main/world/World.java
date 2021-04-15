package world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;
import java.util.function.Function;

import entity.bullet.Bullet;
import entity.bullet.BulletController;
import entity.player.Player;
import entity.player.controller.PlayerController;
import util.Point2D;

public class World<TController extends PlayerController> {

    private final Map<String, TController> players = new ConcurrentHashMap<>();
    private final Map<Player, BulletController> bullets = new ConcurrentHashMap<>();

    private final List<Consumer<Player>> onPlayerAddedSubscribers = new ArrayList<>();
    private final List<Consumer<Bullet>> onBulletAddedSubscribers = new ArrayList<>();
    private final List<Consumer<Bullet>> onBulletRemovedSubscribers = new ArrayList<>();

    private final map.Map map;
    private final Function<Player, TController> controllerConstructor;

    public World(map.Map map, Function<Player, TController> controllerConstructor) {
        this.map = map;
        this.controllerConstructor = controllerConstructor;
    }

    public void subscribeOnPlayerAdded(Consumer<Player> callback) {
        onPlayerAddedSubscribers.add(callback);
    }

    public void subscribeOnBulletAdded(Consumer<Bullet> callback) {
        onBulletAddedSubscribers.add(callback);
    }

    public void subscribeOnBulletRemoved(Consumer<Bullet> callback) {
        onBulletRemovedSubscribers.add(callback);
    }

    public TController getPlayerController(String id) {
        return players.computeIfAbsent(id, k -> {
            Player player = new Player(new Point2D(3, 2));
            onPlayerAddedSubscribers.forEach(subscriber -> subscriber.accept(player));
            return controllerConstructor.apply(player);
        });
    }

    public void onBulletFired(Player player) {
        bullets.computeIfAbsent(player, p ->
        {
            Bullet bullet = new Bullet(p.getPosition(), p.getRotation());
            onBulletAddedSubscribers.forEach(subscriber -> subscriber.accept(bullet));
            return new BulletController(bullet, map);
        });
    }

    public void update(float delta) {
        players.values().forEach(playerController -> playerController.update(delta));
        bullets.forEach((player, bulletController) ->
        {
            if (!bulletController.update(delta)) {
                onBulletRemovedSubscribers.forEach(subscriber -> subscriber.accept(bulletController.getBullet()));
                bullets.remove(player);
            }
        });
    }

    public Iterable<Map.Entry<String, TController>> getConnectedPlayers() {
        return players.entrySet();
    }
}
