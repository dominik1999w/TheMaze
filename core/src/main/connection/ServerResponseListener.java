package connection;

import java.util.UUID;

import entity.player.Player;

public interface ServerResponseListener {
    void onSequenceNumber(long sequenceNumber, UUID playerID);
    void onPlayerState(Player playerState);
}
