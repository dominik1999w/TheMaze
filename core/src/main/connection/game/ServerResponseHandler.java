package connection.game;

import entity.player.Player;

public interface ServerResponseHandler {
    void onPlayerState(long sequenceNumber, Player playerState);
}
