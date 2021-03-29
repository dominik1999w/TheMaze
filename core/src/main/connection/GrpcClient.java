package connection;

import java.util.UUID;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import lib.connection.ConnectRequest;
import lib.connection.TheMazeGrpc;
import mapobjects.Player;

public class GrpcClient implements GameClient {

    private final ManagedChannel channel;
    private final TheMazeGrpc.TheMazeBlockingStub blockingStub;

    private final Player player;

    private final UUID id;

    public GrpcClient(Player player, String host, int port) {
        this.player = player;
        this.id = UUID.randomUUID();

        channel = ManagedChannelBuilder.forTarget("dns:///" + host + ":" + port).usePlaintext().build();
        blockingStub = TheMazeGrpc.newBlockingStub(channel);
    }

    @Override
    public int connect() {
        ConnectRequest request = ConnectRequest.newBuilder().setId(id.toString()).build();
        return blockingStub.connect(request).getCount();
    }
}
