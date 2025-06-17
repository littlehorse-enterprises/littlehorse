package io.littlehorse.common.model.corecommand.subcommand;

import com.google.common.base.Strings;
import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHValidationError;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.ParentWfSpecReferenceModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.global.wfspec.thread.ThreadSpecModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.RunWfRequest;
import io.littlehorse.sdk.common.proto.VariableValue;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.storeinternals.GetableManager;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RunWfRequestModel extends CoreSubCommand<RunWfRequest> {

    private String wfSpecName;
    private Integer majorVersion;
    private Integer revision;
    private Map<String, VariableValueModel> variables;
    private String id;
    private WfRunIdModel parentWfRunId;

    public String getPartitionKey() {
        if (id == null) id = LHUtil.generateGuid();

        // Child wfrun needs access to state of parent, so it needs to be on the same partition
        if (parentWfRunId != null) {
            return parentWfRunId.getPartitionKey().get();
        }
        return id;
    }

    public Class<RunWfRequest> getProtoBaseClass() {
        return RunWfRequest.class;
    }

    public RunWfRequestModel() {
        variables = new HashMap<>();
    }

    public RunWfRequest.Builder toProto() {
        RunWfRequest.Builder out = RunWfRequest.newBuilder().setWfSpecName(wfSpecName);
        if (id != null) out.setId(id);
        if (majorVersion != null) out.setMajorVersion(majorVersion);
        if (revision != null) out.setRevision(revision);

        for (Map.Entry<String, VariableValueModel> e : variables.entrySet()) {
            out.putVariables(e.getKey(), e.getValue().toProto().build());
        }
        if (parentWfRunId != null) {
            out.setParentWfRunId(parentWfRunId.toProto());
        }
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        RunWfRequest p = (RunWfRequest) proto;
        wfSpecName = p.getWfSpecName();
        if (p.hasId()) id = p.getId();
        if (p.hasMajorVersion()) majorVersion = p.getMajorVersion();
        if (p.hasRevision()) revision = p.getRevision();

        for (Map.Entry<String, VariableValue> e : p.getVariablesMap().entrySet()) {
            variables.put(e.getKey(), VariableValueModel.fromProto(e.getValue(), context));
        }

        if (p.hasParentWfRunId()) {
            parentWfRunId = LHSerializable.fromProto(p.getParentWfRunId(), WfRunIdModel.class, context);
        }
    }

    private boolean isIdValid() {
        return (!id.equals("") && LHUtil.isValidLHName(id));
    }

    @Override
    public WfRun process(CoreProcessorContext processorContext, LHServerConfig config) {
        if (Strings.isNullOrEmpty(wfSpecName)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Missing required argument 'wf_spec_name'");
        }

        if (id != null && !this.isIdValid()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Optional argument 'id' must be a valid hostname");
        }

        GetableManager getableManager = processorContext.getableManager();
        WfSpecModel spec = processorContext.service().getWfSpec(wfSpecName, majorVersion, revision);
        if (spec == null) {
            throw new LHApiException(Status.NOT_FOUND, "Couldn't find specified WfSpec");
        }

        // TODO: Add WfRun Start Metrics

        WfRunModel oldWfRun = getableManager.get(new WfRunIdModel(id));
        if (oldWfRun != null) {
            throw new LHApiException(Status.ALREADY_EXISTS, "WfRun with id " + id + " already exists!");
        }

        // Validate the requests
        if (spec.getParentWfSpec() != null && parentWfRunId == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT,
                    "WfSpec %s requires a parent WfRun ID from WfSpec %s"
                            .formatted(wfSpecName, spec.getParentWfSpec().getWfSpecName()));
        }
        if (parentWfRunId != null && spec.getParentWfSpec() == null) {
            throw new LHApiException(
                    Status.INVALID_ARGUMENT, "WfSpec %s does not refer to a parent WfSpec.".formatted(wfSpecName));
        }

        // Validate that parent WfRun exists and is on this partition.
        if (parentWfRunId != null) {
            ParentWfSpecReferenceModel parentSpec = spec.getParentWfSpec();
            WfRunModel parent = processorContext.getableManager().get(parentWfRunId);
            if (parent == null) {
                throw new LHApiException(
                        Status.FAILED_PRECONDITION,
                        "Parent WfRun of type %s with id %s not found."
                                .formatted(parentSpec.getWfSpecName(), parentWfRunId.toString()));
            }
            if (!parent.getWfSpec().getName().equals(spec.getParentWfSpec().getWfSpecName())) {
                throw new LHApiException(
                        Status.INVALID_ARGUMENT,
                        "Parent WfRun is of incorrect type %s but should be %s"
                                .formatted(parent.getWfSpec().getName(), parentSpec.getWfSpecName()));
            }
        }

        // Validate input variables before saving anything.
        ThreadSpecModel entrypointThread = spec.getEntrypointThread();
        try {
            entrypointThread.validateStartVariables(variables);
        } catch (LHValidationError exn) {
            throw new LHApiException(Status.INVALID_ARGUMENT, exn.getMessage());
        }

        WfRunModel newRun = spec.startNewRun(this, processorContext);
        newRun.advance(processorContext.currentCommand().getTime());

        return newRun.toProto().build();
    }

    public static RunWfRequestModel fromProto(RunWfRequest p, ExecutionContext context) {
        RunWfRequestModel out = new RunWfRequestModel();
        out.initFrom(p, context);
        return out;
    }
}
