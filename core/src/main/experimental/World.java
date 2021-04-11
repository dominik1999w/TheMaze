package experimental;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import player.RemotePlayer;

public class World {

    private final Map<String, RemotePlayer> players = new ConcurrentHashMap<>();

    private final List<OnPlayerAdded> onPlayerAddedSubscribers = new ArrayList<>();

    public World() {

    }

    public void subscribe(OnPlayerAdded callback) {
        onPlayerAddedSubscribers.add(callback);
    }

    public RemotePlayer getPlayer(String id) {
        return players.computeIfAbsent(id, k -> {
            RemotePlayer player = new RemotePlayer();
            onPlayerAddedSubscribers.forEach(subscriber -> subscriber.onPlayerAdded(player));
            return player;
        });
    }

    @FunctionalInterface
    public interface OnPlayerAdded {
        void onPlayerAdded(RemotePlayer player);
    }
}
