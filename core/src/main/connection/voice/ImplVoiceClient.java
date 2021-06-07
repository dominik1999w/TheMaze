package connection.voice;

import com.esotericsoftware.kryonet.Client;

import java.io.IOException;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;

import connection.VoiceNetData;

public class ImplVoiceClient implements VoiceClient {
    private static final int BUFFER_SIZE = 22050;

    private UUID id;
    private final Client client;
    private final String host;
    private final int port;
    private VoiceChatClient sender;

    private final Set<VoiceNetData> voiceSamplesSet = Collections.synchronizedSet(new HashSet<>());

    public ImplVoiceClient(String host, int port) {
        this.host = host;
        this.port = port;
        this.client = new Client(BUFFER_SIZE, BUFFER_SIZE);
    }

    @Override
    public void connect(UUID id) {
        this.id = id;
        try {
            //Log.set(0);
            client.start();
            client.connect(5000, host, port, port);
            this.sender = new VoiceChatClient(id, client.getKryo());
            sender.addReceiver(client, voiceSamples -> {
                System.out.println(Thread.currentThread());
                voiceSamplesSet.add(voiceSamples);
            });
        } catch (IOException e) {
            System.err.println("Failed to connect to VoiceChat");
            e.printStackTrace();
        }
    }

    @Override
    public void syncState(float deltaTime, Function<Float, short[]> audioSampler) {
        sender.sendVoice(client, deltaTime, audioSampler);
    }

    @Override
    public void dispatchMessages(ServerResponseHandler responseHandler) {
        synchronized (voiceSamplesSet) {
            responseHandler.updateActivePlayerMics(
                    voiceSamplesSet.stream()
                            .map(VoiceNetData::getPlayerID)
                            .collect(Collectors.toSet())
            );
            responseHandler.writeAudioSamples(
                    voiceSamplesSet.stream()
                            .filter(playerVoiceSamples -> !playerVoiceSamples.getPlayerID().equals(id))
                            .map(VoiceNetData::getSamples)
                            .reduce((samples1, samples2) -> {
                                for (int i = 0; i < samples1.length; i++) {
                                    samples1[i] += samples2[i];
                                }
                                return samples1;
                            })
            );
            voiceSamplesSet.clear();
        }
    }

    @Override
    public void disconnect() {
        try {
            client.dispose();
        } catch (IOException e) {
            System.err.println("Failed to disconnect from VoiceChat");
            e.printStackTrace();
        }
    }
}
