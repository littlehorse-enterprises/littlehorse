package io.littlehorse.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.57.2)",
    comments = "Source: internal_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LHInternalsGrpc {

  private LHInternalsGrpc() {}

  public static final java.lang.String SERVICE_NAME = "littlehorse.LHInternals";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetObjectRequest,
      io.littlehorse.common.proto.GetObjectResponse> getGetObjectMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetObject",
      requestType = io.littlehorse.common.proto.GetObjectRequest.class,
      responseType = io.littlehorse.common.proto.GetObjectResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetObjectRequest,
      io.littlehorse.common.proto.GetObjectResponse> getGetObjectMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetObjectRequest, io.littlehorse.common.proto.GetObjectResponse> getGetObjectMethod;
    if ((getGetObjectMethod = LHInternalsGrpc.getGetObjectMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getGetObjectMethod = LHInternalsGrpc.getGetObjectMethod) == null) {
          LHInternalsGrpc.getGetObjectMethod = getGetObjectMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetObjectRequest, io.littlehorse.common.proto.GetObjectResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetObject"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetObjectRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetObjectResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("GetObject"))
              .build();
        }
      }
    }
    return getGetObjectMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalScanPb,
      io.littlehorse.common.proto.InternalScanResponse> getInternalScanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "InternalScan",
      requestType = io.littlehorse.common.proto.InternalScanPb.class,
      responseType = io.littlehorse.common.proto.InternalScanResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalScanPb,
      io.littlehorse.common.proto.InternalScanResponse> getInternalScanMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalScanPb, io.littlehorse.common.proto.InternalScanResponse> getInternalScanMethod;
    if ((getInternalScanMethod = LHInternalsGrpc.getInternalScanMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getInternalScanMethod = LHInternalsGrpc.getInternalScanMethod) == null) {
          LHInternalsGrpc.getInternalScanMethod = getInternalScanMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.InternalScanPb, io.littlehorse.common.proto.InternalScanResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "InternalScan"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.InternalScanPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.InternalScanResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("InternalScan"))
              .build();
        }
      }
    }
    return getInternalScanMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForActionRequest,
      io.littlehorse.common.proto.WaitForActionResponse> getWaitForActionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WaitForAction",
      requestType = io.littlehorse.common.proto.WaitForActionRequest.class,
      responseType = io.littlehorse.common.proto.WaitForActionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForActionRequest,
      io.littlehorse.common.proto.WaitForActionResponse> getWaitForActionMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForActionRequest, io.littlehorse.common.proto.WaitForActionResponse> getWaitForActionMethod;
    if ((getWaitForActionMethod = LHInternalsGrpc.getWaitForActionMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getWaitForActionMethod = LHInternalsGrpc.getWaitForActionMethod) == null) {
          LHInternalsGrpc.getWaitForActionMethod = getWaitForActionMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.WaitForActionRequest, io.littlehorse.common.proto.WaitForActionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WaitForAction"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.WaitForActionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.WaitForActionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("WaitForAction"))
              .build();
        }
      }
    }
    return getWaitForActionMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse> getGetAdvertisedHostsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAdvertisedHosts",
      requestType = com.google.protobuf.Empty.class,
      responseType = io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse> getGetAdvertisedHostsMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse> getGetAdvertisedHostsMethod;
    if ((getGetAdvertisedHostsMethod = LHInternalsGrpc.getGetAdvertisedHostsMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getGetAdvertisedHostsMethod = LHInternalsGrpc.getGetAdvertisedHostsMethod) == null) {
          LHInternalsGrpc.getGetAdvertisedHostsMethod = getGetAdvertisedHostsMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAdvertisedHosts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("GetAdvertisedHosts"))
              .build();
        }
      }
    }
    return getGetAdvertisedHostsMethod;
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
  public interface AsyncService {

    /**
     */
    default void getObject(io.littlehorse.common.proto.GetObjectRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetObjectResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetObjectMethod(), responseObserver);
    }

    /**
     */
    default void internalScan(io.littlehorse.common.proto.InternalScanPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalScanResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInternalScanMethod(), responseObserver);
    }

    /**
     */
    default void waitForAction(io.littlehorse.common.proto.WaitForActionRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForActionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWaitForActionMethod(), responseObserver);
    }

    /**
     */
    default void getAdvertisedHosts(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAdvertisedHostsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service LHInternals.
   */
  public static abstract class LHInternalsImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return LHInternalsGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service LHInternals.
   */
  public static final class LHInternalsStub
      extends io.grpc.stub.AbstractAsyncStub<LHInternalsStub> {
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
    public void getObject(io.littlehorse.common.proto.GetObjectRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetObjectResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetObjectMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void internalScan(io.littlehorse.common.proto.InternalScanPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalScanResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInternalScanMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void waitForAction(io.littlehorse.common.proto.WaitForActionRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForActionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getWaitForActionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAdvertisedHosts(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAdvertisedHostsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service LHInternals.
   */
  public static final class LHInternalsBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<LHInternalsBlockingStub> {
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
    public io.littlehorse.common.proto.GetObjectResponse getObject(io.littlehorse.common.proto.GetObjectRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetObjectMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.InternalScanResponse internalScan(io.littlehorse.common.proto.InternalScanPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInternalScanMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.WaitForActionResponse waitForAction(io.littlehorse.common.proto.WaitForActionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getWaitForActionMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse getAdvertisedHosts(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAdvertisedHostsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service LHInternals.
   */
  public static final class LHInternalsFutureStub
      extends io.grpc.stub.AbstractFutureStub<LHInternalsFutureStub> {
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
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetObjectResponse> getObject(
        io.littlehorse.common.proto.GetObjectRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetObjectMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.InternalScanResponse> internalScan(
        io.littlehorse.common.proto.InternalScanPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInternalScanMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.WaitForActionResponse> waitForAction(
        io.littlehorse.common.proto.WaitForActionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getWaitForActionMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse> getAdvertisedHosts(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAdvertisedHostsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_GET_OBJECT = 0;
  private static final int METHODID_INTERNAL_SCAN = 1;
  private static final int METHODID_WAIT_FOR_ACTION = 2;
  private static final int METHODID_GET_ADVERTISED_HOSTS = 3;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final AsyncService serviceImpl;
    private final int methodId;

    MethodHandlers(AsyncService serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_GET_OBJECT:
          serviceImpl.getObject((io.littlehorse.common.proto.GetObjectRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetObjectResponse>) responseObserver);
          break;
        case METHODID_INTERNAL_SCAN:
          serviceImpl.internalScan((io.littlehorse.common.proto.InternalScanPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalScanResponse>) responseObserver);
          break;
        case METHODID_WAIT_FOR_ACTION:
          serviceImpl.waitForAction((io.littlehorse.common.proto.WaitForActionRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForActionResponse>) responseObserver);
          break;
        case METHODID_GET_ADVERTISED_HOSTS:
          serviceImpl.getAdvertisedHosts((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse>) responseObserver);
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

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getGetObjectMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.GetObjectRequest,
              io.littlehorse.common.proto.GetObjectResponse>(
                service, METHODID_GET_OBJECT)))
        .addMethod(
          getInternalScanMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.InternalScanPb,
              io.littlehorse.common.proto.InternalScanResponse>(
                service, METHODID_INTERNAL_SCAN)))
        .addMethod(
          getWaitForActionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.WaitForActionRequest,
              io.littlehorse.common.proto.WaitForActionResponse>(
                service, METHODID_WAIT_FOR_ACTION)))
        .addMethod(
          getGetAdvertisedHostsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              io.littlehorse.common.proto.InternalGetAdvertisedHostsResponse>(
                service, METHODID_GET_ADVERTISED_HOSTS)))
        .build();
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
    private final java.lang.String methodName;

    LHInternalsMethodDescriptorSupplier(java.lang.String methodName) {
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
              .addMethod(getGetObjectMethod())
              .addMethod(getInternalScanMethod())
              .addMethod(getWaitForActionMethod())
              .addMethod(getGetAdvertisedHostsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
