package lib.connection;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.36.0)",
    comments = "Source: the_maze.proto")
public final class TheMazeGrpc {

  private TheMazeGrpc() {}

  public static final String SERVICE_NAME = "the_maze.TheMaze";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<lib.connection.ConnectRequest,
      lib.connection.ConnectReply> getConnectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Connect",
      requestType = lib.connection.ConnectRequest.class,
      responseType = lib.connection.ConnectReply.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<lib.connection.ConnectRequest,
      lib.connection.ConnectReply> getConnectMethod() {
    io.grpc.MethodDescriptor<lib.connection.ConnectRequest, lib.connection.ConnectReply> getConnectMethod;
    if ((getConnectMethod = TheMazeGrpc.getConnectMethod) == null) {
      synchronized (TheMazeGrpc.class) {
        if ((getConnectMethod = TheMazeGrpc.getConnectMethod) == null) {
          TheMazeGrpc.getConnectMethod = getConnectMethod =
              io.grpc.MethodDescriptor.<lib.connection.ConnectRequest, lib.connection.ConnectReply>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Connect"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  lib.connection.ConnectRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  lib.connection.ConnectReply.getDefaultInstance()))
              .setSchemaDescriptor(new TheMazeMethodDescriptorSupplier("Connect"))
              .build();
        }
      }
    }
    return getConnectMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static TheMazeStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TheMazeStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TheMazeStub>() {
        @java.lang.Override
        public TheMazeStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TheMazeStub(channel, callOptions);
        }
      };
    return TheMazeStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static TheMazeBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TheMazeBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TheMazeBlockingStub>() {
        @java.lang.Override
        public TheMazeBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TheMazeBlockingStub(channel, callOptions);
        }
      };
    return TheMazeBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static TheMazeFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<TheMazeFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<TheMazeFutureStub>() {
        @java.lang.Override
        public TheMazeFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new TheMazeFutureStub(channel, callOptions);
        }
      };
    return TheMazeFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class TheMazeImplBase implements io.grpc.BindableService {

    /**
     */
    public void connect(lib.connection.ConnectRequest request,
        io.grpc.stub.StreamObserver<lib.connection.ConnectReply> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getConnectMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getConnectMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                lib.connection.ConnectRequest,
                lib.connection.ConnectReply>(
                  this, METHODID_CONNECT)))
          .build();
    }
  }

  /**
   */
  public static final class TheMazeStub extends io.grpc.stub.AbstractAsyncStub<TheMazeStub> {
    private TheMazeStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TheMazeStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TheMazeStub(channel, callOptions);
    }

    /**
     */
    public void connect(lib.connection.ConnectRequest request,
        io.grpc.stub.StreamObserver<lib.connection.ConnectReply> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getConnectMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class TheMazeBlockingStub extends io.grpc.stub.AbstractBlockingStub<TheMazeBlockingStub> {
    private TheMazeBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TheMazeBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TheMazeBlockingStub(channel, callOptions);
    }

    /**
     */
    public lib.connection.ConnectReply connect(lib.connection.ConnectRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getConnectMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class TheMazeFutureStub extends io.grpc.stub.AbstractFutureStub<TheMazeFutureStub> {
    private TheMazeFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected TheMazeFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new TheMazeFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<lib.connection.ConnectReply> connect(
        lib.connection.ConnectRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getConnectMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CONNECT = 0;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final TheMazeImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(TheMazeImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CONNECT:
          serviceImpl.connect((lib.connection.ConnectRequest) request,
              (io.grpc.stub.StreamObserver<lib.connection.ConnectReply>) responseObserver);
          break;
        default:
          throw new AssertionError();
      }
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public io.grpc.stub.StreamObserver<Req> invoke(
        io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class TheMazeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    TheMazeBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return lib.connection.TheMazeGrpcGdx.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("TheMaze");
    }
  }

  private static final class TheMazeFileDescriptorSupplier
      extends TheMazeBaseDescriptorSupplier {
    TheMazeFileDescriptorSupplier() {}
  }

  private static final class TheMazeMethodDescriptorSupplier
      extends TheMazeBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    TheMazeMethodDescriptorSupplier(String methodName) {
      this.methodName = methodName;
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.MethodDescriptor getMethodDescriptor() {
      return getServiceDescriptor().findMethodByName(methodName);
    }
  }

  private static volatile io.grpc.ServiceDescriptor serviceDescriptor;

  public static io.grpc.ServiceDescriptor getServiceDescriptor() {
    io.grpc.ServiceDescriptor result = serviceDescriptor;
    if (result == null) {
      synchronized (TheMazeGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new TheMazeFileDescriptorSupplier())
              .addMethod(getConnectMethod())
              .build();
        }
      }
    }
    return result;
  }
}
