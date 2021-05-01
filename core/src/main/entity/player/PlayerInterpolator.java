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
        long renderTimestamp = System.currentTimeMillis() - serverDeltaMillis;

        int lastStateIndex = 0;
        while (lastStateIndex < playerStateHistory.size() &&
                playerStateHistory.get(lastStateIndex).timestamp <= renderTimestamp)
            lastStateIndex++;

        if (lastStateIndex < playerStateHistory.size()) {
            if (lastStateIndex > 0) {
                // now timestamp[lastStateIndex - 1] <= renderTimestamp <= timestamp[lastStateIndex]
                PlayerStateTimeStamp playerStateTimeStampA = playerStateHistory.get(lastStateIndex - 1);
                PlayerStateTimeStamp playerStateTimeStampB = playerStateHistory.get(lastStateIndex);
                long timestampA = playerStateTimeStampA.timestamp;
                long timestampB = playerStateTimeStampB.timestamp;
                Point2D positionA = playerStateTimeStampA.playerState.getPosition();
                Point2D positionB = playerStateTimeStampB.playerState.getPosition();
                float rotationA = playerStateTimeStampA.playerState.getRotation();
                float rotationB = playerStateTimeStampB.playerState.getRotation();
                float smoothFactor = ((float)(renderTimestamp - timestampA)) / ((float)(timestampB - timestampA));
                out.setPosition(interpolate(positionA, positionB, smoothFactor));
                out.setRotation(interpolate(rotationA, rotationB, smoothFactor));
            } else {
                //out.setPosition(playerStateHistory.get(0).playerState.getPosition());
                //out.setRotation(playerStateHistory.get(0).playerState.getRotation());
            }
        }

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
