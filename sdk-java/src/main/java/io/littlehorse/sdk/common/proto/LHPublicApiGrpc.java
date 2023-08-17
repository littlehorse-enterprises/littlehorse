package io.littlehorse.sdk.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.54.0)",
    comments = "Source: service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LHPublicApiGrpc {

  private LHPublicApiGrpc() {}

  public static final String SERVICE_NAME = "littlehorse.LHPublicApi";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTaskDefRequest,
      io.littlehorse.sdk.common.proto.PutTaskDefResponse> getPutTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutTaskDef",
      requestType = io.littlehorse.sdk.common.proto.PutTaskDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.PutTaskDefResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTaskDefRequest,
      io.littlehorse.sdk.common.proto.PutTaskDefResponse> getPutTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTaskDefRequest, io.littlehorse.sdk.common.proto.PutTaskDefResponse> getPutTaskDefMethod;
    if ((getPutTaskDefMethod = LHPublicApiGrpc.getPutTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutTaskDefMethod = LHPublicApiGrpc.getPutTaskDefMethod) == null) {
          LHPublicApiGrpc.getPutTaskDefMethod = getPutTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutTaskDefRequest, io.littlehorse.sdk.common.proto.PutTaskDefResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutTaskDefResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutTaskDef"))
              .build();
        }
      }
    }
    return getPutTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefIdPb,
      io.littlehorse.sdk.common.proto.GetTaskDefReplyPb> getGetTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTaskDef",
      requestType = io.littlehorse.sdk.common.proto.TaskDefIdPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetTaskDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefIdPb,
      io.littlehorse.sdk.common.proto.GetTaskDefReplyPb> getGetTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefIdPb, io.littlehorse.sdk.common.proto.GetTaskDefReplyPb> getGetTaskDefMethod;
    if ((getGetTaskDefMethod = LHPublicApiGrpc.getGetTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetTaskDefMethod = LHPublicApiGrpc.getGetTaskDefMethod) == null) {
          LHPublicApiGrpc.getGetTaskDefMethod = getGetTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskDefIdPb, io.littlehorse.sdk.common.proto.GetTaskDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefIdPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetTaskDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetTaskDef"))
              .build();
        }
      }
    }
    return getGetTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest,
      io.littlehorse.sdk.common.proto.PutExternalEventDefResponse> getPutExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.PutExternalEventDefResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest,
      io.littlehorse.sdk.common.proto.PutExternalEventDefResponse> getPutExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest, io.littlehorse.sdk.common.proto.PutExternalEventDefResponse> getPutExternalEventDefMethod;
    if ((getPutExternalEventDefMethod = LHPublicApiGrpc.getPutExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutExternalEventDefMethod = LHPublicApiGrpc.getPutExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getPutExternalEventDefMethod = getPutExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest, io.littlehorse.sdk.common.proto.PutExternalEventDefResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventDefResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutExternalEventDef"))
              .build();
        }
      }
    }
    return getPutExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventDefIdPb,
      io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb> getGetExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.ExternalEventDefIdPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventDefIdPb,
      io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb> getGetExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventDefIdPb, io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb> getGetExternalEventDefMethod;
    if ((getGetExternalEventDefMethod = LHPublicApiGrpc.getGetExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetExternalEventDefMethod = LHPublicApiGrpc.getGetExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getGetExternalEventDefMethod = getGetExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ExternalEventDefIdPb, io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDefIdPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetExternalEventDef"))
              .build();
        }
      }
    }
    return getGetExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWfSpecRequest,
      io.littlehorse.sdk.common.proto.PutWfSpecResponse> getPutWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutWfSpec",
      requestType = io.littlehorse.sdk.common.proto.PutWfSpecRequest.class,
      responseType = io.littlehorse.sdk.common.proto.PutWfSpecResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWfSpecRequest,
      io.littlehorse.sdk.common.proto.PutWfSpecResponse> getPutWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWfSpecRequest, io.littlehorse.sdk.common.proto.PutWfSpecResponse> getPutWfSpecMethod;
    if ((getPutWfSpecMethod = LHPublicApiGrpc.getPutWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutWfSpecMethod = LHPublicApiGrpc.getPutWfSpecMethod) == null) {
          LHPublicApiGrpc.getPutWfSpecMethod = getPutWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutWfSpecRequest, io.littlehorse.sdk.common.proto.PutWfSpecResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutWfSpecResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutWfSpec"))
              .build();
        }
      }
    }
    return getPutWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecIdPb,
      io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getGetWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWfSpec",
      requestType = io.littlehorse.sdk.common.proto.WfSpecIdPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetWfSpecReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecIdPb,
      io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getGetWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecIdPb, io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getGetWfSpecMethod;
    if ((getGetWfSpecMethod = LHPublicApiGrpc.getGetWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetWfSpecMethod = LHPublicApiGrpc.getGetWfSpecMethod) == null) {
          LHPublicApiGrpc.getGetWfSpecMethod = getGetWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfSpecIdPb, io.littlehorse.sdk.common.proto.GetWfSpecReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecIdPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetWfSpecReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetWfSpec"))
              .build();
        }
      }
    }
    return getGetWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestWfSpecPb,
      io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getGetLatestWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetLatestWfSpec",
      requestType = io.littlehorse.sdk.common.proto.GetLatestWfSpecPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetWfSpecReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestWfSpecPb,
      io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getGetLatestWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestWfSpecPb, io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getGetLatestWfSpecMethod;
    if ((getGetLatestWfSpecMethod = LHPublicApiGrpc.getGetLatestWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetLatestWfSpecMethod = LHPublicApiGrpc.getGetLatestWfSpecMethod) == null) {
          LHPublicApiGrpc.getGetLatestWfSpecMethod = getGetLatestWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.GetLatestWfSpecPb, io.littlehorse.sdk.common.proto.GetWfSpecReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLatestWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetLatestWfSpecPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetWfSpecReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetLatestWfSpec"))
              .build();
        }
      }
    }
    return getGetLatestWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.PutUserTaskDefResponse> getPutUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.PutUserTaskDefResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.PutUserTaskDefResponse> getPutUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest, io.littlehorse.sdk.common.proto.PutUserTaskDefResponse> getPutUserTaskDefMethod;
    if ((getPutUserTaskDefMethod = LHPublicApiGrpc.getPutUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutUserTaskDefMethod = LHPublicApiGrpc.getPutUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getPutUserTaskDefMethod = getPutUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest, io.littlehorse.sdk.common.proto.PutUserTaskDefResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutUserTaskDefResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutUserTaskDef"))
              .build();
        }
      }
    }
    return getPutUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskDefId,
      io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getGetUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.UserTaskDefId.class,
      responseType = io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskDefId,
      io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getGetUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskDefId, io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getGetUserTaskDefMethod;
    if ((getGetUserTaskDefMethod = LHPublicApiGrpc.getGetUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetUserTaskDefMethod = LHPublicApiGrpc.getGetUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getGetUserTaskDefMethod = getGetUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.UserTaskDefId, io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetUserTaskDef"))
              .build();
        }
      }
    }
    return getGetUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb,
      io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getGetLatestUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetLatestUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb,
      io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getGetLatestUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb, io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getGetLatestUserTaskDefMethod;
    if ((getGetLatestUserTaskDefMethod = LHPublicApiGrpc.getGetLatestUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetLatestUserTaskDefMethod = LHPublicApiGrpc.getGetLatestUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getGetLatestUserTaskDefMethod = getGetLatestUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb, io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLatestUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetLatestUserTaskDef"))
              .build();
        }
      }
    }
    return getGetLatestUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskRunId,
      io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb> getGetUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.UserTaskRunId.class,
      responseType = io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskRunId,
      io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb> getGetUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb> getGetUserTaskRunMethod;
    if ((getGetUserTaskRunMethod = LHPublicApiGrpc.getGetUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetUserTaskRunMethod = LHPublicApiGrpc.getGetUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getGetUserTaskRunMethod = getGetUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetUserTaskRun"))
              .build();
        }
      }
    }
    return getGetUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RunWfPb,
      io.littlehorse.sdk.common.proto.RunWfReplyPb> getRunWfMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RunWf",
      requestType = io.littlehorse.sdk.common.proto.RunWfPb.class,
      responseType = io.littlehorse.sdk.common.proto.RunWfReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RunWfPb,
      io.littlehorse.sdk.common.proto.RunWfReplyPb> getRunWfMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RunWfPb, io.littlehorse.sdk.common.proto.RunWfReplyPb> getRunWfMethod;
    if ((getRunWfMethod = LHPublicApiGrpc.getRunWfMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getRunWfMethod = LHPublicApiGrpc.getRunWfMethod) == null) {
          LHPublicApiGrpc.getRunWfMethod = getRunWfMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.RunWfPb, io.littlehorse.sdk.common.proto.RunWfReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RunWf"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RunWfPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RunWfReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("RunWf"))
              .build();
        }
      }
    }
    return getRunWfMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfRunIdPb,
      io.littlehorse.sdk.common.proto.GetWfRunReplyPb> getGetWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWfRun",
      requestType = io.littlehorse.sdk.common.proto.WfRunIdPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfRunIdPb,
      io.littlehorse.sdk.common.proto.GetWfRunReplyPb> getGetWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfRunIdPb, io.littlehorse.sdk.common.proto.GetWfRunReplyPb> getGetWfRunMethod;
    if ((getGetWfRunMethod = LHPublicApiGrpc.getGetWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetWfRunMethod = LHPublicApiGrpc.getGetWfRunMethod) == null) {
          LHPublicApiGrpc.getGetWfRunMethod = getGetWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfRunIdPb, io.littlehorse.sdk.common.proto.GetWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRunIdPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetWfRun"))
              .build();
        }
      }
    }
    return getGetWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AssignUserTaskRunPb,
      io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb> getAssignUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AssignUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.AssignUserTaskRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AssignUserTaskRunPb,
      io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb> getAssignUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AssignUserTaskRunPb, io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb> getAssignUserTaskRunMethod;
    if ((getAssignUserTaskRunMethod = LHPublicApiGrpc.getAssignUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getAssignUserTaskRunMethod = LHPublicApiGrpc.getAssignUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getAssignUserTaskRunMethod = getAssignUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.AssignUserTaskRunPb, io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AssignUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.AssignUserTaskRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("AssignUserTaskRun"))
              .build();
        }
      }
    }
    return getAssignUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb,
      io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb> getCompleteUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CompleteUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb,
      io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb> getCompleteUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb, io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb> getCompleteUserTaskRunMethod;
    if ((getCompleteUserTaskRunMethod = LHPublicApiGrpc.getCompleteUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getCompleteUserTaskRunMethod = LHPublicApiGrpc.getCompleteUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getCompleteUserTaskRunMethod = getCompleteUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb, io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CompleteUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("CompleteUserTaskRun"))
              .build();
        }
      }
    }
    return getCompleteUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.NodeRunId,
      io.littlehorse.sdk.common.proto.GetNodeRunReplyPb> getGetNodeRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNodeRun",
      requestType = io.littlehorse.sdk.common.proto.NodeRunId.class,
      responseType = io.littlehorse.sdk.common.proto.GetNodeRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.NodeRunId,
      io.littlehorse.sdk.common.proto.GetNodeRunReplyPb> getGetNodeRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.GetNodeRunReplyPb> getGetNodeRunMethod;
    if ((getGetNodeRunMethod = LHPublicApiGrpc.getGetNodeRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetNodeRunMethod = LHPublicApiGrpc.getGetNodeRunMethod) == null) {
          LHPublicApiGrpc.getGetNodeRunMethod = getGetNodeRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.GetNodeRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNodeRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetNodeRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetNodeRun"))
              .build();
        }
      }
    }
    return getGetNodeRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListNodeRunsPb,
      io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb> getListNodeRunsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListNodeRuns",
      requestType = io.littlehorse.sdk.common.proto.ListNodeRunsPb.class,
      responseType = io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListNodeRunsPb,
      io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb> getListNodeRunsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListNodeRunsPb, io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb> getListNodeRunsMethod;
    if ((getListNodeRunsMethod = LHPublicApiGrpc.getListNodeRunsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListNodeRunsMethod = LHPublicApiGrpc.getListNodeRunsMethod) == null) {
          LHPublicApiGrpc.getListNodeRunsMethod = getListNodeRunsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListNodeRunsPb, io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListNodeRuns"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListNodeRunsPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListNodeRuns"))
              .build();
        }
      }
    }
    return getListNodeRunsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskRunIdPb,
      io.littlehorse.sdk.common.proto.GetTaskRunReplyPb> getGetTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTaskRun",
      requestType = io.littlehorse.sdk.common.proto.TaskRunIdPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetTaskRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskRunIdPb,
      io.littlehorse.sdk.common.proto.GetTaskRunReplyPb> getGetTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskRunIdPb, io.littlehorse.sdk.common.proto.GetTaskRunReplyPb> getGetTaskRunMethod;
    if ((getGetTaskRunMethod = LHPublicApiGrpc.getGetTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetTaskRunMethod = LHPublicApiGrpc.getGetTaskRunMethod) == null) {
          LHPublicApiGrpc.getGetTaskRunMethod = getGetTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskRunIdPb, io.littlehorse.sdk.common.proto.GetTaskRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRunIdPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetTaskRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetTaskRun"))
              .build();
        }
      }
    }
    return getGetTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.VariableIdPb,
      io.littlehorse.sdk.common.proto.GetVariableReplyPb> getGetVariableMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetVariable",
      requestType = io.littlehorse.sdk.common.proto.VariableIdPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetVariableReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.VariableIdPb,
      io.littlehorse.sdk.common.proto.GetVariableReplyPb> getGetVariableMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.VariableIdPb, io.littlehorse.sdk.common.proto.GetVariableReplyPb> getGetVariableMethod;
    if ((getGetVariableMethod = LHPublicApiGrpc.getGetVariableMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetVariableMethod = LHPublicApiGrpc.getGetVariableMethod) == null) {
          LHPublicApiGrpc.getGetVariableMethod = getGetVariableMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.VariableIdPb, io.littlehorse.sdk.common.proto.GetVariableReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetVariable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.VariableIdPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetVariableReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetVariable"))
              .build();
        }
      }
    }
    return getGetVariableMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListVariablesPb,
      io.littlehorse.sdk.common.proto.ListVariablesReplyPb> getListVariablesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListVariables",
      requestType = io.littlehorse.sdk.common.proto.ListVariablesPb.class,
      responseType = io.littlehorse.sdk.common.proto.ListVariablesReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListVariablesPb,
      io.littlehorse.sdk.common.proto.ListVariablesReplyPb> getListVariablesMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListVariablesPb, io.littlehorse.sdk.common.proto.ListVariablesReplyPb> getListVariablesMethod;
    if ((getListVariablesMethod = LHPublicApiGrpc.getListVariablesMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListVariablesMethod = LHPublicApiGrpc.getListVariablesMethod) == null) {
          LHPublicApiGrpc.getListVariablesMethod = getListVariablesMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListVariablesPb, io.littlehorse.sdk.common.proto.ListVariablesReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListVariables"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListVariablesPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListVariablesReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListVariables"))
              .build();
        }
      }
    }
    return getListVariablesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventRequest,
      io.littlehorse.sdk.common.proto.PutExternalEventResponse> getPutExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutExternalEvent",
      requestType = io.littlehorse.sdk.common.proto.PutExternalEventRequest.class,
      responseType = io.littlehorse.sdk.common.proto.PutExternalEventResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventRequest,
      io.littlehorse.sdk.common.proto.PutExternalEventResponse> getPutExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventRequest, io.littlehorse.sdk.common.proto.PutExternalEventResponse> getPutExternalEventMethod;
    if ((getPutExternalEventMethod = LHPublicApiGrpc.getPutExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutExternalEventMethod = LHPublicApiGrpc.getPutExternalEventMethod) == null) {
          LHPublicApiGrpc.getPutExternalEventMethod = getPutExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutExternalEventRequest, io.littlehorse.sdk.common.proto.PutExternalEventResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutExternalEvent"))
              .build();
        }
      }
    }
    return getPutExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventIdPb,
      io.littlehorse.sdk.common.proto.GetExternalEventReplyPb> getGetExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetExternalEvent",
      requestType = io.littlehorse.sdk.common.proto.ExternalEventIdPb.class,
      responseType = io.littlehorse.sdk.common.proto.GetExternalEventReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventIdPb,
      io.littlehorse.sdk.common.proto.GetExternalEventReplyPb> getGetExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventIdPb, io.littlehorse.sdk.common.proto.GetExternalEventReplyPb> getGetExternalEventMethod;
    if ((getGetExternalEventMethod = LHPublicApiGrpc.getGetExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetExternalEventMethod = LHPublicApiGrpc.getGetExternalEventMethod) == null) {
          LHPublicApiGrpc.getGetExternalEventMethod = getGetExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ExternalEventIdPb, io.littlehorse.sdk.common.proto.GetExternalEventReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventIdPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetExternalEventReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetExternalEvent"))
              .build();
        }
      }
    }
    return getGetExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListExternalEventsPb,
      io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb> getListExternalEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListExternalEvents",
      requestType = io.littlehorse.sdk.common.proto.ListExternalEventsPb.class,
      responseType = io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListExternalEventsPb,
      io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb> getListExternalEventsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListExternalEventsPb, io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb> getListExternalEventsMethod;
    if ((getListExternalEventsMethod = LHPublicApiGrpc.getListExternalEventsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListExternalEventsMethod = LHPublicApiGrpc.getListExternalEventsMethod) == null) {
          LHPublicApiGrpc.getListExternalEventsMethod = getListExternalEventsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListExternalEventsPb, io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListExternalEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListExternalEventsPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListExternalEvents"))
              .build();
        }
      }
    }
    return getListExternalEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfRunPb,
      io.littlehorse.sdk.common.proto.SearchWfRunReplyPb> getSearchWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchWfRun",
      requestType = io.littlehorse.sdk.common.proto.SearchWfRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfRunPb,
      io.littlehorse.sdk.common.proto.SearchWfRunReplyPb> getSearchWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfRunPb, io.littlehorse.sdk.common.proto.SearchWfRunReplyPb> getSearchWfRunMethod;
    if ((getSearchWfRunMethod = LHPublicApiGrpc.getSearchWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchWfRunMethod = LHPublicApiGrpc.getSearchWfRunMethod) == null) {
          LHPublicApiGrpc.getSearchWfRunMethod = getSearchWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWfRunPb, io.littlehorse.sdk.common.proto.SearchWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchWfRun"))
              .build();
        }
      }
    }
    return getSearchWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchNodeRunPb,
      io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb> getSearchNodeRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchNodeRun",
      requestType = io.littlehorse.sdk.common.proto.SearchNodeRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchNodeRunPb,
      io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb> getSearchNodeRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchNodeRunPb, io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb> getSearchNodeRunMethod;
    if ((getSearchNodeRunMethod = LHPublicApiGrpc.getSearchNodeRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchNodeRunMethod = LHPublicApiGrpc.getSearchNodeRunMethod) == null) {
          LHPublicApiGrpc.getSearchNodeRunMethod = getSearchNodeRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchNodeRunPb, io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchNodeRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchNodeRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchNodeRun"))
              .build();
        }
      }
    }
    return getSearchNodeRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskRunPb,
      io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb> getSearchTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTaskRun",
      requestType = io.littlehorse.sdk.common.proto.SearchTaskRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskRunPb,
      io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb> getSearchTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskRunPb, io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb> getSearchTaskRunMethod;
    if ((getSearchTaskRunMethod = LHPublicApiGrpc.getSearchTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchTaskRunMethod = LHPublicApiGrpc.getSearchTaskRunMethod) == null) {
          LHPublicApiGrpc.getSearchTaskRunMethod = getSearchTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchTaskRunPb, io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchTaskRun"))
              .build();
        }
      }
    }
    return getSearchTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskRunPb,
      io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb> getSearchUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.SearchUserTaskRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskRunPb,
      io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb> getSearchUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskRunPb, io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb> getSearchUserTaskRunMethod;
    if ((getSearchUserTaskRunMethod = LHPublicApiGrpc.getSearchUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchUserTaskRunMethod = LHPublicApiGrpc.getSearchUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getSearchUserTaskRunMethod = getSearchUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchUserTaskRunPb, io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchUserTaskRun"))
              .build();
        }
      }
    }
    return getSearchUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchVariablePb,
      io.littlehorse.sdk.common.proto.SearchVariableReplyPb> getSearchVariableMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchVariable",
      requestType = io.littlehorse.sdk.common.proto.SearchVariablePb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchVariableReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchVariablePb,
      io.littlehorse.sdk.common.proto.SearchVariableReplyPb> getSearchVariableMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchVariablePb, io.littlehorse.sdk.common.proto.SearchVariableReplyPb> getSearchVariableMethod;
    if ((getSearchVariableMethod = LHPublicApiGrpc.getSearchVariableMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchVariableMethod = LHPublicApiGrpc.getSearchVariableMethod) == null) {
          LHPublicApiGrpc.getSearchVariableMethod = getSearchVariableMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchVariablePb, io.littlehorse.sdk.common.proto.SearchVariableReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchVariable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchVariablePb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchVariableReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchVariable"))
              .build();
        }
      }
    }
    return getSearchVariableMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskDefPb,
      io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb> getSearchTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTaskDef",
      requestType = io.littlehorse.sdk.common.proto.SearchTaskDefPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskDefPb,
      io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb> getSearchTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskDefPb, io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb> getSearchTaskDefMethod;
    if ((getSearchTaskDefMethod = LHPublicApiGrpc.getSearchTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchTaskDefMethod = LHPublicApiGrpc.getSearchTaskDefMethod) == null) {
          LHPublicApiGrpc.getSearchTaskDefMethod = getSearchTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchTaskDefPb, io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchTaskDef"))
              .build();
        }
      }
    }
    return getSearchTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskDefPb,
      io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb> getSearchUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.SearchUserTaskDefPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskDefPb,
      io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb> getSearchUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskDefPb, io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb> getSearchUserTaskDefMethod;
    if ((getSearchUserTaskDefMethod = LHPublicApiGrpc.getSearchUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchUserTaskDefMethod = LHPublicApiGrpc.getSearchUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getSearchUserTaskDefMethod = getSearchUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchUserTaskDefPb, io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchUserTaskDef"))
              .build();
        }
      }
    }
    return getSearchUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfSpecPb,
      io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb> getSearchWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchWfSpec",
      requestType = io.littlehorse.sdk.common.proto.SearchWfSpecPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfSpecPb,
      io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb> getSearchWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfSpecPb, io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb> getSearchWfSpecMethod;
    if ((getSearchWfSpecMethod = LHPublicApiGrpc.getSearchWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchWfSpecMethod = LHPublicApiGrpc.getSearchWfSpecMethod) == null) {
          LHPublicApiGrpc.getSearchWfSpecMethod = getSearchWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWfSpecPb, io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfSpecPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchWfSpec"))
              .build();
        }
      }
    }
    return getSearchWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventDefPb,
      io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb> getSearchExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.SearchExternalEventDefPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventDefPb,
      io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb> getSearchExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventDefPb, io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb> getSearchExternalEventDefMethod;
    if ((getSearchExternalEventDefMethod = LHPublicApiGrpc.getSearchExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchExternalEventDefMethod = LHPublicApiGrpc.getSearchExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getSearchExternalEventDefMethod = getSearchExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchExternalEventDefPb, io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchExternalEventDef"))
              .build();
        }
      }
    }
    return getSearchExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventPb,
      io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb> getSearchExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchExternalEvent",
      requestType = io.littlehorse.sdk.common.proto.SearchExternalEventPb.class,
      responseType = io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventPb,
      io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb> getSearchExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventPb, io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb> getSearchExternalEventMethod;
    if ((getSearchExternalEventMethod = LHPublicApiGrpc.getSearchExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchExternalEventMethod = LHPublicApiGrpc.getSearchExternalEventMethod) == null) {
          LHPublicApiGrpc.getSearchExternalEventMethod = getSearchExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchExternalEventPb, io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchExternalEvent"))
              .build();
        }
      }
    }
    return getSearchExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb,
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb> getRegisterTaskWorkerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterTaskWorker",
      requestType = io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb.class,
      responseType = io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb,
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb> getRegisterTaskWorkerMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb, io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb> getRegisterTaskWorkerMethod;
    if ((getRegisterTaskWorkerMethod = LHPublicApiGrpc.getRegisterTaskWorkerMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getRegisterTaskWorkerMethod = LHPublicApiGrpc.getRegisterTaskWorkerMethod) == null) {
          LHPublicApiGrpc.getRegisterTaskWorkerMethod = getRegisterTaskWorkerMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb, io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterTaskWorker"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("RegisterTaskWorker"))
              .build();
        }
      }
    }
    return getRegisterTaskWorkerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PollTaskPb,
      io.littlehorse.sdk.common.proto.PollTaskReplyPb> getPollTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PollTask",
      requestType = io.littlehorse.sdk.common.proto.PollTaskPb.class,
      responseType = io.littlehorse.sdk.common.proto.PollTaskReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PollTaskPb,
      io.littlehorse.sdk.common.proto.PollTaskReplyPb> getPollTaskMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PollTaskPb, io.littlehorse.sdk.common.proto.PollTaskReplyPb> getPollTaskMethod;
    if ((getPollTaskMethod = LHPublicApiGrpc.getPollTaskMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPollTaskMethod = LHPublicApiGrpc.getPollTaskMethod) == null) {
          LHPublicApiGrpc.getPollTaskMethod = getPollTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PollTaskPb, io.littlehorse.sdk.common.proto.PollTaskReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PollTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PollTaskPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PollTaskReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PollTask"))
              .build();
        }
      }
    }
    return getPollTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ReportTaskRunPb,
      io.littlehorse.sdk.common.proto.ReportTaskReplyPb> getReportTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReportTask",
      requestType = io.littlehorse.sdk.common.proto.ReportTaskRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.ReportTaskReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ReportTaskRunPb,
      io.littlehorse.sdk.common.proto.ReportTaskReplyPb> getReportTaskMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ReportTaskRunPb, io.littlehorse.sdk.common.proto.ReportTaskReplyPb> getReportTaskMethod;
    if ((getReportTaskMethod = LHPublicApiGrpc.getReportTaskMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getReportTaskMethod = LHPublicApiGrpc.getReportTaskMethod) == null) {
          LHPublicApiGrpc.getReportTaskMethod = getReportTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ReportTaskRunPb, io.littlehorse.sdk.common.proto.ReportTaskReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReportTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ReportTaskRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ReportTaskReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ReportTask"))
              .build();
        }
      }
    }
    return getReportTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StopWfRunPb,
      io.littlehorse.sdk.common.proto.StopWfRunReplyPb> getStopWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StopWfRun",
      requestType = io.littlehorse.sdk.common.proto.StopWfRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.StopWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StopWfRunPb,
      io.littlehorse.sdk.common.proto.StopWfRunReplyPb> getStopWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StopWfRunPb, io.littlehorse.sdk.common.proto.StopWfRunReplyPb> getStopWfRunMethod;
    if ((getStopWfRunMethod = LHPublicApiGrpc.getStopWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getStopWfRunMethod = LHPublicApiGrpc.getStopWfRunMethod) == null) {
          LHPublicApiGrpc.getStopWfRunMethod = getStopWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.StopWfRunPb, io.littlehorse.sdk.common.proto.StopWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StopWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.StopWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.StopWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("StopWfRun"))
              .build();
        }
      }
    }
    return getStopWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ResumeWfRunPb,
      io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb> getResumeWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ResumeWfRun",
      requestType = io.littlehorse.sdk.common.proto.ResumeWfRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ResumeWfRunPb,
      io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb> getResumeWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ResumeWfRunPb, io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb> getResumeWfRunMethod;
    if ((getResumeWfRunMethod = LHPublicApiGrpc.getResumeWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getResumeWfRunMethod = LHPublicApiGrpc.getResumeWfRunMethod) == null) {
          LHPublicApiGrpc.getResumeWfRunMethod = getResumeWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ResumeWfRunPb, io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ResumeWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ResumeWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ResumeWfRun"))
              .build();
        }
      }
    }
    return getResumeWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfRunPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteWfRun",
      requestType = io.littlehorse.sdk.common.proto.DeleteWfRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfRunPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfRunPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteWfRunMethod;
    if ((getDeleteWfRunMethod = LHPublicApiGrpc.getDeleteWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteWfRunMethod = LHPublicApiGrpc.getDeleteWfRunMethod) == null) {
          LHPublicApiGrpc.getDeleteWfRunMethod = getDeleteWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteWfRunPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteWfRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteWfRun"))
              .build();
        }
      }
    }
    return getDeleteWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteTaskDefPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteTaskDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteTaskDefPb.class,
      responseType = io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteTaskDefPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteTaskDefPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteTaskDefMethod;
    if ((getDeleteTaskDefMethod = LHPublicApiGrpc.getDeleteTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteTaskDefMethod = LHPublicApiGrpc.getDeleteTaskDefMethod) == null) {
          LHPublicApiGrpc.getDeleteTaskDefMethod = getDeleteTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteTaskDefPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteTaskDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteTaskDef"))
              .build();
        }
      }
    }
    return getDeleteTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfSpecPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteWfSpec",
      requestType = io.littlehorse.sdk.common.proto.DeleteWfSpecPb.class,
      responseType = io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfSpecPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfSpecPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteWfSpecMethod;
    if ((getDeleteWfSpecMethod = LHPublicApiGrpc.getDeleteWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteWfSpecMethod = LHPublicApiGrpc.getDeleteWfSpecMethod) == null) {
          LHPublicApiGrpc.getDeleteWfSpecMethod = getDeleteWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteWfSpecPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteWfSpecPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteWfSpec"))
              .build();
        }
      }
    }
    return getDeleteWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb.class,
      responseType = io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteUserTaskDefMethod;
    if ((getDeleteUserTaskDefMethod = LHPublicApiGrpc.getDeleteUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteUserTaskDefMethod = LHPublicApiGrpc.getDeleteUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getDeleteUserTaskDefMethod = getDeleteUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteUserTaskDef"))
              .build();
        }
      }
    }
    return getDeleteUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb.class,
      responseType = io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb,
      io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> getDeleteExternalEventDefMethod;
    if ((getDeleteExternalEventDefMethod = LHPublicApiGrpc.getDeleteExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteExternalEventDefMethod = LHPublicApiGrpc.getDeleteExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getDeleteExternalEventDefMethod = getDeleteExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb, io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteObjectReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteExternalEventDef"))
              .build();
        }
      }
    }
    return getDeleteExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CancelUserTaskRunPb,
      io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb> getCancelUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CancelUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.CancelUserTaskRunPb.class,
      responseType = io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CancelUserTaskRunPb,
      io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb> getCancelUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CancelUserTaskRunPb, io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb> getCancelUserTaskRunMethod;
    if ((getCancelUserTaskRunMethod = LHPublicApiGrpc.getCancelUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getCancelUserTaskRunMethod = LHPublicApiGrpc.getCancelUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getCancelUserTaskRunMethod = getCancelUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.CancelUserTaskRunPb, io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CancelUserTaskRunPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("CancelUserTaskRun"))
              .build();
        }
      }
    }
    return getCancelUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.HealthCheckPb,
      io.littlehorse.sdk.common.proto.HealthCheckReplyPb> getHealthCheckMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "HealthCheck",
      requestType = io.littlehorse.sdk.common.proto.HealthCheckPb.class,
      responseType = io.littlehorse.sdk.common.proto.HealthCheckReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.HealthCheckPb,
      io.littlehorse.sdk.common.proto.HealthCheckReplyPb> getHealthCheckMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.HealthCheckPb, io.littlehorse.sdk.common.proto.HealthCheckReplyPb> getHealthCheckMethod;
    if ((getHealthCheckMethod = LHPublicApiGrpc.getHealthCheckMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getHealthCheckMethod = LHPublicApiGrpc.getHealthCheckMethod) == null) {
          LHPublicApiGrpc.getHealthCheckMethod = getHealthCheckMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.HealthCheckPb, io.littlehorse.sdk.common.proto.HealthCheckReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "HealthCheck"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.HealthCheckPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.HealthCheckReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("HealthCheck"))
              .build();
        }
      }
    }
    return getHealthCheckMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb,
      io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb> getTaskDefMetricsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "TaskDefMetrics",
      requestType = io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb.class,
      responseType = io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb,
      io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb> getTaskDefMetricsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb, io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb> getTaskDefMetricsMethod;
    if ((getTaskDefMetricsMethod = LHPublicApiGrpc.getTaskDefMetricsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getTaskDefMetricsMethod = LHPublicApiGrpc.getTaskDefMetricsMethod) == null) {
          LHPublicApiGrpc.getTaskDefMetricsMethod = getTaskDefMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb, io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "TaskDefMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("TaskDefMetrics"))
              .build();
        }
      }
    }
    return getTaskDefMetricsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb,
      io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb> getWfSpecMetricsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "WfSpecMetrics",
      requestType = io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.class,
      responseType = io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb,
      io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb> getWfSpecMetricsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb, io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb> getWfSpecMetricsMethod;
    if ((getWfSpecMetricsMethod = LHPublicApiGrpc.getWfSpecMetricsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getWfSpecMetricsMethod = LHPublicApiGrpc.getWfSpecMetricsMethod) == null) {
          LHPublicApiGrpc.getWfSpecMetricsMethod = getWfSpecMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb, io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "WfSpecMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("WfSpecMetrics"))
              .build();
        }
      }
    }
    return getWfSpecMetricsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskMetricsPb,
      io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb> getListTaskDefMetricsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListTaskDefMetrics",
      requestType = io.littlehorse.sdk.common.proto.ListTaskMetricsPb.class,
      responseType = io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskMetricsPb,
      io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb> getListTaskDefMetricsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskMetricsPb, io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb> getListTaskDefMetricsMethod;
    if ((getListTaskDefMetricsMethod = LHPublicApiGrpc.getListTaskDefMetricsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListTaskDefMetricsMethod = LHPublicApiGrpc.getListTaskDefMetricsMethod) == null) {
          LHPublicApiGrpc.getListTaskDefMetricsMethod = getListTaskDefMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListTaskMetricsPb, io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTaskDefMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListTaskMetricsPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListTaskDefMetrics"))
              .build();
        }
      }
    }
    return getListTaskDefMetricsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWfMetricsPb,
      io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb> getListWfSpecMetricsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListWfSpecMetrics",
      requestType = io.littlehorse.sdk.common.proto.ListWfMetricsPb.class,
      responseType = io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWfMetricsPb,
      io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb> getListWfSpecMetricsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWfMetricsPb, io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb> getListWfSpecMetricsMethod;
    if ((getListWfSpecMetricsMethod = LHPublicApiGrpc.getListWfSpecMetricsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListWfSpecMetricsMethod = LHPublicApiGrpc.getListWfSpecMetricsMethod) == null) {
          LHPublicApiGrpc.getListWfSpecMetricsMethod = getListWfSpecMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListWfMetricsPb, io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListWfSpecMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListWfMetricsPb.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListWfSpecMetrics"))
              .build();
        }
      }
    }
    return getListWfSpecMetricsMethod;
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
  public interface AsyncService {

    /**
     */
    default void putTaskDef(io.littlehorse.sdk.common.proto.PutTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutTaskDefResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void getTaskDef(io.littlehorse.sdk.common.proto.TaskDefIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutExternalEventDefResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutWfSpecResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void getWfSpec(io.littlehorse.sdk.common.proto.WfSpecIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetLatestWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutUserTaskDefResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetLatestUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void runWf(io.littlehorse.sdk.common.proto.RunWfPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RunWfReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRunWfMethod(), responseObserver);
    }

    /**
     */
    default void getWfRun(io.littlehorse.sdk.common.proto.WfRunIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfRunMethod(), responseObserver);
    }

    /**
     */
    default void assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAssignUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCompleteUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetNodeRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetNodeRunMethod(), responseObserver);
    }

    /**
     */
    default void listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListNodeRunsMethod(), responseObserver);
    }

    /**
     */
    default void getTaskRun(io.littlehorse.sdk.common.proto.TaskRunIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void getVariable(io.littlehorse.sdk.common.proto.VariableIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetVariableReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetVariableMethod(), responseObserver);
    }

    /**
     */
    default void listVariables(io.littlehorse.sdk.common.proto.ListVariablesPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListVariablesReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListVariablesMethod(), responseObserver);
    }

    /**
     */
    default void putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutExternalEventResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventMethod(), responseObserver);
    }

    /**
     */
    default void getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventMethod(), responseObserver);
    }

    /**
     */
    default void listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListExternalEventsMethod(), responseObserver);
    }

    /**
     */
    default void searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWfRunMethod(), responseObserver);
    }

    /**
     */
    default void searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchNodeRunMethod(), responseObserver);
    }

    /**
     */
    default void searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void searchVariable(io.littlehorse.sdk.common.proto.SearchVariablePb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchVariableReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchVariableMethod(), responseObserver);
    }

    /**
     */
    default void searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchExternalEventMethod(), responseObserver);
    }

    /**
     */
    default void registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterTaskWorkerMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskPb> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskReplyPb> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getPollTaskMethod(), responseObserver);
    }

    /**
     */
    default void reportTask(io.littlehorse.sdk.common.proto.ReportTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ReportTaskReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReportTaskMethod(), responseObserver);
    }

    /**
     */
    default void stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StopWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStopWfRunMethod(), responseObserver);
    }

    /**
     */
    default void resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getResumeWfRunMethod(), responseObserver);
    }

    /**
     */
    default void deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWfRunMethod(), responseObserver);
    }

    /**
     */
    default void deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void healthCheck(io.littlehorse.sdk.common.proto.HealthCheckPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.HealthCheckReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getHealthCheckMethod(), responseObserver);
    }

    /**
     */
    default void taskDefMetrics(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getTaskDefMetricsMethod(), responseObserver);
    }

    /**
     */
    default void wfSpecMetrics(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWfSpecMetricsMethod(), responseObserver);
    }

    /**
     */
    default void listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTaskDefMetricsMethod(), responseObserver);
    }

    /**
     */
    default void listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListWfSpecMetricsMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service LHPublicApi.
   */
  public static abstract class LHPublicApiImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return LHPublicApiGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service LHPublicApi.
   */
  public static final class LHPublicApiStub
      extends io.grpc.stub.AbstractAsyncStub<LHPublicApiStub> {
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
    public void putTaskDef(io.littlehorse.sdk.common.proto.PutTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutTaskDefResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTaskDef(io.littlehorse.sdk.common.proto.TaskDefIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutExternalEventDefResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutWfSpecResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWfSpec(io.littlehorse.sdk.common.proto.WfSpecIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetLatestWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutUserTaskDefResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetLatestUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void runWf(io.littlehorse.sdk.common.proto.RunWfPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RunWfReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWfRun(io.littlehorse.sdk.common.proto.WfRunIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAssignUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCompleteUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetNodeRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListNodeRunsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTaskRun(io.littlehorse.sdk.common.proto.TaskRunIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getVariable(io.littlehorse.sdk.common.proto.VariableIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetVariableReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listVariables(io.littlehorse.sdk.common.proto.ListVariablesPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListVariablesReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListVariablesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutExternalEventResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventIdPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListExternalEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchNodeRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchVariable(io.littlehorse.sdk.common.proto.SearchVariablePb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchVariableReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchVariableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterTaskWorkerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskPb> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskReplyPb> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getPollTaskMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void reportTask(io.littlehorse.sdk.common.proto.ReportTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ReportTaskReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StopWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void healthCheck(io.littlehorse.sdk.common.proto.HealthCheckPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.HealthCheckReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void taskDefMetrics(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getTaskDefMetricsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void wfSpecMetrics(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getWfSpecMetricsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTaskDefMetricsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsPb request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListWfSpecMetricsMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service LHPublicApi.
   */
  public static final class LHPublicApiBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<LHPublicApiBlockingStub> {
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
    public io.littlehorse.sdk.common.proto.PutTaskDefResponse putTaskDef(io.littlehorse.sdk.common.proto.PutTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetTaskDefReplyPb getTaskDef(io.littlehorse.sdk.common.proto.TaskDefIdPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.PutExternalEventDefResponse putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefIdPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.PutWfSpecResponse putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetWfSpecReplyPb getWfSpec(io.littlehorse.sdk.common.proto.WfSpecIdPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetWfSpecReplyPb getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetLatestWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.PutUserTaskDefResponse putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetLatestUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.RunWfReplyPb runWf(io.littlehorse.sdk.common.proto.RunWfPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRunWfMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetWfRunReplyPb getWfRun(io.littlehorse.sdk.common.proto.WfRunIdPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAssignUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCompleteUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetNodeRunReplyPb getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetNodeRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListNodeRunsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetTaskRunReplyPb getTaskRun(io.littlehorse.sdk.common.proto.TaskRunIdPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetVariableReplyPb getVariable(io.littlehorse.sdk.common.proto.VariableIdPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetVariableMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ListVariablesReplyPb listVariables(io.littlehorse.sdk.common.proto.ListVariablesPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListVariablesMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.PutExternalEventResponse putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.GetExternalEventReplyPb getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventIdPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListExternalEventsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchWfRunReplyPb searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchNodeRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchVariableReplyPb searchVariable(io.littlehorse.sdk.common.proto.SearchVariablePb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchVariableMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterTaskWorkerMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ReportTaskReplyPb reportTask(io.littlehorse.sdk.common.proto.ReportTaskRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReportTaskMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.StopWfRunReplyPb stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStopWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getResumeWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.DeleteObjectReplyPb deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.DeleteObjectReplyPb deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.DeleteObjectReplyPb deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.DeleteObjectReplyPb deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.DeleteObjectReplyPb deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.HealthCheckReplyPb healthCheck(io.littlehorse.sdk.common.proto.HealthCheckPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getHealthCheckMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb taskDefMetrics(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getTaskDefMetricsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb wfSpecMetrics(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getWfSpecMetricsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTaskDefMetricsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsPb request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListWfSpecMetricsMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service LHPublicApi.
   */
  public static final class LHPublicApiFutureStub
      extends io.grpc.stub.AbstractFutureStub<LHPublicApiFutureStub> {
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
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.PutTaskDefResponse> putTaskDef(
        io.littlehorse.sdk.common.proto.PutTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetTaskDefReplyPb> getTaskDef(
        io.littlehorse.sdk.common.proto.TaskDefIdPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.PutExternalEventDefResponse> putExternalEventDef(
        io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb> getExternalEventDef(
        io.littlehorse.sdk.common.proto.ExternalEventDefIdPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.PutWfSpecResponse> putWfSpec(
        io.littlehorse.sdk.common.proto.PutWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getWfSpec(
        io.littlehorse.sdk.common.proto.WfSpecIdPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb> getLatestWfSpec(
        io.littlehorse.sdk.common.proto.GetLatestWfSpecPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetLatestWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.PutUserTaskDefResponse> putUserTaskDef(
        io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getUserTaskDef(
        io.littlehorse.sdk.common.proto.UserTaskDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb> getLatestUserTaskDef(
        io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetLatestUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb> getUserTaskRun(
        io.littlehorse.sdk.common.proto.UserTaskRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.RunWfReplyPb> runWf(
        io.littlehorse.sdk.common.proto.RunWfPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetWfRunReplyPb> getWfRun(
        io.littlehorse.sdk.common.proto.WfRunIdPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb> assignUserTaskRun(
        io.littlehorse.sdk.common.proto.AssignUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAssignUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb> completeUserTaskRun(
        io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCompleteUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetNodeRunReplyPb> getNodeRun(
        io.littlehorse.sdk.common.proto.NodeRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb> listNodeRuns(
        io.littlehorse.sdk.common.proto.ListNodeRunsPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListNodeRunsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetTaskRunReplyPb> getTaskRun(
        io.littlehorse.sdk.common.proto.TaskRunIdPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetVariableReplyPb> getVariable(
        io.littlehorse.sdk.common.proto.VariableIdPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListVariablesReplyPb> listVariables(
        io.littlehorse.sdk.common.proto.ListVariablesPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListVariablesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.PutExternalEventResponse> putExternalEvent(
        io.littlehorse.sdk.common.proto.PutExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.GetExternalEventReplyPb> getExternalEvent(
        io.littlehorse.sdk.common.proto.ExternalEventIdPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb> listExternalEvents(
        io.littlehorse.sdk.common.proto.ListExternalEventsPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListExternalEventsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchWfRunReplyPb> searchWfRun(
        io.littlehorse.sdk.common.proto.SearchWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb> searchNodeRun(
        io.littlehorse.sdk.common.proto.SearchNodeRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchNodeRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb> searchTaskRun(
        io.littlehorse.sdk.common.proto.SearchTaskRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb> searchUserTaskRun(
        io.littlehorse.sdk.common.proto.SearchUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchVariableReplyPb> searchVariable(
        io.littlehorse.sdk.common.proto.SearchVariablePb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchVariableMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb> searchTaskDef(
        io.littlehorse.sdk.common.proto.SearchTaskDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb> searchUserTaskDef(
        io.littlehorse.sdk.common.proto.SearchUserTaskDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb> searchWfSpec(
        io.littlehorse.sdk.common.proto.SearchWfSpecPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb> searchExternalEventDef(
        io.littlehorse.sdk.common.proto.SearchExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb> searchExternalEvent(
        io.littlehorse.sdk.common.proto.SearchExternalEventPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb> registerTaskWorker(
        io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterTaskWorkerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ReportTaskReplyPb> reportTask(
        io.littlehorse.sdk.common.proto.ReportTaskRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.StopWfRunReplyPb> stopWfRun(
        io.littlehorse.sdk.common.proto.StopWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb> resumeWfRun(
        io.littlehorse.sdk.common.proto.ResumeWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> deleteWfRun(
        io.littlehorse.sdk.common.proto.DeleteWfRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> deleteTaskDef(
        io.littlehorse.sdk.common.proto.DeleteTaskDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> deleteWfSpec(
        io.littlehorse.sdk.common.proto.DeleteWfSpecPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> deleteUserTaskDef(
        io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb> deleteExternalEventDef(
        io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb> cancelUserTaskRun(
        io.littlehorse.sdk.common.proto.CancelUserTaskRunPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.HealthCheckReplyPb> healthCheck(
        io.littlehorse.sdk.common.proto.HealthCheckPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getHealthCheckMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb> taskDefMetrics(
        io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getTaskDefMetricsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb> wfSpecMetrics(
        io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getWfSpecMetricsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb> listTaskDefMetrics(
        io.littlehorse.sdk.common.proto.ListTaskMetricsPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTaskDefMetricsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb> listWfSpecMetrics(
        io.littlehorse.sdk.common.proto.ListWfMetricsPb request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListWfSpecMetricsMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUT_TASK_DEF = 0;
  private static final int METHODID_GET_TASK_DEF = 1;
  private static final int METHODID_PUT_EXTERNAL_EVENT_DEF = 2;
  private static final int METHODID_GET_EXTERNAL_EVENT_DEF = 3;
  private static final int METHODID_PUT_WF_SPEC = 4;
  private static final int METHODID_GET_WF_SPEC = 5;
  private static final int METHODID_GET_LATEST_WF_SPEC = 6;
  private static final int METHODID_PUT_USER_TASK_DEF = 7;
  private static final int METHODID_GET_USER_TASK_DEF = 8;
  private static final int METHODID_GET_LATEST_USER_TASK_DEF = 9;
  private static final int METHODID_GET_USER_TASK_RUN = 10;
  private static final int METHODID_RUN_WF = 11;
  private static final int METHODID_GET_WF_RUN = 12;
  private static final int METHODID_ASSIGN_USER_TASK_RUN = 13;
  private static final int METHODID_COMPLETE_USER_TASK_RUN = 14;
  private static final int METHODID_GET_NODE_RUN = 15;
  private static final int METHODID_LIST_NODE_RUNS = 16;
  private static final int METHODID_GET_TASK_RUN = 17;
  private static final int METHODID_GET_VARIABLE = 18;
  private static final int METHODID_LIST_VARIABLES = 19;
  private static final int METHODID_PUT_EXTERNAL_EVENT = 20;
  private static final int METHODID_GET_EXTERNAL_EVENT = 21;
  private static final int METHODID_LIST_EXTERNAL_EVENTS = 22;
  private static final int METHODID_SEARCH_WF_RUN = 23;
  private static final int METHODID_SEARCH_NODE_RUN = 24;
  private static final int METHODID_SEARCH_TASK_RUN = 25;
  private static final int METHODID_SEARCH_USER_TASK_RUN = 26;
  private static final int METHODID_SEARCH_VARIABLE = 27;
  private static final int METHODID_SEARCH_TASK_DEF = 28;
  private static final int METHODID_SEARCH_USER_TASK_DEF = 29;
  private static final int METHODID_SEARCH_WF_SPEC = 30;
  private static final int METHODID_SEARCH_EXTERNAL_EVENT_DEF = 31;
  private static final int METHODID_SEARCH_EXTERNAL_EVENT = 32;
  private static final int METHODID_REGISTER_TASK_WORKER = 33;
  private static final int METHODID_REPORT_TASK = 34;
  private static final int METHODID_STOP_WF_RUN = 35;
  private static final int METHODID_RESUME_WF_RUN = 36;
  private static final int METHODID_DELETE_WF_RUN = 37;
  private static final int METHODID_DELETE_TASK_DEF = 38;
  private static final int METHODID_DELETE_WF_SPEC = 39;
  private static final int METHODID_DELETE_USER_TASK_DEF = 40;
  private static final int METHODID_DELETE_EXTERNAL_EVENT_DEF = 41;
  private static final int METHODID_CANCEL_USER_TASK_RUN = 42;
  private static final int METHODID_HEALTH_CHECK = 43;
  private static final int METHODID_TASK_DEF_METRICS = 44;
  private static final int METHODID_WF_SPEC_METRICS = 45;
  private static final int METHODID_LIST_TASK_DEF_METRICS = 46;
  private static final int METHODID_LIST_WF_SPEC_METRICS = 47;
  private static final int METHODID_POLL_TASK = 48;

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
        case METHODID_PUT_TASK_DEF:
          serviceImpl.putTaskDef((io.littlehorse.sdk.common.proto.PutTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutTaskDefResponse>) responseObserver);
          break;
        case METHODID_GET_TASK_DEF:
          serviceImpl.getTaskDef((io.littlehorse.sdk.common.proto.TaskDefIdPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetTaskDefReplyPb>) responseObserver);
          break;
        case METHODID_PUT_EXTERNAL_EVENT_DEF:
          serviceImpl.putExternalEventDef((io.littlehorse.sdk.common.proto.PutExternalEventDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutExternalEventDefResponse>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT_DEF:
          serviceImpl.getExternalEventDef((io.littlehorse.sdk.common.proto.ExternalEventDefIdPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb>) responseObserver);
          break;
        case METHODID_PUT_WF_SPEC:
          serviceImpl.putWfSpec((io.littlehorse.sdk.common.proto.PutWfSpecRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutWfSpecResponse>) responseObserver);
          break;
        case METHODID_GET_WF_SPEC:
          serviceImpl.getWfSpec((io.littlehorse.sdk.common.proto.WfSpecIdPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb>) responseObserver);
          break;
        case METHODID_GET_LATEST_WF_SPEC:
          serviceImpl.getLatestWfSpec((io.littlehorse.sdk.common.proto.GetLatestWfSpecPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfSpecReplyPb>) responseObserver);
          break;
        case METHODID_PUT_USER_TASK_DEF:
          serviceImpl.putUserTaskDef((io.littlehorse.sdk.common.proto.PutUserTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutUserTaskDefResponse>) responseObserver);
          break;
        case METHODID_GET_USER_TASK_DEF:
          serviceImpl.getUserTaskDef((io.littlehorse.sdk.common.proto.UserTaskDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb>) responseObserver);
          break;
        case METHODID_GET_LATEST_USER_TASK_DEF:
          serviceImpl.getLatestUserTaskDef((io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb>) responseObserver);
          break;
        case METHODID_GET_USER_TASK_RUN:
          serviceImpl.getUserTaskRun((io.littlehorse.sdk.common.proto.UserTaskRunId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb>) responseObserver);
          break;
        case METHODID_RUN_WF:
          serviceImpl.runWf((io.littlehorse.sdk.common.proto.RunWfPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RunWfReplyPb>) responseObserver);
          break;
        case METHODID_GET_WF_RUN:
          serviceImpl.getWfRun((io.littlehorse.sdk.common.proto.WfRunIdPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetWfRunReplyPb>) responseObserver);
          break;
        case METHODID_ASSIGN_USER_TASK_RUN:
          serviceImpl.assignUserTaskRun((io.littlehorse.sdk.common.proto.AssignUserTaskRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb>) responseObserver);
          break;
        case METHODID_COMPLETE_USER_TASK_RUN:
          serviceImpl.completeUserTaskRun((io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb>) responseObserver);
          break;
        case METHODID_GET_NODE_RUN:
          serviceImpl.getNodeRun((io.littlehorse.sdk.common.proto.NodeRunId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetNodeRunReplyPb>) responseObserver);
          break;
        case METHODID_LIST_NODE_RUNS:
          serviceImpl.listNodeRuns((io.littlehorse.sdk.common.proto.ListNodeRunsPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb>) responseObserver);
          break;
        case METHODID_GET_TASK_RUN:
          serviceImpl.getTaskRun((io.littlehorse.sdk.common.proto.TaskRunIdPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetTaskRunReplyPb>) responseObserver);
          break;
        case METHODID_GET_VARIABLE:
          serviceImpl.getVariable((io.littlehorse.sdk.common.proto.VariableIdPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetVariableReplyPb>) responseObserver);
          break;
        case METHODID_LIST_VARIABLES:
          serviceImpl.listVariables((io.littlehorse.sdk.common.proto.ListVariablesPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListVariablesReplyPb>) responseObserver);
          break;
        case METHODID_PUT_EXTERNAL_EVENT:
          serviceImpl.putExternalEvent((io.littlehorse.sdk.common.proto.PutExternalEventRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PutExternalEventResponse>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT:
          serviceImpl.getExternalEvent((io.littlehorse.sdk.common.proto.ExternalEventIdPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.GetExternalEventReplyPb>) responseObserver);
          break;
        case METHODID_LIST_EXTERNAL_EVENTS:
          serviceImpl.listExternalEvents((io.littlehorse.sdk.common.proto.ListExternalEventsPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_WF_RUN:
          serviceImpl.searchWfRun((io.littlehorse.sdk.common.proto.SearchWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchWfRunReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_NODE_RUN:
          serviceImpl.searchNodeRun((io.littlehorse.sdk.common.proto.SearchNodeRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_TASK_RUN:
          serviceImpl.searchTaskRun((io.littlehorse.sdk.common.proto.SearchTaskRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_USER_TASK_RUN:
          serviceImpl.searchUserTaskRun((io.littlehorse.sdk.common.proto.SearchUserTaskRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_VARIABLE:
          serviceImpl.searchVariable((io.littlehorse.sdk.common.proto.SearchVariablePb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchVariableReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_TASK_DEF:
          serviceImpl.searchTaskDef((io.littlehorse.sdk.common.proto.SearchTaskDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_USER_TASK_DEF:
          serviceImpl.searchUserTaskDef((io.littlehorse.sdk.common.proto.SearchUserTaskDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_WF_SPEC:
          serviceImpl.searchWfSpec((io.littlehorse.sdk.common.proto.SearchWfSpecPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_EXTERNAL_EVENT_DEF:
          serviceImpl.searchExternalEventDef((io.littlehorse.sdk.common.proto.SearchExternalEventDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb>) responseObserver);
          break;
        case METHODID_SEARCH_EXTERNAL_EVENT:
          serviceImpl.searchExternalEvent((io.littlehorse.sdk.common.proto.SearchExternalEventPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb>) responseObserver);
          break;
        case METHODID_REGISTER_TASK_WORKER:
          serviceImpl.registerTaskWorker((io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb>) responseObserver);
          break;
        case METHODID_REPORT_TASK:
          serviceImpl.reportTask((io.littlehorse.sdk.common.proto.ReportTaskRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ReportTaskReplyPb>) responseObserver);
          break;
        case METHODID_STOP_WF_RUN:
          serviceImpl.stopWfRun((io.littlehorse.sdk.common.proto.StopWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StopWfRunReplyPb>) responseObserver);
          break;
        case METHODID_RESUME_WF_RUN:
          serviceImpl.resumeWfRun((io.littlehorse.sdk.common.proto.ResumeWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb>) responseObserver);
          break;
        case METHODID_DELETE_WF_RUN:
          serviceImpl.deleteWfRun((io.littlehorse.sdk.common.proto.DeleteWfRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>) responseObserver);
          break;
        case METHODID_DELETE_TASK_DEF:
          serviceImpl.deleteTaskDef((io.littlehorse.sdk.common.proto.DeleteTaskDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>) responseObserver);
          break;
        case METHODID_DELETE_WF_SPEC:
          serviceImpl.deleteWfSpec((io.littlehorse.sdk.common.proto.DeleteWfSpecPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>) responseObserver);
          break;
        case METHODID_DELETE_USER_TASK_DEF:
          serviceImpl.deleteUserTaskDef((io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>) responseObserver);
          break;
        case METHODID_DELETE_EXTERNAL_EVENT_DEF:
          serviceImpl.deleteExternalEventDef((io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>) responseObserver);
          break;
        case METHODID_CANCEL_USER_TASK_RUN:
          serviceImpl.cancelUserTaskRun((io.littlehorse.sdk.common.proto.CancelUserTaskRunPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb>) responseObserver);
          break;
        case METHODID_HEALTH_CHECK:
          serviceImpl.healthCheck((io.littlehorse.sdk.common.proto.HealthCheckPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.HealthCheckReplyPb>) responseObserver);
          break;
        case METHODID_TASK_DEF_METRICS:
          serviceImpl.taskDefMetrics((io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb>) responseObserver);
          break;
        case METHODID_WF_SPEC_METRICS:
          serviceImpl.wfSpecMetrics((io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb>) responseObserver);
          break;
        case METHODID_LIST_TASK_DEF_METRICS:
          serviceImpl.listTaskDefMetrics((io.littlehorse.sdk.common.proto.ListTaskMetricsPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb>) responseObserver);
          break;
        case METHODID_LIST_WF_SPEC_METRICS:
          serviceImpl.listWfSpecMetrics((io.littlehorse.sdk.common.proto.ListWfMetricsPb) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb>) responseObserver);
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
        case METHODID_POLL_TASK:
          return (io.grpc.stub.StreamObserver<Req>) serviceImpl.pollTask(
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskReplyPb>) responseObserver);
        default:
          throw new AssertionError();
      }
    }
  }

  public static final io.grpc.ServerServiceDefinition bindService(AsyncService service) {
    return io.grpc.ServerServiceDefinition.builder(getServiceDescriptor())
        .addMethod(
          getPutTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutTaskDefRequest,
              io.littlehorse.sdk.common.proto.PutTaskDefResponse>(
                service, METHODID_PUT_TASK_DEF)))
        .addMethod(
          getGetTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TaskDefIdPb,
              io.littlehorse.sdk.common.proto.GetTaskDefReplyPb>(
                service, METHODID_GET_TASK_DEF)))
        .addMethod(
          getPutExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutExternalEventDefRequest,
              io.littlehorse.sdk.common.proto.PutExternalEventDefResponse>(
                service, METHODID_PUT_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getGetExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ExternalEventDefIdPb,
              io.littlehorse.sdk.common.proto.GetExternalEventDefReplyPb>(
                service, METHODID_GET_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getPutWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutWfSpecRequest,
              io.littlehorse.sdk.common.proto.PutWfSpecResponse>(
                service, METHODID_PUT_WF_SPEC)))
        .addMethod(
          getGetWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WfSpecIdPb,
              io.littlehorse.sdk.common.proto.GetWfSpecReplyPb>(
                service, METHODID_GET_WF_SPEC)))
        .addMethod(
          getGetLatestWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.GetLatestWfSpecPb,
              io.littlehorse.sdk.common.proto.GetWfSpecReplyPb>(
                service, METHODID_GET_LATEST_WF_SPEC)))
        .addMethod(
          getPutUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutUserTaskDefRequest,
              io.littlehorse.sdk.common.proto.PutUserTaskDefResponse>(
                service, METHODID_PUT_USER_TASK_DEF)))
        .addMethod(
          getGetUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.UserTaskDefId,
              io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb>(
                service, METHODID_GET_USER_TASK_DEF)))
        .addMethod(
          getGetLatestUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.GetLatestUserTaskDefPb,
              io.littlehorse.sdk.common.proto.GetUserTaskDefReplyPb>(
                service, METHODID_GET_LATEST_USER_TASK_DEF)))
        .addMethod(
          getGetUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.UserTaskRunId,
              io.littlehorse.sdk.common.proto.GetUserTaskRunReplyPb>(
                service, METHODID_GET_USER_TASK_RUN)))
        .addMethod(
          getRunWfMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.RunWfPb,
              io.littlehorse.sdk.common.proto.RunWfReplyPb>(
                service, METHODID_RUN_WF)))
        .addMethod(
          getGetWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WfRunIdPb,
              io.littlehorse.sdk.common.proto.GetWfRunReplyPb>(
                service, METHODID_GET_WF_RUN)))
        .addMethod(
          getAssignUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.AssignUserTaskRunPb,
              io.littlehorse.sdk.common.proto.AssignUserTaskRunReplyPb>(
                service, METHODID_ASSIGN_USER_TASK_RUN)))
        .addMethod(
          getCompleteUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.CompleteUserTaskRunPb,
              io.littlehorse.sdk.common.proto.CompleteUserTaskRunReplyPb>(
                service, METHODID_COMPLETE_USER_TASK_RUN)))
        .addMethod(
          getGetNodeRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.NodeRunId,
              io.littlehorse.sdk.common.proto.GetNodeRunReplyPb>(
                service, METHODID_GET_NODE_RUN)))
        .addMethod(
          getListNodeRunsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListNodeRunsPb,
              io.littlehorse.sdk.common.proto.ListNodeRunsReplyPb>(
                service, METHODID_LIST_NODE_RUNS)))
        .addMethod(
          getGetTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TaskRunIdPb,
              io.littlehorse.sdk.common.proto.GetTaskRunReplyPb>(
                service, METHODID_GET_TASK_RUN)))
        .addMethod(
          getGetVariableMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.VariableIdPb,
              io.littlehorse.sdk.common.proto.GetVariableReplyPb>(
                service, METHODID_GET_VARIABLE)))
        .addMethod(
          getListVariablesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListVariablesPb,
              io.littlehorse.sdk.common.proto.ListVariablesReplyPb>(
                service, METHODID_LIST_VARIABLES)))
        .addMethod(
          getPutExternalEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutExternalEventRequest,
              io.littlehorse.sdk.common.proto.PutExternalEventResponse>(
                service, METHODID_PUT_EXTERNAL_EVENT)))
        .addMethod(
          getGetExternalEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ExternalEventIdPb,
              io.littlehorse.sdk.common.proto.GetExternalEventReplyPb>(
                service, METHODID_GET_EXTERNAL_EVENT)))
        .addMethod(
          getListExternalEventsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListExternalEventsPb,
              io.littlehorse.sdk.common.proto.ListExternalEventsReplyPb>(
                service, METHODID_LIST_EXTERNAL_EVENTS)))
        .addMethod(
          getSearchWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchWfRunPb,
              io.littlehorse.sdk.common.proto.SearchWfRunReplyPb>(
                service, METHODID_SEARCH_WF_RUN)))
        .addMethod(
          getSearchNodeRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchNodeRunPb,
              io.littlehorse.sdk.common.proto.SearchNodeRunReplyPb>(
                service, METHODID_SEARCH_NODE_RUN)))
        .addMethod(
          getSearchTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchTaskRunPb,
              io.littlehorse.sdk.common.proto.SearchTaskRunReplyPb>(
                service, METHODID_SEARCH_TASK_RUN)))
        .addMethod(
          getSearchUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchUserTaskRunPb,
              io.littlehorse.sdk.common.proto.SearchUserTaskRunReplyPb>(
                service, METHODID_SEARCH_USER_TASK_RUN)))
        .addMethod(
          getSearchVariableMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchVariablePb,
              io.littlehorse.sdk.common.proto.SearchVariableReplyPb>(
                service, METHODID_SEARCH_VARIABLE)))
        .addMethod(
          getSearchTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchTaskDefPb,
              io.littlehorse.sdk.common.proto.SearchTaskDefReplyPb>(
                service, METHODID_SEARCH_TASK_DEF)))
        .addMethod(
          getSearchUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchUserTaskDefPb,
              io.littlehorse.sdk.common.proto.SearchUserTaskDefReplyPb>(
                service, METHODID_SEARCH_USER_TASK_DEF)))
        .addMethod(
          getSearchWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchWfSpecPb,
              io.littlehorse.sdk.common.proto.SearchWfSpecReplyPb>(
                service, METHODID_SEARCH_WF_SPEC)))
        .addMethod(
          getSearchExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchExternalEventDefPb,
              io.littlehorse.sdk.common.proto.SearchExternalEventDefReplyPb>(
                service, METHODID_SEARCH_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getSearchExternalEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchExternalEventPb,
              io.littlehorse.sdk.common.proto.SearchExternalEventReplyPb>(
                service, METHODID_SEARCH_EXTERNAL_EVENT)))
        .addMethod(
          getRegisterTaskWorkerMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.RegisterTaskWorkerPb,
              io.littlehorse.sdk.common.proto.RegisterTaskWorkerReplyPb>(
                service, METHODID_REGISTER_TASK_WORKER)))
        .addMethod(
          getPollTaskMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PollTaskPb,
              io.littlehorse.sdk.common.proto.PollTaskReplyPb>(
                service, METHODID_POLL_TASK)))
        .addMethod(
          getReportTaskMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ReportTaskRunPb,
              io.littlehorse.sdk.common.proto.ReportTaskReplyPb>(
                service, METHODID_REPORT_TASK)))
        .addMethod(
          getStopWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.StopWfRunPb,
              io.littlehorse.sdk.common.proto.StopWfRunReplyPb>(
                service, METHODID_STOP_WF_RUN)))
        .addMethod(
          getResumeWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ResumeWfRunPb,
              io.littlehorse.sdk.common.proto.ResumeWfRunReplyPb>(
                service, METHODID_RESUME_WF_RUN)))
        .addMethod(
          getDeleteWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteWfRunPb,
              io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>(
                service, METHODID_DELETE_WF_RUN)))
        .addMethod(
          getDeleteTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteTaskDefPb,
              io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>(
                service, METHODID_DELETE_TASK_DEF)))
        .addMethod(
          getDeleteWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteWfSpecPb,
              io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>(
                service, METHODID_DELETE_WF_SPEC)))
        .addMethod(
          getDeleteUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteUserTaskDefPb,
              io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>(
                service, METHODID_DELETE_USER_TASK_DEF)))
        .addMethod(
          getDeleteExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteExternalEventDefPb,
              io.littlehorse.sdk.common.proto.DeleteObjectReplyPb>(
                service, METHODID_DELETE_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getCancelUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.CancelUserTaskRunPb,
              io.littlehorse.sdk.common.proto.CancelUserTaskRunReplyPb>(
                service, METHODID_CANCEL_USER_TASK_RUN)))
        .addMethod(
          getHealthCheckMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.HealthCheckPb,
              io.littlehorse.sdk.common.proto.HealthCheckReplyPb>(
                service, METHODID_HEALTH_CHECK)))
        .addMethod(
          getTaskDefMetricsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TaskDefMetricsQueryPb,
              io.littlehorse.sdk.common.proto.TaskDefMetricsReplyPb>(
                service, METHODID_TASK_DEF_METRICS)))
        .addMethod(
          getWfSpecMetricsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WfSpecMetricsQueryPb,
              io.littlehorse.sdk.common.proto.WfSpecMetricsReplyPb>(
                service, METHODID_WF_SPEC_METRICS)))
        .addMethod(
          getListTaskDefMetricsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListTaskMetricsPb,
              io.littlehorse.sdk.common.proto.ListTaskMetricsReplyPb>(
                service, METHODID_LIST_TASK_DEF_METRICS)))
        .addMethod(
          getListWfSpecMetricsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListWfMetricsPb,
              io.littlehorse.sdk.common.proto.ListWfMetricsReplyPb>(
                service, METHODID_LIST_WF_SPEC_METRICS)))
        .build();
  }

  private static abstract class LHPublicApiBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LHPublicApiBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.getDescriptor();
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
              .addMethod(getGetLatestWfSpecMethod())
              .addMethod(getPutUserTaskDefMethod())
              .addMethod(getGetUserTaskDefMethod())
              .addMethod(getGetLatestUserTaskDefMethod())
              .addMethod(getGetUserTaskRunMethod())
              .addMethod(getRunWfMethod())
              .addMethod(getGetWfRunMethod())
              .addMethod(getAssignUserTaskRunMethod())
              .addMethod(getCompleteUserTaskRunMethod())
              .addMethod(getGetNodeRunMethod())
              .addMethod(getListNodeRunsMethod())
              .addMethod(getGetTaskRunMethod())
              .addMethod(getGetVariableMethod())
              .addMethod(getListVariablesMethod())
              .addMethod(getPutExternalEventMethod())
              .addMethod(getGetExternalEventMethod())
              .addMethod(getListExternalEventsMethod())
              .addMethod(getSearchWfRunMethod())
              .addMethod(getSearchNodeRunMethod())
              .addMethod(getSearchTaskRunMethod())
              .addMethod(getSearchUserTaskRunMethod())
              .addMethod(getSearchVariableMethod())
              .addMethod(getSearchTaskDefMethod())
              .addMethod(getSearchUserTaskDefMethod())
              .addMethod(getSearchWfSpecMethod())
              .addMethod(getSearchExternalEventDefMethod())
              .addMethod(getSearchExternalEventMethod())
              .addMethod(getRegisterTaskWorkerMethod())
              .addMethod(getPollTaskMethod())
              .addMethod(getReportTaskMethod())
              .addMethod(getStopWfRunMethod())
              .addMethod(getResumeWfRunMethod())
              .addMethod(getDeleteWfRunMethod())
              .addMethod(getDeleteTaskDefMethod())
              .addMethod(getDeleteWfSpecMethod())
              .addMethod(getDeleteUserTaskDefMethod())
              .addMethod(getDeleteExternalEventDefMethod())
              .addMethod(getCancelUserTaskRunMethod())
              .addMethod(getHealthCheckMethod())
              .addMethod(getTaskDefMetricsMethod())
              .addMethod(getWfSpecMetricsMethod())
              .addMethod(getListTaskDefMetricsMethod())
              .addMethod(getListWfSpecMetricsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
