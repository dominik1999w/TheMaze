package connection.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import entity.player.PlayerInput;

public class PlayerInputLog {

    // TODO: circular buffer
    private final List<LogEntry> inputLog = new ArrayList<>();

    private long currentSequenceNumber = 0;

    public void log(PlayerInput playerInput) {
        inputLog.add(new LogEntry(currentSequenceNumber, playerInput));
        currentSequenceNumber++;

        //System.out.println("Non-acknowledged inputs: " + inputLog.size());
    }

    public Collection<PlayerInput> getInputLog() {
        //System.out.println(String.format(Locale.ENGLISH, "Get Log (%d,%d)", inputLog.size(), currentSequenceNumber));
        return inputLog.stream().map(logEntry -> logEntry.playerInput).collect(Collectors.toList());
    }

    public long getCurrentSequenceNumber() {
        return currentSequenceNumber;
    }

    public void discardLogUntil(long sequenceNumber) {
        //System.out.println("Discard until " + sequenceNumber);
        inputLog.removeIf(logEntry -> logEntry.sequenceNumber <= sequenceNumber);
    }

    private static class LogEntry {
        private final long sequenceNumber;
        private final PlayerInput playerInput;
        public LogEntry(long sequenceNumber, PlayerInput playerInput) {
            this.sequenceNumber = sequenceNumber;
            this.playerInput = playerInput;
        }
    }
}
