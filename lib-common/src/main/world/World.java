package world;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import player.Player;

public class World {

    private final Map<String, Player> players = new ConcurrentHashMap<>();

    private final List<OnPlayerAdded> onPlayerAddedSubscribers = new ArrayList<>();

    public World() {

    }

    public void subscribe(OnPlayerAdded callback) {
        onPlayerAddedSubscribers.add(callback);
    }

    public Player getPlayer(String id) {
        return players.computeIfAbsent(id, k -> {
            Player player = new Player();
            onPlayerAddedSubscribers.forEach(subscriber -> subscriber.onPlayerAdded(player));
            return player;
        });
    }

    @FunctionalInterface
    public interface OnPlayerAdded {
        void onPlayerAdded(Player player);
    }
}
