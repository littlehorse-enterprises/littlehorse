package io.littlehorse.common.model.corecommand.subcommand;

import com.google.protobuf.Message;
import io.grpc.Status;
import io.littlehorse.common.LHSerializable;
import io.littlehorse.common.LHServerConfig;
import io.littlehorse.common.exceptions.LHApiException;
import io.littlehorse.common.exceptions.LHValidationException;
import io.littlehorse.common.model.corecommand.CoreSubCommand;
import io.littlehorse.common.model.getable.core.variable.VariableValueModel;
import io.littlehorse.common.model.getable.core.wfrun.InlineWfSpecModel;
import io.littlehorse.common.model.getable.core.wfrun.WfRunModel;
import io.littlehorse.common.model.getable.global.wfspec.WfSpecModel;
import io.littlehorse.common.model.getable.objectId.WfRunIdModel;
import io.littlehorse.common.util.LHUtil;
import io.littlehorse.sdk.common.proto.RunInlineWfRequest;
import io.littlehorse.sdk.common.proto.WfRun;
import io.littlehorse.server.streams.topology.core.CoreProcessorContext;
import io.littlehorse.server.streams.topology.core.ExecutionContext;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class RunInlineWfRequestModel extends CoreSubCommand<RunInlineWfRequest> {
    private String id;
    private InlineWfSpecModel definition;
    private final Map<String, VariableValueModel> variables = new HashMap<>();

    @Override
    public String getPartitionKey() {
        if (id == null) id = LHUtil.generateGuid();
        return id;
    }

    @Override
    public Class<RunInlineWfRequest> getProtoBaseClass() {
        return RunInlineWfRequest.class;
    }

    @Override
    public RunInlineWfRequest.Builder toProto() {
        RunInlineWfRequest.Builder out = RunInlineWfRequest.newBuilder();
        if (id != null) out.setId(id);
        if (definition != null) out.setWfSpec(definition.toProto());
        variables.forEach(
                (name, value) -> out.putVariables(name, value.toProto().build()));
        return out;
    }

    @Override
    public void initFrom(Message proto, ExecutionContext context) {
        RunInlineWfRequest request = (RunInlineWfRequest) proto;
        id = request.hasId() ? request.getId() : null;
        if (!request.hasWfSpec()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Missing required argument 'wf_spec'");
        }
        if (request.getWfSpec().hasId() || request.getWfSpec().hasCreatedAt()) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Inline id and created_at are server-managed");
        }
        definition = LHSerializable.fromProto(request.getWfSpec(), InlineWfSpecModel.class, context);
        request.getVariablesMap()
                .forEach((name, value) -> variables.put(name, VariableValueModel.fromProto(value, context)));
    }

    @Override
    public WfRun process(CoreProcessorContext context, LHServerConfig config) {
        if (id.isEmpty() || !LHUtil.isValidLHName(id)) {
            throw new LHApiException(Status.INVALID_ARGUMENT, "Optional argument 'id' must be a valid hostname");
        }
        WfRunIdModel runId = new WfRunIdModel(id);
        if (context.getableManager().get(runId) != null) {
            throw new LHApiException(Status.ALREADY_EXISTS, "WfRun with id " + id + " already exists!");
        }
        // Keep server-generated metadata out of the submitted command payload.
        InlineWfSpecModel inline =
                LHSerializable.fromProto(definition.toProto().build(), InlineWfSpecModel.class, context);
        WfSpecModel spec = inline.asWfSpecModel();
        try {
            spec.validateAndMaybeBumpVersion(Optional.empty(), context);
            spec.getEntrypointThread().validateStartVariables(variables, context.metadataManager());
        } catch (LHValidationException ex) {
            throw new LHApiException(Status.INVALID_ARGUMENT, ex.getMessage());
        }
        inline.setId(runId);
        inline.setCreatedAt(context.currentCommand().getTime());
        context.getableManager().put(inline);

        WfRunModel run = spec.startNewRun(runId, true, variables, context);
        run.advance(context.currentCommand().getTime());
        return run.toProto().build();
    }
}
