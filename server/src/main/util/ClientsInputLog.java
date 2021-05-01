package util;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class ClientsInputLog {

    private final Map<String, Long> lastProcessedInput = new ConcurrentHashMap<>();

    public void onInputProcessed(String playerID, long sequenceNumber) {
        Long oldSequenceNumber = lastProcessedInput.get(playerID);
        if (oldSequenceNumber == null || oldSequenceNumber < sequenceNumber)
            lastProcessedInput.put(playerID, sequenceNumber);
    }

    public long getLastProcessedInput(String playerID) {
        return lastProcessedInput.getOrDefault(playerID, 0L);
    }
}
