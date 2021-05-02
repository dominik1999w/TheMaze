package connection;

import java.util.UUID;

import io.grpc.Context;
import io.grpc.Metadata;

public class CallKey {

    public static final Context.Key<UUID> PLAYER_ID = Context.key("playerID");
    public static final Metadata.Key<String> PLAYER_ID_METADATA = Metadata.Key
            .of("playerID", Metadata.ASCII_STRING_MARSHALLER);

}
