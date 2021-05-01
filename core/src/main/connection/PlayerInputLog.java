package connection;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import entity.player.GameInputListener;
import entity.player.PlayerInput;
import entity.player.controller.InputPlayerController;

public class PlayerInputLog {

    // NOTE: maybe there is better data structure for our needs?
    private final List<LogEntry> inputLog = new ArrayList<>();

    private long currentSequenceNumber = 0;

    public void log(PlayerInput playerInput) {
        inputLog.add(new LogEntry(currentSequenceNumber, playerInput));
        currentSequenceNumber++;

        System.out.println("Non-acknowledged inputs: " + inputLog.size());
    }

    public Collection<PlayerInput> getInputLog() {
        return inputLog.stream().map(logEntry -> logEntry.playerInput).collect(Collectors.toList());
    }

    public long getCurrentSequenceNumber() {
        return currentSequenceNumber;
    }

    public void discardLogUntil(long sequenceNumber) {
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
