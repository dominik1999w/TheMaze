package connection.voice;

import java.util.Collection;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;

import connection.Client;

public interface VoiceClient extends Client {
    interface ServerResponseHandler {
        void updateActivePlayerMics(Collection<UUID> activePlayerMics);
        void writeAudioSamples(Optional<short[]> audioSamples);
    }

    void syncState(float deltaTime, Function<Float, short[]> audioSampler);

    void dispatchMessages(ServerResponseHandler responseHandler);
}
