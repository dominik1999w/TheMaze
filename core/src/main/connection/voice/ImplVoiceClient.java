package connection.voice;

import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.util.UUID;

import co.uk.epucguru.classes.VoiceChatClient;

public class ImplVoiceClient implements VoiceClient {
    private static final int BUFFER_SIZE = 22050;

    private final Client client;
    private final String host;
    private final int port;
    private VoiceChatClient sender;

    public ImplVoiceClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.client = new Client(BUFFER_SIZE, BUFFER_SIZE);
    }

    @Override
    public void connect(UUID id) {
        try {
            client.start();
            client.connect(5000, host, port, port);
            this.sender = new VoiceChatClient(client.getKryo());
            sender.addReceiver(client);
        } catch (IOException e) {
            System.err.println("Failed to connect to VoiceChat");
            e.printStackTrace();
        }
    }

    @Override
    public void syncState(float deltaTime) {
        sender.sendVoice(client, deltaTime);
    }

    @Override
    public void disconnect() {
        try {
            client.dispose();
        } catch (IOException e) {
            System.err.println("Failed to disconnect from VoiceChat");
        }
    }
}
