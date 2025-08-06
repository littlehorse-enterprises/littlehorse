package io.littlehorse.common.model.metadatacommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHConstants;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.validation.InvalidWfSpecException;
import io.littlehorse.common.model.getable.global.wfspec.ParentWfSpecReferenceModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.WorkflowRetentionPolicyModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.WfSpecIdModel;
import io.littlehorse.common.model.metadatacommand.MetadataSubCommand;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.common.util.WfSpecUtil;
import io.littlehorse.sdk.common.proto.AllowedUpdateType;
import io.littlehorse.sdk.common.proto.PutWfSpecRequest;
import io.littlehorse.sdk.common.proto.ThreadSpec;
import io.littlehorse.sdk.common.proto.WfSpec;
import io.littlehorse.server.streams.storeinternals.MetadataManager;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import io.littlehorse.server.streams.topology.core.MetadataProcessorContext;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import lombok.Getter;

@Getter
public class PutWfSpecRequestModel extends MetadataSubCommand<PutWfSpecRequest> {

    public String name;
    public Map<String, ThreadSpecModel> threadSpecs;
    public String entrypointThreadName;
    public WorkflowRetentionPolicyModel retentionPolicy;
    private ParentWfSpecReferenceModel parentWfSpec;
    public AllowedUpdateType allowedUpdateType;

    public String getPartitionKey() {
        return LHConstants.META_PARTITION_KEY;
    }

    public Class<PutWfSpecRequest> getProtoBaseClass() {
        return PutWfSpecRequest.class;
    }

    public PutWfSpecRequestModel() {
        threadSpecs = new HashMap<>();
    }

    public PutWfSpecRequest.Builder toProto() {
        PutWfSpecRequest.Builder out =
                PutWfSpecRequest.newBuilder().setName(name).setEntrypointThreadName(entrypointThreadName);
        if (retentionPolicy != null) {
            out.setRetentionPolicy(retentionPolicy.toProto());
        }

        out.setAllowedUpdates(allowedUpdateType);

        for (Map.Entry<String, ThreadSpecModel> e : threadSpecs.entrySet()) {
            out.putThreadSpecs(e.getKey(), e.getValue().toProto().build());
        }

        if (parentWfSpec != null) out.setParentWfSpec(parentWfSpec.toProto());
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        PutWfSpecRequest p = (PutWfSpecRequest) proto;
        name = p.getName();
        entrypointThreadName = p.getEntrypointThreadName();
        allowedUpdateType = p.getAllowedUpdates();
        if (p.hasRetentionPolicy())
            retentionPolicy =
                    LHSerializable.fromProto(p.getRetentionPolicy(), WorkflowRetentionPolicyModel.class, context);
        for (Map.Entry<String, ThreadSpec> e : p.getThreadSpecsMap().entrySet()) {
            threadSpecs.put(e.getKey(), ThreadSpecModel.fromProto(e.getValue(), context));
        }

        if (p.hasParentWfSpec()) {
            parentWfSpec = LHSerializable.fromProto(p.getParentWfSpec(), ParentWfSpecReferenceModel.class, context);
        }
    }

    @Override
    public WfSpec process(MetadataProcessorContext executionContext) {
        MetadataManager metadataManager = executionContext.metadataManager();
        if (!LHUtil.isValidLHName(name)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "WfSpecName must be a valid hostname");
        }

        WfSpecModel spec = new WfSpecModel(executionContext);
        spec.setId(new WfSpecIdModel(name, 0, 0)); // version gets set later, don't worry
        spec.entrypointThreadName = entrypointThreadName;
        spec.threadSpecs = threadSpecs;
        spec.createdAt = new Date();
        if (parentWfSpec != null) spec.setParentWfSpec(parentWfSpec);

        spec.setRetentionPolicy(retentionPolicy);
        for (Map.Entry<String, ThreadSpecModel> entry : spec.threadSpecs.entrySet()) {
            ThreadSpecModel tspec = entry.getValue();
            tspec.wfSpec = spec;
            tspec.name = entry.getKey();
        }

        WfSpecModel oldVersion = executionContext.service().getWfSpec(name, null, null);
        Optional<WfSpecModel> optWfSpec = oldVersion == null ? Optional.empty() : Optional.of(oldVersion);

        try {
            spec.validateAndMaybeBumpVersion(optWfSpec, executionContext);
        } catch (InvalidWfSpecException exn) {
            throw new LHApiException(Status.INVALID_ARGUMENT, exn.getMessage());
        }

        if (optWfSpec.isPresent() && WfSpecUtil.equals(spec, oldVersion)) {
            return optWfSpec.get().toProto().build();
        }

        verifyUpdateType(allowedUpdateType, spec, optWfSpec);
        metadataManager.put(spec);
        return spec.toProto().build();
    }

    private void verifyUpdateType(AllowedUpdateType updateType, WfSpecModel spec, Optional<WfSpecModel> oldSpec) {
        switch (updateType) {
            case MINOR_REVISION_UPDATES:
                if (oldSpec.isPresent() && WfSpecUtil.hasBreakingChanges(spec, oldSpec.get())) {
                    throw new LHApiException(Status.FAILED_PRECONDITION, "The resulting WfSpec has a breaking change.");
                }
                break;
            case NO_UPDATES:
                if (oldSpec.isPresent()) {
                    throw new LHApiException(Status.ALREADY_EXISTS, "WfSpec already exists.");
                }
                break;
            default:
                break;
        }
    }

    public static PutWfSpecRequestModel fromProto(PutWfSpecRequest p, ExecutionContext context) {
        PutWfSpecRequestModel out = new PutWfSpecRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
