package io.littlehorse.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.50.2)",
    comments = "Source: internal_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LHInternalsGrpc {

  private LHInternalsGrpc() {}

  public static final String SERVICE_NAME = "lh_proto.LHInternals";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.CentralStoreQueryPb,
      io.littlehorse.common.proto.CentralStoreQueryReplyPb> getCentralStoreQueryMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CentralStoreQuery",
      requestType = io.littlehorse.common.proto.CentralStoreQueryPb.class,
      responseType = io.littlehorse.common.proto.CentralStoreQueryReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.CentralStoreQueryPb,
      io.littlehorse.common.proto.CentralStoreQueryReplyPb> getCentralStoreQueryMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.CentralStoreQueryPb, io.littlehorse.common.proto.CentralStoreQueryReplyPb> getCentralStoreQueryMethod;
    if ((getCentralStoreQueryMethod = LHInternalsGrpc.getCentralStoreQueryMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getCentralStoreQueryMethod = LHInternalsGrpc.getCentralStoreQueryMethod) == null) {
          LHInternalsGrpc.getCentralStoreQueryMethod = getCentralStoreQueryMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.CentralStoreQueryPb, io.littlehorse.common.proto.CentralStoreQueryReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CentralStoreQuery"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.CentralStoreQueryPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.CentralStoreQueryReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("CentralStoreQuery"))
              .build();
        }
      }
    }
    return getCentralStoreQueryMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForCommandResultPb,
      io.littlehorse.common.proto.WaitForCommandResultReplyPb> getWaitForCommandResultMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WaitForCommandResult",
      requestType = io.littlehorse.common.proto.WaitForCommandResultPb.class,
      responseType = io.littlehorse.common.proto.WaitForCommandResultReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForCommandResultPb,
      io.littlehorse.common.proto.WaitForCommandResultReplyPb> getWaitForCommandResultMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForCommandResultPb, io.littlehorse.common.proto.WaitForCommandResultReplyPb> getWaitForCommandResultMethod;
    if ((getWaitForCommandResultMethod = LHInternalsGrpc.getWaitForCommandResultMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getWaitForCommandResultMethod = LHInternalsGrpc.getWaitForCommandResultMethod) == null) {
          LHInternalsGrpc.getWaitForCommandResultMethod = getWaitForCommandResultMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.WaitForCommandResultPb, io.littlehorse.common.proto.WaitForCommandResultReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WaitForCommandResult"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.WaitForCommandResultPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.WaitForCommandResultReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("WaitForCommandResult"))
              .build();
        }
      }
    }
    return getWaitForCommandResultMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LHInternalsStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LHInternalsStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LHInternalsStub>() {
        @java.lang.Override
        public LHInternalsStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LHInternalsStub(channel, callOptions);
        }
      };
    return LHInternalsStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LHInternalsBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LHInternalsBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LHInternalsBlockingStub>() {
        @java.lang.Override
        public LHInternalsBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LHInternalsBlockingStub(channel, callOptions);
        }
      };
    return LHInternalsBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LHInternalsFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LHInternalsFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LHInternalsFutureStub>() {
        @java.lang.Override
        public LHInternalsFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LHInternalsFutureStub(channel, callOptions);
        }
      };
    return LHInternalsFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class LHInternalsImplBase implements io.grpc.BindableService {

    /**
     */
    public void centralStoreQuery(io.littlehorse.common.proto.CentralStoreQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.CentralStoreQueryReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCentralStoreQueryMethod(), responseObserver);
    }

    /**
     */
    public void waitForCommandResult(io.littlehorse.common.proto.WaitForCommandResultPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForCommandResultReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWaitForCommandResultMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getCentralStoreQueryMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.CentralStoreQueryPb,
                io.littlehorse.common.proto.CentralStoreQueryReplyPb>(
                  this, METHODID_CENTRAL_STORE_QUERY)))
          .addMethod(
            getWaitForCommandResultMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.WaitForCommandResultPb,
                io.littlehorse.common.proto.WaitForCommandResultReplyPb>(
                  this, METHODID_WAIT_FOR_COMMAND_RESULT)))
          .build();
    }
  }

  /**
   */
  public static final class LHInternalsStub extends io.grpc.stub.AbstractAsyncStub<LHInternalsStub> {
    private LHInternalsStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LHInternalsStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LHInternalsStub(channel, callOptions);
    }

    /**
     */
    public void centralStoreQuery(io.littlehorse.common.proto.CentralStoreQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.CentralStoreQueryReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCentralStoreQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void waitForCommandResult(io.littlehorse.common.proto.WaitForCommandResultPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForCommandResultReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getWaitForCommandResultMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class LHInternalsBlockingStub extends io.grpc.stub.AbstractBlockingStub<LHInternalsBlockingStub> {
    private LHInternalsBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LHInternalsBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LHInternalsBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.littlehorse.common.proto.CentralStoreQueryReplyPb centralStoreQuery(io.littlehorse.common.proto.CentralStoreQueryPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCentralStoreQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.WaitForCommandResultReplyPb waitForCommandResult(io.littlehorse.common.proto.WaitForCommandResultPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getWaitForCommandResultMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class LHInternalsFutureStub extends io.grpc.stub.AbstractFutureStub<LHInternalsFutureStub> {
    private LHInternalsFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LHInternalsFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LHInternalsFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.CentralStoreQueryReplyPb> centralStoreQuery(
        io.littlehorse.common.proto.CentralStoreQueryPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCentralStoreQueryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.WaitForCommandResultReplyPb> waitForCommandResult(
        io.littlehorse.common.proto.WaitForCommandResultPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getWaitForCommandResultMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CENTRAL_STORE_QUERY = 0;
  private static final int METHODID_WAIT_FOR_COMMAND_RESULT = 1;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final LHInternalsImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(LHInternalsImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_CENTRAL_STORE_QUERY:
          serviceImpl.centralStoreQuery((io.littlehorse.common.proto.CentralStoreQueryPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.CentralStoreQueryReplyPb>) responseObserver);
          break;
        case METHODID_WAIT_FOR_COMMAND_RESULT:
          serviceImpl.waitForCommandResult((io.littlehorse.common.proto.WaitForCommandResultPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForCommandResultReplyPb>) responseObserver);
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

  private static abstract class LHInternalsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LHInternalsBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.littlehorse.common.proto.InternalServer.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("LHInternals");
    }
  }

  private static final class LHInternalsFileDescriptorSupplier
      extends LHInternalsBaseDescriptorSupplier {
    LHInternalsFileDescriptorSupplier() {}
  }

  private static final class LHInternalsMethodDescriptorSupplier
      extends LHInternalsBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    LHInternalsMethodDescriptorSupplier(String methodName) {
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
      synchronized (LHInternalsGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LHInternalsFileDescriptorSupplier())
              .addMethod(getCentralStoreQueryMethod())
              .addMethod(getWaitForCommandResultMethod())
              .build();
        }
      }
    }
    return result;
  }
}
