package connection.voice;

import connection.Client;

public interface VoiceClient extends Client {
    void syncState(float deltaTime);
}
