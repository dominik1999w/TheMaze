package util;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ClientsInputLog {

    private final Map<UUID, Long> lastProcessedInput = new ConcurrentHashMap<>();

    public void onInputProcessed(UUID playerID, long sequenceNumber) {
        Long oldSequenceNumber = lastProcessedInput.get(playerID);
        if (oldSequenceNumber == null || oldSequenceNumber < sequenceNumber)
            lastProcessedInput.put(playerID, sequenceNumber);
    }

    public long getLastProcessedInput(UUID playerID) {
        return lastProcessedInput.getOrDefault(playerID, 0L);
    }
}
