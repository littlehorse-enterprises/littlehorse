package io.littlehorse.sdk.common.proto;

import static io.grpc.MethodDescriptor.generateFullMethodName;

/**
 */
@javax.annotation.Generated(
    value = "by gRPC proto compiler (version 1.57.2)",
    comments = "Source: service.proto")
@io.grpc.stub.annotations.GrpcGenerated
public final class LittleHorseGrpc {

  private LittleHorseGrpc() {}

  public static final java.lang.String SERVICE_NAME = "littlehorse.LittleHorse";

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
    if ((getPutTaskDefMethod = LittleHorseGrpc.getPutTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutTaskDefMethod = LittleHorseGrpc.getPutTaskDefMethod) == null) {
          LittleHorseGrpc.getPutTaskDefMethod = getPutTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutTaskDefRequest, io.littlehorse.sdk.common.proto.TaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutTaskDef"))
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
    if ((getGetTaskDefMethod = LittleHorseGrpc.getGetTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetTaskDefMethod = LittleHorseGrpc.getGetTaskDefMethod) == null) {
          LittleHorseGrpc.getGetTaskDefMethod = getGetTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetTaskDef"))
              .build();
        }
      }
    }
    return getGetTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefId,
      io.littlehorse.sdk.common.proto.TaskWorkerGroup> getGetTaskWorkerGroupMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTaskWorkerGroup",
      requestType = io.littlehorse.sdk.common.proto.TaskDefId.class,
      responseType = io.littlehorse.sdk.common.proto.TaskWorkerGroup.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefId,
      io.littlehorse.sdk.common.proto.TaskWorkerGroup> getGetTaskWorkerGroupMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskWorkerGroup> getGetTaskWorkerGroupMethod;
    if ((getGetTaskWorkerGroupMethod = LittleHorseGrpc.getGetTaskWorkerGroupMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetTaskWorkerGroupMethod = LittleHorseGrpc.getGetTaskWorkerGroupMethod) == null) {
          LittleHorseGrpc.getGetTaskWorkerGroupMethod = getGetTaskWorkerGroupMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskDefId, io.littlehorse.sdk.common.proto.TaskWorkerGroup>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskWorkerGroup"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskWorkerGroup.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetTaskWorkerGroup"))
              .build();
        }
      }
    }
    return getGetTaskWorkerGroupMethod;
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
    if ((getPutExternalEventDefMethod = LittleHorseGrpc.getPutExternalEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutExternalEventDefMethod = LittleHorseGrpc.getPutExternalEventDefMethod) == null) {
          LittleHorseGrpc.getPutExternalEventDefMethod = getPutExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutExternalEventDefRequest, io.littlehorse.sdk.common.proto.ExternalEventDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutExternalEventDef"))
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
    if ((getGetExternalEventDefMethod = LittleHorseGrpc.getGetExternalEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetExternalEventDefMethod = LittleHorseGrpc.getGetExternalEventDefMethod) == null) {
          LittleHorseGrpc.getGetExternalEventDefMethod = getGetExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ExternalEventDefId, io.littlehorse.sdk.common.proto.ExternalEventDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetExternalEventDef"))
              .build();
        }
      }
    }
    return getGetExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventDef> getPutWorkflowEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutWorkflowEventDef",
      requestType = io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WorkflowEventDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventDef> getPutWorkflowEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest, io.littlehorse.sdk.common.proto.WorkflowEventDef> getPutWorkflowEventDefMethod;
    if ((getPutWorkflowEventDefMethod = LittleHorseGrpc.getPutWorkflowEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutWorkflowEventDefMethod = LittleHorseGrpc.getPutWorkflowEventDefMethod) == null) {
          LittleHorseGrpc.getPutWorkflowEventDefMethod = getPutWorkflowEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest, io.littlehorse.sdk.common.proto.WorkflowEventDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutWorkflowEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEventDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutWorkflowEventDef"))
              .build();
        }
      }
    }
    return getPutWorkflowEventDefMethod;
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
    if ((getPutWfSpecMethod = LittleHorseGrpc.getPutWfSpecMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutWfSpecMethod = LittleHorseGrpc.getPutWfSpecMethod) == null) {
          LittleHorseGrpc.getPutWfSpecMethod = getPutWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpec.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutWfSpec"))
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
    if ((getGetWfSpecMethod = LittleHorseGrpc.getGetWfSpecMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetWfSpecMethod = LittleHorseGrpc.getGetWfSpecMethod) == null) {
          LittleHorseGrpc.getGetWfSpecMethod = getGetWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfSpecId, io.littlehorse.sdk.common.proto.WfSpec>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpec.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetWfSpec"))
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
    if ((getGetLatestWfSpecMethod = LittleHorseGrpc.getGetLatestWfSpecMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetLatestWfSpecMethod = LittleHorseGrpc.getGetLatestWfSpecMethod) == null) {
          LittleHorseGrpc.getGetLatestWfSpecMethod = getGetLatestWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLatestWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpec.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetLatestWfSpec"))
              .build();
        }
      }
    }
    return getGetLatestWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.MigrateWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpec> getMigrateWfSpecMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "MigrateWfSpec",
      requestType = io.littlehorse.sdk.common.proto.MigrateWfSpecRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfSpec.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.MigrateWfSpecRequest,
      io.littlehorse.sdk.common.proto.WfSpec> getMigrateWfSpecMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.MigrateWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec> getMigrateWfSpecMethod;
    if ((getMigrateWfSpecMethod = LittleHorseGrpc.getMigrateWfSpecMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getMigrateWfSpecMethod = LittleHorseGrpc.getMigrateWfSpecMethod) == null) {
          LittleHorseGrpc.getMigrateWfSpecMethod = getMigrateWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.MigrateWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpec>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "MigrateWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.MigrateWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpec.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("MigrateWfSpec"))
              .build();
        }
      }
    }
    return getMigrateWfSpecMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutStructDefRequest,
      io.littlehorse.sdk.common.proto.StructDef> getPutStructDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutStructDef",
      requestType = io.littlehorse.sdk.common.proto.PutStructDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.StructDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutStructDefRequest,
      io.littlehorse.sdk.common.proto.StructDef> getPutStructDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutStructDefRequest, io.littlehorse.sdk.common.proto.StructDef> getPutStructDefMethod;
    if ((getPutStructDefMethod = LittleHorseGrpc.getPutStructDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutStructDefMethod = LittleHorseGrpc.getPutStructDefMethod) == null) {
          LittleHorseGrpc.getPutStructDefMethod = getPutStructDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutStructDefRequest, io.littlehorse.sdk.common.proto.StructDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutStructDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutStructDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.StructDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutStructDef"))
              .build();
        }
      }
    }
    return getPutStructDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StructDefId,
      io.littlehorse.sdk.common.proto.StructDef> getGetStructDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetStructDef",
      requestType = io.littlehorse.sdk.common.proto.StructDefId.class,
      responseType = io.littlehorse.sdk.common.proto.StructDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StructDefId,
      io.littlehorse.sdk.common.proto.StructDef> getGetStructDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.StructDefId, io.littlehorse.sdk.common.proto.StructDef> getGetStructDefMethod;
    if ((getGetStructDefMethod = LittleHorseGrpc.getGetStructDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetStructDefMethod = LittleHorseGrpc.getGetStructDefMethod) == null) {
          LittleHorseGrpc.getGetStructDefMethod = getGetStructDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.StructDefId, io.littlehorse.sdk.common.proto.StructDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetStructDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.StructDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.StructDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetStructDef"))
              .build();
        }
      }
    }
    return getGetStructDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest,
      io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse> getValidateStructDefEvolutionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ValidateStructDefEvolution",
      requestType = io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest,
      io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse> getValidateStructDefEvolutionMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest, io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse> getValidateStructDefEvolutionMethod;
    if ((getValidateStructDefEvolutionMethod = LittleHorseGrpc.getValidateStructDefEvolutionMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getValidateStructDefEvolutionMethod = LittleHorseGrpc.getValidateStructDefEvolutionMethod) == null) {
          LittleHorseGrpc.getValidateStructDefEvolutionMethod = getValidateStructDefEvolutionMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest, io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ValidateStructDefEvolution"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ValidateStructDefEvolution"))
              .build();
        }
      }
    }
    return getValidateStructDefEvolutionMethod;
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
    if ((getPutUserTaskDefMethod = LittleHorseGrpc.getPutUserTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutUserTaskDefMethod = LittleHorseGrpc.getPutUserTaskDefMethod) == null) {
          LittleHorseGrpc.getPutUserTaskDefMethod = getPutUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutUserTaskDef"))
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
    if ((getGetUserTaskDefMethod = LittleHorseGrpc.getGetUserTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetUserTaskDefMethod = LittleHorseGrpc.getGetUserTaskDefMethod) == null) {
          LittleHorseGrpc.getGetUserTaskDefMethod = getGetUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.UserTaskDefId, io.littlehorse.sdk.common.proto.UserTaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetUserTaskDef"))
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
    if ((getGetLatestUserTaskDefMethod = LittleHorseGrpc.getGetLatestUserTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetLatestUserTaskDefMethod = LittleHorseGrpc.getGetLatestUserTaskDefMethod) == null) {
          LittleHorseGrpc.getGetLatestUserTaskDefMethod = getGetLatestUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetLatestUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetLatestUserTaskDef"))
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
    if ((getRunWfMethod = LittleHorseGrpc.getRunWfMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getRunWfMethod = LittleHorseGrpc.getRunWfMethod) == null) {
          LittleHorseGrpc.getRunWfMethod = getRunWfMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.RunWfRequest, io.littlehorse.sdk.common.proto.WfRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RunWf"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RunWfRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("RunWf"))
              .build();
        }
      }
    }
    return getRunWfMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ScheduleWfRequest,
      io.littlehorse.sdk.common.proto.ScheduledWfRun> getScheduleWfMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ScheduleWf",
      requestType = io.littlehorse.sdk.common.proto.ScheduleWfRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ScheduledWfRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ScheduleWfRequest,
      io.littlehorse.sdk.common.proto.ScheduledWfRun> getScheduleWfMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ScheduleWfRequest, io.littlehorse.sdk.common.proto.ScheduledWfRun> getScheduleWfMethod;
    if ((getScheduleWfMethod = LittleHorseGrpc.getScheduleWfMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getScheduleWfMethod = LittleHorseGrpc.getScheduleWfMethod) == null) {
          LittleHorseGrpc.getScheduleWfMethod = getScheduleWfMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ScheduleWfRequest, io.littlehorse.sdk.common.proto.ScheduledWfRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ScheduleWf"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ScheduleWfRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ScheduledWfRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ScheduleWf"))
              .build();
        }
      }
    }
    return getScheduleWfMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest,
      io.littlehorse.sdk.common.proto.ScheduledWfRunIdList> getSearchScheduledWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchScheduledWfRun",
      requestType = io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.class,
      responseType = io.littlehorse.sdk.common.proto.ScheduledWfRunIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest,
      io.littlehorse.sdk.common.proto.ScheduledWfRunIdList> getSearchScheduledWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest, io.littlehorse.sdk.common.proto.ScheduledWfRunIdList> getSearchScheduledWfRunMethod;
    if ((getSearchScheduledWfRunMethod = LittleHorseGrpc.getSearchScheduledWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchScheduledWfRunMethod = LittleHorseGrpc.getSearchScheduledWfRunMethod) == null) {
          LittleHorseGrpc.getSearchScheduledWfRunMethod = getSearchScheduledWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest, io.littlehorse.sdk.common.proto.ScheduledWfRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchScheduledWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ScheduledWfRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchScheduledWfRun"))
              .build();
        }
      }
    }
    return getSearchScheduledWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ScheduledWfRunId,
      io.littlehorse.sdk.common.proto.ScheduledWfRun> getGetScheduledWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetScheduledWfRun",
      requestType = io.littlehorse.sdk.common.proto.ScheduledWfRunId.class,
      responseType = io.littlehorse.sdk.common.proto.ScheduledWfRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ScheduledWfRunId,
      io.littlehorse.sdk.common.proto.ScheduledWfRun> getGetScheduledWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ScheduledWfRunId, io.littlehorse.sdk.common.proto.ScheduledWfRun> getGetScheduledWfRunMethod;
    if ((getGetScheduledWfRunMethod = LittleHorseGrpc.getGetScheduledWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetScheduledWfRunMethod = LittleHorseGrpc.getGetScheduledWfRunMethod) == null) {
          LittleHorseGrpc.getGetScheduledWfRunMethod = getGetScheduledWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ScheduledWfRunId, io.littlehorse.sdk.common.proto.ScheduledWfRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetScheduledWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ScheduledWfRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ScheduledWfRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetScheduledWfRun"))
              .build();
        }
      }
    }
    return getGetScheduledWfRunMethod;
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
    if ((getGetWfRunMethod = LittleHorseGrpc.getGetWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetWfRunMethod = LittleHorseGrpc.getGetWfRunMethod) == null) {
          LittleHorseGrpc.getGetWfRunMethod = getGetWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfRunId, io.littlehorse.sdk.common.proto.WfRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetWfRun"))
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
    if ((getGetUserTaskRunMethod = LittleHorseGrpc.getGetUserTaskRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetUserTaskRunMethod = LittleHorseGrpc.getGetUserTaskRunMethod) == null) {
          LittleHorseGrpc.getGetUserTaskRunMethod = getGetUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.UserTaskRunId, io.littlehorse.sdk.common.proto.UserTaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetUserTaskRun"))
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
    if ((getAssignUserTaskRunMethod = LittleHorseGrpc.getAssignUserTaskRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getAssignUserTaskRunMethod = LittleHorseGrpc.getAssignUserTaskRunMethod) == null) {
          LittleHorseGrpc.getAssignUserTaskRunMethod = getAssignUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AssignUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("AssignUserTaskRun"))
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
    if ((getCompleteUserTaskRunMethod = LittleHorseGrpc.getCompleteUserTaskRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getCompleteUserTaskRunMethod = LittleHorseGrpc.getCompleteUserTaskRunMethod) == null) {
          LittleHorseGrpc.getCompleteUserTaskRunMethod = getCompleteUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CompleteUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("CompleteUserTaskRun"))
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
    if ((getCancelUserTaskRunMethod = LittleHorseGrpc.getCancelUserTaskRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getCancelUserTaskRunMethod = LittleHorseGrpc.getCancelUserTaskRunMethod) == null) {
          LittleHorseGrpc.getCancelUserTaskRunMethod = getCancelUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "CancelUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("CancelUserTaskRun"))
              .build();
        }
      }
    }
    return getCancelUserTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getSaveUserTaskRunProgressMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SaveUserTaskRunProgress",
      requestType = io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getSaveUserTaskRunProgressMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest, io.littlehorse.sdk.common.proto.UserTaskRun> getSaveUserTaskRunProgressMethod;
    if ((getSaveUserTaskRunProgressMethod = LittleHorseGrpc.getSaveUserTaskRunProgressMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSaveUserTaskRunProgressMethod = LittleHorseGrpc.getSaveUserTaskRunProgressMethod) == null) {
          LittleHorseGrpc.getSaveUserTaskRunProgressMethod = getSaveUserTaskRunProgressMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest, io.littlehorse.sdk.common.proto.UserTaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SaveUserTaskRunProgress"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SaveUserTaskRunProgress"))
              .build();
        }
      }
    }
    return getSaveUserTaskRunProgressMethod;
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
    if ((getListUserTaskRunsMethod = LittleHorseGrpc.getListUserTaskRunsMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListUserTaskRunsMethod = LittleHorseGrpc.getListUserTaskRunsMethod) == null) {
          LittleHorseGrpc.getListUserTaskRunsMethod = getListUserTaskRunsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListUserTaskRunRequest, io.littlehorse.sdk.common.proto.UserTaskRunList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListUserTaskRuns"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRunList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListUserTaskRuns"))
              .build();
        }
      }
    }
    return getListUserTaskRunsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getPutUserTaskRunCommentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutUserTaskRunComment",
      requestType = io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getPutUserTaskRunCommentMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest, io.littlehorse.sdk.common.proto.UserTaskRun> getPutUserTaskRunCommentMethod;
    if ((getPutUserTaskRunCommentMethod = LittleHorseGrpc.getPutUserTaskRunCommentMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutUserTaskRunCommentMethod = LittleHorseGrpc.getPutUserTaskRunCommentMethod) == null) {
          LittleHorseGrpc.getPutUserTaskRunCommentMethod = getPutUserTaskRunCommentMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest, io.littlehorse.sdk.common.proto.UserTaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutUserTaskRunComment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutUserTaskRunComment"))
              .build();
        }
      }
    }
    return getPutUserTaskRunCommentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getEditUserTaskRunCommentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "EditUserTaskRunComment",
      requestType = io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getEditUserTaskRunCommentMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest, io.littlehorse.sdk.common.proto.UserTaskRun> getEditUserTaskRunCommentMethod;
    if ((getEditUserTaskRunCommentMethod = LittleHorseGrpc.getEditUserTaskRunCommentMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getEditUserTaskRunCommentMethod = LittleHorseGrpc.getEditUserTaskRunCommentMethod) == null) {
          LittleHorseGrpc.getEditUserTaskRunCommentMethod = getEditUserTaskRunCommentMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest, io.littlehorse.sdk.common.proto.UserTaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "EditUserTaskRunComment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("EditUserTaskRunComment"))
              .build();
        }
      }
    }
    return getEditUserTaskRunCommentMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getDeleteUserTaskRunCommentMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteUserTaskRunComment",
      requestType = io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest.class,
      responseType = io.littlehorse.sdk.common.proto.UserTaskRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest,
      io.littlehorse.sdk.common.proto.UserTaskRun> getDeleteUserTaskRunCommentMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest, io.littlehorse.sdk.common.proto.UserTaskRun> getDeleteUserTaskRunCommentMethod;
    if ((getDeleteUserTaskRunCommentMethod = LittleHorseGrpc.getDeleteUserTaskRunCommentMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteUserTaskRunCommentMethod = LittleHorseGrpc.getDeleteUserTaskRunCommentMethod) == null) {
          LittleHorseGrpc.getDeleteUserTaskRunCommentMethod = getDeleteUserTaskRunCommentMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest, io.littlehorse.sdk.common.proto.UserTaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteUserTaskRunComment"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteUserTaskRunComment"))
              .build();
        }
      }
    }
    return getDeleteUserTaskRunCommentMethod;
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
    if ((getGetNodeRunMethod = LittleHorseGrpc.getGetNodeRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetNodeRunMethod = LittleHorseGrpc.getGetNodeRunMethod) == null) {
          LittleHorseGrpc.getGetNodeRunMethod = getGetNodeRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.NodeRunId, io.littlehorse.sdk.common.proto.NodeRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetNodeRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetNodeRun"))
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
    if ((getListNodeRunsMethod = LittleHorseGrpc.getListNodeRunsMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListNodeRunsMethod = LittleHorseGrpc.getListNodeRunsMethod) == null) {
          LittleHorseGrpc.getListNodeRunsMethod = getListNodeRunsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListNodeRunsRequest, io.littlehorse.sdk.common.proto.NodeRunList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListNodeRuns"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListNodeRunsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRunList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListNodeRuns"))
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
    if ((getGetTaskRunMethod = LittleHorseGrpc.getGetTaskRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetTaskRunMethod = LittleHorseGrpc.getGetTaskRunMethod) == null) {
          LittleHorseGrpc.getGetTaskRunMethod = getGetTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskRunId, io.littlehorse.sdk.common.proto.TaskRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRunId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetTaskRun"))
              .build();
        }
      }
    }
    return getGetTaskRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskRunsRequest,
      io.littlehorse.sdk.common.proto.TaskRunList> getListTaskRunsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListTaskRuns",
      requestType = io.littlehorse.sdk.common.proto.ListTaskRunsRequest.class,
      responseType = io.littlehorse.sdk.common.proto.TaskRunList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskRunsRequest,
      io.littlehorse.sdk.common.proto.TaskRunList> getListTaskRunsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListTaskRunsRequest, io.littlehorse.sdk.common.proto.TaskRunList> getListTaskRunsMethod;
    if ((getListTaskRunsMethod = LittleHorseGrpc.getListTaskRunsMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListTaskRunsMethod = LittleHorseGrpc.getListTaskRunsMethod) == null) {
          LittleHorseGrpc.getListTaskRunsMethod = getListTaskRunsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListTaskRunsRequest, io.littlehorse.sdk.common.proto.TaskRunList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTaskRuns"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListTaskRunsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRunList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListTaskRuns"))
              .build();
        }
      }
    }
    return getListTaskRunsMethod;
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
    if ((getGetVariableMethod = LittleHorseGrpc.getGetVariableMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetVariableMethod = LittleHorseGrpc.getGetVariableMethod) == null) {
          LittleHorseGrpc.getGetVariableMethod = getGetVariableMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.VariableId, io.littlehorse.sdk.common.proto.Variable>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetVariable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.VariableId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.Variable.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetVariable"))
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
    if ((getListVariablesMethod = LittleHorseGrpc.getListVariablesMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListVariablesMethod = LittleHorseGrpc.getListVariablesMethod) == null) {
          LittleHorseGrpc.getListVariablesMethod = getListVariablesMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListVariablesRequest, io.littlehorse.sdk.common.proto.VariableList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListVariables"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListVariablesRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.VariableList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListVariables"))
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
    if ((getPutExternalEventMethod = LittleHorseGrpc.getPutExternalEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutExternalEventMethod = LittleHorseGrpc.getPutExternalEventMethod) == null) {
          LittleHorseGrpc.getPutExternalEventMethod = getPutExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutExternalEventRequest, io.littlehorse.sdk.common.proto.ExternalEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutExternalEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutExternalEvent"))
              .build();
        }
      }
    }
    return getPutExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest,
      io.littlehorse.sdk.common.proto.CorrelatedEvent> getPutCorrelatedEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutCorrelatedEvent",
      requestType = io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest.class,
      responseType = io.littlehorse.sdk.common.proto.CorrelatedEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest,
      io.littlehorse.sdk.common.proto.CorrelatedEvent> getPutCorrelatedEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest, io.littlehorse.sdk.common.proto.CorrelatedEvent> getPutCorrelatedEventMethod;
    if ((getPutCorrelatedEventMethod = LittleHorseGrpc.getPutCorrelatedEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutCorrelatedEventMethod = LittleHorseGrpc.getPutCorrelatedEventMethod) == null) {
          LittleHorseGrpc.getPutCorrelatedEventMethod = getPutCorrelatedEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest, io.littlehorse.sdk.common.proto.CorrelatedEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutCorrelatedEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CorrelatedEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutCorrelatedEvent"))
              .build();
        }
      }
    }
    return getPutCorrelatedEventMethod;
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
    if ((getGetExternalEventMethod = LittleHorseGrpc.getGetExternalEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetExternalEventMethod = LittleHorseGrpc.getGetExternalEventMethod) == null) {
          LittleHorseGrpc.getGetExternalEventMethod = getGetExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ExternalEventId, io.littlehorse.sdk.common.proto.ExternalEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetExternalEvent"))
              .build();
        }
      }
    }
    return getGetExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CorrelatedEventId,
      io.littlehorse.sdk.common.proto.CorrelatedEvent> getGetCorrelatedEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetCorrelatedEvent",
      requestType = io.littlehorse.sdk.common.proto.CorrelatedEventId.class,
      responseType = io.littlehorse.sdk.common.proto.CorrelatedEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CorrelatedEventId,
      io.littlehorse.sdk.common.proto.CorrelatedEvent> getGetCorrelatedEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.CorrelatedEventId, io.littlehorse.sdk.common.proto.CorrelatedEvent> getGetCorrelatedEventMethod;
    if ((getGetCorrelatedEventMethod = LittleHorseGrpc.getGetCorrelatedEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetCorrelatedEventMethod = LittleHorseGrpc.getGetCorrelatedEventMethod) == null) {
          LittleHorseGrpc.getGetCorrelatedEventMethod = getGetCorrelatedEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.CorrelatedEventId, io.littlehorse.sdk.common.proto.CorrelatedEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetCorrelatedEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CorrelatedEventId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CorrelatedEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetCorrelatedEvent"))
              .build();
        }
      }
    }
    return getGetCorrelatedEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest,
      io.littlehorse.sdk.common.proto.WorkflowEvent> getAwaitWorkflowEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "AwaitWorkflowEvent",
      requestType = io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WorkflowEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest,
      io.littlehorse.sdk.common.proto.WorkflowEvent> getAwaitWorkflowEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest, io.littlehorse.sdk.common.proto.WorkflowEvent> getAwaitWorkflowEventMethod;
    if ((getAwaitWorkflowEventMethod = LittleHorseGrpc.getAwaitWorkflowEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getAwaitWorkflowEventMethod = LittleHorseGrpc.getAwaitWorkflowEventMethod) == null) {
          LittleHorseGrpc.getAwaitWorkflowEventMethod = getAwaitWorkflowEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest, io.littlehorse.sdk.common.proto.WorkflowEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "AwaitWorkflowEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("AwaitWorkflowEvent"))
              .build();
        }
      }
    }
    return getAwaitWorkflowEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WorkflowEventDefId,
      io.littlehorse.sdk.common.proto.WorkflowEventDef> getGetWorkflowEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWorkflowEventDef",
      requestType = io.littlehorse.sdk.common.proto.WorkflowEventDefId.class,
      responseType = io.littlehorse.sdk.common.proto.WorkflowEventDef.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WorkflowEventDefId,
      io.littlehorse.sdk.common.proto.WorkflowEventDef> getGetWorkflowEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDef> getGetWorkflowEventDefMethod;
    if ((getGetWorkflowEventDefMethod = LittleHorseGrpc.getGetWorkflowEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetWorkflowEventDefMethod = LittleHorseGrpc.getGetWorkflowEventDefMethod) == null) {
          LittleHorseGrpc.getGetWorkflowEventDefMethod = getGetWorkflowEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WorkflowEventDefId, io.littlehorse.sdk.common.proto.WorkflowEventDef>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWorkflowEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEventDefId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEventDef.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetWorkflowEventDef"))
              .build();
        }
      }
    }
    return getGetWorkflowEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WorkflowEventId,
      io.littlehorse.sdk.common.proto.WorkflowEvent> getGetWorkflowEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetWorkflowEvent",
      requestType = io.littlehorse.sdk.common.proto.WorkflowEventId.class,
      responseType = io.littlehorse.sdk.common.proto.WorkflowEvent.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WorkflowEventId,
      io.littlehorse.sdk.common.proto.WorkflowEvent> getGetWorkflowEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.WorkflowEventId, io.littlehorse.sdk.common.proto.WorkflowEvent> getGetWorkflowEventMethod;
    if ((getGetWorkflowEventMethod = LittleHorseGrpc.getGetWorkflowEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetWorkflowEventMethod = LittleHorseGrpc.getGetWorkflowEventMethod) == null) {
          LittleHorseGrpc.getGetWorkflowEventMethod = getGetWorkflowEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WorkflowEventId, io.littlehorse.sdk.common.proto.WorkflowEvent>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWorkflowEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEventId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEvent.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetWorkflowEvent"))
              .build();
        }
      }
    }
    return getGetWorkflowEventMethod;
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
    if ((getListExternalEventsMethod = LittleHorseGrpc.getListExternalEventsMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListExternalEventsMethod = LittleHorseGrpc.getListExternalEventsMethod) == null) {
          LittleHorseGrpc.getListExternalEventsMethod = getListExternalEventsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListExternalEventsRequest, io.littlehorse.sdk.common.proto.ExternalEventList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListExternalEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListExternalEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListExternalEvents"))
              .build();
        }
      }
    }
    return getListExternalEventsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventList> getListWorkflowEventsMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "ListWorkflowEvents",
      requestType = io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WorkflowEventList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventList> getListWorkflowEventsMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest, io.littlehorse.sdk.common.proto.WorkflowEventList> getListWorkflowEventsMethod;
    if ((getListWorkflowEventsMethod = LittleHorseGrpc.getListWorkflowEventsMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListWorkflowEventsMethod = LittleHorseGrpc.getListWorkflowEventsMethod) == null) {
          LittleHorseGrpc.getListWorkflowEventsMethod = getListWorkflowEventsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest, io.littlehorse.sdk.common.proto.WorkflowEventList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListWorkflowEvents"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEventList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListWorkflowEvents"))
              .build();
        }
      }
    }
    return getListWorkflowEventsMethod;
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
    if ((getSearchWfRunMethod = LittleHorseGrpc.getSearchWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchWfRunMethod = LittleHorseGrpc.getSearchWfRunMethod) == null) {
          LittleHorseGrpc.getSearchWfRunMethod = getSearchWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWfRunRequest, io.littlehorse.sdk.common.proto.WfRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchWfRun"))
              .build();
        }
      }
    }
    return getSearchWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest,
      io.littlehorse.sdk.common.proto.CorrelatedEventIdList> getSearchCorrelatedEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchCorrelatedEvent",
      requestType = io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest.class,
      responseType = io.littlehorse.sdk.common.proto.CorrelatedEventIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest,
      io.littlehorse.sdk.common.proto.CorrelatedEventIdList> getSearchCorrelatedEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest, io.littlehorse.sdk.common.proto.CorrelatedEventIdList> getSearchCorrelatedEventMethod;
    if ((getSearchCorrelatedEventMethod = LittleHorseGrpc.getSearchCorrelatedEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchCorrelatedEventMethod = LittleHorseGrpc.getSearchCorrelatedEventMethod) == null) {
          LittleHorseGrpc.getSearchCorrelatedEventMethod = getSearchCorrelatedEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest, io.littlehorse.sdk.common.proto.CorrelatedEventIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchCorrelatedEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.CorrelatedEventIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchCorrelatedEvent"))
              .build();
        }
      }
    }
    return getSearchCorrelatedEventMethod;
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
    if ((getSearchNodeRunMethod = LittleHorseGrpc.getSearchNodeRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchNodeRunMethod = LittleHorseGrpc.getSearchNodeRunMethod) == null) {
          LittleHorseGrpc.getSearchNodeRunMethod = getSearchNodeRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchNodeRunRequest, io.littlehorse.sdk.common.proto.NodeRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchNodeRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchNodeRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.NodeRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchNodeRun"))
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
    if ((getSearchTaskRunMethod = LittleHorseGrpc.getSearchTaskRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchTaskRunMethod = LittleHorseGrpc.getSearchTaskRunMethod) == null) {
          LittleHorseGrpc.getSearchTaskRunMethod = getSearchTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchTaskRunRequest, io.littlehorse.sdk.common.proto.TaskRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchTaskRun"))
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
    if ((getSearchUserTaskRunMethod = LittleHorseGrpc.getSearchUserTaskRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchUserTaskRunMethod = LittleHorseGrpc.getSearchUserTaskRunMethod) == null) {
          LittleHorseGrpc.getSearchUserTaskRunMethod = getSearchUserTaskRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest, io.littlehorse.sdk.common.proto.UserTaskRunIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchUserTaskRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskRunIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchUserTaskRun"))
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
    if ((getSearchVariableMethod = LittleHorseGrpc.getSearchVariableMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchVariableMethod = LittleHorseGrpc.getSearchVariableMethod) == null) {
          LittleHorseGrpc.getSearchVariableMethod = getSearchVariableMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchVariableRequest, io.littlehorse.sdk.common.proto.VariableIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchVariable"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchVariableRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.VariableIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchVariable"))
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
    if ((getSearchExternalEventMethod = LittleHorseGrpc.getSearchExternalEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchExternalEventMethod = LittleHorseGrpc.getSearchExternalEventMethod) == null) {
          LittleHorseGrpc.getSearchExternalEventMethod = getSearchExternalEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchExternalEventRequest, io.littlehorse.sdk.common.proto.ExternalEventIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchExternalEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchExternalEvent"))
              .build();
        }
      }
    }
    return getSearchExternalEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventIdList> getSearchWorkflowEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchWorkflowEvent",
      requestType = io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WorkflowEventIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventIdList> getSearchWorkflowEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest, io.littlehorse.sdk.common.proto.WorkflowEventIdList> getSearchWorkflowEventMethod;
    if ((getSearchWorkflowEventMethod = LittleHorseGrpc.getSearchWorkflowEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchWorkflowEventMethod = LittleHorseGrpc.getSearchWorkflowEventMethod) == null) {
          LittleHorseGrpc.getSearchWorkflowEventMethod = getSearchWorkflowEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest, io.littlehorse.sdk.common.proto.WorkflowEventIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWorkflowEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEventIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchWorkflowEvent"))
              .build();
        }
      }
    }
    return getSearchWorkflowEventMethod;
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
    if ((getSearchTaskDefMethod = LittleHorseGrpc.getSearchTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchTaskDefMethod = LittleHorseGrpc.getSearchTaskDefMethod) == null) {
          LittleHorseGrpc.getSearchTaskDefMethod = getSearchTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchTaskDefRequest, io.littlehorse.sdk.common.proto.TaskDefIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchTaskDef"))
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
    if ((getSearchUserTaskDefMethod = LittleHorseGrpc.getSearchUserTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchUserTaskDefMethod = LittleHorseGrpc.getSearchUserTaskDefMethod) == null) {
          LittleHorseGrpc.getSearchUserTaskDefMethod = getSearchUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest, io.littlehorse.sdk.common.proto.UserTaskDefIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.UserTaskDefIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchUserTaskDef"))
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
    if ((getSearchWfSpecMethod = LittleHorseGrpc.getSearchWfSpecMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchWfSpecMethod = LittleHorseGrpc.getSearchWfSpecMethod) == null) {
          LittleHorseGrpc.getSearchWfSpecMethod = getSearchWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWfSpecRequest, io.littlehorse.sdk.common.proto.WfSpecIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchWfSpec"))
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
    if ((getSearchExternalEventDefMethod = LittleHorseGrpc.getSearchExternalEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchExternalEventDefMethod = LittleHorseGrpc.getSearchExternalEventDefMethod) == null) {
          LittleHorseGrpc.getSearchExternalEventDefMethod = getSearchExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest, io.littlehorse.sdk.common.proto.ExternalEventDefIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ExternalEventDefIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchExternalEventDef"))
              .build();
        }
      }
    }
    return getSearchExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventDefIdList> getSearchWorkflowEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchWorkflowEventDef",
      requestType = io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WorkflowEventDefIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest,
      io.littlehorse.sdk.common.proto.WorkflowEventDefIdList> getSearchWorkflowEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest, io.littlehorse.sdk.common.proto.WorkflowEventDefIdList> getSearchWorkflowEventDefMethod;
    if ((getSearchWorkflowEventDefMethod = LittleHorseGrpc.getSearchWorkflowEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchWorkflowEventDefMethod = LittleHorseGrpc.getSearchWorkflowEventDefMethod) == null) {
          LittleHorseGrpc.getSearchWorkflowEventDefMethod = getSearchWorkflowEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest, io.littlehorse.sdk.common.proto.WorkflowEventDefIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchWorkflowEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WorkflowEventDefIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchWorkflowEventDef"))
              .build();
        }
      }
    }
    return getSearchWorkflowEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTenantRequest,
      io.littlehorse.sdk.common.proto.TenantIdList> getSearchTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchTenant",
      requestType = io.littlehorse.sdk.common.proto.SearchTenantRequest.class,
      responseType = io.littlehorse.sdk.common.proto.TenantIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTenantRequest,
      io.littlehorse.sdk.common.proto.TenantIdList> getSearchTenantMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchTenantRequest, io.littlehorse.sdk.common.proto.TenantIdList> getSearchTenantMethod;
    if ((getSearchTenantMethod = LittleHorseGrpc.getSearchTenantMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchTenantMethod = LittleHorseGrpc.getSearchTenantMethod) == null) {
          LittleHorseGrpc.getSearchTenantMethod = getSearchTenantMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchTenantRequest, io.littlehorse.sdk.common.proto.TenantIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TenantIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchTenant"))
              .build();
        }
      }
    }
    return getSearchTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchPrincipalRequest,
      io.littlehorse.sdk.common.proto.PrincipalIdList> getSearchPrincipalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "SearchPrincipal",
      requestType = io.littlehorse.sdk.common.proto.SearchPrincipalRequest.class,
      responseType = io.littlehorse.sdk.common.proto.PrincipalIdList.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchPrincipalRequest,
      io.littlehorse.sdk.common.proto.PrincipalIdList> getSearchPrincipalMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.SearchPrincipalRequest, io.littlehorse.sdk.common.proto.PrincipalIdList> getSearchPrincipalMethod;
    if ((getSearchPrincipalMethod = LittleHorseGrpc.getSearchPrincipalMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getSearchPrincipalMethod = LittleHorseGrpc.getSearchPrincipalMethod) == null) {
          LittleHorseGrpc.getSearchPrincipalMethod = getSearchPrincipalMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.SearchPrincipalRequest, io.littlehorse.sdk.common.proto.PrincipalIdList>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "SearchPrincipal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.SearchPrincipalRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PrincipalIdList.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("SearchPrincipal"))
              .build();
        }
      }
    }
    return getSearchPrincipalMethod;
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
    if ((getRegisterTaskWorkerMethod = LittleHorseGrpc.getRegisterTaskWorkerMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getRegisterTaskWorkerMethod = LittleHorseGrpc.getRegisterTaskWorkerMethod) == null) {
          LittleHorseGrpc.getRegisterTaskWorkerMethod = getRegisterTaskWorkerMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest, io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RegisterTaskWorker"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("RegisterTaskWorker"))
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
    if ((getPollTaskMethod = LittleHorseGrpc.getPollTaskMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPollTaskMethod = LittleHorseGrpc.getPollTaskMethod) == null) {
          LittleHorseGrpc.getPollTaskMethod = getPollTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PollTaskRequest, io.littlehorse.sdk.common.proto.PollTaskResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.BIDI_STREAMING)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PollTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PollTaskRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PollTaskResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PollTask"))
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
    if ((getReportTaskMethod = LittleHorseGrpc.getReportTaskMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getReportTaskMethod = LittleHorseGrpc.getReportTaskMethod) == null) {
          LittleHorseGrpc.getReportTaskMethod = getReportTaskMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ReportTaskRun, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ReportTask"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ReportTaskRun.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ReportTask"))
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
    if ((getStopWfRunMethod = LittleHorseGrpc.getStopWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getStopWfRunMethod = LittleHorseGrpc.getStopWfRunMethod) == null) {
          LittleHorseGrpc.getStopWfRunMethod = getStopWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.StopWfRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "StopWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.StopWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("StopWfRun"))
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
    if ((getResumeWfRunMethod = LittleHorseGrpc.getResumeWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getResumeWfRunMethod = LittleHorseGrpc.getResumeWfRunMethod) == null) {
          LittleHorseGrpc.getResumeWfRunMethod = getResumeWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ResumeWfRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ResumeWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ResumeWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ResumeWfRun"))
              .build();
        }
      }
    }
    return getResumeWfRunMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RescueThreadRunRequest,
      io.littlehorse.sdk.common.proto.WfRun> getRescueThreadRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "RescueThreadRun",
      requestType = io.littlehorse.sdk.common.proto.RescueThreadRunRequest.class,
      responseType = io.littlehorse.sdk.common.proto.WfRun.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RescueThreadRunRequest,
      io.littlehorse.sdk.common.proto.WfRun> getRescueThreadRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.RescueThreadRunRequest, io.littlehorse.sdk.common.proto.WfRun> getRescueThreadRunMethod;
    if ((getRescueThreadRunMethod = LittleHorseGrpc.getRescueThreadRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getRescueThreadRunMethod = LittleHorseGrpc.getRescueThreadRunMethod) == null) {
          LittleHorseGrpc.getRescueThreadRunMethod = getRescueThreadRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.RescueThreadRunRequest, io.littlehorse.sdk.common.proto.WfRun>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "RescueThreadRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.RescueThreadRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfRun.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("RescueThreadRun"))
              .build();
        }
      }
    }
    return getRescueThreadRunMethod;
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
    if ((getDeleteWfRunMethod = LittleHorseGrpc.getDeleteWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteWfRunMethod = LittleHorseGrpc.getDeleteWfRunMethod) == null) {
          LittleHorseGrpc.getDeleteWfRunMethod = getDeleteWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteWfRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteWfRun"))
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
    if ((getDeleteTaskDefMethod = LittleHorseGrpc.getDeleteTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteTaskDefMethod = LittleHorseGrpc.getDeleteTaskDefMethod) == null) {
          LittleHorseGrpc.getDeleteTaskDefMethod = getDeleteTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteTaskDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteTaskDef"))
              .build();
        }
      }
    }
    return getDeleteTaskDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteStructDefRequest,
      com.google.protobuf.Empty> getDeleteStructDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteStructDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteStructDefRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteStructDefRequest,
      com.google.protobuf.Empty> getDeleteStructDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteStructDefRequest, com.google.protobuf.Empty> getDeleteStructDefMethod;
    if ((getDeleteStructDefMethod = LittleHorseGrpc.getDeleteStructDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteStructDefMethod = LittleHorseGrpc.getDeleteStructDefMethod) == null) {
          LittleHorseGrpc.getDeleteStructDefMethod = getDeleteStructDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteStructDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteStructDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteStructDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteStructDef"))
              .build();
        }
      }
    }
    return getDeleteStructDefMethod;
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
    if ((getDeleteWfSpecMethod = LittleHorseGrpc.getDeleteWfSpecMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteWfSpecMethod = LittleHorseGrpc.getDeleteWfSpecMethod) == null) {
          LittleHorseGrpc.getDeleteWfSpecMethod = getDeleteWfSpecMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteWfSpecRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWfSpec"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteWfSpecRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteWfSpec"))
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
    if ((getDeleteUserTaskDefMethod = LittleHorseGrpc.getDeleteUserTaskDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteUserTaskDefMethod = LittleHorseGrpc.getDeleteUserTaskDefMethod) == null) {
          LittleHorseGrpc.getDeleteUserTaskDefMethod = getDeleteUserTaskDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteUserTaskDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteUserTaskDef"))
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
    if ((getDeleteExternalEventDefMethod = LittleHorseGrpc.getDeleteExternalEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteExternalEventDefMethod = LittleHorseGrpc.getDeleteExternalEventDefMethod) == null) {
          LittleHorseGrpc.getDeleteExternalEventDefMethod = getDeleteExternalEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteExternalEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteExternalEventDef"))
              .build();
        }
      }
    }
    return getDeleteExternalEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest,
      com.google.protobuf.Empty> getDeleteCorrelatedEventMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteCorrelatedEvent",
      requestType = io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest,
      com.google.protobuf.Empty> getDeleteCorrelatedEventMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest, com.google.protobuf.Empty> getDeleteCorrelatedEventMethod;
    if ((getDeleteCorrelatedEventMethod = LittleHorseGrpc.getDeleteCorrelatedEventMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteCorrelatedEventMethod = LittleHorseGrpc.getDeleteCorrelatedEventMethod) == null) {
          LittleHorseGrpc.getDeleteCorrelatedEventMethod = getDeleteCorrelatedEventMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteCorrelatedEvent"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteCorrelatedEvent"))
              .build();
        }
      }
    }
    return getDeleteCorrelatedEventMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest,
      com.google.protobuf.Empty> getDeleteWorkflowEventDefMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteWorkflowEventDef",
      requestType = io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest,
      com.google.protobuf.Empty> getDeleteWorkflowEventDefMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest, com.google.protobuf.Empty> getDeleteWorkflowEventDefMethod;
    if ((getDeleteWorkflowEventDefMethod = LittleHorseGrpc.getDeleteWorkflowEventDefMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteWorkflowEventDefMethod = LittleHorseGrpc.getDeleteWorkflowEventDefMethod) == null) {
          LittleHorseGrpc.getDeleteWorkflowEventDefMethod = getDeleteWorkflowEventDefMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteWorkflowEventDef"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteWorkflowEventDef"))
              .build();
        }
      }
    }
    return getDeleteWorkflowEventDefMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeletePrincipalRequest,
      com.google.protobuf.Empty> getDeletePrincipalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeletePrincipal",
      requestType = io.littlehorse.sdk.common.proto.DeletePrincipalRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeletePrincipalRequest,
      com.google.protobuf.Empty> getDeletePrincipalMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeletePrincipalRequest, com.google.protobuf.Empty> getDeletePrincipalMethod;
    if ((getDeletePrincipalMethod = LittleHorseGrpc.getDeletePrincipalMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeletePrincipalMethod = LittleHorseGrpc.getDeletePrincipalMethod) == null) {
          LittleHorseGrpc.getDeletePrincipalMethod = getDeletePrincipalMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeletePrincipalRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeletePrincipal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeletePrincipalRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeletePrincipal"))
              .build();
        }
      }
    }
    return getDeletePrincipalMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest,
      com.google.protobuf.Empty> getDeleteScheduledWfRunMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "DeleteScheduledWfRun",
      requestType = io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest.class,
      responseType = com.google.protobuf.Empty.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest,
      com.google.protobuf.Empty> getDeleteScheduledWfRunMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest, com.google.protobuf.Empty> getDeleteScheduledWfRunMethod;
    if ((getDeleteScheduledWfRunMethod = LittleHorseGrpc.getDeleteScheduledWfRunMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getDeleteScheduledWfRunMethod = LittleHorseGrpc.getDeleteScheduledWfRunMethod) == null) {
          LittleHorseGrpc.getDeleteScheduledWfRunMethod = getDeleteScheduledWfRunMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest, com.google.protobuf.Empty>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "DeleteScheduledWfRun"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("DeleteScheduledWfRun"))
              .build();
        }
      }
    }
    return getDeleteScheduledWfRunMethod;
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
    if ((getGetTaskDefMetricsWindowMethod = LittleHorseGrpc.getGetTaskDefMetricsWindowMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetTaskDefMetricsWindowMethod = LittleHorseGrpc.getGetTaskDefMetricsWindowMethod) == null) {
          LittleHorseGrpc.getGetTaskDefMetricsWindowMethod = getGetTaskDefMetricsWindowMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest, io.littlehorse.sdk.common.proto.TaskDefMetrics>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTaskDefMetricsWindow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TaskDefMetrics.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetTaskDefMetricsWindow"))
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
    if ((getGetWfSpecMetricsWindowMethod = LittleHorseGrpc.getGetWfSpecMetricsWindowMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetWfSpecMetricsWindowMethod = LittleHorseGrpc.getGetWfSpecMetricsWindowMethod) == null) {
          LittleHorseGrpc.getGetWfSpecMetricsWindowMethod = getGetWfSpecMetricsWindowMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest, io.littlehorse.sdk.common.proto.WfSpecMetrics>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetWfSpecMetricsWindow"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.WfSpecMetrics.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetWfSpecMetricsWindow"))
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
    if ((getListTaskDefMetricsMethod = LittleHorseGrpc.getListTaskDefMetricsMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListTaskDefMetricsMethod = LittleHorseGrpc.getListTaskDefMetricsMethod) == null) {
          LittleHorseGrpc.getListTaskDefMetricsMethod = getListTaskDefMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListTaskMetricsRequest, io.littlehorse.sdk.common.proto.ListTaskMetricsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListTaskDefMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListTaskMetricsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListTaskMetricsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListTaskDefMetrics"))
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
    if ((getListWfSpecMetricsMethod = LittleHorseGrpc.getListWfSpecMetricsMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getListWfSpecMetricsMethod = LittleHorseGrpc.getListWfSpecMetricsMethod) == null) {
          LittleHorseGrpc.getListWfSpecMetricsMethod = getListWfSpecMetricsMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.ListWfMetricsRequest, io.littlehorse.sdk.common.proto.ListWfMetricsResponse>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "ListWfSpecMetrics"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListWfMetricsRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.ListWfMetricsResponse.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("ListWfSpecMetrics"))
              .build();
        }
      }
    }
    return getListWfSpecMetricsMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTenantRequest,
      io.littlehorse.sdk.common.proto.Tenant> getPutTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutTenant",
      requestType = io.littlehorse.sdk.common.proto.PutTenantRequest.class,
      responseType = io.littlehorse.sdk.common.proto.Tenant.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTenantRequest,
      io.littlehorse.sdk.common.proto.Tenant> getPutTenantMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutTenantRequest, io.littlehorse.sdk.common.proto.Tenant> getPutTenantMethod;
    if ((getPutTenantMethod = LittleHorseGrpc.getPutTenantMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutTenantMethod = LittleHorseGrpc.getPutTenantMethod) == null) {
          LittleHorseGrpc.getPutTenantMethod = getPutTenantMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutTenantRequest, io.littlehorse.sdk.common.proto.Tenant>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutTenantRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.Tenant.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutTenant"))
              .build();
        }
      }
    }
    return getPutTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TenantId,
      io.littlehorse.sdk.common.proto.Tenant> getGetTenantMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetTenant",
      requestType = io.littlehorse.sdk.common.proto.TenantId.class,
      responseType = io.littlehorse.sdk.common.proto.Tenant.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TenantId,
      io.littlehorse.sdk.common.proto.Tenant> getGetTenantMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.TenantId, io.littlehorse.sdk.common.proto.Tenant> getGetTenantMethod;
    if ((getGetTenantMethod = LittleHorseGrpc.getGetTenantMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetTenantMethod = LittleHorseGrpc.getGetTenantMethod) == null) {
          LittleHorseGrpc.getGetTenantMethod = getGetTenantMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.TenantId, io.littlehorse.sdk.common.proto.Tenant>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetTenant"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.TenantId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.Tenant.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetTenant"))
              .build();
        }
      }
    }
    return getGetTenantMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutPrincipalRequest,
      io.littlehorse.sdk.common.proto.Principal> getPutPrincipalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "PutPrincipal",
      requestType = io.littlehorse.sdk.common.proto.PutPrincipalRequest.class,
      responseType = io.littlehorse.sdk.common.proto.Principal.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutPrincipalRequest,
      io.littlehorse.sdk.common.proto.Principal> getPutPrincipalMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PutPrincipalRequest, io.littlehorse.sdk.common.proto.Principal> getPutPrincipalMethod;
    if ((getPutPrincipalMethod = LittleHorseGrpc.getPutPrincipalMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getPutPrincipalMethod = LittleHorseGrpc.getPutPrincipalMethod) == null) {
          LittleHorseGrpc.getPutPrincipalMethod = getPutPrincipalMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PutPrincipalRequest, io.littlehorse.sdk.common.proto.Principal>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "PutPrincipal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PutPrincipalRequest.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.Principal.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("PutPrincipal"))
              .build();
        }
      }
    }
    return getPutPrincipalMethod;
  }

  private static volatile io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PrincipalId,
      io.littlehorse.sdk.common.proto.Principal> getGetPrincipalMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetPrincipal",
      requestType = io.littlehorse.sdk.common.proto.PrincipalId.class,
      responseType = io.littlehorse.sdk.common.proto.Principal.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PrincipalId,
      io.littlehorse.sdk.common.proto.Principal> getGetPrincipalMethod() {
    io.grpc.MethodDescriptor<io.littlehorse.sdk.common.proto.PrincipalId, io.littlehorse.sdk.common.proto.Principal> getGetPrincipalMethod;
    if ((getGetPrincipalMethod = LittleHorseGrpc.getGetPrincipalMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetPrincipalMethod = LittleHorseGrpc.getGetPrincipalMethod) == null) {
          LittleHorseGrpc.getGetPrincipalMethod = getGetPrincipalMethod =
              io.grpc.MethodDescriptor.<io.littlehorse.sdk.common.proto.PrincipalId, io.littlehorse.sdk.common.proto.Principal>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetPrincipal"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.PrincipalId.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.Principal.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetPrincipal"))
              .build();
        }
      }
    }
    return getGetPrincipalMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      io.littlehorse.sdk.common.proto.Principal> getWhoamiMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "Whoami",
      requestType = com.google.protobuf.Empty.class,
      responseType = io.littlehorse.sdk.common.proto.Principal.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      io.littlehorse.sdk.common.proto.Principal> getWhoamiMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, io.littlehorse.sdk.common.proto.Principal> getWhoamiMethod;
    if ((getWhoamiMethod = LittleHorseGrpc.getWhoamiMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getWhoamiMethod = LittleHorseGrpc.getWhoamiMethod) == null) {
          LittleHorseGrpc.getWhoamiMethod = getWhoamiMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, io.littlehorse.sdk.common.proto.Principal>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "Whoami"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.Principal.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("Whoami"))
              .build();
        }
      }
    }
    return getWhoamiMethod;
  }

  private static volatile io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      io.littlehorse.sdk.common.proto.LittleHorseVersion> getGetServerVersionMethod;

  @io.grpc.stub.annotations.RpcMethod(
      fullMethodName = SERVICE_NAME + '/' + "GetServerVersion",
      requestType = com.google.protobuf.Empty.class,
      responseType = io.littlehorse.sdk.common.proto.LittleHorseVersion.class,
      methodType = io.grpc.MethodDescriptor.MethodType.UNARY)
  public static io.grpc.MethodDescriptor<com.google.protobuf.Empty,
      io.littlehorse.sdk.common.proto.LittleHorseVersion> getGetServerVersionMethod() {
    io.grpc.MethodDescriptor<com.google.protobuf.Empty, io.littlehorse.sdk.common.proto.LittleHorseVersion> getGetServerVersionMethod;
    if ((getGetServerVersionMethod = LittleHorseGrpc.getGetServerVersionMethod) == null) {
      synchronized (LittleHorseGrpc.class) {
        if ((getGetServerVersionMethod = LittleHorseGrpc.getGetServerVersionMethod) == null) {
          LittleHorseGrpc.getGetServerVersionMethod = getGetServerVersionMethod =
              io.grpc.MethodDescriptor.<com.google.protobuf.Empty, io.littlehorse.sdk.common.proto.LittleHorseVersion>newBuilder()
              .setType(io.grpc.MethodDescriptor.MethodType.UNARY)
              .setFullMethodName(generateFullMethodName(SERVICE_NAME, "GetServerVersion"))
              .setSampledToLocalTracing(true)
              .setRequestMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  com.google.protobuf.Empty.getDefaultInstance()))
              .setResponseMarshaller(io.grpc.protobuf.ProtoUtils.marshaller(
                  io.littlehorse.sdk.common.proto.LittleHorseVersion.getDefaultInstance()))
              .setSchemaDescriptor(new LittleHorseMethodDescriptorSupplier("GetServerVersion"))
              .build();
        }
      }
    }
    return getGetServerVersionMethod;
  }

  /**
   * Creates a new async stub that supports all call types for the service
   */
  public static LittleHorseStub newStub(io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LittleHorseStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LittleHorseStub>() {
        @java.lang.Override
        public LittleHorseStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LittleHorseStub(channel, callOptions);
        }
      };
    return LittleHorseStub.newStub(factory, channel);
  }

  /**
   * Creates a new blocking-style stub that supports unary and streaming output calls on the service
   */
  public static LittleHorseBlockingStub newBlockingStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LittleHorseBlockingStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LittleHorseBlockingStub>() {
        @java.lang.Override
        public LittleHorseBlockingStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LittleHorseBlockingStub(channel, callOptions);
        }
      };
    return LittleHorseBlockingStub.newStub(factory, channel);
  }

  /**
   * Creates a new ListenableFuture-style stub that supports unary calls on the service
   */
  public static LittleHorseFutureStub newFutureStub(
      io.grpc.Channel channel) {
    io.grpc.stub.AbstractStub.StubFactory<LittleHorseFutureStub> factory =
      new io.grpc.stub.AbstractStub.StubFactory<LittleHorseFutureStub>() {
        @java.lang.Override
        public LittleHorseFutureStub newStub(io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
          return new LittleHorseFutureStub(channel, callOptions);
        }
      };
    return LittleHorseFutureStub.newStub(factory, channel);
  }

  /**
   */
  public interface AsyncService {

    /**
     * <pre>
     * Creates a TaskDef.
     * </pre>
     */
    default void putTaskDef(io.littlehorse.sdk.common.proto.PutTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a TaskDef.
     * </pre>
     */
    default void getTaskDef(io.littlehorse.sdk.common.proto.TaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets the registered task worker group associated with a specific TaskDef.
     * </pre>
     */
    default void getTaskWorkerGroup(io.littlehorse.sdk.common.proto.TaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskWorkerGroup> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskWorkerGroupMethod(), responseObserver);
    }

    /**
     * <pre>
     * Creates an ExternalEventDef.
     * </pre>
     */
    default void putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets an ExternalEventDef.
     * </pre>
     */
    default void getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Creates a WorkflowEventDef.
     * </pre>
     */
    default void putWorkflowEventDef(io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutWorkflowEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Creates a WfSpec.
     * </pre>
     */
    default void putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutWfSpecMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a WfSpec.
     * </pre>
     */
    default void getWfSpec(io.littlehorse.sdk.common.proto.WfSpecId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfSpecMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns the latest WfSpec with a specified name (and optionally a specified Major Version).
     * </pre>
     */
    default void getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetLatestWfSpecMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Migrates all WfRun's from one version of a WfSpec onto a newer version of the
     * same WfSpec. This is useful for long-running WfRun's (eg. a 60-day marketing campaign) where
     * you must update WfRun's that are in the RUNNING state rather than allowing them to run to
     * completion.
     * As of 0.7.2, this feature is only partially implemented.
     * </pre>
     */
    default void migrateWfSpec(io.littlehorse.sdk.common.proto.MigrateWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getMigrateWfSpecMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates a new `StructDef``.
     * Note that this request is idempotent: if you
     * make a request to create a `StructDef` identical to the currently-created
     * one with the same `name`, no new `StructDef` will be created. This is the
     * same behavior as `rpc PutWfSpec` and `rpc PutUserTaskDef`.
     * For schema evolution / compatibility rules, see the `AllowedStructDefUpdateType`
     * enum within the `PutStructDefRequest`.
     * </pre>
     */
    default void putStructDef(io.littlehorse.sdk.common.proto.PutStructDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StructDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutStructDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Get a StructDef.
     * </pre>
     */
    default void getStructDef(io.littlehorse.sdk.common.proto.StructDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StructDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetStructDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Validate evolution of an existing `StructDef` into a new `StructDef`
     * </pre>
     */
    default void validateStructDefEvolution(io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getValidateStructDefEvolutionMethod(), responseObserver);
    }

    /**
     * <pre>
     * Creates a UserTaskDef.
     * </pre>
     */
    default void putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutUserTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a specific UserTaskDef.
     * This RPC is highly useful for applications built around
     * User Tasks. For example, a UI that dynamically displays form fields based on the User Task
     * might first receive a UserTaskRun, then use that UserTaskRun to look up the UserTaskDef.
     * The frontend would inspect the UserTaskDef and display a form field on the browser page
     * for each field in the UserTaskDef.
     * </pre>
     */
    default void getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns the most recent UserTaskDef with a specific name.
     * </pre>
     */
    default void getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetLatestUserTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Runs a WfSpec to create a WfRun.
     * </pre>
     */
    default void runWf(io.littlehorse.sdk.common.proto.RunWfRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRunWfMethod(), responseObserver);
    }

    /**
     * <pre>
     * Schedule repeated WfRun based on a cron expression
     * </pre>
     */
    default void scheduleWf(io.littlehorse.sdk.common.proto.ScheduleWfRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getScheduleWfMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for existing schedules
     * </pre>
     */
    default void searchScheduledWfRun(io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchScheduledWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Find a specific ScheduledWfRun
     * </pre>
     */
    default void getScheduledWfRun(io.littlehorse.sdk.common.proto.ScheduledWfRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetScheduledWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a WfRun. Although useful for development and debugging, this RPC is not often
     * used by applications.
     * </pre>
     */
    default void getWfRun(io.littlehorse.sdk.common.proto.WfRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Loads a specific UserTaskRun. It includes information about to whom the UserTask is
     * currently assigned, history of assignments and reassignments, and any context for that
     * UserTaskRun which is specific to the WfRun.
     * </pre>
     */
    default void getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetUserTaskRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Change the ownership of a UserTaskRun to a new userId, userGroup, or both. The
     * action will be reflected in your next call to SearchUserTaskRun. This RPC is useful for
     * applications that are using User Tasks to build an internal task-list and wish to
     * administer the tasks.
     * </pre>
     */
    default void assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAssignUserTaskRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Completes a UserTaskRun. Includes the results of the UserTaskRun, the UserTaskRun Id, and
     * the userId of the user who completes the UserTaskRun. Results in the UserTask NodeRun being
     * completed, and unblocks the associated ThreadRun in the WfRun.
     * This RPC is highly useful for applications built around a WfSpec that uses USER_TASK nodes.
     * </pre>
     */
    default void completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCompleteUserTaskRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Cancels a UserTaskRun. This will result in an EXCEPTION being propagated to the WfRun.
     * </pre>
     */
    default void cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getCancelUserTaskRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Saves the results of a UserTaskRun and logs who saved the content.&lt;br/&gt;
     * &lt;li&gt; Throws FAILED_PRECONDITION if the UserTaskRun is in the `DONE` or `CANCELLED` state.&lt;/li&gt;
     * &lt;li&gt; If `policy` is set to `FAIL_IF_CLAIMED_BY_OTHER`, returns `FAILED_PRECONDITION` if the
     * `user_id` field of the `UserTaskRun` does not match the `user_id` of the request.&lt;/li&gt;
     * </pre>
     */
    default void saveUserTaskRunProgress(io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSaveUserTaskRunProgressMethod(), responseObserver);
    }

    /**
     * <pre>
     * Lists all UserTaskRun's for a specific WfRun. Can be useful when using a WfRun
     * to model an entity.
     * </pre>
     */
    default void listUserTaskRuns(io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListUserTaskRunsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Adds userComment to a UserTaskRun
     * </pre>
     */
    default void putUserTaskRunComment(io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutUserTaskRunCommentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Edits userComment with the correlated userCommentId 
     * </pre>
     */
    default void editUserTaskRunComment(io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getEditUserTaskRunCommentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a comment logically, this does not affect the userTaskEvent Log
     * </pre>
     */
    default void deleteUserTaskRunComment(io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteUserTaskRunCommentMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a specific NodeRun.
     * </pre>
     */
    default void getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetNodeRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Lists all NodeRun's for a specific WfRun.
     * </pre>
     */
    default void listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListNodeRunsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets a specific TaskRun.
     * </pre>
     */
    default void getTaskRun(io.littlehorse.sdk.common.proto.TaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Lists all TaskRun's for a specific WfRun.
     * </pre>
     */
    default void listTaskRuns(io.littlehorse.sdk.common.proto.ListTaskRunsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTaskRunsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get the value of a specific Variable. When using a WfRun to model an entity, this
     * RPC is useful for retrieving information. It is equivalent to looking up the value of a
     * column for a specific row in a SQL table.
     * </pre>
     */
    default void getVariable(io.littlehorse.sdk.common.proto.VariableId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Variable> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetVariableMethod(), responseObserver);
    }

    /**
     * <pre>
     * List all Variables from a WfRun.
     * </pre>
     */
    default void listVariables(io.littlehorse.sdk.common.proto.ListVariablesRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListVariablesMethod(), responseObserver);
    }

    /**
     * <pre>
     * Post an ExternalEvent.
     * </pre>
     */
    default void putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutExternalEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Post a `CorrelatedEvent`, which is a precursor to `ExternalEvent`s.
     * </pre>
     */
    default void putCorrelatedEvent(io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutCorrelatedEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get a specific ExternalEvent.
     * </pre>
     */
    default void getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetExternalEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get a specific CorrelatedEvent
     * </pre>
     */
    default void getCorrelatedEvent(io.littlehorse.sdk.common.proto.CorrelatedEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetCorrelatedEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Waits for a WorkflowEvent to be thrown by a given WfRun. Returns immediately if a matching
     * WorkflowEvent has already been thrown; throws a DEADLINE_EXCEEDED error if the WorkflowEvent
     * is not thrown before the deadline specified by the client.
     * To specify the deadline, the client should use GRPC deadlines.
     * </pre>
     */
    default void awaitWorkflowEvent(io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getAwaitWorkflowEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get a specific WorkflowEventDef.
     * </pre>
     */
    default void getWorkflowEventDef(io.littlehorse.sdk.common.proto.WorkflowEventDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDef> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWorkflowEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Get a specific WorkflowEvent.
     * </pre>
     */
    default void getWorkflowEvent(io.littlehorse.sdk.common.proto.WorkflowEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEvent> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWorkflowEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * List ExternalEvent's for a specific WfRun.
     * </pre>
     */
    default void listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListExternalEventsMethod(), responseObserver);
    }

    /**
     * <pre>
     * List WorkflowEvent's for a specific WfRun.
     * </pre>
     */
    default void listWorkflowEvents(io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListWorkflowEventsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for WfRun's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    default void searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for CorrelatedEvents. This RPC is useful for day 2 operations and viewing
     * events that may be orphaned.
     * </pre>
     */
    default void searchCorrelatedEvent(io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEventIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchCorrelatedEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for NodeRun's. This RPC is useful for monitoring and finding bugs in
     * your workflows or Task Workers.
     * </pre>
     */
    default void searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchNodeRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for TaskRun's. This RPC is useful for finding bugs in your Task Workers.
     * </pre>
     */
    default void searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTaskRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for UserTaskRun's. This RPC is highly useful for applications that connect
     * human end-users to LittleHorse: it enables you to find all tasks assigned to a specific
     * person or group of people.
     * </pre>
     */
    default void searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchUserTaskRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for Variable's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    default void searchVariable(io.littlehorse.sdk.common.proto.SearchVariableRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchVariableMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for ExternalEvent's.
     * </pre>
     */
    default void searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchExternalEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for WorkflowEvents's.
     * </pre>
     */
    default void searchWorkflowEvent(io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWorkflowEventMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for TaskDef's.
     * </pre>
     */
    default void searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for UserTaskDef's.
     * </pre>
     */
    default void searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDefIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchUserTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for WfSpec's.
     * </pre>
     */
    default void searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWfSpecMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for ExternalEventDef's.
     * </pre>
     */
    default void searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDefIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchExternalEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for WorkflowEventDef's.
     * </pre>
     */
    default void searchWorkflowEventDef(io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDefIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchWorkflowEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Search for all available TenantIds for current Principal
     * </pre>
     */
    default void searchTenant(io.littlehorse.sdk.common.proto.SearchTenantRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TenantIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchTenantMethod(), responseObserver);
    }

    /**
     */
    default void searchPrincipal(io.littlehorse.sdk.common.proto.SearchPrincipalRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PrincipalIdList> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getSearchPrincipalMethod(), responseObserver);
    }

    /**
     * <pre>
     * Used by the Task Worker to:
     * 1. Tell the LH Server that the Task Worker has joined the Task Worker Group.
     * 2. Receive the assignemnt of LH Server's to poll from.
     * Generally, you won't use this request manually.
     * </pre>
     */
    default void registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRegisterTaskWorkerMethod(), responseObserver);
    }

    /**
     * <pre>
     * Used by Task Workers to listen for TaskRuns on the Task Queue. Generally, you won't
     * use this RPC manually.
     * </pre>
     */
    default io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskRequest> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskResponse> responseObserver) {
      return io.grpc.stub.ServerCalls.asyncUnimplementedStreamingCall(getPollTaskMethod(), responseObserver);
    }

    /**
     * <pre>
     * Used by Task Workers to report the result of a TaskRun. Generally, you won't use
     * this rpc manually.
     * </pre>
     */
    default void reportTask(io.littlehorse.sdk.common.proto.ReportTaskRun request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getReportTaskMethod(), responseObserver);
    }

    /**
     * <pre>
     * Move a WfRun or a specific ThreadRun in that WfRun to the HALTED state.
     * </pre>
     */
    default void stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getStopWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Resumes a WfRun or a specific ThreadRun of a WfRun.
     * </pre>
     */
    default void resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getResumeWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Rescues a failed ThreadRun (in the ERROR state only) by restarting it from
     * the point of failure. Useful if a bug in Task Worker implementation caused
     * a WfRun to fail and you did not have a FailureHandler for that NodeRun.
     * The specified `ThreadRun` must be in a state where it's latest `NodeRun` is: &lt;br/&gt;
     * - In the `ERROR` state.&lt;br/&gt;
     * - Has no `FailureHandler` `ThreadRun`s &lt;br/&gt;
     * - The parent `ThreadRun`, or any parent of the parent, has not handled the `Failure`
     * yet.
     * If that is not true, then the `ThreadRun` cannot be rescued and the request
     * will return `FAILED_PRECONDITION`.
     * </pre>
     */
    default void rescueThreadRun(io.littlehorse.sdk.common.proto.RescueThreadRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getRescueThreadRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a WfRun. The WfRun cannot be in the RUNNING state.
     * </pre>
     */
    default void deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a TaskDef.
     * </pre>
     */
    default void deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Deletes a StructDef.
     * </pre>
     */
    default void deleteStructDef(io.littlehorse.sdk.common.proto.DeleteStructDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteStructDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a WfSpec.
     * </pre>
     */
    default void deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWfSpecMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a UserTaskDef.
     * </pre>
     */
    default void deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteUserTaskDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes an ExternalEventDef.
     * </pre>
     */
    default void deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteExternalEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a CorrelatedEvent
     * </pre>
     */
    default void deleteCorrelatedEvent(io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteCorrelatedEventMethod(), responseObserver);
    }

    /**
     */
    default void deleteWorkflowEventDef(io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteWorkflowEventDefMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a `Principal`. Fails with `FAILED_PRECONDITION` if the specified `Principal`
     * is the last remaining `Principal` with admin permissions. Admin permissions are defined
     * as having the `global_acls` of `ALL_ACTIONS` over the `ACL_ALL_RESOURCES` scope.
     * </pre>
     */
    default void deletePrincipal(io.littlehorse.sdk.common.proto.DeletePrincipalRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeletePrincipalMethod(), responseObserver);
    }

    /**
     * <pre>
     * Deletes a scheduled run and prevents any further associated WfRun from being executed.
     * </pre>
     */
    default void deleteScheduledWfRun(io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getDeleteScheduledWfRunMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns TaskDef Metrics for a specific TaskDef and a specific time window.
     * </pre>
     */
    default void getTaskDefMetricsWindow(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetrics> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTaskDefMetricsWindowMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns WfSpec Metrics for a specific WfSpec and a specific time window.
     * </pre>
     */
    default void getWfSpecMetricsWindow(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetrics> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetWfSpecMetricsWindowMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns a list of TaskDef Metrics Windows.
     * </pre>
     */
    default void listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListTaskDefMetricsMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns a list of WfSpec Metrics Windows.
     * </pre>
     */
    default void listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsResponse> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getListWfSpecMetricsMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates another Tenant in the LH Server.
     * </pre>
     */
    default void putTenant(io.littlehorse.sdk.common.proto.PutTenantRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Tenant> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutTenantMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Gets a Tenant from the LH Server.
     * </pre>
     */
    default void getTenant(io.littlehorse.sdk.common.proto.TenantId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Tenant> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetTenantMethod(), responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates an Principal.
     * </pre>
     */
    default void putPrincipal(io.littlehorse.sdk.common.proto.PutPrincipalRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getPutPrincipalMethod(), responseObserver);
    }

    /**
     */
    default void getPrincipal(io.littlehorse.sdk.common.proto.PrincipalId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetPrincipalMethod(), responseObserver);
    }

    /**
     * <pre>
     * Returns the Principal of the caller.
     * </pre>
     */
    default void whoami(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getWhoamiMethod(), responseObserver);
    }

    /**
     * <pre>
     * Gets the version of the LH Server.
     * </pre>
     */
    default void getServerVersion(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.LittleHorseVersion> responseObserver) {
      io.grpc.stub.ServerCalls.asyncUnimplementedUnaryCall(getGetServerVersionMethod(), responseObserver);
    }
  }

  /**
   * Base class for the server implementation of the service LittleHorse.
   */
  public static abstract class LittleHorseImplBase
      implements io.grpc.BindableService, AsyncService {

    @java.lang.Override public final io.grpc.ServerServiceDefinition bindService() {
      return LittleHorseGrpc.bindService(this);
    }
  }

  /**
   * A stub to allow clients to do asynchronous rpc calls to service LittleHorse.
   */
  public static final class LittleHorseStub
      extends io.grpc.stub.AbstractAsyncStub<LittleHorseStub> {
    private LittleHorseStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LittleHorseStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LittleHorseStub(channel, callOptions);
    }

    /**
     * <pre>
     * Creates a TaskDef.
     * </pre>
     */
    public void putTaskDef(io.littlehorse.sdk.common.proto.PutTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a TaskDef.
     * </pre>
     */
    public void getTaskDef(io.littlehorse.sdk.common.proto.TaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets the registered task worker group associated with a specific TaskDef.
     * </pre>
     */
    public void getTaskWorkerGroup(io.littlehorse.sdk.common.proto.TaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskWorkerGroup> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskWorkerGroupMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates an ExternalEventDef.
     * </pre>
     */
    public void putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets an ExternalEventDef.
     * </pre>
     */
    public void getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a WorkflowEventDef.
     * </pre>
     */
    public void putWorkflowEventDef(io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutWorkflowEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a WfSpec.
     * </pre>
     */
    public void putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a WfSpec.
     * </pre>
     */
    public void getWfSpec(io.littlehorse.sdk.common.proto.WfSpecId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns the latest WfSpec with a specified name (and optionally a specified Major Version).
     * </pre>
     */
    public void getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetLatestWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Migrates all WfRun's from one version of a WfSpec onto a newer version of the
     * same WfSpec. This is useful for long-running WfRun's (eg. a 60-day marketing campaign) where
     * you must update WfRun's that are in the RUNNING state rather than allowing them to run to
     * completion.
     * As of 0.7.2, this feature is only partially implemented.
     * </pre>
     */
    public void migrateWfSpec(io.littlehorse.sdk.common.proto.MigrateWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getMigrateWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates a new `StructDef``.
     * Note that this request is idempotent: if you
     * make a request to create a `StructDef` identical to the currently-created
     * one with the same `name`, no new `StructDef` will be created. This is the
     * same behavior as `rpc PutWfSpec` and `rpc PutUserTaskDef`.
     * For schema evolution / compatibility rules, see the `AllowedStructDefUpdateType`
     * enum within the `PutStructDefRequest`.
     * </pre>
     */
    public void putStructDef(io.littlehorse.sdk.common.proto.PutStructDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StructDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutStructDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Get a StructDef.
     * </pre>
     */
    public void getStructDef(io.littlehorse.sdk.common.proto.StructDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StructDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetStructDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Validate evolution of an existing `StructDef` into a new `StructDef`
     * </pre>
     */
    public void validateStructDefEvolution(io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getValidateStructDefEvolutionMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Creates a UserTaskDef.
     * </pre>
     */
    public void putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a specific UserTaskDef.
     * This RPC is highly useful for applications built around
     * User Tasks. For example, a UI that dynamically displays form fields based on the User Task
     * might first receive a UserTaskRun, then use that UserTaskRun to look up the UserTaskDef.
     * The frontend would inspect the UserTaskDef and display a form field on the browser page
     * for each field in the UserTaskDef.
     * </pre>
     */
    public void getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns the most recent UserTaskDef with a specific name.
     * </pre>
     */
    public void getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetLatestUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Runs a WfSpec to create a WfRun.
     * </pre>
     */
    public void runWf(io.littlehorse.sdk.common.proto.RunWfRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Schedule repeated WfRun based on a cron expression
     * </pre>
     */
    public void scheduleWf(io.littlehorse.sdk.common.proto.ScheduleWfRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getScheduleWfMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for existing schedules
     * </pre>
     */
    public void searchScheduledWfRun(io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchScheduledWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Find a specific ScheduledWfRun
     * </pre>
     */
    public void getScheduledWfRun(io.littlehorse.sdk.common.proto.ScheduledWfRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetScheduledWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a WfRun. Although useful for development and debugging, this RPC is not often
     * used by applications.
     * </pre>
     */
    public void getWfRun(io.littlehorse.sdk.common.proto.WfRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Loads a specific UserTaskRun. It includes information about to whom the UserTask is
     * currently assigned, history of assignments and reassignments, and any context for that
     * UserTaskRun which is specific to the WfRun.
     * </pre>
     */
    public void getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Change the ownership of a UserTaskRun to a new userId, userGroup, or both. The
     * action will be reflected in your next call to SearchUserTaskRun. This RPC is useful for
     * applications that are using User Tasks to build an internal task-list and wish to
     * administer the tasks.
     * </pre>
     */
    public void assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAssignUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Completes a UserTaskRun. Includes the results of the UserTaskRun, the UserTaskRun Id, and
     * the userId of the user who completes the UserTaskRun. Results in the UserTask NodeRun being
     * completed, and unblocks the associated ThreadRun in the WfRun.
     * This RPC is highly useful for applications built around a WfSpec that uses USER_TASK nodes.
     * </pre>
     */
    public void completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCompleteUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Cancels a UserTaskRun. This will result in an EXCEPTION being propagated to the WfRun.
     * </pre>
     */
    public void cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getCancelUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Saves the results of a UserTaskRun and logs who saved the content.&lt;br/&gt;
     * &lt;li&gt; Throws FAILED_PRECONDITION if the UserTaskRun is in the `DONE` or `CANCELLED` state.&lt;/li&gt;
     * &lt;li&gt; If `policy` is set to `FAIL_IF_CLAIMED_BY_OTHER`, returns `FAILED_PRECONDITION` if the
     * `user_id` field of the `UserTaskRun` does not match the `user_id` of the request.&lt;/li&gt;
     * </pre>
     */
    public void saveUserTaskRunProgress(io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSaveUserTaskRunProgressMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Lists all UserTaskRun's for a specific WfRun. Can be useful when using a WfRun
     * to model an entity.
     * </pre>
     */
    public void listUserTaskRuns(io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListUserTaskRunsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Adds userComment to a UserTaskRun
     * </pre>
     */
    public void putUserTaskRunComment(io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutUserTaskRunCommentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Edits userComment with the correlated userCommentId 
     * </pre>
     */
    public void editUserTaskRunComment(io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getEditUserTaskRunCommentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a comment logically, this does not affect the userTaskEvent Log
     * </pre>
     */
    public void deleteUserTaskRunComment(io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteUserTaskRunCommentMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a specific NodeRun.
     * </pre>
     */
    public void getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Lists all NodeRun's for a specific WfRun.
     * </pre>
     */
    public void listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListNodeRunsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets a specific TaskRun.
     * </pre>
     */
    public void getTaskRun(io.littlehorse.sdk.common.proto.TaskRunId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Lists all TaskRun's for a specific WfRun.
     * </pre>
     */
    public void listTaskRuns(io.littlehorse.sdk.common.proto.ListTaskRunsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTaskRunsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get the value of a specific Variable. When using a WfRun to model an entity, this
     * RPC is useful for retrieving information. It is equivalent to looking up the value of a
     * column for a specific row in a SQL table.
     * </pre>
     */
    public void getVariable(io.littlehorse.sdk.common.proto.VariableId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Variable> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * List all Variables from a WfRun.
     * </pre>
     */
    public void listVariables(io.littlehorse.sdk.common.proto.ListVariablesRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListVariablesMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Post an ExternalEvent.
     * </pre>
     */
    public void putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Post a `CorrelatedEvent`, which is a precursor to `ExternalEvent`s.
     * </pre>
     */
    public void putCorrelatedEvent(io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutCorrelatedEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get a specific ExternalEvent.
     * </pre>
     */
    public void getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get a specific CorrelatedEvent
     * </pre>
     */
    public void getCorrelatedEvent(io.littlehorse.sdk.common.proto.CorrelatedEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetCorrelatedEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Waits for a WorkflowEvent to be thrown by a given WfRun. Returns immediately if a matching
     * WorkflowEvent has already been thrown; throws a DEADLINE_EXCEEDED error if the WorkflowEvent
     * is not thrown before the deadline specified by the client.
     * To specify the deadline, the client should use GRPC deadlines.
     * </pre>
     */
    public void awaitWorkflowEvent(io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getAwaitWorkflowEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get a specific WorkflowEventDef.
     * </pre>
     */
    public void getWorkflowEventDef(io.littlehorse.sdk.common.proto.WorkflowEventDefId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDef> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWorkflowEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Get a specific WorkflowEvent.
     * </pre>
     */
    public void getWorkflowEvent(io.littlehorse.sdk.common.proto.WorkflowEventId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEvent> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWorkflowEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * List ExternalEvent's for a specific WfRun.
     * </pre>
     */
    public void listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListExternalEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * List WorkflowEvent's for a specific WfRun.
     * </pre>
     */
    public void listWorkflowEvents(io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListWorkflowEventsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for WfRun's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    public void searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for CorrelatedEvents. This RPC is useful for day 2 operations and viewing
     * events that may be orphaned.
     * </pre>
     */
    public void searchCorrelatedEvent(io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEventIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchCorrelatedEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for NodeRun's. This RPC is useful for monitoring and finding bugs in
     * your workflows or Task Workers.
     * </pre>
     */
    public void searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.NodeRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchNodeRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for TaskRun's. This RPC is useful for finding bugs in your Task Workers.
     * </pre>
     */
    public void searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for UserTaskRun's. This RPC is highly useful for applications that connect
     * human end-users to LittleHorse: it enables you to find all tasks assigned to a specific
     * person or group of people.
     * </pre>
     */
    public void searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchUserTaskRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for Variable's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    public void searchVariable(io.littlehorse.sdk.common.proto.SearchVariableRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.VariableIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchVariableMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for ExternalEvent's.
     * </pre>
     */
    public void searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchExternalEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for WorkflowEvents's.
     * </pre>
     */
    public void searchWorkflowEvent(io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWorkflowEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for TaskDef's.
     * </pre>
     */
    public void searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for UserTaskDef's.
     * </pre>
     */
    public void searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskDefIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for WfSpec's.
     * </pre>
     */
    public void searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for ExternalEventDef's.
     * </pre>
     */
    public void searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDefIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for WorkflowEventDef's.
     * </pre>
     */
    public void searchWorkflowEventDef(io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDefIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchWorkflowEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Search for all available TenantIds for current Principal
     * </pre>
     */
    public void searchTenant(io.littlehorse.sdk.common.proto.SearchTenantRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TenantIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void searchPrincipal(io.littlehorse.sdk.common.proto.SearchPrincipalRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PrincipalIdList> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getSearchPrincipalMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Used by the Task Worker to:
     * 1. Tell the LH Server that the Task Worker has joined the Task Worker Group.
     * 2. Receive the assignemnt of LH Server's to poll from.
     * Generally, you won't use this request manually.
     * </pre>
     */
    public void registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRegisterTaskWorkerMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Used by Task Workers to listen for TaskRuns on the Task Queue. Generally, you won't
     * use this RPC manually.
     * </pre>
     */
    public io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskRequest> pollTask(
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PollTaskResponse> responseObserver) {
      return io.grpc.stub.ClientCalls.asyncBidiStreamingCall(
          getChannel().newCall(getPollTaskMethod(), getCallOptions()), responseObserver);
    }

    /**
     * <pre>
     * Used by Task Workers to report the result of a TaskRun. Generally, you won't use
     * this rpc manually.
     * </pre>
     */
    public void reportTask(io.littlehorse.sdk.common.proto.ReportTaskRun request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Move a WfRun or a specific ThreadRun in that WfRun to the HALTED state.
     * </pre>
     */
    public void stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Resumes a WfRun or a specific ThreadRun of a WfRun.
     * </pre>
     */
    public void resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Rescues a failed ThreadRun (in the ERROR state only) by restarting it from
     * the point of failure. Useful if a bug in Task Worker implementation caused
     * a WfRun to fail and you did not have a FailureHandler for that NodeRun.
     * The specified `ThreadRun` must be in a state where it's latest `NodeRun` is: &lt;br/&gt;
     * - In the `ERROR` state.&lt;br/&gt;
     * - Has no `FailureHandler` `ThreadRun`s &lt;br/&gt;
     * - The parent `ThreadRun`, or any parent of the parent, has not handled the `Failure`
     * yet.
     * If that is not true, then the `ThreadRun` cannot be rescued and the request
     * will return `FAILED_PRECONDITION`.
     * </pre>
     */
    public void rescueThreadRun(io.littlehorse.sdk.common.proto.RescueThreadRunRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getRescueThreadRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a WfRun. The WfRun cannot be in the RUNNING state.
     * </pre>
     */
    public void deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a TaskDef.
     * </pre>
     */
    public void deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Deletes a StructDef.
     * </pre>
     */
    public void deleteStructDef(io.littlehorse.sdk.common.proto.DeleteStructDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteStructDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a WfSpec.
     * </pre>
     */
    public void deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWfSpecMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a UserTaskDef.
     * </pre>
     */
    public void deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteUserTaskDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes an ExternalEventDef.
     * </pre>
     */
    public void deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteExternalEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a CorrelatedEvent
     * </pre>
     */
    public void deleteCorrelatedEvent(io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteCorrelatedEventMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void deleteWorkflowEventDef(io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteWorkflowEventDefMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a `Principal`. Fails with `FAILED_PRECONDITION` if the specified `Principal`
     * is the last remaining `Principal` with admin permissions. Admin permissions are defined
     * as having the `global_acls` of `ALL_ACTIONS` over the `ACL_ALL_RESOURCES` scope.
     * </pre>
     */
    public void deletePrincipal(io.littlehorse.sdk.common.proto.DeletePrincipalRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeletePrincipalMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Deletes a scheduled run and prevents any further associated WfRun from being executed.
     * </pre>
     */
    public void deleteScheduledWfRun(io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest request,
        io.grpc.stub.StreamObserver<com.google.protobuf.Empty> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getDeleteScheduledWfRunMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns TaskDef Metrics for a specific TaskDef and a specific time window.
     * </pre>
     */
    public void getTaskDefMetricsWindow(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskDefMetrics> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTaskDefMetricsWindowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns WfSpec Metrics for a specific WfSpec and a specific time window.
     * </pre>
     */
    public void getWfSpecMetricsWindow(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpecMetrics> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetWfSpecMetricsWindowMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns a list of TaskDef Metrics Windows.
     * </pre>
     */
    public void listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListTaskDefMetricsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns a list of WfSpec Metrics Windows.
     * </pre>
     */
    public void listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ListWfMetricsResponse> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getListWfSpecMetricsMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates another Tenant in the LH Server.
     * </pre>
     */
    public void putTenant(io.littlehorse.sdk.common.proto.PutTenantRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Tenant> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Gets a Tenant from the LH Server.
     * </pre>
     */
    public void getTenant(io.littlehorse.sdk.common.proto.TenantId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Tenant> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetTenantMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates an Principal.
     * </pre>
     */
    public void putPrincipal(io.littlehorse.sdk.common.proto.PutPrincipalRequest request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getPutPrincipalMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     */
    public void getPrincipal(io.littlehorse.sdk.common.proto.PrincipalId request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetPrincipalMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Returns the Principal of the caller.
     * </pre>
     */
    public void whoami(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getWhoamiMethod(), getCallOptions()), request, responseObserver);
    }

    /**
     * <pre>
     * Gets the version of the LH Server.
     * </pre>
     */
    public void getServerVersion(com.google.protobuf.Empty request,
        io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.LittleHorseVersion> responseObserver) {
      io.grpc.stub.ClientCalls.asyncUnaryCall(
          getChannel().newCall(getGetServerVersionMethod(), getCallOptions()), request, responseObserver);
    }
  }

  /**
   * A stub to allow clients to do synchronous rpc calls to service LittleHorse.
   */
  public static final class LittleHorseBlockingStub
      extends io.grpc.stub.AbstractBlockingStub<LittleHorseBlockingStub> {
    private LittleHorseBlockingStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LittleHorseBlockingStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LittleHorseBlockingStub(channel, callOptions);
    }

    /**
     * <pre>
     * Creates a TaskDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskDef putTaskDef(io.littlehorse.sdk.common.proto.PutTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a TaskDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskDef getTaskDef(io.littlehorse.sdk.common.proto.TaskDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets the registered task worker group associated with a specific TaskDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskWorkerGroup getTaskWorkerGroup(io.littlehorse.sdk.common.proto.TaskDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskWorkerGroupMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates an ExternalEventDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDef putExternalEventDef(io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets an ExternalEventDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDef getExternalEventDef(io.littlehorse.sdk.common.proto.ExternalEventDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a WorkflowEventDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDef putWorkflowEventDef(io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutWorkflowEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a WfSpec.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfSpec putWfSpec(io.littlehorse.sdk.common.proto.PutWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutWfSpecMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a WfSpec.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfSpec getWfSpec(io.littlehorse.sdk.common.proto.WfSpecId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfSpecMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns the latest WfSpec with a specified name (and optionally a specified Major Version).
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfSpec getLatestWfSpec(io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetLatestWfSpecMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Migrates all WfRun's from one version of a WfSpec onto a newer version of the
     * same WfSpec. This is useful for long-running WfRun's (eg. a 60-day marketing campaign) where
     * you must update WfRun's that are in the RUNNING state rather than allowing them to run to
     * completion.
     * As of 0.7.2, this feature is only partially implemented.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfSpec migrateWfSpec(io.littlehorse.sdk.common.proto.MigrateWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getMigrateWfSpecMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates a new `StructDef``.
     * Note that this request is idempotent: if you
     * make a request to create a `StructDef` identical to the currently-created
     * one with the same `name`, no new `StructDef` will be created. This is the
     * same behavior as `rpc PutWfSpec` and `rpc PutUserTaskDef`.
     * For schema evolution / compatibility rules, see the `AllowedStructDefUpdateType`
     * enum within the `PutStructDefRequest`.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.StructDef putStructDef(io.littlehorse.sdk.common.proto.PutStructDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutStructDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Get a StructDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.StructDef getStructDef(io.littlehorse.sdk.common.proto.StructDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetStructDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Validate evolution of an existing `StructDef` into a new `StructDef`
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse validateStructDefEvolution(io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getValidateStructDefEvolutionMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Creates a UserTaskDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskDef putUserTaskDef(io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a specific UserTaskDef.
     * This RPC is highly useful for applications built around
     * User Tasks. For example, a UI that dynamically displays form fields based on the User Task
     * might first receive a UserTaskRun, then use that UserTaskRun to look up the UserTaskDef.
     * The frontend would inspect the UserTaskDef and display a form field on the browser page
     * for each field in the UserTaskDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskDef getUserTaskDef(io.littlehorse.sdk.common.proto.UserTaskDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns the most recent UserTaskDef with a specific name.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskDef getLatestUserTaskDef(io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetLatestUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Runs a WfSpec to create a WfRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfRun runWf(io.littlehorse.sdk.common.proto.RunWfRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRunWfMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Schedule repeated WfRun based on a cron expression
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ScheduledWfRun scheduleWf(io.littlehorse.sdk.common.proto.ScheduleWfRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getScheduleWfMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for existing schedules
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ScheduledWfRunIdList searchScheduledWfRun(io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchScheduledWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Find a specific ScheduledWfRun
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ScheduledWfRun getScheduledWfRun(io.littlehorse.sdk.common.proto.ScheduledWfRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetScheduledWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a WfRun. Although useful for development and debugging, this RPC is not often
     * used by applications.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfRun getWfRun(io.littlehorse.sdk.common.proto.WfRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Loads a specific UserTaskRun. It includes information about to whom the UserTask is
     * currently assigned, history of assignments and reassignments, and any context for that
     * UserTaskRun which is specific to the WfRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRun getUserTaskRun(io.littlehorse.sdk.common.proto.UserTaskRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Change the ownership of a UserTaskRun to a new userId, userGroup, or both. The
     * action will be reflected in your next call to SearchUserTaskRun. This RPC is useful for
     * applications that are using User Tasks to build an internal task-list and wish to
     * administer the tasks.
     * </pre>
     */
    public com.google.protobuf.Empty assignUserTaskRun(io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAssignUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Completes a UserTaskRun. Includes the results of the UserTaskRun, the UserTaskRun Id, and
     * the userId of the user who completes the UserTaskRun. Results in the UserTask NodeRun being
     * completed, and unblocks the associated ThreadRun in the WfRun.
     * This RPC is highly useful for applications built around a WfSpec that uses USER_TASK nodes.
     * </pre>
     */
    public com.google.protobuf.Empty completeUserTaskRun(io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCompleteUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Cancels a UserTaskRun. This will result in an EXCEPTION being propagated to the WfRun.
     * </pre>
     */
    public com.google.protobuf.Empty cancelUserTaskRun(io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getCancelUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Saves the results of a UserTaskRun and logs who saved the content.&lt;br/&gt;
     * &lt;li&gt; Throws FAILED_PRECONDITION if the UserTaskRun is in the `DONE` or `CANCELLED` state.&lt;/li&gt;
     * &lt;li&gt; If `policy` is set to `FAIL_IF_CLAIMED_BY_OTHER`, returns `FAILED_PRECONDITION` if the
     * `user_id` field of the `UserTaskRun` does not match the `user_id` of the request.&lt;/li&gt;
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRun saveUserTaskRunProgress(io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSaveUserTaskRunProgressMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Lists all UserTaskRun's for a specific WfRun. Can be useful when using a WfRun
     * to model an entity.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunList listUserTaskRuns(io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListUserTaskRunsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Adds userComment to a UserTaskRun
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRun putUserTaskRunComment(io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutUserTaskRunCommentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Edits userComment with the correlated userCommentId 
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRun editUserTaskRunComment(io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getEditUserTaskRunCommentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a comment logically, this does not affect the userTaskEvent Log
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRun deleteUserTaskRunComment(io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteUserTaskRunCommentMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a specific NodeRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.NodeRun getNodeRun(io.littlehorse.sdk.common.proto.NodeRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetNodeRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Lists all NodeRun's for a specific WfRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.NodeRunList listNodeRuns(io.littlehorse.sdk.common.proto.ListNodeRunsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListNodeRunsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets a specific TaskRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskRun getTaskRun(io.littlehorse.sdk.common.proto.TaskRunId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Lists all TaskRun's for a specific WfRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskRunList listTaskRuns(io.littlehorse.sdk.common.proto.ListTaskRunsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTaskRunsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get the value of a specific Variable. When using a WfRun to model an entity, this
     * RPC is useful for retrieving information. It is equivalent to looking up the value of a
     * column for a specific row in a SQL table.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.Variable getVariable(io.littlehorse.sdk.common.proto.VariableId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetVariableMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * List all Variables from a WfRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.VariableList listVariables(io.littlehorse.sdk.common.proto.ListVariablesRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListVariablesMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Post an ExternalEvent.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ExternalEvent putExternalEvent(io.littlehorse.sdk.common.proto.PutExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutExternalEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Post a `CorrelatedEvent`, which is a precursor to `ExternalEvent`s.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.CorrelatedEvent putCorrelatedEvent(io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutCorrelatedEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get a specific ExternalEvent.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ExternalEvent getExternalEvent(io.littlehorse.sdk.common.proto.ExternalEventId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetExternalEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get a specific CorrelatedEvent
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.CorrelatedEvent getCorrelatedEvent(io.littlehorse.sdk.common.proto.CorrelatedEventId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetCorrelatedEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Waits for a WorkflowEvent to be thrown by a given WfRun. Returns immediately if a matching
     * WorkflowEvent has already been thrown; throws a DEADLINE_EXCEEDED error if the WorkflowEvent
     * is not thrown before the deadline specified by the client.
     * To specify the deadline, the client should use GRPC deadlines.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEvent awaitWorkflowEvent(io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getAwaitWorkflowEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get a specific WorkflowEventDef.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDef getWorkflowEventDef(io.littlehorse.sdk.common.proto.WorkflowEventDefId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWorkflowEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Get a specific WorkflowEvent.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEvent getWorkflowEvent(io.littlehorse.sdk.common.proto.WorkflowEventId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWorkflowEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * List ExternalEvent's for a specific WfRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventList listExternalEvents(io.littlehorse.sdk.common.proto.ListExternalEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListExternalEventsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * List WorkflowEvent's for a specific WfRun.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventList listWorkflowEvents(io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListWorkflowEventsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for WfRun's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfRunIdList searchWfRun(io.littlehorse.sdk.common.proto.SearchWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for CorrelatedEvents. This RPC is useful for day 2 operations and viewing
     * events that may be orphaned.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.CorrelatedEventIdList searchCorrelatedEvent(io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchCorrelatedEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for NodeRun's. This RPC is useful for monitoring and finding bugs in
     * your workflows or Task Workers.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.NodeRunIdList searchNodeRun(io.littlehorse.sdk.common.proto.SearchNodeRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchNodeRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for TaskRun's. This RPC is useful for finding bugs in your Task Workers.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskRunIdList searchTaskRun(io.littlehorse.sdk.common.proto.SearchTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTaskRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for UserTaskRun's. This RPC is highly useful for applications that connect
     * human end-users to LittleHorse: it enables you to find all tasks assigned to a specific
     * person or group of people.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskRunIdList searchUserTaskRun(io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchUserTaskRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for Variable's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.VariableIdList searchVariable(io.littlehorse.sdk.common.proto.SearchVariableRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchVariableMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for ExternalEvent's.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventIdList searchExternalEvent(io.littlehorse.sdk.common.proto.SearchExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchExternalEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for WorkflowEvents's.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventIdList searchWorkflowEvent(io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWorkflowEventMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for TaskDef's.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskDefIdList searchTaskDef(io.littlehorse.sdk.common.proto.SearchTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for UserTaskDef's.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.UserTaskDefIdList searchUserTaskDef(io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for WfSpec's.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfSpecIdList searchWfSpec(io.littlehorse.sdk.common.proto.SearchWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWfSpecMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for ExternalEventDef's.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ExternalEventDefIdList searchExternalEventDef(io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for WorkflowEventDef's.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WorkflowEventDefIdList searchWorkflowEventDef(io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchWorkflowEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Search for all available TenantIds for current Principal
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TenantIdList searchTenant(io.littlehorse.sdk.common.proto.SearchTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchTenantMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.PrincipalIdList searchPrincipal(io.littlehorse.sdk.common.proto.SearchPrincipalRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getSearchPrincipalMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Used by the Task Worker to:
     * 1. Tell the LH Server that the Task Worker has joined the Task Worker Group.
     * 2. Receive the assignemnt of LH Server's to poll from.
     * Generally, you won't use this request manually.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse registerTaskWorker(io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRegisterTaskWorkerMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Used by Task Workers to report the result of a TaskRun. Generally, you won't use
     * this rpc manually.
     * </pre>
     */
    public com.google.protobuf.Empty reportTask(io.littlehorse.sdk.common.proto.ReportTaskRun request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getReportTaskMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Move a WfRun or a specific ThreadRun in that WfRun to the HALTED state.
     * </pre>
     */
    public com.google.protobuf.Empty stopWfRun(io.littlehorse.sdk.common.proto.StopWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getStopWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Resumes a WfRun or a specific ThreadRun of a WfRun.
     * </pre>
     */
    public com.google.protobuf.Empty resumeWfRun(io.littlehorse.sdk.common.proto.ResumeWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getResumeWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Rescues a failed ThreadRun (in the ERROR state only) by restarting it from
     * the point of failure. Useful if a bug in Task Worker implementation caused
     * a WfRun to fail and you did not have a FailureHandler for that NodeRun.
     * The specified `ThreadRun` must be in a state where it's latest `NodeRun` is: &lt;br/&gt;
     * - In the `ERROR` state.&lt;br/&gt;
     * - Has no `FailureHandler` `ThreadRun`s &lt;br/&gt;
     * - The parent `ThreadRun`, or any parent of the parent, has not handled the `Failure`
     * yet.
     * If that is not true, then the `ThreadRun` cannot be rescued and the request
     * will return `FAILED_PRECONDITION`.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfRun rescueThreadRun(io.littlehorse.sdk.common.proto.RescueThreadRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getRescueThreadRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a WfRun. The WfRun cannot be in the RUNNING state.
     * </pre>
     */
    public com.google.protobuf.Empty deleteWfRun(io.littlehorse.sdk.common.proto.DeleteWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a TaskDef.
     * </pre>
     */
    public com.google.protobuf.Empty deleteTaskDef(io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Deletes a StructDef.
     * </pre>
     */
    public com.google.protobuf.Empty deleteStructDef(io.littlehorse.sdk.common.proto.DeleteStructDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteStructDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a WfSpec.
     * </pre>
     */
    public com.google.protobuf.Empty deleteWfSpec(io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWfSpecMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a UserTaskDef.
     * </pre>
     */
    public com.google.protobuf.Empty deleteUserTaskDef(io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteUserTaskDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes an ExternalEventDef.
     * </pre>
     */
    public com.google.protobuf.Empty deleteExternalEventDef(io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteExternalEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a CorrelatedEvent
     * </pre>
     */
    public com.google.protobuf.Empty deleteCorrelatedEvent(io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteCorrelatedEventMethod(), getCallOptions(), request);
    }

    /**
     */
    public com.google.protobuf.Empty deleteWorkflowEventDef(io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteWorkflowEventDefMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a `Principal`. Fails with `FAILED_PRECONDITION` if the specified `Principal`
     * is the last remaining `Principal` with admin permissions. Admin permissions are defined
     * as having the `global_acls` of `ALL_ACTIONS` over the `ACL_ALL_RESOURCES` scope.
     * </pre>
     */
    public com.google.protobuf.Empty deletePrincipal(io.littlehorse.sdk.common.proto.DeletePrincipalRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeletePrincipalMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Deletes a scheduled run and prevents any further associated WfRun from being executed.
     * </pre>
     */
    public com.google.protobuf.Empty deleteScheduledWfRun(io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getDeleteScheduledWfRunMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns TaskDef Metrics for a specific TaskDef and a specific time window.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.TaskDefMetrics getTaskDefMetricsWindow(io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTaskDefMetricsWindowMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns WfSpec Metrics for a specific WfSpec and a specific time window.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.WfSpecMetrics getWfSpecMetricsWindow(io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetWfSpecMetricsWindowMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns a list of TaskDef Metrics Windows.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ListTaskMetricsResponse listTaskDefMetrics(io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListTaskDefMetricsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns a list of WfSpec Metrics Windows.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.ListWfMetricsResponse listWfSpecMetrics(io.littlehorse.sdk.common.proto.ListWfMetricsRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getListWfSpecMetricsMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates another Tenant in the LH Server.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.Tenant putTenant(io.littlehorse.sdk.common.proto.PutTenantRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutTenantMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Gets a Tenant from the LH Server.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.Tenant getTenant(io.littlehorse.sdk.common.proto.TenantId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetTenantMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates an Principal.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.Principal putPrincipal(io.littlehorse.sdk.common.proto.PutPrincipalRequest request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getPutPrincipalMethod(), getCallOptions(), request);
    }

    /**
     */
    public io.littlehorse.sdk.common.proto.Principal getPrincipal(io.littlehorse.sdk.common.proto.PrincipalId request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetPrincipalMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Returns the Principal of the caller.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.Principal whoami(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getWhoamiMethod(), getCallOptions(), request);
    }

    /**
     * <pre>
     * Gets the version of the LH Server.
     * </pre>
     */
    public io.littlehorse.sdk.common.proto.LittleHorseVersion getServerVersion(com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.blockingUnaryCall(
          getChannel(), getGetServerVersionMethod(), getCallOptions(), request);
    }
  }

  /**
   * A stub to allow clients to do ListenableFuture-style rpc calls to service LittleHorse.
   */
  public static final class LittleHorseFutureStub
      extends io.grpc.stub.AbstractFutureStub<LittleHorseFutureStub> {
    private LittleHorseFutureStub(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      super(channel, callOptions);
    }

    @java.lang.Override
    protected LittleHorseFutureStub build(
        io.grpc.Channel channel, io.grpc.CallOptions callOptions) {
      return new LittleHorseFutureStub(channel, callOptions);
    }

    /**
     * <pre>
     * Creates a TaskDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDef> putTaskDef(
        io.littlehorse.sdk.common.proto.PutTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a TaskDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDef> getTaskDef(
        io.littlehorse.sdk.common.proto.TaskDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets the registered task worker group associated with a specific TaskDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskWorkerGroup> getTaskWorkerGroup(
        io.littlehorse.sdk.common.proto.TaskDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskWorkerGroupMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates an ExternalEventDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventDef> putExternalEventDef(
        io.littlehorse.sdk.common.proto.PutExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets an ExternalEventDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventDef> getExternalEventDef(
        io.littlehorse.sdk.common.proto.ExternalEventDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a WorkflowEventDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WorkflowEventDef> putWorkflowEventDef(
        io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutWorkflowEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a WfSpec.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpec> putWfSpec(
        io.littlehorse.sdk.common.proto.PutWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutWfSpecMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a WfSpec.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpec> getWfSpec(
        io.littlehorse.sdk.common.proto.WfSpecId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfSpecMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns the latest WfSpec with a specified name (and optionally a specified Major Version).
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpec> getLatestWfSpec(
        io.littlehorse.sdk.common.proto.GetLatestWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetLatestWfSpecMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Migrates all WfRun's from one version of a WfSpec onto a newer version of the
     * same WfSpec. This is useful for long-running WfRun's (eg. a 60-day marketing campaign) where
     * you must update WfRun's that are in the RUNNING state rather than allowing them to run to
     * completion.
     * As of 0.7.2, this feature is only partially implemented.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpec> migrateWfSpec(
        io.littlehorse.sdk.common.proto.MigrateWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getMigrateWfSpecMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates a new `StructDef``.
     * Note that this request is idempotent: if you
     * make a request to create a `StructDef` identical to the currently-created
     * one with the same `name`, no new `StructDef` will be created. This is the
     * same behavior as `rpc PutWfSpec` and `rpc PutUserTaskDef`.
     * For schema evolution / compatibility rules, see the `AllowedStructDefUpdateType`
     * enum within the `PutStructDefRequest`.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.StructDef> putStructDef(
        io.littlehorse.sdk.common.proto.PutStructDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutStructDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Get a StructDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.StructDef> getStructDef(
        io.littlehorse.sdk.common.proto.StructDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetStructDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Validate evolution of an existing `StructDef` into a new `StructDef`
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse> validateStructDefEvolution(
        io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getValidateStructDefEvolutionMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Creates a UserTaskDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDef> putUserTaskDef(
        io.littlehorse.sdk.common.proto.PutUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a specific UserTaskDef.
     * This RPC is highly useful for applications built around
     * User Tasks. For example, a UI that dynamically displays form fields based on the User Task
     * might first receive a UserTaskRun, then use that UserTaskRun to look up the UserTaskDef.
     * The frontend would inspect the UserTaskDef and display a form field on the browser page
     * for each field in the UserTaskDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDef> getUserTaskDef(
        io.littlehorse.sdk.common.proto.UserTaskDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns the most recent UserTaskDef with a specific name.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDef> getLatestUserTaskDef(
        io.littlehorse.sdk.common.proto.GetLatestUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetLatestUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Runs a WfSpec to create a WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfRun> runWf(
        io.littlehorse.sdk.common.proto.RunWfRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRunWfMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Schedule repeated WfRun based on a cron expression
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ScheduledWfRun> scheduleWf(
        io.littlehorse.sdk.common.proto.ScheduleWfRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getScheduleWfMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for existing schedules
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ScheduledWfRunIdList> searchScheduledWfRun(
        io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchScheduledWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Find a specific ScheduledWfRun
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ScheduledWfRun> getScheduledWfRun(
        io.littlehorse.sdk.common.proto.ScheduledWfRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetScheduledWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a WfRun. Although useful for development and debugging, this RPC is not often
     * used by applications.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfRun> getWfRun(
        io.littlehorse.sdk.common.proto.WfRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Loads a specific UserTaskRun. It includes information about to whom the UserTask is
     * currently assigned, history of assignments and reassignments, and any context for that
     * UserTaskRun which is specific to the WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRun> getUserTaskRun(
        io.littlehorse.sdk.common.proto.UserTaskRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Change the ownership of a UserTaskRun to a new userId, userGroup, or both. The
     * action will be reflected in your next call to SearchUserTaskRun. This RPC is useful for
     * applications that are using User Tasks to build an internal task-list and wish to
     * administer the tasks.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> assignUserTaskRun(
        io.littlehorse.sdk.common.proto.AssignUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAssignUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Completes a UserTaskRun. Includes the results of the UserTaskRun, the UserTaskRun Id, and
     * the userId of the user who completes the UserTaskRun. Results in the UserTask NodeRun being
     * completed, and unblocks the associated ThreadRun in the WfRun.
     * This RPC is highly useful for applications built around a WfSpec that uses USER_TASK nodes.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> completeUserTaskRun(
        io.littlehorse.sdk.common.proto.CompleteUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCompleteUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Cancels a UserTaskRun. This will result in an EXCEPTION being propagated to the WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> cancelUserTaskRun(
        io.littlehorse.sdk.common.proto.CancelUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getCancelUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Saves the results of a UserTaskRun and logs who saved the content.&lt;br/&gt;
     * &lt;li&gt; Throws FAILED_PRECONDITION if the UserTaskRun is in the `DONE` or `CANCELLED` state.&lt;/li&gt;
     * &lt;li&gt; If `policy` is set to `FAIL_IF_CLAIMED_BY_OTHER`, returns `FAILED_PRECONDITION` if the
     * `user_id` field of the `UserTaskRun` does not match the `user_id` of the request.&lt;/li&gt;
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRun> saveUserTaskRunProgress(
        io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSaveUserTaskRunProgressMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Lists all UserTaskRun's for a specific WfRun. Can be useful when using a WfRun
     * to model an entity.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRunList> listUserTaskRuns(
        io.littlehorse.sdk.common.proto.ListUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListUserTaskRunsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Adds userComment to a UserTaskRun
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRun> putUserTaskRunComment(
        io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutUserTaskRunCommentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Edits userComment with the correlated userCommentId 
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRun> editUserTaskRunComment(
        io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getEditUserTaskRunCommentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a comment logically, this does not affect the userTaskEvent Log
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRun> deleteUserTaskRunComment(
        io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteUserTaskRunCommentMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a specific NodeRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.NodeRun> getNodeRun(
        io.littlehorse.sdk.common.proto.NodeRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetNodeRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Lists all NodeRun's for a specific WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.NodeRunList> listNodeRuns(
        io.littlehorse.sdk.common.proto.ListNodeRunsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListNodeRunsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets a specific TaskRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskRun> getTaskRun(
        io.littlehorse.sdk.common.proto.TaskRunId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Lists all TaskRun's for a specific WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskRunList> listTaskRuns(
        io.littlehorse.sdk.common.proto.ListTaskRunsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTaskRunsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get the value of a specific Variable. When using a WfRun to model an entity, this
     * RPC is useful for retrieving information. It is equivalent to looking up the value of a
     * column for a specific row in a SQL table.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.Variable> getVariable(
        io.littlehorse.sdk.common.proto.VariableId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetVariableMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * List all Variables from a WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.VariableList> listVariables(
        io.littlehorse.sdk.common.proto.ListVariablesRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListVariablesMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Post an ExternalEvent.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEvent> putExternalEvent(
        io.littlehorse.sdk.common.proto.PutExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutExternalEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Post a `CorrelatedEvent`, which is a precursor to `ExternalEvent`s.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.CorrelatedEvent> putCorrelatedEvent(
        io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutCorrelatedEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get a specific ExternalEvent.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEvent> getExternalEvent(
        io.littlehorse.sdk.common.proto.ExternalEventId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetExternalEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get a specific CorrelatedEvent
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.CorrelatedEvent> getCorrelatedEvent(
        io.littlehorse.sdk.common.proto.CorrelatedEventId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetCorrelatedEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Waits for a WorkflowEvent to be thrown by a given WfRun. Returns immediately if a matching
     * WorkflowEvent has already been thrown; throws a DEADLINE_EXCEEDED error if the WorkflowEvent
     * is not thrown before the deadline specified by the client.
     * To specify the deadline, the client should use GRPC deadlines.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WorkflowEvent> awaitWorkflowEvent(
        io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getAwaitWorkflowEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get a specific WorkflowEventDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WorkflowEventDef> getWorkflowEventDef(
        io.littlehorse.sdk.common.proto.WorkflowEventDefId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWorkflowEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Get a specific WorkflowEvent.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WorkflowEvent> getWorkflowEvent(
        io.littlehorse.sdk.common.proto.WorkflowEventId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWorkflowEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * List ExternalEvent's for a specific WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventList> listExternalEvents(
        io.littlehorse.sdk.common.proto.ListExternalEventsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListExternalEventsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * List WorkflowEvent's for a specific WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WorkflowEventList> listWorkflowEvents(
        io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListWorkflowEventsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for WfRun's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfRunIdList> searchWfRun(
        io.littlehorse.sdk.common.proto.SearchWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for CorrelatedEvents. This RPC is useful for day 2 operations and viewing
     * events that may be orphaned.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.CorrelatedEventIdList> searchCorrelatedEvent(
        io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchCorrelatedEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for NodeRun's. This RPC is useful for monitoring and finding bugs in
     * your workflows or Task Workers.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.NodeRunIdList> searchNodeRun(
        io.littlehorse.sdk.common.proto.SearchNodeRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchNodeRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for TaskRun's. This RPC is useful for finding bugs in your Task Workers.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskRunIdList> searchTaskRun(
        io.littlehorse.sdk.common.proto.SearchTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTaskRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for UserTaskRun's. This RPC is highly useful for applications that connect
     * human end-users to LittleHorse: it enables you to find all tasks assigned to a specific
     * person or group of people.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskRunIdList> searchUserTaskRun(
        io.littlehorse.sdk.common.proto.SearchUserTaskRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchUserTaskRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for Variable's. This RPC is highly useful for applications that store data
     * in LittleHorse and need to find a specific WfRun based on certain indexed fields.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.VariableIdList> searchVariable(
        io.littlehorse.sdk.common.proto.SearchVariableRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchVariableMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for ExternalEvent's.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventIdList> searchExternalEvent(
        io.littlehorse.sdk.common.proto.SearchExternalEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchExternalEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for WorkflowEvents's.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WorkflowEventIdList> searchWorkflowEvent(
        io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWorkflowEventMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for TaskDef's.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDefIdList> searchTaskDef(
        io.littlehorse.sdk.common.proto.SearchTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for UserTaskDef's.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.UserTaskDefIdList> searchUserTaskDef(
        io.littlehorse.sdk.common.proto.SearchUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for WfSpec's.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpecIdList> searchWfSpec(
        io.littlehorse.sdk.common.proto.SearchWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWfSpecMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for ExternalEventDef's.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ExternalEventDefIdList> searchExternalEventDef(
        io.littlehorse.sdk.common.proto.SearchExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for WorkflowEventDef's.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WorkflowEventDefIdList> searchWorkflowEventDef(
        io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchWorkflowEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Search for all available TenantIds for current Principal
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TenantIdList> searchTenant(
        io.littlehorse.sdk.common.proto.SearchTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchTenantMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.PrincipalIdList> searchPrincipal(
        io.littlehorse.sdk.common.proto.SearchPrincipalRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getSearchPrincipalMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Used by the Task Worker to:
     * 1. Tell the LH Server that the Task Worker has joined the Task Worker Group.
     * 2. Receive the assignemnt of LH Server's to poll from.
     * Generally, you won't use this request manually.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.RegisterTaskWorkerResponse> registerTaskWorker(
        io.littlehorse.sdk.common.proto.RegisterTaskWorkerRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRegisterTaskWorkerMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Used by Task Workers to report the result of a TaskRun. Generally, you won't use
     * this rpc manually.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> reportTask(
        io.littlehorse.sdk.common.proto.ReportTaskRun request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getReportTaskMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Move a WfRun or a specific ThreadRun in that WfRun to the HALTED state.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> stopWfRun(
        io.littlehorse.sdk.common.proto.StopWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getStopWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Resumes a WfRun or a specific ThreadRun of a WfRun.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> resumeWfRun(
        io.littlehorse.sdk.common.proto.ResumeWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getResumeWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Rescues a failed ThreadRun (in the ERROR state only) by restarting it from
     * the point of failure. Useful if a bug in Task Worker implementation caused
     * a WfRun to fail and you did not have a FailureHandler for that NodeRun.
     * The specified `ThreadRun` must be in a state where it's latest `NodeRun` is: &lt;br/&gt;
     * - In the `ERROR` state.&lt;br/&gt;
     * - Has no `FailureHandler` `ThreadRun`s &lt;br/&gt;
     * - The parent `ThreadRun`, or any parent of the parent, has not handled the `Failure`
     * yet.
     * If that is not true, then the `ThreadRun` cannot be rescued and the request
     * will return `FAILED_PRECONDITION`.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfRun> rescueThreadRun(
        io.littlehorse.sdk.common.proto.RescueThreadRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getRescueThreadRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a WfRun. The WfRun cannot be in the RUNNING state.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteWfRun(
        io.littlehorse.sdk.common.proto.DeleteWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a TaskDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteTaskDef(
        io.littlehorse.sdk.common.proto.DeleteTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Deletes a StructDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteStructDef(
        io.littlehorse.sdk.common.proto.DeleteStructDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteStructDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a WfSpec.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteWfSpec(
        io.littlehorse.sdk.common.proto.DeleteWfSpecRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWfSpecMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a UserTaskDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteUserTaskDef(
        io.littlehorse.sdk.common.proto.DeleteUserTaskDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteUserTaskDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes an ExternalEventDef.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteExternalEventDef(
        io.littlehorse.sdk.common.proto.DeleteExternalEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteExternalEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a CorrelatedEvent
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteCorrelatedEvent(
        io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteCorrelatedEventMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteWorkflowEventDef(
        io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteWorkflowEventDefMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a `Principal`. Fails with `FAILED_PRECONDITION` if the specified `Principal`
     * is the last remaining `Principal` with admin permissions. Admin permissions are defined
     * as having the `global_acls` of `ALL_ACTIONS` over the `ACL_ALL_RESOURCES` scope.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deletePrincipal(
        io.littlehorse.sdk.common.proto.DeletePrincipalRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeletePrincipalMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Deletes a scheduled run and prevents any further associated WfRun from being executed.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<com.google.protobuf.Empty> deleteScheduledWfRun(
        io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getDeleteScheduledWfRunMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns TaskDef Metrics for a specific TaskDef and a specific time window.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.TaskDefMetrics> getTaskDefMetricsWindow(
        io.littlehorse.sdk.common.proto.TaskDefMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTaskDefMetricsWindowMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns WfSpec Metrics for a specific WfSpec and a specific time window.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.WfSpecMetrics> getWfSpecMetricsWindow(
        io.littlehorse.sdk.common.proto.WfSpecMetricsQueryRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetWfSpecMetricsWindowMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns a list of TaskDef Metrics Windows.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListTaskMetricsResponse> listTaskDefMetrics(
        io.littlehorse.sdk.common.proto.ListTaskMetricsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListTaskDefMetricsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns a list of WfSpec Metrics Windows.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.ListWfMetricsResponse> listWfSpecMetrics(
        io.littlehorse.sdk.common.proto.ListWfMetricsRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getListWfSpecMetricsMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates another Tenant in the LH Server.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.Tenant> putTenant(
        io.littlehorse.sdk.common.proto.PutTenantRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutTenantMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Gets a Tenant from the LH Server.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.Tenant> getTenant(
        io.littlehorse.sdk.common.proto.TenantId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetTenantMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * EXPERIMENTAL: Creates an Principal.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.Principal> putPrincipal(
        io.littlehorse.sdk.common.proto.PutPrincipalRequest request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getPutPrincipalMethod(), getCallOptions()), request);
    }

    /**
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.Principal> getPrincipal(
        io.littlehorse.sdk.common.proto.PrincipalId request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetPrincipalMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Returns the Principal of the caller.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.Principal> whoami(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getWhoamiMethod(), getCallOptions()), request);
    }

    /**
     * <pre>
     * Gets the version of the LH Server.
     * </pre>
     */
    public com.google.common.util.concurrent.ListenableFuture<io.littlehorse.sdk.common.proto.LittleHorseVersion> getServerVersion(
        com.google.protobuf.Empty request) {
      return io.grpc.stub.ClientCalls.futureUnaryCall(
          getChannel().newCall(getGetServerVersionMethod(), getCallOptions()), request);
    }
  }

  private static final int METHODID_PUT_TASK_DEF = 0;
  private static final int METHODID_GET_TASK_DEF = 1;
  private static final int METHODID_GET_TASK_WORKER_GROUP = 2;
  private static final int METHODID_PUT_EXTERNAL_EVENT_DEF = 3;
  private static final int METHODID_GET_EXTERNAL_EVENT_DEF = 4;
  private static final int METHODID_PUT_WORKFLOW_EVENT_DEF = 5;
  private static final int METHODID_PUT_WF_SPEC = 6;
  private static final int METHODID_GET_WF_SPEC = 7;
  private static final int METHODID_GET_LATEST_WF_SPEC = 8;
  private static final int METHODID_MIGRATE_WF_SPEC = 9;
  private static final int METHODID_PUT_STRUCT_DEF = 10;
  private static final int METHODID_GET_STRUCT_DEF = 11;
  private static final int METHODID_VALIDATE_STRUCT_DEF_EVOLUTION = 12;
  private static final int METHODID_PUT_USER_TASK_DEF = 13;
  private static final int METHODID_GET_USER_TASK_DEF = 14;
  private static final int METHODID_GET_LATEST_USER_TASK_DEF = 15;
  private static final int METHODID_RUN_WF = 16;
  private static final int METHODID_SCHEDULE_WF = 17;
  private static final int METHODID_SEARCH_SCHEDULED_WF_RUN = 18;
  private static final int METHODID_GET_SCHEDULED_WF_RUN = 19;
  private static final int METHODID_GET_WF_RUN = 20;
  private static final int METHODID_GET_USER_TASK_RUN = 21;
  private static final int METHODID_ASSIGN_USER_TASK_RUN = 22;
  private static final int METHODID_COMPLETE_USER_TASK_RUN = 23;
  private static final int METHODID_CANCEL_USER_TASK_RUN = 24;
  private static final int METHODID_SAVE_USER_TASK_RUN_PROGRESS = 25;
  private static final int METHODID_LIST_USER_TASK_RUNS = 26;
  private static final int METHODID_PUT_USER_TASK_RUN_COMMENT = 27;
  private static final int METHODID_EDIT_USER_TASK_RUN_COMMENT = 28;
  private static final int METHODID_DELETE_USER_TASK_RUN_COMMENT = 29;
  private static final int METHODID_GET_NODE_RUN = 30;
  private static final int METHODID_LIST_NODE_RUNS = 31;
  private static final int METHODID_GET_TASK_RUN = 32;
  private static final int METHODID_LIST_TASK_RUNS = 33;
  private static final int METHODID_GET_VARIABLE = 34;
  private static final int METHODID_LIST_VARIABLES = 35;
  private static final int METHODID_PUT_EXTERNAL_EVENT = 36;
  private static final int METHODID_PUT_CORRELATED_EVENT = 37;
  private static final int METHODID_GET_EXTERNAL_EVENT = 38;
  private static final int METHODID_GET_CORRELATED_EVENT = 39;
  private static final int METHODID_AWAIT_WORKFLOW_EVENT = 40;
  private static final int METHODID_GET_WORKFLOW_EVENT_DEF = 41;
  private static final int METHODID_GET_WORKFLOW_EVENT = 42;
  private static final int METHODID_LIST_EXTERNAL_EVENTS = 43;
  private static final int METHODID_LIST_WORKFLOW_EVENTS = 44;
  private static final int METHODID_SEARCH_WF_RUN = 45;
  private static final int METHODID_SEARCH_CORRELATED_EVENT = 46;
  private static final int METHODID_SEARCH_NODE_RUN = 47;
  private static final int METHODID_SEARCH_TASK_RUN = 48;
  private static final int METHODID_SEARCH_USER_TASK_RUN = 49;
  private static final int METHODID_SEARCH_VARIABLE = 50;
  private static final int METHODID_SEARCH_EXTERNAL_EVENT = 51;
  private static final int METHODID_SEARCH_WORKFLOW_EVENT = 52;
  private static final int METHODID_SEARCH_TASK_DEF = 53;
  private static final int METHODID_SEARCH_USER_TASK_DEF = 54;
  private static final int METHODID_SEARCH_WF_SPEC = 55;
  private static final int METHODID_SEARCH_EXTERNAL_EVENT_DEF = 56;
  private static final int METHODID_SEARCH_WORKFLOW_EVENT_DEF = 57;
  private static final int METHODID_SEARCH_TENANT = 58;
  private static final int METHODID_SEARCH_PRINCIPAL = 59;
  private static final int METHODID_REGISTER_TASK_WORKER = 60;
  private static final int METHODID_REPORT_TASK = 61;
  private static final int METHODID_STOP_WF_RUN = 62;
  private static final int METHODID_RESUME_WF_RUN = 63;
  private static final int METHODID_RESCUE_THREAD_RUN = 64;
  private static final int METHODID_DELETE_WF_RUN = 65;
  private static final int METHODID_DELETE_TASK_DEF = 66;
  private static final int METHODID_DELETE_STRUCT_DEF = 67;
  private static final int METHODID_DELETE_WF_SPEC = 68;
  private static final int METHODID_DELETE_USER_TASK_DEF = 69;
  private static final int METHODID_DELETE_EXTERNAL_EVENT_DEF = 70;
  private static final int METHODID_DELETE_CORRELATED_EVENT = 71;
  private static final int METHODID_DELETE_WORKFLOW_EVENT_DEF = 72;
  private static final int METHODID_DELETE_PRINCIPAL = 73;
  private static final int METHODID_DELETE_SCHEDULED_WF_RUN = 74;
  private static final int METHODID_GET_TASK_DEF_METRICS_WINDOW = 75;
  private static final int METHODID_GET_WF_SPEC_METRICS_WINDOW = 76;
  private static final int METHODID_LIST_TASK_DEF_METRICS = 77;
  private static final int METHODID_LIST_WF_SPEC_METRICS = 78;
  private static final int METHODID_PUT_TENANT = 79;
  private static final int METHODID_GET_TENANT = 80;
  private static final int METHODID_PUT_PRINCIPAL = 81;
  private static final int METHODID_GET_PRINCIPAL = 82;
  private static final int METHODID_WHOAMI = 83;
  private static final int METHODID_GET_SERVER_VERSION = 84;
  private static final int METHODID_POLL_TASK = 85;

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
        case METHODID_GET_TASK_WORKER_GROUP:
          serviceImpl.getTaskWorkerGroup((io.littlehorse.sdk.common.proto.TaskDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskWorkerGroup>) responseObserver);
          break;
        case METHODID_PUT_EXTERNAL_EVENT_DEF:
          serviceImpl.putExternalEventDef((io.littlehorse.sdk.common.proto.PutExternalEventDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT_DEF:
          serviceImpl.getExternalEventDef((io.littlehorse.sdk.common.proto.ExternalEventDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventDef>) responseObserver);
          break;
        case METHODID_PUT_WORKFLOW_EVENT_DEF:
          serviceImpl.putWorkflowEventDef((io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDef>) responseObserver);
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
        case METHODID_MIGRATE_WF_SPEC:
          serviceImpl.migrateWfSpec((io.littlehorse.sdk.common.proto.MigrateWfSpecRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfSpec>) responseObserver);
          break;
        case METHODID_PUT_STRUCT_DEF:
          serviceImpl.putStructDef((io.littlehorse.sdk.common.proto.PutStructDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StructDef>) responseObserver);
          break;
        case METHODID_GET_STRUCT_DEF:
          serviceImpl.getStructDef((io.littlehorse.sdk.common.proto.StructDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.StructDef>) responseObserver);
          break;
        case METHODID_VALIDATE_STRUCT_DEF_EVOLUTION:
          serviceImpl.validateStructDefEvolution((io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse>) responseObserver);
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
        case METHODID_SCHEDULE_WF:
          serviceImpl.scheduleWf((io.littlehorse.sdk.common.proto.ScheduleWfRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRun>) responseObserver);
          break;
        case METHODID_SEARCH_SCHEDULED_WF_RUN:
          serviceImpl.searchScheduledWfRun((io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRunIdList>) responseObserver);
          break;
        case METHODID_GET_SCHEDULED_WF_RUN:
          serviceImpl.getScheduledWfRun((io.littlehorse.sdk.common.proto.ScheduledWfRunId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ScheduledWfRun>) responseObserver);
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
        case METHODID_SAVE_USER_TASK_RUN_PROGRESS:
          serviceImpl.saveUserTaskRunProgress((io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun>) responseObserver);
          break;
        case METHODID_LIST_USER_TASK_RUNS:
          serviceImpl.listUserTaskRuns((io.littlehorse.sdk.common.proto.ListUserTaskRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRunList>) responseObserver);
          break;
        case METHODID_PUT_USER_TASK_RUN_COMMENT:
          serviceImpl.putUserTaskRunComment((io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun>) responseObserver);
          break;
        case METHODID_EDIT_USER_TASK_RUN_COMMENT:
          serviceImpl.editUserTaskRunComment((io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun>) responseObserver);
          break;
        case METHODID_DELETE_USER_TASK_RUN_COMMENT:
          serviceImpl.deleteUserTaskRunComment((io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.UserTaskRun>) responseObserver);
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
        case METHODID_LIST_TASK_RUNS:
          serviceImpl.listTaskRuns((io.littlehorse.sdk.common.proto.ListTaskRunsRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TaskRunList>) responseObserver);
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
        case METHODID_PUT_CORRELATED_EVENT:
          serviceImpl.putCorrelatedEvent((io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEvent>) responseObserver);
          break;
        case METHODID_GET_EXTERNAL_EVENT:
          serviceImpl.getExternalEvent((io.littlehorse.sdk.common.proto.ExternalEventId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEvent>) responseObserver);
          break;
        case METHODID_GET_CORRELATED_EVENT:
          serviceImpl.getCorrelatedEvent((io.littlehorse.sdk.common.proto.CorrelatedEventId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEvent>) responseObserver);
          break;
        case METHODID_AWAIT_WORKFLOW_EVENT:
          serviceImpl.awaitWorkflowEvent((io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEvent>) responseObserver);
          break;
        case METHODID_GET_WORKFLOW_EVENT_DEF:
          serviceImpl.getWorkflowEventDef((io.littlehorse.sdk.common.proto.WorkflowEventDefId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDef>) responseObserver);
          break;
        case METHODID_GET_WORKFLOW_EVENT:
          serviceImpl.getWorkflowEvent((io.littlehorse.sdk.common.proto.WorkflowEventId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEvent>) responseObserver);
          break;
        case METHODID_LIST_EXTERNAL_EVENTS:
          serviceImpl.listExternalEvents((io.littlehorse.sdk.common.proto.ListExternalEventsRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.ExternalEventList>) responseObserver);
          break;
        case METHODID_LIST_WORKFLOW_EVENTS:
          serviceImpl.listWorkflowEvents((io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventList>) responseObserver);
          break;
        case METHODID_SEARCH_WF_RUN:
          serviceImpl.searchWfRun((io.littlehorse.sdk.common.proto.SearchWfRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRunIdList>) responseObserver);
          break;
        case METHODID_SEARCH_CORRELATED_EVENT:
          serviceImpl.searchCorrelatedEvent((io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.CorrelatedEventIdList>) responseObserver);
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
        case METHODID_SEARCH_WORKFLOW_EVENT:
          serviceImpl.searchWorkflowEvent((io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventIdList>) responseObserver);
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
        case METHODID_SEARCH_WORKFLOW_EVENT_DEF:
          serviceImpl.searchWorkflowEventDef((io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WorkflowEventDefIdList>) responseObserver);
          break;
        case METHODID_SEARCH_TENANT:
          serviceImpl.searchTenant((io.littlehorse.sdk.common.proto.SearchTenantRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.TenantIdList>) responseObserver);
          break;
        case METHODID_SEARCH_PRINCIPAL:
          serviceImpl.searchPrincipal((io.littlehorse.sdk.common.proto.SearchPrincipalRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.PrincipalIdList>) responseObserver);
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
        case METHODID_RESCUE_THREAD_RUN:
          serviceImpl.rescueThreadRun((io.littlehorse.sdk.common.proto.RescueThreadRunRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.WfRun>) responseObserver);
          break;
        case METHODID_DELETE_WF_RUN:
          serviceImpl.deleteWfRun((io.littlehorse.sdk.common.proto.DeleteWfRunRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_TASK_DEF:
          serviceImpl.deleteTaskDef((io.littlehorse.sdk.common.proto.DeleteTaskDefRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_STRUCT_DEF:
          serviceImpl.deleteStructDef((io.littlehorse.sdk.common.proto.DeleteStructDefRequest) request,
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
        case METHODID_DELETE_CORRELATED_EVENT:
          serviceImpl.deleteCorrelatedEvent((io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_WORKFLOW_EVENT_DEF:
          serviceImpl.deleteWorkflowEventDef((io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_PRINCIPAL:
          serviceImpl.deletePrincipal((io.littlehorse.sdk.common.proto.DeletePrincipalRequest) request,
              (io.grpc.stub.StreamObserver<com.google.protobuf.Empty>) responseObserver);
          break;
        case METHODID_DELETE_SCHEDULED_WF_RUN:
          serviceImpl.deleteScheduledWfRun((io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest) request,
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
        case METHODID_PUT_TENANT:
          serviceImpl.putTenant((io.littlehorse.sdk.common.proto.PutTenantRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Tenant>) responseObserver);
          break;
        case METHODID_GET_TENANT:
          serviceImpl.getTenant((io.littlehorse.sdk.common.proto.TenantId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Tenant>) responseObserver);
          break;
        case METHODID_PUT_PRINCIPAL:
          serviceImpl.putPrincipal((io.littlehorse.sdk.common.proto.PutPrincipalRequest) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal>) responseObserver);
          break;
        case METHODID_GET_PRINCIPAL:
          serviceImpl.getPrincipal((io.littlehorse.sdk.common.proto.PrincipalId) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal>) responseObserver);
          break;
        case METHODID_WHOAMI:
          serviceImpl.whoami((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.Principal>) responseObserver);
          break;
        case METHODID_GET_SERVER_VERSION:
          serviceImpl.getServerVersion((com.google.protobuf.Empty) request,
              (io.grpc.stub.StreamObserver<io.littlehorse.sdk.common.proto.LittleHorseVersion>) responseObserver);
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
          getGetTaskWorkerGroupMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TaskDefId,
              io.littlehorse.sdk.common.proto.TaskWorkerGroup>(
                service, METHODID_GET_TASK_WORKER_GROUP)))
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
          getPutWorkflowEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutWorkflowEventDefRequest,
              io.littlehorse.sdk.common.proto.WorkflowEventDef>(
                service, METHODID_PUT_WORKFLOW_EVENT_DEF)))
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
          getMigrateWfSpecMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.MigrateWfSpecRequest,
              io.littlehorse.sdk.common.proto.WfSpec>(
                service, METHODID_MIGRATE_WF_SPEC)))
        .addMethod(
          getPutStructDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutStructDefRequest,
              io.littlehorse.sdk.common.proto.StructDef>(
                service, METHODID_PUT_STRUCT_DEF)))
        .addMethod(
          getGetStructDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.StructDefId,
              io.littlehorse.sdk.common.proto.StructDef>(
                service, METHODID_GET_STRUCT_DEF)))
        .addMethod(
          getValidateStructDefEvolutionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionRequest,
              io.littlehorse.sdk.common.proto.ValidateStructDefEvolutionResponse>(
                service, METHODID_VALIDATE_STRUCT_DEF_EVOLUTION)))
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
          getScheduleWfMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ScheduleWfRequest,
              io.littlehorse.sdk.common.proto.ScheduledWfRun>(
                service, METHODID_SCHEDULE_WF)))
        .addMethod(
          getSearchScheduledWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchScheduledWfRunRequest,
              io.littlehorse.sdk.common.proto.ScheduledWfRunIdList>(
                service, METHODID_SEARCH_SCHEDULED_WF_RUN)))
        .addMethod(
          getGetScheduledWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ScheduledWfRunId,
              io.littlehorse.sdk.common.proto.ScheduledWfRun>(
                service, METHODID_GET_SCHEDULED_WF_RUN)))
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
          getSaveUserTaskRunProgressMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SaveUserTaskRunProgressRequest,
              io.littlehorse.sdk.common.proto.UserTaskRun>(
                service, METHODID_SAVE_USER_TASK_RUN_PROGRESS)))
        .addMethod(
          getListUserTaskRunsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListUserTaskRunRequest,
              io.littlehorse.sdk.common.proto.UserTaskRunList>(
                service, METHODID_LIST_USER_TASK_RUNS)))
        .addMethod(
          getPutUserTaskRunCommentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutUserTaskRunCommentRequest,
              io.littlehorse.sdk.common.proto.UserTaskRun>(
                service, METHODID_PUT_USER_TASK_RUN_COMMENT)))
        .addMethod(
          getEditUserTaskRunCommentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.EditUserTaskRunCommentRequest,
              io.littlehorse.sdk.common.proto.UserTaskRun>(
                service, METHODID_EDIT_USER_TASK_RUN_COMMENT)))
        .addMethod(
          getDeleteUserTaskRunCommentMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteUserTaskRunCommentRequest,
              io.littlehorse.sdk.common.proto.UserTaskRun>(
                service, METHODID_DELETE_USER_TASK_RUN_COMMENT)))
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
          getListTaskRunsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListTaskRunsRequest,
              io.littlehorse.sdk.common.proto.TaskRunList>(
                service, METHODID_LIST_TASK_RUNS)))
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
          getPutCorrelatedEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutCorrelatedEventRequest,
              io.littlehorse.sdk.common.proto.CorrelatedEvent>(
                service, METHODID_PUT_CORRELATED_EVENT)))
        .addMethod(
          getGetExternalEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ExternalEventId,
              io.littlehorse.sdk.common.proto.ExternalEvent>(
                service, METHODID_GET_EXTERNAL_EVENT)))
        .addMethod(
          getGetCorrelatedEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.CorrelatedEventId,
              io.littlehorse.sdk.common.proto.CorrelatedEvent>(
                service, METHODID_GET_CORRELATED_EVENT)))
        .addMethod(
          getAwaitWorkflowEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.AwaitWorkflowEventRequest,
              io.littlehorse.sdk.common.proto.WorkflowEvent>(
                service, METHODID_AWAIT_WORKFLOW_EVENT)))
        .addMethod(
          getGetWorkflowEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WorkflowEventDefId,
              io.littlehorse.sdk.common.proto.WorkflowEventDef>(
                service, METHODID_GET_WORKFLOW_EVENT_DEF)))
        .addMethod(
          getGetWorkflowEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.WorkflowEventId,
              io.littlehorse.sdk.common.proto.WorkflowEvent>(
                service, METHODID_GET_WORKFLOW_EVENT)))
        .addMethod(
          getListExternalEventsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListExternalEventsRequest,
              io.littlehorse.sdk.common.proto.ExternalEventList>(
                service, METHODID_LIST_EXTERNAL_EVENTS)))
        .addMethod(
          getListWorkflowEventsMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.ListWorkflowEventsRequest,
              io.littlehorse.sdk.common.proto.WorkflowEventList>(
                service, METHODID_LIST_WORKFLOW_EVENTS)))
        .addMethod(
          getSearchWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchWfRunRequest,
              io.littlehorse.sdk.common.proto.WfRunIdList>(
                service, METHODID_SEARCH_WF_RUN)))
        .addMethod(
          getSearchCorrelatedEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchCorrelatedEventRequest,
              io.littlehorse.sdk.common.proto.CorrelatedEventIdList>(
                service, METHODID_SEARCH_CORRELATED_EVENT)))
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
          getSearchWorkflowEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchWorkflowEventRequest,
              io.littlehorse.sdk.common.proto.WorkflowEventIdList>(
                service, METHODID_SEARCH_WORKFLOW_EVENT)))
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
          getSearchWorkflowEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchWorkflowEventDefRequest,
              io.littlehorse.sdk.common.proto.WorkflowEventDefIdList>(
                service, METHODID_SEARCH_WORKFLOW_EVENT_DEF)))
        .addMethod(
          getSearchTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchTenantRequest,
              io.littlehorse.sdk.common.proto.TenantIdList>(
                service, METHODID_SEARCH_TENANT)))
        .addMethod(
          getSearchPrincipalMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.SearchPrincipalRequest,
              io.littlehorse.sdk.common.proto.PrincipalIdList>(
                service, METHODID_SEARCH_PRINCIPAL)))
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
          getRescueThreadRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.RescueThreadRunRequest,
              io.littlehorse.sdk.common.proto.WfRun>(
                service, METHODID_RESCUE_THREAD_RUN)))
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
          getDeleteStructDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteStructDefRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_STRUCT_DEF)))
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
          getDeleteCorrelatedEventMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteCorrelatedEventRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_CORRELATED_EVENT)))
        .addMethod(
          getDeleteWorkflowEventDefMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteWorkflowEventDefRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_WORKFLOW_EVENT_DEF)))
        .addMethod(
          getDeletePrincipalMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeletePrincipalRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_PRINCIPAL)))
        .addMethod(
          getDeleteScheduledWfRunMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.DeleteScheduledWfRunRequest,
              com.google.protobuf.Empty>(
                service, METHODID_DELETE_SCHEDULED_WF_RUN)))
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
        .addMethod(
          getPutTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutTenantRequest,
              io.littlehorse.sdk.common.proto.Tenant>(
                service, METHODID_PUT_TENANT)))
        .addMethod(
          getGetTenantMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.TenantId,
              io.littlehorse.sdk.common.proto.Tenant>(
                service, METHODID_GET_TENANT)))
        .addMethod(
          getPutPrincipalMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PutPrincipalRequest,
              io.littlehorse.sdk.common.proto.Principal>(
                service, METHODID_PUT_PRINCIPAL)))
        .addMethod(
          getGetPrincipalMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              io.littlehorse.sdk.common.proto.PrincipalId,
              io.littlehorse.sdk.common.proto.Principal>(
                service, METHODID_GET_PRINCIPAL)))
        .addMethod(
          getWhoamiMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              io.littlehorse.sdk.common.proto.Principal>(
                service, METHODID_WHOAMI)))
        .addMethod(
          getGetServerVersionMethod(),
          io.grpc.stub.ServerCalls.asyncUnaryCall(
            new MethodHandlers<
              com.google.protobuf.Empty,
              io.littlehorse.sdk.common.proto.LittleHorseVersion>(
                service, METHODID_GET_SERVER_VERSION)))
        .build();
  }

  private static abstract class LittleHorseBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoFileDescriptorSupplier, io.grpc.protobuf.ProtoServiceDescriptorSupplier {
    LittleHorseBaseDescriptorSupplier() {}

    @java.lang.Override
    public com.google.protobuf.Descriptors.FileDescriptor getFileDescriptor() {
      return io.littlehorse.sdk.common.proto.Service.getDescriptor();
    }

    @java.lang.Override
    public com.google.protobuf.Descriptors.ServiceDescriptor getServiceDescriptor() {
      return getFileDescriptor().findServiceByName("LittleHorse");
    }
  }

  private static final class LittleHorseFileDescriptorSupplier
      extends LittleHorseBaseDescriptorSupplier {
    LittleHorseFileDescriptorSupplier() {}
  }

  private static final class LittleHorseMethodDescriptorSupplier
      extends LittleHorseBaseDescriptorSupplier
      implements io.grpc.protobuf.ProtoMethodDescriptorSupplier {
    private final java.lang.String methodName;

    LittleHorseMethodDescriptorSupplier(java.lang.String methodName) {
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
      synchronized (LittleHorseGrpc.class) {
        result = serviceDescriptor;
        if (result == null) {
          serviceDescriptor = result = io.grpc.ServiceDescriptor.newBuilder(SERVICE_NAME)
              .setSchemaDescriptor(new LittleHorseFileDescriptorSupplier())
              .addMethod(getPutTaskDefMethod())
              .addMethod(getGetTaskDefMethod())
              .addMethod(getGetTaskWorkerGroupMethod())
              .addMethod(getPutExternalEventDefMethod())
              .addMethod(getGetExternalEventDefMethod())
              .addMethod(getPutWorkflowEventDefMethod())
              .addMethod(getPutWfSpecMethod())
              .addMethod(getGetWfSpecMethod())
              .addMethod(getGetLatestWfSpecMethod())
              .addMethod(getMigrateWfSpecMethod())
              .addMethod(getPutStructDefMethod())
              .addMethod(getGetStructDefMethod())
              .addMethod(getValidateStructDefEvolutionMethod())
              .addMethod(getPutUserTaskDefMethod())
              .addMethod(getGetUserTaskDefMethod())
              .addMethod(getGetLatestUserTaskDefMethod())
              .addMethod(getRunWfMethod())
              .addMethod(getScheduleWfMethod())
              .addMethod(getSearchScheduledWfRunMethod())
              .addMethod(getGetScheduledWfRunMethod())
              .addMethod(getGetWfRunMethod())
              .addMethod(getGetUserTaskRunMethod())
              .addMethod(getAssignUserTaskRunMethod())
              .addMethod(getCompleteUserTaskRunMethod())
              .addMethod(getCancelUserTaskRunMethod())
              .addMethod(getSaveUserTaskRunProgressMethod())
              .addMethod(getListUserTaskRunsMethod())
              .addMethod(getPutUserTaskRunCommentMethod())
              .addMethod(getEditUserTaskRunCommentMethod())
              .addMethod(getDeleteUserTaskRunCommentMethod())
              .addMethod(getGetNodeRunMethod())
              .addMethod(getListNodeRunsMethod())
              .addMethod(getGetTaskRunMethod())
              .addMethod(getListTaskRunsMethod())
              .addMethod(getGetVariableMethod())
              .addMethod(getListVariablesMethod())
              .addMethod(getPutExternalEventMethod())
              .addMethod(getPutCorrelatedEventMethod())
              .addMethod(getGetExternalEventMethod())
              .addMethod(getGetCorrelatedEventMethod())
              .addMethod(getAwaitWorkflowEventMethod())
              .addMethod(getGetWorkflowEventDefMethod())
              .addMethod(getGetWorkflowEventMethod())
              .addMethod(getListExternalEventsMethod())
              .addMethod(getListWorkflowEventsMethod())
              .addMethod(getSearchWfRunMethod())
              .addMethod(getSearchCorrelatedEventMethod())
              .addMethod(getSearchNodeRunMethod())
              .addMethod(getSearchTaskRunMethod())
              .addMethod(getSearchUserTaskRunMethod())
              .addMethod(getSearchVariableMethod())
              .addMethod(getSearchExternalEventMethod())
              .addMethod(getSearchWorkflowEventMethod())
              .addMethod(getSearchTaskDefMethod())
              .addMethod(getSearchUserTaskDefMethod())
              .addMethod(getSearchWfSpecMethod())
              .addMethod(getSearchExternalEventDefMethod())
              .addMethod(getSearchWorkflowEventDefMethod())
              .addMethod(getSearchTenantMethod())
              .addMethod(getSearchPrincipalMethod())
              .addMethod(getRegisterTaskWorkerMethod())
              .addMethod(getPollTaskMethod())
              .addMethod(getReportTaskMethod())
              .addMethod(getStopWfRunMethod())
              .addMethod(getResumeWfRunMethod())
              .addMethod(getRescueThreadRunMethod())
              .addMethod(getDeleteWfRunMethod())
              .addMethod(getDeleteTaskDefMethod())
              .addMethod(getDeleteStructDefMethod())
              .addMethod(getDeleteWfSpecMethod())
              .addMethod(getDeleteUserTaskDefMethod())
              .addMethod(getDeleteExternalEventDefMethod())
              .addMethod(getDeleteCorrelatedEventMethod())
              .addMethod(getDeleteWorkflowEventDefMethod())
              .addMethod(getDeletePrincipalMethod())
              .addMethod(getDeleteScheduledWfRunMethod())
              .addMethod(getGetTaskDefMetricsWindowMethod())
              .addMethod(getGetWfSpecMetricsWindowMethod())
              .addMethod(getListTaskDefMetricsMethod())
              .addMethod(getListWfSpecMetricsMethod())
              .addMethod(getPutTenantMethod())
              .addMethod(getGetTenantMethod())
              .addMethod(getPutPrincipalMethod())
              .addMethod(getGetPrincipalMethod())
              .addMethod(getWhoamiMethod())
              .addMethod(getGetServerVersionMethod())
              .build();
        }
      }
    }
    return result;
  }
}
