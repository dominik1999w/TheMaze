package connection;

import java.util.ArrayList;
import java.util.List;

import entity.player.PlayerInput;
import entity.player.controller.InputPlayerController;

public class ServerReconciler {

    // NOTE: maybe there is better data structure for our needs?
    private final List<LogEntry> inputLog = new ArrayList<>();

    private long currentSequenceNumber = 0;

    public void log(PlayerInput playerInput) {
        inputLog.add(new LogEntry(currentSequenceNumber, playerInput));
        currentSequenceNumber++;

        System.out.println("Non-acknowledged inputs: " + inputLog.size());
    }

    public void reconcile(InputPlayerController playerController) {
        for (LogEntry logEntry : inputLog) {
            playerController.notifyInput(logEntry.playerInput);
        }
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
