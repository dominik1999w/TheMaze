package world;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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

    private UUID bulletOwner;
    private BulletController bulletController;

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

    public void removeBulletController() {
        UUID bulletID = bulletController.getBullet().getId();
        bulletController = null;
        onBulletRemovedSubscribers.forEach(subscriber -> subscriber.accept(bulletID));
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
        bulletOwner = playerIdsList.get(random.nextInt(playerIdsList.size()));
        System.out.println(bulletOwner + " has the bullet!");
    }

    public void onBulletFired(UUID shooterID, Bullet bullet) {
        if(bulletController == null) {
            bulletController = bulletControllerConstructor.apply(shooterID, bullet);
            onBulletAddedSubscribers.forEach(subscriber -> subscriber.accept(bullet));
        }
    }

    public void onBulletFired(Player player) {
        if(!player.getId().equals(bulletOwner)) return;
        if(bulletController == null) {
            Point2D bulletPosition = new Point2D(player.getPosition())
                    .add(BulletConfig.textureDependentShift(player.getRotation()));
            Bullet bullet = new Bullet(bulletPosition, player.getRotation());
            bulletOwner = null;
            bulletController = bulletControllerConstructor.apply(player.getId(), bullet);
            onBulletAddedSubscribers.forEach(subscriber -> subscriber.accept(bullet));
        }
    }

    public void onBulletDied(UUID bulletID) {
        onBulletRemovedSubscribers.forEach(subscriber -> subscriber.accept(bulletID));
        if(bulletID.equals(bulletController.getBullet().getId())) {
            UUID shooter = bulletController.getPlayerID();
            bulletOwner = null;
            bulletController = null;
            onRoundResultSubscribers.forEach(subscriber -> subscriber.accept(new RoundResult(shooter)));
        }
    }

    public void update(float delta) {
        players.values().forEach(PlayerController::update);
        if(bulletController != null) {
            bulletController.update(delta);
        }
    }

    public Iterable<Map.Entry<UUID, TController>> getConnectedPlayers() {
        return players.entrySet();
    }

    public BulletController getBulletController() {
        return bulletController;
    }
}
