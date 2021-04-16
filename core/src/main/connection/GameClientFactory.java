package connection;

import com.google.protobuf.Empty;

import java.util.Locale;
import java.util.logging.Logger;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lib.connection.TheMazeGrpc;

public class GameClientFactory {
    private static final Logger logger = Logger.getLogger(GameClientFactory.class.getName());

    private final GameClient client;

    @SuppressWarnings("CheckResult")
    public GameClientFactory(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("dns:///" + host + ":" + port)
                .usePlaintext().build();
        try {
            TheMazeGrpc.newBlockingStub(channel).handshake(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            logger.info(String.format(Locale.ENGLISH,
                    "No answer from (%s:%d), switching to NoOpClient", host, port));
            this.client = new NoOpClient();
            return;
        }
        this.client = new GrpcClient(channel);
    }

    public GameClient getClient() {
        return this.client;
    }

}
