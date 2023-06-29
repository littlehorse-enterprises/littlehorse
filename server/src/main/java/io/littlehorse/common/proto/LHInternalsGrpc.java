package io.littlehorse.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.54.0)",
    comments = "Source: internal_server.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LHInternalsGrpc {

  private LHInternalsGrpc() {}

  public static final String SERVICE_NAME = "littlehorse.LHInternals";

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

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalScanPb,
      io.littlehorse.common.proto.InternalScanReplyPb> getInternalScanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "InternalScan",
      requestType = io.littlehorse.common.proto.InternalScanPb.class,
      responseType = io.littlehorse.common.proto.InternalScanReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalScanPb,
      io.littlehorse.common.proto.InternalScanReplyPb> getInternalScanMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalScanPb, io.littlehorse.common.proto.InternalScanReplyPb> getInternalScanMethod;
    if ((getInternalScanMethod = LHInternalsGrpc.getInternalScanMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getInternalScanMethod = LHInternalsGrpc.getInternalScanMethod) == null) {
          LHInternalsGrpc.getInternalScanMethod = getInternalScanMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.InternalScanPb, io.littlehorse.common.proto.InternalScanReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "InternalScan"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.InternalScanPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.InternalScanReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("InternalScan"))
              .build();
        }
      }
    }
    return getInternalScanMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForCommandPb,
      io.littlehorse.common.proto.WaitForCommandReplyPb> getWaitForCommandMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "waitForCommand",
      requestType = io.littlehorse.common.proto.WaitForCommandPb.class,
      responseType = io.littlehorse.common.proto.WaitForCommandReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForCommandPb,
      io.littlehorse.common.proto.WaitForCommandReplyPb> getWaitForCommandMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.WaitForCommandPb, io.littlehorse.common.proto.WaitForCommandReplyPb> getWaitForCommandMethod;
    if ((getWaitForCommandMethod = LHInternalsGrpc.getWaitForCommandMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getWaitForCommandMethod = LHInternalsGrpc.getWaitForCommandMethod) == null) {
          LHInternalsGrpc.getWaitForCommandMethod = getWaitForCommandMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.WaitForCommandPb, io.littlehorse.common.proto.WaitForCommandReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "waitForCommand"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.WaitForCommandPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.WaitForCommandReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("waitForCommand"))
              .build();
        }
      }
    }
    return getWaitForCommandMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalGetAdvertisedHostsPb,
      io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb> getGetAdvertisedHostsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetAdvertisedHosts",
      requestType = io.littlehorse.common.proto.InternalGetAdvertisedHostsPb.class,
      responseType = io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalGetAdvertisedHostsPb,
      io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb> getGetAdvertisedHostsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.InternalGetAdvertisedHostsPb, io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb> getGetAdvertisedHostsMethod;
    if ((getGetAdvertisedHostsMethod = LHInternalsGrpc.getGetAdvertisedHostsMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getGetAdvertisedHostsMethod = LHInternalsGrpc.getGetAdvertisedHostsMethod) == null) {
          LHInternalsGrpc.getGetAdvertisedHostsMethod = getGetAdvertisedHostsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.InternalGetAdvertisedHostsPb, io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetAdvertisedHosts"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.InternalGetAdvertisedHostsPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("GetAdvertisedHosts"))
              .build();
        }
      }
    }
    return getGetAdvertisedHostsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.TopologyInstanceStatePb,
      io.littlehorse.common.proto.TopologyInstanceStateReplyPb> getTopologyInstancesStateMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TopologyInstancesState",
      requestType = io.littlehorse.common.proto.TopologyInstanceStatePb.class,
      responseType = io.littlehorse.common.proto.TopologyInstanceStateReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.TopologyInstanceStatePb,
      io.littlehorse.common.proto.TopologyInstanceStateReplyPb> getTopologyInstancesStateMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.TopologyInstanceStatePb, io.littlehorse.common.proto.TopologyInstanceStateReplyPb> getTopologyInstancesStateMethod;
    if ((getTopologyInstancesStateMethod = LHInternalsGrpc.getTopologyInstancesStateMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getTopologyInstancesStateMethod = LHInternalsGrpc.getTopologyInstancesStateMethod) == null) {
          LHInternalsGrpc.getTopologyInstancesStateMethod = getTopologyInstancesStateMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.TopologyInstanceStatePb, io.littlehorse.common.proto.TopologyInstanceStateReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TopologyInstancesState"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.TopologyInstanceStatePb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.TopologyInstanceStateReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("TopologyInstancesState"))
              .build();
        }
      }
    }
    return getTopologyInstancesStateMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.LocalTasksPb,
      io.littlehorse.common.proto.LocalTasksReplyPb> getLocalTasksMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "LocalTasks",
      requestType = io.littlehorse.common.proto.LocalTasksPb.class,
      responseType = io.littlehorse.common.proto.LocalTasksReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.LocalTasksPb,
      io.littlehorse.common.proto.LocalTasksReplyPb> getLocalTasksMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.LocalTasksPb, io.littlehorse.common.proto.LocalTasksReplyPb> getLocalTasksMethod;
    if ((getLocalTasksMethod = LHInternalsGrpc.getLocalTasksMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getLocalTasksMethod = LHInternalsGrpc.getLocalTasksMethod) == null) {
          LHInternalsGrpc.getLocalTasksMethod = getLocalTasksMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.LocalTasksPb, io.littlehorse.common.proto.LocalTasksReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "LocalTasks"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.LocalTasksPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.LocalTasksReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("LocalTasks"))
              .build();
        }
      }
    }
    return getLocalTasksMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.TagScanPb,
      io.littlehorse.common.proto.TagScanReplyPb> getTagScanMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TagScan",
      requestType = io.littlehorse.common.proto.TagScanPb.class,
      responseType = io.littlehorse.common.proto.TagScanReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.TagScanPb,
      io.littlehorse.common.proto.TagScanReplyPb> getTagScanMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.TagScanPb, io.littlehorse.common.proto.TagScanReplyPb> getTagScanMethod;
    if ((getTagScanMethod = LHInternalsGrpc.getTagScanMethod) == null) {
      synchronized (LHInternalsGrpc.class) {
        if ((getTagScanMethod = LHInternalsGrpc.getTagScanMethod) == null) {
          LHInternalsGrpc.getTagScanMethod = getTagScanMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.TagScanPb, io.littlehorse.common.proto.TagScanReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TagScan"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.TagScanPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.TagScanReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHInternalsMethodDescriptorSupplier("TagScan"))
              .build();
        }
      }
    }
    return getTagScanMethod;
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
    default void centralStoreQuery(io.littlehorse.common.proto.CentralStoreQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.CentralStoreQueryReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCentralStoreQueryMethod(), responseObserver);
    }

    /**
     */
    default void internalScan(io.littlehorse.common.proto.InternalScanPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalScanReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getInternalScanMethod(), responseObserver);
    }

    /**
     */
    default void waitForCommand(io.littlehorse.common.proto.WaitForCommandPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForCommandReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWaitForCommandMethod(), responseObserver);
    }

    /**
     */
    default void getAdvertisedHosts(io.littlehorse.common.proto.InternalGetAdvertisedHostsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetAdvertisedHostsMethod(), responseObserver);
    }

    /**
     */
    default void topologyInstancesState(io.littlehorse.common.proto.TopologyInstanceStatePb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.TopologyInstanceStateReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTopologyInstancesStateMethod(), responseObserver);
    }

    /**
     */
    default void localTasks(io.littlehorse.common.proto.LocalTasksPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.LocalTasksReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getLocalTasksMethod(), responseObserver);
    }

    /**
     */
    default void tagScan(io.littlehorse.common.proto.TagScanPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.TagScanReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTagScanMethod(), responseObserver);
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
    public void centralStoreQuery(io.littlehorse.common.proto.CentralStoreQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.CentralStoreQueryReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCentralStoreQueryMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void internalScan(io.littlehorse.common.proto.InternalScanPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalScanReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getInternalScanMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void waitForCommand(io.littlehorse.common.proto.WaitForCommandPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForCommandReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getWaitForCommandMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getAdvertisedHosts(io.littlehorse.common.proto.InternalGetAdvertisedHostsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetAdvertisedHostsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void topologyInstancesState(io.littlehorse.common.proto.TopologyInstanceStatePb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.TopologyInstanceStateReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTopologyInstancesStateMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void localTasks(io.littlehorse.common.proto.LocalTasksPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.LocalTasksReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getLocalTasksMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void tagScan(io.littlehorse.common.proto.TagScanPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.TagScanReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTagScanMethod(), getCallOptions()), request, responseObserver);
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
    public io.littlehorse.common.proto.CentralStoreQueryReplyPb centralStoreQuery(io.littlehorse.common.proto.CentralStoreQueryPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCentralStoreQueryMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.InternalScanReplyPb internalScan(io.littlehorse.common.proto.InternalScanPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getInternalScanMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.WaitForCommandReplyPb waitForCommand(io.littlehorse.common.proto.WaitForCommandPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getWaitForCommandMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb getAdvertisedHosts(io.littlehorse.common.proto.InternalGetAdvertisedHostsPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetAdvertisedHostsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.TopologyInstanceStateReplyPb topologyInstancesState(io.littlehorse.common.proto.TopologyInstanceStatePb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTopologyInstancesStateMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.LocalTasksReplyPb localTasks(io.littlehorse.common.proto.LocalTasksPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getLocalTasksMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.TagScanReplyPb tagScan(io.littlehorse.common.proto.TagScanPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTagScanMethod(), getCallOptions(), request);
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
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.CentralStoreQueryReplyPb> centralStoreQuery(
        io.littlehorse.common.proto.CentralStoreQueryPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCentralStoreQueryMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.InternalScanReplyPb> internalScan(
        io.littlehorse.common.proto.InternalScanPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getInternalScanMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.WaitForCommandReplyPb> waitForCommand(
        io.littlehorse.common.proto.WaitForCommandPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getWaitForCommandMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb> getAdvertisedHosts(
        io.littlehorse.common.proto.InternalGetAdvertisedHostsPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetAdvertisedHostsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.TopologyInstanceStateReplyPb> topologyInstancesState(
        io.littlehorse.common.proto.TopologyInstanceStatePb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTopologyInstancesStateMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.LocalTasksReplyPb> localTasks(
        io.littlehorse.common.proto.LocalTasksPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getLocalTasksMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.TagScanReplyPb> tagScan(
        io.littlehorse.common.proto.TagScanPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTagScanMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_CENTRAL_STORE_QUERY = 0;
  private static final int METHODID_INTERNAL_SCAN = 1;
  private static final int METHODID_WAIT_FOR_COMMAND = 2;
  private static final int METHODID_GET_ADVERTISED_HOSTS = 3;
  private static final int METHODID_TOPOLOGY_INSTANCES_STATE = 4;
  private static final int METHODID_LOCAL_TASKS = 5;
  private static final int METHODID_TAG_SCAN = 6;

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
        case METHODID_CENTRAL_STORE_QUERY:
          serviceImpl.centralStoreQuery((io.littlehorse.common.proto.CentralStoreQueryPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.CentralStoreQueryReplyPb>) responseObserver);
          break;
        case METHODID_INTERNAL_SCAN:
          serviceImpl.internalScan((io.littlehorse.common.proto.InternalScanPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalScanReplyPb>) responseObserver);
          break;
        case METHODID_WAIT_FOR_COMMAND:
          serviceImpl.waitForCommand((io.littlehorse.common.proto.WaitForCommandPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.WaitForCommandReplyPb>) responseObserver);
          break;
        case METHODID_GET_ADVERTISED_HOSTS:
          serviceImpl.getAdvertisedHosts((io.littlehorse.common.proto.InternalGetAdvertisedHostsPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb>) responseObserver);
          break;
        case METHODID_TOPOLOGY_INSTANCES_STATE:
          serviceImpl.topologyInstancesState((io.littlehorse.common.proto.TopologyInstanceStatePb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.TopologyInstanceStateReplyPb>) responseObserver);
          break;
        case METHODID_LOCAL_TASKS:
          serviceImpl.localTasks((io.littlehorse.common.proto.LocalTasksPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.LocalTasksReplyPb>) responseObserver);
          break;
        case METHODID_TAG_SCAN:
          serviceImpl.tagScan((io.littlehorse.common.proto.TagScanPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.TagScanReplyPb>) responseObserver);
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
          getCentralStoreQueryMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.CentralStoreQueryPb,
              io.littlehorse.common.proto.CentralStoreQueryReplyPb>(
                service, METHODID_CENTRAL_STORE_QUERY)))
        .addMethod(
          getInternalScanMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.InternalScanPb,
              io.littlehorse.common.proto.InternalScanReplyPb>(
                service, METHODID_INTERNAL_SCAN)))
        .addMethod(
          getWaitForCommandMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.WaitForCommandPb,
              io.littlehorse.common.proto.WaitForCommandReplyPb>(
                service, METHODID_WAIT_FOR_COMMAND)))
        .addMethod(
          getGetAdvertisedHostsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.InternalGetAdvertisedHostsPb,
              io.littlehorse.common.proto.InternalGetAdvertisedHostsReplyPb>(
                service, METHODID_GET_ADVERTISED_HOSTS)))
        .addMethod(
          getTopologyInstancesStateMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.TopologyInstanceStatePb,
              io.littlehorse.common.proto.TopologyInstanceStateReplyPb>(
                service, METHODID_TOPOLOGY_INSTANCES_STATE)))
        .addMethod(
          getLocalTasksMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.LocalTasksPb,
              io.littlehorse.common.proto.LocalTasksReplyPb>(
                service, METHODID_LOCAL_TASKS)))
        .addMethod(
          getTagScanMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.common.proto.TagScanPb,
              io.littlehorse.common.proto.TagScanReplyPb>(
                service, METHODID_TAG_SCAN)))
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
              .addMethod(getInternalScanMethod())
              .addMethod(getWaitForCommandMethod())
              .addMethod(getGetAdvertisedHostsMethod())
              .addMethod(getTopologyInstancesStateMethod())
              .addMethod(getLocalTasksMethod())
              .addMethod(getTagScanMethod())
              .build();
        }
      }
    }
    return result;
  }
}
