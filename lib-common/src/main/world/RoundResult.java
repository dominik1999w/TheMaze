package world;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RoundResult {
    private Map<UUID, Integer> points;

    private RoundResult() {
        points = new HashMap<>();
    }

    private RoundResult givePoints(UUID playerID, int val) {
        points.put(playerID, val);
        return this;
    }

    public static RoundResult missed(UUID playerID) {
        return new RoundResult()
                .givePoints(playerID, -1);
    }

    public static RoundResult killed(UUID shooterID, UUID killedID) {
        return new RoundResult()
                .givePoints(shooterID, 3)
                .givePoints(killedID, -1);
    }

    public static RoundResult notFired(UUID playerID, List<UUID> allPlayerIDs) {
        RoundResult result = new RoundResult();
        for(UUID player : allPlayerIDs) {
            result.givePoints(player, 1);
        }
        return result.givePoints(playerID, -1);
    }

    public Map<UUID, Integer> getPoints() {
        return points;
    }

    @Override
    public String toString() {
        StringBuilder s = new StringBuilder("-- Round results:\n");
        for(Map.Entry<UUID, Integer> entry : points.entrySet()) {
            s.append(entry.getKey()).append(" = ").append(entry.getValue()).append("\n");
        }
        return s.toString();
    }
}
