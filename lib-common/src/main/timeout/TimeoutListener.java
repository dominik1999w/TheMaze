package timeout;

import java.util.UUID;

public interface TimeoutListener {
    void timedOut(UUID playerId);
}
