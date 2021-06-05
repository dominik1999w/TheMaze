package connection;

import com.esotericsoftware.kryonet.Client;

import connection.game.GameClient;
import connection.game.GrpcGameClient;
import connection.map.GrpcMapClient;
import connection.map.MapClient;
import connection.state.GrpcStateClient;
import connection.state.StateClient;
import connection.voice.ImplVoiceClient;
import connection.voice.VoiceClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;

public class ClientFactory {

    public static GameClient newGameClient(String host, int port) {
        ManagedChannel channel = getManagedChannel(host, port);
        return new GrpcGameClient(channel);
    }

    public static MapClient newMapClient(String host, int port) {
        ManagedChannel channel = getManagedChannel(host, port);
        return new GrpcMapClient(channel);
    }

    public static StateClient newStateClient(String host, int port) {
        ManagedChannel channel = getManagedChannel(host, port);
        return new GrpcStateClient(channel);
    }

    private static ManagedChannel getManagedChannel(String host, int port) {
        return ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .intercept(new PlayerIDInterceptor())
                .build();
    }

    public static VoiceClient newVoiceClient(String host, int port) {
        return new ImplVoiceClient(host, port);
    }
}
