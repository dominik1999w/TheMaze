package service;

import java.util.UUID;

import io.grpc.Context;
import io.grpc.Contexts;
import io.grpc.Metadata;
import io.grpc.ServerCall;
import io.grpc.ServerCallHandler;
import io.grpc.ServerInterceptor;

import static connection.CallKey.PLAYER_ID;
import static connection.CallKey.PLAYER_ID_METADATA;

public class PlayerIDInterceptor implements ServerInterceptor {

    @Override
    public <ReqT, RespT> ServerCall.Listener<ReqT> interceptCall(ServerCall<ReqT, RespT> call, Metadata headers, ServerCallHandler<ReqT, RespT> next) {
        Context context;
        if (
                "SyncGameState".equals(call.getMethodDescriptor().getBareMethodName())
                        || "SyncMapState".equals(call.getMethodDescriptor().getBareMethodName())
        ) {
            UUID playerID = UUID.fromString(headers.get(PLAYER_ID_METADATA));
            context = Context.current().withValue(PLAYER_ID, playerID);
        } else {
            context = Context.current();
        }
        return Contexts.interceptCall(context, call, headers, next);
    }
}
