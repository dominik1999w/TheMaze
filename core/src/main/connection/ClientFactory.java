package connection;

import com.google.protobuf.Empty;

import java.util.Locale;
import java.util.logging.Logger;

import connection.game.GameClient;
import connection.game.GrpcGameClient;
import connection.game.NoOpGameClient;
import connection.map.GrpcMapClient;
import connection.map.MapClient;
import connection.map.NoOpMapClient;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lib.connection.TheMazeGrpc;
import lib.map.MapGrpc;

public class ClientFactory {
    private static final Logger logger = Logger.getLogger(ClientFactory.class.getName());

    @SuppressWarnings("CheckResult")
    public static GameClient newGameClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("dns:///" + host + ":" + port)
                .usePlaintext().build();
        try {
            TheMazeGrpc.newBlockingStub(channel).handshake(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            logger.info(String.format(Locale.ENGLISH,
                    "No answer from (%s:%d), switching to NoOpClient", host, port));
            return new NoOpGameClient();
        }
        return new GrpcGameClient(channel);
    }

    @SuppressWarnings("CheckResult")
    public static MapClient newMapClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("dns:///" + host + ":" + port)
                .usePlaintext().build();
        try {
            MapGrpc.newBlockingStub(channel).handshake(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            logger.info(String.format(Locale.ENGLISH,
                    "No answer from (%s:%d), switching to NoOpClient", host, port));
            return new NoOpMapClient();
        }
        return new GrpcMapClient(channel);
    }

}
