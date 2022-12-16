package io.littlehorse.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.50.2)",
    comments = "Source: service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LHPublicApiGrpc {

  private LHPublicApiGrpc() {}

  public static final String SERVICE_NAME = "lh_proto.LHPublicApi";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutTaskDefPb,
      io.littlehorse.common.proto.PutTaskDefReplyPb> getPutTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutTaskDef",
      requestType = io.littlehorse.common.proto.PutTaskDefPb.class,
      responseType = io.littlehorse.common.proto.PutTaskDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutTaskDefPb,
      io.littlehorse.common.proto.PutTaskDefReplyPb> getPutTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutTaskDefPb, io.littlehorse.common.proto.PutTaskDefReplyPb> getPutTaskDefMethod;
    if ((getPutTaskDefMethod = LHPublicApiGrpc.getPutTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutTaskDefMethod = LHPublicApiGrpc.getPutTaskDefMethod) == null) {
          LHPublicApiGrpc.getPutTaskDefMethod = getPutTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.PutTaskDefPb, io.littlehorse.common.proto.PutTaskDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutTaskDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutTaskDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutTaskDef"))
              .build();
        }
      }
    }
    return getPutTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetTaskDefPb,
      io.littlehorse.common.proto.GetTaskDefReplyPb> getGetTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTaskDef",
      requestType = io.littlehorse.common.proto.GetTaskDefPb.class,
      responseType = io.littlehorse.common.proto.GetTaskDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetTaskDefPb,
      io.littlehorse.common.proto.GetTaskDefReplyPb> getGetTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetTaskDefPb, io.littlehorse.common.proto.GetTaskDefReplyPb> getGetTaskDefMethod;
    if ((getGetTaskDefMethod = LHPublicApiGrpc.getGetTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetTaskDefMethod = LHPublicApiGrpc.getGetTaskDefMethod) == null) {
          LHPublicApiGrpc.getGetTaskDefMethod = getGetTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetTaskDefPb, io.littlehorse.common.proto.GetTaskDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetTaskDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetTaskDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetTaskDef"))
              .build();
        }
      }
    }
    return getGetTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutExternalEventDefPb,
      io.littlehorse.common.proto.PutExternalEventDefReplyPb> getPutExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutExternalEventDef",
      requestType = io.littlehorse.common.proto.PutExternalEventDefPb.class,
      responseType = io.littlehorse.common.proto.PutExternalEventDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutExternalEventDefPb,
      io.littlehorse.common.proto.PutExternalEventDefReplyPb> getPutExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutExternalEventDefPb, io.littlehorse.common.proto.PutExternalEventDefReplyPb> getPutExternalEventDefMethod;
    if ((getPutExternalEventDefMethod = LHPublicApiGrpc.getPutExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutExternalEventDefMethod = LHPublicApiGrpc.getPutExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getPutExternalEventDefMethod = getPutExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.PutExternalEventDefPb, io.littlehorse.common.proto.PutExternalEventDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutExternalEventDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutExternalEventDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutExternalEventDef"))
              .build();
        }
      }
    }
    return getPutExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetExternalEventDefPb,
      io.littlehorse.common.proto.GetExternalEventDefReplyPb> getGetExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetExternalEventDef",
      requestType = io.littlehorse.common.proto.GetExternalEventDefPb.class,
      responseType = io.littlehorse.common.proto.GetExternalEventDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetExternalEventDefPb,
      io.littlehorse.common.proto.GetExternalEventDefReplyPb> getGetExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetExternalEventDefPb, io.littlehorse.common.proto.GetExternalEventDefReplyPb> getGetExternalEventDefMethod;
    if ((getGetExternalEventDefMethod = LHPublicApiGrpc.getGetExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetExternalEventDefMethod = LHPublicApiGrpc.getGetExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getGetExternalEventDefMethod = getGetExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetExternalEventDefPb, io.littlehorse.common.proto.GetExternalEventDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetExternalEventDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetExternalEventDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetExternalEventDef"))
              .build();
        }
      }
    }
    return getGetExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutWfSpecPb,
      io.littlehorse.common.proto.PutWfSpecReplyPb> getPutWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutWfSpec",
      requestType = io.littlehorse.common.proto.PutWfSpecPb.class,
      responseType = io.littlehorse.common.proto.PutWfSpecReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutWfSpecPb,
      io.littlehorse.common.proto.PutWfSpecReplyPb> getPutWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutWfSpecPb, io.littlehorse.common.proto.PutWfSpecReplyPb> getPutWfSpecMethod;
    if ((getPutWfSpecMethod = LHPublicApiGrpc.getPutWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutWfSpecMethod = LHPublicApiGrpc.getPutWfSpecMethod) == null) {
          LHPublicApiGrpc.getPutWfSpecMethod = getPutWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.PutWfSpecPb, io.littlehorse.common.proto.PutWfSpecReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutWfSpecPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutWfSpecReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutWfSpec"))
              .build();
        }
      }
    }
    return getPutWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetWfSpecPb,
      io.littlehorse.common.proto.GetWfSpecReplyPb> getGetWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWfSpec",
      requestType = io.littlehorse.common.proto.GetWfSpecPb.class,
      responseType = io.littlehorse.common.proto.GetWfSpecReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetWfSpecPb,
      io.littlehorse.common.proto.GetWfSpecReplyPb> getGetWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetWfSpecPb, io.littlehorse.common.proto.GetWfSpecReplyPb> getGetWfSpecMethod;
    if ((getGetWfSpecMethod = LHPublicApiGrpc.getGetWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetWfSpecMethod = LHPublicApiGrpc.getGetWfSpecMethod) == null) {
          LHPublicApiGrpc.getGetWfSpecMethod = getGetWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetWfSpecPb, io.littlehorse.common.proto.GetWfSpecReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetWfSpecPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetWfSpecReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetWfSpec"))
              .build();
        }
      }
    }
    return getGetWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.RunWfPb,
      io.littlehorse.common.proto.RunWfReplyPb> getRunWfMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RunWf",
      requestType = io.littlehorse.common.proto.RunWfPb.class,
      responseType = io.littlehorse.common.proto.RunWfReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.RunWfPb,
      io.littlehorse.common.proto.RunWfReplyPb> getRunWfMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.RunWfPb, io.littlehorse.common.proto.RunWfReplyPb> getRunWfMethod;
    if ((getRunWfMethod = LHPublicApiGrpc.getRunWfMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getRunWfMethod = LHPublicApiGrpc.getRunWfMethod) == null) {
          LHPublicApiGrpc.getRunWfMethod = getRunWfMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.RunWfPb, io.littlehorse.common.proto.RunWfReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RunWf"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.RunWfPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.RunWfReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("RunWf"))
              .build();
        }
      }
    }
    return getRunWfMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetWfRunPb,
      io.littlehorse.common.proto.GetWfRunReplyPb> getGetWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWfRun",
      requestType = io.littlehorse.common.proto.GetWfRunPb.class,
      responseType = io.littlehorse.common.proto.GetWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetWfRunPb,
      io.littlehorse.common.proto.GetWfRunReplyPb> getGetWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetWfRunPb, io.littlehorse.common.proto.GetWfRunReplyPb> getGetWfRunMethod;
    if ((getGetWfRunMethod = LHPublicApiGrpc.getGetWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetWfRunMethod = LHPublicApiGrpc.getGetWfRunMethod) == null) {
          LHPublicApiGrpc.getGetWfRunMethod = getGetWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetWfRunPb, io.littlehorse.common.proto.GetWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetWfRun"))
              .build();
        }
      }
    }
    return getGetWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetNodeRunPb,
      io.littlehorse.common.proto.GetNodeRunReplyPb> getGetNodeRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNodeRun",
      requestType = io.littlehorse.common.proto.GetNodeRunPb.class,
      responseType = io.littlehorse.common.proto.GetNodeRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetNodeRunPb,
      io.littlehorse.common.proto.GetNodeRunReplyPb> getGetNodeRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetNodeRunPb, io.littlehorse.common.proto.GetNodeRunReplyPb> getGetNodeRunMethod;
    if ((getGetNodeRunMethod = LHPublicApiGrpc.getGetNodeRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetNodeRunMethod = LHPublicApiGrpc.getGetNodeRunMethod) == null) {
          LHPublicApiGrpc.getGetNodeRunMethod = getGetNodeRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetNodeRunPb, io.littlehorse.common.proto.GetNodeRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNodeRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetNodeRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetNodeRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetNodeRun"))
              .build();
        }
      }
    }
    return getGetNodeRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetVariablePb,
      io.littlehorse.common.proto.GetVariableReplyPb> getGetVariableMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetVariable",
      requestType = io.littlehorse.common.proto.GetVariablePb.class,
      responseType = io.littlehorse.common.proto.GetVariableReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetVariablePb,
      io.littlehorse.common.proto.GetVariableReplyPb> getGetVariableMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetVariablePb, io.littlehorse.common.proto.GetVariableReplyPb> getGetVariableMethod;
    if ((getGetVariableMethod = LHPublicApiGrpc.getGetVariableMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetVariableMethod = LHPublicApiGrpc.getGetVariableMethod) == null) {
          LHPublicApiGrpc.getGetVariableMethod = getGetVariableMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetVariablePb, io.littlehorse.common.proto.GetVariableReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetVariable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetVariablePb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetVariableReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetVariable"))
              .build();
        }
      }
    }
    return getGetVariableMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutExternalEventPb,
      io.littlehorse.common.proto.PutExternalEventReplyPb> getPutExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutExternalEvent",
      requestType = io.littlehorse.common.proto.PutExternalEventPb.class,
      responseType = io.littlehorse.common.proto.PutExternalEventReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutExternalEventPb,
      io.littlehorse.common.proto.PutExternalEventReplyPb> getPutExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.PutExternalEventPb, io.littlehorse.common.proto.PutExternalEventReplyPb> getPutExternalEventMethod;
    if ((getPutExternalEventMethod = LHPublicApiGrpc.getPutExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutExternalEventMethod = LHPublicApiGrpc.getPutExternalEventMethod) == null) {
          LHPublicApiGrpc.getPutExternalEventMethod = getPutExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.PutExternalEventPb, io.littlehorse.common.proto.PutExternalEventReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutExternalEventPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PutExternalEventReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutExternalEvent"))
              .build();
        }
      }
    }
    return getPutExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetExternalEventPb,
      io.littlehorse.common.proto.GetExternalEventReplyPb> getGetExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetExternalEvent",
      requestType = io.littlehorse.common.proto.GetExternalEventPb.class,
      responseType = io.littlehorse.common.proto.GetExternalEventReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetExternalEventPb,
      io.littlehorse.common.proto.GetExternalEventReplyPb> getGetExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.GetExternalEventPb, io.littlehorse.common.proto.GetExternalEventReplyPb> getGetExternalEventMethod;
    if ((getGetExternalEventMethod = LHPublicApiGrpc.getGetExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetExternalEventMethod = LHPublicApiGrpc.getGetExternalEventMethod) == null) {
          LHPublicApiGrpc.getGetExternalEventMethod = getGetExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.GetExternalEventPb, io.littlehorse.common.proto.GetExternalEventReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetExternalEventPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.GetExternalEventReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetExternalEvent"))
              .build();
        }
      }
    }
    return getGetExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.SearchWfRunPb,
      io.littlehorse.common.proto.SearchWfRunReplyPb> getSearchWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchWfRun",
      requestType = io.littlehorse.common.proto.SearchWfRunPb.class,
      responseType = io.littlehorse.common.proto.SearchWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.SearchWfRunPb,
      io.littlehorse.common.proto.SearchWfRunReplyPb> getSearchWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.SearchWfRunPb, io.littlehorse.common.proto.SearchWfRunReplyPb> getSearchWfRunMethod;
    if ((getSearchWfRunMethod = LHPublicApiGrpc.getSearchWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchWfRunMethod = LHPublicApiGrpc.getSearchWfRunMethod) == null) {
          LHPublicApiGrpc.getSearchWfRunMethod = getSearchWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.SearchWfRunPb, io.littlehorse.common.proto.SearchWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.SearchWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.SearchWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchWfRun"))
              .build();
        }
      }
    }
    return getSearchWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.RegisterTaskWorkerPb,
      io.littlehorse.common.proto.RegisterTaskWorkerReplyPb> getRegisterTaskWorkerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterTaskWorker",
      requestType = io.littlehorse.common.proto.RegisterTaskWorkerPb.class,
      responseType = io.littlehorse.common.proto.RegisterTaskWorkerReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.RegisterTaskWorkerPb,
      io.littlehorse.common.proto.RegisterTaskWorkerReplyPb> getRegisterTaskWorkerMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.RegisterTaskWorkerPb, io.littlehorse.common.proto.RegisterTaskWorkerReplyPb> getRegisterTaskWorkerMethod;
    if ((getRegisterTaskWorkerMethod = LHPublicApiGrpc.getRegisterTaskWorkerMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getRegisterTaskWorkerMethod = LHPublicApiGrpc.getRegisterTaskWorkerMethod) == null) {
          LHPublicApiGrpc.getRegisterTaskWorkerMethod = getRegisterTaskWorkerMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.RegisterTaskWorkerPb, io.littlehorse.common.proto.RegisterTaskWorkerReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterTaskWorker"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.RegisterTaskWorkerPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.RegisterTaskWorkerReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("RegisterTaskWorker"))
              .build();
        }
      }
    }
    return getRegisterTaskWorkerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.PollTaskPb,
      io.littlehorse.common.proto.PollTaskReplyPb> getPollTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PollTask",
      requestType = io.littlehorse.common.proto.PollTaskPb.class,
      responseType = io.littlehorse.common.proto.PollTaskReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.PollTaskPb,
      io.littlehorse.common.proto.PollTaskReplyPb> getPollTaskMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.PollTaskPb, io.littlehorse.common.proto.PollTaskReplyPb> getPollTaskMethod;
    if ((getPollTaskMethod = LHPublicApiGrpc.getPollTaskMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPollTaskMethod = LHPublicApiGrpc.getPollTaskMethod) == null) {
          LHPublicApiGrpc.getPollTaskMethod = getPollTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.PollTaskPb, io.littlehorse.common.proto.PollTaskReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PollTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PollTaskPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.PollTaskReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PollTask"))
              .build();
        }
      }
    }
    return getPollTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.TaskResultEventPb,
      io.littlehorse.common.proto.ReportTaskReplyPb> getReportTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReportTask",
      requestType = io.littlehorse.common.proto.TaskResultEventPb.class,
      responseType = io.littlehorse.common.proto.ReportTaskReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.TaskResultEventPb,
      io.littlehorse.common.proto.ReportTaskReplyPb> getReportTaskMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.TaskResultEventPb, io.littlehorse.common.proto.ReportTaskReplyPb> getReportTaskMethod;
    if ((getReportTaskMethod = LHPublicApiGrpc.getReportTaskMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getReportTaskMethod = LHPublicApiGrpc.getReportTaskMethod) == null) {
          LHPublicApiGrpc.getReportTaskMethod = getReportTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.TaskResultEventPb, io.littlehorse.common.proto.ReportTaskReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReportTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.TaskResultEventPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.ReportTaskReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ReportTask"))
              .build();
        }
      }
    }
    return getReportTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.StopWfRunPb,
      io.littlehorse.common.proto.StopWfRunReplyPb> getStopWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StopWfRun",
      requestType = io.littlehorse.common.proto.StopWfRunPb.class,
      responseType = io.littlehorse.common.proto.StopWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.StopWfRunPb,
      io.littlehorse.common.proto.StopWfRunReplyPb> getStopWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.StopWfRunPb, io.littlehorse.common.proto.StopWfRunReplyPb> getStopWfRunMethod;
    if ((getStopWfRunMethod = LHPublicApiGrpc.getStopWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getStopWfRunMethod = LHPublicApiGrpc.getStopWfRunMethod) == null) {
          LHPublicApiGrpc.getStopWfRunMethod = getStopWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.StopWfRunPb, io.littlehorse.common.proto.StopWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StopWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.StopWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.StopWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("StopWfRun"))
              .build();
        }
      }
    }
    return getStopWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.ResumeWfRunPb,
      io.littlehorse.common.proto.ResumeWfRunReplyPb> getResumeWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ResumeWfRun",
      requestType = io.littlehorse.common.proto.ResumeWfRunPb.class,
      responseType = io.littlehorse.common.proto.ResumeWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.ResumeWfRunPb,
      io.littlehorse.common.proto.ResumeWfRunReplyPb> getResumeWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.ResumeWfRunPb, io.littlehorse.common.proto.ResumeWfRunReplyPb> getResumeWfRunMethod;
    if ((getResumeWfRunMethod = LHPublicApiGrpc.getResumeWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getResumeWfRunMethod = LHPublicApiGrpc.getResumeWfRunMethod) == null) {
          LHPublicApiGrpc.getResumeWfRunMethod = getResumeWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.ResumeWfRunPb, io.littlehorse.common.proto.ResumeWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ResumeWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.ResumeWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.ResumeWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ResumeWfRun"))
              .build();
        }
      }
    }
    return getResumeWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.common.proto.DeleteWfRunPb,
      io.littlehorse.common.proto.DeleteWfRunReplyPb> getDeleteWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteWfRun",
      requestType = io.littlehorse.common.proto.DeleteWfRunPb.class,
      responseType = io.littlehorse.common.proto.DeleteWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.common.proto.DeleteWfRunPb,
      io.littlehorse.common.proto.DeleteWfRunReplyPb> getDeleteWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.common.proto.DeleteWfRunPb, io.littlehorse.common.proto.DeleteWfRunReplyPb> getDeleteWfRunMethod;
    if ((getDeleteWfRunMethod = LHPublicApiGrpc.getDeleteWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteWfRunMethod = LHPublicApiGrpc.getDeleteWfRunMethod) == null) {
          LHPublicApiGrpc.getDeleteWfRunMethod = getDeleteWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.common.proto.DeleteWfRunPb, io.littlehorse.common.proto.DeleteWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.DeleteWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.common.proto.DeleteWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteWfRun"))
              .build();
        }
      }
    }
    return getDeleteWfRunMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LHPublicApiStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LHPublicApiStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LHPublicApiStub>() {
        @java.lang.Override
        public LHPublicApiStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LHPublicApiStub(channel, callOptions);
        }
      };
    return LHPublicApiStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LHPublicApiBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LHPublicApiBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LHPublicApiBlockingStub>() {
        @java.lang.Override
        public LHPublicApiBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LHPublicApiBlockingStub(channel, callOptions);
        }
      };
    return LHPublicApiBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LHPublicApiFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LHPublicApiFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LHPublicApiFutureStub>() {
        @java.lang.Override
        public LHPublicApiFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LHPublicApiFutureStub(channel, callOptions);
        }
      };
    return LHPublicApiFutureStub.newStub(factory, channel);
  }

  /**
   */
  public static abstract class LHPublicApiImplBase implements io.grpc.BindableService {

    /**
     */
    public void putTaskDef(io.littlehorse.common.proto.PutTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutTaskDefMethod(), responseObserver);
    }

    /**
     */
    public void getTaskDef(io.littlehorse.common.proto.GetTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskDefMethod(), responseObserver);
    }

    /**
     */
    public void putExternalEventDef(io.littlehorse.common.proto.PutExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    public void getExternalEventDef(io.littlehorse.common.proto.GetExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    public void putWfSpec(io.littlehorse.common.proto.PutWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutWfSpecMethod(), responseObserver);
    }

    /**
     */
    public void getWfSpec(io.littlehorse.common.proto.GetWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfSpecMethod(), responseObserver);
    }

    /**
     */
    public void runWf(io.littlehorse.common.proto.RunWfPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RunWfReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRunWfMethod(), responseObserver);
    }

    /**
     */
    public void getWfRun(io.littlehorse.common.proto.GetWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfRunMethod(), responseObserver);
    }

    /**
     */
    public void getNodeRun(io.littlehorse.common.proto.GetNodeRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetNodeRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetNodeRunMethod(), responseObserver);
    }

    /**
     */
    public void getVariable(io.littlehorse.common.proto.GetVariablePb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetVariableReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetVariableMethod(), responseObserver);
    }

    /**
     */
    public void putExternalEvent(io.littlehorse.common.proto.PutExternalEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventMethod(), responseObserver);
    }

    /**
     */
    public void getExternalEvent(io.littlehorse.common.proto.GetExternalEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventMethod(), responseObserver);
    }

    /**
     */
    public void searchWfRun(io.littlehorse.common.proto.SearchWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.SearchWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWfRunMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RegisterTaskWorkerPb> registerTaskWorker(
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RegisterTaskWorkerReplyPb> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getRegisterTaskWorkerMethod(), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PollTaskPb> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PollTaskReplyPb> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getPollTaskMethod(), responseObserver);
    }

    /**
     */
    public void reportTask(io.littlehorse.common.proto.TaskResultEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.ReportTaskReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReportTaskMethod(), responseObserver);
    }

    /**
     */
    public void stopWfRun(io.littlehorse.common.proto.StopWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.StopWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStopWfRunMethod(), responseObserver);
    }

    /**
     */
    public void resumeWfRun(io.littlehorse.common.proto.ResumeWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.ResumeWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getResumeWfRunMethod(), responseObserver);
    }

    /**
     */
    public void deleteWfRun(io.littlehorse.common.proto.DeleteWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.DeleteWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWfRunMethod(), responseObserver);
    }

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
          .addMethod(
            getPutTaskDefMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.PutTaskDefPb,
                io.littlehorse.common.proto.PutTaskDefReplyPb>(
                  this, METHODID_PUT_TASK_DEF)))
          .addMethod(
            getGetTaskDefMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.GetTaskDefPb,
                io.littlehorse.common.proto.GetTaskDefReplyPb>(
                  this, METHODID_GET_TASK_DEF)))
          .addMethod(
            getPutExternalEventDefMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.PutExternalEventDefPb,
                io.littlehorse.common.proto.PutExternalEventDefReplyPb>(
                  this, METHODID_PUT_EXTERNAL_EVENT_DEF)))
          .addMethod(
            getGetExternalEventDefMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.GetExternalEventDefPb,
                io.littlehorse.common.proto.GetExternalEventDefReplyPb>(
                  this, METHODID_GET_EXTERNAL_EVENT_DEF)))
          .addMethod(
            getPutWfSpecMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.PutWfSpecPb,
                io.littlehorse.common.proto.PutWfSpecReplyPb>(
                  this, METHODID_PUT_WF_SPEC)))
          .addMethod(
            getGetWfSpecMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.GetWfSpecPb,
                io.littlehorse.common.proto.GetWfSpecReplyPb>(
                  this, METHODID_GET_WF_SPEC)))
          .addMethod(
            getRunWfMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.RunWfPb,
                io.littlehorse.common.proto.RunWfReplyPb>(
                  this, METHODID_RUN_WF)))
          .addMethod(
            getGetWfRunMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.GetWfRunPb,
                io.littlehorse.common.proto.GetWfRunReplyPb>(
                  this, METHODID_GET_WF_RUN)))
          .addMethod(
            getGetNodeRunMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.GetNodeRunPb,
                io.littlehorse.common.proto.GetNodeRunReplyPb>(
                  this, METHODID_GET_NODE_RUN)))
          .addMethod(
            getGetVariableMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.GetVariablePb,
                io.littlehorse.common.proto.GetVariableReplyPb>(
                  this, METHODID_GET_VARIABLE)))
          .addMethod(
            getPutExternalEventMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.PutExternalEventPb,
                io.littlehorse.common.proto.PutExternalEventReplyPb>(
                  this, METHODID_PUT_EXTERNAL_EVENT)))
          .addMethod(
            getGetExternalEventMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.GetExternalEventPb,
                io.littlehorse.common.proto.GetExternalEventReplyPb>(
                  this, METHODID_GET_EXTERNAL_EVENT)))
          .addMethod(
            getSearchWfRunMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.SearchWfRunPb,
                io.littlehorse.common.proto.SearchWfRunReplyPb>(
                  this, METHODID_SEARCH_WF_RUN)))
          .addMethod(
            getRegisterTaskWorkerMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                io.littlehorse.common.proto.RegisterTaskWorkerPb,
                io.littlehorse.common.proto.RegisterTaskWorkerReplyPb>(
                  this, METHODID_REGISTER_TASK_WORKER)))
          .addMethod(
            getPollTaskMethod(),
            io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
              new MethodHandlers<
                io.littlehorse.common.proto.PollTaskPb,
                io.littlehorse.common.proto.PollTaskReplyPb>(
                  this, METHODID_POLL_TASK)))
          .addMethod(
            getReportTaskMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.TaskResultEventPb,
                io.littlehorse.common.proto.ReportTaskReplyPb>(
                  this, METHODID_REPORT_TASK)))
          .addMethod(
            getStopWfRunMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.StopWfRunPb,
                io.littlehorse.common.proto.StopWfRunReplyPb>(
                  this, METHODID_STOP_WF_RUN)))
          .addMethod(
            getResumeWfRunMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.ResumeWfRunPb,
                io.littlehorse.common.proto.ResumeWfRunReplyPb>(
                  this, METHODID_RESUME_WF_RUN)))
          .addMethod(
            getDeleteWfRunMethod(),
            io.grpc.stub.ServerCalls.asyncUnaryCall(
              new MethodHandlers<
                io.littlehorse.common.proto.DeleteWfRunPb,
                io.littlehorse.common.proto.DeleteWfRunReplyPb>(
                  this, METHODID_DELETE_WF_RUN)))
          .build();
    }
  }

  /**
   */
  public static final class LHPublicApiStub extends io.grpc.stub.AbstractAsyncStub<LHPublicApiStub> {
    private LHPublicApiStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LHPublicApiStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LHPublicApiStub(channel, callOptions);
    }

    /**
     */
    public void putTaskDef(io.littlehorse.common.proto.PutTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTaskDef(io.littlehorse.common.proto.GetTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putExternalEventDef(io.littlehorse.common.proto.PutExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getExternalEventDef(io.littlehorse.common.proto.GetExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putWfSpec(io.littlehorse.common.proto.PutWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWfSpec(io.littlehorse.common.proto.GetWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void runWf(io.littlehorse.common.proto.RunWfPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RunWfReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWfRun(io.littlehorse.common.proto.GetWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNodeRun(io.littlehorse.common.proto.GetNodeRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetNodeRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getVariable(io.littlehorse.common.proto.GetVariablePb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetVariableReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putExternalEvent(io.littlehorse.common.proto.PutExternalEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getExternalEvent(io.littlehorse.common.proto.GetExternalEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchWfRun(io.littlehorse.common.proto.SearchWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.SearchWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RegisterTaskWorkerPb> registerTaskWorker(
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RegisterTaskWorkerReplyPb> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getRegisterTaskWorkerMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PollTaskPb> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PollTaskReplyPb> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getPollTaskMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void reportTask(io.littlehorse.common.proto.TaskResultEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.ReportTaskReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stopWfRun(io.littlehorse.common.proto.StopWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.StopWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void resumeWfRun(io.littlehorse.common.proto.ResumeWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.ResumeWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteWfRun(io.littlehorse.common.proto.DeleteWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.common.proto.DeleteWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   */
  public static final class LHPublicApiBlockingStub extends io.grpc.stub.AbstractBlockingStub<LHPublicApiBlockingStub> {
    private LHPublicApiBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LHPublicApiBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LHPublicApiBlockingStub(channel, callOptions);
    }

    /**
     */
    public io.littlehorse.common.proto.PutTaskDefReplyPb putTaskDef(io.littlehorse.common.proto.PutTaskDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.GetTaskDefReplyPb getTaskDef(io.littlehorse.common.proto.GetTaskDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.PutExternalEventDefReplyPb putExternalEventDef(io.littlehorse.common.proto.PutExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.GetExternalEventDefReplyPb getExternalEventDef(io.littlehorse.common.proto.GetExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.PutWfSpecReplyPb putWfSpec(io.littlehorse.common.proto.PutWfSpecPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.GetWfSpecReplyPb getWfSpec(io.littlehorse.common.proto.GetWfSpecPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.RunWfReplyPb runWf(io.littlehorse.common.proto.RunWfPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRunWfMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.GetWfRunReplyPb getWfRun(io.littlehorse.common.proto.GetWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.GetNodeRunReplyPb getNodeRun(io.littlehorse.common.proto.GetNodeRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetNodeRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.GetVariableReplyPb getVariable(io.littlehorse.common.proto.GetVariablePb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetVariableMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.PutExternalEventReplyPb putExternalEvent(io.littlehorse.common.proto.PutExternalEventPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.GetExternalEventReplyPb getExternalEvent(io.littlehorse.common.proto.GetExternalEventPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.SearchWfRunReplyPb searchWfRun(io.littlehorse.common.proto.SearchWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.ReportTaskReplyPb reportTask(io.littlehorse.common.proto.TaskResultEventPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReportTaskMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.StopWfRunReplyPb stopWfRun(io.littlehorse.common.proto.StopWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStopWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.ResumeWfRunReplyPb resumeWfRun(io.littlehorse.common.proto.ResumeWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getResumeWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.common.proto.DeleteWfRunReplyPb deleteWfRun(io.littlehorse.common.proto.DeleteWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWfRunMethod(), getCallOptions(), request);
    }
  }

  /**
   */
  public static final class LHPublicApiFutureStub extends io.grpc.stub.AbstractFutureStub<LHPublicApiFutureStub> {
    private LHPublicApiFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LHPublicApiFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LHPublicApiFutureStub(channel, callOptions);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.PutTaskDefReplyPb> putTaskDef(
        io.littlehorse.common.proto.PutTaskDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetTaskDefReplyPb> getTaskDef(
        io.littlehorse.common.proto.GetTaskDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.PutExternalEventDefReplyPb> putExternalEventDef(
        io.littlehorse.common.proto.PutExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetExternalEventDefReplyPb> getExternalEventDef(
        io.littlehorse.common.proto.GetExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.PutWfSpecReplyPb> putWfSpec(
        io.littlehorse.common.proto.PutWfSpecPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetWfSpecReplyPb> getWfSpec(
        io.littlehorse.common.proto.GetWfSpecPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.RunWfReplyPb> runWf(
        io.littlehorse.common.proto.RunWfPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetWfRunReplyPb> getWfRun(
        io.littlehorse.common.proto.GetWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetNodeRunReplyPb> getNodeRun(
        io.littlehorse.common.proto.GetNodeRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetVariableReplyPb> getVariable(
        io.littlehorse.common.proto.GetVariablePb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.PutExternalEventReplyPb> putExternalEvent(
        io.littlehorse.common.proto.PutExternalEventPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.GetExternalEventReplyPb> getExternalEvent(
        io.littlehorse.common.proto.GetExternalEventPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.SearchWfRunReplyPb> searchWfRun(
        io.littlehorse.common.proto.SearchWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.ReportTaskReplyPb> reportTask(
        io.littlehorse.common.proto.TaskResultEventPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.StopWfRunReplyPb> stopWfRun(
        io.littlehorse.common.proto.StopWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.ResumeWfRunReplyPb> resumeWfRun(
        io.littlehorse.common.proto.ResumeWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.common.proto.DeleteWfRunReplyPb> deleteWfRun(
        io.littlehorse.common.proto.DeleteWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUT_TASK_DEF = 0;
  private static final int METHODID_GET_TASK_DEF = 1;
  private static final int METHODID_PUT_EXTERNAL_EVENT_DEF = 2;
  private static final int METHODID_GET_EXTERNAL_EVENT_DEF = 3;
  private static final int METHODID_PUT_WF_SPEC = 4;
  private static final int METHODID_GET_WF_SPEC = 5;
  private static final int METHODID_RUN_WF = 6;
  private static final int METHODID_GET_WF_RUN = 7;
  private static final int METHODID_GET_NODE_RUN = 8;
  private static final int METHODID_GET_VARIABLE = 9;
  private static final int METHODID_PUT_EXTERNAL_EVENT = 10;
  private static final int METHODID_GET_EXTERNAL_EVENT = 11;
  private static final int METHODID_SEARCH_WF_RUN = 12;
  private static final int METHODID_REPORT_TASK = 13;
  private static final int METHODID_STOP_WF_RUN = 14;
  private static final int METHODID_RESUME_WF_RUN = 15;
  private static final int METHODID_DELETE_WF_RUN = 16;
  private static final int METHODID_REGISTER_TASK_WORKER = 17;
  private static final int METHODID_POLL_TASK = 18;

  private static final class MethodHandlers<Req, Resp> implements
      io.grpc.stub.ServerCalls.UnaryMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ServerStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.ClientStreamingMethod<Req, Resp>,
      io.grpc.stub.ServerCalls.BidiStreamingMethod<Req, Resp> {
    private final LHPublicApiImplBase serviceImpl;
    private final int methodId;

    MethodHandlers(LHPublicApiImplBase serviceImpl, int methodId) {
      this.serviceImpl = serviceImpl;
      this.methodId = methodId;
    }

    @java.lang.Override
    @java.lang.SuppressWarnings("unchecked")
    public void invoke(Req request, io.grpc.stub.StreamObserver<Resp> responseObserver) {
      switch (methodId) {
        case METHODID_PUT_TASK_DEF:
          serviceImpl.putTaskDef((io.littlehorse.common.proto.PutTaskDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutTaskDefReplyPb>) responseObserver);
          break;
        case METHODID_GET_TASK_DEF:
          serviceImpl.getTaskDef((io.littlehorse.common.proto.GetTaskDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetTaskDefReplyPb>) responseObserver);
          break;
        case METHODID_PUT_EXTERNAL_EVENT_DEF:
          serviceImpl.putExternalEventDef((io.littlehorse.common.proto.PutExternalEventDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutExternalEventDefReplyPb>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT_DEF:
          serviceImpl.getExternalEventDef((io.littlehorse.common.proto.GetExternalEventDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetExternalEventDefReplyPb>) responseObserver);
          break;
        case METHODID_PUT_WF_SPEC:
          serviceImpl.putWfSpec((io.littlehorse.common.proto.PutWfSpecPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutWfSpecReplyPb>) responseObserver);
          break;
        case METHODID_GET_WF_SPEC:
          serviceImpl.getWfSpec((io.littlehorse.common.proto.GetWfSpecPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetWfSpecReplyPb>) responseObserver);
          break;
        case METHODID_RUN_WF:
          serviceImpl.runWf((io.littlehorse.common.proto.RunWfPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RunWfReplyPb>) responseObserver);
          break;
        case METHODID_GET_WF_RUN:
          serviceImpl.getWfRun((io.littlehorse.common.proto.GetWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetWfRunReplyPb>) responseObserver);
          break;
        case METHODID_GET_NODE_RUN:
          serviceImpl.getNodeRun((io.littlehorse.common.proto.GetNodeRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetNodeRunReplyPb>) responseObserver);
          break;
        case METHODID_GET_VARIABLE:
          serviceImpl.getVariable((io.littlehorse.common.proto.GetVariablePb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetVariableReplyPb>) responseObserver);
          break;
        case METHODID_PUT_EXTERNAL_EVENT:
          serviceImpl.putExternalEvent((io.littlehorse.common.proto.PutExternalEventPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PutExternalEventReplyPb>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT:
          serviceImpl.getExternalEvent((io.littlehorse.common.proto.GetExternalEventPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.GetExternalEventReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_WF_RUN:
          serviceImpl.searchWfRun((io.littlehorse.common.proto.SearchWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.SearchWfRunReplyPb>) responseObserver);
          break;
        case METHODID_REPORT_TASK:
          serviceImpl.reportTask((io.littlehorse.common.proto.TaskResultEventPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.ReportTaskReplyPb>) responseObserver);
          break;
        case METHODID_STOP_WF_RUN:
          serviceImpl.stopWfRun((io.littlehorse.common.proto.StopWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.StopWfRunReplyPb>) responseObserver);
          break;
        case METHODID_RESUME_WF_RUN:
          serviceImpl.resumeWfRun((io.littlehorse.common.proto.ResumeWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.ResumeWfRunReplyPb>) responseObserver);
          break;
        case METHODID_DELETE_WF_RUN:
          serviceImpl.deleteWfRun((io.littlehorse.common.proto.DeleteWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.DeleteWfRunReplyPb>) responseObserver);
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
        case METHODID_REGISTER_TASK_WORKER:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.registerTaskWorker(
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.RegisterTaskWorkerReplyPb>) responseObserver);
        case METHODID_POLL_TASK:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.pollTask(
              (io.grpc.stub.StreamObserver<io.littlehorse.common.proto.PollTaskReplyPb>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  private static abstract class LHPublicApiBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LHPublicApiBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.littlehorse.common.proto.Service.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("LHPublicApi");
    }
  }

  private static final class LHPublicApiFileDescriptorSupplier
      extends LHPublicApiBaseDescriptorSupplier {
    LHPublicApiFileDescriptorSupplier() {}
  }

  private static final class LHPublicApiMethodDescriptorSupplier
      extends LHPublicApiBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final String methodName;

    LHPublicApiMethodDescriptorSupplier(String methodName) {
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
      synchronized (LHPublicApiGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LHPublicApiFileDescriptorSupplier())
              .addMethod(getPutTaskDefMethod())
              .addMethod(getGetTaskDefMethod())
              .addMethod(getPutExternalEventDefMethod())
              .addMethod(getGetExternalEventDefMethod())
              .addMethod(getPutWfSpecMethod())
              .addMethod(getGetWfSpecMethod())
              .addMethod(getRunWfMethod())
              .addMethod(getGetWfRunMethod())
              .addMethod(getGetNodeRunMethod())
              .addMethod(getGetVariableMethod())
              .addMethod(getPutExternalEventMethod())
              .addMethod(getGetExternalEventMethod())
              .addMethod(getSearchWfRunMethod())
              .addMethod(getRegisterTaskWorkerMethod())
              .addMethod(getPollTaskMethod())
              .addMethod(getReportTaskMethod())
              .addMethod(getStopWfRunMethod())
              .addMethod(getResumeWfRunMethod())
              .addMethod(getDeleteWfRunMethod())
              .build();
        }
      }
    }
    return result;
  }
}
