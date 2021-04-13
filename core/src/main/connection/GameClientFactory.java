package connection;

import com.google.protobuf.Empty;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import io.grpc.StatusRuntimeException;
import lib.connection.TheMazeGrpc;

public class GameClientFactory {

    private final GameClient client;

    @SuppressWarnings("CheckResult")
    public GameClientFactory(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forTarget("dns:///" + host + ":" + port).usePlaintext().build();
        try {
            TheMazeGrpc.newBlockingStub(channel).handshake(Empty.newBuilder().build());
        } catch (StatusRuntimeException e) {
            this.client = new NoOpClient();
            return;
        }
        this.client = new GrpcClient(channel);
    }

    public GameClient getClient() {
        return this.client;
    }

}
