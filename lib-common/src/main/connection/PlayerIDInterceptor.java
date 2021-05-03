package connection;

import io.grpc.CallOptions;
import io.grpc.Channel;
import io.grpc.ClientCall;
import io.grpc.ClientInterceptor;
import io.grpc.ForwardingClientCall;
import io.grpc.Metadata;
import io.grpc.MethodDescriptor;

import static connection.CallKey.PLAYER_ID;
import static connection.CallKey.PLAYER_ID_METADATA;

public class PlayerIDInterceptor implements ClientInterceptor {
    @Override
    public <ReqT, RespT> ClientCall<ReqT, RespT> interceptCall(MethodDescriptor<ReqT, RespT> method, CallOptions callOptions, Channel next) {
        return new ForwardingClientCall.SimpleForwardingClientCall<ReqT, RespT>(next.newCall(method, callOptions)) {
            @Override
            public void start(Listener<RespT> responseListener, Metadata headers) {
                if (PLAYER_ID.get() != null) {
                    headers.put(PLAYER_ID_METADATA, PLAYER_ID.get().toString());
                }
                super.start(responseListener, headers);
            }
        };
    }
}
