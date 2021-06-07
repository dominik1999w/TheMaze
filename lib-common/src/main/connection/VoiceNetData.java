package connection;

import java.util.Objects;
import java.util.UUID;

public class VoiceNetData {

	private UUID playerID;
	private short[] samples;

	public VoiceNetData() {

	}

	public VoiceNetData(UUID playerID, short[] samples) {
		this.playerID = playerID;
		this.samples = samples;
	}

	public UUID getPlayerID() {
		return playerID;
	}

	public short[] getSamples() {
		return samples;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;
		if (o == null || getClass() != o.getClass()) return false;
		VoiceNetData that = (VoiceNetData) o;
		return playerID.equals(that.playerID);
	}

	@Override
	public int hashCode() {
		return Objects.hash(playerID);
	}
}
