package entity.player;

import java.util.ArrayList;
import java.util.List;

import util.Point2D;

import static util.Interpolation.interpolate;

public class PlayerInterpolator {

    private final List<PlayerStateTimeStamp> playerStateHistory = new ArrayList<>();

    private final long serverDeltaMillis;

    public PlayerInterpolator(int serverUpdateRate) {
        this.serverDeltaMillis = (long)(1000.0f / serverUpdateRate);
    }

    public void addState(Player state) {
        playerStateHistory.add(new PlayerStateTimeStamp(state, System.currentTimeMillis()));
    }

    public void computeCurrentState(Player out) {
        // insufficient data
        if (playerStateHistory.size() <= 1) return;

        long renderTimestamp = System.currentTimeMillis() - serverDeltaMillis;

        int lastStateIndex = 0;
        while (lastStateIndex < playerStateHistory.size() &&
                playerStateHistory.get(lastStateIndex).timestamp <= renderTimestamp)
            lastStateIndex++;

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
        out.setPosition(interpolate(positionA, positionB, smoothFactor));
        out.setRotation(interpolate(rotationA, rotationB, smoothFactor));

        System.out.println(renderTimestamp + " " + out.getPosition());

        while (lastStateIndex > 1) {
            playerStateHistory.remove(0);
            lastStateIndex--;
        }
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
