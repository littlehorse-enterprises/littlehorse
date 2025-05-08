package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;

import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.model.getable.global.structdef.InlineStructDefModel;
import io.littlehorse.common.model.getable.global.structdef.StructDefModel;
import io.littlehorse.common.model.getable.objectId.StructDefIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.StructDefUtil;
import io.littlehorse.sdk.common.exception.LHSerdeException;
import io.littlehorse.sdk.common.proto.PutStructDefRequest;
import io.littlehorse.sdk.common.proto.PutStructDefRequest.AllowedStructDefUpdateType;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataCommandExecution;

public class PutStructDefRequestModel extends MetadataSubCommand<PutStructDefRequest> {

  private String name;
  private InlineStructDefModel structDef;
  private AllowedStructDefUpdateType allowedUpdates;

  @Override
  public PutStructDefRequest.Builder toProto() {
    PutStructDefRequest.Builder out = PutStructDefRequest.newBuilder()
      .setName(name)
      .setStructDef(structDef.toProto())
      .setAllowedUpdates(allowedUpdates);

    return out;
  }

  @Override
  public void initFrom(Message p, ExecutionContext context) throws LHSerdeException {
    PutStructDefRequest proto = (PutStructDefRequest) p;

    name = proto.getName();
    structDef = LHSerializable.fromProto(proto.getStructDef(), InlineStructDefModel.class, context);
    allowedUpdates = proto.getAllowedUpdates();
  }

  @Override
  public boolean hasResponse() {
    return true;
  }

  @Override
  public Message process(MetadataCommandExecution context) {
    MetadataManager metadataManager = context.metadataManager();
    if (!LHUtil.isValidLHName(name)) {
      throw new LHApiException(Status.INVALID_ARGUMENT, "StructDef name must be a valid hostname");
    }

    StructDefModel spec = new StructDefModel();

    spec.setId(new StructDefIdModel(name));
    spec.setStructDef(structDef);

    StructDefModel oldVersion = metadataManager.get(new StructDefIdModel(name));

    // TODO: AllowedUpdateType checking
    if (oldVersion != null) {
        if (StructDefUtil.equals(spec, oldVersion)) {
            return oldVersion.toProto().build();
        }
    }

    metadataManager.put(spec);
    return structDef.toProto().build();
  }

  @Override
  public Class<PutStructDefRequest> getProtoBaseClass() {
    return PutStructDefRequest.class;
  }
  
}
