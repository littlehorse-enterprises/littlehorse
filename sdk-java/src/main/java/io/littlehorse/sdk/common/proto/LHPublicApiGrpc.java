package io.littlehorse.sdk.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.57.2)",
    comments = "Source: service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LHPublicApiGrpc {

  private LHPublicApiGrpc() {}

  public static final java.lang.String SERVICE_NAME = "littlehorse.LHPublicApi";

  // Static method descriptors that strictly reflect the proto.
  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTaskDefRequest,
      io.littlehorse.sdk.common.proto.TaskDef> getPutTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutTaskDef",
      requestType = io.littlehorse.sdk.common.proto.PutTaskDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.TaskDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTaskDefRequest,
      io.littlehorse.sdk.common.proto.TaskDef> getPutTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTaskDefRequest, io.littlehorse.sdk.common.proto.TaskDef> getPutTaskDefMethod;
    if ((getPutTaskDefMethod = LHPublicApiGrpc.getPutTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutTaskDefMethod = LHPublicApiGrpc.getPutTaskDefMethod) == null) {
          LHPublicApiGrpc.getPutTaskDefMethod = getPutTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutTaskDefRequest, io.littlehorse.sdk.common.proto.TaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutTaskDef"))
              .build();
        }
      }
    }
    return getPutTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefId,
      io.littlehorse.sdk.common.proto.TaskDef> getGetTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTaskDef",
      requestType = io.littlehorse.sdk.common.proto.TaskDefId.class,
      responseType = io.littlehorse.sdk.common.proto.TaskDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefId,
      io.littlehorse.sdk.common.proto.TaskDef> getGetTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDef> getGetTaskDefMethod;
    if ((getGetTaskDefMethod = LHPublicApiGrpc.getGetTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetTaskDefMethod = LHPublicApiGrpc.getGetTaskDefMethod) == null) {
          LHPublicApiGrpc.getGetTaskDefMethod = getGetTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetTaskDef"))
              .build();
        }
      }
    }
    return getGetTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest,
      io.littlehorse.sdk.common.proto.ExternalEventDef> getPutExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ExternalEventDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest,
      io.littlehorse.sdk.common.proto.ExternalEventDef> getPutExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest, io.littlehorse.sdk.common.proto.ExternalEventDef> getPutExternalEventDefMethod;
    if ((getPutExternalEventDefMethod = LHPublicApiGrpc.getPutExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutExternalEventDefMethod = LHPublicApiGrpc.getPutExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getPutExternalEventDefMethod = getPutExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest, io.littlehorse.sdk.common.proto.ExternalEventDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDef.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutExternalEventDef"))
              .build();
        }
      }
    }
    return getPutExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventDefId,
      io.littlehorse.sdk.common.proto.ExternalEventDef> getGetExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.ExternalEventDefId.class,
      responseType = io.littlehorse.sdk.common.proto.ExternalEventDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventDefId,
      io.littlehorse.sdk.common.proto.ExternalEventDef> getGetExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDef> getGetExternalEventDefMethod;
    if ((getGetExternalEventDefMethod = LHPublicApiGrpc.getGetExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetExternalEventDefMethod = LHPublicApiGrpc.getGetExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getGetExternalEventDefMethod = getGetExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDef.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetExternalEventDef"))
              .build();
        }
      }
    }
    return getGetExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpec> getPutWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutWfSpec",
      requestType = io.littlehorse.sdk.common.proto.PutWfSpecRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfSpec.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpec> getPutWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec> getPutWfSpecMethod;
    if ((getPutWfSpecMethod = LHPublicApiGrpc.getPutWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutWfSpecMethod = LHPublicApiGrpc.getPutWfSpecMethod) == null) {
          LHPublicApiGrpc.getPutWfSpecMethod = getPutWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpec.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutWfSpec"))
              .build();
        }
      }
    }
    return getPutWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecId,
      io.littlehorse.sdk.common.proto.WfSpec> getGetWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWfSpec",
      requestType = io.littlehorse.sdk.common.proto.WfSpecId.class,
      responseType = io.littlehorse.sdk.common.proto.WfSpec.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecId,
      io.littlehorse.sdk.common.proto.WfSpec> getGetWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpec> getGetWfSpecMethod;
    if ((getGetWfSpecMethod = LHPublicApiGrpc.getGetWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetWfSpecMethod = LHPublicApiGrpc.getGetWfSpecMethod) == null) {
          LHPublicApiGrpc.getGetWfSpecMethod = getGetWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpec>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpec.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetWfSpec"))
              .build();
        }
      }
    }
    return getGetWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpec> getGetLatestWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetLatestWfSpec",
      requestType = io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfSpec.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpec> getGetLatestWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec> getGetLatestWfSpecMethod;
    if ((getGetLatestWfSpecMethod = LHPublicApiGrpc.getGetLatestWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetLatestWfSpecMethod = LHPublicApiGrpc.getGetLatestWfSpecMethod) == null) {
          LHPublicApiGrpc.getGetLatestWfSpecMethod = getGetLatestWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLatestWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpec.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetLatestWfSpec"))
              .build();
        }
      }
    }
    return getGetLatestWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.UserTaskDef> getPutUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.UserTaskDef> getPutUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDef> getPutUserTaskDefMethod;
    if ((getPutUserTaskDefMethod = LHPublicApiGrpc.getPutUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutUserTaskDefMethod = LHPublicApiGrpc.getPutUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getPutUserTaskDefMethod = getPutUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutUserTaskDef"))
              .build();
        }
      }
    }
    return getPutUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskDefId,
      io.littlehorse.sdk.common.proto.UserTaskDef> getGetUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.UserTaskDefId.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskDefId,
      io.littlehorse.sdk.common.proto.UserTaskDef> getGetUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskDefId, io.littlehorse.sdk.common.proto.UserTaskDef> getGetUserTaskDefMethod;
    if ((getGetUserTaskDefMethod = LHPublicApiGrpc.getGetUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetUserTaskDefMethod = LHPublicApiGrpc.getGetUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getGetUserTaskDefMethod = getGetUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.UserTaskDefId, io.littlehorse.sdk.common.proto.UserTaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetUserTaskDef"))
              .build();
        }
      }
    }
    return getGetUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.UserTaskDef> getGetLatestUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetLatestUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.UserTaskDef> getGetLatestUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDef> getGetLatestUserTaskDefMethod;
    if ((getGetLatestUserTaskDefMethod = LHPublicApiGrpc.getGetLatestUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetLatestUserTaskDefMethod = LHPublicApiGrpc.getGetLatestUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getGetLatestUserTaskDefMethod = getGetLatestUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLatestUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetLatestUserTaskDef"))
              .build();
        }
      }
    }
    return getGetLatestUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RunWfRequest,
      io.littlehorse.sdk.common.proto.WfRun> getRunWfMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RunWf",
      requestType = io.littlehorse.sdk.common.proto.RunWfRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RunWfRequest,
      io.littlehorse.sdk.common.proto.WfRun> getRunWfMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RunWfRequest, io.littlehorse.sdk.common.proto.WfRun> getRunWfMethod;
    if ((getRunWfMethod = LHPublicApiGrpc.getRunWfMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getRunWfMethod = LHPublicApiGrpc.getRunWfMethod) == null) {
          LHPublicApiGrpc.getRunWfMethod = getRunWfMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.RunWfRequest, io.littlehorse.sdk.common.proto.WfRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RunWf"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RunWfRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRun.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("RunWf"))
              .build();
        }
      }
    }
    return getRunWfMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfRunId,
      io.littlehorse.sdk.common.proto.WfRun> getGetWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWfRun",
      requestType = io.littlehorse.sdk.common.proto.WfRunId.class,
      responseType = io.littlehorse.sdk.common.proto.WfRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfRunId,
      io.littlehorse.sdk.common.proto.WfRun> getGetWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfRunId, io.littlehorse.sdk.common.proto.WfRun> getGetWfRunMethod;
    if ((getGetWfRunMethod = LHPublicApiGrpc.getGetWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetWfRunMethod = LHPublicApiGrpc.getGetWfRunMethod) == null) {
          LHPublicApiGrpc.getGetWfRunMethod = getGetWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfRunId, io.littlehorse.sdk.common.proto.WfRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRun.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetWfRun"))
              .build();
        }
      }
    }
    return getGetWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskRunId,
      io.littlehorse.sdk.common.proto.UserTaskRun> getGetUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.UserTaskRunId.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskRunId,
      io.littlehorse.sdk.common.proto.UserTaskRun> getGetUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.UserTaskRun> getGetUserTaskRunMethod;
    if ((getGetUserTaskRunMethod = LHPublicApiGrpc.getGetUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetUserTaskRunMethod = LHPublicApiGrpc.getGetUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getGetUserTaskRunMethod = getGetUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.UserTaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetUserTaskRun"))
              .build();
        }
      }
    }
    return getGetUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest,
      com.google.protobuf.Empty> getAssignUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AssignUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest,
      com.google.protobuf.Empty> getAssignUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest, com.google.protobuf.Empty> getAssignUserTaskRunMethod;
    if ((getAssignUserTaskRunMethod = LHPublicApiGrpc.getAssignUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getAssignUserTaskRunMethod = LHPublicApiGrpc.getAssignUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getAssignUserTaskRunMethod = getAssignUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AssignUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("AssignUserTaskRun"))
              .build();
        }
      }
    }
    return getAssignUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest,
      com.google.protobuf.Empty> getCompleteUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CompleteUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest,
      com.google.protobuf.Empty> getCompleteUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest, com.google.protobuf.Empty> getCompleteUserTaskRunMethod;
    if ((getCompleteUserTaskRunMethod = LHPublicApiGrpc.getCompleteUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getCompleteUserTaskRunMethod = LHPublicApiGrpc.getCompleteUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getCompleteUserTaskRunMethod = getCompleteUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CompleteUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("CompleteUserTaskRun"))
              .build();
        }
      }
    }
    return getCompleteUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest,
      com.google.protobuf.Empty> getCancelUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "CancelUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest,
      com.google.protobuf.Empty> getCancelUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest, com.google.protobuf.Empty> getCancelUserTaskRunMethod;
    if ((getCancelUserTaskRunMethod = LHPublicApiGrpc.getCancelUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getCancelUserTaskRunMethod = LHPublicApiGrpc.getCancelUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getCancelUserTaskRunMethod = getCancelUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("CancelUserTaskRun"))
              .build();
        }
      }
    }
    return getCancelUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListUserTaskRunRequest,
      io.littlehorse.sdk.common.proto.UserTaskRunList> getListUserTaskRunsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListUserTaskRuns",
      requestType = io.littlehorse.sdk.common.proto.ListUserTaskRunRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskRunList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListUserTaskRunRequest,
      io.littlehorse.sdk.common.proto.UserTaskRunList> getListUserTaskRunsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListUserTaskRunRequest, io.littlehorse.sdk.common.proto.UserTaskRunList> getListUserTaskRunsMethod;
    if ((getListUserTaskRunsMethod = LHPublicApiGrpc.getListUserTaskRunsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListUserTaskRunsMethod = LHPublicApiGrpc.getListUserTaskRunsMethod) == null) {
          LHPublicApiGrpc.getListUserTaskRunsMethod = getListUserTaskRunsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListUserTaskRunRequest, io.littlehorse.sdk.common.proto.UserTaskRunList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListUserTaskRuns"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRunList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListUserTaskRuns"))
              .build();
        }
      }
    }
    return getListUserTaskRunsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.NodeRunId,
      io.littlehorse.sdk.common.proto.NodeRun> getGetNodeRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetNodeRun",
      requestType = io.littlehorse.sdk.common.proto.NodeRunId.class,
      responseType = io.littlehorse.sdk.common.proto.NodeRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.NodeRunId,
      io.littlehorse.sdk.common.proto.NodeRun> getGetNodeRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.NodeRun> getGetNodeRunMethod;
    if ((getGetNodeRunMethod = LHPublicApiGrpc.getGetNodeRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetNodeRunMethod = LHPublicApiGrpc.getGetNodeRunMethod) == null) {
          LHPublicApiGrpc.getGetNodeRunMethod = getGetNodeRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.NodeRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNodeRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRun.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetNodeRun"))
              .build();
        }
      }
    }
    return getGetNodeRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListNodeRunsRequest,
      io.littlehorse.sdk.common.proto.NodeRunList> getListNodeRunsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListNodeRuns",
      requestType = io.littlehorse.sdk.common.proto.ListNodeRunsRequest.class,
      responseType = io.littlehorse.sdk.common.proto.NodeRunList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListNodeRunsRequest,
      io.littlehorse.sdk.common.proto.NodeRunList> getListNodeRunsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListNodeRunsRequest, io.littlehorse.sdk.common.proto.NodeRunList> getListNodeRunsMethod;
    if ((getListNodeRunsMethod = LHPublicApiGrpc.getListNodeRunsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListNodeRunsMethod = LHPublicApiGrpc.getListNodeRunsMethod) == null) {
          LHPublicApiGrpc.getListNodeRunsMethod = getListNodeRunsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListNodeRunsRequest, io.littlehorse.sdk.common.proto.NodeRunList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListNodeRuns"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListNodeRunsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRunList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListNodeRuns"))
              .build();
        }
      }
    }
    return getListNodeRunsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskRunId,
      io.littlehorse.sdk.common.proto.TaskRun> getGetTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTaskRun",
      requestType = io.littlehorse.sdk.common.proto.TaskRunId.class,
      responseType = io.littlehorse.sdk.common.proto.TaskRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskRunId,
      io.littlehorse.sdk.common.proto.TaskRun> getGetTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskRunId, io.littlehorse.sdk.common.proto.TaskRun> getGetTaskRunMethod;
    if ((getGetTaskRunMethod = LHPublicApiGrpc.getGetTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetTaskRunMethod = LHPublicApiGrpc.getGetTaskRunMethod) == null) {
          LHPublicApiGrpc.getGetTaskRunMethod = getGetTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskRunId, io.littlehorse.sdk.common.proto.TaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetTaskRun"))
              .build();
        }
      }
    }
    return getGetTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.VariableId,
      io.littlehorse.sdk.common.proto.Variable> getGetVariableMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetVariable",
      requestType = io.littlehorse.sdk.common.proto.VariableId.class,
      responseType = io.littlehorse.sdk.common.proto.Variable.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.VariableId,
      io.littlehorse.sdk.common.proto.Variable> getGetVariableMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.VariableId, io.littlehorse.sdk.common.proto.Variable> getGetVariableMethod;
    if ((getGetVariableMethod = LHPublicApiGrpc.getGetVariableMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetVariableMethod = LHPublicApiGrpc.getGetVariableMethod) == null) {
          LHPublicApiGrpc.getGetVariableMethod = getGetVariableMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.VariableId, io.littlehorse.sdk.common.proto.Variable>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetVariable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.VariableId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.Variable.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetVariable"))
              .build();
        }
      }
    }
    return getGetVariableMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListVariablesRequest,
      io.littlehorse.sdk.common.proto.VariableList> getListVariablesMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListVariables",
      requestType = io.littlehorse.sdk.common.proto.ListVariablesRequest.class,
      responseType = io.littlehorse.sdk.common.proto.VariableList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListVariablesRequest,
      io.littlehorse.sdk.common.proto.VariableList> getListVariablesMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListVariablesRequest, io.littlehorse.sdk.common.proto.VariableList> getListVariablesMethod;
    if ((getListVariablesMethod = LHPublicApiGrpc.getListVariablesMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListVariablesMethod = LHPublicApiGrpc.getListVariablesMethod) == null) {
          LHPublicApiGrpc.getListVariablesMethod = getListVariablesMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListVariablesRequest, io.littlehorse.sdk.common.proto.VariableList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListVariables"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListVariablesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.VariableList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListVariables"))
              .build();
        }
      }
    }
    return getListVariablesMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventRequest,
      io.littlehorse.sdk.common.proto.ExternalEvent> getPutExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutExternalEvent",
      requestType = io.littlehorse.sdk.common.proto.PutExternalEventRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ExternalEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventRequest,
      io.littlehorse.sdk.common.proto.ExternalEvent> getPutExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutExternalEventRequest, io.littlehorse.sdk.common.proto.ExternalEvent> getPutExternalEventMethod;
    if ((getPutExternalEventMethod = LHPublicApiGrpc.getPutExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPutExternalEventMethod = LHPublicApiGrpc.getPutExternalEventMethod) == null) {
          LHPublicApiGrpc.getPutExternalEventMethod = getPutExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutExternalEventRequest, io.littlehorse.sdk.common.proto.ExternalEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PutExternalEvent"))
              .build();
        }
      }
    }
    return getPutExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventId,
      io.littlehorse.sdk.common.proto.ExternalEvent> getGetExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetExternalEvent",
      requestType = io.littlehorse.sdk.common.proto.ExternalEventId.class,
      responseType = io.littlehorse.sdk.common.proto.ExternalEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventId,
      io.littlehorse.sdk.common.proto.ExternalEvent> getGetExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ExternalEventId, io.littlehorse.sdk.common.proto.ExternalEvent> getGetExternalEventMethod;
    if ((getGetExternalEventMethod = LHPublicApiGrpc.getGetExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetExternalEventMethod = LHPublicApiGrpc.getGetExternalEventMethod) == null) {
          LHPublicApiGrpc.getGetExternalEventMethod = getGetExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ExternalEventId, io.littlehorse.sdk.common.proto.ExternalEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetExternalEvent"))
              .build();
        }
      }
    }
    return getGetExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListExternalEventsRequest,
      io.littlehorse.sdk.common.proto.ExternalEventList> getListExternalEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListExternalEvents",
      requestType = io.littlehorse.sdk.common.proto.ListExternalEventsRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ExternalEventList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListExternalEventsRequest,
      io.littlehorse.sdk.common.proto.ExternalEventList> getListExternalEventsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListExternalEventsRequest, io.littlehorse.sdk.common.proto.ExternalEventList> getListExternalEventsMethod;
    if ((getListExternalEventsMethod = LHPublicApiGrpc.getListExternalEventsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListExternalEventsMethod = LHPublicApiGrpc.getListExternalEventsMethod) == null) {
          LHPublicApiGrpc.getListExternalEventsMethod = getListExternalEventsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListExternalEventsRequest, io.littlehorse.sdk.common.proto.ExternalEventList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListExternalEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListExternalEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListExternalEvents"))
              .build();
        }
      }
    }
    return getListExternalEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfRunRequest,
      io.littlehorse.sdk.common.proto.WfRunIdList> getSearchWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchWfRun",
      requestType = io.littlehorse.sdk.common.proto.SearchWfRunRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfRunIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfRunRequest,
      io.littlehorse.sdk.common.proto.WfRunIdList> getSearchWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfRunRequest, io.littlehorse.sdk.common.proto.WfRunIdList> getSearchWfRunMethod;
    if ((getSearchWfRunMethod = LHPublicApiGrpc.getSearchWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchWfRunMethod = LHPublicApiGrpc.getSearchWfRunMethod) == null) {
          LHPublicApiGrpc.getSearchWfRunMethod = getSearchWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWfRunRequest, io.littlehorse.sdk.common.proto.WfRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchWfRun"))
              .build();
        }
      }
    }
    return getSearchWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchNodeRunRequest,
      io.littlehorse.sdk.common.proto.NodeRunIdList> getSearchNodeRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchNodeRun",
      requestType = io.littlehorse.sdk.common.proto.SearchNodeRunRequest.class,
      responseType = io.littlehorse.sdk.common.proto.NodeRunIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchNodeRunRequest,
      io.littlehorse.sdk.common.proto.NodeRunIdList> getSearchNodeRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchNodeRunRequest, io.littlehorse.sdk.common.proto.NodeRunIdList> getSearchNodeRunMethod;
    if ((getSearchNodeRunMethod = LHPublicApiGrpc.getSearchNodeRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchNodeRunMethod = LHPublicApiGrpc.getSearchNodeRunMethod) == null) {
          LHPublicApiGrpc.getSearchNodeRunMethod = getSearchNodeRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchNodeRunRequest, io.littlehorse.sdk.common.proto.NodeRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchNodeRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchNodeRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchNodeRun"))
              .build();
        }
      }
    }
    return getSearchNodeRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskRunRequest,
      io.littlehorse.sdk.common.proto.TaskRunIdList> getSearchTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTaskRun",
      requestType = io.littlehorse.sdk.common.proto.SearchTaskRunRequest.class,
      responseType = io.littlehorse.sdk.common.proto.TaskRunIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskRunRequest,
      io.littlehorse.sdk.common.proto.TaskRunIdList> getSearchTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskRunRequest, io.littlehorse.sdk.common.proto.TaskRunIdList> getSearchTaskRunMethod;
    if ((getSearchTaskRunMethod = LHPublicApiGrpc.getSearchTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchTaskRunMethod = LHPublicApiGrpc.getSearchTaskRunMethod) == null) {
          LHPublicApiGrpc.getSearchTaskRunMethod = getSearchTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchTaskRunRequest, io.littlehorse.sdk.common.proto.TaskRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchTaskRun"))
              .build();
        }
      }
    }
    return getSearchTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest,
      io.littlehorse.sdk.common.proto.UserTaskRunIdList> getSearchUserTaskRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchUserTaskRun",
      requestType = io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskRunIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest,
      io.littlehorse.sdk.common.proto.UserTaskRunIdList> getSearchUserTaskRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest, io.littlehorse.sdk.common.proto.UserTaskRunIdList> getSearchUserTaskRunMethod;
    if ((getSearchUserTaskRunMethod = LHPublicApiGrpc.getSearchUserTaskRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchUserTaskRunMethod = LHPublicApiGrpc.getSearchUserTaskRunMethod) == null) {
          LHPublicApiGrpc.getSearchUserTaskRunMethod = getSearchUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest, io.littlehorse.sdk.common.proto.UserTaskRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchUserTaskRun"))
              .build();
        }
      }
    }
    return getSearchUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchVariableRequest,
      io.littlehorse.sdk.common.proto.VariableIdList> getSearchVariableMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchVariable",
      requestType = io.littlehorse.sdk.common.proto.SearchVariableRequest.class,
      responseType = io.littlehorse.sdk.common.proto.VariableIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchVariableRequest,
      io.littlehorse.sdk.common.proto.VariableIdList> getSearchVariableMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchVariableRequest, io.littlehorse.sdk.common.proto.VariableIdList> getSearchVariableMethod;
    if ((getSearchVariableMethod = LHPublicApiGrpc.getSearchVariableMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchVariableMethod = LHPublicApiGrpc.getSearchVariableMethod) == null) {
          LHPublicApiGrpc.getSearchVariableMethod = getSearchVariableMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchVariableRequest, io.littlehorse.sdk.common.proto.VariableIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchVariable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchVariableRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.VariableIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchVariable"))
              .build();
        }
      }
    }
    return getSearchVariableMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventRequest,
      io.littlehorse.sdk.common.proto.ExternalEventIdList> getSearchExternalEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchExternalEvent",
      requestType = io.littlehorse.sdk.common.proto.SearchExternalEventRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ExternalEventIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventRequest,
      io.littlehorse.sdk.common.proto.ExternalEventIdList> getSearchExternalEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventRequest, io.littlehorse.sdk.common.proto.ExternalEventIdList> getSearchExternalEventMethod;
    if ((getSearchExternalEventMethod = LHPublicApiGrpc.getSearchExternalEventMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchExternalEventMethod = LHPublicApiGrpc.getSearchExternalEventMethod) == null) {
          LHPublicApiGrpc.getSearchExternalEventMethod = getSearchExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchExternalEventRequest, io.littlehorse.sdk.common.proto.ExternalEventIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchExternalEvent"))
              .build();
        }
      }
    }
    return getSearchExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskDefRequest,
      io.littlehorse.sdk.common.proto.TaskDefIdList> getSearchTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTaskDef",
      requestType = io.littlehorse.sdk.common.proto.SearchTaskDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.TaskDefIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskDefRequest,
      io.littlehorse.sdk.common.proto.TaskDefIdList> getSearchTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTaskDefRequest, io.littlehorse.sdk.common.proto.TaskDefIdList> getSearchTaskDefMethod;
    if ((getSearchTaskDefMethod = LHPublicApiGrpc.getSearchTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchTaskDefMethod = LHPublicApiGrpc.getSearchTaskDefMethod) == null) {
          LHPublicApiGrpc.getSearchTaskDefMethod = getSearchTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchTaskDefRequest, io.littlehorse.sdk.common.proto.TaskDefIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchTaskDef"))
              .build();
        }
      }
    }
    return getSearchTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.UserTaskDefIdList> getSearchUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskDefIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest,
      io.littlehorse.sdk.common.proto.UserTaskDefIdList> getSearchUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDefIdList> getSearchUserTaskDefMethod;
    if ((getSearchUserTaskDefMethod = LHPublicApiGrpc.getSearchUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchUserTaskDefMethod = LHPublicApiGrpc.getSearchUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getSearchUserTaskDefMethod = getSearchUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDefIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDefIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchUserTaskDef"))
              .build();
        }
      }
    }
    return getSearchUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpecIdList> getSearchWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchWfSpec",
      requestType = io.littlehorse.sdk.common.proto.SearchWfSpecRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfSpecIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpecIdList> getSearchWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpecIdList> getSearchWfSpecMethod;
    if ((getSearchWfSpecMethod = LHPublicApiGrpc.getSearchWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchWfSpecMethod = LHPublicApiGrpc.getSearchWfSpecMethod) == null) {
          LHPublicApiGrpc.getSearchWfSpecMethod = getSearchWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpecIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchWfSpec"))
              .build();
        }
      }
    }
    return getSearchWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest,
      io.littlehorse.sdk.common.proto.ExternalEventDefIdList> getSearchExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ExternalEventDefIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest,
      io.littlehorse.sdk.common.proto.ExternalEventDefIdList> getSearchExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest, io.littlehorse.sdk.common.proto.ExternalEventDefIdList> getSearchExternalEventDefMethod;
    if ((getSearchExternalEventDefMethod = LHPublicApiGrpc.getSearchExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getSearchExternalEventDefMethod = LHPublicApiGrpc.getSearchExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getSearchExternalEventDefMethod = getSearchExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest, io.littlehorse.sdk.common.proto.ExternalEventDefIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDefIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("SearchExternalEventDef"))
              .build();
        }
      }
    }
    return getSearchExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest,
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> getRegisterTaskWorkerMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RegisterTaskWorker",
      requestType = io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.class,
      responseType = io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest,
      io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> getRegisterTaskWorkerMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest, io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> getRegisterTaskWorkerMethod;
    if ((getRegisterTaskWorkerMethod = LHPublicApiGrpc.getRegisterTaskWorkerMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getRegisterTaskWorkerMethod = LHPublicApiGrpc.getRegisterTaskWorkerMethod) == null) {
          LHPublicApiGrpc.getRegisterTaskWorkerMethod = getRegisterTaskWorkerMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest, io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterTaskWorker"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("RegisterTaskWorker"))
              .build();
        }
      }
    }
    return getRegisterTaskWorkerMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PollTaskRequest,
      io.littlehorse.sdk.common.proto.PollTaskResponse> getPollTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PollTask",
      requestType = io.littlehorse.sdk.common.proto.PollTaskRequest.class,
      responseType = io.littlehorse.sdk.common.proto.PollTaskResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PollTaskRequest,
      io.littlehorse.sdk.common.proto.PollTaskResponse> getPollTaskMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PollTaskRequest, io.littlehorse.sdk.common.proto.PollTaskResponse> getPollTaskMethod;
    if ((getPollTaskMethod = LHPublicApiGrpc.getPollTaskMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getPollTaskMethod = LHPublicApiGrpc.getPollTaskMethod) == null) {
          LHPublicApiGrpc.getPollTaskMethod = getPollTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PollTaskRequest, io.littlehorse.sdk.common.proto.PollTaskResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PollTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PollTaskRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PollTaskResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("PollTask"))
              .build();
        }
      }
    }
    return getPollTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ReportTaskRun,
      com.google.protobuf.Empty> getReportTaskMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ReportTask",
      requestType = io.littlehorse.sdk.common.proto.ReportTaskRun.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ReportTaskRun,
      com.google.protobuf.Empty> getReportTaskMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ReportTaskRun, com.google.protobuf.Empty> getReportTaskMethod;
    if ((getReportTaskMethod = LHPublicApiGrpc.getReportTaskMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getReportTaskMethod = LHPublicApiGrpc.getReportTaskMethod) == null) {
          LHPublicApiGrpc.getReportTaskMethod = getReportTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ReportTaskRun, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReportTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ReportTaskRun.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ReportTask"))
              .build();
        }
      }
    }
    return getReportTaskMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StopWfRunRequest,
      com.google.protobuf.Empty> getStopWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "StopWfRun",
      requestType = io.littlehorse.sdk.common.proto.StopWfRunRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StopWfRunRequest,
      com.google.protobuf.Empty> getStopWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StopWfRunRequest, com.google.protobuf.Empty> getStopWfRunMethod;
    if ((getStopWfRunMethod = LHPublicApiGrpc.getStopWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getStopWfRunMethod = LHPublicApiGrpc.getStopWfRunMethod) == null) {
          LHPublicApiGrpc.getStopWfRunMethod = getStopWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.StopWfRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StopWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.StopWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("StopWfRun"))
              .build();
        }
      }
    }
    return getStopWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ResumeWfRunRequest,
      com.google.protobuf.Empty> getResumeWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ResumeWfRun",
      requestType = io.littlehorse.sdk.common.proto.ResumeWfRunRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ResumeWfRunRequest,
      com.google.protobuf.Empty> getResumeWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ResumeWfRunRequest, com.google.protobuf.Empty> getResumeWfRunMethod;
    if ((getResumeWfRunMethod = LHPublicApiGrpc.getResumeWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getResumeWfRunMethod = LHPublicApiGrpc.getResumeWfRunMethod) == null) {
          LHPublicApiGrpc.getResumeWfRunMethod = getResumeWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ResumeWfRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ResumeWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ResumeWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ResumeWfRun"))
              .build();
        }
      }
    }
    return getResumeWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfRunRequest,
      com.google.protobuf.Empty> getDeleteWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteWfRun",
      requestType = io.littlehorse.sdk.common.proto.DeleteWfRunRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfRunRequest,
      com.google.protobuf.Empty> getDeleteWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfRunRequest, com.google.protobuf.Empty> getDeleteWfRunMethod;
    if ((getDeleteWfRunMethod = LHPublicApiGrpc.getDeleteWfRunMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteWfRunMethod = LHPublicApiGrpc.getDeleteWfRunMethod) == null) {
          LHPublicApiGrpc.getDeleteWfRunMethod = getDeleteWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteWfRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteWfRun"))
              .build();
        }
      }
    }
    return getDeleteWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteTaskDefRequest,
      com.google.protobuf.Empty> getDeleteTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteTaskDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteTaskDefRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteTaskDefRequest,
      com.google.protobuf.Empty> getDeleteTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteTaskDefRequest, com.google.protobuf.Empty> getDeleteTaskDefMethod;
    if ((getDeleteTaskDefMethod = LHPublicApiGrpc.getDeleteTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteTaskDefMethod = LHPublicApiGrpc.getDeleteTaskDefMethod) == null) {
          LHPublicApiGrpc.getDeleteTaskDefMethod = getDeleteTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteTaskDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteTaskDef"))
              .build();
        }
      }
    }
    return getDeleteTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfSpecRequest,
      com.google.protobuf.Empty> getDeleteWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteWfSpec",
      requestType = io.littlehorse.sdk.common.proto.DeleteWfSpecRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfSpecRequest,
      com.google.protobuf.Empty> getDeleteWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWfSpecRequest, com.google.protobuf.Empty> getDeleteWfSpecMethod;
    if ((getDeleteWfSpecMethod = LHPublicApiGrpc.getDeleteWfSpecMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteWfSpecMethod = LHPublicApiGrpc.getDeleteWfSpecMethod) == null) {
          LHPublicApiGrpc.getDeleteWfSpecMethod = getDeleteWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteWfSpecRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteWfSpec"))
              .build();
        }
      }
    }
    return getDeleteWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest,
      com.google.protobuf.Empty> getDeleteUserTaskDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteUserTaskDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest,
      com.google.protobuf.Empty> getDeleteUserTaskDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest, com.google.protobuf.Empty> getDeleteUserTaskDefMethod;
    if ((getDeleteUserTaskDefMethod = LHPublicApiGrpc.getDeleteUserTaskDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteUserTaskDefMethod = LHPublicApiGrpc.getDeleteUserTaskDefMethod) == null) {
          LHPublicApiGrpc.getDeleteUserTaskDefMethod = getDeleteUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteUserTaskDef"))
              .build();
        }
      }
    }
    return getDeleteUserTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest,
      com.google.protobuf.Empty> getDeleteExternalEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteExternalEventDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest,
      com.google.protobuf.Empty> getDeleteExternalEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest, com.google.protobuf.Empty> getDeleteExternalEventDefMethod;
    if ((getDeleteExternalEventDefMethod = LHPublicApiGrpc.getDeleteExternalEventDefMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getDeleteExternalEventDefMethod = LHPublicApiGrpc.getDeleteExternalEventDefMethod) == null) {
          LHPublicApiGrpc.getDeleteExternalEventDefMethod = getDeleteExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("DeleteExternalEventDef"))
              .build();
        }
      }
    }
    return getDeleteExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest,
      io.littlehorse.sdk.common.proto.TaskDefMetrics> getGetTaskDefMetricsWindowMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTaskDefMetricsWindow",
      requestType = io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest.class,
      responseType = io.littlehorse.sdk.common.proto.TaskDefMetrics.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest,
      io.littlehorse.sdk.common.proto.TaskDefMetrics> getGetTaskDefMetricsWindowMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest, io.littlehorse.sdk.common.proto.TaskDefMetrics> getGetTaskDefMetricsWindowMethod;
    if ((getGetTaskDefMetricsWindowMethod = LHPublicApiGrpc.getGetTaskDefMetricsWindowMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetTaskDefMetricsWindowMethod = LHPublicApiGrpc.getGetTaskDefMetricsWindowMethod) == null) {
          LHPublicApiGrpc.getGetTaskDefMetricsWindowMethod = getGetTaskDefMetricsWindowMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest, io.littlehorse.sdk.common.proto.TaskDefMetrics>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskDefMetricsWindow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefMetrics.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetTaskDefMetricsWindow"))
              .build();
        }
      }
    }
    return getGetTaskDefMetricsWindowMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest,
      io.littlehorse.sdk.common.proto.WfSpecMetrics> getGetWfSpecMetricsWindowMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWfSpecMetricsWindow",
      requestType = io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfSpecMetrics.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest,
      io.littlehorse.sdk.common.proto.WfSpecMetrics> getGetWfSpecMetricsWindowMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest, io.littlehorse.sdk.common.proto.WfSpecMetrics> getGetWfSpecMetricsWindowMethod;
    if ((getGetWfSpecMetricsWindowMethod = LHPublicApiGrpc.getGetWfSpecMetricsWindowMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getGetWfSpecMetricsWindowMethod = LHPublicApiGrpc.getGetWfSpecMetricsWindowMethod) == null) {
          LHPublicApiGrpc.getGetWfSpecMetricsWindowMethod = getGetWfSpecMetricsWindowMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest, io.littlehorse.sdk.common.proto.WfSpecMetrics>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfSpecMetricsWindow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecMetrics.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("GetWfSpecMetricsWindow"))
              .build();
        }
      }
    }
    return getGetWfSpecMetricsWindowMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskMetricsRequest,
      io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> getListTaskDefMetricsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListTaskDefMetrics",
      requestType = io.littlehorse.sdk.common.proto.ListTaskMetricsRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ListTaskMetricsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskMetricsRequest,
      io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> getListTaskDefMetricsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskMetricsRequest, io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> getListTaskDefMetricsMethod;
    if ((getListTaskDefMetricsMethod = LHPublicApiGrpc.getListTaskDefMetricsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListTaskDefMetricsMethod = LHPublicApiGrpc.getListTaskDefMetricsMethod) == null) {
          LHPublicApiGrpc.getListTaskDefMetricsMethod = getListTaskDefMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListTaskMetricsRequest, io.littlehorse.sdk.common.proto.ListTaskMetricsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTaskDefMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListTaskMetricsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListTaskMetricsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LHPublicApiMethodDescriptorSupplier("ListTaskDefMetrics"))
              .build();
        }
      }
    }
    return getListTaskDefMetricsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWfMetricsRequest,
      io.littlehorse.sdk.common.proto.ListWfMetricsResponse> getListWfSpecMetricsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListWfSpecMetrics",
      requestType = io.littlehorse.sdk.common.proto.ListWfMetricsRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ListWfMetricsResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWfMetricsRequest,
      io.littlehorse.sdk.common.proto.ListWfMetricsResponse> getListWfSpecMetricsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWfMetricsRequest, io.littlehorse.sdk.common.proto.ListWfMetricsResponse> getListWfSpecMetricsMethod;
    if ((getListWfSpecMetricsMethod = LHPublicApiGrpc.getListWfSpecMetricsMethod) == null) {
      synchronized (LHPublicApiGrpc.class) {
        if ((getListWfSpecMetricsMethod = LHPublicApiGrpc.getListWfSpecMetricsMethod) == null) {
          LHPublicApiGrpc.getListWfSpecMetricsMethod = getListWfSpecMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListWfMetricsRequest, io.littlehorse.sdk.common.proto.ListWfMetricsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListWfSpecMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListWfMetricsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListWfMetricsResponse.getDefaultInstance()))
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
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void getTaskDef(io.littlehorse.sdk.common.proto.TaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void getWfSpec(io.littlehorse.sdk.common.proto.WfSpecId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetLatestWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetLatestUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void runWf(io.littlehorse.sdk.common.proto.RunWfRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRunWfMethod(), responseObserver);
    }

    /**
     */
    default void getWfRun(io.littlehorse.sdk.common.proto.WfRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfRunMethod(), responseObserver);
    }

    /**
     */
    default void getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAssignUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCompleteUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void listUserTaskRuns(io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListUserTaskRunsMethod(), responseObserver);
    }

    /**
     */
    default void getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetNodeRunMethod(), responseObserver);
    }

    /**
     */
    default void listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListNodeRunsMethod(), responseObserver);
    }

    /**
     */
    default void getTaskRun(io.littlehorse.sdk.common.proto.TaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void getVariable(io.littlehorse.sdk.common.proto.VariableId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Variable> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetVariableMethod(), responseObserver);
    }

    /**
     */
    default void listVariables(io.littlehorse.sdk.common.proto.ListVariablesRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListVariablesMethod(), responseObserver);
    }

    /**
     */
    default void putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventMethod(), responseObserver);
    }

    /**
     */
    default void getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventMethod(), responseObserver);
    }

    /**
     */
    default void listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListExternalEventsMethod(), responseObserver);
    }

    /**
     */
    default void searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWfRunMethod(), responseObserver);
    }

    /**
     */
    default void searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchNodeRunMethod(), responseObserver);
    }

    /**
     */
    default void searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchUserTaskRunMethod(), responseObserver);
    }

    /**
     */
    default void searchVariable(io.littlehorse.sdk.common.proto.SearchVariableRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchVariableMethod(), responseObserver);
    }

    /**
     */
    default void searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchExternalEventMethod(), responseObserver);
    }

    /**
     */
    default void searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDefIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDefIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterTaskWorkerMethod(), responseObserver);
    }

    /**
     */
    default io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskRequest> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getPollTaskMethod(), responseObserver);
    }

    /**
     */
    default void reportTask(io.littlehorse.sdk.common.proto.ReportTaskRun request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReportTaskMethod(), responseObserver);
    }

    /**
     */
    default void stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStopWfRunMethod(), responseObserver);
    }

    /**
     */
    default void resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getResumeWfRunMethod(), responseObserver);
    }

    /**
     */
    default void deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWfRunMethod(), responseObserver);
    }

    /**
     */
    default void deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWfSpecMethod(), responseObserver);
    }

    /**
     */
    default void deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteUserTaskDefMethod(), responseObserver);
    }

    /**
     */
    default void deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteExternalEventDefMethod(), responseObserver);
    }

    /**
     */
    default void getTaskDefMetricsWindow(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetrics> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskDefMetricsWindowMethod(), responseObserver);
    }

    /**
     */
    default void getWfSpecMetricsWindow(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetrics> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfSpecMetricsWindowMethod(), responseObserver);
    }

    /**
     */
    default void listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTaskDefMetricsMethod(), responseObserver);
    }

    /**
     */
    default void listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsResponse> responseObserver) {
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
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTaskDef(io.littlehorse.sdk.common.proto.TaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWfSpec(io.littlehorse.sdk.common.proto.WfSpecId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetLatestWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetLatestUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void runWf(io.littlehorse.sdk.common.proto.RunWfRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWfRun(io.littlehorse.sdk.common.proto.WfRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAssignUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCompleteUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listUserTaskRuns(io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListUserTaskRunsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListNodeRunsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTaskRun(io.littlehorse.sdk.common.proto.TaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getVariable(io.littlehorse.sdk.common.proto.VariableId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Variable> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listVariables(io.littlehorse.sdk.common.proto.ListVariablesRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListVariablesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListExternalEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchNodeRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchVariable(io.littlehorse.sdk.common.proto.SearchVariableRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchVariableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDefIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDefIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterTaskWorkerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskRequest> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getPollTaskMethod(), getCallOptions()), responseObserver);
    }

    /**
     */
    public void reportTask(io.littlehorse.sdk.common.proto.ReportTaskRun request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getTaskDefMetricsWindow(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetrics> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskDefMetricsWindowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getWfSpecMetricsWindow(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetrics> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfSpecMetricsWindowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTaskDefMetricsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsResponse> responseObserver) {
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
    public io.littlehorse.sdk.common.proto.TaskDef putTaskDef(io.littlehorse.sdk.common.proto.PutTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.TaskDef getTaskDef(io.littlehorse.sdk.common.proto.TaskDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDef putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDef getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfSpec putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfSpec getWfSpec(io.littlehorse.sdk.common.proto.WfSpecId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfSpec getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetLatestWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.UserTaskDef putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.UserTaskDef getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.UserTaskDef getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetLatestUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfRun runWf(io.littlehorse.sdk.common.proto.RunWfRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRunWfMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfRun getWfRun(io.littlehorse.sdk.common.proto.WfRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.UserTaskRun getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAssignUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCompleteUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunList listUserTaskRuns(io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListUserTaskRunsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.NodeRun getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetNodeRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.NodeRunList listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListNodeRunsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.TaskRun getTaskRun(io.littlehorse.sdk.common.proto.TaskRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.Variable getVariable(io.littlehorse.sdk.common.proto.VariableId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetVariableMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.VariableList listVariables(io.littlehorse.sdk.common.proto.ListVariablesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListVariablesMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ExternalEvent putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ExternalEvent getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ExternalEventList listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListExternalEventsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfRunIdList searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdList searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchNodeRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.TaskRunIdList searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunIdList searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.VariableIdList searchVariable(io.littlehorse.sdk.common.proto.SearchVariableRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchVariableMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ExternalEventIdList searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchExternalEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.TaskDefIdList searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.UserTaskDefIdList searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfSpecIdList searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefIdList searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterTaskWorkerMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty reportTask(io.littlehorse.sdk.common.proto.ReportTaskRun request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReportTaskMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStopWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getResumeWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWfRunMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWfSpecMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.TaskDefMetrics getTaskDefMetricsWindow(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskDefMetricsWindowMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.WfSpecMetrics getWfSpecMetricsWindow(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfSpecMetricsWindowMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ListTaskMetricsResponse listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTaskDefMetricsMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.ListWfMetricsResponse listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsRequest request) {
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
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDef> putTaskDef(
        io.littlehorse.sdk.common.proto.PutTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDef> getTaskDef(
        io.littlehorse.sdk.common.proto.TaskDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventDef> putExternalEventDef(
        io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventDef> getExternalEventDef(
        io.littlehorse.sdk.common.proto.ExternalEventDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpec> putWfSpec(
        io.littlehorse.sdk.common.proto.PutWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpec> getWfSpec(
        io.littlehorse.sdk.common.proto.WfSpecId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpec> getLatestWfSpec(
        io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetLatestWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDef> putUserTaskDef(
        io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDef> getUserTaskDef(
        io.littlehorse.sdk.common.proto.UserTaskDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDef> getLatestUserTaskDef(
        io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetLatestUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfRun> runWf(
        io.littlehorse.sdk.common.proto.RunWfRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfRun> getWfRun(
        io.littlehorse.sdk.common.proto.WfRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRun> getUserTaskRun(
        io.littlehorse.sdk.common.proto.UserTaskRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> assignUserTaskRun(
        io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAssignUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> completeUserTaskRun(
        io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCompleteUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> cancelUserTaskRun(
        io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRunList> listUserTaskRuns(
        io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListUserTaskRunsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.NodeRun> getNodeRun(
        io.littlehorse.sdk.common.proto.NodeRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.NodeRunList> listNodeRuns(
        io.littlehorse.sdk.common.proto.ListNodeRunsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListNodeRunsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskRun> getTaskRun(
        io.littlehorse.sdk.common.proto.TaskRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.Variable> getVariable(
        io.littlehorse.sdk.common.proto.VariableId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.VariableList> listVariables(
        io.littlehorse.sdk.common.proto.ListVariablesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListVariablesMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEvent> putExternalEvent(
        io.littlehorse.sdk.common.proto.PutExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEvent> getExternalEvent(
        io.littlehorse.sdk.common.proto.ExternalEventId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventList> listExternalEvents(
        io.littlehorse.sdk.common.proto.ListExternalEventsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListExternalEventsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfRunIdList> searchWfRun(
        io.littlehorse.sdk.common.proto.SearchWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.NodeRunIdList> searchNodeRun(
        io.littlehorse.sdk.common.proto.SearchNodeRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchNodeRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskRunIdList> searchTaskRun(
        io.littlehorse.sdk.common.proto.SearchTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRunIdList> searchUserTaskRun(
        io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.VariableIdList> searchVariable(
        io.littlehorse.sdk.common.proto.SearchVariableRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchVariableMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventIdList> searchExternalEvent(
        io.littlehorse.sdk.common.proto.SearchExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchExternalEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDefIdList> searchTaskDef(
        io.littlehorse.sdk.common.proto.SearchTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDefIdList> searchUserTaskDef(
        io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpecIdList> searchWfSpec(
        io.littlehorse.sdk.common.proto.SearchWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventDefIdList> searchExternalEventDef(
        io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> registerTaskWorker(
        io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterTaskWorkerMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> reportTask(
        io.littlehorse.sdk.common.proto.ReportTaskRun request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> stopWfRun(
        io.littlehorse.sdk.common.proto.StopWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> resumeWfRun(
        io.littlehorse.sdk.common.proto.ResumeWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteWfRun(
        io.littlehorse.sdk.common.proto.DeleteWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteTaskDef(
        io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteWfSpec(
        io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWfSpecMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteUserTaskDef(
        io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteExternalEventDef(
        io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDefMetrics> getTaskDefMetricsWindow(
        io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskDefMetricsWindowMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpecMetrics> getWfSpecMetricsWindow(
        io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfSpecMetricsWindowMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> listTaskDefMetrics(
        io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTaskDefMetricsMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListWfMetricsResponse> listWfSpecMetrics(
        io.littlehorse.sdk.common.proto.ListWfMetricsRequest request) {
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
  private static final int METHODID_RUN_WF = 10;
  private static final int METHODID_GET_WF_RUN = 11;
  private static final int METHODID_GET_USER_TASK_RUN = 12;
  private static final int METHODID_ASSIGN_USER_TASK_RUN = 13;
  private static final int METHODID_COMPLETE_USER_TASK_RUN = 14;
  private static final int METHODID_CANCEL_USER_TASK_RUN = 15;
  private static final int METHODID_LIST_USER_TASK_RUNS = 16;
  private static final int METHODID_GET_NODE_RUN = 17;
  private static final int METHODID_LIST_NODE_RUNS = 18;
  private static final int METHODID_GET_TASK_RUN = 19;
  private static final int METHODID_GET_VARIABLE = 20;
  private static final int METHODID_LIST_VARIABLES = 21;
  private static final int METHODID_PUT_EXTERNAL_EVENT = 22;
  private static final int METHODID_GET_EXTERNAL_EVENT = 23;
  private static final int METHODID_LIST_EXTERNAL_EVENTS = 24;
  private static final int METHODID_SEARCH_WF_RUN = 25;
  private static final int METHODID_SEARCH_NODE_RUN = 26;
  private static final int METHODID_SEARCH_TASK_RUN = 27;
  private static final int METHODID_SEARCH_USER_TASK_RUN = 28;
  private static final int METHODID_SEARCH_VARIABLE = 29;
  private static final int METHODID_SEARCH_EXTERNAL_EVENT = 30;
  private static final int METHODID_SEARCH_TASK_DEF = 31;
  private static final int METHODID_SEARCH_USER_TASK_DEF = 32;
  private static final int METHODID_SEARCH_WF_SPEC = 33;
  private static final int METHODID_SEARCH_EXTERNAL_EVENT_DEF = 34;
  private static final int METHODID_REGISTER_TASK_WORKER = 35;
  private static final int METHODID_REPORT_TASK = 36;
  private static final int METHODID_STOP_WF_RUN = 37;
  private static final int METHODID_RESUME_WF_RUN = 38;
  private static final int METHODID_DELETE_WF_RUN = 39;
  private static final int METHODID_DELETE_TASK_DEF = 40;
  private static final int METHODID_DELETE_WF_SPEC = 41;
  private static final int METHODID_DELETE_USER_TASK_DEF = 42;
  private static final int METHODID_DELETE_EXTERNAL_EVENT_DEF = 43;
  private static final int METHODID_GET_TASK_DEF_METRICS_WINDOW = 44;
  private static final int METHODID_GET_WF_SPEC_METRICS_WINDOW = 45;
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
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef>) responseObserver);
          break;
        case METHODID_GET_TASK_DEF:
          serviceImpl.getTaskDef((io.littlehorse.sdk.common.proto.TaskDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef>) responseObserver);
          break;
        case METHODID_PUT_EXTERNAL_EVENT_DEF:
          serviceImpl.putExternalEventDef((io.littlehorse.sdk.common.proto.PutExternalEventDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT_DEF:
          serviceImpl.getExternalEventDef((io.littlehorse.sdk.common.proto.ExternalEventDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef>) responseObserver);
          break;
        case METHODID_PUT_WF_SPEC:
          serviceImpl.putWfSpec((io.littlehorse.sdk.common.proto.PutWfSpecRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec>) responseObserver);
          break;
        case METHODID_GET_WF_SPEC:
          serviceImpl.getWfSpec((io.littlehorse.sdk.common.proto.WfSpecId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec>) responseObserver);
          break;
        case METHODID_GET_LATEST_WF_SPEC:
          serviceImpl.getLatestWfSpec((io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec>) responseObserver);
          break;
        case METHODID_PUT_USER_TASK_DEF:
          serviceImpl.putUserTaskDef((io.littlehorse.sdk.common.proto.PutUserTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef>) responseObserver);
          break;
        case METHODID_GET_USER_TASK_DEF:
          serviceImpl.getUserTaskDef((io.littlehorse.sdk.common.proto.UserTaskDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef>) responseObserver);
          break;
        case METHODID_GET_LATEST_USER_TASK_DEF:
          serviceImpl.getLatestUserTaskDef((io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef>) responseObserver);
          break;
        case METHODID_RUN_WF:
          serviceImpl.runWf((io.littlehorse.sdk.common.proto.RunWfRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun>) responseObserver);
          break;
        case METHODID_GET_WF_RUN:
          serviceImpl.getWfRun((io.littlehorse.sdk.common.proto.WfRunId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun>) responseObserver);
          break;
        case METHODID_GET_USER_TASK_RUN:
          serviceImpl.getUserTaskRun((io.littlehorse.sdk.common.proto.UserTaskRunId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun>) responseObserver);
          break;
        case METHODID_ASSIGN_USER_TASK_RUN:
          serviceImpl.assignUserTaskRun((io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_COMPLETE_USER_TASK_RUN:
          serviceImpl.completeUserTaskRun((io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_CANCEL_USER_TASK_RUN:
          serviceImpl.cancelUserTaskRun((io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_LIST_USER_TASK_RUNS:
          serviceImpl.listUserTaskRuns((io.littlehorse.sdk.common.proto.ListUserTaskRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunList>) responseObserver);
          break;
        case METHODID_GET_NODE_RUN:
          serviceImpl.getNodeRun((io.littlehorse.sdk.common.proto.NodeRunId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRun>) responseObserver);
          break;
        case METHODID_LIST_NODE_RUNS:
          serviceImpl.listNodeRuns((io.littlehorse.sdk.common.proto.ListNodeRunsRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunList>) responseObserver);
          break;
        case METHODID_GET_TASK_RUN:
          serviceImpl.getTaskRun((io.littlehorse.sdk.common.proto.TaskRunId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRun>) responseObserver);
          break;
        case METHODID_GET_VARIABLE:
          serviceImpl.getVariable((io.littlehorse.sdk.common.proto.VariableId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Variable>) responseObserver);
          break;
        case METHODID_LIST_VARIABLES:
          serviceImpl.listVariables((io.littlehorse.sdk.common.proto.ListVariablesRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableList>) responseObserver);
          break;
        case METHODID_PUT_EXTERNAL_EVENT:
          serviceImpl.putExternalEvent((io.littlehorse.sdk.common.proto.PutExternalEventRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT:
          serviceImpl.getExternalEvent((io.littlehorse.sdk.common.proto.ExternalEventId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent>) responseObserver);
          break;
        case METHODID_LIST_EXTERNAL_EVENTS:
          serviceImpl.listExternalEvents((io.littlehorse.sdk.common.proto.ListExternalEventsRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventList>) responseObserver);
          break;
        case METHODID_SEARCH_WF_RUN:
          serviceImpl.searchWfRun((io.littlehorse.sdk.common.proto.SearchWfRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRunIdList>) responseObserver);
          break;
        case METHODID_SEARCH_NODE_RUN:
          serviceImpl.searchNodeRun((io.littlehorse.sdk.common.proto.SearchNodeRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunIdList>) responseObserver);
          break;
        case METHODID_SEARCH_TASK_RUN:
          serviceImpl.searchTaskRun((io.littlehorse.sdk.common.proto.SearchTaskRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunIdList>) responseObserver);
          break;
        case METHODID_SEARCH_USER_TASK_RUN:
          serviceImpl.searchUserTaskRun((io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunIdList>) responseObserver);
          break;
        case METHODID_SEARCH_VARIABLE:
          serviceImpl.searchVariable((io.littlehorse.sdk.common.proto.SearchVariableRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableIdList>) responseObserver);
          break;
        case METHODID_SEARCH_EXTERNAL_EVENT:
          serviceImpl.searchExternalEvent((io.littlehorse.sdk.common.proto.SearchExternalEventRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventIdList>) responseObserver);
          break;
        case METHODID_SEARCH_TASK_DEF:
          serviceImpl.searchTaskDef((io.littlehorse.sdk.common.proto.SearchTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefIdList>) responseObserver);
          break;
        case METHODID_SEARCH_USER_TASK_DEF:
          serviceImpl.searchUserTaskDef((io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDefIdList>) responseObserver);
          break;
        case METHODID_SEARCH_WF_SPEC:
          serviceImpl.searchWfSpec((io.littlehorse.sdk.common.proto.SearchWfSpecRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecIdList>) responseObserver);
          break;
        case METHODID_SEARCH_EXTERNAL_EVENT_DEF:
          serviceImpl.searchExternalEventDef((io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDefIdList>) responseObserver);
          break;
        case METHODID_REGISTER_TASK_WORKER:
          serviceImpl.registerTaskWorker((io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse>) responseObserver);
          break;
        case METHODID_REPORT_TASK:
          serviceImpl.reportTask((io.littlehorse.sdk.common.proto.ReportTaskRun) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_STOP_WF_RUN:
          serviceImpl.stopWfRun((io.littlehorse.sdk.common.proto.StopWfRunRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_RESUME_WF_RUN:
          serviceImpl.resumeWfRun((io.littlehorse.sdk.common.proto.ResumeWfRunRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_WF_RUN:
          serviceImpl.deleteWfRun((io.littlehorse.sdk.common.proto.DeleteWfRunRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_TASK_DEF:
          serviceImpl.deleteTaskDef((io.littlehorse.sdk.common.proto.DeleteTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_WF_SPEC:
          serviceImpl.deleteWfSpec((io.littlehorse.sdk.common.proto.DeleteWfSpecRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_USER_TASK_DEF:
          serviceImpl.deleteUserTaskDef((io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_EXTERNAL_EVENT_DEF:
          serviceImpl.deleteExternalEventDef((io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_GET_TASK_DEF_METRICS_WINDOW:
          serviceImpl.getTaskDefMetricsWindow((io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetrics>) responseObserver);
          break;
        case METHODID_GET_WF_SPEC_METRICS_WINDOW:
          serviceImpl.getWfSpecMetricsWindow((io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetrics>) responseObserver);
          break;
        case METHODID_LIST_TASK_DEF_METRICS:
          serviceImpl.listTaskDefMetrics((io.littlehorse.sdk.common.proto.ListTaskMetricsRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsResponse>) responseObserver);
          break;
        case METHODID_LIST_WF_SPEC_METRICS:
          serviceImpl.listWfSpecMetrics((io.littlehorse.sdk.common.proto.ListWfMetricsRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsResponse>) responseObserver);
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
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskResponse>) responseObserver);
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
              io.littlehorse.sdk.common.proto.TaskDef>(
                service, METHODID_PUT_TASK_DEF)))
        .addMethod(
          getGetTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TaskDefId,
              io.littlehorse.sdk.common.proto.TaskDef>(
                service, METHODID_GET_TASK_DEF)))
        .addMethod(
          getPutExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutExternalEventDefRequest,
              io.littlehorse.sdk.common.proto.ExternalEventDef>(
                service, METHODID_PUT_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getGetExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ExternalEventDefId,
              io.littlehorse.sdk.common.proto.ExternalEventDef>(
                service, METHODID_GET_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getPutWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutWfSpecRequest,
              io.littlehorse.sdk.common.proto.WfSpec>(
                service, METHODID_PUT_WF_SPEC)))
        .addMethod(
          getGetWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WfSpecId,
              io.littlehorse.sdk.common.proto.WfSpec>(
                service, METHODID_GET_WF_SPEC)))
        .addMethod(
          getGetLatestWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest,
              io.littlehorse.sdk.common.proto.WfSpec>(
                service, METHODID_GET_LATEST_WF_SPEC)))
        .addMethod(
          getPutUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutUserTaskDefRequest,
              io.littlehorse.sdk.common.proto.UserTaskDef>(
                service, METHODID_PUT_USER_TASK_DEF)))
        .addMethod(
          getGetUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.UserTaskDefId,
              io.littlehorse.sdk.common.proto.UserTaskDef>(
                service, METHODID_GET_USER_TASK_DEF)))
        .addMethod(
          getGetLatestUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest,
              io.littlehorse.sdk.common.proto.UserTaskDef>(
                service, METHODID_GET_LATEST_USER_TASK_DEF)))
        .addMethod(
          getRunWfMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.RunWfRequest,
              io.littlehorse.sdk.common.proto.WfRun>(
                service, METHODID_RUN_WF)))
        .addMethod(
          getGetWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WfRunId,
              io.littlehorse.sdk.common.proto.WfRun>(
                service, METHODID_GET_WF_RUN)))
        .addMethod(
          getGetUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.UserTaskRunId,
              io.littlehorse.sdk.common.proto.UserTaskRun>(
                service, METHODID_GET_USER_TASK_RUN)))
        .addMethod(
          getAssignUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest,
              com.google.protobuf.Empty>(
                service, METHODID_ASSIGN_USER_TASK_RUN)))
        .addMethod(
          getCompleteUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest,
              com.google.protobuf.Empty>(
                service, METHODID_COMPLETE_USER_TASK_RUN)))
        .addMethod(
          getCancelUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest,
              com.google.protobuf.Empty>(
                service, METHODID_CANCEL_USER_TASK_RUN)))
        .addMethod(
          getListUserTaskRunsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListUserTaskRunRequest,
              io.littlehorse.sdk.common.proto.UserTaskRunList>(
                service, METHODID_LIST_USER_TASK_RUNS)))
        .addMethod(
          getGetNodeRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.NodeRunId,
              io.littlehorse.sdk.common.proto.NodeRun>(
                service, METHODID_GET_NODE_RUN)))
        .addMethod(
          getListNodeRunsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListNodeRunsRequest,
              io.littlehorse.sdk.common.proto.NodeRunList>(
                service, METHODID_LIST_NODE_RUNS)))
        .addMethod(
          getGetTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TaskRunId,
              io.littlehorse.sdk.common.proto.TaskRun>(
                service, METHODID_GET_TASK_RUN)))
        .addMethod(
          getGetVariableMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.VariableId,
              io.littlehorse.sdk.common.proto.Variable>(
                service, METHODID_GET_VARIABLE)))
        .addMethod(
          getListVariablesMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListVariablesRequest,
              io.littlehorse.sdk.common.proto.VariableList>(
                service, METHODID_LIST_VARIABLES)))
        .addMethod(
          getPutExternalEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutExternalEventRequest,
              io.littlehorse.sdk.common.proto.ExternalEvent>(
                service, METHODID_PUT_EXTERNAL_EVENT)))
        .addMethod(
          getGetExternalEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ExternalEventId,
              io.littlehorse.sdk.common.proto.ExternalEvent>(
                service, METHODID_GET_EXTERNAL_EVENT)))
        .addMethod(
          getListExternalEventsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListExternalEventsRequest,
              io.littlehorse.sdk.common.proto.ExternalEventList>(
                service, METHODID_LIST_EXTERNAL_EVENTS)))
        .addMethod(
          getSearchWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchWfRunRequest,
              io.littlehorse.sdk.common.proto.WfRunIdList>(
                service, METHODID_SEARCH_WF_RUN)))
        .addMethod(
          getSearchNodeRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchNodeRunRequest,
              io.littlehorse.sdk.common.proto.NodeRunIdList>(
                service, METHODID_SEARCH_NODE_RUN)))
        .addMethod(
          getSearchTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchTaskRunRequest,
              io.littlehorse.sdk.common.proto.TaskRunIdList>(
                service, METHODID_SEARCH_TASK_RUN)))
        .addMethod(
          getSearchUserTaskRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest,
              io.littlehorse.sdk.common.proto.UserTaskRunIdList>(
                service, METHODID_SEARCH_USER_TASK_RUN)))
        .addMethod(
          getSearchVariableMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchVariableRequest,
              io.littlehorse.sdk.common.proto.VariableIdList>(
                service, METHODID_SEARCH_VARIABLE)))
        .addMethod(
          getSearchExternalEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchExternalEventRequest,
              io.littlehorse.sdk.common.proto.ExternalEventIdList>(
                service, METHODID_SEARCH_EXTERNAL_EVENT)))
        .addMethod(
          getSearchTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchTaskDefRequest,
              io.littlehorse.sdk.common.proto.TaskDefIdList>(
                service, METHODID_SEARCH_TASK_DEF)))
        .addMethod(
          getSearchUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest,
              io.littlehorse.sdk.common.proto.UserTaskDefIdList>(
                service, METHODID_SEARCH_USER_TASK_DEF)))
        .addMethod(
          getSearchWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchWfSpecRequest,
              io.littlehorse.sdk.common.proto.WfSpecIdList>(
                service, METHODID_SEARCH_WF_SPEC)))
        .addMethod(
          getSearchExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest,
              io.littlehorse.sdk.common.proto.ExternalEventDefIdList>(
                service, METHODID_SEARCH_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getRegisterTaskWorkerMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest,
              io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse>(
                service, METHODID_REGISTER_TASK_WORKER)))
        .addMethod(
          getPollTaskMethod(),
          io.grpc.stub.ServerCalls.asyncBidiStreamingCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PollTaskRequest,
              io.littlehorse.sdk.common.proto.PollTaskResponse>(
                service, METHODID_POLL_TASK)))
        .addMethod(
          getReportTaskMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ReportTaskRun,
              com.google.protobuf.Empty>(
                service, METHODID_REPORT_TASK)))
        .addMethod(
          getStopWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.StopWfRunRequest,
              com.google.protobuf.Empty>(
                service, METHODID_STOP_WF_RUN)))
        .addMethod(
          getResumeWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ResumeWfRunRequest,
              com.google.protobuf.Empty>(
                service, METHODID_RESUME_WF_RUN)))
        .addMethod(
          getDeleteWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteWfRunRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_WF_RUN)))
        .addMethod(
          getDeleteTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteTaskDefRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_TASK_DEF)))
        .addMethod(
          getDeleteWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteWfSpecRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_WF_SPEC)))
        .addMethod(
          getDeleteUserTaskDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_USER_TASK_DEF)))
        .addMethod(
          getDeleteExternalEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_EXTERNAL_EVENT_DEF)))
        .addMethod(
          getGetTaskDefMetricsWindowMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest,
              io.littlehorse.sdk.common.proto.TaskDefMetrics>(
                service, METHODID_GET_TASK_DEF_METRICS_WINDOW)))
        .addMethod(
          getGetWfSpecMetricsWindowMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest,
              io.littlehorse.sdk.common.proto.WfSpecMetrics>(
                service, METHODID_GET_WF_SPEC_METRICS_WINDOW)))
        .addMethod(
          getListTaskDefMetricsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListTaskMetricsRequest,
              io.littlehorse.sdk.common.proto.ListTaskMetricsResponse>(
                service, METHODID_LIST_TASK_DEF_METRICS)))
        .addMethod(
          getListWfSpecMetricsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListWfMetricsRequest,
              io.littlehorse.sdk.common.proto.ListWfMetricsResponse>(
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
    private final java.lang.String methodName;

    LHPublicApiMethodDescriptorSupplier(java.lang.String methodName) {
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
              .addMethod(getRunWfMethod())
              .addMethod(getGetWfRunMethod())
              .addMethod(getGetUserTaskRunMethod())
              .addMethod(getAssignUserTaskRunMethod())
              .addMethod(getCompleteUserTaskRunMethod())
              .addMethod(getCancelUserTaskRunMethod())
              .addMethod(getListUserTaskRunsMethod())
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
              .addMethod(getSearchExternalEventMethod())
              .addMethod(getSearchTaskDefMethod())
              .addMethod(getSearchUserTaskDefMethod())
              .addMethod(getSearchWfSpecMethod())
              .addMethod(getSearchExternalEventDefMethod())
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
              .addMethod(getGetTaskDefMetricsWindowMethod())
              .addMethod(getGetWfSpecMetricsWindowMethod())
              .addMethod(getListTaskDefMetricsMethod())
              .addMethod(getListWfSpecMetricsMethod())
              .build();
        }
      }
    }
    return result;
  }
}
