package service;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import connection.voice.VoiceChatServer;

public class VoiceChatService {
    private static final int BUFFER_SIZE = 22050;

    private final int port;
    private final Server server;

    public VoiceChatService(int port) {
        this.port = port;
        this.server = new Server(BUFFER_SIZE, BUFFER_SIZE);
    }

    public void start() throws IOException {
        server.start();
        server.bind(port, port);

        VoiceChatServer voiceServer = new VoiceChatServer(server.getKryo());
        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                voiceServer.relayVoice(connection, object, server);
            }
        });
    }
}
