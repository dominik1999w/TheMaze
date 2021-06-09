package entity.player;

import java.util.ArrayList;
import java.util.List;

import util.Point2D;

import static util.Interpolation.interpolate;
import static util.Interpolation.interpolateRotation;

public class PlayerInterpolator {

    // TODO: circular buffer
    private final List<PlayerStateTimeStamp> playerStateHistory = new ArrayList<>();

    private final long serverDeltaMillis;

    public PlayerInterpolator(int serverUpdateRate) {
        this.serverDeltaMillis = (long)(1000.0f / serverUpdateRate);
    }

    // NOTE: using timestamp yields more recent snapshots of entities' states, but timestamp1 appears more smooth
    public void addState(long timestamp1, Player state) {
        long timestamp = System.currentTimeMillis();
        if (playerStateHistory.size() == 0 ||
                (playerStateHistory.get(playerStateHistory.size() - 1).timestamp != timestamp))
            playerStateHistory.add(new PlayerStateTimeStamp(state, timestamp));
    }

    public void computeCurrentState(Player out) {
        //StringBuilder log = new StringBuilder("computeCurrentState:\n");
        // insufficient data
        if (playerStateHistory.size() <= 1) return;

        long renderTimestamp = System.currentTimeMillis() - serverDeltaMillis;

        //log.append("    renderTimestamp=").append(renderTimestamp).append('\n');
        //log.append("    playerStateHistory:").append('\n');
        //for (PlayerStateTimeStamp playerStateTimeStamp : playerStateHistory) {
        //    log.append("        ").append(playerStateTimeStamp.timestamp).append(' ').append(playerStateTimeStamp.playerState.getPosition()).append('\n');
        //}

        int lastStateIndex = 0;
        while (lastStateIndex < playerStateHistory.size() &&
                playerStateHistory.get(lastStateIndex).timestamp <= renderTimestamp)
            lastStateIndex++;

        //log.append("    lastStateIndex=").append(lastStateIndex).append('\n');

        PlayerStateTimeStamp playerStateTimeStampA;
        PlayerStateTimeStamp playerStateTimeStampB;
        if (0 < lastStateIndex && lastStateIndex < playerStateHistory.size()) {
            // interpolating
            playerStateTimeStampA = playerStateHistory.get(lastStateIndex - 1);
            playerStateTimeStampB = playerStateHistory.get(lastStateIndex);
        } else if (lastStateIndex == playerStateHistory.size()) { // timestamp[last] <= renderTimestamp
            // extrapolating
            playerStateTimeStampA = playerStateHistory.get(lastStateIndex - 2);
            playerStateTimeStampB = playerStateHistory.get(lastStateIndex - 1);
        } else { // lastStateIndex == 0
            return;
        }
        long timestampA = playerStateTimeStampA.timestamp;
        long timestampB = playerStateTimeStampB.timestamp;
        Point2D positionA = playerStateTimeStampA.playerState.getPosition();
        Point2D positionB = playerStateTimeStampB.playerState.getPosition();
        float rotationA = playerStateTimeStampA.playerState.getRotation();
        float rotationB = playerStateTimeStampB.playerState.getRotation();
        float smoothFactor = ((float)(renderTimestamp - timestampA)) / ((float)(timestampB - timestampA));
        out.setPosition(interpolate(out.getPosition(), interpolate(positionA, positionB, smoothFactor), 0.25f));
        out.setRotation(interpolateRotation(out.getRotation(), interpolateRotation(rotationA, rotationB, smoothFactor), 0.25f));

        //log.append("    outPosition=").append(out.getPosition()).append('\n');

        while (lastStateIndex > 1) {
            playerStateHistory.remove(0);
            lastStateIndex--;
        }

        //log.append("    playerStateHistorySize=").append(playerStateHistory.size()).append('\n');
        //System.out.println(log);
    }

    private static class PlayerStateTimeStamp {
        private final Player playerState;
        private final long timestamp;
        private PlayerStateTimeStamp(Player playerState, long timestamp) {
            this.playerState = playerState;
            this.timestamp = timestamp;
        }

        @Override
        public String toString() {
            return "PlayerStateTimeStamp{" +
                    "playerState=" + playerState +
                    ", timestamp=" + timestamp +
                    '}';
        }
    }
}
