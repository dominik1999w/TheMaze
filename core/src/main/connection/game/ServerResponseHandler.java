package connection.game;

import java.util.Collection;
import java.util.UUID;

import entity.player.Player;

public interface ServerResponseHandler {
    void onActivePlayers(Collection<UUID> playerIDs);
    void onPlayerState(long sequenceNumber, Player playerState);
}
