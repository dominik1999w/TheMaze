package util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public final class PlayerMicsMap {

    private final Map<UUID, Boolean> playerMics = new HashMap<>();

    public PlayerMicsMap() {
    }

    public void setMic(UUID playerID, boolean micActive) {
        playerMics.put(playerID, micActive);
    }

    public boolean getMic(UUID playerID) {
        return playerMics.get(playerID);
    }

    public Iterable<UUID> getActiveMics() {
        return playerMics.entrySet().stream()
                .filter(Map.Entry::getValue)
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
    }
}
