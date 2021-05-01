package connection;

import entity.player.Player;

public interface ServerResponseListener {
    void onPlayerState(long sequenceNumber, Player playerState);
}
