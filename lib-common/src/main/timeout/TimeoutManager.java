package timeout;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class TimeoutManager {
    TimeoutListener listener;
    Map<UUID, Long> map;
    long threshold;

    public TimeoutManager(TimeoutListener listener, long thresholdMillis) {
        this.listener = listener;
        map = new ConcurrentHashMap<>();
        threshold = thresholdMillis;
    }

    public void notify(UUID playerId) {
        long currentTime = System.currentTimeMillis();
        map.put(playerId, currentTime);
        for(Map.Entry<UUID, Long> entry : map.entrySet()) {
            if(currentTime - entry.getValue() > threshold) {
                map.remove(entry.getKey());
                listener.timedOut(entry.getKey());
            }
        }
    }
}
