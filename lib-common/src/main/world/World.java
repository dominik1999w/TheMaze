package world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Consumer;

import entity.bullet.Bullet;
import entity.bullet.BulletConfig;
import entity.bullet.BulletController;
import entity.player.Player;
import entity.player.controller.PlayerController;
import map.MapConfig;
import util.Point2D;

public class World<TController extends PlayerController> {

    private final Map<UUID, TController> players = new HashMap<>(); //new ConcurrentHashMap<>();

    private final CachedBullet cachedBullet = new CachedBullet();

    private final List<Consumer<Player>> onPlayerAddedSubscribers = new ArrayList<>();
    private final List<Consumer<UUID>> onPlayerRemovedSubscribers = new ArrayList<>();
    private final List<Consumer<Bullet>> onBulletAddedSubscribers = new ArrayList<>();
    private final List<Consumer<UUID>> onBulletRemovedSubscribers = new ArrayList<>();
    private final List<Consumer<RoundResult>> onRoundResultSubscribers = new ArrayList<>();

    private final BiFunction<Player, World<?>, TController> controllerConstructor;
    private final BiFunction<UUID, Bullet, BulletController> bulletControllerConstructor;

    private final Random random;

    public World(BiFunction<Player, World<?>, TController> playerControllerConstructor,
                 BiFunction<UUID, Bullet, BulletController> bulletControllerConstructor) {
        this.controllerConstructor = playerControllerConstructor;
        this.bulletControllerConstructor = bulletControllerConstructor;
        this.random = new Random();
    }

    public void subscribeOnPlayerAdded(Consumer<Player> callback) {
        onPlayerAddedSubscribers.add(callback);
    }

    public void subscribeOnPlayerRemoved(Consumer<UUID> callback) {
        onPlayerRemovedSubscribers.add(callback);
    }

    public void subscribeOnBulletAdded(Consumer<Bullet> callback) {
        onBulletAddedSubscribers.add(callback);
    }

    public void subscribeOnBulletRemoved(Consumer<UUID> callback) {
        onBulletRemovedSubscribers.add(callback);
    }

    public void subscribeOnRoundResult(Consumer<RoundResult> callback) {
        onRoundResultSubscribers.add(callback);
    }

    public void onPlayerJoined(UUID id) {
        // TODO: check if has already been in map
        getPlayerController(id);
    }

    public void removePlayerController(UUID playerID) {
        onPlayerRemovedSubscribers.forEach(subscriber -> subscriber.accept(playerID));
        players.remove(playerID);
    }

    public TController getPlayerController(UUID playerID, Point2D position) {
        return players.computeIfAbsent(playerID, k ->
        {
            Player player = new Player(playerID, position);
            onPlayerAddedSubscribers.forEach(subscriber -> subscriber.accept(player));
            return controllerConstructor.apply(player, this);
        });
    }

    public TController getPlayerController(UUID playerID) {
        return getPlayerController(playerID, new Point2D(2.5f * MapConfig.BOX_SIZE, 3.5f * MapConfig.BOX_SIZE));
    }

    public void assignBulletRandomly() {
        List<UUID> playerIdsList = new ArrayList<>(players.keySet());
        cachedBullet.passTo(playerIdsList.get(random.nextInt(playerIdsList.size())));
    }

    public void onBulletFired(UUID shooterID, Bullet bullet) {
        if (!cachedBullet.enabled()) {
            cachedBullet.passTo(shooterID);
            cachedBullet.enable(bulletControllerConstructor.apply(shooterID, bullet));
            onBulletAddedSubscribers.forEach(subscriber -> subscriber.accept(bullet));
        }
    }

    public void onBulletFired(Player player) {
        if (!cachedBullet.enabled() && player.getId().equals(cachedBullet.getShooterID())) {
            Point2D bulletPosition = new Point2D(player.getPosition())
                    .add(BulletConfig.textureDependentShift(player.getRotation()));
            Bullet bullet = new Bullet(bulletPosition, player.getRotation());
            cachedBullet.passTo(player.getId());
            cachedBullet.enable(bulletControllerConstructor.apply(player.getId(), bullet));
            onBulletAddedSubscribers.forEach(subscriber -> subscriber.accept(bullet));
        }
    }

    public void onBulletDied() {
        UUID bulletID = cachedBullet.getID();
        onBulletRemovedSubscribers.forEach(subscriber -> subscriber.accept(bulletID));

        UUID shooterID = cachedBullet.getShooterID();
        cachedBullet.disable();
        onRoundResultSubscribers.forEach(subscriber -> subscriber.accept(new RoundResult(shooterID)));
    }

    public void update(float delta) {
        players.values().forEach(PlayerController::update);
        if (cachedBullet.enabled()) {
            cachedBullet.getController().update(delta);
        }
    }

    public Iterable<Map.Entry<UUID, TController>> getConnectedPlayers() {
        return players.entrySet();
    }

    public Optional<CachedBullet> getBullet() {
        return cachedBullet.enabled() ? Optional.of(cachedBullet) : Optional.empty();
    }
}
