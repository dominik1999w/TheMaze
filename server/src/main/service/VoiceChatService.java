package service;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;

import java.io.IOException;

import co.uk.epucguru.classes.VoiceChatServer;

public class VoiceChatService {
    private static final int BUFFER_SIZE = 22050;

    private final int port;
    private final Server server;

    public VoiceChatService(int port) {
        this.port = port;
        this.server = new Server(BUFFER_SIZE, BUFFER_SIZE);
    }

    public void start() throws IOException {
        // TODO: consider only tcp connection
        server.bind(port, port);
        server.start();

        VoiceChatServer voiceServer = new VoiceChatServer(server.getKryo());
        server.addListener(new Listener() {
            @Override
            public void received(Connection connection, Object object) {
                voiceServer.relayVoice(connection, object, server);
            }
        });
    }
}
